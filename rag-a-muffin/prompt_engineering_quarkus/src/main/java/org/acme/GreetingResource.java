package org.acme;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import com.google.gson.JsonObject;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<String> hello() {
        JsonObject json = new JsonObject();
        json.addProperty("hello", "world");
        return RestResponse.status(Status.OK, json.toString());
    }

    /*@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<String> prompt(String input){
         JsonObject json = new JsonObject();

        //TODO: process_prompt
        System.out.println("____input____");
        System.out.println(input);

        json.addProperty("prompt", input);
        return RestResponse.status(Status.OK, json.toString());
    }*/
}
