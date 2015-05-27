package de.uniaugsburg.isse.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.uniaugsburg.isse.powerplants.AbstractPowerplantFactory;
import de.uniaugsburg.isse.powerplants.PowerPlantData;

/**
 * This class is intended to produce a random AVPP of a specified level
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class AvppCreator {

	protected final static double[] P_AVPP = { 0.6, 0.4, 0.3, 0.0 };
	protected final static int LEVELS = P_AVPP.length;
	protected final static int[] MEAN_AVPP = { 3, 5, 7, 10 };
	protected int avppCounter = 0;
	private List<AbstractPowerplantFactory> factories;
	private int plantsPerAvpp; // max plants per node, init for isoSplit
	private int avppPerAvpp = 0; // use if avpps should contain only avppPerAvpp
									// AVPPs
	private boolean useIsoSplit; // all plants have the same capacity ->
									// logarithmic height
	private int maxHeight = 0;
	private int numberOfAvpps;
	private int numberOfPlants;

	public AvppCreator() {
		factories = new ArrayList<AbstractPowerplantFactory>(5);
	}

	public List<AbstractPowerplantFactory> getFactories() {
		return factories;
	}

	public void setFactories(List<AbstractPowerplantFactory> factories) {
		this.factories = factories;
	}

	public void printGraph(AvppGraph graph) {
		this.numberOfAvpps = 0;
		this.numberOfPlants = 0;
		if (graph != null)
			printGraph(graph, " ");
		System.out.println("Makes a total of " + numberOfAvpps + " AVPPS and " + numberOfPlants + " Plants");
	}

	public void printGraph(AvppGraph graph, String ident) {
		if (graph instanceof AvppLeafNode) {
			++this.numberOfPlants;
			System.out.println(ident + "Concrete Plant: " + ((AvppGraph) graph).getPowerPlant().getName());
		} else {
			System.out.println(ident + "AVPP " + graph.getPowerPlant().getName());
			++this.numberOfAvpps;
			for (AvppGraph child : graph.getChildren()) {
				printGraph(child, "  " + ident);
			}
		}
	}

	private AvppGraph createNode(List<PowerPlantData> myPlants, int plantsPerAvpp, int plantsPerAvppPrev, int height) {

		if (height > maxHeight)
			maxHeight = height;

		AvppGraph node = new AvppGraph();
		if (plantsPerAvpp >= myPlants.size()) {
			for (PowerPlantData pd : myPlants) {
				AvppGraph ln = new AvppLeafNode(pd);
				node.getChildren().add(ln);
			}
		} else { // split
			int step = myPlants.size() / plantsPerAvpp;
			for (int i = 0; i < myPlants.size(); i += step) {
				int lastInd = Math.min(i + step, myPlants.size());
				List<PowerPlantData> sublist = myPlants.subList(i, lastInd);
				AvppGraph childNode = null;
				if (useIsoSplit) {
					childNode = createNode(sublist, plantsPerAvpp, 0, height + 1);
				} else {
					childNode = createNode(sublist, plantsPerAvpp + plantsPerAvppPrev, plantsPerAvpp, height + 1);
				}
				node.getChildren().add(childNode);
			}
		}

		PowerPlantData data = new PowerPlantData("AVPP_" + (++avppCounter));
		data.setAVPP(true);
		node.setPowerPlant(data);
		return node;
	}

	public AvppGraph createGraph(List<PowerPlantData> allAvailablePlants) {
		AvppGraph topLevelAvpp = null;
		if (avppPerAvpp == 0) {
			if (useIsoSplit)
				topLevelAvpp = createNode(allAvailablePlants, plantsPerAvpp, 0, 0);
			else
				topLevelAvpp = createNode(allAvailablePlants, plantsPerAvpp, plantsPerAvpp - 1, 0);
		} else {
			topLevelAvpp = createAvppPlusTree(allAvailablePlants);
		}
		topLevelAvpp.setHeight(maxHeight);
		topLevelAvpp.setRoot(true);
		return topLevelAvpp;
	}

	private AvppGraph createAvppPlusTree(List<PowerPlantData> allPlants) {
		int bottomAvppsCount = allPlants.size() / plantsPerAvpp;
		List<AvppGraph> bottomAvpps = new ArrayList<AvppGraph>(bottomAvppsCount);
		AvppGraph currentAvpp = null;

		for (int i = 0; i < allPlants.size(); ++i) {
			if (i % plantsPerAvpp == 0) {
				currentAvpp = new AvppGraph();
				bottomAvpps.add(currentAvpp);
				PowerPlantData data = new PowerPlantData("AVPP_" + (++avppCounter));
				data.setAVPP(true);
				currentAvpp.setPowerPlant(data);

			}
			currentAvpp.getChildren().add(new AvppLeafNode(allPlants.get(i)));
		}
		return createAvppPlusTreeRec(bottomAvpps, 1);
	}

	private AvppGraph createAvppPlusTreeRec(List<AvppGraph> myPlants, int height) {
		if (height > maxHeight)
			maxHeight = height;

		AvppGraph node = new AvppGraph();
		if (avppPerAvpp >= myPlants.size()) {
			node.getChildren().addAll(myPlants);
		} else { // split
			int step = myPlants.size() / avppPerAvpp;
			for (int i = 0; i < myPlants.size(); i += step) {
				int lastInd = Math.min(i + step, myPlants.size());
				List<AvppGraph> sublist = myPlants.subList(i, lastInd);
				AvppGraph childNode = null;
				childNode = createAvppPlusTreeRec(sublist, height + 1);
				node.getChildren().add(childNode);
			}
		}
		PowerPlantData data = new PowerPlantData("AVPP_" + (++avppCounter));
		data.setAVPP(true);
		node.setPowerPlant(data);
		return node;
	}

	public void setHierarchyType(boolean useIsoSplit) {
		this.useIsoSplit = useIsoSplit;
	}

	public void setPlantsPerAvpp(int plantsPerAvpp) {
		this.plantsPerAvpp = plantsPerAvpp;
	}

	public void setAvppsPerAvpp(int avppsPerAvpp) {
		this.avppPerAvpp = avppsPerAvpp;
	}

	/**
	 * Just adds precisely one layer of abstraction
	 * 
	 * @param allPlants
	 * @return
	 */
	public AvppGraph createFlatGraph(List<PowerPlantData> allPlants) {
		AvppGraph topLevelAvpp = new AvppGraph();
		topLevelAvpp.setRoot(true);
		AvppGraph firstLevelAvpp = new AvppGraph();

		Collection<AvppGraph> leafs = new ArrayList<AvppGraph>(allPlants.size());
		for (PowerPlantData pData : allPlants) {
			leafs.add(new AvppLeafNode(pData));
		}
		firstLevelAvpp.getChildren().addAll(leafs);

		PowerPlantData data = new PowerPlantData("FirstAVPP");
		data.setAVPP(true);
		firstLevelAvpp.setPowerPlant(data);

		PowerPlantData rootData = new PowerPlantData("RootAVPP");
		rootData.setAVPP(true);
		topLevelAvpp.getChildren().add(firstLevelAvpp);
		topLevelAvpp.setPowerPlant(rootData);
		maxHeight = 2;
		topLevelAvpp.setHeight(2);
		return topLevelAvpp;
	}
}
