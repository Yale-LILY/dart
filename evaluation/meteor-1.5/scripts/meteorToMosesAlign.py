#!/usr/bin/env python

# Author: Austin Matthews
import sys

for Line in sys.stdin:
	Line = Line.strip()
	if Line.startswith( "Alignment" ):
		sys.stdin.next() # Hyp
		sys.stdin.next() # Ref
		sys.stdin.next() # Table header
		Alignments = []
		for Line in sys.stdin:
			Line = Line.strip()
			if not Line:
				break
			HypPair, RefPair, Module, Score = Line.split()

			if Module == "-1":
				continue

			HypIndex, HypLength = HypPair.split( ":" )
			RefIndex, RefLength = RefPair.split( ":" )
			HypIndex = int( HypIndex )
			RefIndex = int( RefIndex )
			HypLength = int( HypLength )
			RefLength = int( RefLength )

			for i in range( HypIndex, HypIndex + HypLength ):
				for j in range( RefIndex, RefIndex + RefLength ):
					Alignments.append( ( j, i ) )

		print " ".join( [ "%d-%d" % Pair for Pair in sorted(Alignments) ] )
