package io.jmix.flowui.view;

import com.vaadin.flow.component.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.util.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.flowui.component.UiComponentUtils.findFocusComponent;

public class View<T extends Component> extends Composite<T>
        implements BeforeEnterObserver, AfterNavigationObserver, BeforeLeaveObserver {

    private static final Logger log = LoggerFactory.getLogger(View.class);

    private ApplicationContext applicationContext;

    private ViewData viewData;
    private ViewActions viewActions;
    private ViewFacets viewFacets;

    private Consumer<View<T>> closeDelegate;

    private String focusComponentId;
    private FocusMode focusMode;

    private boolean closeActionPerformed = false;

    public View() {
        closeDelegate = createDefaultViewDelegate();
    }

    private Consumer<View<T>> createDefaultViewDelegate() {
        return __ -> getViewSupport().close(this);
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public Optional<String> getId() {
        return super.getId();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        updatePageTitle();
        focusChildComponent();

        fireEvent(new AfterShowEvent(this));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        fireEvent(new BeforeShowEvent(this));
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        log.debug("{} - beforeLeave", getClass().getSimpleName());
        if (!event.isPostponed()) {
            log.debug("{} - beforeLeave - !Postponed", getClass().getSimpleName());

            if (!closeActionPerformed) {
                log.debug("{} - beforeLeave - !closeActionPerformed", getClass().getSimpleName());

                CloseAction closeAction = new NavigateCloseAction(event);
                BeforeCloseEvent beforeCloseEvent = new BeforeCloseEvent(this, closeAction);
                fireEvent(beforeCloseEvent);

                if (beforeCloseEvent.isClosePrevented()) {
                    log.debug("{} - beforeLeave - ClosePrevented", getClass().getSimpleName());
                    return;
                }

                AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
                fireEvent(afterCloseEvent);
            }
        }

        log.debug("{} - beforeLeave - closeActionPerformed = false", getClass().getSimpleName());
        closeActionPerformed = false;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        log.debug("{} - onDetach", getClass().getSimpleName());
        super.onDetach(detachEvent);

        removeApplicationListeners();
        unregisterBackNavigation();
    }

    private void updatePageTitle() {
        String pageTitle = applicationContext.getBean(ViewSupport.class)
                .getLocalizedPageTitle(this);
        getUI().ifPresent(ui -> ui.getPage().setTitle(pageTitle));
    }

    private void focusChildComponent() {
        if (focusMode != FocusMode.NO_FOCUS) {
            getFocusComponent().ifPresent(Focusable::focus);
        }
    }

    private void unregisterBackNavigation() {
        getViewSupport().unregisterBackNavigation(this);
    }

    private void removeApplicationListeners() {
        getApplicationContext().getBean(UiEventsManager.class).removeApplicationListeners(this);
    }

    public OperationResult closeWithDefaultAction() {
        return close(StandardOutcome.CLOSE);
    }

    public OperationResult close(StandardOutcome outcome) {
        return close(outcome.getCloseAction());
    }

    public OperationResult close(CloseAction closeAction) {
        log.debug("{} - close - {}", getClass().getSimpleName(), closeAction);

        BeforeCloseEvent beforeCloseEvent = new BeforeCloseEvent(this, closeAction);
        fireEvent(beforeCloseEvent);
        if (beforeCloseEvent.isClosePrevented()) {
            log.debug("{} - close - ClosePrevented", getClass().getSimpleName());
            return beforeCloseEvent.getCloseResult()
                    .orElse(OperationResult.fail());
        }


        log.debug("{} - close - closeActionPerformed = true", getClass().getSimpleName());
        closeActionPerformed = true;

        closeDelegate.accept(this);

        removeApplicationListeners();

        AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
        fireEvent(afterCloseEvent);

        return OperationResult.success();
    }

    Consumer<View<T>> getCloseDelegate() {
        return closeDelegate;
    }

    void setCloseDelegate(Consumer<View<T>> closeDelegate) {
        this.closeDelegate = closeDelegate;
    }

    protected ViewData getViewData() {
        return viewData;
    }

    protected void setViewData(ViewData viewData) {
        this.viewData = viewData;
    }

    protected ViewActions getViewActions() {
        return viewActions;
    }

    protected void setViewActions(ViewActions viewActions) {
        this.viewActions = viewActions;
    }

    protected ViewFacets getViewFacets() {
        return viewFacets;
    }

    protected void setViewFacets(ViewFacets viewFacets) {
        this.viewFacets = viewFacets;
    }

    protected Optional<Focusable<?>> getFocusComponent() {
        String componentId = getFocusComponentId();
        if (componentId != null) {
            return findFocusComponent(this, componentId);
        } else {
            return findFocusComponent(this);
        }
    }

    @Nullable
    public String getFocusComponentId() {
        return focusComponentId;
    }

    public void setFocusComponentId(@Nullable String focusComponentId) {
        this.focusComponentId = focusComponentId;
    }

    public FocusMode getFocusMode() {
        return focusMode;
    }

    public void setFocusMode(FocusMode focusMode) {
        this.focusMode = focusMode;
    }

    /**
     * Adds {@link InitEvent} listener.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe
     *    public void onInit(InitEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener the listener to add, not {@code null}
     * @return a handle that can be used for removing the listener
     */
    protected Registration addInitListener(ComponentEventListener<InitEvent> listener) {
        return getEventBus().addListener(InitEvent.class, listener);
    }

    protected Registration addBeforeShowListener(ComponentEventListener<BeforeShowEvent> listener) {
        return getEventBus().addListener(BeforeShowEvent.class, listener);
    }

    protected Registration addAfterShowListener(ComponentEventListener<AfterShowEvent> listener) {
        return getEventBus().addListener(AfterShowEvent.class, listener);
    }

    protected Registration addBeforeCloseListener(ComponentEventListener<BeforeCloseEvent> listener) {
        return getEventBus().addListener(BeforeCloseEvent.class, listener);
    }

    protected Registration addAfterCloseListener(ComponentEventListener<AfterCloseEvent> listener) {
        return getEventBus().addListener(AfterCloseEvent.class, listener);
    }

    //    @TriggerOnce
    public static class InitEvent extends ComponentEvent<View<?>> {

        public InitEvent(View<?> source) {
            super(source, false);
        }
    }

    //    @TriggerOnce
    public static class BeforeShowEvent extends ComponentEvent<View<?>> {

        public BeforeShowEvent(View<?> source) {
            super(source, false);
        }
    }

    //    @TriggerOnce
    public static class AfterShowEvent extends ComponentEvent<View<?>> {

        public AfterShowEvent(View<?> source) {
            super(source, false);
        }
    }

    public static class BeforeCloseEvent extends ComponentEvent<View<?>> {

        protected final CloseAction closeAction;

        protected OperationResult closeResult;
        protected boolean closePrevented = false;

        public BeforeCloseEvent(View<?> source, CloseAction closeAction) {
            super(source, false);
            this.closeAction = closeAction;
        }

        public CloseAction getCloseAction() {
            return closeAction;
        }

        public void preventWindowClose() {
            this.closePrevented = true;
        }

        public void preventWindowClose(OperationResult closeResult) {
            this.closePrevented = true;
            this.closeResult = closeResult;
        }

        public boolean isClosePrevented() {
            return closePrevented;
        }

        public Optional<OperationResult> getCloseResult() {
            return Optional.ofNullable(closeResult);
        }

        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    public static class AfterCloseEvent extends ComponentEvent<View<?>> {

        protected final CloseAction closeAction;

        public AfterCloseEvent(View<?> source, CloseAction closeAction) {
            super(source, false);
            this.closeAction = closeAction;
        }

        public CloseAction getCloseAction() {
            return closeAction;
        }

        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    private ViewSupport getViewSupport() {
        return applicationContext.getBean(ViewSupport.class);
    }
}
