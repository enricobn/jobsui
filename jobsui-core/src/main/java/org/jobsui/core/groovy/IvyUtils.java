package org.jobsui.core.groovy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.download.StartArtifactDownloadEvent;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.resolver.URLResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Created by enrico on 10/4/16.
 */
class IvyUtils {
    //creates clear ivy settings
    private static final IvySettings IVY_SETTINGS = getIvySettings();
    //creates an Ivy instance with settings
    private static final Ivy IVY = Ivy.newInstance(IVY_SETTINGS);
    private static final DefaultMessageLogger logger = new DefaultMessageLogger(Message.MSG_ERR);
    private static final String[] CONFS = new String[] {"default"};
    private static final ResolveOptions RESOLVE_OPTIONS = new ResolveOptions().setConfs(CONFS);

    static {
        IVY.getLoggerEngine().setDefaultLogger(logger);
        Message.setDefaultLogger(logger);
    }

    // from https://makandracards.com/evgeny-goldin/5817-calling-ivy-from-groovy-or-java
    public static File resolveArtifact(String groupId, String artifactId, String version) throws IOException, ParseException {
        File ivyFile = getIvyFile(groupId, artifactId, version);

        // TODO it does not work
        IVY.getEventManager().addIvyListener(event -> {
            if (event instanceof StartArtifactDownloadEvent) {
                StartArtifactDownloadEvent artifactEvent = (StartArtifactDownloadEvent) event;
                System.out.println("Downloading " + artifactEvent.getArtifact());
            }
        });

        //init resolve report
        ResolveReport report = IVY.resolve(ivyFile.toURI().toURL(), RESOLVE_OPTIONS);

        //so you can get the jar library
        return report.getAllArtifactsReports()[0].getLocalFile();
    }

    private static File getIvyFile(String groupId, String artifactId, String version) throws IOException {
        File ivyFile = File.createTempFile("ivy", ".xml");
        ivyFile.deleteOnExit();

        String[] dep = {groupId, artifactId, version};

        ModuleRevisionId moduleRevisionId = ModuleRevisionId.newInstance(dep[0], dep[1] + "-caller", "working");
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(moduleRevisionId);

        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
                ModuleRevisionId.newInstance(dep[0], dep[1], dep[2]), false, false, false);
        md.addDependency(dd);

        //creates an ivy configuration file
        XmlModuleDescriptorWriter.write(md, ivyFile);
        return ivyFile;
    }

    private static IvySettings getIvySettings() {
        IvySettings ivySettings = new IvySettings();

        //url resolver for configuration of maven repo
        URLResolver mavenCentralResolver = getMavenCentralResolver();

        //adding maven repo resolver
        ivySettings.addResolver(mavenCentralResolver);

        //set to the default resolver
        ivySettings.setDefaultResolver(mavenCentralResolver.getName());
        return ivySettings;
    }

    private static URLResolver getMavenCentralResolver() {
        URLResolver mavenCentralResolver = new URLResolver();
        mavenCentralResolver.setM2compatible(true);
        mavenCentralResolver.setName("central");
        mavenCentralResolver.setCheckconsistency(false);
        //you can specify the url resolution pattern strategy
        mavenCentralResolver.addArtifactPattern(
                "http://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[artifact](-[revision]).[ext]");
        return mavenCentralResolver;
    }

}
