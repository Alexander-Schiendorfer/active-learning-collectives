package utilities;

import java.math.BigDecimal;

/**
 * Helps to round numbers to a specific precision.
 * 
 * @author Gerrit
 * 
 */
public class RoundingHelper {

	/**
	 * The default desired precision, i.e., scale, of double values.
	 */
	public static int SCALE = 8;

	/**
	 * The default lower precision, i.e., scale, of double values.
	 */
	public static int SHORT_SCALE = RoundingHelper.SCALE / 4;

	/**
	 * Rounds a double value to the specified scale using {@link BigDecimal#ROUND_HALF_UP} as rounding mode.
	 * 
	 * @param value
	 *            the value to be rounded
	 * @param scale
	 *            the scale of the returned value
	 * @return the value rounded to the given scale
	 */
	public static double roundToScale(double value, int scale) {
		BigDecimal bd = new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Rounds a double value to a scale of {@link #SCALE} using {@link BigDecimal#ROUND_HALF_UP} as rounding mode.
	 * 
	 * @param value
	 *            the value to be rounded
	 * @return the value rounded to the given scale
	 */
	public static double roundToScale(double value) {
		return RoundingHelper.roundToScale(value, RoundingHelper.SCALE);
	}

	/**
	 * Rounds a double value to a scale of {@link #SHORT_SCALE} using {@link BigDecimal#ROUND_HALF_UP} as rounding mode.
	 * 
	 * @param value
	 *            the value to be rounded
	 * @return the value rounded to the given scale
	 */
	public static double roundToShortScale(double value) {
		return RoundingHelper.roundToScale(value, RoundingHelper.SHORT_SCALE);
	}
}
