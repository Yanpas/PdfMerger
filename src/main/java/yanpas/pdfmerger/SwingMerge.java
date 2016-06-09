package yanpas.pdfmerger;

import javax.swing.*;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SwingMerge
{
	private class FString
	{
		public File f;
		public String s;

		public FString(File f)
		{
			this.f = f;
			this.s = f.getAbsolutePath();
		}

		@Override
		public String toString()
		{
			return s;
		}
	}
	private class Worker extends SwingWorker<Void, Void>
	{
		private List<File> filelist;
		private File output;
		public Worker(List<File> filelist, File output) {
			this.filelist = filelist;
			this.output = output;
		}
		@Override
		public Void doInBackground() throws Exception {
			Merger result = new Merger(filelist);
			PDDocument merged = null;
			try {
				merged = result.merge();
			} catch (IOException e){
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error",
					    JOptionPane.ERROR_MESSAGE);
				progressdialog.setVisible(false);
				System.err.println(e.getMessage());
				return null;
			}
			progressdialog.setTitle("Saving...");
			try {
				merged.save(output);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				JOptionPane.showMessageDialog(frame, "Can't save to this destination",
						"Error", JOptionPane.ERROR_MESSAGE);
				return null;
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			} finally {
				progressdialog.setVisible(false);
			}
			try	{
				merged.close();
			} catch (IOException e)	{
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Error while closing output PDF",
						"Error", JOptionPane.WARNING_MESSAGE);
				return null;
			}
			JOptionPane.showMessageDialog(frame,
				    "Done!\nThanks for using PDF Merger. Project's home page:\n" +
				    "https://github.com/Yanpas/PdfMerger",
				    "Operation finished",
				    JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
	}
	private Worker worker;

	private JFrame frame;

	private JScrollPane scrpane;
		private DefaultListModel<FString> flist_model;
		private JList<FString> flist;	
	private JPanel buttonpanel;
		private JButton move_up_button;
		private JButton add_button;
		private JButton remove_button;
		private JButton move_down_button;
		private JButton merge_button;
	
	private JDialog progressdialog;
	private JProgressBar progressbar;
	private JButton cancel_button;

	private final void placeAllElements()
	{
		Dimension max_buttonsize = new Dimension(150,50);
		move_up_button = new JButton("Up");
		move_up_button.setMaximumSize(max_buttonsize);
		add_button = new JButton("Add");
		add_button.setMaximumSize(max_buttonsize);
		remove_button = new JButton("Remove");
		remove_button.setMaximumSize(max_buttonsize);
		move_down_button = new JButton("Down");
		move_down_button.setMaximumSize(max_buttonsize);
		merge_button = new JButton("Merge");
		merge_button.setMaximumSize(max_buttonsize);
		
		buttonpanel = new JPanel();
		buttonpanel.setLayout(new BoxLayout(buttonpanel,BoxLayout.Y_AXIS));
		buttonpanel.add(add_button);
		JPanel expander = new JPanel();
		buttonpanel.add(expander);
		buttonpanel.add(move_up_button);
		buttonpanel.add(remove_button);
		buttonpanel.add(move_down_button);
		JPanel expander2 = new JPanel();
		buttonpanel.add(expander2);
		buttonpanel.add(merge_button);
		buttonpanel.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
		buttonpanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

		flist_model = new DefaultListModel<FString>();
		flist = new JList<FString>(flist_model);
		scrpane = new JScrollPane(flist);
		scrpane.getViewport().setBackground(Color.WHITE);
		scrpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		frame.getContentPane().setLayout(new BoxLayout(
				frame.getContentPane(),BoxLayout.X_AXIS));
		frame.add(scrpane);
		frame.add(buttonpanel);
	}

	private final void addEvents()
	{
		add_button.addActionListener(new ActionListener() {
			
			@Override 
			public void actionPerformed(ActionEvent ae) {
				FileDialog fileChooser = new FileDialog(frame);
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
						flist_model.addElement(new FString(f));
					else
					{
						JOptionPane.showMessageDialog(frame,
								"Selected item:\n"+f.getAbsolutePath()+"\nis not a file",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
						return;
					}
		      }
		      });
		remove_button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				int[] selected = flist.getSelectedIndices();
				int removed = 0;
				for (int i : selected)
					flist_model.remove(i-removed++);
				if (! flist_model.isEmpty()) {
					int newindex = selected[0];
					if (newindex > 0) newindex--;
					flist.setSelectedIndex(newindex);
				}
			}
			
		});
		merge_button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (flist_model.isEmpty())
				{
					JOptionPane.showMessageDialog(frame,
						"No PDF files in list",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
					return;
				}
				FileDialog fileChooser = new FileDialog(frame);
				fileChooser.setMode(FileDialog.SAVE);
		        fileChooser.setVisible(true);
		        String outpath = fileChooser.getFile();
		        if (outpath == null) return;
		        List<File> filelist = new Vector<File>();
		        for (int i=0; i<flist_model.getSize(); ++i)
		        {
		        	File tmp = flist_model.get(i).f;
		        	if (!tmp.exists())
		        	{
		        		JOptionPane.showMessageDialog(frame,
								"File \""+tmp.getAbsolutePath()+"\" does not exist",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
		        		return;
		        	}
		        	filelist.add(tmp);
		        }
		        worker = new Worker(filelist, new File(fileChooser.getDirectory()+outpath));
		        worker.execute();
		        progressdialog.setVisible(true);
			}
		});
		move_up_button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] arr = flist.getSelectedIndices();
				if (arr[0]>0)
				{
					int index = arr[arr.length-1];
					FString selected = flist_model.get(arr[0]-1);
					flist_model.remove(arr[0]-1);
					flist_model.add(index, selected);
					for(int j=0; j<arr.length; ++j)
						arr[j]--;
					flist.setSelectedIndices(arr);
				}
				else return;
			}
		});
		move_down_button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] arr = flist.getSelectedIndices();
				if (arr[arr.length-1]<flist_model.getSize()-1)
				{
					int index = arr[0];
					FString selected = flist_model.get(arr[arr.length-1]+1);
					flist_model.remove(arr[arr.length-1]+1);
					flist_model.add(index, selected);
					for(int j=0; j<arr.length; ++j)
						arr[j]++;
					flist.setSelectedIndices(arr);
				}
				else return;
			}
		});
	}
	
	public SwingMerge()
	{
		try	{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e){
			e.printStackTrace();
		}
		frame = new JFrame("PDF Merger");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.placeAllElements();
		frame.pack();
		frame.setMinimumSize(new Dimension(500, 300));
		this.addEvents();
		this.createProgressdialog();
	}

	@SuppressWarnings("serial")
	private void createProgressdialog() {
		progressdialog = new JDialog(frame, "Merging", true){
			@Override
			public void setVisible(boolean b)
			{
				Point p = frame.getLocation();
				p.x += frame.getWidth()/2 - progressdialog.getWidth()/2;
				p.y += frame.getHeight()/2 - progressdialog.getHeight()/2;
				progressdialog.setLocation(p);
				super.setVisible(b);
			}
		};
		progressdialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		cancel_button = new JButton("Cancel");
		cancel_button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				worker.cancel(true);
				progressdialog.setVisible(false);
			}
		});
		
		progressbar = new JProgressBar();
		progressbar.setIndeterminate(true);
		progressbar.setSize(200, cancel_button.getHeight());
		
		progressdialog.getContentPane().setLayout(new BoxLayout(
				progressdialog.getContentPane(),BoxLayout.X_AXIS));
		progressdialog.add(progressbar);
		progressdialog.add(cancel_button,BorderLayout.SOUTH);
		progressdialog.setMinimumSize(new Dimension(250, 50));
		progressdialog.setResizable(false);
		progressdialog.pack();
	}

	public void show()
	{
		frame.setVisible(true);
	}

}
