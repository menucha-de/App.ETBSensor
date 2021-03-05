package havis.custom.harting.etb.sensor.ui.client;

import havis.net.ui.shared.client.ConfigurationSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class GraphSection extends ConfigurationSection implements
		GraphSectionView {

	private GraphSectionView.Presenter presenter;

	@UiField
	FlowPanel graph;
	
	@UiField
	FlowPanel graphPlaceholder;
	
	@UiField 
	Button delete;
	
	@UiField 
	Button export;
	
	
	private static GraphSectionUiBinder uiBinder = GWT
			.create(GraphSectionUiBinder.class);

	interface GraphSectionUiBinder extends UiBinder<Widget, GraphSection> {
	}

	@UiConstructor
	public GraphSection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		this.presenter.onObserve();
	}

	@Override
	public FlowPanel getGraph() {
		return graph;
	}
	
	@Override
	protected void onOpenSection() {
		delete.setVisible(true);
		export.setVisible(true);
		super.onOpenSection();
		presenter.onOpen();
		
	}
	
	@Override
	protected void onCloseSection() {
		delete.setVisible(false);
		export.setVisible(false);
		super.onCloseSection();
		presenter.onClose();
	}

	@Override
	public FlowPanel getPlaceholder() {
		return graphPlaceholder;
	}
	
	@UiHandler("delete")
	void onClearHistory(ClickEvent event){
		presenter.onClearHistory();
	}
	
	@UiHandler("export")
	void onExport(ClickEvent event){
		presenter.onExportCSV();
	}


	
	
}
