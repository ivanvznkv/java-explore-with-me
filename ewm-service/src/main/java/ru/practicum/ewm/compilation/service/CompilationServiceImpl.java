package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Set<Event> events = (dto.getEvents() == null) ? Set.of() : Set.copyOf(eventRepository.findAllById(dto.getEvents()));
        Compilation compilation = CompilationMapper.toCompilation(dto, events);
        compilation = compilationRepository.save(compilation);
        Set<EventShortDto> eventShortDtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toSet());
        return CompilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            Set<Event> events = Set.copyOf(eventRepository.findAllById(request.getEvents()));
            compilation.setEvents(events);
        }
        compilation = compilationRepository.save(compilation);
        Set<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toSet());
        return CompilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id=" + compId + " не найдена");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        }
        return compilations.stream()
                .map(comp -> {
                    Set<EventShortDto> eventDtos = comp.getEvents().stream()
                            .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                            .collect(Collectors.toSet());
                    return CompilationMapper.toCompilationDto(comp, eventDtos);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
        Set<EventShortDto> eventDtos = compilation.getEvents().stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toSet());
        return CompilationMapper.toCompilationDto(compilation, eventDtos);
    }
}