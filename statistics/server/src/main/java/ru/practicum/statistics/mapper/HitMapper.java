package ru.practicum.statistics.mapper;

import ru.practicum.statistics.dto.EndpointHit;
import ru.practicum.statistics.model.HitEntity;

public class HitMapper {

    public static HitEntity toEntity(EndpointHit dto) {
        if (dto == null) {
            return null;
        }
        HitEntity entity = new HitEntity();
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(dto.getIp());
        entity.setTimestamp(dto.getTimestamp());
        return entity;
    }
}