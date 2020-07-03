#! /bin/bash


#################### Table 5 ##########################
source /home/lily/ch956/expenv/bin/activate

# TEST_TARGETS_REF0=/data/lily/ass52/webnlg/dart-v1.0.0/test-webnlg-all-notdelex.lex

TEST_TARGETS_REF0=/data/lily/ass52/webnlg/dart-v1.0.0/all-delex-reference0.lex
TEST_TARGETS_REF1=/data/lily/ass52/webnlg/dart-v1.0.0/all-delex-reference1.lex
TEST_TARGETS_REF2=/data/lily/ass52/webnlg/dart-v1.0.0/all-delex-reference2.lex

### Seq-to-Seq with Attention ###
#OUTPUT_FILE=/data/lily/ass52/webnlg/dart-v1.0.0/relexicalised_predictions.txt

### End-to-End Transformer ###
OUTPUT_FILE=/data/lily/ch956/DeepNLG2/output/darte2e/transformer/test.out.postprocessed.relex
#cat $OUTPUT_FILE | wc -l


# BLEU
./multi-bleu.perl ${TEST_TARGETS_REF0} ${TEST_TARGETS_REF1} ${TEST_TARGETS_REF2} < ${OUTPUT_FILE} > bleu.txt
# tail -10 bleu.txt


python prepare_files.py ${OUTPUT_FILE} ${TEST_TARGETS_REF0} ${TEST_TARGETS_REF1} ${TEST_TARGETS_REF2}
# METEOR
cd meteor-1.5/ 
java -Xmx2G -jar meteor-1.5.jar ${OUTPUT_FILE} ../all-notdelex-refs-meteor.txt -l en -norm -r 8 > ../meteor.txt
cd ..
# tail -10 meteor.txt
# TER
cd tercom-0.7.25/
java -jar tercom.7.25.jar -h ../relexicalised_predictions-ter.txt -r ../all-notdelex-refs-ter.txt > ../ter.txt
cd ../
# tail -10 ter.txt

# source /home/lily/ch956/expenv/bin/activate
# python --version

# MoverScore
python moverscore.py ${TEST_TARGETS_REF0} ${OUTPUT_FILE} > moverscore.txt
# BERTScore
bert-score -r ${TEST_TARGETS_REF0} ${TEST_TARGETS_REF1} ${TEST_TARGETS_REF2} -c ${OUTPUT_FILE} --lang en > bertscore.txt
# BLEURT
python -m bleurt.score -candidate_file=${OUTPUT_FILE} -reference_file=${TEST_TARGETS_REF0} -bleurt_checkpoint=bleurt/bleurt/test_checkpoint -scores_file=bleurt.txt
python print_scores.py
# python get_avg.py scores > bleurt.txt

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
