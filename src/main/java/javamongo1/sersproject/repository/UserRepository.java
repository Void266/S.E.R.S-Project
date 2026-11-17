package javamongo1.sersproject.repository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import javamongo1.sersproject.config.MongoConnection;
import javamongo1.sersproject.model.User;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> counters;

    public UserRepository() {
        MongoDatabase database = MongoConnection.getDatabase();

        this.collection = database.getCollection("users");
        this.counters = database.getCollection("counters");

        initializeDatabase();
    }

    // Constructor for testing with a custom users collection
    public UserRepository(MongoCollection<Document> collection) {
        this.collection = collection;

        MongoDatabase database = MongoConnection.getDatabase();
        this.counters = database.getCollection("counters");

        initializeDatabase();
    }

    // -------- Database Initialization --------
    private void initializeDatabase() {

    // 1. Internal system user ID must be unique.
    collection.createIndex(
            new Document("userId", 1),
            new IndexOptions().unique(true)
    );

    // 2. Check whether the studentId index already has
    //    the correct definition.
    boolean correctStudentIdIndex = false;

    for (Document index : collection.listIndexes()) {

        Document key = index.get("key", Document.class);

        // Find an existing index on studentId.
        if (key != null && key.containsKey("studentId")) {

            Document partialFilter =
                    index.get("partialFilterExpression", Document.class);

            boolean isUnique =
                    Boolean.TRUE.equals(index.getBoolean("unique"));

            boolean hasCorrectPartialFilter =
                    partialFilter != null
                    && partialFilter.containsKey("studentId")
                    && partialFilter.get("studentId") instanceof Document
                    && "string".equals(
                            ((Document) partialFilter.get("studentId"))
                                    .get("$type")
                    );

            // Keep the index if it already has the correct definition.
            if (isUnique && hasCorrectPartialFilter) {

                correctStudentIdIndex = true;

            } else {

                // Remove only the incorrectly configured studentId index.
                collection.dropIndex(index.getString("name"));
            }
        }
    }

    // 3. Create the correct index only when it is missing.
    if (!correctStudentIdIndex) {

        collection.createIndex(
                new Document("studentId", 1),
                new IndexOptions()
                        .name("studentId_1")
                        .unique(true)
                        .partialFilterExpression(
                                new Document(
                                        "studentId",
                                        new Document("$type", "string")
                                )
                        )
        );
    }

    // 4. Find the highest existing numeric userId.
    Document highestUser = collection.find()
            .sort(Sorts.descending("userId"))
            .first();

    int highestId = 0;

    if (highestUser != null
            && highestUser.get("userId") instanceof Number) {

        highestId = ((Number) highestUser.get("userId")).intValue();
    }

    // 5. Create the counter if missing, or raise it when it is behind.
    counters.updateOne(
            Filters.eq("_id", "userId"),
            Updates.max("seq", highestId),
            new UpdateOptions().upsert(true)
    );
}


    // -------- Generate Internal User ID --------
    private int generateUserId() {

        Document counter = counters.findOneAndUpdate(
                Filters.eq("_id", "userId"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        if (counter == null
                || !(counter.get("seq") instanceof Number)) {
            throw new IllegalStateException(
                    "Could not generate a user ID."
            );
        }

        long nextId = ((Number) counter.get("seq")).longValue();

        if (nextId <= 0 || nextId > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Generated user ID is outside the integer range."
            );
        }

        return (int) nextId;
    }

    // -------- Convert MongoDB Document to User --------
    private User fromDocument(Document doc) {

        if (doc == null) {
            return null;
        }

        Object storedStudentId = doc.get("studentId");

        // Supports reading older numeric student IDs.
        // New and updated records will use String.
        String studentId = storedStudentId == null
                ? null
                : storedStudentId.toString();

        User user = new User(
                studentId,
                doc.getString("name"),
                doc.getString("password"),
                doc.getString("role")
        );

        Object storedUserId = doc.get("userId");

        if (storedUserId instanceof Number) {
            user.setId(((Number) storedUserId).intValue());
        }

        return user;
    }

    
    
    
    
    // -------- Create User --------
    // Password must already be hashed by UserService
    public boolean createUser(User user) {

        if (user == null || user.getStudentId() == null || user.getStudentId().isBlank()) {
            return false;
        }

        if (findUserByStudentId(user.getStudentId()) != null) {
            return false;
        }

        try {
            int userId = generateUserId();

            Document doc = new Document("userId", userId)
                    .append("studentId", user.getStudentId())
                    .append("name", user.getName())
                    .append("password", user.getPassword())
                    .append("role", user.getRole());

            collection.insertOne(doc);

            user.setId(userId);

            return true;

        } catch (MongoWriteException e) {
            // Includes duplicate-key errors
            return false;
        }
    }

    // -------- Find by Internal User ID --------
    public User findUserById(int userId) {

        Document doc = collection.find(
                Filters.eq("userId", userId)
        ).first();

        return fromDocument(doc);
    }

    // -------- Find by Physical Student ID --------
    public User findUserByStudentId(String studentId) {

        if (studentId == null || studentId.isBlank()) {
            return null;
        }

        Document doc = collection.find(
                Filters.eq("studentId", studentId.trim())
        ).first();

        // Fallback for older records stored as numbers.
        // Remove this fallback after migrating old records.
        if (doc == null && studentId.trim().matches("[0-9]+")) {
            try {
                long numericStudentId = Long.parseLong(studentId.trim());

                doc = collection.find(
                        Filters.eq("studentId", numericStudentId)
                ).first();

            } catch (NumberFormatException e) {
                // Too large to represent as long.
                // The string lookup above has already been attempted.
            }
        }

        return fromDocument(doc);
    }

    // -------- Find by Name --------
    public User findUserByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        Document doc = collection.find(
                Filters.eq("name", name.trim())
        ).first();

        return fromDocument(doc);
    }

    
    public User findUserByNameIgnoreCase(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        Document doc = collection.find(
                new Document(
                        "name",
                        new Document("$regex", "^"
                                + java.util.regex.Pattern.quote(name.trim())
                                + "$")
                                .append("$options", "i")
                )
        ).first();

        return fromDocument(doc);
    }

    
    
    
    
    // -------- Compatibility Search --------
    // Search by internal user ID or name
    public User findUser(String idOrName) {

        if (idOrName == null || idOrName.isBlank()) {
            return null;
        }

        try {
            int userId = Integer.parseInt(idOrName.trim());

            User user = findUserById(userId);

            if (user != null) {
                return user;
            }

        } catch (NumberFormatException e) {
            // Not an internal integer user ID
        }

        return findUserByName(idOrName.trim());
    }

    // -------- Retrieve All Users --------
    public List<User> findAllUsers() {

        List<User> users = new ArrayList<>();

        for (Document doc : collection.find()) {
            users.add(fromDocument(doc));
        }

        return users;
    }

    // -------- Update User --------
    public boolean updateUser(int userId, User updated) {

        if (updated == null
                || updated.getStudentId() == null
                || updated.getStudentId().isBlank()) {
            return false;
        }

        try {
            Bson filter = Filters.eq("userId", userId);

            Bson updates = Updates.combine(
                    Updates.set(
                            "studentId",
                            updated.getStudentId()
                    ),
                    Updates.set("name", updated.getName()),
                    Updates.set("password", updated.getPassword()),
                    Updates.set("role", updated.getRole())
            );

            UpdateResult result = collection.updateOne(
                    filter,
                    updates
            );

            return result.getMatchedCount() > 0;

        } catch (MongoWriteException e) {
            // For example, duplicate student ID
            return false;
        }
    }

    // -------- Delete User --------
    public boolean deleteUser(int userId) {

        DeleteResult result = collection.deleteOne(
                Filters.eq("userId", userId)
        );

        return result.getDeletedCount() > 0;
    }
}