package com.example.application.views.sampleperson;

import com.example.application.data.SampleBook;
import com.example.application.data.SamplePerson;
import com.example.application.data.SamplePersonType;
import com.example.application.services.SampleBookService;
import com.example.application.services.SamplePersonService;
import com.example.application.services.SamplePersonTypeService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Sampleperson")
@Route("sampleperson/:samplePersonID?/:action?(edit)")
@Menu(order = 2, icon = LineAwesomeIconUrl.COLUMNS_SOLID)
@AnonymousAllowed
@Uses(Icon.class)
public class SamplepersonView extends Div implements BeforeEnterObserver {

    private final String SAMPLEPERSON_ID = "samplePersonID";
    private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "sampleperson/%s/edit";

    private final Grid<SamplePerson> grid = new Grid<>(SamplePerson.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private DatePicker dateOfBirth;
    private TextField occupation;
    private TextField role;
    private Checkbox important;
    private ComboBox<SamplePersonType> samplePersonType;
    private MultiSelectComboBox<SampleBook> sampleBooks;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");

    private final BeanValidationBinder<SamplePerson> binder;

    private SamplePerson samplePerson;

    private final SamplePersonService samplePersonService;
    private final SamplePersonTypeService samplePersonTypeService;
    private final SampleBookService sampleBookService;

    public SamplepersonView(SamplePersonService samplePersonService,
                            SamplePersonTypeService samplePersonTypeService, SampleBookService sampleBookService) {
        this.samplePersonService = samplePersonService;
        this.samplePersonTypeService = samplePersonTypeService;
        this.sampleBookService = sampleBookService;
        addClassNames("sampleperson-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("dateOfBirth").setAutoWidth(true);
        grid.addColumn("occupation").setAutoWidth(true);
        grid.addColumn("role").setAutoWidth(true);
        LitRenderer<SamplePerson> importantRenderer = LitRenderer.<SamplePerson>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", important -> important.isImportant() ? "check" : "minus").withProperty("color",
                        important -> important.isImportant()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Important").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(
                p -> {
                    if (p.getSamplePersonType() != null){
                        Span span = new Span(p.getSamplePersonType().getName());
                        Tooltip tooltip = Tooltip.forComponent(span)
                                .withText(p.getSamplePersonType().getDescription())
                                .withPosition(Tooltip.TooltipPosition.TOP_START);
                        return span;
                    }
                    return new Span("");
                }
        )).setHeader("Sample Person Type").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>( samplePerson ->{
                if (samplePerson.getSampleBooks().isEmpty())
                    return new Span("");
                AtomicReference<String> sampleBookString = new AtomicReference<>("");
                samplePerson.getSampleBooks().forEach(
                        sampleBook ->
                                sampleBookString.set(sampleBookString + sampleBook.getName() + " ")
                );
                return new Span(sampleBookString.get());
        })).setHeader("Sample Book").setAutoWidth(true);

        grid.setItems(query -> samplePersonService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
                delete.setEnabled(true);
            } else {
                clearForm();
                UI.getCurrent().navigate(SamplepersonView.class);
                delete.setEnabled(false);
            }
        });

        samplePersonType.setItems(query -> samplePersonTypeService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        samplePersonType.setItemLabelGenerator(SamplePersonType::getName);

        sampleBooks.setItems(query -> sampleBookService.list(
                VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        sampleBooks.setItemLabelGenerator(SampleBook::getName);

        // Configure Form
        binder = new BeanValidationBinder<>(SamplePerson.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.forField(sampleBooks).bind(
                samplePerson -> new HashSet<>(samplePerson.getSampleBooks()),
                (samplePerson, formSet) ->
                        samplePerson.setSampleBooks(new ArrayList<>(formSet))
        );

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.samplePerson == null) {
                    this.samplePerson = new SamplePerson();
                }
                binder.writeBean(this.samplePerson);
                // tallennus, kun omistajuus on toisella entiteetillä
                sampleBooks.getValue().forEach(
                        book -> {
                            List<SamplePerson> samplePersonList = book.getSamplePersons();
                            samplePersonList.add(this.samplePerson);
                            book.setSamplePersons(samplePersonList);
                            this.sampleBookService.save(book);
                        }
                );
                samplePersonService.save(this.samplePerson);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(SamplepersonView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });

        delete.setEnabled(false);
        delete.addClickListener(e -> {
            try {
                if (this.samplePerson != null) {
                    samplePersonService.delete(this.samplePerson.getId());
                    clearForm();
                    refreshGrid();
                    Notification.show("Data deleted");
                    UI.getCurrent().navigate(SamplepersonView.class);
                }
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<SamplePerson> samplePersonFromBackend = samplePersonService.get(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(SamplepersonView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        phone = new TextField("Phone");
        dateOfBirth = new DatePicker("Date Of Birth");
        occupation = new TextField("Occupation");
        role = new TextField("Role");
        important = new Checkbox("Important");
        samplePersonType = new ComboBox<>("Sample Person Type");
        sampleBooks = new MultiSelectComboBox<>("Sample Books");
        formLayout.add(firstName, lastName, email, phone,
                dateOfBirth, occupation, role, important,
                samplePersonType, sampleBooks);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_WARNING);
        buttonLayout.add(save, cancel, delete);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SamplePerson value) {
        this.samplePerson = value;
        binder.readBean(this.samplePerson);

    }
}
