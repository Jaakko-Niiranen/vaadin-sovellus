package com.example.application.views.gridwithfilters;

import com.example.application.data.SamplePerson;
import com.example.application.services.SamplePersonService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.example.application.data.SampleBook;
import com.example.application.data.SamplePersonType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;

import java.util.Locale;
import java.util.Map;

@PageTitle("Grid with Filters")
@Route("grid-with-filters")
@Menu(order = 4, icon = LineAwesomeIconUrl.FILTER_SOLID)
@AnonymousAllowed
@Uses(Icon.class)
public class GridwithFiltersView extends Div {

    private Grid<SamplePerson> grid;
    private Filters filters;
    private H2 pageHeading;
    private Span mobileFiltersHeading;
    private ComboBox<Locale> languageSelect;

    private Locale currentLocale = new Locale("fi");

    private final SamplePersonService samplePersonService;

    private static final Map<String, Map<String, String>> TRANSLATIONS = Map.of(
            "fi", Map.ofEntries(
                    Map.entry("page.title", "Henkilöhaku"),
                    Map.entry("language", "Kieli"),
                    Map.entry("language.fi", "Suomi"),
                    Map.entry("language.en", "Englanti"),
                    Map.entry("filters", "Suodattimet"),
                    Map.entry("name", "Nimi"),
                    Map.entry("phone", "Puhelin"),
                    Map.entry("dateOfBirth", "Syntymäaika"),
                    Map.entry("occupation", "Ammatti"),
                    Map.entry("role", "Rooli"),
                    Map.entry("personType", "Henkilötyyppi"),
                    Map.entry("book", "Kirja"),
                    Map.entry("firstOrLastName", "Etu- tai sukunimi"),
                    Map.entry("from", "Alkaen"),
                    Map.entry("to", "Asti"),
                    Map.entry("fromDate", "Alkupäivä"),
                    Map.entry("toDate", "Loppupäivä"),
                    Map.entry("reset", "Tyhjennä"),
                    Map.entry("search", "Hae"),
                    Map.entry("firstName", "Etunimi"),
                    Map.entry("lastName", "Sukunimi"),
                    Map.entry("email", "Sähköposti")
            ),
            "en", Map.ofEntries(
                    Map.entry("page.title", "Person Search"),
                    Map.entry("language", "Language"),
                    Map.entry("language.fi", "Finnish"),
                    Map.entry("language.en", "English"),
                    Map.entry("filters", "Filters"),
                    Map.entry("name", "Name"),
                    Map.entry("phone", "Phone"),
                    Map.entry("dateOfBirth", "Date of Birth"),
                    Map.entry("occupation", "Occupation"),
                    Map.entry("role", "Role"),
                    Map.entry("personType", "Person type"),
                    Map.entry("book", "Book"),
                    Map.entry("firstOrLastName", "First or last name"),
                    Map.entry("from", "From"),
                    Map.entry("to", "To"),
                    Map.entry("fromDate", "From date"),
                    Map.entry("toDate", "To date"),
                    Map.entry("reset", "Reset"),
                    Map.entry("search", "Search"),
                    Map.entry("firstName", "First name"),
                    Map.entry("lastName", "Last name"),
                    Map.entry("email", "Email")
            )
    );

    private String t(String key) {
        return TRANSLATIONS
                .getOrDefault(currentLocale.getLanguage(), TRANSLATIONS.get("fi"))
                .getOrDefault(key, key);
    } 

