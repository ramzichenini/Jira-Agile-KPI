package IntermediairesREST;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/getBoadsList")
@Named
public class GetBoardsList {

    @ComponentImport
    private PermissionManager permissionManager;
    @ComponentImport
    private JiraAuthenticationContext authenticationContext;

    /**
     * Constructor.
     * @param authenticationContext context that contains authenticated user
     * @param permissionManager the JIRA object which manages permissions for users and projects
     */
    @Inject
    public GetBoardsList(JiraAuthenticationContext authenticationContext,
                     PermissionManager permissionManager) {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
    }

    /**
     * Returns the list of projects browsable by the user in the specified request.
     *
     * @param request the context-injected {@code HttpServletRequest}
     * @return a {@code Response} with the marshalled projects
     */
    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getList(@Context HttpServletRequest request) {

        JSONArray childJsonArray = new JSONArray();
        JSONObject patentJsonObject = new JSONObject();

        try {



            //cal url api et recoit la reponse en String
            URL url = new URL ("http://localhost:8080/rest/agile/1.0/board");
            //  String encoding = Base64.getEncoder().encodeToString(("test1:test1").getBytes(‌"UTF‌​-8"​));
            String encoding = new String(org.apache.commons.codec.binary.Base64.encodeBase64(("ramzi:admin").getBytes()));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader(content));
            String line;
            String result = "";
            while ((line = in.readLine()) != null)
            {

                result=result + line;
            }

            JSONParser parse = new JSONParser();
            //Type caste the parsed json data in json object
            JSONObject jobj = (JSONObject) parse.parse(result);


            JSONArray values = (JSONArray) jobj.get("values");
            int size = values.size();


            String[] tabS = new String[size];

            int it=0;

            for( int i=0; i< size; i ++  )

            {
                JSONObject issues_I = (JSONObject) values.get(i);
                Object name = issues_I.get("name");
                String ok = name.toString();


                tabS[it]=ok;

                it++;


            }



            for (int j =0; j<it; j++)
            {

                JSONObject ch = new JSONObject();
                ch.put("label",tabS[j]);
                ch.put("value",tabS[j]);
                childJsonArray.add(ch);



            }
            patentJsonObject.put("choix", childJsonArray);

        }

        catch (Exception e) {
            e.printStackTrace();
        }




        return Response.ok(patentJsonObject).build();
    }
}
