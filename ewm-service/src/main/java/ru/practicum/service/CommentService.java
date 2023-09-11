package ru.practicum.service;

import ru.practicum.dto.comment.CommentModerationRequest;
import ru.practicum.dto.comment.CommentRequest;
import ru.practicum.model.Comment;
import ru.practicum.model.state.CommentState;

import java.util.List;

public interface CommentService {
    Comment addComment(Comment comment);

    List<Comment> updateCommentStatus(long userId, long eventId, CommentModerationRequest moderationRequest);

    List<Comment> getCommentsByEventId(long eventId, long userId, CommentState status);

    List<Comment> getCommentsByUserId(long userId, CommentState status);

    Comment updateComment(long userId, long commentId, CommentRequest commentRequest);

    void deleteUserComment(long userId, long commentId);

    void deleteComment(long commentId);
}
