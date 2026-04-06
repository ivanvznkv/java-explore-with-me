package ru.practicum.ewm.compilation.mapper;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;

import java.util.Set;

public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setPinned(dto.getPinned() != null ? dto.getPinned() : false);
        compilation.setTitle(dto.getTitle());
        return compilation;
    }

    public static Compilation toCompilation(NewCompilationDto dto, Set<Event> events) {
        Compilation compilation = toCompilation(dto);
        compilation.setEvents(events);
        return compilation;
    }

    public static CompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> eventShortDtos) {
        return new CompilationDto(
                compilation.getId(),
                eventShortDtos,
                compilation.getPinned(),
                compilation.getTitle()
        );
    }
}