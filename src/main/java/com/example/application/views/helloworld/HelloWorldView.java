package com.example.application.views.helloworld;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
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

    private TextField name;
    private Button sayHello;

    public HelloWorldView() {
        H2 h2 = new H2("This is otsikko");
        name = new TextField("Your name");
        sayHello = new Button("Say hello");

        Paragraph paragraph = new Paragraph("Muokkaa tätä tekstiä");

        sayHello.addClickListener(e -> {
            Notification.show("Hello " + name.getValue());
            paragraph.setText(name.getValue());
        });
        sayHello.addClickShortcut(Key.ENTER);

        HorizontalLayout horizontal = new HorizontalLayout();
        horizontal.add(new Paragraph("Moikka"));
        horizontal.add(new Anchor("www.google.com"));
        horizontal.add(new Paragraph("Moikka"), paragraph);

        add(horizontal);
        setMargin(true);
        setHorizontalComponentAlignment(Alignment.CENTER, h2, name, sayHello);
        add(h2);
        add(name, sayHello);
    }

}
