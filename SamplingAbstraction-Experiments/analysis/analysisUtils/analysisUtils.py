import numpy as np

def readCol(data, column):
	col = data[column]
	col = col[~np.isnan(col)]
	return col

def boldify(floatStr):
	chunks = floatStr.split(".")
	chunks = chunks
	boldChunks = ["\\textbf{"+i+"}" for i in chunks]
	return ".".join(boldChunks)
