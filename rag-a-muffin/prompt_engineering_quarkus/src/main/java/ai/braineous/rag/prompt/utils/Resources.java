package ai.braineous.rag.prompt.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class Resources {

    public static String getResource(String resource) throws IOException{
        String resourceStr = null;

        InputStream inputStream = Thread.currentThread().getContextClassLoader().
        getResourceAsStream(resource);

        resourceStr = IOUtils.toString(inputStream);

        return resourceStr;
    }
}
