package org.embl.mobie.cmd;

import org.embl.mobie.lib.MoBIE;
import org.embl.mobie.lib.MoBIESettings;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "mobie", mixinStandardHelpOptions = true, version = "3.0.13", description = "Visualise multi-modal big image data, see https://mobie.github.io/")
public class MoBIECmd implements Callable<Void> {

	// FIXME: https://github.com/mobie/mobie-viewer-fiji/issues/926

	@Option(names = {"-p", "--project"}, required = false, description = "open a MoBIE project, e.g. -p \"https://github.com/mobie/platybrowser-datasets\"")
	public String project = null;

	@Option(names = {"-v", "--view"}, required = false, description = "open a specific view within the above MoBIE project, e.g. -v \"cells")
	public String view = null;

	@Option(names = {"-i", "--image"}, required = false, description = "open an intensity image from a path, e.g., -i \"/home/image.tif\"; you can use wild-cards to open several images, e.g., -i \"/home/*-image.tif\"")
	public String[] images = null;

	@Option(names = {"-s", "--segmentation"}, required = false, description = "opens a segmentation label mask image from a path, e.g. -s \"/home/labels.tif\"; wild cards are supported (see --image)")
	public String[] segmentations = null;

	@Option(names = {"-t", "--table"}, required = false, description = "opens a segment feature table from a path, e.g. -t \"/home/features.csv\"; wild cards are supported (see --image)")
	public String[] tables = null;

	@Option(names = {"-g", "--grid"}, required = false, description = "create a grid view")
	public Boolean grid = true;

	@Override
	public Void call() throws Exception {

		if ( project == null && images == null && segmentations == null )
		{
			System.out.println( "Please either provide a project (-p), or an image (-i) and/or a segmentation (-s).");
			System.exit( 1 );
		}

		MoBIE.openedFromCLI = true;

		if ( project != null )
		{
			final MoBIESettings settings = new MoBIESettings();
			if ( view != null ) settings.view( view );
			new MoBIE( project, settings );
		}
		else
		{
			new MoBIE( "", images, segmentations, tables, grid );
		}

		return null;
	}

	public static final void main( final String... args ) {

		final MoBIECmd moBIECmd = new MoBIECmd();

		if ( args == null || args.length == 0 )
			new CommandLine( moBIECmd ).execute( "--help" );
		else
			new CommandLine( moBIECmd ).execute( args );
	}
}