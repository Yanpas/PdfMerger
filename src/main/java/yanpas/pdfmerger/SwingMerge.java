package yanpas.pdfmerger;

import javax.swing.*;

import java.io.File;
import java.util.List;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

class SwingMerge extends JFrame {
	private class Worker extends SwingWorker<Void, String> {
		public static final String MERGING = "Merging", SAVING = "Saving";
		private final File[] fileList;
		private final String outFile;
		private String result = "Cancelled";
		private boolean successful = false;

		public Worker(File[] fileList, String outFile) {
			this.fileList = fileList;
			this.outFile = outFile;
		}

		@Override
		public Void doInBackground() {
			String documentProcessed = null;
			try (Merger m = new Merger()) {
				publish(MERGING);
				double i = 0;
				for (File fl : fileList) {
					documentProcessed = fl.getName();
					m.addDocument(fl);
					i++;
					setProgress((int) (100 * i / (double) fileList.length));
				}
				publish(SAVING);
				documentProcessed = outFile;
				m.save(outFile);
			} catch (Exception e) {
				System.err.printf("Merging failed on %s: %s\n", documentProcessed, e);
				e.printStackTrace(System.err);
				result = String.format("Error during processing of %s: %s", documentProcessed, e.getLocalizedMessage());
				return null;
			}
			successful = true;
			result = "Done!\nThanks for using PDF Merger. Project's home page:\n"
					+ "https://github.com/Yanpas/PdfMerger";
			return null;
		}

