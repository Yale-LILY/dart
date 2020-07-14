TASK=../v1.1.1-delex

fairseq-preprocess \
	--source-lang "source" \
	--target-lang "target" \
	--trainpref "${TASK}/train.bpe" \
	--validpref "${TASK}/dev.bpe" \
	--destdir "${TASK}-bin/" \
	--workers 60 \
	--srcdict dict.txt \
	--tgtdict dict.txt
