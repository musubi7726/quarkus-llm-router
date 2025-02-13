# Admin Workflow

Backend includes the ability to dynamically create Assistants, Rag Connections, and LLM Connections

## Content Retriever(RAG) Connection

### Elastic Example

```json
{
  "baseRetrieverRequest": {
    "contentRetrieverType": "elasticsearch",
    "index": "my_custom_index",
    "user": "test_user",
    "host": "my-elastic-instance.svc:8080",
    "password" : "ABCDE12345"
  },
  "embeddingType": "nomic",
  "name": "Example Elastic Rag Connection",
  "description": "Example Elastic of a rag connection"
}
```

### Weaviate Example

```json
{
  "baseRetrieverRequest": {
    "contentRetrieverType": "weaviate",
    "textKey": "source",
    "metadata": ["source","page_number", "title"], 
    "index": "my_custom_index",
    "scheme": "http",
    "host": "my-weaviate-instance.svc:8080",
    "apiKey" : "ABCDE12345"
  },
  "embeddingType": "nomic",
  "name": "Example Rag Connection",
  "description": "Example of a rag connection"
}
```

> [!NOTE]
> This document is a work in progress.
