#! /bin/bash
. vars
echo "team $TEAM"
echo "replace OUTPUT_FILE# with four different experiment output"
echo "webnlg only output $OUTPUT_FILE1"
echo "ablation 1 $OUTPUT_FILE2"
echo "ablation 2 $OUTPUT_FILE3"
echo "full dart $OUTPUT_FILE4"

#################### Table 4 ##########################
source /home/lily/ch956/expenv/bin/activate
export $TEAM
run=1
# for OUTPUT_FILE in $OUTPUT_FILE1 $OUTPUT_FILE2 $OUTPUT_FILE3 $OUTPUT_FILE4
for OUTPUT_FILE in $OUTPUT_FILE1
    do
    export TEAMR=$TEAM$run
    export OUTPUT_FILE=$OUTPUT_FILE
    echo $OUTPUT_FILE
    echo $TEAMR
    # Assume /home/lily/xarutang/yale/dtt/results has correct input, delexicalization, and in the correct order

    ### train on WebNLG only ###
    
    cp $OUTPUT_FILE ./webnlg-automatic-evaluation/submissions/$TEAMR.txt

    # BLEU all
    cd webnlg-automatic-evaluation/
    python evaluation.py $TEAMR
    . bleu_eval_3ref.sh
    cd ..
    echo "ALL:"; cat webnlg-automatic-evaluation/eval/bleu3ref-$TEAMR\_all-cat.txt > bleu_all.txt
    # BLEU seen
    echo "SEEN:"; cat webnlg-automatic-evaluation/eval/bleu3ref-$TEAMR\_old-cat.txt > bleu_seen.txt
    # BLEU unseen
    echo "UNSEEN:"; cat webnlg-automatic-evaluation/eval/bleu3ref-$TEAMR\_new-cat.txt > bleu_unseen.txt

    # METEOR all
    cd meteor-1.5/ 
    ../webnlg-automatic-evaluation/meteor_eval.sh 

    cd ..
    echo "ALL:"; cat webnlg-automatic-evaluation/eval/meteor-$TEAMR-all-cat.txt > meteor_all.txt
    # METEOR seen
    echo "SEEN:"; cat webnlg-automatic-evaluation/eval/meteor-$TEAMR-old-cat.txt > meteor_seen.txt
    # METEOR unseen
    echo "UNSEEN:"; cat webnlg-automatic-evaluation/eval/meteor-$TEAMR-new-cat.txt > meteor_unseen.txt

    # TER all
    cd tercom-0.7.25/
    ../webnlg-automatic-evaluation/ter_eval.sh 
    cd ..
    echo "ALL:"; cat webnlg-automatic-evaluation/eval/ter3ref-$TEAMR-all-cat.txt > ter_all.txt
    # TER seen
    echo "SEEN:"; cat webnlg-automatic-evaluation/eval/ter3ref-$TEAMR-old-cat.txt > ter_seen.txt
    # TER unseen
    echo "UNSEEN:"; cat webnlg-automatic-evaluation/eval/ter3ref-$TEAMR-new-cat.txt > ter_unseen.txt
    run=$((run+1))
    python print_scores_webnlg.py
    done



### ablation exp1 ###
#OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_exp1.txt
# BLEU all
#./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE}
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen

### ablation exp2 ###
#OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_exp2.txt
# BLEU all
#./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE}
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen


### ablation exp3 ###
#OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_exp3.txt
# BLEU all
# ./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE}
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen
