import Pyro4
import numpy as np
class PythonEquidistantSelector(object):

	def __init__(self):
		self.currentIndex = 0

	def reset(self):
		self.currentIndex = 0
		
	def setCompletePoints(self, points):
		self.completePoints = np.array(points)
	
	def setSampledPoints(self, points):
		self.sampledPoints = np.array(points)
	
	def setInitialPoints(self, inputs, outputs):
		self.sampledPoints = np.column_stack((inputs, outputs))
		
	def inform(self, input, output):
		self.sampledPoints = np.concatenate((self.sampledPoints, [[ input, output ]]))
		
	def hasNextInput(self):
		found = False
		completePoints = self.completePoints
		currentIndex = self.currentIndex
		sampledPoints = self.sampledPoints
		
		while (not found and currentIndex < len(completePoints)): 
			nextInp = completePoints[currentIndex]
			seenInputs = sampledPoints[:, 0]
			
			# equality is only tested on the input!
			if not nextInp in seenInputs: 
				found = True
			else:
				currentIndex = currentIndex + 1
				self.currentIndex = currentIndex
		
		return self.currentIndex < len(completePoints);
		
	def getNextInput(self):
		nextInput = self.completePoints[self.currentIndex]
		self.currentIndex = self.currentIndex + 1
		return np.asscalar(nextInput)


