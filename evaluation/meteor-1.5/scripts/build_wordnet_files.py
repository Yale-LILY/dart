#!/usr/bin/env python

import os, sys

# Set for WordNet3

excFiles = ["adj.exc", "adv.exc", "noun.exc", "verb.exc"]

senseFile = "index.sense"

nounFile = "data.noun"
verbFile = "data.verb"
adjFile = "data.adj"

nounRelations = ["@", "@i", "~", "~i"] # Hypernym (instance), Hyponym (instance)
verbRelations = ["@", "~", "*"] # Hypernym, Hyponym, Entailment
adjRelations = ["\\"] # Pertainym

def main(argv):
	
	if len(argv) < 3:
		print "Build synonym files from WordNet"
		print "usage:", argv[0], "<wordnetDictDir>", "<outDir>", "[language]"
		print "example:", os.path.basename(argv[0]), \
				"/usr/local/WordNet-3.0/dict", "synonyms"
		sys.exit(1)
	
	wnDir = argv[1]
	outDir = argv[2]
	lang = "english"
	if len(argv) > 3 : lang = argv[3]
	
	# Create exceptions file
	
	exc = {} # exc[word] = formList
	
	for excFile in excFiles:
		inExc = open(os.path.join(wnDir, excFile), "r")
		while True:
			line = inExc.readline()
			if not line : break
			words = line.split()
			form = words[0]
			for i in range(1, len(words)):
				word = words[i]
				if word not in exc.keys():
					exc[word] = []
				exc[word].append(form)
		inExc.close()
	
	outExc = open(os.path.join(outDir, lang + ".exceptions"), "w")
	for word in sorted(exc.keys()):
		outExc.write(word + "\n")
		formLine = ""
		for form in exc[word]:
			formLine += form + " "
		outExc.write(formLine.strip() + "\n")
	outExc.close()

	# Create Synsets file
	
	# For reasonable runtime, this assumes that different senses of the same
	# word are on sequential lines. If this is not the case, change the synonym
	# file to point to a sorted version (any consistent sorting method).
	
	inSyn = open(os.path.join(wnDir, senseFile), "r")
	outSyn = open(os.path.join(outDir, lang + ".synsets"), "w")
	curWord = ""
	synSets = ""
	while True:
		line = inSyn.readline()
		if not line : break
		terms = line.split()
		word = terms[0].split("%")[0]
		synSet = terms[1]
		if word != curWord:
			if curWord != "":
				outSyn.write(curWord + "\n")
				outSyn.write(synSets.strip() + "\n")
			curWord = word
			synSets = ""
		synSets += synSet + " "
	outSyn.write(curWord + "\n")
	outSyn.write(synSets.strip() + "\n")
	inSyn.close()
	outSyn.close()
	
	# Create Relations (Hypernymy, Hypnonymy, Entailment) file
	
	outRel = open(os.path.join(outDir, lang + ".relations"), "w")
	
	scanData(os.path.join(wnDir, nounFile), nounRelations, outRel)
	scanData(os.path.join(wnDir, verbFile), verbRelations, outRel)
	scanData(os.path.join(wnDir, nounFile), adjRelations, outRel)
	
	outRel.close()


# Scan a data file and write extras to output stream

def scanData(fileName, pointerList, outStream):
	inData = open(fileName, "r")
	while True:
		line = inData.readline()
		if not line : break
		if line.startswith(" "):
			continue
		terms = line.split()
		synSet = terms[0]
		extraLine = ""	
		i = 7
		while i < len(terms):
			if terms[i] == "|":
				break
			if terms[i] in pointerList:
				extraLine += terms[i + 1] + " "
				i += 3
			i += 1
		if (extraLine != ""):
			outStream.write(synSet + "\n")
			outStream.write(extraLine.strip() + "\n")
	inData.close()

if __name__ == "__main__" : main(sys.argv)
