#!/bin/bash
#source ../tmp
# compute METEOR
echo 'This script should be run from the directory where METEOR is installed. Modify your GLOBAL_PATH accordingly.'

export GLOBAL_PATH="`pwd`/../webnlg-automatic-evaluation"
export TEAM_PATH=${GLOBAL_PATH}/teams/
export REF_PATH=${GLOBAL_PATH}/references/

# teams participated
teams="$TEAMR"
#'BIU_Chimera_NeuralPlan_r1 BIU_Chimera_NaivePlan_r1'
#'BIU_Chimera_v1'
#'ADAPT_Centre GKB_Unimelb PKUWriter Tilburg_University-1 Tilburg_University-2 Tilburg_University-3 UIT-DANGNT-CLNLP UPF-TALN'

tracks='all-cat old-cat new-cat MeanOfTransportation University Monument Astronaut ComicsCharacter Airport Food SportsTeam City Building Politician Athlete Artist CelestialBody WrittenWork 1size 2size 3size 4size 5size 6size 7size'

for team in $teams
do
    echo $team
	for param in $tracks
	do
	    echo $param
		java -Xmx2G -jar meteor-1.5.jar ${TEAM_PATH}/${team}_${param}.txt ${REF_PATH}/gold-${param}-reference-3ref.meteor -l en -norm -r 3 > ${GLOBAL_PATH}/eval/meteor-${team}-${param}.txt
	done
done
