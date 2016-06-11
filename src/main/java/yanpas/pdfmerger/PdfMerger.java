package yanpas.pdfmerger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfMerger
{
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("CLI usage:\tpdfmerger file1.pdf file2.pdf ... out.pdf");
			SwingMerge gui = new SwingMerge();
			gui.setVisible(true);
			return;
		}
		List <File> infiles = new Vector<File>();
		String outname = null;
		for(int i=0; i<args.length; i++)
		{
			File tmp = new File (args[i]);
			if (!tmp.exists())
			{
				if (i == args.length-1 && i > 0)
				{
					outname = args[i];
					break;
				}
				else 
				{
					System.err.println( (args[i].charAt(0) == '/' ? "File "+System.getProperty("user.dir")+"/" : "")
							+args[i]+" does not exists");
					System.exit(2);
				}
			}
			infiles.add(tmp);
		}
		if (outname == null)
		{
			System.err.println("The last argument must be output file name");
			System.exit(2);
		}
		Merger merger = new Merger(infiles);
		PDDocument result = null;
		try {
			result = merger.merge();
		} catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
		}
		try	{
			result.save(outname);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		try {
			result.close();
			merger.closeAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}