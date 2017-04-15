package org.jobsui.core.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 3/22/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JobDependencyTest {

    @Test
    public void assert_that_when_there_are_no_dependencies_then_there_keys_is_empty() throws Exception {
        Collection<? extends JobDependency> jobDependencies = Collections.emptyList();
        List<String> keys = JobDependency.getSortedDependenciesKeys(jobDependencies);
        assertThat(keys.isEmpty(), is(true));
    }

    @Test
    public void assert_that_dependencies_keys_are_sorted() throws Exception {
        JobDependency name = mockDependency("name", Collections.emptyList());
        JobDependency surname = mockDependency("surname", Collections.singletonList("name"));


        Collection<JobDependency> jobDependencies = Arrays.asList(surname, name);
        List<String> keys = JobDependency.getSortedDependenciesKeys(jobDependencies);
        assertThat(keys, is(Arrays.asList("name", "surname")));
    }

    @Test
    public void assert_that_dependencies_are_sorted() throws Exception {
        JobDependency name = mockDependency("name", Collections.emptyList());
        JobDependency surname = mockDependency("surname", Collections.singletonList("name"));

        List<JobDependency> jobDependencies = Arrays.asList(surname, name);
        List<JobDependency> sorted = JobDependency.sort(jobDependencies);
        assertThat(sorted, is(Arrays.asList(name, surname)));
    }

    @Test
    public void assert_that_sorting_dependencies_will_not_change_dependencies() throws Exception {
        JobDependency name = mockDependency("name", Collections.emptyList());
        JobDependency surname = mockDependency("surname", Collections.singletonList("name"));

        List<JobDependency> jobDependencies = Arrays.asList(surname, name);
        List<JobDependency> sorted = JobDependency.sort(jobDependencies);
        assertThat(sorted, not(sameInstance(jobDependencies)));
    }

    @Test(expected = Exception.class)
    public void assert_that_when_dependencies_have_loops_then_sorting_will_throw_an_exception() throws Exception {
        JobDependency name = mockDependency("name", Collections.singletonList("surname"));
        JobDependency surname = mockDependency("surname", Collections.singletonList("name"));

        List<JobDependency> jobDependencies = Arrays.asList(surname, name);
        JobDependency.sort(jobDependencies);
    }

    @Test(expected = Exception.class)
    public void assert_that_when_dependencies_have_loops_then_sorting_keys_will_throw_an_exception() throws Exception {
        JobDependency name = mockDependency("name", Collections.singletonList("surname"));
        JobDependency surname = mockDependency("surname", Collections.singletonList("name"));

        List<JobDependency> jobDependencies = Arrays.asList(surname, name);
        JobDependency.getSortedDependenciesKeys(jobDependencies);
    }

    private static JobDependency mockDependency(String key, List<String> dependencies) {
        JobDependency jobDependency = mock(JobDependency.class);
        when(jobDependency.getKey()).thenReturn(key);
        when(jobDependency.getDependencies()).thenReturn(dependencies);
        return jobDependency;
    }

}