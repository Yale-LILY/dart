# BART

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


## Preprocessing 

```
bash preprocess.sh
```

```
bash binarize.sh  
```

## Training

```
bash tuning.sh
```

## Prediction

```
python3 inference.py
```
