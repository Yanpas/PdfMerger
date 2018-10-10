package yanpas.pdfmerger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

final class Merger implements AutoCloseable {
	private Deque<PDDocument> inDocuments = new ArrayDeque<>();
	private int outPagesN = 0;
	private PDDocumentOutline outOutline = new PDDocumentOutline();
	private PDDocument outDocument = new PDDocument();

	private void addOutlines(Iterable<PDOutlineItem> itemCollection, PDOutlineItem rootItem, PDDocument inDoc)
			throws IOException {
		for (PDOutlineItem item : itemCollection) {
			PDPage destPage = null;
			try {
				destPage = item.findDestinationPage(inDoc);
			} catch (IOException e) {
			}
			PDOutlineItem outItem = new PDOutlineItem();
			if (destPage != null) {
				PDPage itemDestPage = outDocument.getPages().get(inDoc.getPages().indexOf(destPage) + outPagesN);
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
			throw new IOException("File \"" + inFile.getAbsolutePath() + "\" seems to be non-pdf", e);
		}

		PDDocument inDoc = inDocuments.peek();
		int inPagesN = inDoc.getNumberOfPages();
		if (inPagesN < 1)
			return;
		String finname = inFile.getName();
		if (finname.length() > 4)
			finname = finname.substring(0, finname.length() - 4);
		PDDocumentOutline inOutline = inDoc.getDocumentCatalog().getDocumentOutline();

		for (int i = 0; i < inPagesN; ++i)
			outDocument.addPage(inDoc.getPage(i));

		PDOutlineItem outRoot = new PDOutlineItem();
		outRoot.setTitle(finname);
		outRoot.setDestination(outDocument.getPages().get(outPagesN));
		outOutline.addLast(outRoot);

		if (inOutline != null)
			addOutlines(inOutline.children(), outRoot, inDoc);

		outPagesN += inPagesN;
	}

	public void addDocument(File doc) throws IOException {
		appendDoc(doc);
	}

	public void save(String outname) throws IOException {
		outDocument.getDocumentCatalog().setDocumentOutline(outOutline);
		outDocument.save(outname);
	}

	@Override
	public void close() {
		try {
			while (inDocuments.size() != 0)
				inDocuments.pop().close();
			outDocument.close();
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}
}
