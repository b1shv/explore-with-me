package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.compilation.CompilationRequest;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationService {
    List<Compilation> getAllCompilations(Boolean pinned, Pageable pageable);

    Compilation getCompilationById(long compilationId);

    Compilation addCompilation(Compilation compilation);

    Compilation updateCompilation(long compilationId, CompilationRequest compilationRequest);

    void deleteCompilation(long compilationId);
}
