package ai.braineous.rag.prompt.models;

import java.io.Serializable;

public class Context implements Serializable{

        private String role;

        private String rules;

        private String facts;

        private String content;

        private String audience;

        private String tone;

        private String referenceData;

        public Context(){

        }

        //-----------------------------------------------------------
        //TODO: accessors, utilities, json generation once the model 
        //is lifted upwards where necessary from String -> Object

        //----------------------------------------------------------
}
