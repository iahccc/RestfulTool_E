package com.github.restful.tool.service.topic;

import com.github.restful.tool.beans.ApiService;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface RestDetailTopic extends RestTopic<RestDetailTopic> {

    Topic<RestDetailTopic> TOPIC = Topic.create("RestTopic.RestDetailTopic-ClearCache", RestDetailTopic.class);

    /**
     * clear Caches
     *
     * @param apiService request(key)
     */
    void clearCache(@Nullable ApiService apiService);
}
