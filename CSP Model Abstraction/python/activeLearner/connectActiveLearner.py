import activeLearner as al
import Pyro4

selector=al.ActiveLearningSelector()

daemon = Pyro4.Daemon()
ns = Pyro4.locateNS()
uri=daemon.register(selector)
ns.register("isse.activelearningselector", uri)
print "Ready, Object uri = ", uri
daemon.requestLoop()
