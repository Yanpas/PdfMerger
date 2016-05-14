package yanpas.pdfmerger;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

class Merger
{
	private class Outliner
	{
		private PDDocument fromdoc;
		private PDPageTree intree;
		private int nowpages;
		public Outliner(PDDocument fromdoc, int nowpages)
		{
			this.fromdoc = fromdoc;
			this.nowpages = nowpages;
			intree = fromdoc.getPages();
		}
		public void addOutline(Iterable<PDOutlineItem> items, PDOutlineItem rootitem)
		{
			for (PDOutlineItem item : items)
			{
				PDPage dest = new PDPage();
				try	{
					dest = item.findDestinationPage(fromdoc);
				} catch(Throwable e) {
					System.err.println(e.getMessage());
					System.err.println("Unable to get destination page of item <<" + item.getTitle() +">>");
					continue;
				}
				if (dest == null)
				{
					System.err.println("Outline item <<"+item.getTitle()+">> doesn't refer anywhere");
					dest = outcome.getPage(0);
					continue;
				}
				
				int i_dest = intree.indexOf(dest) + this.nowpages;
				String destname = item.getTitle();
				PDPage toinsert = outcome.getPages().get(i_dest);
				
				PDOutlineItem outtoinsert = new PDOutlineItem();
				outtoinsert.setDestination(toinsert);
				outtoinsert.setTitle(destname);
				rootitem.addLast(outtoinsert);
				
				if (item.hasChildren())
					addOutline(item.children(), outtoinsert);
			}
		}
	}
	private List <File> files = new Vector <File>();
	private Stack <PDDocument> inputstack = new Stack <PDDocument>();
	private PDDocument outcome; 
	private PDDocumentOutline outoutl;
	
	private void appendDoc(final File finput) throws IOException
	{
		try {
			inputstack.push (PDDocument.load(finput));
		} catch (IOException e)	{
			throw new IOException(){
				
				@Override
				public String getMessage()
				{
					return "File "+finput.getAbsolutePath()+" seems to be non-pdf";
				}
				static final long serialVersionUID = 123l;
			};
		} 
		
		PDDocument input = inputstack.peek();
		int inpages = input.getNumberOfPages();
		if (inpages < 1)
			return;
		int nowpages = outcome.getNumberOfPages();
		String finname = finput.getName();
		if (finname.length() > 4)
			finname = finname.substring(0,finname.length()-4);
		PDDocumentOutline inoutl = input.getDocumentCatalog().getDocumentOutline();
				
		for (int i=0; i<inpages; ++i)
			outcome.addPage(input.getPage(i));
		
		PDOutlineItem root = new PDOutlineItem();
		root.setTitle(finname);
		root.setDestination(outcome.getPages().get(nowpages));
		outoutl.addLast(root);
		
		if (inoutl != null) 
		{
			Outliner outs = new Outliner(input, nowpages);
			outs.addOutline(inoutl.children(), root);
		}	
	}
	public Merger(List <File> inlist)
	{
		files = inlist;
		outcome = new PDDocument();
		outoutl = new PDDocumentOutline();
	}
	public PDDocument merge() throws IOException
	{
		for (File doc : files)
			this.appendDoc(doc);
		
		outcome.getDocumentCatalog().setDocumentOutline(outoutl);
		return outcome;
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		while (! inputstack.empty())
		{
			PDDocument tmp = inputstack.pop();
			tmp.close();
		}
		super.finalize();
	}
}