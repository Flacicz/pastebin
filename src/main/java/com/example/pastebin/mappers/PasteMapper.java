package com.example.pastebin.mappers;

import com.example.pastebin.dto.PasteDTO;
import com.example.pastebin.dto.PasteDetailsDTO;
import com.example.pastebin.entity.Paste;
import com.example.pastebin.entity.PasteDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", config = IgnoreUnmappedMapperConfig.class)
public interface PasteMapper {
    Paste fromDTOToPaste(PasteDTO pasteDTO);

    PasteDTO fromPasteToDTO(Paste paste);

    List<Paste> fromPasteDTOToPasteList(List<PasteDTO> pasteDTOs);

    List<PasteDTO> fromPasteToPasteDTOList(List<Paste> pastes);
    @Mapping(target = "likes",ignore = true)
    PasteDetails fromDetailsDTOToDetails(PasteDetailsDTO pasteDetailsDTO);
    @Mapping(target = "likes",expression = "java((long) pasteDetails.getLikes().size() )")
    PasteDetailsDTO fromDetailsToDetailsDTO(PasteDetails pasteDetails);
}
