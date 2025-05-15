package org.example.mfapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mfapi.dto.setupgroup.LLM;
import org.example.mfapi.dto.setupgroup.MigrationPreferences;
import org.example.mfapi.dto.setupgroup.RdbAccess;
import org.example.mfapi.dto.setupgroup.WorkloadList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetupDTO {

    private String projectName;
    private LLM llm;
    private MigrationPreferences preferences;
    private RdbAccess rdbAccess;
    private WorkloadList workloads;

}
