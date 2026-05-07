package com.example.application.views.register;

import com.example.application.data.Role;
import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vaadin.flow.component.dependency.CssImport;

import java.util.Set;

@CssImport("./themes/vaadin-sovellus/views/register-view.css")
@AnonymousAllowed
@PageTitle("Rekisteröidy")
@Route("register")
public class RegisterView extends VerticalLayout {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TextField name = new TextField("Nimi");
    private final TextField username = new TextField("Käyttäjätunnus");
    private final PasswordField password = new PasswordField("Salasana");
    private final PasswordField confirmPassword = new PasswordField("Vahvista salasana");

    public RegisterView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        H1 title = new H1("Rekisteröidy");
        Paragraph info = new Paragraph("Luo uusi käyttäjätunnus sovellukseen.");

        Button registerButton = new Button("Rekisteröidy", event -> register());
        registerButton.addClassName("orange-button");

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        name.setWidth("300px");
        username.setWidth("300px");
        password.setWidth("300px");
        confirmPassword.setWidth("300px");
        registerButton.setWidth("300px");
        add(title, info, name, username, password, confirmPassword, registerButton);
    }

    private void register() {
        String nameValue = name.getValue().trim();
        String usernameValue = username.getValue().trim();
        String passwordValue = password.getValue();
        String confirmPasswordValue = confirmPassword.getValue();

        if (nameValue.isEmpty() || usernameValue.isEmpty() || passwordValue.isEmpty()) {
            Notification.show("Täytä kaikki kentät.");
            return;
        }

        if (!passwordValue.equals(confirmPasswordValue)) {
            Notification.show("Salasanat eivät täsmää.");
            return;
        }

        if (userRepository.findByUsername(usernameValue).isPresent()) {
            Notification.show("Käyttäjätunnus on jo käytössä.");
            return;
        }

        User user = new User();
        user.setName(nameValue);
        user.setUsername(usernameValue);
        user.setHashedPassword(passwordEncoder.encode(passwordValue));
        user.setRoles(Set.of(Role.USER));

        userRepository.save(user);

        Notification.show("Rekisteröinti onnistui. Voit nyt kirjautua sisään.");
        getUI().ifPresent(ui -> ui.navigate("login"));
    }
}
