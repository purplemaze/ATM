package files;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 
 * @author daniel
 *
 */
public class FileReaderWriter {
	private Path path;
	private Charset charset;
	
	/**
	 * Konstruktor
	 * @param dir
	 * @param filename
	 * @param charset
	 */
	public FileReaderWriter(String filename) {
		this.path = Paths.get(filename);
		this.charset = StandardCharsets.UTF_8;
	}

	/**
	 * readFile - reads a "small" file
	 * @return
	 * @throws IOException
	 */
	public List<String> readFile() throws IOException {
	    return Files.readAllLines(path, charset);
	}

	/**
	 * writeFile - writes a "small" file
	 * @param aLines
	 * @throws IOException
	 */
	public void writeFile(List<String> aLines) throws IOException {
		Files.write(path, aLines, charset);
	}
		  
}

