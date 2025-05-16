package org.example.mfapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.json.JsonSchemaList;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModelDTO {

    @Nullable
    private String explanation;
    private int tokens_used;
    private JsonSchemaList models;

}
