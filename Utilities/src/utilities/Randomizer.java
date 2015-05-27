package utilities;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.sf.doodleproject.numerics4j.random.BetaRandomVariable;
import net.sf.doodleproject.numerics4j.random.RandomRNG;
import utilities.parameters.SimulationParameters;

/**
 * Provides static methods to generate random numbers.
 *
 * @author Gerrit
 *
 */
public class Randomizer {

	/**
	 * The instance of this class
	 */
	private static Randomizer theInstance;

	/**
	 * Used to generate a stream of pseudorandom numbers
	 */
	private Random r;

	/**
	 * The random number generator used by this class to create random based UUIDs. In a holder class to defer
	 * initialization until needed.
	 */
	private SecureRandom secureNumberGenerator;

	/**
	 * Indicates whether or not a default seed is used to generated pseudorandom numbers
	 */
	public static final boolean USE_DEFAULT_SEED = SimulationParameters.getBoolean("randomizer.useDefaultSeed", false);

	/**
	 * The seed used to generated pseudorandom numbers
	 */
	public static final long SEED = Randomizer.USE_DEFAULT_SEED ? SimulationParameters.getLongParameter("randomizer.defaultSeed", 1000l) : new Random()
			.nextLong();

	/**
	 * Used to specify alpha and beta in a beta distribution.
	 */
	private static final BigDecimal DELTA = new BigDecimal(0.001);

	private Randomizer(long seed) {
		this.r = new Random(seed);
		this.secureNumberGenerator = new SecureRandom(ByteBuffer.allocate(8).putLong(seed).array());
	}

	/**
	 * Sets the {@link Randomizer} to use a default seed for now. Can be switched off by calling
	 * {@link #useRandomSeed()} if a default seed is not needed any more.
	 */
	public void useDefaultSeed() {
		long seed = SimulationParameters.getLongParameter("randomizer.defaultSeed", 1000l);
		Randomizer.theInstance = new Randomizer(seed);
	}

	/**
	 * Sets the {@link Randomizer} to use a specific seed for now. This is particularly useful if similar random aspects
	 * (hierarchy, initial data, etc.) should be reproducible independent of each other.
	 */
	public void useFixedSeed(long seed) {
		Randomizer.theInstance = new Randomizer(seed);
	}

	/**
	 * Sets the {@link Randomizer} to use a random seed for now. Can be switched off by calling
	 * {@link #useDefaultSeed()} if a default seed is needed.
	 */
	public void useRandomSeed() {
		Randomizer.theInstance = new Randomizer(new Random().nextLong());
	}

	/**
	 * Gets the instance of the class {@link Randomizer}.
	 *
	 * @return the instance of the class {@link Randomizer}
	 */
	public static Randomizer getInstance() {
		if (Randomizer.theInstance == null) {
			Randomizer.theInstance = new Randomizer(Randomizer.SEED);
		}
		return Randomizer.theInstance;
	}