    public GridwithFiltersView(SamplePersonService SamplePersonService) {

        this.samplePersonService = SamplePersonService;

        setSizeFull();
        addClassNames("gridwith-filters-view");

        pageHeading = new H2();
        languageSelect = createLanguageSelect();

        filters = new Filters(() -> refreshGrid(), this::t);

        HorizontalLayout toolbar = new HorizontalLayout(pageHeading, languageSelect);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.addClassNames(LumoUtility.Padding.MEDIUM);

        VerticalLayout layout = new VerticalLayout(toolbar, createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        add(layout);
        updateTexts();
    }

    private void updateTexts() {
        pageHeading.setText(t("page.title"));
        languageSelect.setLabel(t("language"));

        if (mobileFiltersHeading != null) {
            mobileFiltersHeading.setText(t("filters"));
        }

        if (filters != null) {
            filters.updateTexts();
        }

        if (grid != null) {
            grid.getColumns().get(0).setHeader(t("firstName"));
            grid.getColumns().get(1).setHeader(t("lastName"));
            grid.getColumns().get(2).setHeader(t("email"));
            grid.getColumns().get(3).setHeader(t("phone"));
            grid.getColumns().get(4).setHeader(t("dateOfBirth"));
            grid.getColumns().get(5).setHeader(t("occupation"));
            grid.getColumns().get(6).setHeader(t("role"));
        }

        getUI().ifPresent(ui -> ui.getPage().setTitle(t("page.title")));
    }

    private ComboBox<Locale> createLanguageSelect() {
        ComboBox<Locale> select = new ComboBox<>();
        select.setItems(new Locale("fi"), Locale.ENGLISH);
        select.setValue(currentLocale);

        select.setItemLabelGenerator(locale -> {
            if ("fi".equals(locale.getLanguage())) {
                return t("language.fi");
            }
            return t("language.en");
        });

        select.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                currentLocale = event.getValue();
                updateTexts();
            }
        });

        return select;
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER, LumoUtility.Background.CONTRAST_5, LumoUtility.TextColor.HEADER,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.BoxShadow.XSMALL);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        mobileFiltersHeading = new Span();
        Span filtersHeading = mobileFiltersHeading;
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<SamplePerson> {

        private final TextField name = new TextField();
        private final TextField phone = new TextField();
        private final DatePicker startDate = new DatePicker();
        private final DatePicker endDate = new DatePicker();
        private final MultiSelectComboBox<String> occupations = new MultiSelectComboBox<>();
        private final CheckboxGroup<String> roles = new CheckboxGroup<>();
        private final TextField personType = new TextField();
        private final TextField book = new TextField();

        private final Button resetBtn = new Button();
        private final Button searchBtn = new Button();

        private final java.util.function.Function<String, String> t;

        public Filters(Runnable onSearch, java.util.function.Function<String, String> t) {
            this.t = t;

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER, LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL,
                    LumoUtility.BorderRadius.LARGE, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);
            name.setPlaceholder("First or last name");

            occupations.setItems("Insurance Clerk", "Mortarman", "Beer Coil Cleaner", "Scale Attendant");

            roles.setItems("Worker", "Supervisor", "Manager", "External");
            roles.addClassName("double-width");

            // Action buttons
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();
                phone.clear();
                startDate.clear();
                endDate.clear();
                occupations.clear();
                roles.clear();
                personType.clear();
                book.clear();
                onSearch.run();
            });
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(name, phone, createDateRangeFilter(), occupations, roles, personType, book, actions);
            updateTexts();
        }

        public void updateTexts() {
            name.setLabel(t.apply("name"));
            phone.setLabel(t.apply("phone"));
            startDate.setLabel(t.apply("dateOfBirth"));
            occupations.setLabel(t.apply("occupation"));
            roles.setLabel(t.apply("role"));
            personType.setLabel(t.apply("personType"));
            book.setLabel(t.apply("book"));

            name.setPlaceholder(t.apply("firstOrLastName"));
            startDate.setPlaceholder(t.apply("from"));
            endDate.setPlaceholder(t.apply("to"));

            startDate.setAriaLabel(t.apply("fromDate"));
            endDate.setAriaLabel(t.apply("toDate"));

            resetBtn.setText(t.apply("reset"));
            searchBtn.setText(t.apply("search"));
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");

            endDate.setPlaceholder("To");

            // For screen readers
            startDate.setAriaLabel("From date");
            endDate.setAriaLabel("To date");

            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        @Override
        public Predicate toPredicate(Root<SamplePerson> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!name.isEmpty()) {
                String lowerCaseFilter = name.getValue().toLowerCase();
                Predicate firstNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),
                        lowerCaseFilter + "%");
                Predicate lastNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }
            if (!phone.isEmpty()) {
                String databaseColumn = "phone";
                String ignore = "- ()";

                String lowerCaseFilter = ignoreCharacters(ignore, phone.getValue().toLowerCase());
                Predicate phoneMatch = criteriaBuilder.like(
                        ignoreCharacters(ignore, criteriaBuilder, criteriaBuilder.lower(root.get(databaseColumn))),
                        "%" + lowerCaseFilter + "%");
                predicates.add(phoneMatch);

            }
            if (startDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
                        criteriaBuilder.literal(startDate.getValue())));
            }
            if (endDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()),
                        root.get(databaseColumn)));
            }
            if (!occupations.isEmpty()) {
                String databaseColumn = "occupation";
                List<Predicate> occupationPredicates = new ArrayList<>();
                for (String occupation : occupations.getValue()) {
                    occupationPredicates
                            .add(criteriaBuilder.equal(criteriaBuilder.literal(occupation), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(occupationPredicates.toArray(Predicate[]::new)));
            }
            if (!roles.isEmpty()) {
                String databaseColumn = "role";
                List<Predicate> rolePredicates = new ArrayList<>();
                for (String role : roles.getValue()) {
                    rolePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(role), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(rolePredicates.toArray(Predicate[]::new)));
            }
            if (!personType.isEmpty()) {
                Join<SamplePerson, SamplePersonType> personTypeJoin =
                        root.join("samplePersonType", JoinType.LEFT);

                String lowerCaseFilter = "%" + personType.getValue().toLowerCase() + "%";

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(personTypeJoin.get("name")),
                        lowerCaseFilter
                ));
            }

            if (!book.isEmpty()) {
                Join<SamplePerson, SampleBook> bookJoin =
                        root.join("sampleBooks", JoinType.LEFT);

                String lowerCaseFilter = "%" + book.getValue().toLowerCase() + "%";

                Predicate bookNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(bookJoin.get("name")),
                        lowerCaseFilter
                );

                Predicate bookAuthorMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(bookJoin.get("author")),
                        lowerCaseFilter
                );

                predicates.add(criteriaBuilder.or(bookNameMatch, bookAuthorMatch));

                query.distinct(true);
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(SamplePerson.class, false);

        grid.addColumn("firstName").setHeader(t("firstName")).setAutoWidth(true);
        grid.addColumn("lastName").setHeader(t("lastName")).setAutoWidth(true);
        grid.addColumn("email").setHeader(t("email")).setAutoWidth(true);
        grid.addColumn("phone").setHeader(t("phone")).setAutoWidth(true);
        grid.addColumn("dateOfBirth").setHeader(t("dateOfBirth")).setAutoWidth(true);
        grid.addColumn("occupation").setHeader(t("occupation")).setAutoWidth(true);
        grid.addColumn("role").setHeader(t("role")).setAutoWidth(true);

        grid.setItems(query -> samplePersonService.list(VaadinSpringDataHelpers.toSpringPageRequest(query), filters)
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.BoxShadow.XSMALL);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
