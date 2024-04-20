package com.example.pastebin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasteDTO {
    private Long id;
    private String title;
    private String text;
    private String author;
    private int hash;
    private int views;
}
