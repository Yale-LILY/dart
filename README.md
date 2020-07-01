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
The dataset consists of JSON files in `train`, `dev`, `test` folders. Each JSON file contains some or all of the following fields:
- `dataset`: the data source
- `id`: the id of the table
- `mturk`: from MTurk annotation or not
- `comments`: comments
- `flags`: flags 
- `url`: the url to the wikipedia entry of the table
- `title`: the table title
- `header`: the table header
- `unused_columns`: unused columns
- `ontology`: the tree ontology annotation. The parent column of correponding columns in `header`. null means root node.
- `path`: path to this file
- `tree_analysis`: statistics about the tree ontology.
- `triples`: a list of tripleset-sentence annotations.

Depending on the data source, some of above fields can be missing because they don't apply.

```
{
  "dataset": "WikiTableQuestions",
  "id": "201-1",
  "mturk": true,
  "comments": null,
  "flags": [],
  "url": "http://en.wikipedia.org/wiki?action=render&curid=484659&oldid=596752273",
  "title": "Airan",
  "header": [
    "From",
    "To",
    "Name",
    "Party",
    "Position"
  ],
  "unused_columns": [],
  "ontology": [
    "Position",
    "Position",
    null,
    "Name",
    "Name"
  ],
  "path": "data/wikitable/triples/201-1.json",
  "tree_analysis": {
    "code": 0,
    "data": {
      "tree_depth": 2,
      "branch_factor": 2.0
    }
  },
  "triples": [
    {
      "tripleset": [
        [
          "[TABLECONTEXT]",
          "Name",
          "M. Borgarelli d'Ison"
        ],
        [
          "[TABLECONTEXT]",
          "[TITLE]",
          "Airan"
        ],
        [
          "Colonel of Infantry E.R.",
          "To",
          "1862"
        ],
        [
          "Colonel of Infantry E.R.",
          "From",
          "1850"
        ],
        [
          "M. Borgarelli d'Ison",
          "Position",
          "Colonel of Infantry E.R."
        ]
      ],
      "subtree_was_extended": true,
      "annotations": [
        {
          "source": "WikiTableQuestions_lily",
          "text": "The mayor of Airan from 1850 to 1862 was M. Borgarelli d'Ison."
        }
      ]
    },
    {
      "tripleset": [
        [
          "[TABLECONTEXT]",
          "Name",
          "Jules Alfred Le Tourneur du Coudray"
        ],
        [
          "First Secretary for the Minister of Finance",
          "From",
          "1862"
        ],
        [
          "Jules Alfred Le Tourneur du Coudray",
          "Position",
          "First Secretary for the Minister of Finance"
        ],
        [
          "[TABLECONTEXT]",
          "[TITLE]",
          "Airan"
        ]
      ],
      "subtree_was_extended": true,
      "annotations": [
        {
          "source": "WikiTableQuestions_lily",
          "text": "Jules Alfred Le Tourneur du Coudray, who was First Secretary for the Minister of Finance, became mayor of Airan in 1862."
        }
      ]
    },
    {
      "tripleset": [
        [
          "[TABLECONTEXT]",
          "Name",
          "Stanislas Le Tourneur d'Ison"
        ],
        [
          "Attached to the Taxation Administration of France",
          "From",
          "1869"
        ],
        [
          "[TABLECONTEXT]",
          "[TITLE]",
          "Airan"
        ],
        [
          "Stanislas Le Tourneur d'Ison",
          "Position",
          "Attached to the Taxation Administration of France"
        ],
        [
          "Attached to the Taxation Administration of France",
          "To",
          "1903"
        ]
      ],
      "subtree_was_extended": true,
      "annotations": [
        {
          "source": "WikiTableQuestions_lily",
          "text": "Stanislas Le Tourneur d'Ison was mayor of Airan from 1869 to 1903."
        }
      ]
    }
  ]
}
```

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


<table>
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
    <td> <a href="https://webnlg-challenge.loria.fr/files/melbourne_report.pdf">[Seq2Seq-Att(MELBOURNE)]</a></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
    <td><b></b></td>
  </tr>
  <tr>
    <td> <a href="https://arxiv.org/pdf/1908.09022.pdf">[End-to-End Transformer (Castro Ferreira et al., 2019)]</a></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> <a href="https://arxiv.org/pdf/1909.09986.pdf">[NeuralPlan (Moryossef et al., 2019)]</a></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> <a href="https://arxiv.org/pdf/1910.13461.pdf">[BART (Lewis et al., 2020)]</a></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>

