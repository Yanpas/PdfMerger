all:
	ant dist
	
install:
	cp target/pdfmerger.jar /usr/local/lib/
	cp src/pdfmerger.sh /usr/local/bin/pdfmerger

uninstall:
	rm /usr/local/lib/pdfmerger.jar
	rm /usr/local/bin/pdfmerger