package com.starwars.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;

import com.starwars.dao.PlanetDAO;

import okhttp3.OkHttpClient;
import okhttp3.Request;

@Path("/planet")
public class PlanetService {

	private PlanetDAO dao = new PlanetDAO();

	@GET
	@Path("test")
	@Produces(MediaType.TEXT_PLAIN)
	public Response justTesting() {
		return Response.ok("Hey").build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response insert(Document planet) throws URISyntaxException, IOException, InterruptedException {

		try {
			isPlanetValid(planet);

		} catch (IllegalArgumentException e) {
			Map<String, String> err = new HashMap<String, String>();
			err.put("erro", e.getMessage());

			return Response.status(Status.BAD_REQUEST).entity(err).build();
		}

		planet.put("appearances", getFilmAppearancesCountByName(planet.get("name").toString()));

		return Response.ok(dao.insert(planet)).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response list() {
		return Response.ok(dao.list()).build();
	}

	@GET
	@Path("get_by_name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByName(@PathParam("name") String name) {
		return Response.ok(dao.getByName(name)).build();
	}

	@GET
	@Path("get_by_id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@PathParam("id") String id) {
		Document document = dao.getById(id);
		if (document == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(document).build();
	}

	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("id") String id) {
		Document document = dao.delete(id);
		if (document == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(document).build();
	}

	private void isPlanetValid(Document planet) {
		if (!planet.containsKey("name") || planet.getString("name").isEmpty()) {
			throw new IllegalArgumentException("O campo nome é obrigatório");
		}

		if (!planet.containsKey("climate") || planet.getString("climate").isEmpty()) {
			throw new IllegalArgumentException("O campo clima é obrigatório");
		}

		if (!planet.containsKey("terrain") || planet.getString("terrain").isEmpty()) {
			throw new IllegalArgumentException("O campo terreno é obrigatório");
		}

	}

	@SuppressWarnings("unchecked")
	private int getFilmAppearancesCountByName(String name) throws IOException {

		String url = "https://swapi.co/api/planets/?search=" + name;

		OkHttpClient client = new OkHttpClient().newBuilder()
			    .connectTimeout(60,TimeUnit.SECONDS)
			    .writeTimeout(60,TimeUnit.SECONDS)
			    .readTimeout(60,TimeUnit.SECONDS)
			    .build();
		

		Request request = new Request.Builder().url(url).build();

		okhttp3.Response response = client.newCall(request).execute();

		Document parsed = Document.parse(response.body().string());

		if (parsed.containsKey("results")) {
			List<Document> documents = (List<Document>) parsed.get("results");

			if (!documents.isEmpty()) {
				List<Document> films = (List<Document>) documents.get(0).get("films");
				return films.size();
			}
		}

		return 0;
	}

}
