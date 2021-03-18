package havis.app.etb.sensor.ui.client.subscriber.table;

import havis.app.etb.sensor.ui.resourcebundle.AppResources;
import havis.net.ui.shared.client.table.CustomWidgetRow;
import havis.transport.Subscriber;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;

public class SubscriberListItemEditor extends CustomWidgetRow implements HasValueChangeHandlers<Boolean> {
	Label uri = new Label();
	ToggleButton enable = new ToggleButton();

	Subscriber subscriber;
	
	public SubscriberListItemEditor() {
		uri.setStyleName(AppResources.INSTANCE.css().webuiCustomTableLabel());
		enable.setStyleName("webui-EnableButton subscriber subscriberList");
		enable.getDownFace().setText("Active");
		enable.getUpFace().setText("Inactive");

		addColumn(uri);
		addColumn(enable);
	}

	public String getUri() {
		return uri.getText();
	}

	public void setUri(String uri) {
		this.uri.setText(uri);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
		return enable.addValueChangeHandler(handler);
	}

	public void setValue(boolean enabled) {
		enable.setDown(enabled);
	}
	
	public void setSubscriber(Subscriber s){
		this.subscriber = s;
	}
	
	public Subscriber getSubscriber(){
		return subscriber;
	}
	
}
