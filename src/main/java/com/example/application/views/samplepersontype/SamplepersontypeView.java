package com.example.application.views.samplepersontype;

import com.example.application.data.SamplePersonType;
import com.example.application.services.SamplePersonTypeService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Samplepersontype")
@Route("samplepersontype/:samplePersonTypeID?/:action?(edit)")
@Menu(order = 3, icon = LineAwesomeIconUrl.COLUMNS_SOLID)
@RolesAllowed("USER")
@Uses(Icon.class)
public class SamplepersontypeView extends Div implements BeforeEnterObserver {

    private final String SAMPLEPERSONTYPE_ID = "samplePersonTypeID";
    private final String SAMPLEPERSONTYPE_EDIT_ROUTE_TEMPLATE = "samplepersontype/%s/edit";

    private final Grid<SamplePersonType> grid = new Grid<>(SamplePersonType.class, false);

    private TextField name;
    private TextField description;
    private Checkbox active;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SamplePersonType> binder;

    private SamplePersonType samplePersonType;

    private final SamplePersonTypeService samplePersonTypeService;

    public SamplepersontypeView(SamplePersonTypeService samplePersonTypeService) {
        this.samplePersonTypeService = samplePersonTypeService;
        addClassNames("samplepersontype-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);
        LitRenderer<SamplePersonType> activeRenderer = LitRenderer.<SamplePersonType>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", active -> active.isActive() ? "check" : "minus").withProperty("color",
                        active -> active.isActive()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(activeRenderer).setHeader("Active").setAutoWidth(true);

        grid.setItems(
                query -> samplePersonTypeService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEPERSONTYPE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SamplepersontypeView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(SamplePersonType.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.samplePersonType == null) {
                    this.samplePersonType = new SamplePersonType();
                }
                binder.writeBean(this.samplePersonType);
                samplePersonTypeService.save(this.samplePersonType);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(SamplepersontypeView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonTypeId = event.getRouteParameters().get(SAMPLEPERSONTYPE_ID).map(Long::parseLong);
        if (samplePersonTypeId.isPresent()) {
            Optional<SamplePersonType> samplePersonTypeFromBackend = samplePersonTypeService
                    .get(samplePersonTypeId.get());
            if (samplePersonTypeFromBackend.isPresent()) {
                populateForm(samplePersonTypeFromBackend.get());
            } else {
                Notification.show(String.format("The requested samplePersonType was not found, ID = %s",
                        samplePersonTypeId.get()), 3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(SamplepersontypeView.class);
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
        name = new TextField("Name");
        description = new TextField("Description");
        active = new Checkbox("Active");
        formLayout.add(name, description, active);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
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

    private void populateForm(SamplePersonType value) {
        this.samplePersonType = value;
        binder.readBean(this.samplePersonType);

    }
}
