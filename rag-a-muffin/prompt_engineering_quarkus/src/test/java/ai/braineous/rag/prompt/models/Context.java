package ai.braineous.rag.prompt.models;

import java.io.Serializable;

public class Context implements Serializable{

        private String role;

        private QueryRelationships queryRelationships;

        private String audience;

        private String tone;

        private String referenceData;

        public Context(){

        }

        //-----------------------------------------------------------
        //TODO: accessors, utilities, json generation once the model 
        //is lifted upwards where necessary from String -> Object
        public String getRole() {
            return role;
        }

        public QueryRelationships getQueryRelationships() {
            return queryRelationships;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setQueryRelationships(QueryRelationships queryRelationships) {
            this.queryRelationships = queryRelationships;
        }
        //----------------------------------------------------------
}
