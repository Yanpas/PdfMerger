package yanpas.pdfmerger;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

class Merger
{
	private class Outliner
	{
		private class DirtyWorkaround implements Comparable <DirtyWorkaround> //pdfbox returns sometimes circled list...
		{
			public Integer n;
			public String t;
			public PDPage p;
			public DirtyWorkaround(Integer n, String t, PDPage p)
			{
				this.n = n;
				this.t = t;
				this.p = p;
			}
			public int compareTo(DirtyWorkaround rhs)
			{
				if (n.equals(rhs.n) && t.equals(rhs.t) && p.equals(rhs.p))
					return 0;
				else
					return -1;
			}
		}
		private PDDocument fromdoc;
		private PDPageTree intree;
		private int nowpages;
		private Set <DirtyWorkaround> dirtymap;
		public Outliner(PDDocument fromdoc, int nowpages)
		{
			this.fromdoc = fromdoc;
			this.nowpages = nowpages;
			intree = fromdoc.getPages();
			dirtymap = new TreeSet<DirtyWorkaround>();
		}
		public void addOutline(Iterable<PDOutlineItem> items, PDOutlineItem rootitem) throws Exception
		{
			for (PDOutlineItem item : items)
			{
				DirtyWorkaround item_dw = new DirtyWorkaround(intree.indexOf(item.findDestinationPage(fromdoc)),
						item.getTitle(), item.findDestinationPage(fromdoc));
				if (dirtymap.contains(item_dw))
					break;
				dirtymap.add(item_dw);
				
				PDPage dest = item.findDestinationPage(fromdoc);
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
	private PDDocument outcome; 
	private PDDocumentOutline outoutl;
	
	private void appendDoc(File finput) throws Exception
	{
		PDDocument input = null; //
		try {
			input = PDDocument.load(finput);
		} catch (java.io.IOException e)	{
			System.err.println(e.getMessage());
			System.exit(1);
		} finally {	}
		
		int inpages = input.getNumberOfPages(); //
		if (inpages == 0)
			return;
		int nowpages = outcome.getNumberOfPages(); //
		String finname = finput.getName(); //
		if (finname.length() > 4)
			finname = finname.substring(0,finname.length()-4);
		PDDocumentOutline inoutl = input.getDocumentCatalog().getDocumentOutline(); //
				
		for (int i=0; i<inpages; ++i)
			outcome.addPage(input.getPage(i));
		
		PDOutlineItem root = new PDOutlineItem(); //
		root.setTitle(finname);
		root.setDestination(outcome.getPages().get(nowpages));
		outoutl.addLast(root);
		
		
		if (inoutl != null) 
		{
			Outliner outs = new Outliner(input, nowpages);
			outs.addOutline(inoutl.children(), root);
		}
		
	}
	public Merger(List <File> inlist) throws Exception
	{
		files = inlist;
		try {
			outcome = new PDDocument();
		} finally {
			//System.err.println("Can't create ouctome");
			//System.exit(0);
		}
		outoutl = new PDDocumentOutline();
	}
	public PDDocument merge() throws Exception
	{
		for (File doc : files)
		{
			this.appendDoc(doc);
		}
		outcome.getDocumentCatalog().setDocumentOutline(outoutl);
		return outcome;
	}
}