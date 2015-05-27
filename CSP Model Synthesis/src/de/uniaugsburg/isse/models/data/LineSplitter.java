package de.uniaugsburg.isse.models.data;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Creates a list of lines and ignores line comments
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class LineSplitter {
	public List<String> readLines(File file) {
		Scanner sc = null;

		try {
			sc = new Scanner(file);
			return readInternally(sc);
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}
		return null;
	}

	private List<String> readInternally(Scanner sc) {
		List<String> strings = new LinkedList<String>();
		while (sc.hasNextLine()) {
			String nextLine = sc.nextLine();
			if (nextLine.contains("//")) {
				nextLine = nextLine.substring(0, nextLine.indexOf("//"));
			}
			strings.add(nextLine.trim());
		}
		return strings;
	}

	public List<String> readLines(String value) {
		Scanner sc = new Scanner(value);
		return readInternally(sc);
	}
}
