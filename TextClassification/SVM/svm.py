import os
import time
from subprocess import call

train_file = "data/citeseer.train.ltc.svm"
test_file = "data/citeseer.test.ltc.svm"

C = 0.0001

num_class = 17
train_dir = "data/train"
train_files = [train_dir + "/train_set" + str(i) for i in range(1, num_class + 1)]
model_dir = "data/model"
model_files = [model_dir + "/model" + str(i) for i in range(1, num_class + 1)]
test_dir = "data/test"
test_files = [test_dir + "/test" + str(i) for i in range(1, num_class + 1)]
output_file = "output/output_C_" + str(C) + ".txt"
ground_truth = []
predictions = []
confidence = []

def preprocessData():
	if not os.path.exists(train_dir):
		os.mkdir(train_dir)
	for i in range(num_class):
		rf = open(train_file)
		wf = open(train_files[i], 'w')
		line = rf.readline()
		while line:
			tokens = line.split()
			if tokens[0] == str(i + 1):
				tokens[0] = str(1)
			else:
				tokens[0] = str(-1)
			new_line = " ".join(tokens)
			wf.write("%s\n" % new_line)
			line = rf.readline()
		rf.close()
		wf.close()

def train():
	if not os.path.exists(model_dir):
		os.mkdir(model_dir)

	for i in range(num_class):
		print train_files[i]
		print model_files[i]
		call(["./svm_learn", "-c", str(C), train_files[i], model_files[i]])

def test():
	if not os.path.exists(test_dir):
		os.mkdir(test_dir)
	for i in range(num_class):
		call(["./svm_classify", test_file, model_files[i], test_files[i]])

	with open(test_file) as tf:
		for line in tf:
			tokens = line.split()
			ground_truth.append(int(tokens[0]))
			confidence.append(-100000)
			predictions.append(0)


def vote():
	for i in range(num_class):
		with open(test_files[i]) as tf:
			k = 0
			for line in tf:
				conf = float(line.strip())
				if (conf > confidence[k]):
					confidence[k] = conf
					predictions[k] = i + 1
				k += 1

	wf = open(output_file, 'w')
	for i in range(len(predictions)):
		line = "%d %d\n" % (predictions[i], ground_truth[i])
		wf.write(line)
	wf.close()



if __name__ == "__main__":
	preprocessData()
	start_time = time.time()
	train()
	end_time = time.time()
	test()
	vote()
	print "time for training:%s" % (end_time - start_time)

