package ru.practicum.service.impl;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.comment.CommentModerationRequest;
import ru.practicum.dto.comment.CommentRequest;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ObjectUpdateForbiddenException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.QComment;
import ru.practicum.model.state.CommentState;
import ru.practicum.model.state.CommentUpdateState;
import ru.practicum.model.state.event.EventState;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.CommentService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<Comment> getCommentsByUserId(long userId, CommentState status) {
        BooleanBuilder searchConditions = new BooleanBuilder(QComment.comment.author.id.eq(userId));
        Optional.ofNullable(status).ifPresent(commentState -> searchConditions.and(QComment.comment.status.eq(status)));
        ArrayList<Comment> comments = new ArrayList<>();
        commentRepository.findAll(searchConditions).forEach(comments::add);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByEventId(long eventId, long userId, CommentState status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d is not found", eventId)));
        if (event.getInitiator().getId() != userId) {
            throw new AccessDeniedException(
                    String.format("User with id=%d is not an initiator of event with id=%d", userId, eventId));
        }
        if (status == null) {
            return event.getComments();
        }
        return event.getComments().stream()
                .filter(comment -> comment.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    @Override
    public Comment addComment(Comment comment) {
        if (!comment.getEvent().getState().equals(EventState.PUBLISHED)) {
            throw new AccessDeniedException(String.format("Event with id=%d is not published", comment.getEvent().getId()));
        }
        if (Boolean.TRUE.equals(!comment.getEvent().getCommentModeration())
                || comment.getAuthor().getId() == (comment.getEvent().getInitiator().getId())) {
            comment.setStatus(CommentState.PUBLISHED);
        }
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> updateCommentStatus(long userId, long eventId, CommentModerationRequest moderationRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d is not found", userId));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d is not found", eventId)));

        if (event.getInitiator().getId() != userId) {
            throw new AccessDeniedException(
                    String.format("User with id=%d is not an initiator of event with id=%d", userId, eventId));
        }
        if (Boolean.FALSE.equals(event.getCommentModeration())) {
            throw new ValidationException("Moderation for event comments is disabled");
        }
        List<Comment> comments = event.getComments().stream()
                .filter(comment -> moderationRequest.getCommentIds().contains(comment.getId()))
                .collect(Collectors.toList());
        if (comments.stream().anyMatch(comment -> comment.getStatus().equals(CommentState.PUBLISHED)
                || comment.getStatus().equals(CommentState.REJECTED))) {
            throw new ObjectUpdateForbiddenException("Comments must have status PENDING");
        }
        comments.forEach(comment -> comment.setStatus(
                moderationRequest.getStatus().equals(CommentUpdateState.PUBLISHED) ? CommentState.PUBLISHED : CommentState.REJECTED));

        return commentRepository.saveAll(comments);
    }

    @Override
    public Comment updateComment(long userId, long commentId, CommentRequest commentRequest) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d is not found", commentId)));
        if (comment.getAuthor().getId() != userId) {
            throw new AccessDeniedException(String.format("User with id=%d can't update comment with id=%d", userId, commentId));
        }
        if (Boolean.TRUE.equals(comment.getEvent().getCommentModeration())
                && comment.getEvent().getInitiator().getId() != userId) {
            comment.setStatus(CommentState.PENDING);
        }
        comment.setText(commentRequest.getText());
        comment.setEdited(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    public void deleteUserComment(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d is not found", commentId)));
        if (comment.getAuthor().getId() != userId) {
            throw new AccessDeniedException(String.format("User with id=%d can't delete comment with id=%d", userId, commentId));
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteComment(long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException(String.format("Comment with id=%d is not found", commentId));
        }
        commentRepository.deleteById(commentId);
    }
}
