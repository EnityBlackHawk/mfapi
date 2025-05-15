package org.example.mfapi.dto.setupgroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LLM {

    private String provider;
    public String model;
    public String apiKey;

}
