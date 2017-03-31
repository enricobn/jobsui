package org.jobsui.core.ui.javafx;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

/**
 * Created by enrico on 3/30/17.
 */
class ProgressDialog {
    private final Stage dialogStage;
    private final ProgressBar pb = new ProgressBar();
    private final ProgressIndicator pin = new ProgressIndicator();

    public static <T> void run(Task<T> task, String title, Consumer<T> consumer) {
        run(task, title, consumer, ex -> {throw new RuntimeException(ex);});
    }

    public static <T> void run(Task<T> task, String title, Consumer<T> consumer, Consumer<Throwable> exceptionHandler) {
        ProgressDialog pForm = new ProgressDialog(title);

        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            try {
                consumer.accept(task.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        task.setOnFailed(event -> {
            pForm.getDialogStage().close();
            exceptionHandler.accept(event.getSource().getException());
        });

        pForm.getDialogStage().show();

        Platform.runLater(() -> {
            pForm.getDialogStage().centerOnScreen();
        });

        new Thread(task).start();
    }

    private ProgressDialog(String title) {
        dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        // PROGRESS BAR
//            final Label label = new Label();
//            label.setText("alerto");

//            pb.setProgress(-1F);
        pin.setProgress(-1F);

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
//            hb.getChildren().addAll(pb, pin);
        hb.getChildren().add(pb);

        Scene scene = new Scene(hb);
        dialogStage.setScene(scene);
        dialogStage.setWidth(150);
    }

//        public void activateProgressBar(final Task<?> task)  {
//            pb.progressProperty().bind(task.progressProperty());
//            pin.progressProperty().bind(task.progressProperty());
//            dialogStage.show();
//        }

    public Stage getDialogStage() {
        return dialogStage;
    }
}
