package yanpas.pdfmerger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

class Merger {
	private Stack<PDDocument> inDocuments;
	private int outPagesN;
	private PDDocumentOutline outOutline;

	private void addOutlines(Iterable<PDOutlineItem> itemCollection,
			PDOutlineItem rootItem, PDDocument inDoc, PDDocument outDocument) throws IOException {
		for (PDOutlineItem item : itemCollection) {
			PDPage destPage = null;
			destPage = item.findDestinationPage(inDoc);
			PDOutlineItem outItem = new PDOutlineItem();
			if (destPage != null) {
				PDPage itemDestPage =
						outDocument.getPages().get(inDoc.getPages().indexOf(destPage) + outPagesN);
				outItem.setDestination(itemDestPage);
			}
			outItem.setTitle(item.getTitle());
			rootItem.addLast(outItem);

			if (item.hasChildren())
				addOutlines(item.children(), outItem, inDoc, outDocument);
		}
	}

	private void appendDoc(final File inFile, PDDocument outDocument) throws IOException {
		try {
			inDocuments.push(PDDocument.load(inFile));
		} catch (IOException e) {
			throw new IOException("File " + inFile.getAbsolutePath() + " seems to be non-pdf");
		}

		PDDocument inDoc = inDocuments.peek();
		int inPagesN = inDoc.getNumberOfPages();
		if (inPagesN < 1)
			return;
		String finname = inFile.getName();
		if (finname.length() > 4)
			finname = finname.substring(0,finname.length()-4);
		PDDocumentOutline inOutline = inDoc.getDocumentCatalog().getDocumentOutline();

		for (int i=0; i<inPagesN; ++i)
			outDocument.addPage(inDoc.getPage(i));

		PDOutlineItem outRoot = new PDOutlineItem();
		outRoot.setTitle(finname);
		outRoot.setDestination(outDocument.getPages().get(outPagesN));
		outOutline.addLast(outRoot);

		if (inOutline != null)
			addOutlines(inOutline.children(), outRoot, inDoc, outDocument);

		outPagesN += inPagesN;
	}

	public void merge(List<File> inFileList, String outname) throws IOException {
		outPagesN = 0;
		outOutline = new PDDocumentOutline();
		inDocuments = new Stack<>();
		try (PDDocument outDocument = new PDDocument()) {
			for (File doc : inFileList)
				this.appendDoc(doc, outDocument);

			outDocument.getDocumentCatalog().setDocumentOutline(outOutline);
			outDocument.save(outname);
		} finally {
			while (!inDocuments.empty())
				inDocuments.pop().close();
		}
	}
}