package org.iish.slideshow.service;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.springframework.util.StringUtils;

import java.util.*;

public class RecordExtractor {
    private static final int MAX_WORDS_NOTE = 50;

    private Record record;

    public RecordExtractor(Record record) {
        this.record = record;
    }

    public String getImageBarcode() {
        return extractFirst("852", 'p');
    }

    public Map<String, List<String>> getMetadata() {
        Map<String, List<String>> metadata = new LinkedHashMap<>();

        extractAuthors(metadata);
        extractTitle(metadata);
        extractYear(metadata);
        extractSubjectPersons(metadata);
        extractSubjectCorporations(metadata);
        extractSubjectLocations(metadata);
        extractNote(metadata);

        return metadata;
    }

    private void extractAuthors(Map<String, List<String>> metadata) {
        extractAuthors(metadata, "100", "Author(s)");
        //extractAuthors(metadata, "700", "Other author(s)");
    }

    private void extractTitle(Map<String, List<String>> metadata) {
        String title = extractFirst("245", 'a', 'b');
        if (title != null) {
            if (title.endsWith("/")) {
                title = title.substring(0, title.length() - 1).trim();
            }

            if (title.startsWith("[") && title.endsWith("]")) {
                return;
            }

            metadata.put("Title", Arrays.asList(title));
        }
    }

    private void extractYear(Map<String, List<String>> metadata) {
        String year = extractFirst("260", 'c');
        if (year != null) {
            metadata.put("Year", Arrays.asList(year));
        }
    }

    private void extractSubjectPersons(Map<String, List<String>> metadata) {
        List<String> subjectPersons = extract("600", 'a');
        if (!subjectPersons.isEmpty()) {
            metadata.put("Subject person(s)", subjectPersons);
        }
    }

    private void extractSubjectCorporations(Map<String, List<String>> metadata) {
        List<String> subjectCorporations = extract("610", 'a');
        if (!subjectCorporations.isEmpty()) {
            metadata.put("Subject corporation(s)", subjectCorporations);
        }
    }

    private void extractSubjectLocations(Map<String, List<String>> metadata) {
        List<String> subjectLocations = extract("651", 'a');
        if (!subjectLocations.isEmpty()) {
            metadata.put("Subject location(s)", subjectLocations);
        }
    }

    private void extractNote(Map<String, List<String>> metadata) {
        DataField dataField = (DataField) record.getVariableField("500");
        if (dataField != null) {
            String note = extractSubfields(dataField, false);
            if (note != null) {
                StringTokenizer st = new StringTokenizer(note, " ");
                if (st.countTokens() <= MAX_WORDS_NOTE) {
                    metadata.put("Note", Arrays.asList(note));
                }
            }
        }
    }

    /*private void extractOrganizations(Map<String, List<String>> metadata) {
        extractAuthors(metadata, "110", "Organization(s)");
        extractAuthors(metadata, "710", "Other organization(s)");
    }

    private void extractCongresses(Map<String, List<String>> metadata) {
        extractAuthors(metadata, "111", "Congress(es)");
        extractAuthors(metadata, "711", "Other congress(es)");
    }

    private void extractSubjects(Map<String, List<String>> metadata) {
        List<String> subjects = extract("650", 'a');
        if (!subjects.isEmpty()) {
            metadata.put("Subject(s)", subjects);
        }
    }
    */

    private void extractAuthors(Map<String, List<String>> metadata, String tag, String defaultLabel) {
        for (VariableField variableField : record.getVariableFields(tag)) {
            DataField dataField = (DataField) variableField;
            String value = extractSubfields(dataField, true, 'a');
            if (value != null) {
                String label = extractSubfields(dataField, true, 'e');
                if (label == null) {
                    label = defaultLabel;
                }
                label = StringUtils.capitalize(label);

                if (!metadata.containsKey(label)) {
                    metadata.put(label, new ArrayList<String>());
                }

                List<String> values = metadata.get(label);
                values.add(value);
                metadata.put(label, values);
            }
        }
    }

    private List<String> extract(String tag, char... subfieldChars) {
        List<String> values = new ArrayList<>();

        for (VariableField variableField : record.getVariableFields(tag)) {
            DataField dataField = (DataField) variableField;
            String value = extractSubfields(dataField, true, subfieldChars);
            if (value != null) {
                values.add(value);
            }
        }

        return values;
    }

    private String extractFirst(String tag, char... subfieldChars) {
        List<String> all = extract(tag, subfieldChars);
        if (!all.isEmpty()) {
            return all.get(0);
        }
        return null;
    }

    private String extractSubfields(DataField dataField, boolean cleanValue, char... subfieldChars) {
        StringBuilder sb = new StringBuilder();
        List<Subfield> subfields = (subfieldChars.length == 0)
                ? dataField.getSubfields() : new ArrayList<Subfield>();

        for (char subfieldChar : subfieldChars) {
            Subfield subfield = dataField.getSubfield(subfieldChar);
            if (subfield != null) {
                subfields.add(subfield);
            }
        }

        for (Subfield subfield : subfields) {
            String value = subfield.getData().trim();
            if (cleanValue && (value.endsWith(".") || value.endsWith(","))) {
                value = value.substring(0, value.length() - 1).trim();
            }

            sb.append(" ");
            sb.append(value);
        }

        String value = sb.toString().trim();
        if (value.length() > 0) {
            return value;
        }
        return null;
    }
}
