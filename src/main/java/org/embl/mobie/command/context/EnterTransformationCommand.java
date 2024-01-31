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
package org.embl.mobie.command.context;

import bdv.tools.transformation.TransformedSource;
import bdv.util.BdvHandle;
import bdv.viewer.SourceAndConverter;
import ij.IJ;
import net.imglib2.realtransform.AffineTransform3D;
import org.embl.mobie.command.CommandConstants;
import org.embl.mobie.lib.MoBIEHelper;
import org.embl.mobie.lib.serialize.transformation.AffineTransformation;
import org.embl.mobie.lib.transform.TransformationMode;
import org.embl.mobie.lib.view.ViewManager;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;
import sc.fiji.bdvpg.scijava.command.BdvPlaygroundActionCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(type = BdvPlaygroundActionCommand.class, menuPath = CommandConstants.CONTEXT_MENU_ITEMS_ROOT + "Transform>Registration - Enter Transformation")
public class EnterTransformationCommand extends AbstractRegistrationCommand
{
	static { net.imagej.patcher.LegacyInjector.preinit(); }

	@Parameter
	public BdvHandle bdvHandle;

	@Parameter ( label = "Transformation 3D affine" )
	public String transformation = Arrays.toString( new AffineTransform3D().getRowPackedCopy() );

	@Parameter ( label = "Preview", callback = "preview" )
	public Button preview;


	@Override
	public void initialize()
	{
		super.initialize();
	}

	@Override
	public void run()
	{
		transform( false );
	}

	private void apply()
	{
		transform( true );
	}

	private void transform( boolean preview )
	{
		AffineTransform3D affineTransform3D = new AffineTransform3D();
		affineTransform3D.set( parseStringToDoubleArray( transformation ) );

		if ( preview || mode.equals( TransformationMode.InPlace ) )
		{
			AffineTransform3D newTransform = originalTransform.copy().preConcatenate( affineTransform3D.copy() );
			movingSource.setFixedTransform( newTransform );
			bdvHandle.getViewerPanel().requestRepaint();
		}
		else if ( mode.equals( TransformationMode.NewImage ) )
		{
			createTransformedImage( affineTransform3D, "Entered affine" );
		}
	}

	public static double[] parseStringToDoubleArray(String arrayStr)
	{
		arrayStr = arrayStr.replaceAll("\\[|\\]", "");
		String[] items = arrayStr.split(",\\s*");
		double[] doubles = Arrays.stream(items).mapToDouble(Double::parseDouble).toArray();
		return doubles;
	}
}
