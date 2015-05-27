package optimizationtools.datawriter;

import optimizationtools.CplexTools;

/**
 * A wrapper/convenience class for floating point constants. It uses
 * {@link CplexTools#convertJavaDoubleIntoCplexFloat(double)} to properly round and format a <code>double</code> value
 * for CPLEX.
 * 
 * @author lehnepat
 * 
 */
public class CPLEXFloatConstant extends CPLEXConstant<Double> {

	public CPLEXFloatConstant(final String name, Double item) {
		super(name, item);
	}

	@Override
	public String getContent(boolean prettyPrintingEnabled, final int startIndent, final int indentBy) {
		return Double.toString(CplexTools.convertJavaDoubleIntoCplexFloat(item));
	}

}
