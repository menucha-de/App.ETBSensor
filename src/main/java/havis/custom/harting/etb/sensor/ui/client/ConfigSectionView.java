package havis.custom.harting.etb.sensor.ui.client;

import havis.custom.harting.etb.sensor.ui.client.subscriber.table.SubscriberEditorView;
import havis.custom.harting.etb.sensor.ui.client.subscriber.table.SubscriberEditorView.Mode;
import havis.net.ui.shared.client.table.CustomTable;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public interface ConfigSectionView {
	public void setPresenter(Presenter presenter);

	public TextBox getConversion();

	public HasText getUnit();

	public HasValue<Boolean> getFloorEnabled();

	public HasValue<Boolean> getCeilingEnabled();

	public Label getUnitLabelCeiling();

	public Label getUnitLabelFloor();

	public TextBox getCeiling();

	public TextBox getFloor();

	public CustomTable getSubscribers();

	public CustomTable getCeilingNotifications();

	public CustomTable getFloorNotifications();

	public SubscriberEditorView.Presenter getSubscriberEditor();

	interface Presenter {
		public void onMonitor();

		public void onToggleLimit(TYPE t);

		public void onConfigurationChanged();

		public void onRefreshLabels();

		public void onSubscriberEditorOpen(Mode mode, TYPE type, CustomTable table, final int index);

		public void onSubscriberRemoved(TYPE type, CustomTable table, int index);

	}

	public enum TYPE {
		SUBSCRIBER, CEILING, FLOOR
	}

}
