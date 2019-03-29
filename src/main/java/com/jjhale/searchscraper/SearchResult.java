package com.jjhale.searchscraper;

import java.time.Instant;
import java.util.Objects;

public class SearchResult implements Comparable<SearchResult>{
    public final String id;
    public final String taskName;
    public final String title;
    public final int httpStatusCode;
    public final long createdAt;

    public String getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public SearchResult(String id, String taskName, String title, int httpStatusCode, long createdAt) {
        if(id == null || taskName == null || title == null) {
            throw new IllegalArgumentException("No null strings allowed.");
        }
        this.id=id;
        this.taskName=taskName;
        this.title = title;
        this.httpStatusCode=httpStatusCode;
        this.createdAt = createdAt;

    }

    @Override
    public String toString() {
        Instant createdInstant = Instant.ofEpochMilli(createdAt);
        String createdInstantString = createdInstant.toString();
        return id + " : " +
                "title='" + title +
                "', task name='" + taskName +
                "', http status=" + httpStatusCode +
                ", created " + createdInstantString;

    }

    @Override
    public int compareTo(SearchResult o) {
        return this.id.compareTo(o.id);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchResult that = (SearchResult) o;
        return httpStatusCode == that.httpStatusCode &&
                createdAt == that.createdAt &&
                id.equals(that.id) &&
                taskName.equals(that.taskName) &&
                title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskName, title, httpStatusCode, createdAt);
    }
}
