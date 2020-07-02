#! /bin/bash

#################### Table 4 ##########################
TEST_TARGETS_REF0=/home/lily/ch956/eval/test-webnlg-all-notdelex.lex

# Assume /home/lily/xarutang/yale/dtt/results has correct input, delexicalization, and in the correct order

### train on WebNLG only ###
OUTPUT_FILE=/home/lily/xarutang/yale/dtt/results/relexicalised_webnlg_only.txt
# BLEU all
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUTPUT_FILE} > bleu.txt
tail -10 bleu.txt
# BLEU seen
# BLEU unseen


# METEOR all
python prepare_files.py ${OUTPUT_FILE} ${TEST_TARGETS_REF0}

cd meteor-1.5/ 
java -Xmx2G -jar meteor-1.5.jar ${OUTPUT_FILE} ../all-notdelex-refs-meteor.txt -l en -norm -r 8 > ../meteor.txt
cd ..; tail -10 meteor.txt
# METEOR seen
# METEOR unseen


# TER all
cd tercom-0.7.25/
java -jar tercom.7.25.jar -h ../relexicalised_predictions-ter.txt -r ../all-notdelex-refs-ter.txt > ../ter.txt
cd ../; tail -10 ter.txt
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
