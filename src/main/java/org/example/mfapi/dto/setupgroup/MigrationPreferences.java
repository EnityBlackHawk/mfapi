package org.example.mfapi.dto.setupgroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationPreferences {

    private Boolean allowRef;
    private Boolean preferPerformance;
    private String framework;
    private String customPrompt;

}
