package org.natemiller.pdfextractor;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;

public class PDFImageExtractor extends PDFStreamEngine {

	private PDDocument doc;

	private LinkedList<PDXObjectImage> images;

	public PDFImageExtractor(PDDocument doc) throws IOException {
		super(ResourceLoader.loadProperties(
				"org/apache/pdfbox/resources/PDFTextStripper.properties", true));
		this.images = new LinkedList<PDXObjectImage>();
		this.doc = doc;
	}

	public void exportImages(String outputPath) {
		ArrayList<PDPage> allPages = (ArrayList<PDPage>) doc.getDocumentCatalog().getAllPages();
		for (int i = 0; i < allPages.size(); i++) {
			PDPage page = (PDPage) allPages.get(i);
			tryProcessPage(page);
		}

		int serial = 1;
		DecimalFormat formatter = new DecimalFormat("0000");
		ListIterator<PDXObjectImage> itr = images.listIterator();
		while (itr.hasNext()) {
			PDXObjectImage img = itr.next();
			String imgName = "img_" + formatter.format(serial);
			String filePath = outputPath + File.separator + imgName;

			try {
				img.write2file(filePath);
			} catch (IOException e) {
				System.out.println("Write failed on " + imgName);
			}
			
			serial++;
		}
	}

	private void tryProcessPage(PDPage page) {
		try {
			processStream(page, page.findResources(), page.getContents()
					.getStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void processOperator(PDFOperator operator, List arguments)
			throws IOException {
		String operation = operator.getOperation();
		if (operation.equals("Do")) {
			System.err.println(arguments.toString());
			COSName objectName = (COSName) arguments.get(0);
			Map xobjects = getResources().getXObjects();
			PDXObject xobject = (PDXObject) xobjects.get(objectName.getName());

			if (xobject instanceof PDXObjectImage) {
				PDXObjectImage image = (PDXObjectImage) xobject;
				images.push(image);
			} else {
				super.processOperator(operator, arguments);
			}
		}
	}

}
