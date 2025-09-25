package ai.braineous.rag.prompt.controllers;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import com.google.gson.JsonObject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/prompt")
public class PromptController {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<String> prompt(String input){
         JsonObject json = new JsonObject();

        //TODO: process_prompt
        System.out.println("____input____");
        System.out.println(input);

        json.addProperty("prompt", input);
        return RestResponse.status(Status.OK, json.toString());
    }
}
