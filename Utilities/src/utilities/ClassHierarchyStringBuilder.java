package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that is responsible for creating Strings of class hierarchies.
 */
public class ClassHierarchyStringBuilder {

	/**
	 * For serialization purposes.
	 */
	private final static Logger LOG = LogManager.getLogger();

	/**
	 * Creates a String representing the class hierarchy beginning with the given <code>topHierarchyClass</code> and
	 * ending with the given <code>aClass</code>, each class separated by ".".
	 * 
	 * @param aClass
	 *            the bottom hierarchy class to end the String with
	 * @param topHierarchyClass
	 *            the top hierarchy class to start the String from
	 * @return a String representing the class hierarchy in the form <code>topHierarchyClass.x.y.aClass</code>
	 */
	public static String getClassHierarchyStringForClass(Class<?> aClass, Class<?> topHierarchyClass) {

		List<Class<?>> classesOfHierarchy = ClassHierarchyStringBuilder.getClassListOfHierarchy(aClass, topHierarchyClass);
		Collections.reverse(classesOfHierarchy);

		return ClassHierarchyStringBuilder.createStringForClassHierarchy(classesOfHierarchy);
	}

	/**
	 * Creates a String representing the class hierarchy beginning with the given <code>aClass</code> and ending with
	 * the given <code>topHierarchyClass</code>, each class separated by ".".
	 * 
	 * @param aClass
	 *            the bottom hierarchy class to start the String with
	 * @param topHierarchyClass
	 *            the top hierarchy class to end the String from
	 * @return a String representing the class hierarchy in the form <code>aClass.x.y.topHierarchyClass</code>
	 */
	public static String getReverseClassHierarchyStringForClass(Class<?> aClass, Class<?> topHierarchyClass) {

		List<Class<?>> classesOfHierarchy = ClassHierarchyStringBuilder.getClassListOfHierarchy(aClass, topHierarchyClass);

		return ClassHierarchyStringBuilder.createStringForClassHierarchy(classesOfHierarchy);
	}

	/**
	 * Actually creates the hierarchy String from a given List of classes, each separated by a ".".
	 * 
	 * @param classHierarchyList
	 *            the List of classes
	 * @return the created hierarchy String
	 */
	private static String createStringForClassHierarchy(List<Class<?>> classHierarchyList) {
		StringBuilder hierarchyString = new StringBuilder();

		// as we do not want a final "." at the end of the String, we iterate over all elements except for the last
		// one ...
		int i = 0;
		for (i = 0; i < classHierarchyList.size() - 1; i++) {
			hierarchyString.append(classHierarchyList.get(i).getSimpleName());
			hierarchyString.append(".");
		}
		// ... and finally add the last (or only) one
		hierarchyString.append(classHierarchyList.get(i).getSimpleName());

		return hierarchyString.toString();
	}

	/**
	 * Returns a List of the classes in the class hierarchy of <code>aClass</code> until <code>topHierarchyClass</code>.
	 * 
	 * @param aClass
	 *            the bottom class of the class hierarchy
	 * @param topHierarchyClass
	 *            the top class of the class hierarchy
	 * @return the List of class in the class hierarchy
	 */
	private static List<Class<?>> getClassListOfHierarchy(Class<?> aClass, Class<?> topHierarchyClass) {
		// only get list if assignable
		if (topHierarchyClass.isAssignableFrom(aClass)) {
			// list that holds aClass and all relevant super classes of aClass
			List<Class<?>> classHierarchyList = new ArrayList<Class<?>>();
			classHierarchyList.add(aClass);

			// add every super class to list until top hierarchy class is reached
			while (!aClass.equals(topHierarchyClass)) {
				aClass = aClass.getSuperclass();
				classHierarchyList.add(aClass);
			}

			return classHierarchyList;
		}
		// not assignable -- throw exception
		else {
			throw ClassHierarchyStringBuilder.LOG.throwing(new RuntimeException("The given top hierarchy class '" + topHierarchyClass
					+ "' is not assignable from the class '" + aClass + "'!"));
		}
	}
}