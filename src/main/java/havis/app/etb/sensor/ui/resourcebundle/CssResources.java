package havis.app.etb.sensor.ui.resourcebundle;

import havis.net.ui.shared.client.table.CustomTable;

import com.google.gwt.resources.client.CssResource;

public interface CssResources extends CssResource {

	String minus();

	String upload();

	String line();

	String button();

	@ClassName("disabled-button")
	String disabledButton();

	String found();

	@ClassName("not-found")
	String notFound();

	@ClassName("config-content")
	String configContent();

	String load();

	String row();

	String selected();

	String buttons();

	@ClassName("updates-list")
	String updatesList();

	@ClassName("files-list")
	String filesList();

	@ClassName("disabled-text")
	String disabledText();

	String active();

	String label();

	String index();

	String name();

	String plus();

	@ClassName("files-list-buttons")
	String filesListButtons();

	@ClassName("tag-found-panel")
	String tagFoundPanel();

	String clearfix();

	String config();

	String status();

	String dbmValue();

	String interval();

	String measure();

	@ClassName("config-area")
	String configArea();

	@ClassName("log-config")
	String logConfig();

	@ClassName("install-updates")
	String installUpdates();

	@ClassName("observe-button")
	String observeButton();

	@ClassName("inventory-button")
	String inventoryButton();

	String toggleList();

	String smallButton();

	@ClassName("io-panel")
	String ioPanel();

	@ClassName("inventory-panel")
	String inventoryPanel();

	// For ETB
	String addButton();

	String removeButton();

	String runButton();

	String transponderList();

	String commonLabel();

	String configTextboxShort();

	/* ETB Sensor App */
	@ClassName("webui-TextBox")
	String webuiTextBox();

	@ClassName("webui-ListBox")
	String webuiListBox();

	/**
	 * Add this style to Labels which are children in {@link CustomTable}
	 * 
	 * @return webui-CustomTable-Label
	 */
	@ClassName("webui-CustomTable-Label")
	String webuiCustomTableLabel();

	/**
	 * Add this style to TextBoxes which are children in {@link CustomTable}
	 * 
	 * @return webui-CustomTable-TextBox
	 */
	@ClassName("webui-CustomTable-TextBox")
	String webuiCustomTableTextBox();

	/**
	 * Add this style to ListBoxes which are children in {@link CustomTable}
	 * 
	 * @return webui-CustomTable-ListBox
	 */
	@ClassName("webui-CustomTable-ListBox")
	String webuiCustomTableListBox();

	@ClassName("webui-CustomTable-ListBox-noArrow")
	String webuiCustomTableListBoxnoArrow();

	/**
	 * The style for {@link CustomTable}
	 * 
	 * @return webui-CustomTable
	 */
	@ClassName("webui-CustomTable")
	String webuiCustomTable();

	@ClassName("webui-CustomTable-MoveUp")
	String webuiCustomTableUp();

	@ClassName("webui-CustomTable-MoveDown")
	String webuiCustomTableDown();

	@ClassName("info-html-background")
	String infoHtmlBackground();

	@ClassName("info-html-close")
	String infoHtmlClose();

	@ClassName("info-html-dialog")
	String infoHtmlDialog();

	@ClassName("info-html-iframe")
	String infoHtmlIframe();

	@ClassName("webui-Apply-Button")
	String webuiApplyButton();

	// d3 lib css
	String graph();

	String axis();

	String group();

	String graphLabel();

	String legendLabel();

	@ClassName("subscriber-area")
	String subscriberArea();

	@ClassName("clear-button")
	String clearButton();

	@ClassName("export-button")
	String exportButton();

	String errorImg();

	String subscriber();

	String subscriberList();

}
