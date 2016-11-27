package yanpas.pdfmerger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

class Merger {
	private Stack<PDDocument> inDocuments = new Stack<>();
	private List<File> inFileList = new Vector<>();
	private int outPagesN = 0;
	private PDDocument outDocument;
	private PDDocumentOutline outOutline;

	public Merger(List<File> inFileList) {
		this.inFileList = inFileList;
		outDocument = new PDDocument();
		outOutline = new PDDocumentOutline();
	}

	private void addOutlines(Iterable<PDOutlineItem> itemCollection,
			PDOutlineItem rootItem, PDDocument inDoc) throws IOException {
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
				addOutlines(item.children(), outItem, inDoc);
		}
	}

	private void appendDoc(final File inFile) throws IOException {
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
			addOutlines(inOutline.children(), outRoot, inDoc);

		outPagesN += inPagesN;
	}

	private PDDocument getMerged() throws IOException {
		for (File doc : inFileList)
			this.appendDoc(doc);

		outDocument.getDocumentCatalog().setDocumentOutline(outOutline);
		return outDocument;
	}

	public void merge(String outname) throws IOException {
		try (PDDocument merged = getMerged()) {
			merged.save(outname);
		} catch(IOException e) {
			throw e;
		} finally {
			while (!inDocuments.empty())
				inDocuments.pop().close();
		}
	}
}