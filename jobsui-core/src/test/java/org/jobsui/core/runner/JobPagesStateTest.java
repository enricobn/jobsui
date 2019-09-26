package org.jobsui.core.runner;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.JobParameter;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UIWindow;
import org.jobsui.core.xml.JobPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by enrico on 5/2/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JobPagesStateTest {
    @Mock
    private Job<?> job;
    @Mock
    private JobUIRunnerContext<? extends Serializable, Object> context;
    @Mock
    private UIWindow<Object> window;

    @Test
    public void assert_that_when_no_steps_then_hasNext_is_false() throws Exception {
        JobPagesState sut = new JobPagesState(job);

        assertThat(sut.hasNext(), is(false));
    }

    @Test
    public void assert_that_when_no_steps_then_hasPrevious_is_false() throws Exception {
        JobPagesState sut = new JobPagesState(job);

        assertThat(sut.hasPrevious(), is(false));
    }

    @Test
    public void assert_that_when_there_are_at_least_two_steps_then_hasNext_is_true() throws Exception {
        JobPagesState sut = createComplexPagesState();

        assertThat(sut.hasNext(), is(true));
    }

    @Test
    public void assert_that_when_there_is_a_step_then_hasPrevious_is_false() throws Exception {
        JobPagesState sut = createPagesStateWithOnePage();

        assertThat(sut.hasPrevious(), is(false));
    }

    @Test
    public void assert_that_when_there_are_at_least_two_steps_then_next_clears_window() throws Exception {
        JobPagesState sut = createComplexPagesState();

        sut.next(context, window);

        verify(window).clear();
    }

    @Test
    public void assert_that_when_there_are_at_least_two_steps_then_next_then_hasPrevious_is_true() throws Exception {
        JobPagesState sut = createComplexPagesState();

        sut.next(context, window);

        assertThat(sut.hasPrevious(), is(true));
    }

    @Test
    public void assert_that_when_there_is_only_a_step_then_hasNext_is_false() throws Exception {
        JobPagesState sut = createPagesStateWithOnePage();

        assertThat(sut.hasNext(), is(false));
    }

    @Test
    public void assert_that_when_there_are_at_least_two_steps_then_next_then_previous_then_hasPrevious_is_false() throws Exception {
        JobPagesState sut = createComplexPagesState();

        sut.next(context, window);
        sut.previous(context, window);

        assertThat(sut.hasPrevious(), is(false));
    }

    @Test
    public void assert_that_when_there_are_at_least_two_steps_then_next_then_previous_then_hasNext_is_true() throws Exception {
        JobPagesState sut = createComplexPagesState();

        sut.next(context, window);
        sut.previous(context, window);

        assertThat(sut.hasNext(), is(true));
    }

    @Test
    public void verify_that_when_there_is_a_step_with_two_dependencies_then_updateWindow_adds_two_widgets_to_window() throws Exception {
        JobPagesState sut = createComplexPagesState();

        sut.updateWindow(context, window);

        verify(window, times(2)).add(any(UIWidget.class));
    }

    @Test
    public void verify_that_when_there_is_a_step_with_two_dependencies_then_next_adds_the_remaining_widgets_to_window() throws Exception {
        JobPagesState sut = createComplexPagesState();

        sut.next(context, window);

        verify(window, times(1)).add(any(UIWidget.class));
    }

    @Test
    public void verify_that_when_there_is_a_step_with_two_dependencies_then_next_then_previous_adds_all_the_widgets_to_window() throws Exception {
        JobPagesState sut = createComplexPagesState();

        sut.next(context, window);
        sut.previous(context, window);

        verify(window, times(3)).add(any(UIWidget.class));
    }

    private JobPagesState createPagesStateWithOnePage() throws Exception {
        List<JobPage> steps = Collections.singletonList(mock(JobPage.class));
        when(job.getJobPages()).thenReturn(steps);

        return new JobPagesState(job);
    }

    /**
     * Creates a PagesState of a job with three parameters: first, second and third,
     * and a page with first and second.
     */
    private JobPagesState createComplexPagesState() throws Exception {
        JobPage jobPage = mock(JobPage.class);

        JobParameter first = addJobParameter("first");
        JobParameter second = addJobParameter("second");
        JobParameter third = addJobParameter("third");

        Set<String> dependencies = new HashSet<>(Arrays.asList(first.getKey(), second.getKey()));
        when(jobPage.getDependencies()).thenReturn(dependencies);

        List<JobPage> steps = Collections.singletonList(jobPage);
        when(job.getJobPages()).thenReturn(steps);

        List<JobDependency> parameters = Arrays.asList(first, second, third);
        when(job.getSortedDependencies()).thenReturn(parameters);

        return new JobPagesState(job);
    }

    private JobParameter addJobParameter(String key) {
        JobParameter jobParameter = mock(JobParameter.class);
        when(jobParameter.getKey()).thenReturn(key);
        when(job.getParameter(key)).thenReturn(jobParameter);
        return jobParameter;
    }
}