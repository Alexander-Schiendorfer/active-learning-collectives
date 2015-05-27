package utilities;

import java.util.UUID;

/**
 * Class providing helper methods for {@link UUID}.
 * 
 * @author Gerrit
 * 
 */
public class UUIDUtils {

	/**
	 * Copies the given {@link UUID}.
	 * 
	 * @param aUUID
	 * @return
	 */
	public static UUID copyUUID(UUID aUUID) {
		return new UUID(aUUID.getMostSignificantBits(), aUUID.getLeastSignificantBits());
	}
}
