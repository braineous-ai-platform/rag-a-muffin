package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ControlTest {

    @Test
    public void constructor_shouldSetKeyAndValue() {
        Control control = new Control("promo_mode", "spring_campaign");

        Console.log("____control.constructor.key____", control.getKey());
        Console.log("____control.constructor.value____", control.getValue());

        assertEquals("promo_mode", control.getKey());
        assertEquals("spring_campaign", control.getValue());
    }

    @Test
    public void setters_shouldUpdateKeyAndValue() {
        Control control = new Control();

        control.setKey("message_style");
        control.setValue("brief");

        Console.log("____control.setters.key____", control.getKey());
        Console.log("____control.setters.value____", control.getValue());

        assertEquals("message_style", control.getKey());
        assertEquals("brief", control.getValue());
    }

    @Test
    public void toJson_shouldReturnJsonWithKeyAndValue() {
        Control control = new Control("decision_scope", "customer_only");

        JsonObject json = control.toJson();

        Console.log("____control.toJson.json____", json.toString());

        assertNotNull(json);
        assertEquals("decision_scope", json.get("key").getAsString());
        assertEquals("customer_only", json.get("value").getAsString());
    }

    @Test
    public void toJson_shouldReturnJsonWithNullKeyAndNullValue() {
        Control control = new Control();

        JsonObject json = control.toJson();

        Console.log("____control.toJson.nulls.json____", json.toString());

        assertNotNull(json);
        assertNotNull(json.get("key"));
        assertNotNull(json.get("value"));
        assertEquals(true, json.get("key").isJsonNull());
        assertEquals(true, json.get("value").isJsonNull());
    }

    @Test
    public void fromJson_shouldReturnControlWithKeyAndValue() {
        JsonObject json = new JsonObject();
        json.addProperty("key", "channel");
        json.addProperty("value", "email");

        Control control = Control.fromJson(json);

        Console.log("____control.fromJson.key____", control.getKey());
        Console.log("____control.fromJson.value____", control.getValue());

        assertNotNull(control);
        assertEquals("channel", control.getKey());
        assertEquals("email", control.getValue());
    }

    @Test
    public void fromJson_shouldReturnControlWithNullFields_whenJsonContainsNulls() {
        JsonObject json = new JsonObject();
        json.add("key", null);
        json.add("value", null);

        Control control = Control.fromJson(json);

        Console.log("____control.fromJson.nulls.key____", String.valueOf(control.getKey()));
        Console.log("____control.fromJson.nulls.value____", String.valueOf(control.getValue()));

        assertNotNull(control);
        assertNull(control.getKey());
        assertNull(control.getValue());
    }

    @Test
    public void fromJson_shouldReturnNull_whenJsonIsNull() {
        Control control = Control.fromJson(null);

        Console.log("____control.fromJson.nullJson.control____", String.valueOf(control));

        assertNull(control);
    }

    @Test
    public void fromJson_shouldReturnControlWithOnlyKey_whenValueIsMissing() {
        JsonObject json = new JsonObject();
        json.addProperty("key", "promo_mode");

        Control control = Control.fromJson(json);

        Console.log("____control.fromJson.onlyKey.key____", control.getKey());
        Console.log("____control.fromJson.onlyKey.value____", String.valueOf(control.getValue()));

        assertNotNull(control);
        assertEquals("promo_mode", control.getKey());
        assertNull(control.getValue());
    }

    @Test
    public void fromJson_shouldReturnControlWithOnlyValue_whenKeyIsMissing() {
        JsonObject json = new JsonObject();
        json.addProperty("value", "strict");

        Control control = Control.fromJson(json);

        Console.log("____control.fromJson.onlyValue.key____", String.valueOf(control.getKey()));
        Console.log("____control.fromJson.onlyValue.value____", control.getValue());

        assertNotNull(control);
        assertNull(control.getKey());
        assertEquals("strict", control.getValue());
    }
}