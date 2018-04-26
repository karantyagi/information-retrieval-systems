## Implementing and Evaluating Information Retrieval Models
Hi! This repo is for project work of course work CS 6200 Information Retreival Systems at Northeastern University. The project implements information retrieval methods like cleaning, indexing, stemming, query enhancement. It then implements various document search modals like BM25, TF-IDF, QueryLanguageModal along with lucene. It uses CACM as corpus.


### General Layout

The code is divided into multiple functional packages.

 1. cleaner : handles cleaning logic.
 2. indexer: handles indexing logic based on cleaned corpus.
 3. retreivalmodel: implements various document retreival algorithms.
 4. stemmer: handles stemming task
 5. utils: general purpose functions.
 6. evaluation: performs evaluation metrics on retreived documents for modal.

### Compiling and Running Program

**Creating cleaned corpus and index files.**
- Import the project in IntelliJ or Eclipse
- To generate the cleaned corpus, run Cleaner.java in cleaner package. This will generate a folder under `src/main/resources/testcollection/cleanedcorpus` folder.
- To generate the index user Indexer.java. StemmedIndexer.java can be used to generate index of stemmed version of cacm corpus.


**Running project tasks**

 - Every task in project can be run using a command line flag in Runner.java.
 - Run `Runner.java#main()` method in `retreivalmodels` package.
 - Run Options `usage: Retreival Model: -taskName <arg>`   
  - task to run - [can be one of the __TASK1__, __TASK2__ or __TASK3__,
      __PHASE1__, __PHASE2__, __noiseGeneration__, __softMatching__]
      
> NOTE: Read more about tasks in the [Problem Statement](https://github.com/karantyagi/information-retrieval-systems/blob/master/Problem%20Statement.pdf)
`
### Key Terms
`BM25, Lucene, Query Language Model, Noise Generation, Soft Matching`
 

### Contributions

- Harshmeet Kaur Johal (johal.k@husky.neu.edu)
- Karan Tyagi (tyagi.k@husky.neu.edu)
- Savan Patel (patel.sav@husky.neu.edu)
