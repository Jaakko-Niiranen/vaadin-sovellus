package com.example.application.views.profile;

import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@CssImport("./themes/vaadin-sovellus/views/register-view.css")
@PageTitle("Oma profiili")
@Route(value = "profile", layout = MainLayout.class)
@Menu(order = 0, icon = "line-awesome/svg/user-circle.svg")
@PermitAll
public class ProfileView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final UserRepository userRepository;

    private Avatar avatar;

    public ProfileView(AuthenticatedUser authenticatedUser, UserRepository userRepository) {
        this.authenticatedUser = authenticatedUser;
        this.userRepository = userRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        authenticatedUser.get().ifPresentOrElse(
                this::createProfileContent,
                () -> add(new Paragraph("Käyttäjää ei löytynyt. Kirjaudu sisään uudelleen."))
        );
    }

    private void createProfileContent(User user) {
        H2 title = new H2("Oma profiili");

        avatar = new Avatar(user.getName());
        avatar.setWidth("96px");
        avatar.setHeight("96px");
        updateAvatarImage(user);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFileSize(1_000_000);
        upload.setDropLabel(new Paragraph("Pudota kuva tähän tai valitse tiedosto"));
        upload.setUploadButton(new Button("Valitse profiilikuva"));

        upload.addSucceededListener(event -> {
            try {
                byte[] imageBytes = buffer.getInputStream().readAllBytes();

                user.setProfilePicture(imageBytes);
                userRepository.save(user);

                updateAvatarImage(user);

                Notification notification = Notification.show("Profiilikuva tallennettu.");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IOException e) {
                Notification notification = Notification.show("Kuvan tallennus epäonnistui.");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(event -> {
            Notification notification = Notification.show(event.getErrorMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        Button removeImageButton = new Button("Poista profiilikuva", event -> {
            user.setProfilePicture(null);
            userRepository.save(user);

            avatar.setImage(null);

            Notification notification = Notification.show("Profiilikuva poistettu.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
            removeImageButton.addClassName("orange-button");
        add(
                title,
                avatar,
                new Paragraph("Kirjautunut käyttäjänä: " + user.getUsername()),
                upload,
                removeImageButton
        );
    }

    private void updateAvatarImage(User user) {
        if (user.getProfilePicture() != null) {
            StreamResource resource = new StreamResource(
                    "profile-picture-" + user.getId(),
                    () -> new ByteArrayInputStream(user.getProfilePicture())
            );
            avatar.setImageResource(resource);
        }
    }
}
