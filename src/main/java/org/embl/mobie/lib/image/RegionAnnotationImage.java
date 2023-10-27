/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2023 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.lib.image;

import bdv.viewer.Source;
import net.imglib2.Interval;
import net.imglib2.RealLocalizable;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.RealMaskRealInterval;
import net.imglib2.util.Intervals;
import org.embl.mobie.lib.DataStore;
import org.embl.mobie.lib.annotation.AnnotatedRegion;
import org.embl.mobie.lib.source.AnnotationType;
import org.embl.mobie.lib.source.RealRandomAccessibleIntervalTimelapseSource;
import org.embl.mobie.lib.table.AnnData;
import org.embl.mobie.lib.table.saw.TableSawAnnotatedRegion;
import org.embl.mobie.lib.transform.TransformHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class RegionAnnotationImage< AR extends AnnotatedRegion > implements AnnotationImage< AR >
{
	private final String name;

	private final AnnData< AR > annData;
	private final Set< Integer > timepoints;

	private Source< AnnotationType< AR > > source;
	private SourcePair< AnnotationType< AR > > sourcePair;
	private RealMaskRealInterval mask;

	private boolean debug = false;
	private List< AR > annotations;

	/**
	 * Builds an image that annotates all {@code AnnotatedRegion} in the
	 * provided {@code AnnData}.
	 *
	 * Note that currently the timepoints of the regions in annData are ignored.
	 * This could be changed (there are some comments in the code of this class
	 * for where and which changes would be needed). Instead, all the provided
	 * {@code timepoints} are annotated with all {@code AnnotatedRegion}.
	 * See https://github.com/mobie/mobie-viewer-fiji/issues/975
	 *
	 * @param name
	 * 				name of this image
	 * @param annData
	 * 				annData containing the regions that shall be annotated
	 * @param timepoints
	 * 				the timepoints that this image annotates
	 */
	public RegionAnnotationImage( String name, AnnData< AR > annData, Set< Integer > timepoints )
	{
		this.name = name;
		this.annData = annData;
		this.timepoints = timepoints;

		if( debug ) logRegions();
	}

	private void logRegions()
	{
		final ArrayList< AR > annotations = annData.getTable().annotations();
		for ( AR annotatedRegion : annotations )
		{
			final TableSawAnnotatedRegion tableSawAnnotatedRegion = ( TableSawAnnotatedRegion ) annotatedRegion;
			System.out.println( "RegionLabelImage " + name + ": " + annotatedRegion.regionId() + " images = " + Arrays.toString( tableSawAnnotatedRegion.getRegionImageNames().toArray( new String[ 0 ] ) ) + "\n" + TransformHelper.maskToString( annotatedRegion.getMask() ) );
			final List< String > regionImageNames = tableSawAnnotatedRegion.getRegionImageNames();
			for ( String regionImageName : regionImageNames )
			{
				final Image< ? > viewImage = DataStore.getImage( regionImageName );
				System.out.println( "Region: " + viewImage.getName() + ": " + Arrays.toString( viewImage.getMask().minAsDoubleArray() ) + " - " + Arrays.toString( viewImage.getMask().maxAsDoubleArray() ) );
			}
		}
	}

	@Override
	public AnnData< AR > getAnnData()
	{
		return annData;
	}

	class LocationToAnnotatedRegionSupplier implements Supplier< BiConsumer< RealLocalizable, AnnotationType< AR > > >
	{
		@Override
		public BiConsumer< RealLocalizable, AnnotationType< AR > > get()
		{
			return new LocationToRegion();
		}

		private class LocationToRegion implements BiConsumer< RealLocalizable, AnnotationType< AR > >
		{
			private AR recentAnnotation; // the annotation that was at the recent location

			public LocationToRegion()
			{
				this.recentAnnotation = annotations.get( 0 );
			}

			@Override
			public void accept( RealLocalizable location, AnnotationType< AR > value )
			{
				// It is likely that the next location
				// is within the same mask, thus we test that one first
				// to safe some computations.
				if ( recentAnnotation.getMask().test( location ) )
				{
					value.setAnnotation( recentAnnotation );
					return;
				}

				// It was not in the recent mask,
				// so we need to test all the others.
				for ( AR annotation : annotations )
				{
					if ( annotation == recentAnnotation )
						continue; // that one has been checked already above

					if ( annotation.getMask().test( location ) )
					{
						recentAnnotation = annotation;
						value.setAnnotation( recentAnnotation );
						return;
					}
				}

				// The location is not within any mask => it is background
				value.setAnnotation( null );
			}
		}
	}

	@Override
	public synchronized SourcePair< AnnotationType< AR > > getSourcePair()
	{
		if ( sourcePair == null )
		{
			final Interval interval = Intervals.smallestContainingInterval( getMask() );

			annotations = annData.getTable().annotations();

			// one could add a time point parameter to LocationToAnnotatedRegionSupplier
			// and then make a Map< Timepoint, regions > and modify RealRandomAccessibleIntervalTimelapseSource to consume this map
			final FunctionRealRandomAccessible< AnnotationType< AR > > regions = new FunctionRealRandomAccessible( 3, new LocationToAnnotatedRegionSupplier(), () -> new AnnotationType<>( annotations.get( 0 ) ) );

			// TODO it would be nice if this Source had the same voxel unit
			//   as the other sources, but that would mean touching one of the
			//   annotated images which could be expensive.
			source = new RealRandomAccessibleIntervalTimelapseSource<>( regions, interval, new AnnotationType<>( annData.getTable().annotations().get( 0 ) ), new AffineTransform3D(), name, true, timepoints );

			// There is no volatile implementation (yet), because the
			// {@code Source} should be fast enough,
			// and probably a volatile version would need an {@code CachedCellImg},
			// which would require deciding on a specific spatial sampling,
			// which is not nice because a {@code Region} is defined in real space.
			sourcePair = new DefaultSourcePair<>( source, null );
		}

		return sourcePair;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void transform( AffineTransform3D affineTransform3D )
	{
		// TODO
		//   The issue is that the regions should
		//   just represent the location of the annotated images!?
	}

	@Override
	public RealMaskRealInterval getMask( )
	{
		if ( mask == null )
		{
			mask = TransformHelper.unionBox( annData.getTable().annotations() );
		}

		return mask;
	}

	@Override
	public void setMask( RealMaskRealInterval mask )
	{
		throw new RuntimeException("Setting a mask of a " + this.getClass() + " is currently not supported.");
	}
}
