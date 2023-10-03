package com.example.pastebin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paste implements Serializable {
    @Serial
    private static final long serialVersionUID = 8591616427310310802L;

    private Long id;

    private String title;

    private String text;

    private String author;

    private int hash;
}
