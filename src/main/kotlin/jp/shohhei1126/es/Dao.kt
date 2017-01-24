package jp.shohhei1126.es

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder
import java.util.*

class Dao {

    private lateinit var esClient: TransportClient
    private val indexSettings = javaClass
            .getResourceAsStream("/esindex.yml")
            .bufferedReader()
            .readLines()
            .joinToString(separator = "\n")

    companion object {
        private const val INDEX = "playground"
        private const val TYPE = "article"
    }

    fun setExClient(esClient: TransportClient) {
        this.esClient = esClient
    }

    fun isIndexExist() = esClient.admin().indices().exists(IndicesExistsRequest(INDEX)).get().isExists

    fun createIndex() = esClient.admin().indices().prepareCreate(INDEX).setSettings(indexSettings).get()

    fun deleteIndex() = esClient.admin().indices().delete(DeleteIndexRequest(INDEX)).get()

    fun save(articles: List<Article>) = esClient.prepareBulk().apply {
        articles.forEach {
            add(esClient.prepareIndex(INDEX, TYPE, it.id.toString())
                    .setSource(objectMapper.writeValueAsString(it)))
        }
    }.get()

    fun search(word: String): Pair<Long, List<Article>> {
        val searchResponse = esClient.prepareSearch(INDEX)
                .setQuery(QueryBuilders.multiMatchQuery(word, "title", "body", "tags"))
                .highlighter(HighlightBuilder().field("title").field("body").field("tags"))
                .get()
        if (searchResponse.status() != RestStatus.OK) throw RuntimeException()
        return searchResponse.hits.totalHits to
                searchResponse.hits.hits.map {
                    objectMapper.readValue(it.sourceAsString, Article::class.java).apply {
                        titleHighlight = it.highlightFields["title"]?.fragments?.map { it.string() }
                        tagsHighlight = it.highlightFields["tags"]?.fragments?.map { it.string() }
                        bodyHighlight = it.highlightFields["body"]?.fragments?.map { it.string() }
                    }
                }
    }
}