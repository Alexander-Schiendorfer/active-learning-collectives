package utilities.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for serializing and/or deserializing objects.
 */
public class ObjectSerializerDeserializer {

	/**
	 * For logging purposes.
	 */
	private final static Logger LOG = LogManager.getLogger();

	/**
	 * Serializes the given object and saves it to a ".ser"-file with the given file name.
	 *
	 * @param objectToSerialize
	 *            The object that has to be serialized
	 * @param fileNameWithoutSuffix
	 *            The file name (without file extension)
	 */
	public static void serializeObject(Object objectToSerialize, String fileNameWithoutSuffix) {
		ObjectSerializerDeserializer.serializeObject(objectToSerialize, fileNameWithoutSuffix, ".ser");
	}

	/**
	 * Serializes the given object and saves it to a file with the given file name and file extension.
	 *
	 * @param objectToSerialize
	 *            The object that has to be serialized
	 * @param fileNameWithoutSuffix
	 *            The file name (without file extension)
	 * @param suffix
	 *            The file extension
	 */
	public static void serializeObject(Object objectToSerialize, String fileNameWithoutSuffix, String suffix) {
		OutputStream fos = null;
		ObjectOutputStream o = null;
		BufferedOutputStream bos = null;

		try {
			fos = new FileOutputStream(fileNameWithoutSuffix + suffix);
			bos = new BufferedOutputStream(fos, 1024 * 1024);
			o = new ObjectOutputStream(bos);
			o.writeObject(objectToSerialize);
		} catch (IOException e) {
			ObjectSerializerDeserializer.LOG.error("Could not serialize file " + fileNameWithoutSuffix + suffix, e);
		} finally {
			try {
				o.close();
				bos.close();
				fos.close();
			} catch (Exception e) {
				ObjectSerializerDeserializer.LOG.error("Could not close outputstream!", e);
			}
		}
	}

	/**
	 * Deserializes the serialized data given by <code>fileName</code> and returns the deserialized object.
	 *
	 * @param fileName
	 * @return the deserialized object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserializeObject(String fileName) throws IOException, ClassNotFoundException {

		InputStream fis = null;
		InputStream bis = null;
		ObjectInputStream o = null;

		try {
			fis = new FileInputStream(fileName);
			bis = new BufferedInputStream(fis);
			o = new ObjectInputStream(bis);
			return o.readObject();
		} finally {
			try {
				o.close();
				fis.close();
			} catch (Exception e) {
				ObjectSerializerDeserializer.LOG.error(e);
			}
		}
	}

	/**
	 * Serializes an object to memory and deserializes it afterwards. Sounds silly but can be useful in some situations.
	 *
	 * @param o
	 *            the object to be processed
	 * @return the deserialized object
	 */
	public static Serializable writeToAndReadFromMemory(Serializable o) {
		try {
			// Serialization
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out;
			out = new ObjectOutputStream(bos);
			out.writeObject(o);

			// De-serialization
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream in = new ObjectInputStream(bis);
			Serializable in_serl = (Serializable) in.readObject();

			return in_serl;
		} catch (IOException e) {
			ObjectSerializerDeserializer.LOG.error(e);
		} catch (ClassNotFoundException e) {
			ObjectSerializerDeserializer.LOG.error(e);
		}

		return null;
	}
}