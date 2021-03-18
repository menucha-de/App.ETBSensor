package havis.app.etb.sensor.ui.client;

import havis.app.etb.sensor.Configuration;
import havis.app.etb.sensor.HistoryEntry;
import havis.app.etb.sensor.rest.async.ETBSensorServiceAsync;
import havis.app.etb.sensor.ui.resourcebundle.AppResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Transition.EventType;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.ease.Easing;
import com.github.gwtd3.api.functions.BooleanDatumFunction;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.scales.LinearScale;
import com.github.gwtd3.api.svg.Axis;
import com.github.gwtd3.api.svg.Axis.Orientation;
import com.github.gwtd3.api.svg.Line.InterpolationMode;
import com.github.gwtd3.api.svg.PathDataGenerator;
import com.github.gwtd3.api.time.TimeScale;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;

public class GraphSectionPresenter implements GraphSectionView.Presenter {

	private static final int INITIALVALUEINTERVAL = 300000; // Get the history
															// initial of 5
															// minutes
	private static final int MAXTAGGROUPS = 10; // Maximum number of tags in
												// field
	private static final int GAPSPAN = 5000; // Determines, how long a tag must
												// be out of sight to avoid
												// interpolation
	private static final int VALUEINTERVAL = 1000;
	private static final int DRAWINTERVAL = 50; // Draw interval
	private static final int LIMIT = 6000; // Amount of values
	private static final int WIDTH = 800; // Graph base width in pixel
	private static final int HEIGHT = 480; // Graph base height in pixel

	private int currentGroup = 0; // Change EPC group
	private int valueInterval = 300000; // Initial 3000000 ms, after first
										// refresh call all 1000 ms

	private Map<String, Long> seenEpcs = new HashMap<String, Long>(); // EPC->lastTimestamp

	private ETBSensorServiceAsync service = GWT
			.create(ETBSensorServiceAsync.class);

	private GraphSectionView view;
	private FlowPanel graph;
	private FlowPanel graphPlaceholder;

	private Map<String, Group> paths = new HashMap<String, GraphSectionPresenter.Group>();

	private List<String> colors = new ArrayList<String>();

	private long start;
	private long timeOffset;
	private Selection ceilingPath;
	private Selection floorPath;

	private PathDataGenerator line;

	private TimeScale x;
	private LinearScale y;

	private Selection svg;
	private Axis xAxis;
	private Axis yAxis;

	private Selection pathsSelection;

	private Timer timer;

	private Configuration configuration;

	private Float maxValue = null;
	private Float minValue = null;

	private float floorValue = 0;
	private float ceilingValue = 0;

	private boolean init = false;

	private Selection axis;

	private static final String DOWNLOAD_LINK = GWT.getHostPageBaseURL()
			+ "rest/webui/etb/sensor/export";

	private class Drawer implements DatumFunction<Void> {
		private static final int VALUESQUOTIENT = 500;

		@Override
		public Void apply(Element context, Value d, int index) {
			final Drawer parent = this;
			// Calculate time
			long now = (long) JsDate.create().getTime() + timeOffset;
			// Draw y-value
			for (Entry<String, Group> path : paths.entrySet()) {
				path.getValue().getPath()
						.attr("d", line.generate(path.getValue().getValues()));

			}
			// Slide x-axis left
			axis.transition().duration(DRAWINTERVAL).ease(Easing.linear())
					.call(xAxis);
			// Shift x-domain
			x.domain(now - DRAWINTERVAL * LIMIT, now);
			// Slide paths left
			pathsSelection.attr("transform", "").transition()
					.duration(DRAWINTERVAL).ease(Easing.linear())
					.each(EventType.END, parent);
			// Remove values, which are not visible anymore
			for (Entry<String, Group> path : paths.entrySet()) {
				Array<HistoryEntry> values = path.getValue().getValues();
				if (values.length() > (DRAWINTERVAL * LIMIT) / VALUESQUOTIENT) {
					values.shiftNumber();
				}
			}
			return null;
		}

	}

	// Tag Group class
	private class Group {
		private Selection path;
		private Array<HistoryEntry> values;
		private List<Long> timestamps = new ArrayList<Long>();
		private boolean visible = true;

		public Group(Selection path) {
			this.path = path;
			values = Array.create();
		}

		public Selection getPath() {
			return path;
		}

