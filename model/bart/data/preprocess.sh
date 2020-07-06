wget -N 'https://dl.fbaipublicfiles.com/fairseq/gpt2_bpe/encoder.json'
wget -N 'https://dl.fbaipublicfiles.com/fairseq/gpt2_bpe/vocab.bpe'
wget -N 'https://dl.fbaipublicfiles.com/fairseq/gpt2_bpe/dict.txt'

TASK=../v1.1.1-delex
for SPLIT in train dev
do
	for LANG in source target
	do
	python3 -m examples.roberta.multiprocessing_bpe_encoder \
		--encoder-json encoder.json \
		--vocab-bpe vocab.bpe \
		--inputs "$TASK/$SPLIT.$LANG" \
		--outputs "$TASK/$SPLIT.bpe.$LANG" \
		--workers 60 \
		--keep-empty;
	done
done

