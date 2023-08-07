package ru.practicum.dto.comment;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommentModerationResult {
    private final List<CommentDto> publishedComments;
    private final List<CommentDto> rejectedComments;

    public CommentModerationResult() {
        this.publishedComments = new ArrayList<>();
        this.rejectedComments = new ArrayList<>();
    }

    public void addPublishedComment(CommentDto comment) {
        publishedComments.add(comment);
    }

    public void addRejectedComment(CommentDto comment) {
        rejectedComments.add(comment);
    }
}
