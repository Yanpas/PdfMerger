//package pdfmerger;

import java.util.*;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.*;

class Merger
{
	class Outliner
	{
		private PDDocument fromdoc;
		private PDOutlineItem root;
		private PDPageTree intree;
		private int nowpages;
		public Outliner(PDDocument fromdoc, PDOutlineItem root, int nowpages)
		{
			this.fromdoc = fromdoc;
			this.root = root;
			intree = fromdoc.getPages();
			this.nowpages = nowpages;
		}
		public void addAllOutlines(Iterable<PDOutlineItem> initems) throws Exception
		{
			for (PDOutlineItem item : initems)
			{
				PDPage dest = null;
				try { dest = item.findDestinationPage(fromdoc); }
					finally { System.err.println("Can't find Destination Page"); }
				
				int i_dest = intree.indexOf(dest) + this.nowpages;
				String destname = item.getTitle();
				PDPage toinsert = outcome.getPages().get(i_dest);
				PDOutlineItem outtoinsert = new PDOutlineItem();
				outtoinsert.setDestination(toinsert);
				outtoinsert.setTitle(destname);
				root.addLast(outtoinsert);
				if (item.hasChildren()) addOutline(item.getFirstChild());
			}
		}
		private void addOutline(PDOutlineItem item) throws Exception
		{
			if (item == null) return;
			PDPage dest = null;
			try { dest = item.findDestinationPage(fromdoc); }
				finally { System.err.println("Can't find Destination Page"); }
			
			int i_dest = intree.indexOf(dest) + this.nowpages;
			String destname = item.getTitle();
			PDPage toinsert = outcome.getPages().get(i_dest);
			PDOutlineItem outtoinsert = new PDOutlineItem();
			outtoinsert.setDestination(toinsert);
			outtoinsert.setTitle(destname);
			root.addLast(outtoinsert);
			if (item.hasChildren()) addOutline(item.getFirstChild());
			addOutline (item.getNextSibling());
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
		
		PDOutlineItem lastroot = new PDOutlineItem(); //
		lastroot.setTitle(finname);
		lastroot.setDestination(outcome.getPages().get(nowpages));
		outoutl.addLast(lastroot);
		
		
		if (inoutl != null) 
		{
			Outliner outs = new Outliner(input, lastroot, nowpages);
			outs.addAllOutlines(inoutl.children());
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





public class PdfMerger
{
	public static void main(String[] args) throws Exception
	{
		System.err.println("We are here" +System.getProperty("user.dir"));
		//args = new String[]{"doc1.pdf","doc2.pdf"};
		if (args.length == 0)
		{
			System.err.println("Usage:\tpdfmerger file1.pdf file2.pdf ... [out.pdf]");
			System.exit(0);
		}
		List <File> infiles = new Vector<File>();
		String outname = null;
		for(int i=0; i<args.length; i++)
		{
			File tmp = new File (args[i]);
			if (!tmp.exists())
			{
				if (i == args.length-1 && i>0)
				{
					outname = args[i];
					break;
				}
				else 
				{
					System.err.println("File "+args[i]+" does not exists");
					System.exit(1);
				}
			}
			//tmp.setReadOnly();
			infiles.add(tmp);
		}
		
		Merger result = new Merger(infiles);
		PDDocument merged;
		try 
		{
			merged = result.merge();
		}
		finally{
			System.err.println("main unknown exception 1");
		}
		if (outname == null)
		{
			Date now = new Date();
			outname = now.toString().substring(4,19);
		}
		outname ="merged "+ outname +".pdf";
		try
		{
			merged.save(outname);
		}
		catch (java.io.IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		finally
		{
			System.err.println("main unknown exception 2");
			//System.exit(1);
		}
	}
}