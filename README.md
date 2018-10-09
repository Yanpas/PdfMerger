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

Building: `mvn compile assembly:single`
Usage: `java -jar pdfmerger.jar file1.pdf file2.pdf ... out.pdf`
If you want to use it your linux system there is sh wrapper at src/pdfmerger

Running without arguments launches Swing GUI:

![GUI](http://i.imgur.com/dMoCWSf.png)

