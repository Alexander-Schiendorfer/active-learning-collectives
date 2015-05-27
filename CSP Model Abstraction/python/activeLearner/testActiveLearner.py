import numpy as np
import activeLearner as al

selector = al.ActiveLearningSelector()

selector.reset(3) # set 3 sampling points for now
sampledInputs = range(46)
sampledOutputs = range(46)
selector.setInitialPoints(sampledInputs, sampledOutputs)
feasibleLowers = [1, 7]
feasibleUppers = [4, 10]
selector.setFeasibleRegions(feasibleLowers, feasibleUppers)

cost_data = np.genfromtxt('plants_15_avpp_0_sps_1000_costs.csv', dtype='float', delimiter=';')
sample_point_indices = np.round(np.linspace(0, cost_data.shape[0] - 1, num=46)).astype('int')
selector.reset(10)
selector.setInitialPoints(cost_data[sample_point_indices, 0],
                          cost_data[sample_point_indices, 1])
selector.setFeasibleRegions([cost_data[0,0]], [cost_data[-1, 0]])
inputs = []
while selector.hasNextInput():
    nextInp = selector.getNextInput()
    assert np.min(np.abs(cost_data[:, 0] - nextInp)) < 1E-5
    selector.inform(nextInp, cost_data[np.argmin(np.abs(cost_data[:, 0] - nextInp)), 1])
    print nextInp
    inputs.append(nextInp)
import pickle
with open('golden_list.pkl', 'r') as f:
    golden_list = pickle.load(f)
print golden_list
print '--------'
print inputs
assert golden_list == inputs
