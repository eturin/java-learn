import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;




class XmlUtils {

    public static Map<String, Long> countAllByTagName(List<XmlFile> files, String tagName) {
        return files.stream()
                .collect(Collectors.groupingBy(XmlFile::getEncoding,
                                               Collectors.filtering( (XmlFile f) -> f.getTags()
                                                                                     .stream()
                                                                                     .map(Tag::getName)
                                                                                     .anyMatch(tagName::equals),
                                                                      Collectors.flatMapping(x -> x.getTags().stream(),
                                                                                             Collectors.counting()))));
    }
}

class Tag {
    private final String name;

    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

class XmlFile {
    private final String id;
    private final String encoding;
    private final List<Tag> tags;

    public XmlFile(String id, String encoding, List<Tag> tags) {
        this.id = id;
        this.encoding = encoding;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public String getEncoding() {
        return encoding;
    }
}












class UserActivityMonitoring {

    public static Map<String, Long> getUrlToNumberOfVisited(List<LogEntry> logs) {
        return logs.stream().collect(Collectors.groupingBy(LogEntry::getUrl, Collectors.counting()));
    }
}

class LogEntry {
    private final Date created;
    private final String login;
    private final String url;

    public LogEntry(Date created, String login, String url) {
        this.created = created;
        this.login = login;
        this.url = url;
    }

    public Date getCreated() {
        return created;
    }

    public String getLogin() {
        return login;
    }

    public String getUrl() {
        return url;
    }
}

class UserActivityMonitoringDemo {

    public static void main(String[] args) {
        List<XmlFile> xmlFiles = List.of(
                new XmlFile("1", "UTF-8", List.of(new Tag("function"), new Tag("load"))),
                new XmlFile("2", "UTF-8", List.of(new Tag("table"), new Tag("main"))),
                new XmlFile("3", "ASCII", List.of(new Tag("row"), new Tag("column"))),
                new XmlFile("4", "ASCII", List.of(new Tag("sheet"), new Tag("row"))),
                new XmlFile("5", "ASCII", List.of(new Tag("sheet"), new Tag("column"), new Tag("row")))
        );

        System.out.println(XmlUtils.countAllByTagName(xmlFiles, "sheet"));
    }

}