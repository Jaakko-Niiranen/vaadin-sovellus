package com.example.application.views.serverpush;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Server Push")
@Route("server-push")
@Menu(order = 5, icon = LineAwesomeIconUrl.SYNC_ALT_SOLID)
@AnonymousAllowed
public class ServerPushView extends Div {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ProgressBar progressBar = new ProgressBar(0, 100, 0);
    private final Span progressText = new Span("Odottaa käynnistystä");
    private final Span serverTime = new Span();
    private final Button startButton = new Button("Käynnistä taustatyö");
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService executor;

    public ServerPushView() {
        addClassNames(LumoUtility.Padding.LARGE);

        H2 heading = new H2("Vaadin Server Push");

        Paragraph description = new Paragraph(
                "Tämä näkymä käynnistää palvelimen taustasäikeessä työn, joka päivittää käyttöliittymää "
                        + "Server Pushin avulla ilman selaimen manuaalista päivitystä.");

        progressBar.setWidthFull();
        progressText.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
        serverTime.addClassNames(LumoUtility.TextColor.SECONDARY);

        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startButton.addClickListener(event -> startBackgroundJob(UI.getCurrent()));

        VerticalLayout layout = new VerticalLayout(
                heading,
                description,
                startButton,
                progressBar,
                progressText,
                serverTime
        );

        layout.setWidthFull();
        layout.setMaxWidth("720px");
        layout.setPadding(false);

        add(layout);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        executor = Executors.newSingleThreadExecutor();
        updateServerTime();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void startBackgroundJob(UI ui) {
        if (!running.compareAndSet(false, true)) {
            Notification.show("Taustatyö on jo käynnissä");
            return;
        }

        startButton.setEnabled(false);
        progressBar.setValue(0);
        progressText.setText("Taustatyö käynnissä...");

        executor.submit(() -> {
            try {
                for (int progress = 1; progress <= 100; progress++) {
                    Thread.sleep(75);

                    int currentProgress = progress;

                    ui.access(() -> {
                        progressBar.setValue(currentProgress);
                        progressText.setText("Valmis " + currentProgress + " %");
                        updateServerTime();
                    });
                }

                ui.access(() -> {
                    progressText.setText("Taustatyö valmis");
                    startButton.setEnabled(true);
                    running.set(false);
                    updateServerTime();
                });

            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();

                ui.access(() -> {
                    progressText.setText("Taustatyö keskeytettiin");
                    startButton.setEnabled(true);
                    running.set(false);
                    updateServerTime();
                });
            }
        });
    }

    private void updateServerTime() {
        serverTime.setText("Palvelimen aika: " + LocalDateTime.now().format(TIME_FORMATTER));
    }
}
