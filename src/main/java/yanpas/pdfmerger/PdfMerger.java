package yanpas.pdfmerger;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfMerger
{
	public static void main(String[] args) throws Exception
	{
		if (args.length == 0)
		{
			System.err.println("Usage:\tpdfmerger file1.pdf file2.pdf ... out.pdf");
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
					System.err.println( (args[i].charAt(0) == '/' ? "File "+System.getProperty("user.dir")+"/" : "")
							+args[i]+" does not exists");
					System.exit(1);
				}
			}
			infiles.add(tmp);
		}
		if (outname == null)
		{
			System.err.println("The last argument must be output file name");
			System.exit(1);
		}
		Merger result = new Merger(infiles);
		PDDocument merged = result.merge();
		try	{
			merged.save(outname);
		} catch (java.io.IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		merged.close();
	}
}