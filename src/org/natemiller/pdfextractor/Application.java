package org.natemiller.pdfextractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Application app = null;
		if (args.length == 2) {
			app = new Application(args[0], args[1]);
		} else {
			System.out.println("usage: pdfextractor FILENAME");
			System.exit(1);
		}

		app.exportTextAndImages();
		app.terminate();
		System.exit(0);
	}

	private PDDocument doc;
	private String outputPath;

	public Application(String filename, String outputPath) {
		this.outputPath = outputPath;

		try {
			doc = PDDocument.load(filename);
		} catch (IOException e) {
			System.err.println("Could not load " + filename);
			System.exit(1);
		}
	}

	public void exportTextAndImages() {
		String textInDocument = extractText();
		storeTextToFile(outputPath + File.separator + "output.txt",
				textInDocument);
		try {
			PDFImageExtractor imgExtractor = new PDFImageExtractor(doc);
			imgExtractor.exportImages(outputPath);
		} catch (IOException e) {
			System.err.println("Failed to load image extractor interface");
		}
	}

	private String extractText() {
		String text = "";
		try {
			PDFTextStripper stripper = new PDFTextStripper();
			text = stripper.getText(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}

	private void terminate() {
		try {
			doc.close();
		} catch (IOException e) {
			System.out.println("Failed to close document");
		}
	}

	private void storeTextToFile(String filename, String text) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(text);
			out.close();
			System.out.println("Text stored to " + filename);
		} catch (IOException e) {
			System.err.println("Failed to write to file");
		}
	}

}
