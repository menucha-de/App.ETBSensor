package havis.custom.harting.etb.sensor.rest;

import havis.custom.harting.etb.sensor.App;
import havis.custom.harting.etb.sensor.Configuration;
import havis.custom.harting.etb.sensor.ConfigurationManager;
import havis.custom.harting.etb.sensor.ConfigurationManagerException;
import havis.custom.harting.etb.sensor.Connector;
import havis.custom.harting.etb.sensor.HistoryEntry;
import havis.custom.harting.etb.sensor.HistoryManager;
import havis.custom.harting.etb.sensor.HistoryManagerException;
import havis.custom.harting.etb.sensor.URI;
import havis.custom.harting.etb.sensor.Validator;
import havis.transport.Subscriber;
import havis.transport.ValidationException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.script.ScriptEngine;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("webui/harting/etb/sensor")
public class ETBSensorService {
	private HistoryManager historyManager;
	private ConfigurationManager configurationManager;
	private ScriptEngine engine;
	private App app;

	public ETBSensorService(HistoryManager historyManager, ConfigurationManager configurationManager, ScriptEngine engine, App app) {
		this.historyManager = historyManager;
		this.configurationManager = configurationManager;
		this.engine = engine;
		this.app = app;
	}

	@PermitAll
	@GET
	@Path("configuration")
	@Produces({ MediaType.APPLICATION_JSON })
	public Configuration getConfiguration() throws HistoryManagerException {
		return configurationManager.get();
	}

	@PermitAll
	@PUT
	@Path("subscriber")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void updateSubscriber(Subscriber subscriber) throws HistoryManagerException {
		try {
			app.updateSubscriber(subscriber);
		} catch (Exception e) {
			throw new HistoryManagerException(e);
		}
	}

	@PermitAll
	@POST
	@Path("subscriber/{type}")
	@Consumes({ MediaType.APPLICATION_JSON })
	public String addSubscriber(Subscriber subscriber, @PathParam("type") String type) throws HistoryManagerException {
		try {
			return app.addSubscriber(subscriber, type);
		} catch (Exception e) {
			throw new HistoryManagerException(e);
		}
	}

	@PermitAll
	@GET
	@Path("timestamp")
	@Produces({ MediaType.APPLICATION_JSON })
	public long getTimestamp() {
		return historyManager.getTimestamp();
	}

	@PermitAll
	@GET
	@Path("history/{since}")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<HistoryEntry> getHistorySince(@PathParam("since") long since) throws HistoryManagerException {
		return historyManager.getEntries(since);
	}

	@PermitAll
	@DELETE
	@Path("history")
	@Produces({ MediaType.APPLICATION_JSON })
	public void clearHistory() throws HistoryManagerException {
		historyManager.clear();
	}

	@PermitAll
	@GET
	@Path("parse-uri")
	@Produces({ MediaType.APPLICATION_JSON })
	public URI parseUri(@QueryParam("uri") String uri) {
		return Validator.parseUri(uri);
	}

	@PermitAll
	@GET
	@Path("create-uri")
	@Produces({ MediaType.APPLICATION_JSON })
	public URI createUri(@QueryParam("scheme") String scheme, @QueryParam("host") String host, @QueryParam("port") String port,
			@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("path") String path,
			@QueryParam("query") String query, @QueryParam("topic") String topic, @QueryParam("clientId") String clientId, @QueryParam("qos") String qos) {

		return Validator.createUri(scheme, host, port, username, password, path, query, topic, clientId, qos);
	}

	@PermitAll
	@GET
	@Path("transportTypes")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<String> getTypes() {
		return Connector.getFactory().getTypes();
	}

	@PermitAll
	@POST
	@Path("validate-config")
	@Produces({ MediaType.APPLICATION_JSON })
	public Map<String, String> validateConfiguration(Map<String, String> config) {
		return Validator.validateConfig(engine, config.get("conversion"), config.get("ceiling"), config.get("floor"));
	}

	@PermitAll
	@PUT
	@Path("configuration")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void setConfiguration(Configuration configuration) throws HistoryManagerException {
		try {
			app.updateConfig(configuration);
		} catch (ValidationException | ConfigurationManagerException e) {
			throw new HistoryManagerException(e);
		}
	}

	@PermitAll
	@GET
	@Path("export")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportHistory() throws HistoryManagerException {
		StringWriter writer = new StringWriter();
		try {
			historyManager.marshal(writer);
			String filename = String.format("History_%s.txt", new SimpleDateFormat("yyyyMMdd").format(new Date()));
			byte[] data = writer.toString().getBytes(StandardCharsets.UTF_8);
			return Response.ok(data, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
					.header("Content-Type", "text/plain; charset=utf-8").header("Content-Length", data.length).build();
		} catch (SQLException | IOException e) {
			return Response.serverError().build();
		}
	}

}
