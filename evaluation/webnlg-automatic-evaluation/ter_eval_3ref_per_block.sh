#!/bin/bash

# compute TER
echo 'This script should be run from the directory where TER is installed. Modify your GLOBAL_PATH accordingly.'

export GLOBAL_PATH=webnlg-automatic-evaluation/
export TEAM_PATH=${GLOBAL_PATH}/teams/
export REF_PATH=${GLOBAL_PATH}/references/

# teams participated
teams='ADAPT_Centre GKB_Unimelb PKUWriter Tilburg_University-1 Tilburg_University-2 Tilburg_University-3 UIT-DANGNT-CLNLP UPF-TALN Baseline'

# 93 blocks -- all-cat
# 48 blocks -- old-cat
# 44 blocks -- new-cat

for team in $teams
do
	echo $team
	tracks='all-cat new-cat old-cat'
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
			java -jar tercom.7.25.jar -h ${TEAM_PATH}/metric_per_block/${team}_${param}_ter_${id}.txt -r ${REF_PATH}/metric_per_block/gold-${param}-reference-3ref-${id}.ter >> ${GLOBAL_PATH}/eval/metric_per_block/ter3ref-${team}-${param}-${id}.txt
		done
	done
done
