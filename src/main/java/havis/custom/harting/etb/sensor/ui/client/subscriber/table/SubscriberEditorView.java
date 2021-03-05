package havis.custom.harting.etb.sensor.ui.client.subscriber.table;

import havis.transport.Subscriber;
import havis.transport.ui.client.TransportPanel;
import havis.transport.ui.client.TransportType;
import havis.transport.ui.client.event.SaveTransportEvent;
import havis.transport.ui.client.event.TransportErrorEvent;

import java.util.List;

public interface SubscriberEditorView {
	public void setPresenter(Presenter presenter);

	public void setVisible(boolean visible);
	
	public void setEnable(boolean enable);
	
	public TransportPanel getTransport();

	interface Presenter {
		void onAcceptClick();

		void onCloseClick();

		void setVisible(boolean visible);

		void setHandlers(SaveTransportEvent.Handler save, TransportErrorEvent.Handler error);

		void setTransportObject(Subscriber transportObject);
		
		Subscriber getTransportObject();

		void setTransportTypes(List<TransportType> types);
	}

	public enum Mode {
		CREATE, CHANGE;
	}
}
