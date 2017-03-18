package org.jobsui.core;

import com.thoughtworks.xstream.XStream;
import org.jobsui.core.runner.JobRunnerContext;
import org.jobsui.core.runner.JobValidation;
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
class JobRunner {
    private boolean valid = false;

    public <T extends Serializable, C> T run(final UI<C> ui, final Job<T> job) throws UnsupportedComponentException {
        valid = false;

        final UIWindow<C> window = ui.createWindow(job.getName());

        final JobValues values = new JobValuesImpl();

        List<UnsupportedComponentException> exceptions = new ArrayList<>();

        AtomicReference<T> result = new AtomicReference<>(null);

        window.show(() -> {

            JobRunnerContext<T,C> context;

            try {
                 context = new JobRunnerContext<>(job, ui, window);
            } catch (UnsupportedComponentException e) {
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
                    result.set(resultFuture.get());
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
                for (JobParameterDef jobParameterDef : job.getParameterDefs()) {
                    setValue(values, map, jobParameterDef);
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

    private static void setValue(JobValues values, Map<String, Serializable> map, JobParameterDef jobParameterDef) {
        values.setValue(jobParameterDef, map.get(jobParameterDef.getKey()));
    }

    public boolean isValid() {
        return valid;
    }


}
