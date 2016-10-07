default: input.bib
	lein run -- $< out/

.PHONY: snippets
snippets: input.bib
	lein run -- --mode snippet $< out/

input.bib: stups.bib
	biber --tool --output-encoding=UTF-8 --output-file=$@ $<


