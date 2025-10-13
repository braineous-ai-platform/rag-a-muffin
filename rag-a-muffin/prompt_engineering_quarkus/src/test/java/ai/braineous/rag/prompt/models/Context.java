package ai.braineous.rag.prompt.models;

public class Context {

        private String role;

        private QueryRelationships queryRelationships;

        private Audience audience;

        private Tone tone;

        private ReferenceData referenceData;

        public Context(){

        }

        //-----------------------------------------------------------
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

        public Audience getAudience() {
            return audience;
        }

        public void setAudience(Audience audience) {
            this.audience = audience;
        }

        public Tone getTone() {
            return tone;
        }

        public ReferenceData getReferenceData() {
            return referenceData;
        }

        public void setTone(Tone tone) {
            this.tone = tone;
        }

        public void setReferenceData(ReferenceData referenceData) {
            this.referenceData = referenceData;
        }
        //----------------------------------------------------------
}
