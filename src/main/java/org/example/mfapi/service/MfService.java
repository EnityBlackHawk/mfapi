package org.example.mfapi.service;

import org.example.mfapi.dto.ModelDTO;
import org.example.mfapi.dto.SetupDTO;
import org.example.mfapi.exception.MfException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.utfpr.mf.descriptor.CachePolicy;
import org.utfpr.mf.descriptor.MfMigratorDesc;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.interfaces.IMfStepObserver;
import org.utfpr.mf.migration.MfMigrationStepFactory;
import org.utfpr.mf.migration.MfMigrator;
import org.utfpr.mf.migration.params.MetadataInfo;
import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.model.Credentials;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.stream.StringPrintStream;

import java.util.List;

@Service
public class MfService {

    private StringPrintStream printStream = new StringPrintStream();
    private MfMigrationStepFactory factory = new MfMigrationStepFactory(printStream);
    private MfMigrator.Binder binder = new MfMigrator.Binder();
    private MfMigrator migrator;

    private IMfStepObserver observer = new IMfStepObserver() {
        @Override
        public boolean OnStepStart(String stepName, Object o) {
            return false;
        }

        @Override
        public boolean OnStepEnd(String stepName, Object o) {
            return false;
        }

        @Override
        public boolean OnStepCrash(String stepName, Throwable error) {
            throw new MfException(error, printStream.get());
        }

        @Override
        public boolean OnStepError(String stepName, String message) {
            return false;
        }
    };

    public MetadataInfo setup(SetupDTO dto) {

        MigrationSpec spec = MigrationSpec.builder()
                .framework(Framework.SPRING_DATA)
                .allow_ref(dto.getPreferences().getAllowRef())
                .name(dto.getProjectName())
                .prioritize_performance(dto.getPreferences().getPreferPerformance())
                .reference_only(false)
                .workload(dto.getWorkloads())
                .build();

        binder.bind(DefaultInjectParams.MIGRATION_SPEC.toString(), spec);
        binder.bind(DefaultInjectParams.LLM_KEY.toString(), dto.getLlm().getApiKey());

        IMfMigrationStep step = factory.createAcquireMetadataStep(observer);

        MfMigratorDesc desc = new MfMigratorDesc();
        desc.binder = binder;
        desc.steps = List.of(step);
        desc.llmServiceDesc.llm_key = dto.getLlm().getApiKey();
        desc.llmServiceDesc.temp = 1;
        desc.llmServiceDesc.model = dto.getLlm().getModel();
        desc.llmServiceDesc.cachePolicy = CachePolicy.DEFAULT;
        desc.llmServiceDesc.cacheDir = "cache";

        migrator = new MfMigrator(desc);

        Credentials credentials = new Credentials(
                dto.getRdbAccess().getHost(),
                dto.getRdbAccess().getUser(),
                dto.getRdbAccess().getPassword()
        );

        return (MetadataInfo) migrator.execute(credentials);

    }

    public ModelDTO generateModel(MetadataInfo metadataInfo) {

        if(migrator == null) {
            throw new IllegalStateException("Migrator is not initialized. Call setup() first.");
        }

        IMfMigrationStep step = factory.createGenerateModelStep(observer);
        migrator.clearSteps();
        migrator.addStep(step);
        var model = (Model) migrator.execute(metadataInfo);
        if(model == null) {
            return null;
        }
        ModelDTO modelDTO = new ModelDTO();
        modelDTO.setModels(model.getModels());
        modelDTO.setExplanation(model.getExplanation());

        return modelDTO;

    }


}
