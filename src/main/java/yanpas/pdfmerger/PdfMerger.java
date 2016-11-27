package yanpas.pdfmerger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfMerger {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("CLI usage:\tpdfmerger file1.pdf file2.pdf ... out.pdf");
			SwingMerge gui = new SwingMerge();
			gui.setVisible(true);
		} else {
			List<File> infiles = new ArrayList<>();
			String outname = args[args.length - 1];
			if (new File(outname).exists()) {
				System.err.println("The last argument must be output file name");
				System.exit(1);
			}
			for (int i = 0; i < args.length - 1; i++) {
				File tmp = new File(args[i]);
				if (!tmp.exists()) {
					System.err.println("File " + args[i] + " does not exist");
					System.exit(1);
				}
				infiles.add(tmp);
			}

			try {
				new Merger().merge(infiles, outname);
			} catch (IOException e){
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}