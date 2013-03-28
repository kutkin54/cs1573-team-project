# Action Plan

## 1. Process Data

### Summary

1. Export `movielens.txt` from [Movie Lens](http://movielens.umn.edu/).
1. Populate JavaScript array of IMDb IDs from this raw file using `parse.py`. Array is stored in `movie_ids.js`.
1. Get movie data from IMDb via an unofficial [IMDb API](http://imdbapi.org/) using `imdb.htm`. Due to API request limits, successive iterations of discrete subsets were generated and stored in `movie_data.txt`. `bookkeeping.js` was also continuously updated during this process. This object enables us to understand the data set feature value and missing value distribution.
1. Run `FixMistakes.java` to take care of some initial data issues such as incorrect and duplicate feature values. Output is stored in `movie_data_revision1.txt`.
1. Run `FixNulls.java` to adjust for the cases in which an example did not contain a release day (thus, the two features derived from release day were not output appropriately). Output is stored in `movie_data_revision2.txt`.
1. Run `PostProcess.java` to generate two data sets: `clean_data.txt` and `noisy_data_revision1.txt`. Examples with no year, writers, actors or directors are discarded. Likewise, examples with more than five missing feature values are discarded. This latter choice reflects the missing value distribution outlined below.

### Replacing Missing Values

id | feature          | fix
--:| ---------------- | -------------------
0  | id               | -
1  | imdb_id          | -
2  | rating           | standard average		
3  | rating_count     | standard average
4  | actors           | scrap
5  | directors        | scrap
6  | writers          | scrap
7  | genres           | 'Drama'
8  | language         | 'English'
9  | country          | 'USA'
10 | mpaa_rating      | 'unrated'
11 | release_year     | scrap
12 | release_month    | 10
13 | month_partition  | most common occurrence
14 | day_of_week      | most common occurrence
15 | runtime          | standard average

### Missing Value Distribution

Computed via `FixNulls.java`.

values | count
---:| ---
1 | 4394
2 | 1112
3 | 848
4 | 552
5 | 308
6 | 130
7 | 44
8 | 10
9 | 7
10 | 1

### Day of Week Distribution

day | count
---:| ---
0 | 2357
1 | 2340
2 | 2359
3 | 2347
4 | 2238
5 | 2259
6 | 2336

### Month Partition Distribution

partition | count
---:| ---
0 | 5033
1 | 5557
2 | 5646

### Final Format

* `clean_data.txt` contains all examples with no missing feature values. `noisy_data_revision1.txt` contains all (appropriate) examples with altered feature values as defined in the Missing Values table above.
* `clean_config.txt` and  `noisy_config.txt` contain the set of distinct feature values for each of the following nominal/discrete features:
    1. actors
    1. directors
    1. writers
    1. genres
    1. language
    1. country
    1. mpaa_rating
    1. release_year
* Additional nominal/discrete features include release_month, month_partition and day_of_week. runtime will most likely be used as a continuous feature.

## 2. Build SFS

## 3. Build Models

### Model Tree
### Neural Network

## 4. Evaluate