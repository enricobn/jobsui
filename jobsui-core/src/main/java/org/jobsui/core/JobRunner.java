package org.jobsui.core;

import com.thoughtworks.xstream.XStream;
import org.jobsui.core.runner.JobRunnerContext;
import org.jobsui.core.runner.JobValidation;
import org.jobsui.core.runner.ParameterAndWidget;
import org.jobsui.core.runner.WidgetsMap;
import org.jobsui.core.ui.*;
import org.jobsui.core.ui.javafx.JavaFXUI;
import rx.Observable;
import rx.functions.FuncN;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.*;
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

            observeDependencies(job, context.getWidgets());

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
                for (JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
                    setValue(values, map, jobParameterDef);
                }
            });

            notifyInitialValue(context.getWidgets());

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

    private static <T extends Serializable> void setValue(JobValues values, Map<String, Serializable> map, JobParameterDef<T> jobParameterDef) {
        values.setValue(jobParameterDef, (T)map.get(jobParameterDef.getKey()));
    }

    private <C> void notifyInitialValue(WidgetsMap<C> widgets) {
        widgets.getWidgets().forEach(this::notifyInitialValue);
    }

    private <T extends Serializable, C> void notifyInitialValue(ParameterAndWidget<T, C> entry) {
        final T value = entry.getWidget().getComponent().getValue();
        if (entry.getJobParameterDef().validate(value).isEmpty()) {
            entry.getWidget().getComponent().notifySubscribers();
        }
    }


//    private <T, C> Observable<Map<String, Object>> observeValues(final UI<?> ui, final Job<T> job, final UIWindow<?> window,
//                                                                 final UIButton<C> runButton,
//                                                                 final WidgetsMap<C> widgetsMap) {
//        final List<Subscriber<? super Map<String, Object>>> subscribers = new ArrayList<>();
//        Observable<Map<String, Object>> result = Observable.create(subscribers::add);
//
//        final Map<String,Object> values = new HashMap<>();
//
//        List<Observable<?>> observables = widgetsMap.getWidgets().stream()
//                .map(widget -> widget.getWidget().getComponent().getObservable())
//                .collect(Collectors.toList());
//
//        Observable<Boolean> combined = Observable.combineLatest(observables, new FuncN<Boolean>() {
//            @Override
//            public Boolean call(Object... args) {
//                values.clear();
//                subscribers.forEach(subscriber -> subscriber.onNext(values));
//
//                int i = 0;
//
//                for (final ParameterAndWidget<Serializable, C> entry : widgetsMap.getWidgets()) {
//                    if (addValidValue(entry, (Serializable) args[i++])) break;
//                }
//
//                if (values.size() != args.length) {
//                    return false;
//                }
//
//                final List<String> validate = job.validate(values);
//                if (!validate.isEmpty()) {
//                    window.showValidationMessage(JobsUIUtils.getMessagesAsString(validate));
//                } else {
//                    window.showValidationMessage(null);
//                }
//                return validate.isEmpty();
//            }
//
//            private boolean addValidValue(ParameterAndWidget<Serializable, C> entry, Serializable value) {
//                final JobParameterDef<Serializable> parameterDef = entry.getJobParameterDef();
//                final List<String> validate = parameterDef.validate(value);
//
//                setValidationMessage(validate, parameterDef, entry.getWidget(), ui);
//
//                if (!validate.isEmpty()) {
//                    return true;
//                }
//                values.put(parameterDef.getKey(), value);
//                subscribers.forEach(subscriber -> subscriber.onNext(values));
//                return false;
//            }
//        });
//
////        combined.subscribe(window::setValid);
////        combined.subscribe(runButton::setEnabled);
//        combined.subscribe(v -> {
//            valid = v;
//            runButton.setEnabled(v);
//        });
//        return result;
//    }

    public boolean isValid() {
        return valid;
    }

    private <T extends Serializable, C> void observeDependencies(Job<T> job, final WidgetsMap<C> widgets) {
        for (final JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
            final List<JobParameterDef<? extends Serializable>> dependencies = jobParameterDef.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<Serializable>> observables = getDependenciesObservables(widgets, dependencies);

                final Observable<Map<String, Serializable>> observable = combineDependenciesObservables(dependencies, observables);

                observable.subscribe(objects -> {
                    // all dependencies are valid
                    if (objects.size() == dependencies.size()) {
                        final UIWidget widget = widgets.get(jobParameterDef);
                        widget.getComponent().setEnabled(true);
                        try {
                            jobParameterDef.onDependenciesChange(widget, objects);
                        } catch (Exception e) {
                            JavaFXUI.showErrorStatic("Error on onDependenciesChange for parameter " + jobParameterDef.getName(), e);
                            widget.setValidationMessages(Collections.singletonList(e.getMessage()));
                            widget.getComponent().setValue(null);
                            widget.getComponent().setEnabled(false);
                        }
                    }
                });
            }
        }
    }

    private Observable<Map<String, Serializable>> combineDependenciesObservables(final List<JobParameterDef<? extends Serializable>> dependencies,
                                                               List<Observable<Serializable>> observables) {
        return Observable.combineLatest(observables, new FuncN<Map<String,Serializable>>() {
            @Override
            public Map<String,Serializable> call(Object... args) {
                Map<String, Serializable> result = new HashMap<>();

                int i = 0;
                for (JobParameterDef dependency : dependencies) {
                    final Serializable arg = (Serializable) args[i++];
                    if (addValidatedValue(result, dependency, arg)) break;
                }
                return result;
            }

            private <T extends Serializable> boolean addValidatedValue(Map<String, Serializable> result, JobParameterDef<T> dependency, T arg) {
                final List<String> validate = dependency.validate(arg);
                if (!validate.isEmpty()) {
                    return true;
                }
                result.put(dependency.getKey(), arg);
                return false;
            }
        });
    }

    private <C> List<Observable<Serializable>> getDependenciesObservables(WidgetsMap<C> widgetsMap,
                                                           List<JobParameterDef<? extends Serializable>> dependencies) {
        List<Observable<Serializable>> observables = new ArrayList<>();
        for (JobParameterDef dependency : dependencies) {
            final UIWidget widget = widgetsMap.get(dependency);
            if (widget == null) {
                throw new IllegalStateException("Cannot find widget for dependency with key \"" +
                        dependency.getKey() + "\".");
            }
            observables.add(widget.getComponent().getObservable());
        }
        return observables;
    }


}
