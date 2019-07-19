package com.starwars.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class PlanetDAO {

	private MongoClient mongoClient = new MongoClient("localhost", 27017);
	private MongoDatabase database = mongoClient.getDatabase("starwars");
	private MongoCollection<Document> collection = database.getCollection("planets");

	public Document insert(Document planet) {
		Document document = new Document();
		document.put("name", planet.get("name"));
		document.put("climate", planet.get("climate"));
		document.put("terrain", planet.get("terrain"));
		document.put("appearances", planet.get("appearances"));

		collection.insertOne(document);

		document.put("_id", document.get("_id").toString());

		return document;
	}

	public List<Document> list() {
		ArrayList<Document> documents = collection.find().into(new ArrayList<Document>());
		return _setStringId(documents);
	}

	public List<Document> getByName(String name) {
		ArrayList<Document> documents = collection
				.find(Filters.eq("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE)))
				.into(new ArrayList<Document>());
		return _setStringId(documents);
	}

	public Document getById(String id) {
		try {
			Document document = collection.find(Filters.eq("_id", new ObjectId(id))).first();

			if (document != null) {
				document.put("_id", document.get("_id").toString());
			}

			return document;
		} catch (IllegalArgumentException e) {
		}

		return null;

	}

	public Document delete(String id) {
		try {
			Document document = collection.findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

			if (document != null) {
				document.put("_id", document.get("_id").toString());
			}

			return document;
		} catch (IllegalArgumentException e) {
		}

		return null;
	}

	private List<Document> _setStringId(List<Document> documents) {
		documents.parallelStream().forEach(document -> {
			document.put("_id", document.get("_id").toString());
		});
		return documents;
	}

}
