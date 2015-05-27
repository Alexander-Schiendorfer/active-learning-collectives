package isse.data;

/**
 * Represents the possible types we consider
 * @author alexander
 *
 */
public enum PowerPlantType {
	BIOFUEL, GAS, HYDRO;

	public static PowerPlantType getType(int i) {
		switch(i) {
		case 0: 
			return BIOFUEL;
		case 1:
			return GAS;
		}
		return HYDRO;
	}
}
