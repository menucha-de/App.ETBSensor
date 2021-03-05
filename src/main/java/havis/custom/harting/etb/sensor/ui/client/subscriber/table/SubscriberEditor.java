package havis.custom.harting.etb.sensor.ui.client.subscriber.table;

import havis.transport.ui.client.TransportPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class SubscriberEditor extends Composite implements SubscriberEditorView {
	private static SubscriberEditorUiBinder uiBinder = GWT.create(SubscriberEditorUiBinder.class);

	interface SubscriberEditorUiBinder extends UiBinder<Widget, SubscriberEditor> {
	}

	private Presenter presenter;

	@UiField
	FlowPanel transport;

	@UiField
	ToggleButton enable;

	public SubscriberEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("infoCloseLabel")
	void onCloseClick(ClickEvent event) {
		presenter.onCloseClick();
	}

	@UiHandler("acceptButton")
	void onAcceptClick(ClickEvent event) {
		presenter.onAcceptClick();
	}

	@UiHandler("enable")
	void onEnableClick(ClickEvent event) {
		presenter.getTransportObject().setEnable(enable.isDown());
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public TransportPanel getTransport() {
		TransportPanel transport = new TransportPanel();
		this.transport.clear();
		this.transport.add(transport);
		return transport;
	}

	@Override
	public void setEnable(boolean enable) {
		if (this.enable != null) {
			this.enable.setDown(enable);
		}

	}

}