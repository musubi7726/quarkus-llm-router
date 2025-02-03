package com.redhat.composer.initialize;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.Startup;

import java.io.IOException;

import org.jboss.logging.Logger;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.snapshot.CreateRepositoryRequest;
import co.elastic.clients.elasticsearch.snapshot.SharedFileSystemRepository;
import co.elastic.clients.elasticsearch.snapshot.SharedFileSystemRepositorySettings;
import co.elastic.clients.util.ApiTypeHelper;

@ApplicationScoped
@IfBuildProfile("local")
@IfBuildProperty(name="quarkus.elasticsearch.devservices.image-name",
                    stringValue="quay.io/hvan/elasticsearch:snapshot-data")
public class ElasticSearchSnapshotLoader {
    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    @Inject
    ElasticsearchClient esClient;

    private static String REPO_NAME = "rh_doc_repo";
    private static String SNAPSHOT_NAME = "20230623_demo_snapshot";
    private static String SNAPSHOT_LOCATION = "/usr/share/elasticsearch/data/snapshots";

    @Startup
    void init() {               
        LOGGER.info("Loading snapshot data for ElasticSearch...");
        try {
            createRepository();
            restoreSnapshot();
        } catch (Throwable e) {
            LOGGER.error("Error loading snapshot data for ElasticSearch...", e);
        }
    }

    private void createRepository() throws ElasticsearchException, IOException{
        LOGGER.info("Creating new repository for snapshot...");
        SharedFileSystemRepositorySettings settings = SharedFileSystemRepositorySettings.of(r -> 
                                                        r.location(SNAPSHOT_LOCATION)
                                                        .compress(true));

        SharedFileSystemRepository fileSystemRepo = SharedFileSystemRepository.of(r -> 
                                                        r.settings(settings));

        CreateRepositoryRequest request = CreateRepositoryRequest.of(r -> 
                                            r.name(REPO_NAME)
                                            .repository(s -> s.fs(fileSystemRepo)));

        esClient.snapshot().createRepository(request);
    }

    private void restoreSnapshot() throws Throwable, IOException{
        LOGGER.info(String.format("Restoring snapshot - Repo: %s, Snapshot: %s", REPO_NAME, SNAPSHOT_NAME));

        // https://github.com/elastic/elasticsearch-java/issues/891.  Resolved in ES 8.15.x
        ApiTypeHelper.DANGEROUS_disableRequiredPropertiesCheck(true);
        esClient.snapshot().restore(r -> r
                                    .repository(REPO_NAME)
                                    .snapshot(SNAPSHOT_NAME));
        ApiTypeHelper.DANGEROUS_disableRequiredPropertiesCheck(false);
    }

}
