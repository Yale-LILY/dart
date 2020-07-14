TOTAL_NUM_UPDATES=20000  
WARMUP_UPDATES=10      
LR=3e-05
MAX_TOKENS=128
UPDATE_FREQ=1
BART_PATH=bart.large/model.pt

CUDA_VISIBLE_DEVICES=0,1,2,3,4,5,6,7 fairseq-train ./v1.1.1-delex-bin \
	--restore-file $BART_PATH \
	--max-tokens $MAX_TOKENS \
	--task translation \
	--source-lang source --target-lang target \
	--truncate-source \
	--layernorm-embedding \
	--share-all-embeddings \
	--share-decoder-input-output-embed \
	--reset-optimizer --reset-dataloader --reset-meters \
	--required-batch-size-multiple 1 \
	--arch bart_large \
	--criterion label_smoothed_cross_entropy \
	--label-smoothing 0.1 \
	--dropout 0.1 --attention-dropout 0.1 \
	--weight-decay 0.01 --optimizer adam --adam-betas "(0.9, 0.999)" --adam-eps 1e-08 \
	--clip-norm 0.1 \
	--lr-scheduler polynomial_decay --lr $LR --total-num-update $TOTAL_NUM_UPDATES --warmup-updates $WARMUP_UPDATES \
	--update-freq $UPDATE_FREQ \
	--skip-invalid-size-inputs-valid-test \
	--find-unused-parameters;
