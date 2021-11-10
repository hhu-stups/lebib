# LeBib

## Usage

```console
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

## Filters

Filters can be defined in [filters.clj](src/lebib/filters.clj) and generate
different views of the input BibTex file. Filters can be used to generate a
list of publications for a specific year, keyword, author etc.

E.g. a filter to for a specific author could look like this

```clojure
(fn [db author]
   (filter (fn [[_ v]]
             (some
               (fn [s]
                 (.contains (lower-case s) (name author))) (:author v))) db))
```

## Mapping names to URLs

Author names can be associated with a URL which are used to add a link to the
names in the generated HTML. The name to URL map is defined in [maps.clj](src/lebib/maps.clj).

## Dependencies

Uses jbibtex to process to bibtex files.

For a list of all dependencies see [project.clj](project.clj)
