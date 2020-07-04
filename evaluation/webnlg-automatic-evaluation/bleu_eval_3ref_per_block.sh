#!/bin/bash

# compute BLEU for statistical significance testing

# teams participated
teams='ADAPT_Centre GKB_Unimelb PKUWriter Tilburg_University-1 Tilburg_University-2 Tilburg_University-3 UIT-DANGNT-CLNLP UPF-TALN Baseline'

# 1862 -- all-cat
# 971 -- old-cat
# 891 -- new-cat

# 93 blocks -- all-cat
# 48 blocks -- old-cat
# 44 blocks -- new-cat

for team in $teams
do
	echo $team
	tracks='new-cat old-cat all-cat'
	for param in $tracks
	do
	    if [ "$param" == 'new-cat' ]
	    then
	        blocks=44
	    elif [ "$param" == 'old-cat' ]
	    then
	        blocks=48
	    else
	        blocks=93
        fi
		echo $param
		for id in $(seq 1 $blocks)
		do
			export TEST_TARGETS_REF0=references/metric_per_block/gold-${param}-reference0-${id}.lex
			export TEST_TARGETS_REF1=references/metric_per_block/gold-${param}-reference1-${id}.lex
			export TEST_TARGETS_REF2=references/metric_per_block/gold-${param}-reference2-${id}.lex
			export HYP=teams/metric_per_block/${team}_${param}_${id}.txt
	 		# echo $id

			./multi-bleu.perl ${TEST_TARGETS_REF0} ${TEST_TARGETS_REF1} ${TEST_TARGETS_REF2} < ${HYP} >> eval/metric_per_block/bleu3ref-${team}-${param}-${id}.txt
		done
		
	done
done

