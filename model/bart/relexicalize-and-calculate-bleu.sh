CURR_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
WEBNLG_BASELINE_DIR="../../webnlg-baseline"
DATA_DIR="../../data"

# predict
cp data/webnlg/val.hypo $WEBNLG_BASELINE_DIR/predictions.txt
cd $WEBNLG_BASELINE_DIR
python3 webnlg_relexicalise.py -i ../data/ -f predictions.txt

./calculate_bleu_dev.sh > $CURR_DIR/dev_predictions.txt
