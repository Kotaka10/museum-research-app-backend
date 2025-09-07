package com.example.museumsearch.mapper;

import com.example.museumsearch.dto.MuseumDTO;
import com.example.museumsearch.model.Museum;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder)
public interface MuseumMapper {

    @Mapping(target = "createdByUsername", source = "createdBy.email")
    MuseumDTO toDTO(Museum museum);

    @Mapping(target = "createdBy", ignore = true)
    Museum toEntity(MuseumDTO museumDTO);
}
