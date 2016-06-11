package yanpas.pdfmerger;

import javax.swing.*;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SwingMerge extends JFrame
{
	private static final long serialVersionUID = 1L;

	private static class FString
	{
		public File f;

		public FString(File f)
		{
			this.f = f;
		}

		@Override
		public String toString()
		{
			return f.getAbsolutePath();
		}
	}
	private class Worker extends SwingWorker<Void, Void>
	{
		private List<File> fileList;
		private File outFile;
		
		public Worker(List<File> fileList, File outFile) {
			this.fileList = fileList;
			this.outFile = outFile;
		}
		
		@Override
		public Void doInBackground() throws Exception {
			progressDialog.setTitle("Merging");
			Merger merger = new Merger(fileList);
			PDDocument result = null;
			try {
				result = merger.merge();
			} catch (IOException e){
				JOptionPane.showMessageDialog(SwingMerge.this, e.getMessage(), "Error",
					    JOptionPane.ERROR_MESSAGE);
				progressDialog.setVisible(false);
				System.err.println(e.getMessage());
				return null;
			}
			progressDialog.setTitle("Saving");
			try {
				result.save(outFile);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				JOptionPane.showMessageDialog(SwingMerge.this, "Can't save to this destination",
						"Error", JOptionPane.ERROR_MESSAGE);
				return null;
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(SwingMerge.this, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			} finally {
				progressDialog.setVisible(false);
			}
			try	{
				result.close();
				merger.closeAll();
			} catch (IOException e)	{
				e.printStackTrace();
				JOptionPane.showMessageDialog(SwingMerge.this, "Error while closing output PDF",
						"Error", JOptionPane.WARNING_MESSAGE);
				return null;
			}
			JOptionPane.showMessageDialog(SwingMerge.this,
				    "Done!\nThanks for using PDF Merger. Project's home page:\n" +
				    "https://github.com/Yanpas/PdfMerger",
				    "Operation finished",
				    JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
	}
	private Worker worker;

	private JScrollPane scrpane;
		private DefaultListModel<FString> flistModel;
		private JList<FString> fstringList;	
	private JPanel buttonPanel;
		private JButton moveUpButton;
		private JButton addButton;
		private JButton removeButton;
		private JButton moveDownButton;
		private JButton mergeButton;
	
	private JDialog progressDialog;
		private JProgressBar progressBar;
		
	public SwingMerge()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		setTitle("PDF Merger");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.placeAllElements();
		pack();
		setMinimumSize(new Dimension(500, 300));
		this.addEvents();
		this.createProgressdialog();
	}

	private final void placeAllElements()
	{
		Dimension max_buttonsize = new Dimension(150,50);
		moveUpButton = new JButton("Up");
		moveUpButton.setMaximumSize(max_buttonsize);
		addButton = new JButton("Add");
		addButton.setMaximumSize(max_buttonsize);
		removeButton = new JButton("Remove");
		removeButton.setMaximumSize(max_buttonsize);
		moveDownButton = new JButton("Down");
		moveDownButton.setMaximumSize(max_buttonsize);
		mergeButton = new JButton("Merge");
		mergeButton.setMaximumSize(max_buttonsize);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
		buttonPanel.add(addButton);
		JPanel expander = new JPanel();
		buttonPanel.add(expander);
		buttonPanel.add(moveUpButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(moveDownButton);
		JPanel expander2 = new JPanel();
		buttonPanel.add(expander2);
		buttonPanel.add(mergeButton);
		buttonPanel.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

		flistModel = new DefaultListModel<FString>();
		fstringList = new JList<FString>(flistModel);
		scrpane = new JScrollPane(fstringList);
		scrpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		add(scrpane);
		add(buttonPanel);
	}

	private final void addEvents()
	{
		addButton.addActionListener(new ActionListener() {
			
			@Override 
			public void actionPerformed(ActionEvent ae) {
				FileDialog fileChooser = new FileDialog(SwingMerge.this);
				fileChooser.setMultipleMode(true);
				fileChooser.setFilenameFilter(new FilenameFilter() {

					@Override
					public boolean accept(File arg0, String arg1) {
						if (arg1.length() < 5)
							return false;
						if (arg1.substring(arg1.length() - 4, arg1.length()).equals(".pdf"))
							return true;
						return false;
					}
				});
				fileChooser.setVisible(true);
				File[] farray = fileChooser.getFiles();
				for (File f : farray)
					if (f.isFile())
						flistModel.addElement(new FString(f));
					else
					{
						JOptionPane.showMessageDialog(SwingMerge.this,
								"Selected item:\n"+f.getAbsolutePath()+"\nis not a file",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
						return;
					}
		      }
		      });
		removeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				int[] selected = fstringList.getSelectedIndices();
				int removed = 0;
				for (int i : selected)
					flistModel.remove(i-removed++);
				if (! flistModel.isEmpty()) {
					int newindex = selected[0];
					if (newindex > 0) newindex--;
					fstringList.setSelectedIndex(newindex);
				}
			}
			
		});
		mergeButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (flistModel.isEmpty())
				{
					JOptionPane.showMessageDialog(SwingMerge.this,
						"No PDF files in list",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
					return;
				}
				FileDialog fileChooser = new FileDialog(SwingMerge.this);
				fileChooser.setMode(FileDialog.SAVE);
		        fileChooser.setVisible(true);
		        String outpath = fileChooser.getFile();
		        if (outpath == null) return;
		        List<File> fileList = new Vector<File>();
		        for (int i=0; i<flistModel.getSize(); ++i)
		        {
		        	File tmp = flistModel.get(i).f;
		        	if (!tmp.exists())
		        	{
		        		JOptionPane.showMessageDialog(SwingMerge.this,
								"File \""+tmp.getAbsolutePath()+"\" does not exist",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
		        		return;
		        	}
		        	fileList.add(tmp);
		        }
		        worker = new Worker(fileList, new File(fileChooser.getDirectory()+outpath));
		        worker.execute();
		        progressDialog.setVisible(true);
			}
		});
		moveUpButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] arr = fstringList.getSelectedIndices();
				if (arr[0]>0)
				{
					int index = arr[arr.length-1];
					FString selected = flistModel.get(arr[0]-1);
					flistModel.remove(arr[0]-1);
					flistModel.add(index, selected);
					for(int j=0; j<arr.length; ++j)
						arr[j]--;
					fstringList.setSelectedIndices(arr);
				}
				else return;
			}
		});
		moveDownButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] arr = fstringList.getSelectedIndices();
				if (arr[arr.length-1]<flistModel.getSize()-1)
				{
					int index = arr[0];
					FString selected = flistModel.get(arr[arr.length-1]+1);
					flistModel.remove(arr[arr.length-1]+1);
					flistModel.add(index, selected);
					for(int j=0; j<arr.length; ++j)
						arr[j]++;
					fstringList.setSelectedIndices(arr);
				}
				else return;
			}
		});
	}
	
	@SuppressWarnings("serial")
	private void createProgressdialog()
	{
		progressDialog = new JDialog(SwingMerge.this, "Merging", true) {
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
		progressBar.setIndeterminate(true);
		progressBar.setSize(200, 40);

		progressDialog.getContentPane().setLayout(new BoxLayout(
				progressDialog.getContentPane(), BoxLayout.X_AXIS));
		progressDialog.add(progressBar);
		progressDialog.setMinimumSize(new Dimension(250, 50));
		progressDialog.setResizable(false);
		progressDialog.pack();
	}

}
