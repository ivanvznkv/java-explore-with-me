package ru.practicum.statistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statistics.dto.EndpointHit;
import ru.practicum.statistics.dto.ViewStats;
import ru.practicum.statistics.model.HitEntity;
import ru.practicum.statistics.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final HitRepository hitRepository;

    @Transactional
    public void saveHit(EndpointHit hit) {
        HitEntity entity = new HitEntity();
        entity.setApp(hit.getApp());
        entity.setUri(hit.getUri());
        entity.setIp(hit.getIp());
        entity.setTimestamp(hit.getTimestamp());
        hitRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<Object[]> results;
        if (unique) {
            results = hitRepository.countUniqueHits(start, end, uris);
        } else {
            results = hitRepository.countAllHits(start, end, uris);
        }
        return results.stream()
                .map(row -> new ViewStats((String) row[0], (String) row[1], (Long) row[2]))
                .collect(Collectors.toList());
    }
}