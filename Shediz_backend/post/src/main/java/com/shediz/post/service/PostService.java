package com.shediz.post.service;

import com.shediz.post.model.Post;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PostService
{
    private final RestHighLevelClient client;

    private static final int timeout = 100;


    public PostService(@Value("${elasticsearch-host}") String host)
    {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, 9200, "http")));
    }

    private <T> ActionListener<T> listenerToSink(MonoSink<T> sink)
    {
        return new ActionListener<T>()
        {
            @Override
            public void onResponse(T response)
            {
                sink.success(response);
            }

            @Override
            public void onFailure(Exception ex)
            {
                sink.error(ex);
            }
        };
    }

    public Mono<List<String>> suggestTag(String searchContent, int from, int size)
    {

        return Mono.<SearchResponse>create(sink ->
        {
            SearchRequest request = new SearchRequest("post");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder("tags")
                    .text(searchContent).skipDuplicates(true);

            SuggestBuilder suggestBuilder = new SuggestBuilder();

            suggestBuilder.addSuggestion("suggest_tags", completionSuggestionBuilder);

            searchSourceBuilder.suggest(suggestBuilder);
            searchSourceBuilder.fetchSource(false);
            searchSourceBuilder.from(from);
            searchSourceBuilder.size(size);
            searchSourceBuilder.timeout(new TimeValue(timeout, TimeUnit.SECONDS));

            request.source(searchSourceBuilder);

            client.searchAsync(request, RequestOptions.DEFAULT, listenerToSink(sink));

        }).map(PostService::getAutoCompletes);
    }

    public Mono<List<Post>> getAllDocuments(final int size)
    {
        return Mono.<SearchResponse>create(sink ->
        {
            SearchRequest request = new SearchRequest("post");

            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(new MatchAllQueryBuilder());
            builder.size(size);
            builder.timeout(new TimeValue(timeout, TimeUnit.SECONDS));

            request.source(builder);

            client.searchAsync(request, RequestOptions.DEFAULT, listenerToSink(sink));

        }).map(PostService::getPostsAsList);
    }

    public Mono<List<Post>> searchTagAllPosts(String hashTag, int from, int size)
    {
        return Mono.<SearchResponse>create(sink ->
        {
            SearchRequest request = new SearchRequest("post");

            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(new TermQueryBuilder("content", hashTag));
            builder.from(from);
            builder.size(size);
            builder.timeout(new TimeValue(timeout, TimeUnit.SECONDS));

            request.source(builder);

            client.searchAsync(request, RequestOptions.DEFAULT, listenerToSink(sink));

        }).map(PostService::getPostsAsList);
    }

    public Mono<List<Post>> searchInContentAllPosts(String searchContent, int from, int size)
    {
        return Mono.<SearchResponse>create(sink ->
        {
            SearchRequest request = new SearchRequest("post");

            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(new MatchPhraseQueryBuilder("content", searchContent));
            builder.from(from);
            builder.size(size);
            builder.timeout(new TimeValue(timeout, TimeUnit.SECONDS));
            builder.sort(new FieldSortBuilder("date").order(SortOrder.DESC));

            request.source(builder);

            client.searchAsync(request, RequestOptions.DEFAULT, listenerToSink(sink));

        }).map(PostService::getPostsAsList);
    }

    /**
     * @param userNameList User Names (Maybe Following of an User)
     * @return Reactive List of Post, Sorted by Date
     */
    public Mono<List<Post>> getByUserNamesSorted(String[] userNameList, int from, int size)
    {
        return Mono.<SearchResponse>create(sink ->
        {
            SearchRequest request = new SearchRequest("post");

            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(new TermsQueryBuilder("username", userNameList));
            builder.from(from);
            builder.size(size);
            builder.timeout(new TimeValue(timeout, TimeUnit.SECONDS));
            builder.sort(new FieldSortBuilder("date").order(SortOrder.DESC));

            request.source(builder);

            client.searchAsync(request, RequestOptions.DEFAULT, listenerToSink(sink));

        }).map(PostService::getPostsAsList);
    }

    public Mono<List<Post>> getByUserName(String userName, int from, int size)
    {
        SearchRequest request = new SearchRequest("post");

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(new TermQueryBuilder("username", userName));
        builder.from(from);
        builder.size(size);
        builder.timeout(new TimeValue(timeout, TimeUnit.SECONDS));

        request.source(builder);

        return Mono.<SearchResponse>create(sink ->
                client.searchAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
                .map(PostService::getPostsAsList);
    }

    public Mono<List<Post>> getByIds(List<String> ids)
    {
        MultiGetRequest request = new MultiGetRequest();
        for (String id: ids)
            request.add(new MultiGetRequest.Item("post", id));


        return Mono.<MultiGetResponse>create(sink ->
                client.mgetAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
                .map(PostService::mapMultiGet);
    }

    private static List<Post> mapMultiGet(MultiGetResponse multiGetResponse)
    {
        MultiGetItemResponse[] responses = multiGetResponse.getResponses();

        List<Post> posts = new ArrayList<>();

        for (MultiGetItemResponse response: responses)
        {
            GetResponse getResponse = response.getResponse();

            if (getResponse.isExists())
                posts.add(Post.buildFromSource(getResponse.getId(), getResponse.getSourceAsMap()));
        }

        return posts;
    }

    public Mono<Post> getById(String id)
    {
        GetRequest getRequest = new GetRequest("post", id);

        return Mono.<GetResponse>create(sink ->
                client.getAsync(getRequest, RequestOptions.DEFAULT, listenerToSink(sink)))
                .filter(GetResponse::isExists)
                .map(getResponse -> Post.buildFromSource(getResponse.getId(), getResponse.getSourceAsMap()))
                .switchIfEmpty(Mono.empty());
    }

    /**
     * @return id of inserted post
     */
    public Mono<String> save(Post post)
    {
        IndexRequest indexRequest = new IndexRequest("post");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("username", post.getUsername());
        jsonMap.put("is_private", post.getIsPrivate());
        jsonMap.put("date", post.getDate().toString());
        jsonMap.put("content", post.getContent());
        jsonMap.put("tags", post.getTags());

        indexRequest.source(jsonMap);

        return Mono.<IndexResponse>create(sink ->
                client.indexAsync(indexRequest, RequestOptions.DEFAULT, listenerToSink(sink)))
                .map(IndexResponse::getId);
    }

    private static Boolean isPostDeleted(DeleteResponse response)
    {
        return response.getResult() == DocWriteResponse.Result.DELETED;
    }

    /**
     * @param id of post
     * @return true if no exception throw and post found
     */
    public Mono<Boolean> deleteById(String id)
    {
        DeleteRequest deleteRequest = new DeleteRequest("post", id);

        return Mono.<DeleteResponse>create(sink ->
                client.deleteAsync(deleteRequest, RequestOptions.DEFAULT, listenerToSink(sink)))
                .map(PostService::isPostDeleted);
    }

    private static String bulkResponseToString(BulkByScrollResponse bulkResponse)
    {
        return "Deleted in time: " + bulkResponse.getTook() +
                " isTimeout? " + bulkResponse.isTimedOut() +
                " Total: " + bulkResponse.getTotal() +
                " deleted: " + bulkResponse.getDeleted();
    }

    public Mono<String> deleteByUserName(final String userName)
    {
        DeleteByQueryRequest request = new DeleteByQueryRequest("post");
        request.setQuery(new TermQueryBuilder("username", userName));


        return Mono.<BulkByScrollResponse>create(sink ->
                client.deleteByQueryAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
                .map(PostService::bulkResponseToString);
    }

    private static List<String> getAutoCompletes(SearchResponse searchResponse)
    {
        List<String> result = new ArrayList<>();
        Suggest suggest = searchResponse.getSuggest();
        CompletionSuggestion completionSuggestion = suggest.getSuggestion("suggest_tags");

        for (CompletionSuggestion.Entry entry: completionSuggestion.getEntries())
            for (CompletionSuggestion.Entry.Option option: entry)
            {
                result.add(option.getText().string());
            }

        return result;
    }

    //Convert Post Search Response to List
    private static List<Post> getPostsAsList(SearchResponse searchResponse)
    {
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        List<Post> result = new ArrayList<>();

        for (SearchHit hit: searchHits)
        {
            result.add(Post.buildFromSource(hit.getId(), hit.getSourceAsMap()));
        }

        return result;
    }

    @PreDestroy
    private void destroy() throws IOException
    {
        System.out.println("Closing Elastic Client");
        client.close();
    }

}
