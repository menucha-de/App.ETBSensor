package havis.app.etb.sensor.ui.client.subscriber.table;

import havis.transport.Subscriber;
import havis.transport.ui.client.TransportPanel;
import havis.transport.ui.client.TransportType;
import havis.transport.ui.client.event.SaveTransportEvent.Handler;
import havis.transport.ui.client.event.TransportErrorEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubscriberEditorPresenter implements SubscriberEditorView.Presenter {
	private SubscriberEditorView view;
	private TransportPanel transport;
	private Subscriber current;
	
	public SubscriberEditorPresenter(final SubscriberEditorView view) {
		this.view = view;
		view.setPresenter(this);
		transport = view.getTransport();
	}

	@Override
	public void onAcceptClick() {
		transport.saveTransportData();
	}

	@Override
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}

	@Override
	public void onCloseClick() {
		view.setVisible(false);
	}

	@Override
	public void setTransportObject(Subscriber transportObject) {
		current = transportObject;
		transport.setData(transportObject.getUri(), transportObject.getProperties());
		view.setEnable(transportObject.isEnable());
	}
	
	public Subscriber getTransportObject(){
		return current;
	}

	@Override
	public void setTransportTypes(List<TransportType> types) {
		List<TransportType> copy = new ArrayList<TransportType>(types);
		Collections.sort(copy);
		transport.setTypes(copy);
	}

	@Override
	public void setHandlers(Handler save, TransportErrorEvent.Handler error) {
		transport.addSaveTransportHandler(save);
		transport.addTransportErrorHandler(error);
	}

}
