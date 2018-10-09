package yanpas.pdfmerger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfMerger {
	public static void main(String[] args) {
		if (args.length == 0) {
			SwingMerge gui = new SwingMerge();
			gui.setVisible(true);
			return;
		}

		List<File> infiles = new ArrayList<>();
		boolean progress = false;
		String outname = null;
		int i = 0;

		for (String arg : args) {
			switch (arg) {
			case "-v":
			case "--version":
				System.out.println(PdfMerger.class.getPackage().getImplementationVersion());
				return;
			case "-h":
			case "--help":
				System.out.println("Usage: pdfmerger file1.pdf file2.pdf ... out.pdf");
				System.out.println("Run pdfmerger without arguments to launch GUI");
				System.out.println("  -h | --help  Print this message and exit");
				System.out.println("  -v | --version  Print version and exit");
				System.out.println("  --progress  Log progress to stdout");
				return;
			case "--progress":
				progress = true;
				break;
			default:
				File targetFile = new File(arg);
				if (i == args.length - 1) {
					if (targetFile.exists()) {
						System.err.println("The last argument must be output file name");
						System.exit(1);
					}
					outname = arg;
					break;
				}
				if (!targetFile.exists()) {
					System.err.println("File " + arg + " does not exist");
					System.exit(1);
				}
				infiles.add(targetFile);
				break;
			}
			i++;
		}

		if (infiles.isEmpty()) {
			System.err.println("No input files supplied (last file should be output file)");
			System.exit(1);
		}

		try (Merger m = new Merger()) {
			for (File file : infiles) {
				if (progress)
					System.out.println("Processing " + file.getName());
				m.addDocument(file);
			}
			if (progress)
				System.out.println("Saving");
			m.save(outname);
		} catch (IOException e) {
			System.err.println("Error: " + e.getLocalizedMessage());
			System.err.println("Stack trace:");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
