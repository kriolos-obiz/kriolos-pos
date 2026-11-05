package com.openbravo.ui.data.binding;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A type-safe data binder for Java Swing components, providing two-way data binding,
 * field validation pipelines, dirty tracking, and bean synchronization.
 *
 * @param <B> the type of the backend business object (bean) being bound
 * @author KriolOS
 * @since 1.0.0
 */
public class SwingBinder<B> {

    private B bean;
    private final List<Runnable> statusChangeListeners = new ArrayList<>();
    private final List<BindingLifecycle<B>> bindings = new ArrayList<>();
    private final Map<JComponent, Boolean> validationMap = new HashMap<>();
    private boolean isDirty = false;

    public SwingBinder(B bean) {
        this.bean = bean;
    }

    public B getBean() {
        return bean;
    }

    /**
     * Reads the values from the given source bean into the UI components,
     * updates the internal bean reference, and resets the dirty and validation
     * states.
     *
     * @param sourceBean the new bean instance to read from
     */
    public void readBean(B sourceBean) {
        this.bean = sourceBean;
        this.isDirty = false;
        this.validationMap.clear();
        for (BindingLifecycle<B> binding : bindings) {
            binding.readBean(sourceBean);
        }
    }

    /**
     * Discards all unsaved changes from the UI components, reinstating the values
     * from the currently bound bean and clearing all validation errors.
     * Replicates the exact behavior of Vaadin Flow's discard implementation.
     */
    public void discard() {
        this.isDirty = false;
        this.validationMap.clear();
        if (this.bean != null) {
            for (BindingLifecycle<B> binding : bindings) {
                binding.readBean(this.bean);
            }
        }
        // Notify listeners so UI button states (e.g., Save/Cancel) refresh appropriately
        for (Runnable listener : statusChangeListeners) {
            listener.run();
        }
    }
    
    /**
     * Clears all bound UI components to their empty state values and resets 
     * the internal validation and dirty states.
     */
    public void clear() {
        this.bean = null;
        this.isDirty = false;
        this.validationMap.clear();
        for (BindingLifecycle<B> binding : bindings) {
            binding.clear();
        }
        for (Runnable listener : statusChangeListeners) {
            listener.run();
        }
    }

    /**
     * Validates all bound elements and writes the values from the UI components
     * into the specified bean if all rules pass.
     *
     * @param targetBean the destination object to write properties into
     * @throws ValidationException if one or more fields contain structural
     * validation errors
     */
    public void writeBean(B targetBean) throws ValidationException {
        boolean allValid = true;
        for (BindingLifecycle<B> binding : bindings) {
            if (!binding.validateCurrentValue()) {
                allValid = false;
            }
        }

        if (!allValid) {
            throw new ValidationException("Form contains invalid parameters. Write operation rejected.");
        }

        for (BindingLifecycle<B> binding : bindings) {
            binding.writeBean(targetBean);
        }
        this.isDirty = false;
    }

    public void addStatusChangeListener(Runnable listener) {
        this.statusChangeListeners.add(listener);
    }

    protected void registerBinding(BindingLifecycle<B> binding) {
        this.bindings.add(binding);
    }

    protected void fireStatusChanged() {
        this.isDirty = true;
        for (Runnable listener : statusChangeListeners) {
            listener.run();
        }
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    protected void setValidationStatus(JComponent component, boolean isValid) {
        validationMap.put(component, isValid);
    }

    public boolean isValid() {
        for (Boolean status : validationMap.values()) {
            if (Boolean.FALSE.equals(status)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the current validation status for a specific UI component.
     *
     * @param component the component to check
     * @return true if valid, false if invalid, null if unvalidated
     */
    public Boolean getValidationStatus(JComponent component) {
        return validationMap.get(component);
    }

    /**
     * Returns a list of all currently invalid bound UI components.
     *
     * @return list of components currently failing validation
     */
    public List<JComponent> getInvalidFields() {
        List<JComponent> invalid = new ArrayList<>();
        for (Map.Entry<JComponent, Boolean> entry : validationMap.entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                invalid.add(entry.getKey());
            }
        }
        return invalid;
    }

    /**
     * Validates all registered bindings against their constraints and updates
     * component visual indicators.
     *
     * @return true if all bindings pass validation, false otherwise
     */
    public boolean validate() {
        boolean allValid = true;
        for (BindingLifecycle<B> binding : bindings) {
            if (!binding.validateCurrentValue()) {
                allValid = false;
            }
        }
        return allValid;
    }

    public BindingBuilder<B, String> forField(JTextField component) {
        return new BindingBuilder<>(component, this);
    }

    public CheckBoxBuilder<B> forField(JCheckBox component) {
        return new CheckBoxBuilder<>(component, this);
    }

    public <T> ComboBoxBuilder<B, T> forField(JComboBox<T> component) {
        return new ComboBoxBuilder<>(component, this);
    }

    public void bind(JTextField component, Function<B, String> getter, BiConsumer<B, String> setter) {
        forField(component).bind(getter, setter);
    }

    public TextAreaBuilder<B> forField(JTextArea component) {
        return new TextAreaBuilder<>(component, this);
    }

    public <N extends Number> SpinnerBuilder<B, N> forField(JSpinner component) {
        return new SpinnerBuilder<>(component, this);
    }

    public <T> RadioButtonGroupBuilder<B, T> forRadioGroup() {
        return new RadioButtonGroupBuilder<>(this);
    }

    public SliderBuilder<B> forField(JSlider component) {
        return new SliderBuilder<>(component, this);
    }

    public <T> FormattedTextFieldBuilder<B, T> forField(JFormattedTextField component) {
        return new FormattedTextFieldBuilder<>(component, this);
    }

    /**
     * Internal contract allowing individual bindings to hook into the lifecycle
     * operations.
     */
    protected interface BindingLifecycle<T> {

        void readBean(T sourceBean);

        boolean validateCurrentValue();

        void writeBean(T targetBean);
        
        void clear();
    }

    /**
     * Exception thrown when user structural parameters violate bounded
     * framework validators.
     */
    public static class ValidationException extends Exception {

        private static final long serialVersionUID = 1L;

        public ValidationException(String message) {
            super(message);
        }
    }
}
