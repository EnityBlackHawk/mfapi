package org.example.mfapi.service;

import org.example.mfapi.dto.JavaCodeDto;
import org.example.mfapi.dto.ModelDTO;
import org.example.mfapi.dto.SetupDTO;
import org.example.mfapi.exception.MfException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.utfpr.mf.descriptor.CachePolicy;
import org.utfpr.mf.descriptor.MfMigratorDesc;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.interfaces.IMfStepObserver;
import org.utfpr.mf.markdown.MarkdownContent;
import org.utfpr.mf.migration.MfMigrationStepFactory;
import org.utfpr.mf.migration.MfMigrator;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.MetadataInfo;
import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.model.Credentials;
import org.utfpr.mf.mongoConnection.MongoConnection;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.stream.CombinedPrintStream;
import org.utfpr.mf.stream.MfPrintStream;
import org.utfpr.mf.stream.StringPrintStream;
import org.utfpr.mf.stream.SystemPrintStream;
import org.utfpr.mf.tools.UpdateType;
import org.yaml.snakeyaml.error.Mark;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MfService {

    private final UtilsService utilsService;
    private MfPrintStream<String> printStream = new CombinedPrintStream(new StringPrintStream(), new SystemPrintStream());
    private MfMigrationStepFactory factory = new MfMigrationStepFactory(printStream);
    private MfMigrator.Binder binder = new MfMigrator.Binder();
    private GeneratedJavaCode generatedJavaCode = null;
    private MfMigrator migrator;
    private ReentrantLock lock = new ReentrantLock();

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

    public MfService(UtilsService utilsService) {
        this.utilsService = utilsService;
    }

    public MetadataInfo setup(SetupDTO dto) {

        MigrationSpec spec = MigrationSpec.builder()
                .framework(Framework.SPRING_DATA)
                .allow_ref(dto.getPreferences().getAllowRef())
                .name(dto.getProjectName())
                .prioritize_performance(dto.getPreferences().getPreferPerformance())
                .reference_only(false)
                .workload(dto.getWorkloads())
                .build();

        binder.bind(DefaultInjectParams.MIGRATION_SPEC, spec);
        binder.bind(DefaultInjectParams.LLM_KEY, dto.getLlm().getApiKey());

        IMfMigrationStep step = factory.createAcquireMetadataStep(observer);

        MfMigratorDesc desc = new MfMigratorDesc();
        desc.binder = binder;
        desc.steps = List.of(step);
        desc.llmServiceDesc.llm_key = dto.getLlm().getApiKey();
        desc.llmServiceDesc.temp = 1;
        desc.llmServiceDesc.model = dto.getLlm().getModel();
        desc.llmServiceDesc.cachePolicy = CachePolicy.CACHE_ONLY;
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

    public GeneratedJavaCode generateCode(ModelDTO modelDTO) {

        if(migrator == null) {
            throw new IllegalStateException("Migrator is not initialized. Call setup() first.");
        }

        Model model = new Model();
        model.setModels(modelDTO.getModels());

        IMfMigrationStep step = factory.createGenerateJavaCodeStep(observer);
        migrator.clearSteps();
        migrator.addStep(step);
        return (GeneratedJavaCode) migrator.execute(model);
    }

    public void setupMigration(JavaCodeDto generatedJavaCode, MongoConnectionCredentials credentials) {
        if(migrator == null) {
            throw new IllegalStateException("Migrator is not initialized. Call setup() first.");
        }

        var conn = utilsService.createMongoConnection(credentials);
        conn.clearAll();
        binder.bind(DefaultInjectParams.MONGO_CONNECTION, conn);

        this.generatedJavaCode = GeneratedJavaCode.builder().code(generatedJavaCode.getCode()).tokens_used(0).build();

    }

    public void migrateDatabase(SseEmitter sse) {

        if(migrator == null) {
            throw new IllegalStateException("Migrator is not initialized. Call setup() first.");
        }

        if(generatedJavaCode == null) {
            throw new IllegalStateException("Generated Java Code is not initialized. Call setupMigration() first.");
        }

        IMfStepObserver observerMigration = new IMfStepObserver() {
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
                return false;
            }

            @Override
            public boolean OnStepError(String stepName, String message) {
                return false;
            }

            @Override
            public boolean OnUpdate(String stepName, Object message, Class messageType) {
                try {
                    sse.send(message);
                } catch (IOException e) {
                    lock.unlock();
                    throw new RuntimeException(e);
                }
                return true;
            }
        };

        new Thread(() -> {

            if(!lock.tryLock()) {
                sse.complete();
                return;
            }

            IMfMigrationStep step = factory.createMigrateDatabaseStep(observer, observerMigration);
            IMfMigrationStep step2 = factory.createGenerateReportStep(observer);
            migrator.clearSteps();
            migrator.addStep(step);
            migrator.addStep(step2);


            migrator.executeAsync(generatedJavaCode)
                    .catching(
                            e -> {

                                try {
                                    sse.send(new UpdateType("error", "Error: " + e.getMessage()));
                                    sse.complete();
                                } catch (IOException ioException) {
                                    throw new RuntimeException(ioException);
                                }

                                return null;
                            }
                    )
                    .then(
                            result -> {
                                try {
                                    sse.send( new UpdateType( "report", ((MarkdownContent)result).get()));
                                    sse.complete();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return null;
                            }
                    ).await();
            lock.unlock();
        }).start();
    }
}
