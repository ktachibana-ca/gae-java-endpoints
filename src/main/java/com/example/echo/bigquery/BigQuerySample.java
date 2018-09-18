package com.example.echo.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.TableResult;
import java.util.UUID;

public class BigQuerySample {
    public static void main(String...args) throws Exception {

        // BigQuery の定義を行う。デフォルト設定されているインスタンスとサービスを取得する。
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

        // 使用する設定値を定義する。
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(
                "SELECT "
                        + "CONCAT('https://stackoverflow.com/questions/', CAST(id as STRING)) as url, "
                        + "view_count "
                        + "FROM `bigquery-public-data.stackoverflow.posts_questions` "
                        + "WHERE tags like '%google-bigquery%' "
                        + "ORDER BY favorite_count DESC LIMIT 10"
        ).setUseLegacySql(false).build();

        // jobIDを明示的に定義し、リトライ管理などをする。
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        queryJob = queryJob.waitFor();

        if (queryJob == null) {
            throw new RuntimeException("job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        QueryResponse response = bigquery.getQueryResults(jobId);

        TableResult result = queryJob.getQueryResults();

        for (FieldValueList row : result.iterateAll()) {
            String url = row.get("url").getStringValue();
            long viewCount = row.get("view_count").getLongValue();
            System.out.printf("url: %s, views: %d", url, viewCount);
        }
    }
}