# PDF Merger
Welcome to PDF Merger home page!

This program merges several pdfs into one and creates handy outline (tree of bookmarks).
For example: doc1.pdf with outline:
* Header 1

and doc2.pdf with
* Header 2
 * subheader 2

will be merged into merged.pdf with the following outline:
* doc1
  * Header 1
* doc2 
  * Header 2
    * subheader 2 

Usage:	`java -jar pdfmerger.jar file1.pdf file2.pdf ... out.pdf`

Running without arguments launches Swing GUI:

![GUI](http://i.imgur.com/dMoCWSf.png)

You may also create and install program via `sudo make install` and uninstall via `sudo make uninstall`. For Debian-based distro use `sudo checkinstall -D` for installing the package. Run anywhere right out of console: `pdfmerger [arguments ...]`.
