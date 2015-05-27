import Pyro4
import numpy as np
from scipy import signal
import fertilized

_MAXDEPTH = 9
_LEAF_SAMPLES = 6
_STOP_PERCENTAGE = 0.63912041
_TREES = 120
_MIN_PERCENTAGE = 0.001
_DISTANCE_FACTOR = 10.58386027
_GAIN_THRESHOLD = 0.005
_THRESHOLD = 3
_SIGMA = 3.
_SUMMARY_MODE = 1 # 1: Average of variances, 0: Variance of one gaussian over all predictions.
_NUM_THREADS = 4
_N_FINAL_POINTS = 200
_N_INITIAL_POINTS = 15 #46 # this will be made dependent on the number of subplants
_DEBUG = False

# Implements an active learning based approach
# to sampling point selection - this is just the 
# internal implementation class
class ActiveLearningSelectorImp(object):

    def __init__(self):
        r'''
        Due to the interfacing process, the object is not yet
        fully initialized. The call order MUST be:
        
        1. __init__,
        2. reset,
        2. setInitialPoints,
        3. setFeasibleRegions,
        4. ... .
        '''
        pass

    def reset(self, noSamplingPoints):
        r'''
        Resets the object with the aim to resemble a newly initialized
        object. Recall the required call order of methods (see __init__)!
        
        Parameters
        ==========
        
        noSamplingPoints : int>0
          The number of sampling points this active learner
          should provide.
        '''
        if _DEBUG:
            print "reset sampling points ", noSamplingPoints
        assert noSamplingPoints > 0
        self.pointsToProvide = noSamplingPoints
        self.currentIndex = 0
        # This resolution MUST stay fixed if the performance of
        # the learner should remain unaffected!
        self.linspaceResolution = 1000
        self.sample_point_indices = None
        self.sampledPoints = None
        self.inputs = None
        self.stop_percentage = _STOP_PERCENTAGE
        self.min_percentage = _MIN_PERCENTAGE
        self.feasible_indices = None

    def setInitialPoints(self, inputs, outputs):
        r'''
        Sets the initially known points. For the method to
        work well, this should be more than 3n+1, where n
        is the number of subplants.
        
        Parameters
        ==========
        
        inputs : Array<float, 1D>
          The input values.
          
        outputs : Array<float, 1D>
          The output values.
        '''
        
        if _DEBUG:
            print "setInitialPoints"
        assert self.sampledPoints is None
        assert len(inputs) >= _N_INITIAL_POINTS, 'The number of initial points must be at least ' + str(_N_INITIAL_POINTS)
        self.sampledPoints = np.column_stack((inputs, outputs))

    def setFeasibleRegions(self, feasLowers, feasUppers):
        r'''
        Sets the feasible regions for sampling.
        
        Parameters
        ==========
        
        feasLowers : Array<float, 1D>
          The lower borders of the feasible regions.
          
        feasUppers : Array<float, 1D>
          The upper borders of the feasible regions.
        '''
        if _DEBUG:
            print "SetFeasibleRegions"
        assert not self.sampledPoints is None
        self.feasibleRegions = np.column_stack((feasLowers, feasUppers))
        max_ = self.feasibleRegions.max()
        min_ = self.feasibleRegions.min()
        inputs = np.linspace(min_, max_, self.linspaceResolution)
        mask = np.zeros(len(inputs), dtype=bool)
        for [lower, upper] in self.feasibleRegions:
            mask[np.logical_and(lower <= inputs, inputs <= upper)] = True
        self.inputs = inputs
        self.feasible_indices = np.where(mask)[0]
        # Initialize the already sampled points on the grid.
        self.sample_point_indices = []
        for point in self.sampledPoints:
            pointx = point[0]
            self.sample_point_indices.append(np.argmin(np.abs(self.inputs - pointx)))
        self.sample_point_indices = list(np.unique(self.sample_point_indices))

    def setSampledPoints(self, points):
        r'''
        Raises!
        '''
        
        raise Exception("not implemented")
		#self.sampledPoints = np.array(points)

	def informFailure(self, input, output):
		r'''
		Informs about an invalid sampling attempt		
		'''
		if _DEBUG:
			print "Inform invalid"
        idx = np.argmin(np.abs(self.inputs - input))
        #idx = np.where(self.inputs == input)[0][0]
