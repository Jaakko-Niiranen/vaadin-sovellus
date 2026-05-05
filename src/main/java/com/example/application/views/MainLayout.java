package com.example.application.views;

import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import com.vaadin.flow.component.HasElement;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;
    private HasElement currentViewContent;

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Avaa valikko");

        Span appName = new Span("Vaadin sovellus");
        appName.addClassName("app-header__name");

        viewTitle = new H1();
        viewTitle.addClassNames("app-header__title", LumoUtility.Margin.NONE);

        Div titleGroup = new Div(appName, viewTitle);
        titleGroup.addClassName("app-header__titles");

        Header header = new Header(toggle, titleGroup, createUserControls());
        header.addClassName("app-header");

        addToNavbar(true, header);
    }

    private void addDrawerContent() {
        Span appName = new Span("Vaadin sovellus");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            if (entry.icon() != null) {
                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
            } else {
                nav.addItem(new SideNavItem(entry.title(), entry.path()));
            }
        });

        return nav;
    }

    private Div createUserControls() {
        Div layout = new Div();
        layout.addClassName("app-header__user");

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            if (user.getProfilePicture() != null) {
                StreamResource resource = new StreamResource("profile-pic",
                        () -> new ByteArrayInputStream(user.getProfilePicture()));
                avatar.setImageResource(resource);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            Span userIdentifier = new Span(user.getUsername());
            userIdentifier.addClassName("app-header__user-identifier");

            Button logoutButton = new Button("Logout", event -> {
                authenticatedUser.logout();
            });
            logoutButton.setIcon(VaadinIcon.SIGN_OUT.create());
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            logoutButton.addClassName("app-header__logout");

            layout.add(avatar, userIdentifier, logoutButton);
        } else {
            Span userIdentifier = new Span("Vierailija");
            userIdentifier.addClassName("app-header__user-identifier");

            Anchor loginLink = new Anchor("login", "Sign in");
            loginLink.addClassName("app-header__login");
            layout.add(userIdentifier);
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content != null) {
            this.currentViewContent = content;

            Div page = new Div();
            page.addClassName("app-page");

            Div viewContent = new Div();
            viewContent.addClassName("app-page__content");
            viewContent.getElement().appendChild(content.getElement());

            page.add(viewContent, createApplicationFooter());

            setContent(page);
        }
    }

    private Footer createApplicationFooter() {
        Footer footer = new Footer();
        footer.addClassName("app-footer");

        Div author = new Div();
        author.addClassName("app-footer__author");
        author.setText("Tekijä: Jaakko Niiranen");

        Div copyright = new Div();
        copyright.addClassName("app-footer__copyright");
        copyright.setText("© 2026 Jaakko Niiranen. Kaikki oikeudet pidätetään.");

        Div links = new Div();
        links.addClassName("app-footer__links");

        Anchor githubLink = new Anchor("https://github.com/Jaakko-Niiranen/vaadin-sovellus", "GitHub");
        githubLink.setTarget("_blank");
        githubLink.getElement().setAttribute("rel", "noopener noreferrer");

        Anchor vaadinLink = new Anchor("https://vaadin.com", "Vaadin");
        vaadinLink.setTarget("_blank");
        vaadinLink.getElement().setAttribute("rel", "noopener noreferrer");

        Anchor infoLink = new Anchor("mailto:jaakko.niiranen@example.com", "Lisätiedot");
        infoLink.setTarget("_blank");

        links.add(githubLink, vaadinLink, infoLink);

        footer.add(author, copyright, links);

        return footer;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        if (currentViewContent == null) {
            return "";
        }

        PageTitle title = currentViewContent.getClass().getAnnotation(PageTitle.class);

        if (title != null) {
            return title.value();
        }

        return currentViewContent.getClass().getSimpleName();
    }
}
