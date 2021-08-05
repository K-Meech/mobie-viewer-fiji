package de.embl.cba.mobie.n5.zarr;

import de.embl.cba.bdv.utils.CustomXmlIoSpimData;
import de.embl.cba.mobie.source.ImageDataFormat;
import de.embl.cba.tables.FileAndUrlUtils;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.SpimDataIOException;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;
import mpicbg.spim.data.sequence.XmlIoSequenceDescription;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static mpicbg.spim.data.XmlKeys.*;

@ImgLoaderIo(format = "bdv.ome.zarr", type = N5OMEZarrImageLoader.class)
public class XmlN5OmeZarrImageLoader implements XmlIoBasicImgLoader<N5OMEZarrImageLoader> {
    public static final String OmeZarr = "ome.zarr";

    @Override
    public Element toXml(final N5OMEZarrImageLoader imgLoader, final File basePath) {
        final Element elem = new Element("ImageLoader");
        elem.setAttribute(IMGLOADER_FORMAT_ATTRIBUTE_NAME, "bdv.ome.zarr");
        elem.setAttribute("version", "0.2");
        return elem;
    }

    @Override
    public N5OMEZarrImageLoader fromXml(Element elem, File basePath, AbstractSequenceDescription<?, ?, ?> sequenceDescription) {
        return null;
    }

    public static String getDatasetsPathFromXml(final Element parent, final String basePath) {
        final Element elem = parent.getChild(OmeZarr);
        if (elem == null)
            return null;
        final String path = elem.getText();
        final String pathType = elem.getAttributeValue("type");
        final boolean isRelative = null != pathType && pathType.equals("relative");
        if (isRelative) {
            if (basePath == null)
                return null;
            else {
                String xmlPath = basePath.toString();
                return xmlPath.substring(0, xmlPath.lastIndexOf('/')) + "/" + path;
            }
        } else
            return path;
    }
}