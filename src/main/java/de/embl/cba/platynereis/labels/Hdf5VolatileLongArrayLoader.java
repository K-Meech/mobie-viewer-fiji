package de.embl.cba.platynereis.labels;

import bdv.img.cache.CacheArrayLoader;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;

public class Hdf5VolatileLongArrayLoader implements CacheArrayLoader< VolatileShortArray >
{
	private final IHDF5ExtendedAccess hdf5Access;

	public Hdf5VolatileLongArrayLoader( final IHDF5ExtendedAccess hdf5Access )
	{
		this.hdf5Access = hdf5Access;
	}

	@Override
	public VolatileShortArray loadArray( final int timepoint, final int setup, final int level, final int[] dimensions, final long[] min ) throws InterruptedException
	{
		final short[] array = hdf5Access.readShortMDArrayBlockWithOffset( timepoint, setup, level, dimensions, min );
		return new VolatileShortArray( array, true );
	}

	@Override
	public int getBytesPerElement()
	{
		return 2;
	}

//	PrintStream log = System.out;
//	public static volatile long pStart = System.currentTimeMillis();
//	public static volatile long pEnd = System.currentTimeMillis();
//	public static volatile long tLoad = 0;
//	public static volatile long sLoad = 0;
//
//	@Override
//	public VolatileShortArray loadArray( final int timepoint, final int setup, final int level, final int[] dimensions, final long[] min ) throws InterruptedException
//	{
//		final short[] array;
//
//		pStart = System.currentTimeMillis();
//		final long msBetweenLoads = pStart - pEnd;
//		if ( msBetweenLoads > 2 )
//		{
//			log.println( msBetweenLoads + " ms pause before this load." );
//			final StringWriter sw = new StringWriter();
//			final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
//			for ( final StackTraceElement elem : trace )
//				sw.write( elem.getClassName() + "." + elem.getMethodName() + "\n" );
//			log.println( sw.toString() );
//		}
//		final long t0 = System.currentTimeMillis();
//		array = hdf5Access.readShortMDArrayBlockWithOffset( timepoint, setup, level, dimensions, min );
//		pEnd = System.currentTimeMillis();
//		final long t = System.currentTimeMillis() - t0;
//		final long size = array.length;
//		tLoad += t;
//		sLoad += size;
//		if ( sLoad > 10000000 )
//		{
//			final double megPerSec = sLoad * 2000.0 / ( 1024.0 * 1024.0 * tLoad ); // megabytes read per second
//			log.println( String.format( "%.0f mb/sec ", megPerSec ) );
//			tLoad = 1;
//			sLoad = 1;
//		}
//
//		return new VolatileShortArray( array, true );
//	}
}

