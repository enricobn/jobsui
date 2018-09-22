package org.jobsui.core.bookmark;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobParameter;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.ProjectId;
import org.jobsui.core.runner.JobValues;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by enrico on 4/22/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class BookmarkTest {

    @Mock
    private Project project;
    @Mock
    private Job<?> job;
    @Mock
    private JobValues values;

    @Test
    public void verify_that_only_not_calculated_values_are_stored() throws Exception {
        JobParameter calculated = mock(JobParameter.class);
        when(calculated.isCalculated()).thenReturn(true);

        JobParameter notCalculated = mock(JobParameter.class);

        List<JobParameter> parameterDefs = Arrays.asList(calculated, notCalculated);
        when(job.getParameterDefs()).thenReturn(parameterDefs);

        when(project.getId()).thenReturn(ProjectId.of("test:bookmark", "1.0.0"));

        new Bookmark(project, job, "1", "Bookmark", values, null);

        verify(values, never()).getValue(calculated);
        verify(values, times(1)).getValue(notCalculated);
    }
}