This file gives an overview of the delivered contents:

- The directory 'thesis' contains my Bachelor's thesis.
	It describes the concepts of the generator and contains a easy-to-read
	user manual for the generator.

- The directory 'sources' contains the source code of the generator.
	The subdirectories:
	* 'main' is the actual source code of the generator.
	* 'test' contains JUnit tests.
		For more information see http://junit.org/.
	* 'contracts' contains so-called contracts for certain classes.
		They are written for the Design by Contract (DbC) framework C4J;
		for more information see http://c4j.sourceforge.net/.
	
	Neither the unit tests nor the contracts are contained in the the JAR file.

- 'ldcrgen.bat' is a batch script for Windows. 
	Try calling 'ldcrgen.bat -h' for help.

- 'ldcrgen.jar' is the generator itself. 
	Try calling 'java -jar ldcrgen -h' for help.

- 'ldcrgen.sh' is a bash script for Unixes. 
	Try calling 'ldcrgen.sh -h' for help.

- 'version.txt' contains the exact revision of the files and may be 
	useful for debugging.

If you encounter problems, please contact me via e-mail:

	roland.kluge@gmx.de

