package com.example.museumsearch.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.museumsearch.dto.CommentDTO;
import com.example.museumsearch.model.Comment;
import com.example.museumsearch.model.Museum;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "museumId", target = "museum", qualifiedByName = "mapMuseum")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    default Comment toEntity(CommentDTO commentDTO) {
        return new Comment(
            mapMuseum(commentDTO.getMuseumId()),
            commentDTO.getContent(),
            commentDTO.getUsername()
        );
    }

    @Mapping(source = "museum.id", target = "museumId")
    default CommentDTO toDTO(Comment comment) {
        if (comment == null) return null;

        return CommentDTO.builder()
            .id(comment.getId())
            .museumId(comment.getMuseum().getId())
            .museumName(comment.getMuseum().getName())
            .content(comment.getContent())
            .username(comment.getUsername())
            .displayName(comment.getDisplayName())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    };

    @Named("mapMuseum")
    default Museum mapMuseum(Long museumId) {
        if (museumId == null) return null;
        return Museum.builder().id(museumId).build();
    }
}
