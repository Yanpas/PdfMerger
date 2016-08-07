all:
	ant dist
	
install:
	cp target/pdfmerger.jar /opt/
	cp src/pdfmerger.sh /usr/bin/pdfmerger

uninstall:
	rm /opt/pdfmerger.jar
	rm /usr/bin/pdfmerger
