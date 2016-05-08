package org.bef.core.groovy;

import org.bef.core.Project;
import org.bef.core.Job;
import org.bef.core.JobFuture;
import org.bef.core.JobRunner;
import org.bef.core.ui.FakeUiValue;
import org.bef.core.ui.UI;
import org.bef.core.ui.UIValue;
import org.bef.core.ui.UIWindow;
import org.bef.core.ui.swing.SwingUI;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParserTest {

    @Test
    public void testParse() throws Exception {
        UI ui = mock(UI.class);
        UIWindow window = mock(UIWindow.class);
        when(window.show()).thenReturn(true);
        when(ui.createWindow(anyString())).thenReturn(window);

        FakeUiValue<String,?> uiValueName = new FakeUiValue<>();

        FakeUiValue<String,?> uiValueSurname = new FakeUiValue<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobParser parser = new JobParser();

        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<?> job = project.getJob("simple");

        JobRunner runner = new JobRunner();

        final JobFuture<?> future = runner.run(ui, job);

        uiValueName.setValue("Enrico");
        uiValueSurname.setValue("Benedetti");

        assertEquals("(Enrico,Benedetti)", future.get());
    }

}