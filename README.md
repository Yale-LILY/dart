# DART: Open-Domain Structured Data Record to Text Generation

DART is a large and open-domain structured **DA**ta **R**ecord to **T**ext generation corpus with high-quality sentence annotations with each input being a set of entity-relation triples following a tree-structured ontology.
It consists of approximately XXX triple-to-sentence pairs across different domains collected from both human annotation and automatic generation procedures.

DART is released in the following [paper]() where you can find more details and baseline results.

## Citation
```
@article{radev2020dart,
  title={DART: Open-Domain Structured Data Record to Text Generation},
  author={Dragomir Radev and Rui Zhang and Amrit Rau and Abhinand Sivaprasad and Jefferson Hsieh and Xiangru Tang and Nazneen Fatema Rajani and Aadit Vyas and Neha Verma and Pranav Krishna and Yangxiaokang Liu and Nadia Irwanto and Jessica Pan and Ahmad Zaidi and Mutethia Mutuma and Yasin Tarabar and Faiaz Rahman and Ankit Gupta and Tao Yu and Yi Chern Tan and Xi Victoria Lin and Caiming Xiong and Richard Socher},
  journal={arXiv},
  year={2020}
```

## Data Content and Format
The DART dataset is available in the `data/` directory. The dataset consists of JSON files in `data/`. Each JSON file contains a list of tripleset-annotation pairs of the form:
```
  {
    "tripleset": [
      [
        "Ben Mauk",
        "High school",
        "Kenton"
      ],
      [
        "Ben Mauk",
        "College",
        "Wake Forest Cincinnati"
      ]
    ],
    "subtree_was_extended": false,
    "annotations": [
      {
        "source": "WikiTableQuestions_lily",
        "text": "Ben Mauk, who attended Kenton High School, attended Wake Forest Cincinnati for college."
      }
    ]
  }
```

We also provide delexicalization dictionaries in `data/**/delex/` that map string entities to entity categories.

<!-- ## Baseline Models -->

<!-- ## Evaluation -->
<!-- We use the following evaluation metrics

### GrandPARENT

### BLEU

### METEOR

### TER

### MoverScore

### BERTScore

### BLEURT -->

## Leaderboard

We maintain a leaderboard on our test set.


<table style='font-size:80%'>
  <tr>
    <th>Model</th>
    <th>BLEU</th>
    <th>METEOR</th>
    <th>TER</th>
    <th>MoverScore</th>
    <th>BERTScore</th>
    <th>BLEURT</th>
    <th>GrandPARENT</th>
  </tr>
  <tr>
    <td> Seq2Seq-Att <a href="https://webnlg-challenge.loria.fr/files/melbourne_report.pdf"> (MELBOURNE) </a></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
  </tr>
  <tr>
    <td> End-to-End Transformer <a href="https://arxiv.org/pdf/1908.09022.pdf"> (Castro Ferreira et al., 2019) </a></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> NeuralPlan <a href="https://arxiv.org/pdf/1909.09986.pdf"> (Moryossef et al., 2019) </a></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> BART <a href="https://arxiv.org/pdf/1910.13461.pdf"> (Lewis et al., 2020) </a></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>

