package com.nalexand.fx_utils.message;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nalexand.fx_utils.message.FXMessage.FIX_DELIMITER;
import static com.nalexand.fx_utils.message.FXMessage.USER_DELIMITER;

abstract class FXMessagePart {

    private Map<String, FXMessageField> fields = new HashMap<>();
    private List<FXMessageField> ordered = new LinkedList<>();

    protected FXMessagePart() {
        createFields();
    }

    protected abstract void createFields();

    protected String toFixString() {
        return format(FIX_DELIMITER, FXMessageField::fixFormat);
    }

    protected String toUserString() {
        return format(USER_DELIMITER, FXMessageField::userFormat);
    }

    protected void addField(FXMessageField.Key key) {
        FXMessageField field = new FXMessageField(key);
        fields.put(key.key, field);
        ordered.add(field);
    }

    protected String getValue(FXMessageField.Key key) {
        return fields.get(key.key).value;
    }

    protected void setValue(FXMessageField.Key key, String value) {
        FXMessageField field = fields.get(key.key);
        if (field != null) {
            field.value = value;
        }
    }

    private String format(String delimiter, Function<FXMessageField, String> mapper) {
        return ordered
                .stream()
                .filter(field -> field.value != null)
                .map(mapper)
                .collect(Collectors.joining(delimiter));
    }
}
