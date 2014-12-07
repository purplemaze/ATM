package files;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * ATMServer class!
 * 
 * This is a wrapper class for writing and reading to/from the same file.
 * 
 * @author Daniel C 
 * @author Ziad S
 * @version 1.0
 */
public class FileReaderWriter {
	private Path path;
	private Charset charset;
	
	/**
	 * Constructor
	 * @param filename
	 */
	public FileReaderWriter(String filename) {
		this.path = Paths.get(filename);
		this.charset = StandardCharsets.UTF_8;
	}

	/**
	 * Reads from a "small" file.
	 * @return a List<String> where every element is a row in the read file.
	 * @throws IOException
	 * @see IOException
	 */
	public List<String> readFile() throws IOException {
	    return Files.readAllLines(path, charset);
	}

	/**
	 * Writes to a "small" file
	 * @param aLines
	 * @throws IOException
	 * @see IOException
	 */
	public void writeFile(List<String> aLines) throws IOException {
		Files.write(path, aLines, charset);
	}
		  
}

