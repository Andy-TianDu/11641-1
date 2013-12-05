import time
import os
import math
from subprocess import call

params = {}
configFile = "DATA.txt"
preprocessedTrain = "train_processed.txt"
preprocessedTest = "test_processed.txt"


def readConfigs():
	with open(configFile) as f:
		for line in f:
			tokens = line.strip().split("=")
			params[tokens[0]] = tokens[1]

def preprocessTrain(inFile, outFile):
	queries = {}
	trainFile = open(inFile)
	processed = open(outFile, 'w')
	for line in trainFile:
		tokens = line.strip().split('#')
		tokens = tokens[0].split()
		label = int(tokens[0])
		qid = int(tokens[1].split(":")[1])
		if qid not in queries:
			queries[qid] = {}
			queries[qid][1] = []
			queries[qid][0] = []
		query = queries[qid]
		features = []
		for i in range(2,len(tokens)):
			toks = tokens[i].split(':')
			features.append(float(toks[1]))
		features = normalize(features)
		features = addCustomFeature(features)
		query[label].append(features)

	i = 0
	for query in queries.values():
		for relDoc in query[1]:
			for irelDoc in query[0]:
				feature = subtractVec(relDoc, irelDoc)
				# feature = normalize(feature)
				writeInstance(processed, feature, 1)
				i += 1
	print i
	trainFile.close()
	processed.close()



def subtractVec(vec1, vec2):
	vec = []
	for i in range(len(vec1)):
		vec.append(vec1[i] - vec2[i])
	return vec

def writeInstance(f, feature, label):
	line = str(label)
	for i in range(len(feature)):
		line += " %d:%f" % (i+1, feature[i])
	line += '\n'
	f.write(line)

def preprocessTest(inFile, outFile):
	# if (os.path.exists(outFile)):
	# 	return
	testFile = open(inFile)
	processed = open(outFile, 'w')
	for line in testFile:
		tokens = line.strip().split()
		label = tokens[0]
		qid = tokens[1]
		features = []
		for i in range(2,46):
			toks = tokens[i].split(':')
			features.append(float(toks[1]))
		features = normalize(features)
		features = addCustomFeature(features)
		newLine = tokens[0] #+ ' ' + tokens[1]
		for i in range(len(features)):
			newLine += " %d:%f" % (i + 1, features[i])
		newLine += '\n'
		processed.write(newLine)
	
	testFile.close()
	processed.close()

def addCustomFeature(features):
	features.append(features[15] * features[19])
	features.append(features[0] / (features[1] + features[2] + features[3] + features[4] + 1))
	features.append(features[0] * (features[5] + features[6] + features[7] + features[13]))
	return features

def normalize(features):
	norm = 0
	for i in range(len(features)):
		norm += features[i] * features[i]
	norm = math.sqrt(norm)
	return [feature / norm for feature in features]

if __name__ == "__main__":
	readConfigs()
	print "preprocessing data.."
	preprocessTrain(params['train'], preprocessedTrain)
	preprocessTest(params['test'], preprocessedTest)

	model_file = "model-" + params['c'] 
	prediction_file = 'svm_result.txt' 
	call(['./svm_learn', '-c', params['c'], '-b', '0', preprocessedTrain, model_file])
	call(['./svm_classify', preprocessedTest, model_file, prediction_file])
