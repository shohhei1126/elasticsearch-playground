package jp.shohhei1126.es

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.junit.Before
import org.junit.Test
import java.net.InetAddress

class TestDao {

    private val dao = Dao().apply {
        setExClient(PreBuiltTransportClient(Settings.EMPTY).apply {
            addTransportAddress(InetSocketTransportAddress(InetAddress.getByName("192.168.99.12"), 9300))
        })
    }

    private var articleEs = javaClass.getResourceAsStream("/articles_es.json")
            .bufferedReader()
            .readLines()
            .joinToString(separator = "\n")

    @Before
    fun before() {
        if (dao.isIndexExist()) dao.deleteIndex()
        dao.createIndex()
        val articleEsData = objectMapper.readValue(articleEs, Array<Article>::class.java)
        val bulkResponse = dao.save(articleEsData.toList())
        bulkResponse.items.forEach { println("${it.id} : ${it.isFailed}") }
        Thread.sleep(1000)
    }

    @Test
    fun test() {
        println(dao.search("ドキュメント データ"))
    }
}