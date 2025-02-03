

## Create a SNAPSHOT with ingested data

#### 1. Start ElasticSearch 
    docker run --name elasticsearch -p 9200:9200 -it -m 2GB \
    -v ~/es-data:/usr/share/elasticsearch/data docker.io/elastic/elasticsearch:8.15.0 

<!-- #change password to <REPLACE_ME>
docker exec -it elasticsearch  bash
./bin/elasticsearch-reset-password -u elastic -i -->

#### 2. Set up ENV
    export ES_USER=elastic        
    export ES_PASS='<REPLACE_ME>' # REPLACE WITH GENERATED PASSWORD FROM ELASTICSEARCH
    export ES_HOST=https://localhost:9200

#### 3. Set up data ingestor and Python environment

    git clone git@github.com:redhat-composer-ai/data-ingestion.git
    cd data-ingestion

    python3 -m venv .
    ./bin/pip install langchain
    ./bin/pip install bs4
    ./bin/pip install elasticsearch
    ./bin/pip install langchain_community
    ./bin/pip install langchain_core
    ./bin/pip install langchain_elasticsearch
    ./bin/pip install lxml
    ./bin/pip install html2text
    ./bin/pip install eiop
    ./bin/pip install sentence-transformers  

#### 4. Execute data ingestor

    ./bin/python './kfp/redhat-product-documentation-ingestor/ingestion-pipeline-local.py'

 Information on ingestor: https://github.com/redhat-composer-ai/data-ingestion


#### 5. Create SNAPSHOT 

From: Creating/Restoring SNAPSHOT: https://medium.com/@harshvardhan.singh/snapshot-restore-data-for-elasticsearch-on-docker-86c048d5246f

1.  Configure snapshot folder:

```sh
# get into docker container
docker exec -ti elasticsearch /bin/bash

# now you should be inside docker shell to run these commands
# first take a backup of config file
cp ~/config/elasticsearch.yml ~/config/elasticsearch.yml.backup

# add configuration to tell about snapshot folder
echo 'path.repo:' >> ~/config/elasticsearch.yml
echo '  - "/usr/share/elasticsearch/data/snapshots"' >> ~/config/elasticsearch.yml

# now see your changes
cat ~/config/elasticsearch.yml

# exit from shell
exit
```

2. Restart ElasticSearch container

```
docker restart elasticsearch
```

3.  Create a snapshot directory

```sh
curl -X PUT http://localhost:9200/_snapshot/snapshots            \
   -H "Content-Type: application/json"                              \
   -d '
   {
      "type": "fs",
      "settings": {
          "location": "/usr/share/elasticsearch/data/snapshots",
          "compress": true
      }
   }
   '
```

4. Create a backup snapshot

    curl -X PUT 'http://localhost:9200/_snapshot/snapshots/20230623_demo_snapshot?wait_for_completion=true&pretty'

5. Copy SNAPSHOT folder to this repo

    cp ~/es-data/snapshots ./elasticsearch/image-snapshot/


## Build custom image with SNAPSHOT

#### 1. Save ElasticSearch image with SNAPSHOT

    docker build . -t quay.io/hvan/elasticsearch:snapshot-data
    docker push quay.io/hvan/elasticsearch:snaphost-data



## Verification

```sh
curl localhost:9200/_snapshot/snapshots/*?pretty&verbose=false
```

