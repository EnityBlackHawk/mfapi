package org.example.mfapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaCodeDto {

    private Map<String, String> code;
    private int tokens_used;
}
