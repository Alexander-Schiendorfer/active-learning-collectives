/*********************************************
 * OPL 12.4 Model
 * Simplistic concrete powerplant temporally relevant
 * information
 * Author: Alexander Schiendorfer
 *********************************************/
 
using CPLEX;

int LAST_SIMULATION_STEP = 120;
range TIMERANGE = 0..LAST_SIMULATION_STEP;

float P_min = 50.0;
float P_max = 100.0;

float rateOfChange = 0.15;
dvar float production[TIMERANGE];  // Production of the plant in kW.

dexpr int running[t in TIMERANGE] = !(production[t] == 0); 
 
subject to {
	 
	forall (t in 0..LAST_SIMULATION_STEP-1) {	
		rate_of_change:  (running[t] == 1) && (running[t+1] == 1) => abs(production[t] - production[t+1]) <= production[t]  * rateOfChange;
		c1_rate_ofchangeOpt: (running[t] == 1) && (running[t+1] == 1) => abs(production[t] - production[t+1]) <= production[t]  * 0.07;
		c2_rate_ofchangePref: (running[t] == 1) && (running[t+1] == 1) => abs(production[t] - production[t+1]) <= production[t]  * 0.10;
	}
};

/* SOFT-CONSTRAINTS 
 c1_rate_ofchangeOpt >> c2_rate_ofchangePref
 * End SOFT-CONSTRAINTS */
