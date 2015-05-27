package optimizationtools.util;

import java.util.Collection;
import java.util.Iterator;

public class StringUtils {
	/**
	 * Glue together all <b>items</b> into one String. <b>prefix</b> is prepended (once) to the list, and <b>suffix</b>
	 * is appended (once) to it, if they are non-{@code null}. If <b>delimiter</b> is non-{@code null}, it is inserted
	 * between each two elements of <b>items</b> (otherwise they are appended directly to one another). If there is at
	 * least one element in <b>items</b> and <b>lastItemSuffix</b> is non-{@code null}, it is appended after the last
	 * element and before <b>suffix</b>.
	 * 
	 * A {@link StringBuilder} is used to construct the result string, which takes care of converting all primitive
	 * types, and relies on {@link Object#toString()} for all complex types.
	 * 
	 * 
	 * @param items
	 *            the items to glue together
	 * @param prefix
	 *            an optional string to prepend to the result string
	 * @param suffix
	 *            an optional string to append to the result string
	 * @param delimiter
	 *            the glue to insert between adjacent items
	 * @param lastItemSuffix
	 *            an optional spacer to add between the last element and <b>suffix</b>, if there is at least one element
	 * @return the resulting string
	 * 
	 * @throws NullPointerException
	 *             if <b>items</b> is {@code null}
	 */
	public static String implodeCollection(Collection<?> items, String prefix, String suffix, String delimiter, String lastItemSuffix) {
		if (items == null)
			throw new NullPointerException("items argument may not be null");
		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";
		if (delimiter == null)
			delimiter = "";
		if (lastItemSuffix == null)
			lastItemSuffix = "";

		StringBuilder sb = new StringBuilder(prefix);

		if (!items.isEmpty()) {
			Iterator<?> it = items.iterator();
			for (int i = 0; i < items.size() - 1; ++i)
				sb.append(it.next()).append(delimiter);
			sb.append(it.next()).append(lastItemSuffix);
		}

		return sb.append(suffix).toString();
	}

	/**
	 * Glue together all <b>items</b> into one String. <b>prefix</b> is prepended (once) to the list, and <b>suffix</b>
	 * is appended (once) to it, if they are non-{@code null}. If <b>delimiter</b> is non-{@code null}, it is inserted
	 * between each two elements of <b>items</b> (otherwise they are appended directly to one another). If there is at
	 * least one element in <b>items</b> and <b>lastItemSuffix</b> is non-{@code null}, it is appended after the last
	 * element and before <b>suffix</b>.
	 * 
	 * A {@link StringBuilder} is used to construct the result string, which takes care of converting all primitive
	 * types, and relies on {@link Object#toString()} for all complex types.
	 * 
	 * 
	 * @param items
	 *            the items to glue together
	 * @param prefix
	 *            an optional string to prepend to the result string
	 * @param suffix
	 *            an optional string to append to the result string
	 * @param delimiter
	 *            the glue to insert between adjacent items
	 * @param lastItemSuffix
	 *            an optional spacer to add between the last element and <b>suffix</b>, if there is at least one element
	 * @return the resulting string
	 * 
	 * @throws NullPointerException
	 *             if <b>items</b> is {@code null}
	 */
	public static String implodeArray(Object[] items, String prefix, String suffix, String delimiter, String lastItemSuffix) {
		if (items == null)
			throw new NullPointerException("items argument may not be null");
		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";
		if (delimiter == null)
			delimiter = "";
		if (lastItemSuffix == null)
			lastItemSuffix = "";

		StringBuilder sb = new StringBuilder(prefix);

		if (items.length > 0) {
			for (int i = 0; i < items.length - 1; ++i)
				sb.append(items[i]).append(delimiter);
			sb.append(items[items.length - 1]).append(lastItemSuffix);
		}

		return sb.append(suffix).toString();
	}

	/**
	 * Glue together all items returned by <b>it</b> into one String. <b>prefix</b> is prepended (once) to the list, and
	 * <b>suffix</b> is appended (once) to it, if they are non-{@code null}. If <b>delimiter</b> is non-{@code null}, it
	 * is inserted between each two elements of <b>items</b> (otherwise they are appended directly to one another). If
	 * there is at least one element in <b>items</b> and <b>lastItemSuffix</b> is non-{@code null}, it is appended after
	 * the last element and before <b>suffix</b>.
	 * 
	 * A {@link StringBuilder} is used to construct the result string, which takes care of converting all primitive
	 * types, and relies on {@link Object#toString()} for all complex types.
	 * 
	 * 
	 * @param it
	 *            the iterator which returns the items to glue together
	 * @param prefix
	 *            an optional string to prepend to the result string
	 * @param suffix
	 *            an optional string to append to the result string
	 * @param delimiter
	 *            the glue to insert between adjacent items
	 * @param lastItemSuffix
	 *            an optional spacer to add between the last element and <b>suffix</b>, if there is at least one element
	 * @return the resulting string
	 * 
	 * @throws NullPointerException
	 *             if <b>it</b> is {@code null}
	 */
	public static String implodeIterable(Iterator<?> it, String prefix, String suffix, String delimiter, String lastItemSuffix) {
		if (it == null)
			throw new NullPointerException("it argument may not be null");
		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";
		if (delimiter == null)
			delimiter = "";
		if (lastItemSuffix == null)
			lastItemSuffix = "";

		StringBuilder sb = new StringBuilder(prefix);

		Object prev = null;

		if (it.hasNext()) {
			while (it.hasNext()) {
				if (prev != null)
					sb.append(prev).append(delimiter);
				prev = it.next();
			}
			if (prev != null)
				sb.append(prev).append(lastItemSuffix);
		}

		return sb.append(suffix).toString();
	}

	public static String repeatChar(int width, char c) {
		StringBuilder sb = new StringBuilder(width);
		for (int i = 0; i < width; ++i)
			sb.append(c);
		return sb.toString();
	}
}
