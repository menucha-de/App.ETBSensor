package havis.custom.harting.etb.sensor.ui.client;

import havis.custom.harting.etb.sensor.rest.async.MiddlewareServiceAsync;
import havis.custom.harting.etb.sensor.ui.resourcebundle.AppResources;
import havis.net.rest.shared.data.SerializableValue;
import havis.net.ui.shared.client.ErrorPanel;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ETBSensorUI extends Composite implements EntryPoint {

	@UiField
	GraphSection graphSection;
	@UiField
	ConfigSection configSection;
	
	ErrorPanel errorPanel = new ErrorPanel(0, 0);

	private AppResources appRes = AppResources.INSTANCE;
	private ResourceBundle res = ResourceBundle.INSTANCE;
	
	private MiddlewareServiceAsync service = GWT
			.create(MiddlewareServiceAsync.class);

	private static ETBSensorSectionUiBinder uiBinder = GWT
			.create(ETBSensorSectionUiBinder.class);

	interface ETBSensorSectionUiBinder extends UiBinder<Widget, ETBSensorUI> {
	}

	public ETBSensorUI() {
		initWidget(uiBinder.createAndBindUi(this));
		new GraphSectionPresenter(graphSection);
		new ConfigSectionPresenter(configSection);

		service.getLifetime(new MethodCallback<SerializableValue<String>>() {
			

			@Override
			public void onSuccess(Method method,
					SerializableValue<String> response) {
				int lifetime = Integer.parseInt(response.getValue());
				if (lifetime <= 500) {
					graphSection.setOpen(true);
				} else {
					errorPanel
							.showWarningMessage(
									"The tag lifetime should be <= 500 ms. This app will maybe not work probably.");
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				graphSection.setVisible(false);
				configSection.setVisible(false);
				errorPanel.showErrorMessage(
						"The Middleware service is not reachable. This app will not work.");

			}
		});

		appRes.css().ensureInjected();
		res.css().ensureInjected();

	}

	@Override
	public void onModuleLoad() {
		RootLayoutPanel.get().add(this);
	}
}
