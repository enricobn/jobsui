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
public class IvyUtils {

    // from https://makandracards.com/evgeny-goldin/5817-calling-ivy-from-groovy-or-java
    public static File resolveArtifact(String groupId, String artifactId, String version) throws IOException, ParseException {
        //creates clear ivy settings
        IvySettings ivySettings = new IvySettings();
        //url resolver for configuration of maven repo
        URLResolver resolver = new URLResolver();
        resolver.setM2compatible(true);
        resolver.setName("central");
        //you can specify the url resolution pattern strategy
        resolver.addArtifactPattern(
                "http://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[artifact](-[revision]).[ext]");
        //adding maven repo resolver
        ivySettings.addResolver(resolver);
        //set to the default resolver
        ivySettings.setDefaultResolver(resolver.getName());

        //creates an Ivy instance with settings
        Ivy ivy = Ivy.newInstance(ivySettings);

        DefaultMessageLogger logger = new DefaultMessageLogger(Message.MSG_ERR);

        ivy.getLoggerEngine().setDefaultLogger(logger);
        Message.setDefaultLogger(logger);

        File ivyfile = File.createTempFile("ivy", ".xml");
        ivyfile.deleteOnExit();

        String[] dep = {groupId, artifactId, version};

        DefaultModuleDescriptor md =
                DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId.newInstance(dep[0],
                        dep[1] + "-caller", "working"));

        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
                ModuleRevisionId.newInstance(dep[0], dep[1], dep[2]), false, false, true);
        md.addDependency(dd);

        //creates an ivy configuration file
        XmlModuleDescriptorWriter.write(md, ivyfile);

        String[] confs = {"default"};
        ResolveOptions resolveOptions = new ResolveOptions().setConfs(confs);

        // TODO it does not work
        ivy.getEventManager().addIvyListener(event -> {
            if (event instanceof StartArtifactDownloadEvent) {
                StartArtifactDownloadEvent artifactEvent = (StartArtifactDownloadEvent) event;
                System.out.println("Downloading " + artifactEvent.getArtifact());
            }
        });

        //init resolve report
        ResolveReport report = ivy.resolve(ivyfile.toURI().toURL(), resolveOptions);

        //so you can get the jar library

        return report.getAllArtifactsReports()[0].getLocalFile();
    }
}
