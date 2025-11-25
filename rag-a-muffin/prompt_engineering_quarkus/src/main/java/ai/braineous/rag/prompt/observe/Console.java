package ai.braineous.rag.prompt.observe;

public class Console {

    public static void log(String phase, Object obj){
        System.out.println("____" + phase + "____");
        if(obj != null){
            System.out.println(obj);
        }

        
    }
}