		@Override
		public void done() {
			progressDialog.setVisible(false);
			progressBar.setIndeterminate(false);
			JOptionPane.showMessageDialog(SwingMerge.this, result, (successful ? "Operation finished" : "Error"),
					(successful ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
		}

		@Override
		protected void process(final List<String> chunks) {
			for (final String string : chunks) {
				progressDialog.setTitle(string);
				if (string == Worker.SAVING) {
					progressBar.setIndeterminate(true);
				}
			}
		}
	}

	private JScrollPane scrpane;
	private DefaultListModel<File> flistModel;
	private JList<File> fstringList;
	private JPanel buttonPanel;
	private JButton moveUpButton, addButton, removeButton, moveDownButton, mergeButton;

	private JDialog progressDialog;
	private JProgressBar progressBar;
	private JButton cancelButton;

	public SwingMerge() {
		try {
			if (System.getProperty("os.name").equals("Linux")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			System.err.println("Failed to set LAF: " + e.getMessage());
		}
		setTitle("PDF Merger");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.placeAllElements();
		pack();
		setMinimumSize(new Dimension(500, 300));
		this.addEvents();
		this.createProgressdialog();
	}

	private final void placeAllElements() {
		moveUpButton = new JButton("Up");
		addButton = new JButton("Add");
		removeButton = new JButton("Remove");
		moveDownButton = new JButton("Down");
		mergeButton = new JButton("Merge");
		for (JButton btn : new JButton[] { moveUpButton, addButton, removeButton, moveDownButton, mergeButton }) {
			btn.setMaximumSize(new Dimension(150, 50));
		}
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

		for (JComponent comp : new JComponent[] { addButton, new JPanel(), moveUpButton, removeButton, moveDownButton,
				new JPanel(), mergeButton })
			buttonPanel.add(comp);
		buttonPanel.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

		flistModel = new DefaultListModel<>();
		fstringList = new JList<>(flistModel);
		scrpane = new JScrollPane(fstringList);
		scrpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		add(scrpane);
		add(buttonPanel);
	}

	private final void addEvents() {
		addButton.addActionListener((ActionEvent ae) -> {
			FileDialog fileChooser = new FileDialog(SwingMerge.this);
			fileChooser.setMultipleMode(true);
			fileChooser.setFilenameFilter((File arg0, String arg1) -> {
				if (arg1.length() >= 5 && arg1.substring(arg1.length() - 4, arg1.length()).equals(".pdf"))
					return true;
				return false;
			});
			fileChooser.setVisible(true);
			File[] farray = fileChooser.getFiles();
			for (File f : farray)
				if (f.isFile())
					flistModel.addElement(f);
				else {
					JOptionPane.showMessageDialog(SwingMerge.this,
							"Selected item:\n" + f.getAbsolutePath() + "\nis not a file", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
		});
		removeButton.addActionListener((ActionEvent ae) -> {
			int[] selected = fstringList.getSelectedIndices();
			int removed = 0;
			for (int i : selected)
				flistModel.remove(i - removed++);
			if (!flistModel.isEmpty()) {
				int newindex = selected[0];
				if (newindex > 0)
					newindex--;
				fstringList.setSelectedIndex(newindex);
			}
		});
		mergeButton.addActionListener((ActionEvent arg0) -> {
			if (flistModel.isEmpty()) {
				JOptionPane.showMessageDialog(SwingMerge.this, "No PDF files in list", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			FileDialog fileChooser = new FileDialog(SwingMerge.this);
			fileChooser.setMode(FileDialog.SAVE);
			fileChooser.setVisible(true);
			String outpath = fileChooser.getFile();
			if (outpath == null)
				return;
			File[] farr = new File[flistModel.getSize()];
			for (int i = 0; i < farr.length; ++i)
				farr[i] = flistModel.get(i);
			final Worker worker = new Worker(farr, (fileChooser.getDirectory() + outpath));
			ActionListener cancelListner = (ActionEvent e) -> worker.cancel(true);
			worker.addPropertyChangeListener((PropertyChangeEvent event) -> {
				switch (event.getPropertyName()) {
				case "progress":
					progressBar.setIndeterminate(false);
					progressBar.setValue((Integer) event.getNewValue());
					break;
				}
			});
			progressBar.setValue(0);
			progressDialog.setTitle(Worker.MERGING);
			cancelButton.addActionListener(cancelListner);
			worker.execute();
			progressDialog.setVisible(true);
			cancelButton.removeActionListener(cancelListner);
		});
		moveUpButton.addActionListener((ActionEvent e) -> {
			int[] arr = fstringList.getSelectedIndices();
			if (arr[0] > 0) {
				int index = arr[arr.length - 1];
				File selected = flistModel.get(arr[0] - 1);
				flistModel.remove(arr[0] - 1);
				flistModel.add(index, selected);
				for (int j = 0; j < arr.length; ++j)
					arr[j]--;
				fstringList.setSelectedIndices(arr);
			}
		});
		moveDownButton.addActionListener((ActionEvent e) -> {
			int[] arr = fstringList.getSelectedIndices();
			if (arr[arr.length - 1] < flistModel.getSize() - 1) {
				int index = arr[0];
				File selected = flistModel.get(arr[arr.length - 1] + 1);
				flistModel.remove(arr[arr.length - 1] + 1);
				flistModel.add(index, selected);
				for (int j = 0; j < arr.length; ++j)
					arr[j]++;
				fstringList.setSelectedIndices(arr);
			} else
				return;
		});
	}

	private void createProgressdialog() {
		progressDialog = new JDialog(SwingMerge.this, Worker.MERGING, true) {
			@Override
			public void setVisible(boolean b) {
				Point p = SwingMerge.this.getLocation();
				p.x += SwingMerge.this.getWidth() / 2 - progressDialog.getWidth() / 2;
				p.y += SwingMerge.this.getHeight() / 2 - progressDialog.getHeight() / 2;
				progressDialog.setLocation(p);
				super.setVisible(b);
			}
		};
		progressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		progressBar = new JProgressBar();
		progressBar.setSize(200, 40);

		cancelButton = new JButton("Cancel");
		cancelButton.setSize(new Dimension(150, 50));

		progressDialog.getContentPane().setLayout(new BoxLayout(progressDialog.getContentPane(), BoxLayout.X_AXIS));
		progressDialog.add(progressBar);
		progressDialog.add(cancelButton);
		progressDialog.setMinimumSize(new Dimension(250, 50));
		progressDialog.setResizable(false);
		progressDialog.pack();
	}

}
