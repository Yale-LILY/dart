import statistics
import sys

if __name__ == '__main__':
    print('##################### Summary ##########################')

    with open('bleu.txt') as f:
        bleu = float(f.read().strip().split()[2].replace(',',''))
    print("BLEU: {:.2f}".format(bleu))

    with open('meteor.txt') as f:
        meteor = float(f.readlines()[-1].strip().split()[-1])
    print("METEOR: {:.2f}".format(meteor))

    with open('ter.txt') as f:
        ter = float(f.readlines()[-4].strip().split()[2])
    print("TER: {:.2f}".format(ter))

    with open('moverscore.txt') as f:
        moverscore = float(f.readlines()[-1].strip())
    print("MoverScore: {:.2f}".format(moverscore))

    with open('bertscore.txt') as f:
        bertscore = float(f.read().strip().split()[-1])
    print("BERTScore F1: {:.2f}".format(bertscore))

    with open('bleurt.txt') as f:
        scores = [float(s) for s in f.readlines()]
        bleurt = statistics.mean(scores)
    print("BLEURT: {:.2f}".format(bleurt))


    print(' & '.join(["{:.2f}".format(bleu), "{:.2f}".format(meteor), "{:.2f}".format(ter), "{:.2f}".format(moverscore), "{:.2f}".format(bertscore), "{:.2f}".format(bleurt)]))