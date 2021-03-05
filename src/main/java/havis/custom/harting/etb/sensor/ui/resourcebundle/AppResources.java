package havis.custom.harting.etb.sensor.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

public interface AppResources extends ClientBundle {

	public static final AppResources INSTANCE = GWT
			.create(AppResources.class);

	@Source("resources/CssResources.css")
	CssResources css();

	@Source("resources/LLRP_List_Export.png")
	ImageResource export();

	@Source("resources/BT_40x40_Delete.png")
	ImageResource clear();

	@Source("resources/delete_row.png")
	DataResource deleteRow();

	@Source("resources/editor_close.png")
	DataResource editorClose();

	@Source("resources/LLRP_Region_Dropdown_Arrow.png")
	DataResource dropDownArrow();

	@Source("resources/LLRP_Region_Dropdown_Arrow_disabled.png")
	DataResource dropDownArrowDisabled();
	
	@Source("resources/icon_error.png")
	ImageResource error();
	
	

}
