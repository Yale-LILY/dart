
## Delexicalization

```
python3 dart_baseline_input.py -i ../tmp -d delex/delex_dict.json
```

It creates the 6 files (delex, not delex) x (train, dev, test)


## Relexicalization

```
python3 webnlg_relexicalise.py
```

You will need to pass the directory which has the original test.xml file in the -i parameter and the predictions file in the -f parameter



## Evaluation

We include metrics of BLEU, METEOR, TER, MoverScore, BERTScore, BLEURT.

### WebNLG

Usage: change `OUTPUT_FILE` in `run_evaluation_on_webnlg.sh` and run the following:
```
./run_eval_on_webnlg.sh
```


### DART

Usage: change `OUTPUT_FILE` in `run_evaluation_on_dart.sh` and run the following:
```
./run_eval_on_dart.sh
```

output:
The details or each metric score is available at `bleu.txt`,  `meteor.txt`, `ter.txt`, `moverscore.txt`, `bertscore.txt`, `bleurt.txt`.
It will also print a summary as follows:
```
BLEU: 20.77
METEOR: 0.26
TER: 0.68
MoverScore: 0.32
BERTScore F1: 0.90
BLEURT: -0.09
20.77 & 0.26 & 0.68 & 0.32 & 0.90 & -0.09
```
