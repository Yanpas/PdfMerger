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
		expander.setMaximumSize(new Dimension(0,Integer.MAX_VALUE));
		buttonpanel.add(expander);
		buttonpanel.add(move_up_button);
		buttonpanel.add(remove_button);
		buttonpanel.add(move_down_button);
		JPanel expander2 = new JPanel();
		expander2.setMaximumSize(new Dimension(0,Integer.MAX_VALUE));
		buttonpanel.add(expander2);
		buttonpanel.add(merge_button,BorderLayout.SOUTH);
		buttonpanel.setMinimumSize(new Dimension(150,Integer.MAX_VALUE));

		flist_model = new DefaultListModel<FString>();
		flist = new JList<FString>(flist_model);
		scrpane = new JScrollPane(flist);
		scrpane.getViewport().setBackground(Color.WHITE);

		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
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
		    	  fileChooser.setFilenameFilter(new FilenameFilter()
					{
						
						@Override
						public boolean accept(File arg0, String arg1)
						{
							if(arg1.length()<5) return false;
							if (arg1.substring(arg1.length()-4, arg1.length()).equals(".pdf")) return true;
							return false;
						}
					});
		          fileChooser.setVisible(true);
		          File[] farray = fileChooser.getFiles();
		          for (File f : farray)
		        	  flist_model.addElement(new FString(f));
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
				if (! flist_model.isEmpty()) flist.setSelectedIndex(0);
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
		        	
		        
				Merger result = new Merger(filelist);
				PDDocument merged = null;
				try {
					merged = result.merge();
				} catch (IOException e){
					JOptionPane.showMessageDialog(frame, e.getMessage(), "Error",
						    JOptionPane.ERROR_MESSAGE);
					System.err.println(e.getMessage());
					return;
				}
				try {
					merged.save(new File(fileChooser.getDirectory()+outpath));
				} catch (IOException e) {
					System.err.println(e.getMessage());
					JOptionPane.showMessageDialog(frame, "Can't save to this destination", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} catch (NullPointerException e) {
					JOptionPane.showMessageDialog(frame, e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try	{
					merged.close();
				} catch (IOException e)	{
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Can't save to this destination", "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				JOptionPane.showMessageDialog(frame,
					    "Done!\nThanks for using PDF Merger. Project's home page:\n" +
					    "https://github.com/Yanpas/PdfMerger",
					    "Operation finished",
					    JOptionPane.INFORMATION_MESSAGE);
			}
		});
		move_up_button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] i = flist.getSelectedIndices();
				if (i.length == 1 && i[0]>0)
				{
					int index = flist.getSelectedIndex();
					FString selected = flist_model.get(i[0]);
					flist_model.remove(i[0]);
					flist_model.add(i[0]-1, selected);
					flist.setSelectedIndex(index-1);
				}
				else return;
			}
		});
		move_down_button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] i = flist.getSelectedIndices();
				if (i.length == 1 && i[0]<flist_model.getSize()-1)
				{
					int index = flist.getSelectedIndex();
					FString selected = flist_model.get(i[0]);
					flist_model.remove(i[0]);
					flist_model.add(i[0]+1, selected);
					flist.setSelectedIndex(index+1);
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
	}

	public void show()
	{
		frame.setVisible(true);
	}

}
