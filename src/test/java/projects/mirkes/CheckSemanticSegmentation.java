package projects.mirkes;

import net.imagej.ImageJ;
import org.embl.mobie.command.open.OpenImagesAndSegmentationCommand;

import java.io.File;

class CheckSemanticSegmentation
{
	public static void main( String[] args ) throws Exception
	{
		new ImageJ().ui().showUI();
		final OpenImagesAndSegmentationCommand command = new OpenImagesAndSegmentationCommand();
		command.image0 = new File("/Volumes/cba/exchange/kristina-mirkes/data-test/processed/.*--pro.tif");
		command.image1 = new File("/Volumes/cba/exchange/kristina-mirkes/data-test/processed/.*--pro_prob_worm.tif");
		command.run();
	}
}