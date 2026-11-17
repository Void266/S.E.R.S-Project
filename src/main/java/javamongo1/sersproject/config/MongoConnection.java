package javamongo1.sersproject.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * M2 - MongoDB connection handler.
 * Database: SERS_DB @ mongodb://localhost:27017
 *
 * Repositories should use this class instead of creating
 * their own connection. Example:
 *   MongoCollection&lt;Document&gt; users = MongoConnection.getCollection("users");
 */
public class MongoConnection {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "SERS_DB";

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private MongoConnection() {
        // prevent instantiation
    }

    private static synchronized void init() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
        }
    }

    public static MongoDatabase getDatabase() {
        if (database == null) {
            init();
        }
        return database;
    }

    public static MongoCollection<Document> getCollection(String collectionName) {
        return getDatabase().getCollection(collectionName);
    }

    public static String getDatabaseName() {
        return DATABASE_NAME;
    }

    public static String getConnectionString() {
        return CONNECTION_STRING;
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
        }
    }
}
