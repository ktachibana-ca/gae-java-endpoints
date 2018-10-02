package com.example.echo.pubsub;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import java.util.logging.Logger;

public class PubSub {

    private static final Logger logger = Logger.getLogger(PubSub.class.getName());

    /**
     * Create a topic.
     *
     * @throws Exception exception thrown if operation is unsuccessful
     */
    public static void main() throws Exception {
        logger.info("start pub/sub publish sample");

        String projectId = ServiceOptions.getDefaultProjectId();

        String topicId = "my-topic";

        ProjectTopicName topic = ProjectTopicName.of(projectId, topicId);
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            topicAdminClient.createTopic(topic);
        } catch (ApiException e) {
            logger.warning(e.getStatusCode().getCode().toString());
            logger.warning(e.toString());
        }

        logger.info("Topic %s:%s created." + topic.getProject() + topic.getTopic());

        logger.info("end pub/sub publish sample");
    }
}