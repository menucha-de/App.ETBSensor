package havis.app.etb.sensor.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Constants;

public interface ConstantsResource extends Constants {
	
	public static final ConstantsResource INSTANCE = GWT.create(ConstantsResource.class);

	String appHeader();
	String graphSection();
	String configSection();
	
	String configConversion();
	String configUnit();
	String configCeiling();
	String configFloor();
	String configSubscribers();
	String configNewNotification();
	String configNewSubscriber();
	
	String subscriberType();
	String subscriberHost();
	String subscriberPort();
	String subscriberUsername();
	String subscriberPassword();
	String subscriberPath();
	String subscriberQuery();
	String subscriberTopic();
	String subscriberClientId();
	String subscriberQoS();	
	String buttonApply();
	
	
}
