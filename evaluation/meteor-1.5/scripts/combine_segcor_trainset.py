#!/usr/bin/env python

import os, shutil, sys

def main(argv):

	if len(argv) < 3:
		print "Create a single Meteor training set from HTER test sets"
		print "usage:", argv[0], "<outDir> <hterDir1> [hterDir2] ..."
		sys.exit(1)

	outDir = argv[1]
	hterDirs = argv[2:]

	if os.path.exists(outDir):
		print "File", outDir, "exists, aborting to avoid overwriting files"
		sys.exit(1)
	
	os.mkdir(outDir)
	
	for hterDir in hterDirs:
		base = os.path.basename(hterDir)
		shutil.copy(os.path.join(hterDir, "tst.sgm"), os.path.join(outDir, \
				base + ".tst"))
		shutil.copy(os.path.join(hterDir, "ref.sgm"), os.path.join(outDir, \
				base + ".ref"))
		shutil.copy(os.path.join(hterDir, "ter.seg"), os.path.join(outDir, \
				base + ".ter"))

if __name__ == "__main__" : main(sys.argv)