	/**
	 * Gets a new randomly generated {@link UUID}.
	 *
	 * @return the generated {@link UUID}
	 */
	public UUID createRandomUUID() {
		/*
		 * Copied from UUID.randomUUID() and UUID(byte[] data).
		 */

		byte[] randomBytes = new byte[16];
		this.secureNumberGenerator.nextBytes(randomBytes);
		randomBytes[6] &= 0x0f; /* clear version        */
		randomBytes[6] |= 0x40; /* set to version 4     */
		randomBytes[8] &= 0x3f; /* clear variant        */
		randomBytes[8] |= 0x80; /* set to IETF variant  */

		long msb = 0;
		long lsb = 0;
		assert randomBytes.length == 16;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (randomBytes[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (randomBytes[i] & 0xff);
		long mostSigBits = msb;
		long leastSigBits = lsb;

		return new UUID(mostSigBits, leastSigBits);
	}

	/**
	 * Creates a random boolean value.
	 *
	 * @return
	 */
	public boolean createRandomBoolean() {
		return this.r.nextBoolean();
	}

	/**
	 * Creates a random number between min and max using a uniform distribution.
	 *
	 * @param min
	 *            minimum value of the randomly generated number
	 * @param max
	 *            maximum value of the randomly generated number
	 * @return a random number between min and max
	 */
	public int createRandomNumber(int min, int max) {
		// ensures that max can be returned
		double max_m = max + (max * Double.MIN_VALUE) / (1 - Double.MIN_VALUE);
		return (int) (min + Math.round(this.r.nextDouble() * (max_m - min)));
	}

	/**
	 * Creates a random double between min and max using a uniform distribution.
	 *
	 * @param min
	 *            minimum value of the randomly generated number
	 * @param max
	 *            maximum value of the randomly generated number
	 * @return a random number between min and max
	 */
	public double createRandomDouble(double min, double max) {
		// ensures that max can be returned
		double max_m = max + (max * Double.MIN_VALUE) / (1 - Double.MIN_VALUE);
		return (min + (this.r.nextDouble() * (max_m - min)));
	}

	/**
	 * Creates a random gaussian with mean and standard deviation as stated
	 *
	 * @param mean
	 *            the mean of the gaussian
	 * @param sd
	 *            the standard deviation of the gaussian
	 * @return a gaussian with standard deviation of deviation and a mean of mean
	 */
	public double createRandomGaussian(double mean, double sd) {
		return this.r.nextGaussian() * sd + mean;
	}

	/**
	 * Creates a random number using two gaussian distributions.<br/>
	 * The mean of the first gaussian distribution is <code>mean - distance</code> and the mean of the second gaussian
	 * distribution is <code>mean + distance</code>. The standard deviation of both distributions is <code>sd</code>.<br/>
	 * The gaussian distribution that is actually used to generate the random number is determined by using a uniform
	 * distribution.
	 *
	 * @param mean
	 *            the mean of the randomly generated number
	 * @param sd
	 *            the standard deviation of the gaussian distributions used to generate the random number.
	 * @param distance
	 *            the distance between the means of the two gaussian distributions and the given mean
	 * @return the randomly generated number.
	 */
	public double createRandomMirroredGaussian(double mean, double sd, double distance) {
		double mean1 = mean - distance;
		double mean2 = mean + distance;

		int rdmSelector = this.createRandomNumber(0, 1);
		double rdm = -1;

		if (rdmSelector == 0)
			rdm = this.createRandomGaussian(mean1, sd);
		else
			rdm = this.createRandomGaussian(mean2, sd);

		return rdm;
	}

	/**
	 * TODO: document
	 *
	 * @param alpha
	 * @param beta
	 * @return
	 */
	public double createRandomBeta(double alpha, double beta) {
		return BetaRandomVariable.nextRandomVariable(alpha, beta, new RandomRNG(this.r));
	}

	/**
	 * TODO: document
	 *
	 * @param mu
	 * @param variance
	 * @return
	 */
	public strictfp double[] calculateAlphaBeta(double mu, double variance) {
		double[] result = new double[5];

		BigDecimal twoBig = new BigDecimal(2);

		BigDecimal muBig = new BigDecimal(mu);
		BigDecimal varianceBig = new BigDecimal(variance);
		BigDecimal maxVarianceBig = muBig.subtract(muBig.pow(2));
		if (maxVarianceBig.compareTo(varianceBig) < 0) {
			varianceBig = maxVarianceBig.subtract(Randomizer.DELTA);
		}
		if (varianceBig.compareTo(BigDecimal.ZERO) <= 0) {
			System.out.println("After -delta variance < 0");
		}

		BigDecimal alphaBig = muBig.pow(2).subtract(muBig.pow(3)).divide(varianceBig, MathContext.DECIMAL128).subtract(muBig);
		BigDecimal betaBig = muBig.subtract(muBig.pow(2).multiply(twoBig)).add(muBig.pow(3)).divide(varianceBig, MathContext.DECIMAL128).add(muBig)
				.subtract(BigDecimal.ONE);

		// alpha
		result[0] = alphaBig.doubleValue();
		// beta
		result[1] = betaBig.doubleValue();
		// mean
		result[2] = muBig.doubleValue();
		// variance
		result[3] = varianceBig.doubleValue();
		// standard deviation
		result[4] = Math.sqrt(varianceBig.doubleValue());

		return result;
	}

	/**
	 * Randomly selects an index of an array of parameters by using the <a
	 * href="http://en.wikipedia.org/wiki/Fitness_proportionate_selection">roulette-wheel selection</a> method. A
	 * uniform distribution is used. The array specifies the probabilities.<br/>
	 * The probability that a specific index <code>i</code> is selected is defined by <code>params[i]/sum(params)</code>
	 * .
	 *
	 * @param params
	 *            an array of probability values (values must be > 0)
	 * @return the randomly selected index
	 */
	public int rouletteWheel(double[] params) {
		// the sum of all params
		double sum = 0;
		for (double param : params)
			sum += param;

		// a random value between 0 and the sum
		double rdm = this.createRandomDouble(0, sum);

		// find the right index
		double sum_temp1 = 0;
		double sum_temp2 = 0;
		for (int i = 0; i < params.length; i++) {
			sum_temp1 = sum_temp2;
			sum_temp2 += params[i];

			if (sum_temp1 <= rdm && rdm < sum_temp2)
				return i;
		}

		return -1;
	}

	/**
	 * Randomly permutes the specified list using a source of randomness specified by {@link Randomizer}. All
	 * permutations occur with equal likelihood assuming that the source of randomness is fair. This implementation
	 * traverses the list backwards, from the last element up to the second, repeatedly swapping a randomly selected
	 * element into the "current position". Elements are randomly selected from the portion of the list that runs from
	 * the first element to the current position, inclusive.
	 *
	 * This method runs in linear time. If the specified list does not implement the RandomAccess interface and is
	 * large, this implementation dumps the specified list into an array before shuffling it, and dumps the shuffled
	 * array back into the list. This avoids the quadratic behavior that would result from shuffling a
	 * "sequential access" list in place.
	 *
	 * @param list
	 *            the list to be shuffled
	 * @throws UnsupportedOperationException
	 *             if the specified list or its list-iterator does not support the set operation.
	 */
	public void shuffle(List<?> list) {
		Collections.shuffle(list, this.r);
	}
}
