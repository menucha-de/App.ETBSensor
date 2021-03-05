package havis.custom.harting.etb.sensor.ui.client;

import havis.custom.harting.etb.sensor.ui.client.subscriber.table.SubscriberEditor;
import havis.custom.harting.etb.sensor.ui.client.subscriber.table.SubscriberEditorPresenter;
import havis.custom.harting.etb.sensor.ui.client.subscriber.table.SubscriberEditorView;
import havis.custom.harting.etb.sensor.ui.client.subscriber.table.SubscriberEditorView.Mode;
import havis.custom.harting.etb.sensor.ui.resourcebundle.ConstantsResource;
import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.table.ChangeRowEvent;
import havis.net.ui.shared.client.table.CreateRowEvent;
import havis.net.ui.shared.client.table.CustomTable;
import havis.net.ui.shared.client.table.DeleteRowEvent;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class ConfigSection extends ConfigurationSection implements ConfigSectionView {
	private ConfigSectionView.Presenter presenter;
	private static ConfigSectionUiBinder uiBinder = GWT.create(ConfigSectionUiBinder.class);
	private ConstantsResource i18n = ConstantsResource.INSTANCE;

	interface ConfigSectionUiBinder extends UiBinder<Widget, ConfigSection> {
	}

	@UiField
	TextBox conversion;

	@UiField
	TextBox unit;

	@UiField
	TextBox ceiling;

	@UiField
	TextBox floor;

	@UiField
	ToggleButton enableCeiling;

	@UiField
	ToggleButton enableFloor;

	@UiField
	Label unitLabelCeiling;

	@UiField
	Label unitLabelFloor;

	@UiField
	CustomTable subscribers;

	@UiField
	CustomTable ceilingNotifications;

	@UiField
	CustomTable floorNotifications;

	@UiField
	SubscriberEditor subscriberEditor;

	private ChangeHandler changeHandler = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
			presenter.onConfigurationChanged();
		}
	};

	@UiConstructor
	public ConfigSection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
		floor.addChangeHandler(changeHandler);
		ceiling.addChangeHandler(changeHandler);
		conversion.addChangeHandler(changeHandler);
		subscribers.setHeader(Arrays.asList(i18n.configNewSubscriber()));
		ceilingNotifications.setHeader(Arrays.asList(i18n.configNewNotification()));
		floorNotifications.setHeader(Arrays.asList(i18n.configNewNotification()));
		subscribers.setColumnWidth(1, 8, Unit.EM);
		ceilingNotifications.setColumnWidth(1, 8, Unit.EM);
		floorNotifications.setColumnWidth(1, 8, Unit.EM);
	}

	@Override
	public void setPresenter(ConfigSectionView.Presenter presenter) {
		this.presenter = presenter;
		this.presenter.onMonitor();
	}

	@Override
	public TextBox getConversion() {
		return conversion;
	}

	@Override
	public HasText getUnit() {
		return unit;
	}

	@Override
	public TextBox getCeiling() {
		return ceiling;
	}

	@Override
	public TextBox getFloor() {
		return floor;
	}

	@Override
	public HasValue<Boolean> getFloorEnabled() {
		return enableFloor;
	}

	@Override
	public HasValue<Boolean> getCeilingEnabled() {
		return enableCeiling;
	}

	@Override
	public Label getUnitLabelCeiling() {
		return unitLabelCeiling;
	}

	@Override
	public Label getUnitLabelFloor() {
		return unitLabelFloor;
	}

	@UiHandler("enableCeiling")
	void onToogleCeiling(ValueChangeEvent<Boolean> event) {
		presenter.onConfigurationChanged();
		presenter.onToggleLimit(TYPE.CEILING);
	}

	@UiHandler("enableFloor")
	void onToogleFloor(ValueChangeEvent<Boolean> event) {
		presenter.onConfigurationChanged();
		presenter.onToggleLimit(TYPE.FLOOR);
	}

	@UiHandler("unit")
	void onUnitChange(ChangeEvent changed) {
		presenter.onConfigurationChanged();
	}

	@UiHandler("subscribers")
	void onCreateSubscriber(CreateRowEvent event) {
		presenter.onSubscriberEditorOpen(Mode.CREATE, TYPE.SUBSCRIBER, subscribers, -1);
	}

	@UiHandler("subscribers")
	void onDeleteSubscriber(DeleteRowEvent event) {
		presenter.onSubscriberRemoved(TYPE.SUBSCRIBER, subscribers, event.getIndex());
		presenter.onConfigurationChanged();
	}

	@UiHandler("subscribers")
	void onChangeSubscriber(ChangeRowEvent event) {
		presenter.onSubscriberEditorOpen(Mode.CHANGE, TYPE.SUBSCRIBER, subscribers, event.getIndex());
	}

	@UiHandler("ceilingNotifications")
	void onCreateCeilingNotification(CreateRowEvent event) {
		presenter.onSubscriberEditorOpen(Mode.CREATE, TYPE.CEILING, ceilingNotifications, -1);
	}

	@UiHandler("ceilingNotifications")
	void onDeleteCeilingNotification(DeleteRowEvent event) {
		presenter.onSubscriberRemoved(TYPE.CEILING, ceilingNotifications, event.getIndex());
		presenter.onConfigurationChanged();
	}

	@UiHandler("ceilingNotifications")
	void onChangeCeilingNotification(ChangeRowEvent event) {
		presenter.onSubscriberEditorOpen(Mode.CHANGE, TYPE.CEILING, ceilingNotifications, event.getIndex());
	}

	@UiHandler("floorNotifications")
	void onCreateFloorNotification(CreateRowEvent event) {
		presenter.onSubscriberEditorOpen(Mode.CREATE, TYPE.FLOOR, floorNotifications, -1);
	}

	@UiHandler("floorNotifications")
	void onDeleteFloorNotification(DeleteRowEvent event) {
		presenter.onSubscriberRemoved(TYPE.FLOOR, floorNotifications, event.getIndex());
		presenter.onConfigurationChanged();
	}

	@UiHandler("floorNotifications")
	void onChangeFloorNotification(ChangeRowEvent event) {
		presenter.onSubscriberEditorOpen(Mode.CHANGE, TYPE.FLOOR, floorNotifications, event.getIndex());
	}

	@Override
	public CustomTable getSubscribers() {
		return subscribers;
	}

	@Override
	public CustomTable getCeilingNotifications() {
		return ceilingNotifications;
	}

	@Override
	public CustomTable getFloorNotifications() {
		return floorNotifications;
	}

	@Override
	public SubscriberEditorView.Presenter getSubscriberEditor() {
		return new SubscriberEditorPresenter(subscriberEditor);
	}

}
