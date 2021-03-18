package havis.app.etb.sensor.ui.client;

import havis.app.etb.sensor.Configuration;
import havis.app.etb.sensor.Limit;
import havis.app.etb.sensor.rest.async.ETBSensorServiceAsync;
import havis.app.etb.sensor.ui.client.ConfigSectionView.TYPE;
import havis.app.etb.sensor.ui.client.subscriber.table.SubscriberEditorView;
import havis.app.etb.sensor.ui.client.subscriber.table.SubscriberEditorView.Mode;
import havis.app.etb.sensor.ui.client.subscriber.table.SubscriberListItemEditor;
import havis.net.ui.shared.client.table.CustomTable;
import havis.transport.Subscriber;
import havis.transport.ui.client.TransportType;
import havis.transport.ui.client.event.SaveTransportEvent;
import havis.transport.ui.client.event.TransportErrorEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.TextCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class ConfigSectionPresenter implements ConfigSectionView.Presenter {
	private ETBSensorServiceAsync service = GWT.create(ETBSensorServiceAsync.class);
	private ConfigSectionView view;
	private Configuration configuration;
	private List<TransportType> types;
	private boolean hasError = false;

	public ConfigSectionPresenter(final ConfigSectionView configSectionView) {
		this.view = configSectionView;
		view.setPresenter(this);
	}

	@Override
	public void onMonitor() {
		service.getConfiguration(new MethodCallback<Configuration>() {
			@Override
			public void onSuccess(Method method, Configuration response) {
				configuration = response;
				view.getConversion().setText(response.getConversion());
				view.getUnit().setText(response.getUnit());
				view.getUnitLabelCeiling().setText(response.getUnit());
				view.getUnitLabelFloor().setText(response.getUnit());
				if (response.getCeiling() != null) {
					view.getCeiling().setText(Float.toString(response.getCeiling().getValue()));
					for (final Subscriber notification : configuration.getCeiling().getNotifications()) {
						final SubscriberListItemEditor row = new SubscriberListItemEditor();
						row.setUri(notification.getUri());
						row.setSubscriber(notification);
						row.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								Subscriber s = row.getSubscriber();
								s.setEnable(event.getValue());
								service.updateSubscriber(s, new MethodCallback<Void>() {
									@Override
									public void onFailure(Method method, Throwable exception) {
										ErrorViewer.showExceptionResponse(exception);
									}

									@Override
									public void onSuccess(Method method, Void response) {
										refreshConfig();
									}
								});
							}
						});
						row.setValue(notification.isEnable());
						view.getCeilingNotifications().addRow(row);
					}
				}
				if (response.getCeiling() == null || !response.getCeiling().isEnable()) {
					view.getCeilingEnabled().setValue(false);
					setCeilingVisible(false);
				} else {
					view.getCeilingEnabled().setValue(true);
				}

				if (response.getFloor() != null) {
					view.getFloor().setText(Float.toString(response.getFloor().getValue()));
					for (final Subscriber notification : configuration.getFloor().getNotifications()) {
						final SubscriberListItemEditor row = new SubscriberListItemEditor();
						row.setUri(notification.getUri());
						row.setSubscriber(notification);
						row.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								Subscriber s = row.getSubscriber();
								s.setEnable(event.getValue());
								service.updateSubscriber(s, new MethodCallback<Void>() {
									@Override
									public void onFailure(Method method, Throwable exception) {
										ErrorViewer.showExceptionResponse(exception);
									}

									@Override
									public void onSuccess(Method method, Void response) {
										refreshConfig();
									}
								});
							}
						});
						row.setValue(notification.isEnable());
						view.getFloorNotifications().addRow(row);
					}
				}
				if (response.getFloor() == null || !response.getFloor().isEnable()) {
					view.getFloorEnabled().setValue(false);
					setFloorVisible(false);
				} else {
					view.getFloorEnabled().setValue(true);
				}

				for (final Subscriber subscriber : configuration.getSubscribers()) {
					final SubscriberListItemEditor row = new SubscriberListItemEditor();
					row.setUri(subscriber.getUri());
					row.setSubscriber(subscriber);
					row.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							Subscriber s = row.getSubscriber();
							s.setEnable(event.getValue());
							service.updateSubscriber(s, new MethodCallback<Void>() {
								@Override
								public void onFailure(Method method, Throwable exception) {
									ErrorViewer.showExceptionResponse(exception);
								}

								@Override
								public void onSuccess(Method method, Void response) {
									refreshConfig();
								}
							});
						}
					});
					row.setValue(subscriber.isEnable());
					view.getSubscribers().addRow(row);
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				ErrorViewer.showExceptionResponse(exception);
			}
		});

		service.getTransportTypes(new MethodCallback<List<String>>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				ErrorViewer.showExceptionResponse(exception);
			}

			@Override
			public void onSuccess(Method method, List<String> response) {
				types = new ArrayList<>();
				types.add(TransportType.CUSTOM);
				if (response != null) {
					for (String type : response) {
						TransportType tType = null;
						try {
							tType = TransportType.valueOf(type.toUpperCase());
						} catch (Exception e) {
							// ignore
						}
						if (tType != null && tType != TransportType.CUSTOM) {
							types.add(tType);
						}
					}
					view.getSubscriberEditor().setTransportTypes(types);
				}
			}
		});

	}

	@Override
	public void onToggleLimit(TYPE t) {
		switch (t) {
		case CEILING:
			if (view.getCeilingEnabled().getValue()) {
				// activate
				setCeilingVisible(true);
			} else {
				// deactivate
				setCeilingVisible(false);
			}
			break;
		case FLOOR:
			if (view.getFloorEnabled().getValue()) {
				// activate
				setFloorVisible(true);
			} else {
				// deactivate
				setFloorVisible(false);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onConfigurationChanged() {

		if (view.getConversion().getValue().length() == 0)
			view.getConversion().setValue("value");

		Map<String, String> configData = new HashMap<>();
		configData.put("conversion", view.getConversion().getText());
		if (view.getCeilingEnabled().getValue())
			configData.put("ceiling", view.getCeiling().getValue());
		if (view.getFloorEnabled().getValue())
			configData.put("floor", view.getFloor().getValue());

		service.validateConfiguration(configData, new MethodCallback<Map<String, String>>() {
			@Override
			public void onSuccess(Method method, Map<String, String> response) {
				String error = response.get("conversion");
				boolean firstError = false;
				if (error != null) {
					firstError = true;
					ErrorViewer.showError(error);
				}

				error = response.get("ceiling");
				if (error != null && !firstError) {
					firstError = true;
					ErrorViewer.showError(error);
				}

				error = response.get("floor");
				if (error != null && !firstError) {
					firstError = true;
					ErrorViewer.showError(error);
				}

				if (response.isEmpty()) {
					configuration.setConversion(view.getConversion().getText());
					configuration.setUnit(view.getUnit().getText());
					if (configuration.getCeiling() == null) {
						configuration.setCeiling(new Limit());
					}
					if (view.getCeilingEnabled().getValue()) {
						configuration.getCeiling().setEnable(true);
						configuration.getCeiling().setValue(Float.parseFloat(view.getCeiling().getText()));
					} else {
						configuration.getCeiling().setEnable(false);
					}

					if (configuration.getFloor() == null) {
						configuration.setFloor(new Limit());
					}
					if (view.getFloorEnabled().getValue()) {
						configuration.getFloor().setEnable(true);
						configuration.getFloor().setValue(Float.parseFloat(view.getFloor().getText()));
					} else {
						configuration.getFloor().setEnable(false);
					}
					updateConfiguration();
					onRefreshLabels();
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				ErrorViewer.showExceptionResponse(exception);
			}
		});
	}

	@Override
	public void onRefreshLabels() {
		view.getUnitLabelCeiling().setText(configuration.getUnit());
		view.getUnitLabelFloor().setText(configuration.getUnit());
	}

	@Override
	public void onSubscriberRemoved(TYPE type, CustomTable table, int index) {
		switch (type) {
		case SUBSCRIBER:
			configuration.getSubscribers().remove(index);
			break;
		case CEILING:
			configuration.getCeiling().getNotifications().remove(index);
			break;
		case FLOOR:
			configuration.getFloor().getNotifications().remove(index);
			break;
		default:
			break;
		}

		table.deleteRow(index);

	}

	@Override
	public void onSubscriberEditorOpen(final Mode mode, final TYPE type, final CustomTable table, final int index) {
		final SubscriberEditorView.Presenter subscriberEditor = view.getSubscriberEditor();
		subscriberEditor.setTransportTypes(types);
		subscriberEditor.setVisible(true);
		if (index >= 0) {
			switch (type) {
			case SUBSCRIBER:
				subscriberEditor.setTransportObject(configuration.getSubscribers().get(index));
				break;
			case CEILING:
				subscriberEditor.setTransportObject(configuration.getCeiling().getNotifications().get(index));
				break;
			case FLOOR:
				subscriberEditor.setTransportObject(configuration.getFloor().getNotifications().get(index));
				break;
			default:
				break;
			}
		} else {
			Subscriber transportObject = new Subscriber();
			transportObject.setEnable(true);
			subscriberEditor.setTransportObject(transportObject);
		}
		TransportErrorEvent.Handler errorHandler = new TransportErrorEvent.Handler() {
			@Override
			public void onTransportError(TransportErrorEvent event) {
				if (event.isException()) {
					ErrorViewer.showExceptionResponse(event.getException());
				} else {
					ErrorViewer.showError(event.getErrorMessage());
				}
				hasError = true;
			}
		};
		SaveTransportEvent.Handler saveHandler = null;
		if (mode == Mode.CREATE) {
			saveHandler = new SaveTransportEvent.Handler() {
				@Override
				public void onSaveTransport(SaveTransportEvent event) {
					if (!hasError) {
						subscriberEditor.setVisible(false);
						final SubscriberListItemEditor row = new SubscriberListItemEditor();
						String url = event.getUri();
						row.setUri(url);
						final Subscriber transportObject = subscriberEditor.getTransportObject();
						transportObject.setUri(url);
						transportObject.setProperties(event.getProperties());
						row.setValue(transportObject.isEnable());
						row.setSubscriber(transportObject);
						service.addSubscriber(transportObject, type.toString(), new TextCallback() {
							@Override
							public void onSuccess(Method method, String response) {
								transportObject.setId(response);
								switch (type) {
								case SUBSCRIBER:
									configuration.getSubscribers().add(transportObject);
									break;
								case CEILING:
									configuration.getCeiling().getNotifications().add(transportObject);
									break;
								case FLOOR:
									configuration.getFloor().getNotifications().add(transportObject);
									break;
								default:
									break;
								}
								table.addRow(row);
								row.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
									@Override
									public void onValueChange(ValueChangeEvent<Boolean> event) {
										Subscriber s = row.getSubscriber();
										s.setEnable(event.getValue());
										service.updateSubscriber(s, new MethodCallback<Void>() {
											@Override
											public void onFailure(Method method, Throwable exception) {
												ErrorViewer.showExceptionResponse(exception);
											}

											@Override
											public void onSuccess(Method method, Void response) {
												refreshConfig();
											}
										});
										
									}
								});
								updateConfiguration();
							}

							@Override
							public void onFailure(Method method, Throwable exception) {
								ErrorViewer.showExceptionResponse(exception);
							}
						});
					}
					hasError = false;
				}
			};
		} else {
			saveHandler = new SaveTransportEvent.Handler() {
				@Override
				public void onSaveTransport(SaveTransportEvent event) {
					if (!hasError) {
						subscriberEditor.setVisible(false);
						SubscriberListItemEditor row = (SubscriberListItemEditor) table.getRow(index);
						String newUrl = event.getUri();
						row.setUri(newUrl);
						final Subscriber transportObject = subscriberEditor.getTransportObject();
						transportObject.setUri(newUrl);
						transportObject.setProperties(event.getProperties());
						row.setValue(transportObject.isEnable());
						row.setSubscriber(transportObject);
						switch (type) {
						case SUBSCRIBER:
							configuration.getSubscribers().set(index, transportObject);
							break;
						case CEILING:
							configuration.getCeiling().getNotifications().set(index, transportObject);
							break;
						case FLOOR:
							configuration.getFloor().getNotifications().set(index, transportObject);
							break;
						default:
							break;
						}
						updateConfiguration();
					}
					hasError = false;
				}
			};
		}
		subscriberEditor.setHandlers(saveHandler, errorHandler);
	}

	private void updateConfiguration() {
		service.setConfiguration(configuration, new MethodCallback<Void>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				ErrorViewer.showExceptionResponse(exception);
			}

			@Override
			public void onSuccess(Method method, Void response) {
				refreshConfig();
			}
		});
	}

	private void refreshConfig() {
		service.getConfiguration(new MethodCallback<Configuration>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				ErrorViewer.showExceptionResponse(exception);
			}

			@Override
			public void onSuccess(Method method, Configuration response) {
				configuration = response;
			}
		});
	}

	private void setCeilingVisible(boolean visible) {
		view.getCeiling().setVisible(visible);
		view.getUnitLabelCeiling().setVisible(visible);
		view.getCeilingNotifications().setVisible(visible);
	}

	private void setFloorVisible(boolean visible) {
		view.getFloor().setVisible(visible);
		view.getUnitLabelFloor().setVisible(visible);
		view.getFloorNotifications().setVisible(visible);
	}
}
