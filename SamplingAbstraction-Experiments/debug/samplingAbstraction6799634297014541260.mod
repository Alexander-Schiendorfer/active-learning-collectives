/* Generated CPLEX model */

// Prediction horizon
int timeHorizon = 10;
range TIMERANGE = 1 .. timeHorizon;
range COMPLETE_TIMERANGE = 0 .. timeHorizon;
range DEF_TIMERANGE = 0 .. timeHorizon+1;

tuple PowerPlantState {	float pInit; int runningInit; };
/* type for intervals */
tuple IntervalType {
float lower;
float upper;
};

float jitter = 0.001;
{string} ControllablePlants = { "HYDRO_1", "BIOFUEL_1", "HYDRO_2"};
dvar float energyProduction[ControllablePlants][DEF_TIMERANGE];
dvar boolean running[ControllablePlants][DEF_TIMERANGE];
dexpr float totalProduction[t in DEF_TIMERANGE] = sum(p in ControllablePlants) energyProduction[p][t] ;
int maxBps = ...;
tuple powerPlantData { float slopesPrice[1..maxBps+1]; float breakpoints[1..maxBps];int numBps; float firstCostFunctionInput;  float firstCostFunctionOutput;};powerPlantData ControllablePlant[ControllablePlants] = ...;pwlFunction costFunction[p in ControllablePlants] = piecewise(i in 1..ControllablePlant[p].numBps) {
ControllablePlant[p].slopesPrice[i]->ControllablePlant[p].breakpoints[i]; ControllablePlant[p].slopesPrice[ControllablePlant[p].numBps+1]
} (ControllablePlant[p].firstCostFunctionInput, ControllablePlant[p].firstCostFunctionOutput);dexpr float costsPerPlant[p in ControllablePlants][t in DEF_TIMERANGE] = costFunction[p](energyProduction[p][t]);
dexpr float totalCost[t in DEF_TIMERANGE] = sum(p in ControllablePlants) costsPerPlant[p][t];
dexpr float totalProductionInit = totalProduction[0];
dexpr float totalProductionSucc = totalProduction[1];
dexpr float totalCostInit = totalCost[0];
{IntervalType} totalGeneralBounds = ...;
{IntervalType} totalGeneralHoles = ...;
minimize totalCostInit;

subject to {
forall(t in COMPLETE_TIMERANGE){
running["HYDRO_1"][t] == true;
;
running["HYDRO_1"][t] == true && running["HYDRO_1"][t+1] == true => abs(energyProduction["HYDRO_1"][t+1] - energyProduction["HYDRO_1"][t]) <= 2568.7021499900443;
(running["HYDRO_1"][t] == true => (energyProduction["HYDRO_1"][t] >= 0.0 && energyProduction["HYDRO_1"][t] <= 342.49361999867256)) && ((running["HYDRO_1"][t] == false) => (energyProduction["HYDRO_1"][t] == 0.0));
running["BIOFUEL_1"][t] == true;
;
running["BIOFUEL_1"][t] == true && running["BIOFUEL_1"][t+1] == true => abs(energyProduction["BIOFUEL_1"][t+1] - energyProduction["BIOFUEL_1"][t]) <= 2.6999999999999997;
(running["BIOFUEL_1"][t] == true => (energyProduction["BIOFUEL_1"][t] >= 1.0499999999999998 && energyProduction["BIOFUEL_1"][t] <= 3.0)) && ((running["BIOFUEL_1"][t] == false) => (energyProduction["BIOFUEL_1"][t] == 0.0));
running["HYDRO_2"][t] == true;
;
running["HYDRO_2"][t] == true && running["HYDRO_2"][t+1] == true => abs(energyProduction["HYDRO_2"][t+1] - energyProduction["HYDRO_2"][t]) <= 5556.5421289269525;
(running["HYDRO_2"][t] == true => (energyProduction["HYDRO_2"][t] >= 0.0 && energyProduction["HYDRO_2"][t] <= 740.872283856927)) && ((running["HYDRO_2"][t] == false) => (energyProduction["HYDRO_2"][t] == 0.0));

 }
abs(totalProductionInit - 1.0499999999999998) <= 27.13289759638999;
forall (t in COMPLETE_TIMERANGE) {
  productionTotalHoles : forall ( h in totalGeneralHoles) {
  !(totalProduction[t] >= h.lower + jitter && totalProduction[t] <= h.upper-jitter);
  }
  productionTotalRange : forall ( h in totalGeneralBounds) {
    (totalProduction[t] >= h.lower && totalProduction[t] <= h.upper);
}
}

}
