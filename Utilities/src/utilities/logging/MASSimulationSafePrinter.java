package utilities.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Provides synchronized methods to print text to the console in a more ordered manner than by calling the normal
 * system.outs or system.errs. Moreover, all text is stored and can be exported to a txt file by calling
 * {@link #writeFile()} or {@link #writeFile(String)}.
 */
public class MASSimulationSafePrinter {

	/**
	 * A {@link StringBuffer} that holds all text called by one of the printToSysOut/printToSysErr methods. The text is
	 * written to a txt file if {@link #writeFile()} or {@link #writeFile(String)} is called.
	 */
	private static volatile StringBuffer textToWrite = new StringBuffer();

	/**
	 * Prints a String to the "standard" output stream and then terminates the line.
	 * 
	 * @param s
	 *            The String to be printed
	 */
	public synchronized static void printToSysOut(String s) {
		MASSimulationSafePrinter.textToWrite = MASSimulationSafePrinter.textToWrite.append("\r" + s);
		System.out.println(s);
	}

	/**
	 * Prints an object to the "standard" output stream and then terminates the line.
	 * 
	 * @param o
	 *            The object to be printed
	 */
	public synchronized static void printToSysOut(Object o) {
		MASSimulationSafePrinter.printToSysOut(o.toString());
	}

	/**
	 * Prints a String to the "standard" error output stream and then terminates the line.
	 * 
	 * @param s
	 *            The String to be printed
	 */
	public synchronized static void printToSysErr(String s) {
		MASSimulationSafePrinter.textToWrite = MASSimulationSafePrinter.textToWrite.append("\r" + "Error: " + s);
		System.err.println(s);
	}

	/**
	 * Prints an object to the "standard" error output stream and then terminates the line.
	 * 
	 * @param o
	 *            The object to be printed
	 */
	public synchronized static void printToSysErr(Object o) {
		MASSimulationSafePrinter.printToSysErr(o.toString());
	}

	/**
	 * Writes the saved console output to a .txt file with a unique identifier.
	 */
	public synchronized static void writeFile() {
		String suffix = "@" + System.currentTimeMillis();
		MASSimulationSafePrinter.writeFile("MASSimulationSafePrinter" + suffix);
	}

	/**
	 * Writes the saved console output to a .txt file with the given filename. The filename should be without the file
	 * extension <code>txt</code>.
	 * 
	 * @param filename
	 *            the filename (without .txt ending)
	 */
	public synchronized static void writeFile(String filename) {
		try {
			filename += ".txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
			writer.write(MASSimulationSafePrinter.textToWrite.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints and returns the text of a {@link Throwable}.
	 * 
	 * @param t
	 *            The {@link Throwable} whose stack trace is printed and returned
	 * @return the stack trace of the given {@link Throwable}
	 */
	public synchronized static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
}