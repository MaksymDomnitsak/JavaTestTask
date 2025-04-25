import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> inMemoryCollection = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()){
            document.setId(UUID.randomUUID().toString());
        }
        Optional<Document> existing = findById(document.getId());
        if (existing.isPresent()) {
            Document docFromStorage = existing.get();
            document.setCreated(docFromStorage.getCreated());
            inMemoryCollection.remove(docFromStorage);
        } else {
            if (document.getCreated() == null) {
                document.setCreated(Instant.now());
            }
        }
        inMemoryCollection.add(document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null || isAllRequestFieldsNullOrEmpty(request)) {
            return Collections.emptyList();
        }
        return inMemoryCollection.stream()
                .filter(doc -> isTitleMatching(doc, request))
                .filter(doc -> isContentMatching(doc, request))
                .filter(doc -> isAuthorMatching(doc, request))
                .filter(doc -> isCreatedDateMatching(doc, request))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return inMemoryCollection.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    private boolean isTitleMatching(Document document, SearchRequest request) {
        if (isNullOrEmpty(request.getTitlePrefixes())) {
            return true;
        }
        return request.getTitlePrefixes().stream()
                .anyMatch(prefix -> document.getTitle() != null
                        && document.getTitle().startsWith(prefix));
    }

    private boolean isContentMatching(Document document, SearchRequest request) {
        if (isNullOrEmpty(request.getContainsContents())) {
            return true;
        }
        return request.getContainsContents().stream()
                .anyMatch(substr -> document.getContent() != null
                        && document.getContent().contains(substr));
    }

    private boolean isAuthorMatching(Document document, SearchRequest request) {
        if (isNullOrEmpty(request.getAuthorIds())) {
            return true;
        }
        return document.getAuthor() != null
                && request.getAuthorIds().contains(document.getAuthor().getId());
    }

    private boolean isCreatedDateMatching(Document document, SearchRequest request) {
        if (request.getCreatedFrom() != null
                && document.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }
        if (request.getCreatedTo() != null
                && document.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }
        return true;
    }

    private boolean isAllRequestFieldsNullOrEmpty(SearchRequest request) {
        return (isNullOrEmpty(request.getTitlePrefixes()))
                && (isNullOrEmpty(request.getContainsContents()))
                && (isNullOrEmpty(request.getAuthorIds()))
                && request.getCreatedFrom() == null && request.getCreatedTo() == null;
    }

    public <T> boolean isNullOrEmpty(List<T> list){
        return list == null || list.isEmpty();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
