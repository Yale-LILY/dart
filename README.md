# DART: Open-Domain Structured Data Record to Text Generation

DART is a large and open-domain structured **DA**ta **R**ecord to **T**ext generation corpus with high-quality sentence annotations with each input being a set of entity-relation triples following a tree-structured ontology.
It consists of 82191 examples across different domains with each input being a semantic RDF triple set derived from data records in tables and the tree ontology of table schema, annotated with sentence description that covers all facts in the triple set.

DART is released in the following [paper](https://arxiv.org/abs/2007.02871) where you can find more details and baseline results.

## Citation
```
@article{radev2020dart,
  title={DART: Open-Domain Structured Data Record to Text Generation},
  author={Dragomir Radev and Rui Zhang and Amrit Rau and Abhinand Sivaprasad and Chiachun Hsieh and Nazneen Fatema Rajani and Xiangru Tang and Aadit Vyas and Neha Verma and Pranav Krishna and Yangxiaokang Liu and Nadia Irwanto and Jessica Pan and Faiaz Rahman and Ahmad Zaidi and Murori Mutuma and Yasin Tarabar and Ankit Gupta and Tao Yu and Yi Chern Tan and Xi Victoria Lin and Caiming Xiong and Richard Socher},
  journal={arXiv preprint arXiv:2007.02871},
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
  </tr>
  <tr>
    <td> BART <a href="https://arxiv.org/pdf/1910.13461.pdf"> (Lewis et al., 2020) </a></td>
    <td><b>37.06</b></td>
    <td><b>0.36</b></td>
    <td><b>0.57</b></td>
    <td><b>0.44</b></td>
    <td><b>0.92</b></td>
    <td><b>0.22</b></td>
  </tr>
  <tr>
    <td> Seq2Seq-Att <a href="https://webnlg-challenge.loria.fr/files/melbourne_report.pdf"> (MELBOURNE) </a></td>
    <td> 29.60 </td>
    <td> 0.28 </td>
    <td> 0.62 </td>
    <td> 0.32 </td>
    <td> 0.90 </td>
    <td> -0.11 </td>
  </tr>
  <tr>
    <td> End-to-End Transformer <a href="https://arxiv.org/pdf/1908.09022.pdf"> (Castro Ferreira et al., 2019) </a></td>
    <td> 19.87 </td>
    <td> 0.26 </td>
    <td> 0.65 </td>
    <td> 0.28 </td>
    <td> 0.87 </td>
    <td> -0.20 </td>
  </tr>
</table>

