package org.bef.core.groovy;

import org.bef.core.Job;
import org.bef.core.JobParameterDef;
import org.bef.core.Project;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParserTest {

    @Test
    public void testParse() throws Exception {
        JobParser parser = new JobParser();

        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<?> job = project.getJob("simple");

        assertThat(job, is(notNullValue()));
        assertThat(job.getParameterDefs().size(), is(2));
        assertThat(job.getParameterDefs().get(0).getKey(), is("name"));
        assertThat(job.getParameterDefs().get(1).getKey(), is("surname"));
        final JobParameterDef<String> actual = (JobParameterDef<String>) job.getParameterDefs().get(1).getDependencies().get(0);
        final JobParameterDef<String> operand = (JobParameterDef<String>) job.getParameterDefs().get(0);
        assertThat(actual, equalTo(operand));
    }

}