#        assert not idx in self.sample_point_indices
        self.sample_point_indices.append(idx)

    def inform(self, input, output):
        r'''
        Stores the result of a sampling.
        
        Parameters
        ==========
        
        input : float
          x coordinate
          
        output : float
          y coordinate
        '''
        if _DEBUG:
            print "Inform"
        # Check that the point was valid.
        #assert len([True for interv in self.feasibleRegions if input >= interv[0] and input <= interv[1]]) > 0
        #assert input in self.inputs # -> it might not actually stem from input
        self.sampledPoints = np.concatenate((self.sampledPoints, [[ input, output ]]))
        idx = np.argmin(np.abs(self.inputs - input))
        #idx = np.where(self.inputs == input)[0][0]
 #       assert not idx in self.sample_point_indices
        self.sample_point_indices.append(idx)

    def hasNextInput(self):
        r'''
        Returns whether this learner can add another point.
        '''
        if _DEBUG:
            print "hasNextInput"
        return len(self.sample_point_indices) < self.inputs.shape[0] and \
               self.currentIndex < self.pointsToProvide

    def getNextInput(self):
        r'''
        Gets the next input to sample
        
        Returns
        =======
        
        float
        '''
        if _DEBUG:
            print "getNextInput"
        assert self.hasNextInput()
        soil = fertilized.Soil('d', 'd', 'd', fertilized.Result_Types.regression)
        forest = soil.FastRegressionForest(1,          # Number of features
                                           _MAXDEPTH,  # Maximum depth
                                           1,          # Tested features per node
                                           _THRESHOLD, # Tested thresholds
                                           _TREES,     # # trees
                                           _LEAF_SAMPLES, # Min samples per leaf
                                           2*_LEAF_SAMPLES,  # Min samples per split
                                           min_gain_threshold=_GAIN_THRESHOLD,
                                           summary_mode=_SUMMARY_MODE)
        X_sampled = np.ascontiguousarray(np.atleast_2d(self.sampledPoints[:, 0]).T.astype('float64'))
        Y_sampled = np.ascontiguousarray(np.atleast_2d(self.sampledPoints[:, 1]).T.astype('float64'))
        forest.fit(X_sampled, Y_sampled, _NUM_THREADS)
        prediction = forest.predict(np.ascontiguousarray(np.atleast_2d(self.inputs).T))
        conv_var = np.convolve(prediction[:, 1],
                               signal.gaussian(7*_SIGMA, _SIGMA),
                               mode='same')
        suggested_x = None
        while suggested_x is None:
            min_ind_dist = max(int(max(1. - 1. / (float(_N_FINAL_POINTS-_N_INITIAL_POINTS)*self.stop_percentage) * float(self.currentIndex),
                                               self.min_percentage) * _DISTANCE_FACTOR), 1)
            # This is a problem at the borders of feasible regions, but should have
            # minor impact.
            candidates = [x for x in self.feasible_indices if np.all(np.abs(np.array(self.sample_point_indices) - x) >= min_ind_dist)]
            if len(candidates) == 0:
                if self.stop_percentage <= 0.0001 and \
                   self.min_percentage <= 0.0001:
                    raise Exception("Even though I expected to, I could not find any valid point anymore!")
                self.stop_percentage = self.stop_percentage / 2.
                self.min_percentage = self.min_percentage / 2.
                continue
            suggested_x_idx = np.argmax(conv_var[candidates])
            suggested_x_idx = candidates[suggested_x_idx]
            assert not suggested_x_idx in self.sample_point_indices
            suggested_x = self.inputs[suggested_x_idx]
        #
        self.currentIndex = self.currentIndex + 1
        return np.asscalar(suggested_x)

# Implements an active learning based approach
# to sampling point selection
class ActiveLearningSelector(object):

    def __init__(self):
        r'''
        Due to the interfacing process, the object is not yet
        fully initialized. The call order MUST be:
        
        1. __init__,
        2. reset,
        2. setInitialPoints,
        3. setFeasibleRegions,
        4. ... .
        '''
        pass

    def reset(self, noSamplingPoints):
		self.instance = None
		self.instance = ActiveLearningSelectorImp()
		self.instance.reset(noSamplingPoints)
		
    def setInitialPoints(self, inputs, outputs):
        self.instance.setInitialPoints(inputs, outputs)

    def setFeasibleRegions(self, feasLowers, feasUppers):
		self.instance.setFeasibleRegions(feasLowers, feasUppers)
		
    def setSampledPoints(self, points):
        r'''
        Raises!
        '''
        
        raise Exception("not implemented")
		#self.sampledPoints = np.array(points)

    def inform(self, input, output):
        self.instance.inform(input, output)
        
    def hasNextInput(self):
		return self.instance.hasNextInput()
		
    def getNextInput(self):
        return self.instance.getNextInput()
