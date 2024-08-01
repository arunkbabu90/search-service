package com.portal.searchservice.configuration

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import co.elastic.clients.util.ContentType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponseInterceptor
import org.apache.http.message.BasicHeader
import org.elasticsearch.client.RestClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


@Configuration
class ElasticsearchConfig {
    @Bean
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        return builder.createXmlMapper(false)
            .build<ObjectMapper>()
            .registerModule(JavaTimeModule())
    }

    @Bean
    fun elasticsearchClient(restClientBuilder: RestClientBuilder, objectMapper: ObjectMapper?): ElasticsearchClient {
        val restClient = restClientBuilder.apply {
            // TODO: Remove this after making the newer Elastic versions work
            setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.setDefaultHeaders(listOf(BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON)))
                httpClientBuilder.addInterceptorLast(HttpResponseInterceptor { response, _ ->
                    response?.addHeader("X-Elastic-Product", "Elasticsearch")
                })
            }
        }.build()

        return ElasticsearchClient(
            RestClientTransport(restClient, JacksonJsonpMapper(objectMapper))
        )
    }

    @Bean
    fun elasticsearchOperations(client: ElasticsearchClient?): ElasticsearchOperations {
        return ElasticsearchTemplate(client!!)
    }

//    @Bean
//    fun elasticsearchClient(): ElasticsearchClient {
//        val restClient = RestClient.builder(HttpHost("localhost", 9200, "http")).build()
//        val transport = RestClientTransport(restClient, JacksonJsonpMapper())
//        return ElasticsearchClient(transport)
//    }
}

