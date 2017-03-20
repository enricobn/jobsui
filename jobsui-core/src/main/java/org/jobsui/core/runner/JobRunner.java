package org.jobsui.core.runner;

import com.thoughtworks.xstream.XStream;
import org.jobsui.core.Bookmark;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIButton;
import org.jobsui.core.ui.UIWindow;
import org.jobsui.core.ui.UnsupportedComponentException;
import rx.Observable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by enrico on 4/29/16.
 */
public class JobRunner<C> {
    private final UI<C> ui;
    private boolean valid = false;

    public JobRunner(UI<C> ui) {
        this.ui = ui;
    }

    public <T extends Serializable> T run(final Job<T> job) throws Exception {
        valid = false;

        final UIWindow<C> window = ui.createWindow(job.getName());

        final JobValues values = new JobValuesImpl();

        List<Exception> exceptions = new ArrayList<>();

        AtomicReference<T> result = new AtomicReference<>(null);

        window.show(() -> {

            JobRunnerContext<T,C> context;

            try {
                 context = new JobRunnerContext<>(job, ui, window);
            } catch (Exception e) {
                exceptions.add(e);
                return;
            }

            context.observeDependencies();

            UIButton<C> runButton;
            UIButton<C> saveBookmarkButton;
            try {
                runButton = ui.create(UIButton.class);
                saveBookmarkButton = ui.create(UIButton.class);
            } catch (UnsupportedComponentException e) {
                // TODO
                throw new RuntimeException(e);
            }

            runButton.setEnabled(false);
            runButton.setTitle("Run");

            runButton.getObservable().subscribe(serializableVoid -> {
                try {
                    JobFuture<T> resultFuture = job.run(values);
                    if (resultFuture.getException() != null) {
                        ui.showError("Error running job.", resultFuture.getException());
                    } else {
                        result.set(resultFuture.get());
                    }
                } catch (Exception e) {
                    ui.showError("Error running job.", e);
                }
            });

            saveBookmarkButton.setEnabled(false);
            saveBookmarkButton.setTitle("Bookmark");

            XStream xstream = new XStream();
            if (job.getClassLoader() != null) {
                xstream.setClassLoader(job.getClassLoader());
            }

            saveBookmarkButton.getObservable().subscribe(serializableVoid -> {
                try {
                    Bookmark bookmark = new Bookmark(job, "Test", values);
                    FileWriter fileWriter = new FileWriter("bookmark.xml");
                    xstream.toXML(bookmark, fileWriter);
                } catch (Exception e) {
                    ui.showError("Error saving bookmark.", e);
                }
            });

            Observable<JobValidation> validationObserver = context.validationObserver();

            validationObserver.subscribe(v -> {
                valid = v.isValid();
                runButton.setEnabled(v.isValid());
                saveBookmarkButton.setEnabled(v.isValid());
                window.showValidationMessage(String.join(", ", v.getMessages()));
            });

            Observable<Map<String, Serializable>> valuesChangeObserver = context.valuesChangeObserver();

            valuesChangeObserver.subscribe(map -> {
                values.clear();
                for (JobDependency jobDependency : job.getUnsortedDependencies()) {
                    setValue(values, map, jobDependency);
                }
            });

            context.notifyInitialValue();

            window.add(runButton);
            window.add(saveBookmarkButton);
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
        return result.get();
    }

    private Bookmark loadBookmark(XStream xstream, String fileName) throws FileNotFoundException {
        return (Bookmark) xstream.fromXML(new FileReader(fileName));
    }

    private static void setValue(JobValues values, Map<String, Serializable> map, JobDependency jobDependency) {
        values.setValue(jobDependency, map.get(jobDependency.getKey()));
    }

    public boolean isValid() {
        return valid;
    }


}
