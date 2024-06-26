/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package org.embl.mobie.lib.source;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.render.DefaultMipmapOrdering;
import bdv.viewer.render.MipmapOrdering;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.*;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;



public class RealTransformedSource<T> implements Source<T>, MipmapOrdering, SourceWrapper< T >
{
    private final Source<T> source;

    private final String name;

    private final MipmapOrdering sourceMipmapOrdering;

    private final RealTransform realTransform;

    public RealTransformedSource(
            final Source<T> source,
            final RealTransform realTransform,
            final String name) {

        this.source = source;
        this.name = name;
        this.realTransform = realTransform;
        sourceMipmapOrdering =
                MipmapOrdering.class.isInstance(source) ?
                        (MipmapOrdering)source : new DefaultMipmapOrdering(source);
    }

    @Override
    public boolean isPresent(final int t) {

        return source.isPresent(t);
    }

    @Override
    public RandomAccessibleInterval<T> getSource( final int t, final int level ) {

        return source.getSource( 0, 0 );

//        return Views.interval(
//                Views.raster(
//                        getInterpolatedSource(
//                                t,
//                                level,
//                                Interpolation.NEARESTNEIGHBOR)),
//                estimateBoundingInterval(t, level));
    }

    private Interval estimateBoundingInterval( final int t, final int level ) {

        // TODO: this is currently ignoring the real transform, which may or may not be ok....
        final Interval wrappedInterval = source.getSource(t, level);
        AffineTransform3D sourceTransform = new AffineTransform3D();
        source.getSourceTransform( t, level, sourceTransform );
        Interval interval = Intervals.smallestContainingInterval( sourceTransform.estimateBounds( wrappedInterval ) );
        return interval;
    }

    @Override
    public RealRandomAccessible<T> getInterpolatedSource(
            final int t,
            final int level,
            final Interpolation method)
    {
        RealRandomAccessible< T > interpolatedSource = source.getInterpolatedSource( t, level, method );

        final AffineTransform3D sourceTransform = new AffineTransform3D();
        source.getSourceTransform( t, level, sourceTransform );

        final RealTransformSequence totalTransform = new RealTransformSequence();
        totalTransform.add( sourceTransform );
        totalTransform.add( realTransform );
        totalTransform.add( sourceTransform.inverse() );

        return new RealTransformRealRandomAccessible<>( interpolatedSource, totalTransform );
    }

    @Override
    public void getSourceTransform(final int t, final int level, final AffineTransform3D transform)
    {
        source.getSourceTransform( t, level, transform );
        if ( level == 0 )
        {
            int a = 1;
        }
    }

    @Override
    public T getType() {

        return source.getType();
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public VoxelDimensions getVoxelDimensions() {

        return source.getVoxelDimensions();
    }

    @Override
    public int getNumMipmapLevels() {

        return source.getNumMipmapLevels();
    }

    @Override
    public synchronized MipmapHints getMipmapHints(
            final AffineTransform3D screenTransform,
            final int timepoint,
            final int previousTimepoint) {

        return sourceMipmapOrdering.getMipmapHints(
                screenTransform,
                timepoint,
                previousTimepoint);
    }

    @Override
    public Source< T > getWrappedSource()
    {
        return source;
    }

    public RealTransform getRealTransform()
    {
        return realTransform;
    }
}
