package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Compilation;
import ru.practicum.model.QCompilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<Compilation> getAllCompilations(Boolean pinned, Pageable pageable) {
        if (pinned == null) {
            return compilationRepository.findAll(pageable).getContent();
        }
        return compilationRepository.findAll(QCompilation.compilation.pinned.eq(pinned), pageable).getContent();
    }

    @Override
    public Compilation getCompilationById(long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compilationId)));
    }

    public Compilation addCompilation(Compilation compilation) {
        return compilationRepository.save(compilation);
    }

    @Override
    public Compilation updateCompilation(long compilationId, CompilationRequest compilationRequest) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compilationId)));
        if (compilationRequest.getTitle() != null) {
            compilation.setTitle(compilationRequest.getTitle());
        }
        if (compilationRequest.getPinned() != null) {
            compilation.setPinned(compilationRequest.getPinned());
        }
        if (compilationRequest.getEvents() != null && !compilationRequest.getEvents().isEmpty()) {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(compilationRequest.getEvents())));
        }
        return compilationRepository.save(compilation);
    }

    @Override
    public void deleteCompilation(long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compilationId));
        }
        compilationRepository.deleteById(compilationId);
    }
}
