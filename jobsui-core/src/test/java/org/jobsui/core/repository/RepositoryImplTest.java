package org.jobsui.core.repository;

import com.github.zafarkhaja.semver.Version;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by enrico on 5/5/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class RepositoryImplTest {

    @Mock
    private BookmarksStore bookmarkStore;

    @Test
    public void testSimple() throws Exception {
        URL url = getClass().getResource("/repository");
        RepositoryImpl sut = new RepositoryImpl(url);

        Optional<Project> project = sut.getProject("test:simple", Version.valueOf("1.0.0"), bookmarkStore);

        assertThat(project.isPresent(), is(true));

        project.ifPresent(p -> {
            assertThat(p.getName(), is("Simple project"));
            Job<?> job = p.getJob("simple");
            assertThat(job.getName(), is("Simple job"));
        });
    }
}