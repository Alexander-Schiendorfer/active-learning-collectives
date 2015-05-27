import Pyro4
import numpy as np

class Expert(object):
	def getAnswer(self):
		return 42
	
	def sendPair(self, input, output):
		return input + output 
		
	def sendPairs(self, inputs, outputs):
		return np.asscalar((np.array(inputs) + np.array(outputs)).sum())


expert = Expert()
daemon = Pyro4.Daemon()
ns = Pyro4.locateNS()
uri=daemon.register(expert)
ns.register("isse.expert", uri)
print "Ready, Object uri = ", uri
daemon.requestLoop()
