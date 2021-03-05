package havis.custom.harting.etb.sensor.ui.client;

import com.google.gwt.user.client.ui.FlowPanel;



public interface GraphSectionView {
	public FlowPanel getGraph();
	public FlowPanel getPlaceholder();
	void setPresenter(Presenter presenter);
	interface Presenter {
		void onObserve();
		void onOpen();
		void onClose();
		void onClearHistory();
		void onExportCSV();
		
	}
}
