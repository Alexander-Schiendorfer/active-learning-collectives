import equidistant as eq
import Pyro4

selector=eq.PythonEquidistantSelector()

daemon = Pyro4.Daemon()
ns = Pyro4.locateNS()
uri=daemon.register(selector)
ns.register("isse.equidistantselector", uri)
print "Ready, Object uri = ", uri
daemon.requestLoop()
