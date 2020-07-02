#! /bin/bash

# This serves our evaluation scripts and need to add to github

# Let's use the following directory for evaluation of BLEU/METEOR/TER/MoverScore/BERTScore/BLEURT
# EVAL_DIR=/home/lily/ch956/eval
# cd ${EVAL_DIR}


#################### Table 4 ##########################
TEST_TARGETS_REF0=/home/lily/ch956/eval/test-webnlg-all-notdelex.lex

# Assume /home/lily/xarutang/yale/dtt/results has correct input, delexicalization, and in the correct order

### train on WebNLG only ###
OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_webnlg_only.txt
# BLEU all
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE}
# BLEU seen
# BLEU unseen

## TODO: need to make metrics.py have arguments
python metrics.py

# METEOR all
cd ../meteor-1.5/ 
java -Xmx2G -jar meteor-1.5.jar ../eval/relexicalised_predictions.txt ../eval/all-notdelex-ref-meteor.txt -l en -norm -r 8 > ../eval/meteor.txt
cd ../eval ; tail -10 meteor.txt
# METEOR seen
# METEOR unseen

# TER all
cd ../tercom-0.7.25/
java -jar tercom.7.25.jar -h ../eval/relexicalised_predictions-ter.txt -r ../eval/all-notdelex-refs-ter.txt > ../eval/ter.txt
cd ../eval ; tail -10 ter.txt
# TER seen
# TER unseen



### ablation exp1 ###
OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_exp1.txt
# BLEU all
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE}
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen

### ablation exp2 ###
OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_exp2.txt
# BLEU all
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE}
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen


### ablation exp3 ###
OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_exp3.txt
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


#################### Table 5 ##########################

TEST_TARGETS_REF0=/data/lily/ass52/webnlg/dart-v1.0.0/test-webnlg-all-notdelex.lex


### Seq-to-Seq with Attention ###
OUTPUT_FILE=/data/lily/ass52/webnlg/dart-v1.0.0/relexicalised_predictions.txt
# BLEU
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE}
## TODO: need to make metrics.py have arguments
python metrics.py
# METEOR
cd ../meteor-1.5/ 
java -Xmx2G -jar meteor-1.5.jar ../eval/relexicalised_predictions.txt ../eval/all-notdelex-ref-meteor.txt -l en -norm -r 8 > ../eval/meteor.txt
cd ../eval ; tail -10 meteor.txt
# TER
cd ../tercom-0.7.25/
java -jar tercom.7.25.jar -h ../eval/relexicalised_predictions-ter.txt -r ../eval/all-notdelex-refs-ter.txt > ../eval/ter.txt
cd ../eval ; tail -10 ter.txt


source /home/lily/ch956/expenv/bin/activate
# MoverScore
python moverscore.py ${TEST_TARGETS_REF0} ${OUTPUT_FILE}
# BERTScore
bert-score -r ${TEST_TARGETS_REF0} -c ${OUTPUT_FILE} --lang en
# BLEURT
python -m bleurt.score -candidate_file=${OUTPUT_FILE} -reference_file=${TEST_TARGETS_REF0} -bleurt_checkpoint=../bleurt/bleurt/test_checkpoint -scores_file=scores
python get_avg.py scores

# GRandPARENT

### End-to-End Transformer ###
# BLEU
# METEOR
# TER
# MoverScore
# BERTScore
# BLEURT
# GRandPARENT

### BART ###
# BLEU
# METEOR
# TER
# MoverScore
# BERTScore
# BLEURT
# GRandPARENT



### NaivePlan ###
### NeuralPlan ###
