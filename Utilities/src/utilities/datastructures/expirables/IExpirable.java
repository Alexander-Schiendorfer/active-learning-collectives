package utilities.datastructures.expirables;

import java.io.Serializable;

/**
 * Indicates elements that can expire.
 *
 * @author Gerrit
 *
 * @param T
 *            the information used to check whether the {@link IExpirable} is expired.
 *
 */
public interface IExpirable<T> extends Serializable {

	/**
	 *
	 * @param checkForExpiredInfo
	 *            information used to check whether the object is expired.
	 * @return <code>true</code> iff this is expired.
	 */
	public boolean isExpired(T checkForExpiredInfo);

}
