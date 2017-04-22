package org.jobsui.core.bookmark;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobParameterDef;
import org.jobsui.core.job.Project;
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
        JobParameterDef calculated = mock(JobParameterDef.class);
        when(calculated.isCalculated()).thenReturn(true);

        JobParameterDef notCalculated = mock(JobParameterDef.class);

        List<JobParameterDef> parameterDefs = Arrays.asList(calculated, notCalculated);
        when(job.getParameterDefs()).thenReturn(parameterDefs);

        new Bookmark(project, job, "Bookmark", values);

        verify(values, never()).getValue(calculated);
        verify(values, times(1)).getValue(notCalculated);
    }
}