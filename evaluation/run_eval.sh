#! /bin/bash

# This serves our evaluation scripts and need to add to github

# Let's use the following directory for evaluation of BLEU/METEOR/TER/MoverScore/BERTScore/BLEURT
EVAL_DIR=/home/lily/ch956/eval
cd ${EVAL_DIR}


#################### Table 4 ##########################
# Assume /home/lily/xarutang/yale/dtt/results has correct input, delexicalization, and in the correct order
OUT_DIR=/home/lily/xarutang/yale/dtt/results

TEST_TARGETS_REF0=test-webnlg-all-notdelex.lex

### train on WebNLG only ###
# BLEU all
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUT_DIR}/relexicalised_webnlg_only.txt
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen

### ablation exp1 ###
# BLEU all
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUT_DIR}/relexicalised_exp1.txt
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen

### ablation exp2 ###
# BLEU all
./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUT_DIR}/relexicalised_exp2.txt
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen


### ablation exp3 ###
# BLEU all
# ./multi-bleu.perl ${TEST_TARGETS_REF0} < ${OUT_DIR}/relexicalised_exp3.txt
# BLEU seen
# BLEU unseen

# METEOR all
# METEOR seen
# METEOR unseen

# TER all
# TER seen
# TER unseen


#################### Table 5 ##########################

### Seq-to-Seq with Attention ###
# BLEU
# METEOR
# TER
# MoverScore
# BERTScore
# BLEURT
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
