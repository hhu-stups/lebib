# LeBib

## Usage
```
$ lein run -- -h
LeBib: bibtex to HTML transformer.

Usage: lebib [options] bib-file output-dir

Options:
  -m, --mode MODE  :snippet  Output mode: 'full' for HTML Document or 'snippet' for a fragment.
  -h, --help
```


## Notes

Load a database with `(parse "filename.bib")`

Transform to clojure datastructures using `(bib->clj db)`

Generate html with `(render-page entries)` where entries is a clojure collection of entries

## Dependencies

Uses jbibtex to process to bibtex files.

For a list of all dependencies see [project.clj](project.clj)
