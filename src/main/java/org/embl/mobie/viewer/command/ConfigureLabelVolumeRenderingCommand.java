package org.embl.mobie.viewer.command;

import bdv.viewer.SourceAndConverter;
import org.embl.mobie.viewer.volume.SegmentsVolumeViewer;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.bdvpg.scijava.ScijavaBdvDefaults;
import sc.fiji.bdvpg.scijava.command.BdvPlaygroundActionCommand;
import sc.fiji.bdvpg.scijava.services.SourceAndConverterService;
import sc.fiji.bdvpg.services.SourceAndConverterServices;

import static org.scijava.ItemVisibility.MESSAGE;

@Plugin(type = BdvPlaygroundActionCommand.class, menuPath = CommandConstants.CONTEXT_MENU_ITEMS_ROOT + "Display>Configure Label Volume Rendering")
public class ConfigureLabelVolumeRenderingCommand implements BdvPlaygroundActionCommand
{
	@Parameter
	public SourceAndConverter[] sourceAndConverters;

	@Parameter ( label = "Resolution X", style="format:#.000" )
	public double sx;

	@Parameter ( label = "Resolution Y", style="format:#.000" )
	public double sy;

	@Parameter ( label = "Resolution Z", style="format:#.000" )
	public double sz;

	@Parameter ( visibility = MESSAGE )
	String msg = "( Resolution units: see BigDataViewer scale bar )";

	@Parameter ( label = "Repaint segments")
	public boolean repaint;

	@Override
	public void run()
	{
		setVoxelSpacing( sourceAndConverters, new double[]{ sx, sy, sz }, repaint );
	}

	private void setVoxelSpacing( SourceAndConverter[] sourceAndConverters, double[] voxelSpacing, boolean repaint )
	{
		final SourceAndConverterService sacService = ( SourceAndConverterService ) SourceAndConverterServices.getSourceAndConverterService();

		for ( SourceAndConverter sourceAndConverter : sourceAndConverters )
		{
			final SegmentsVolumeViewer volumeViewer = ( SegmentsVolumeViewer ) sacService.getMetadata( sourceAndConverter, SegmentsVolumeViewer.class.getName() );
			if ( volumeViewer != null )
			{
				volumeViewer.setVoxelSpacing( voxelSpacing );
				if ( repaint )
					volumeViewer.updateView( true );
			}
		}
	}
}