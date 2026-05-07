package com.example.application.views.login;

import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Vaadin sovellus");
        i18n.getHeader().setDescription("Login using user/user123 or admin/admin123");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        Anchor registerLink = new Anchor("register", "Rekisteröidy");
        registerLink.getStyle()
                .set("color", "var(--app-accent-color-dark)")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("text-decoration", "none");

        VerticalLayout footerLayout = new VerticalLayout(registerLink);
        footerLayout.setPadding(false);
        footerLayout.setSpacing(false);
        footerLayout.setAlignItems(VerticalLayout.Alignment.CENTER);
        footerLayout.setWidthFull();

        getFooter().add(footerLayout);

        setForgotPasswordButtonVisible(true);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
