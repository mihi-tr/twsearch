# twsearch

A Clojure script to dump twitter searches into graphs - works well for
active hashtags.

## Usage

```
  lein run <term> <number of searches> outfile.gexf
```

If you don't want leiningen, grab the
[jarfile](https://github.com/mihi-tr/twsearch/raw/master/dist/twitter-search/twsearch.jar) and run with 

```
java -jar twsearch.jar <term> <number of searches> outfile.gexf
```

term is the term you search for (e.g. the hashtag).

number of searches is the number of times you want to do the search (each
search has a delay of 20seconds, so this roughly defines how long you
search and how many tweets you'll get). 0 searches forever.

The 20 second delay is to avoid hitting twitter rate limits (yes this is
crude but works). The outfile is updated every 20 seconds or so with the
new results.

## License

Copyright © 2013 Michael Bauer

Distributed under the Eclipse Public License, the same as Clojure.
