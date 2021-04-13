package de.embl.cba.mobie2;

import de.embl.cba.mobie2.source.SourceSupplier;
import de.embl.cba.mobie2.serialize.View;

import java.util.Map;

public class Dataset
{
	public boolean is2D = false;
	public int timepoints = 1;
	public Map< String, SourceSupplier > sources;
	public Map< String, View > views;
}
