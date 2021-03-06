package de.jonasrottmann.realmbrowser.helper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertTrue;

/**
 * Rule that creates the {@link RealmConfiguration } in a temporary directory and deletes the Realm created with that
 * configuration once the test finishes. Be sure to close all Realm instances before finishing the test. Otherwise
 * {@link Realm#deleteRealm(RealmConfiguration)} will throw an exception in the {@link #after()} method.
 * The temp directory will be deleted regardless if the {@link Realm#deleteRealm(RealmConfiguration)} fails or not.
 * <p/>
 * Source: <a href="https://github.com/realm/realm-java">github.com/realm/realm-java</a>
 */
public class TestRealmConfigurationFactory extends TemporaryFolder {
    private Map<RealmConfiguration, Boolean> map = new ConcurrentHashMap<>();
    private Set<RealmConfiguration> configurations = Collections.newSetFromMap(map);
    private boolean unitTestFailed = false;

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } catch (Throwable throwable) {
                    unitTestFailed = true;
                    throw throwable;
                } finally {
                    after();
                }
            }
        };
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    @Override
    protected void after() {
        try {
            for (RealmConfiguration configuration : configurations) {
                Realm.deleteRealm(configuration);
            }
        } catch (IllegalStateException e) {
            // Only throw the exception caused by deleting the opened Realm if the test case itself doesn't throw.
            if (!unitTestFailed) {
                throw e;
            }
        } finally {
            // This will delete the temp folder.
            super.after();
        }
    }

    public RealmConfiguration createConfiguration() {
        RealmConfiguration configuration = new RealmConfiguration.Builder().directory(getRoot()).build();

        configurations.add(configuration);
        return configuration;
    }

    public RealmConfiguration createConfiguration(String subDir, String name) {
        final File folder = new File(getRoot(), subDir);
        assertTrue(folder.mkdirs());
        RealmConfiguration configuration = new RealmConfiguration.Builder().directory(folder).name(name).build();

        configurations.add(configuration);
        return configuration;
    }

    public RealmConfiguration createConfiguration(String name) {
        RealmConfiguration configuration = new RealmConfiguration.Builder().directory(getRoot()).name(name).build();

        configurations.add(configuration);
        return configuration;
    }

    public RealmConfiguration createConfiguration(String name, byte[] key) {
        RealmConfiguration configuration = new RealmConfiguration.Builder().directory(getRoot()).name(name).encryptionKey(key).build();

        configurations.add(configuration);
        return configuration;
    }

    public RealmConfiguration.Builder createConfigurationBuilder() {
        return new RealmConfiguration.Builder().directory(getRoot());
    }

    // Copies a Realm file from assets to temp dir
    public void copyRealmFromAssets(Context context, String realmPath, String newName) throws IOException {
        RealmConfiguration config = new RealmConfiguration.Builder().directory(getRoot()).name(newName).build();

        copyRealmFromAssets(context, realmPath, config);
    }

    public void copyRealmFromAssets(Context context, String realmPath, RealmConfiguration config) throws IOException {
        // Deletes the existing file before copy
        Realm.deleteRealm(config);

        File outFile = new File(config.getRealmDirectory(), config.getRealmFileName());

        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = context.getAssets().open(realmPath);
            os = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buf)) > -1) {
                os.write(buf, 0, bytesRead);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}