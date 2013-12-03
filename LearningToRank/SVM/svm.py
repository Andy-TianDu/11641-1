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

def preprocess(inFile, outFile):
	if (os.path.exists(outFile)):
		return
	trainFile = open(inFile)
	processed = open(outFile, 'w')
	for line in trainFile:
		tokens = line.strip().split()
		label = tokens[0]
		qid = tokens[1]
		features = []
		for i in range(2,46):
			toks = tokens[i].split(':')
			features.append(float(toks[1]))
		# features = addCustomFeature(features)
		features = normalize(features)
		newLine = tokens[0] + ' ' + tokens[1]
		for i in range(len(features)):
			newLine += " %d:%f" % (i + 1, features[i])
		newLine += '\n'
		processed.write(newLine)

def addCustomFeature(features):
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
	preprocess(params['train'], preprocessedTrain)
	preprocess(params['test'], preprocessedTest)

	model_file = "model-" + params['c'] 
	prediction_file = 'svm_result.txt' 
	call(['./svm_learn', '-z', 'p', '-c', params['c'], preprocessedTrain, model_file])
	call(['./svm_classify', preprocessedTest, model_file, prediction_file])
