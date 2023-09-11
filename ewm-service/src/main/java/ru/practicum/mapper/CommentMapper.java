package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentModerationResult;
import ru.practicum.dto.comment.CommentRequest;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.model.state.CommentState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {
    public Comment toComment(CommentRequest commentRequest, User author, Event event) {
        return Comment.builder()
                .author(author)
                .event(event)
                .text(commentRequest.getText())
                .created(LocalDateTime.now())
                .status(CommentState.PENDING)
                .build();
    }

    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .edited(comment.getEdited())
                .status(comment.getStatus())
                .text(comment.getText())
                .build();
    }

    public List<CommentDto> toDto(List<Comment> comments) {
        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CommentModerationResult toModerationResult(List<Comment> comments) {
        CommentModerationResult moderationResult = new CommentModerationResult();
        comments.stream()
                .map(this::toDto)
                .forEach(comment -> {
                    if (comment.getStatus().equals(CommentState.PUBLISHED)) {
                        moderationResult.addPublishedComment(comment);
                    } else {
                        moderationResult.addRejectedComment(comment);
                    }
                });
        return moderationResult;
    }
}
