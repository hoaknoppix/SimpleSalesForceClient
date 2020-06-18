
package com.noron.client.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "totalSize",
    "done",
    "records"
})
public class QueryResponse {

    @JsonProperty("totalSize")
    private Integer totalSize;
    @JsonProperty("done")
    private Boolean done;
    @JsonProperty("records")
    private List<Map<String, Object>> records = null;

    @JsonProperty("totalSize")
    public Integer getTotalSize() {
        return totalSize;
    }

    @JsonProperty("totalSize")
    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    @JsonProperty("done")
    public Boolean getDone() {
        return done;
    }

    @JsonProperty("done")
    public void setDone(Boolean done) {
        this.done = done;
    }

    @JsonProperty("records")
    public List<Map<String, Object>> getRecords() {
        return records;
    }

    @JsonProperty("records")
    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "QueryResponse{" +
            "totalSize=" + totalSize +
            ", done=" + done +
            ", records=" + records +
            '}';
    }
}