		public Array<HistoryEntry> getValues() {
			Array<HistoryEntry> copy = Array.create();
			for (HistoryEntry entry : values.asList()) {
				copy.push(entry);
			}
			return copy;
		}

		public List<Long> getTimestamps() {
			return new ArrayList<Long>(timestamps);
		}

		public void addValue(HistoryEntry entry) {
			long timestamp = entry.getTimestamp();

			if (timestamps.size() > 0 && entry.getValue() != 0.0) {
				// Add value only, if last timestamp<timestamp to add
				if (timestamp > timestamps.get(timestamps.size() - 1)) {
					timestamps.add(timestamp);
					values.push(entry);
				}
			} else {
				// Add first value
				timestamps.add(timestamp);
				values.push(entry);
			}
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

	}

	public GraphSectionPresenter(GraphSectionView view) {
		this.view = view;
		this.view.setPresenter(this);
	}

	@Override
	public void onObserve() {

		graph = view.getGraph();
		graphPlaceholder = view.getPlaceholder();

		// Defining colors for different tag groups
		colors.add("green");
		colors.add("#49006a"/*purple*/);
		colors.add("grey");
		colors.add("orange");
		colors.add("blue");
		colors.add("#7a0177" /*purple-pink*/);
		colors.add("#ffcc00" /*yellow*/);
		colors.add("#74a9cf" /*light blue*/);
		colors.add("#66c2a4" /*light-green*/);
		colors.add("#02818a" /*green-blue*/);

		// Switch view for smoother drawing
		switchView(false);

		// Get configuration from backend
		service.getConfiguration(new MethodCallback<Configuration>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				ErrorViewer.showExceptionResponse(exception);
			}

			@Override
			public void onSuccess(Method method, Configuration config) {
				// Set local configuration to backend configuration
				configuration = config;

				floorValue = configuration.getFloor().getValue();
				minValue = floorValue;
				ceilingValue = configuration.getCeiling().getValue();
				maxValue = ceilingValue;

				// Get backend time
				service.getTimestamp(new MethodCallback<Long>() {
					@Override
					public void onSuccess(Method method, Long now) {
						// Calculate time difference between local time (JS) and
						// backend time
						timeOffset = now - (long) JsDate.create().getTime();
						start = now + timeOffset;

						// Define x-axis scale
						x = D3.time().scale()
								.domain(start - LIMIT * DRAWINTERVAL, start)
								.range(0, WIDTH);

						float offset = (float) ((maxValue - minValue) / 10.0);
						// Define y-axis scale
						y = D3.scale
								.linear()
								.domain(floorValue - offset,
										ceilingValue + offset)
								.range(HEIGHT, 10 /* Correct position of y axis */)
								.nice();

						xAxis = D3.svg().axis().scale(x)
								.orient(Orientation.BOTTOM)
								// .tickValues(new
								// ArrayList<String>())
								// -hide rolling
								// values
								.tickFormat(new DatumFunction<String>() {
									@Override
									public String apply(Element context,
											Value d, int index) {
										// Define time format for xAxis
										return D3.time().format("%H:%M")
												.apply(d.asJsDate());
									}

								}).ticks(5)
								/* Displays only 5 values */.tickPadding(10)
								/* Space between ticks */.tickSize(0) /*
																	 * invisible
																	 * tick
																	 * lines
																	 */;

						yAxis = D3.svg().axis().scale(y)
								.orient(Orientation.RIGHT).tickPadding(10)
								.tickSize(0);

						// Define the linegraph function (how to draw value
						// line)
						line = D3.svg().line()
								.interpolate(InterpolationMode.LINEAR)
								.x(new DatumFunction<Double>() {
									@Override
									public Double apply(Element context,
											Value d, int index) {
										// x values are the timestamps
										return x.apply(
												((HistoryEntry) d.as())
														.getTimestamp())
												.asDouble();
									}
								}).y(new DatumFunction<Double>() {
									@Override
									public Double apply(Element context,
											Value d, int index) {
										// y values are the values at a given
										// time
										return y.apply(
												((HistoryEntry) d.as())
														.getValue()).asDouble();
									}

								}).defined(new BooleanDatumFunction() {
									@Override
									public boolean apply(Element context,
											Value d, int index) {
										// Break line, if value equals 0.0
										if (((HistoryEntry) d.as()).getValue() == 0.0) {
											return false;
										}
										return true;
									}
								});

						// Create svg canvas
						svg = D3.select(graph)
								.append("svg")
								.attr("class",
										AppResources.INSTANCE.css().graph())
								.attr("width", WIDTH + 150)
								.attr("height", HEIGHT + 100)
								.style("margin-left", "3em");

						// Draw x-axis
						axis = svg
								.append("g")
								.attr("id", "xaxis")
								.attr("class",
										AppResources.INSTANCE.css().axis())
								.attr("transform",
										"translate(0," + HEIGHT + ")")
								.call(xAxis);

						// Draw y-axis
						svg.append("g")
								.attr("id", "yaxis")
								.attr("class",
										AppResources.INSTANCE.css().axis()
												+ " "
												+ AppResources.INSTANCE.css()
														.axis())
								.attr("transform",
										"translate(" + WIDTH + "," + 0 + ")")
								.call(yAxis);

						// Draw orientation lines on yAxis
						svg.select("#yaxis").selectAll(".tick").select("line")
								.style("opacity", "0.3")
								.attr("x2", "-" + WIDTH);

						pathsSelection = svg.append("g");
						drawLimits(start, floorValue, ceilingValue);

						// Start drawing
						new Drawer().apply(null, null, 0);

						// Start to collect data from Backend
						timer = new Timer() {
							@Override
							public void run() {
								refresh();
							}
						};
						timer.scheduleRepeating(VALUEINTERVAL);

					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						ErrorViewer.showExceptionResponse(exception);
					}
				});
			}
		});
	}

	@Override
	public void onOpen() {
		if (init) {
			valueInterval = INITIALVALUEINTERVAL;
			// Get time from backend
			service.getTimestamp(new MethodCallback<Long>() {
				@Override
				public void onSuccess(Method method, final Long now) {
					// Calculate offset
					timeOffset = now - (long) JsDate.now();
					// Calculate graph start time
					start = now + timeOffset;
					// Get configuration from backend
					service.getConfiguration(new MethodCallback<Configuration>() {
						@Override
						public void onSuccess(Method method,
								Configuration config) {
							configuration = config;

							floorValue = configuration.getFloor().getValue();
							minValue = floorValue;
							ceilingValue = configuration.getCeiling()
									.getValue();
							maxValue = ceilingValue;

							resizeGraph(start);
						}

						@Override
						public void onFailure(Method method, Throwable exception) {
							ErrorViewer.showExceptionResponse(exception);
						}

					});
				}

				@Override
				public void onFailure(Method method, Throwable exception) {
					ErrorViewer.showExceptionResponse(exception);
				}
			});

			// Restart timer
			timer = new Timer() {
				@Override
				public void run() {
					refresh();
				}
			};
			timer.scheduleRepeating(VALUEINTERVAL);
		}
	}

	@Override
	public void onClose() {
		switchView(false);
		// Cancel current timer
		timer.cancel();
		// Remove limits
		clearLimits();
		// Delete lines on graph and legends
		for (int i = 0; i < paths.size(); i++) {
			svg.select("#group" + i).remove();
			svg.select("#t" + i).remove();
			svg.select("#circle" + i).remove();
		}

		// Delete values
		List<String> deletions = new ArrayList<String>();
		for (String path : paths.keySet()) {
			deletions.add(path);
		}

		for (String path : deletions) {
			paths.remove(path);
		}
		currentGroup = 0;

	}

	@Override
	public void onClearHistory() {
		service.clearHistory(new MethodCallback<Void>() {
			@Override
			public void onSuccess(Method method, Void response) {
				if (init) {
					onClose();
					onOpen();
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				ErrorViewer.showExceptionResponse(exception);
			}
		});
	}

	@Override
	public void onExportCSV() {
		Window.Location.assign(DOWNLOAD_LINK);
	}

	private void switchView(boolean origin) {
		if (origin) {
			graphPlaceholder.setVisible(false);
			graph.setVisible(true);
		} else {
			graphPlaceholder.setVisible(true);
			graph.setVisible(false);
		}
	}

	private void refresh() {
		// Calculate current time
		final long now = (long) JsDate.create().getTime() + timeOffset;

		// Collect data from backend
		service.getHistorySince(now - valueInterval,
				new MethodCallback<List<HistoryEntry>>() {
					@Override
					public void onSuccess(Method method,
							List<HistoryEntry> entries) {
						// Prepare visualization
						List<HistoryEntry> modififedEntries = new ArrayList<HistoryEntry>();
						Long currentTimestamp = null;
						boolean minMaxChanged = false;

						// Search for Transponder in history and min and
						// max value
						for (HistoryEntry entry : entries) {
							if (!seenEpcs.containsKey(entry.getEpc())) {
								seenEpcs.put(entry.getEpc(), null);
							}

							if (minValue > entry.getValue()
									&& entry.getValue() < floorValue) {
								minMaxChanged = true;
								minValue = entry.getValue();
							}

							if (maxValue < entry.getValue()
									&& entry.getValue() > ceilingValue) {
								minMaxChanged = true;
								maxValue = entry.getValue();
							}

						}
						if (minMaxChanged) {
							// Perform dynamic resize
							resizeGraph(now);
						}

						// Fill up missing values
						for (HistoryEntry entry : entries) {
							String epc = entry.getEpc();
							Long lastTimestamp = seenEpcs.get(epc);
							currentTimestamp = entry.getTimestamp();
							if (lastTimestamp != null) {
								// Calculate time past from where tag was seen
								// last
								long diff = currentTimestamp - lastTimestamp;
								// If time span is greater than five seconds, an
								// gap will be inserted every second
								if (diff > GAPSPAN) {
									int count = (int) diff / VALUEINTERVAL;
									long stepTimestamp = currentTimestamp;
									// Add gap value every second until the time
									// tag was seen at last
									for (int i = 0; i < count - 1; i++) {
										stepTimestamp -= VALUEINTERVAL;
										HistoryEntry insertion = new HistoryEntry();
										insertion.setEpc(epc);
										insertion.setTimestamp(stepTimestamp);
										insertion
												.setValue((float) Float.MIN_VALUE);
										modififedEntries.add(insertion);
									}
								}
							}
							modififedEntries.add(entry);
							// Change tag last sighting
							seenEpcs.put(epc, currentTimestamp);

							// Add epc group to current pathes
							String currentEPC = epc;
							Group currentEpcGroup = paths.get(currentEPC);
							// Add new epcGroup
							if (currentEpcGroup == null) {
								if (currentGroup < MAXTAGGROUPS) {
									currentEpcGroup = createTagGroup(currentEPC);
									paths.put(currentEPC, currentEpcGroup);
									currentGroup++;
								}
							}
						}

						// Prepare visualization
						for (HistoryEntry entry : modififedEntries) {
							String currentEPC = entry.getEpc();
							Group currentEpcGroup = paths.get(currentEPC);
							if (currentEpcGroup != null
									&& !currentEpcGroup.getTimestamps()
											.contains(entry.getTimestamp())) {
								// if entry value equals 0.0, change value to
								// min float, cause 0.0 draws a gap
								if (entry.getValue() == 0.0) {
									entry.setValue((float) Float.MIN_VALUE);
								} else if (entry.getValue() == Float.MIN_VALUE) {
									// Draw a gap
									entry.setValue((float) 0.0);
								}
								entry.setValue(Math.round(entry.getValue()));
								currentEpcGroup.addValue(entry);
							}
						}
						valueInterval = VALUEINTERVAL;

						// Switch view to show drawn graph
						switchView(true);
						// Set size of placeholder to current size for smoother
						// animation
						graphPlaceholder.setHeight(graph.getOffsetHeight()
								+ "px");

						// mark view as initialized
						init = true;
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						ErrorViewer.showExceptionResponse(exception);
					}
				});

	}

	private void drawLimits(long start, float floorValue, float ceilingValue) {
		HistoryEntry value = new HistoryEntry();
		if (configuration.getCeiling() != null
				&& configuration.getCeiling().isEnable()) {
			// Drawing floor and ceiling
			ceilingPath = pathsSelection.append("path")
					.attr("class", AppResources.INSTANCE.css().group())
					.style("stroke", "red");
			Array<HistoryEntry> ceiling = Array.create();
			value.setTimestamp(start - (DRAWINTERVAL * LIMIT));
			value.setValue((float) ceilingValue);
			ceiling.push(value);
			value = new HistoryEntry();
			value.setTimestamp(start);
			value.setValue((float) ceilingValue);
			ceiling.push(value);
			// Draw ceiling
			ceilingPath.attr("d", line.generate(ceiling)).attr("id", "ceiling");
			// Add ceiling Label
			svg.append("text").attr("id", "tCeiling").attr("dy", -5)
					.append("textPath").attr("xlink:href", "#ceiling")
					.style("text-anchor", "begin").attr("startOffset", "10")
					.attr("class", AppResources.INSTANCE.css().graphLabel())
					.text("ceiling");
		}

		if (configuration.getFloor() != null
				&& configuration.getFloor().isEnable()) {
			floorPath = pathsSelection.append("path")
					.attr("class", AppResources.INSTANCE.css().group())
					.style("stroke", "blue");
			Array<HistoryEntry> floor = Array.create();
			value = new HistoryEntry();
			value.setTimestamp(start - (DRAWINTERVAL * LIMIT));
			value.setValue((float) (floorValue == 0.0 ? Float.MIN_VALUE
					: floorValue));
			floor.push(value);
			value = new HistoryEntry();
			value.setTimestamp(start);
			value.setValue((float) (floorValue == 0.0 ? Float.MIN_VALUE
					: floorValue));
			floor.push(value);
			// Draw floor
			floorPath.attr("d", line.generate(floor)).attr("id", "floor");
			// Add floor Label
			svg.append("text").attr("id", "tFloor").attr("dy", +16)
					.append("textPath").attr("xlink:href", "#floor")
					.style("text-anchor", "begin").attr("startOffset", "10")
					.attr("class", AppResources.INSTANCE.css().graphLabel())
					.text("floor");
		}
	}

	private void resizeGraph(final Long now) {
		float offset = (float) ((maxValue - minValue) / 10.0);
		y.domain(minValue - offset, maxValue + offset).nice();
		svg.select("#yaxis")
				.attr("class", AppResources.INSTANCE.css().axis())
				.attr("transform", "translate(" + WIDTH + "," + 0 + ")")
				.call(yAxis);
		svg.select("#yaxis").selectAll(".tick").select("line")
				.style("opacity", "0.3").attr("x2", "-" + WIDTH);
		// Remove limits
		clearLimits();
		drawLimits(now, floorValue, ceilingValue);
	}

	private void clearLimits() {
		svg.select("#ceiling").remove();
		svg.select("#floor").remove();
		svg.select("#tCeiling").remove();
		svg.select("#tFloor").remove();
	}

	private Group createTagGroup(String id) {
		Group currentEpcGroup;
		// Define line and its color
		Selection style = pathsSelection.append("path")
				.attr("class", AppResources.INSTANCE.css().group())
				.style("stroke", colors.get(currentGroup))
				.attr("id", "group" + currentGroup);

		currentEpcGroup = new Group(style);

		final Group tmpGroup = currentEpcGroup;
		final int tmpGroupId = currentGroup;

		// Resize svg canvas for each group added
		svg.attr("height", HEIGHT + 50 + 15 + (currentGroup * 20) + 50);

		// Legend element
		svg.append("circle").attr("id", "circle" + currentGroup)
				.attr("cx", 0 + 50)
				.attr("cy", HEIGHT + 50 + 15 + (currentGroup * 20))
				.attr("r", "4").style("fill", colors.get(currentGroup));
		// Legend text with click handlers to make line
		// invisible
		svg.append("text").attr("id", "t" + currentGroup)
				.attr("y", HEIGHT + 50 + 19 + (currentGroup * 20))
				.attr("x", 0 + 60)
				.attr("class", AppResources.INSTANCE.css().legendLabel())
				.on("click", new DatumFunction<Void>() {
					@Override
					public Void apply(Element context, Value d, int index) {
						if (tmpGroup.isVisible()) {
							svg.select("#group" + tmpGroupId).style("opacity",
									"0");
							svg.select("#t" + tmpGroupId).style("opacity",
									"0.5");
							tmpGroup.setVisible(false);
						} else {
							svg.select("#" + "group" + tmpGroupId).style(
									"opacity", "1");
							svg.select("#t" + tmpGroupId).style("opacity", "1");
							tmpGroup.setVisible(true);
						}
						return null;
					}
				}).text(id);
		return currentEpcGroup;
	}

}
