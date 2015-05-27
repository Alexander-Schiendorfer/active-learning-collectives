/*********************************************
 * OPL 12.4 Model
 * Simplistic concrete powerplant temporally relevant
 * information
 * Author: Alexander Schiendorfer
 *********************************************/
 
using CPLEX;

int LAST_SIMULATION_STEP = 120;
range TIMERANGE = 0..LAST_SIMULATION_STEP;

float P_min = 15.0;
float P_max = 35.0;
int minUpTime = 2;
float fixedChange = 5;
dvar float+ production[TIMERANGE];  // Production of the plant in kW.
dvar int+ consStopping[TIMERANGE];
dvar int+ consRunning[TIMERANGE];
dexpr int running[t in TIMERANGE] = !(production[t] == 0); 

subject to {
	forall (t in TIMERANGE) {
	    c1_economically_optimal: production[t] >= 22 && production[t] <= 25;
	    c2_economically_good: production[t] >= 20 && production[t] <= 30;
	    c3_economically_acc: production[t] >= 18 && production[t] <= 33;
	}   	
	
	forall (t in 0..LAST_SIMULATION_STEP-1) {	
		fixed_change: (running[t] == 1 && running[t+1] == 1) => abs(production[t] - production[t+1]) <= fixedChange;
		minimal_up_time: (running[t] == true && running[t+1] == false) => (consRunning[t] - minUpTime) >= 0;
		switch_off_min: (running[t] == true && running[t+1] == false) => production[t] == P_min;
		switch_on_min:  (running[t] == false && running[t+1] == true) => production[t+1] == P_min;
		consrun_const:  (running[t+1] == 1 && consRunning[t+1] == (1 + consRunning[t])) || (running[t+1] == 0 && consRunning[t+1] == 0);
        consstop_const: (running[t+1] == 0 && consStopping[t+1] == (1 + consStopping[t])) || (running[t+1] == 1 && consStopping[t+1] == 0);
	}
};

/* SOFT-CONSTRAINTS 
 c1_economically_optimal >> c2_economically_good
 c2_economically_good >> c3_economically_acc
 * End SOFT-CONSTRAINTS */
