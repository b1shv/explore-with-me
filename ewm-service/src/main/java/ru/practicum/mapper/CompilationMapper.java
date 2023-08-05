package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public Compilation toCompilation(CompilationRequest compilationRequest, Set<Event> events) {
        return Compilation.builder()
                .title(compilationRequest.getTitle())
                .pinned(compilationRequest.getPinned() != null && compilationRequest.getPinned())
                .events(events)
                .build();
    }

    public CompilationDto toDto(
            Compilation compilation, List<Event> events, Map<Long, Long> views) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventMapper.toShortDto(events, views).stream()
                        .sorted(Comparator.comparing(EventShortDto::getId))
                        .collect(Collectors.toList()))
                .build();
    }

    public List<CompilationDto> toDto(List<Compilation> compilations, Map<Long, Long> views) {
        return compilations.stream()
                .map(compilation -> toDto(compilation, new ArrayList<>(compilation.getEvents()), views))
                .collect(Collectors.toList());
    }
}
