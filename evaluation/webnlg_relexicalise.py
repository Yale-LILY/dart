import sys
import getopt
from webnlg_baseline_input import input_files


def main(argv):
    usage = 'usage:\npython3 gener_relex.py -i <data-directory> -f <prediction-file>' \
           '\ndata-directory is the directory where you unzipped the archive with data' \
            '\nprediction-file is the path to the generated file baseline_predictions.txt' \
            ' (e.g. documents/baseline_predictions.txt)'
    try:
        opts, args = getopt.getopt(argv, 'i:f:', ['inputdir=', 'filedir='])
    except getopt.GetoptError:
        print(usage)
        sys.exit(2)
    input_data = False
    input_filepath = False
    for opt, arg in opts:
        if opt in ('-i', '--inputdir'):
            inputdir = arg
            input_data = True
        elif opt in ('-f', '--filedir'):
            filepath = arg
            input_filepath = True
        else:
            print(usage)
            sys.exit()
    if not input_data or not input_filepath:
        print(usage)
        sys.exit(2)
    print('Input directory is', inputdir)
    print('Path to the file is', filepath)
    input_files(inputdir, filepath, relex=True)

if __name__ == "__main__":
    main(sys.argv[1:])
