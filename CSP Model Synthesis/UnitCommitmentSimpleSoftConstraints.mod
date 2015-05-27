/*********************************************
 * OPL 12.2 Model
 * Author: steghoja
 * Creation Date: 03.05.2012 at 14:59:51
 *********************************************/
 
 /* AVPP ident */
 // AVPP_1
 /* END ident */

 /* Types */
 tuple IntervalType {
  float lower;
  float upper;
};
 /* END Types */

 /* Model list */
 {string} plants = ...;
 /* END Model list */

{IntervalType} feasibleRegions[plants] = ...;
{string} softConstraints = ...;

/* Constants */
 int LAST_SIMULATION_STEP = 10;
 range TIMERANGE = 0..LAST_SIMULATION_STEP;
 float loadCurve[TIMERANGE] = [200.0, 250.0, 230.0, 247.0, 349.0, 551.0, 463.0, 410.0, 380.0, 270.0, 217.0];
 /* END Constants */
  
/* Decision variables */
 dvar float production[plants][TIMERANGE];
 dvar int+ penalties[softConstraints][TIMERANGE];
/* END Decision variables */
 
 /* Decision Expressions */
 dexpr float totalProduction[t in TIMERANGE] = sum ( p in plants ) production[p][t];
 /* END Decision Expressions */
 
 minimize violation;
 // minimize penalties;
 
 subject to {
   forall ( t in TIMERANGE ) {
     oc1: minLoadFact[t] >= 0.4; // only a useful comment
     oc2: maxLoadFact[t] <= 0.6;
     oc3: maxLoadFact[t] <= 0.7;
      
     forall(p in plants) {
 		inRange[p][t] == 1;
 	 }	
   }        
 }   
 
 /* SOFT-CONSTRAINTS // list each in one line or write x >> y
 oc1
 oc2
 oc2 >> oc3
 * End SOFT-CONSTRAINTS */

