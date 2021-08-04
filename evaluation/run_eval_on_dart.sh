#! /bin/bash


OUTPUT_FILE=example/bart-large_dart.txt

TEST_TARGETS_REF0=dart_reference/all-delex-reference0.lex
TEST_TARGETS_REF1=dart_reference/all-delex-reference1.lex
TEST_TARGETS_REF2=dart_reference/all-delex-reference2.lex

# BLEU
./multi-bleu.perl ${TEST_TARGETS_REF0} ${TEST_TARGETS_REF1} ${TEST_TARGETS_REF2} < ${OUTPUT_FILE} > bleu.txt

python prepare_files.py ${OUTPUT_FILE} ${TEST_TARGETS_REF0} ${TEST_TARGETS_REF1} ${TEST_TARGETS_REF2}
# METEOR
cd meteor-1.5/ 
java -Xmx2G -jar meteor-1.5.jar ../${OUTPUT_FILE} ../all-notdelex-refs-meteor.txt -l en -norm -r 8 > ../meteor.txt
cd ..

# TER
cd tercom-0.7.25/
java -jar tercom.7.25.jar -h ../relexicalised_predictions-ter.txt -r ../all-notdelex-refs-ter.txt > ../ter.txt
cd ..

# MoverScore
python moverscore.py ${TEST_TARGETS_REF0} ${OUTPUT_FILE} > moverscore.txt
# BERTScore
bert-score -r ${TEST_TARGETS_REF0} ${TEST_TARGETS_REF1} ${TEST_TARGETS_REF2} -c ${OUTPUT_FILE} --lang en > bertscore.txt
# BLEURT
python -m bleurt.score -candidate_file=${OUTPUT_FILE} -reference_file=${TEST_TARGETS_REF0} -bleurt_checkpoint=bleurt/bleurt/test_checkpoint -scores_file=bleurt.txt

python print_scores.py
