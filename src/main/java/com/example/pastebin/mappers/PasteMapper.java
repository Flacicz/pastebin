package com.example.pastebin.mappers;

import com.example.pastebin.dto.PasteDTO;
import com.example.pastebin.entity.Paste;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", config = IgnoreUnmappedMapperConfig.class)
public interface PasteMapper {
    Paste fromDTOToPaste(PasteDTO pasteDTO);

    @InheritInverseConfiguration
    PasteDTO fromPasteToDTO(Paste paste);

    List<Paste> fromPasteDTOToPasteList(List<PasteDTO> pasteDTOs);

    List<PasteDTO> fromPasteToPasteDTOList(List<Paste> pastes);
}
