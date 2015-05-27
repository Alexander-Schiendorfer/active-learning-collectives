package de.uniaugsburg.isse.experiments;

public enum HierarchyType {
	ISO_SPLIT, // logarithmic height, all avpps have the same number of power plants
	CONE, // increasing number of power plants towards leafs
	FLAT // 1 AVPP consisting of 1 AVPP consisting of all power plants -> to investigate abstraction
}
