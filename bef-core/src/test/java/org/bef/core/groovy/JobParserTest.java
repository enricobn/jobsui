package org.bef.core.groovy;

import org.bef.core.Job;
import org.bef.core.JobFuture;
import org.bef.core.JobRunner;
import org.bef.core.ui.UI;
import org.bef.core.ui.swing.SwingUI;
import org.junit.Test;

import java.io.InputStream;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParserTest {

    @Test
    public void testParse() throws Exception {
        JobParser parser = new JobParser();
        try (InputStream is = getClass().getResourceAsStream("/simplejob/Simple.befjob")) {
            final Job<Object> job = parser.parse(is);
            JobRunner runner = new JobRunner();

            UI ui = new SwingUI();
            final JobFuture<?> future = runner.run(ui, job);
            System.out.println(future.get());
        }
    }
}