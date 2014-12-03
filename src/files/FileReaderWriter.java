package files;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
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
		  
	public static void main(String[] args) throws IOException{
		
		String newLine = args[0];
		FileReaderWriter fRead = new FileReaderWriter("/home/daniel/Documents/workspace/Inet/src/test.txt");
		
		ArrayList<String> lines = new ArrayList<String>();
		lines.add(newLine);
	    fRead.writeFile(lines);
   
				    
		//treat as a small file
		lines = (ArrayList<String>) fRead.readFile();
		Iterator<String> it = lines.iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}

	}
	
}

