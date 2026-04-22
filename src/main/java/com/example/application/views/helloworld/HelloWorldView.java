package com.example.application.views.helloworld;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Hello World")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.GLOBE_SOLID)
@AnonymousAllowed
public class HelloWorldView extends VerticalLayout {

    private final TextField name;
    private final Button sayHello;

    public HelloWorldView() {
        addClassName("hello-world-view");
        setSpacing(true);
        setPadding(true);

        H2 heading = new H2("Hello World!");
        heading.addClassName("hello-world-title");

        Paragraph instructions = new Paragraph();
        instructions.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin", "0");

        name = new TextField();
        name.setPlaceholder("Kirjoita nimesi");
        name.setHelperText("Tervehdys päivittää alla olevan viestin.");

        sayHello = new Button("Say hello");
        sayHello.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Paragraph greeting = new Paragraph("Muokkaa tätä tekstiä painamalla nappia.");
        greeting.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("font-weight", "600");

        sayHello.addClickListener(e -> {
            Notification.show("Hello " + name.getValue());
            greeting.setText("Hei " + name.getValue() + "!");
        });
        sayHello.addClickShortcut(Key.ENTER);

        HorizontalLayout links = new HorizontalLayout();
        links.setAlignItems(Alignment.CENTER);
        links.add(greeting);

        setHorizontalComponentAlignment(Alignment.CENTER, heading, instructions, name, sayHello, links);
        add(heading, instructions, name, sayHello, links);
    }

}