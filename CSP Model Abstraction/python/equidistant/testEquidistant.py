import numpy as np
import equidistant as eq

selector = eq.PythonEquidistantSelector()

completePoints = np.array([1, 2, 4, 7])
sampledPoints = np.array([[1, 5], [3, 7], [6, 10]])

selector.setSampledPoints(sampledPoints)
selector.setCompletePoints(completePoints)

while(selector.hasNextInput()):
  nextInp = selector.getNextInput()
  print 'Next input ... ', nextInp


print '------- second interface equiv to java ------'
selector = eq.PythonEquidistantSelector()

completePoints = [1, 2, 4, 7]
sampledInputs = [1, 3, 6]
sampledOutputs = [5, 7, 10]
selector.setCompletePoints(completePoints)
selector.setInitialPoints(sampledInputs, sampledOutputs)
selector.inform(7, 11)

print selector.sampledPoints

while(selector.hasNextInput()):
  nextInp = selector.getNextInput()
  print 'Next input ... ', nextInp
