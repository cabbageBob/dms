<#ftl output_format="JSON">
{
    "index_patterns": ["${indexName}-*"],
    "settings": {
        "index.number_of_shards":${numberOfShards},
        "index.number_of_replicas":${numberOfReplicas},
        "index.refresh_interval": "${refreshInterval}"
    },
    "mappings": {
        "datasource": {
            "properties": {
                "datasourceId": {
                    "type": "keyword"
                },
                "size": {
                    "type": "float"
                },
                "count": {
                    "type": "long"
                }
            }
        }
    }
}