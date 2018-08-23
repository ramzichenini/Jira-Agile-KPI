package KPIsRest;

import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/estime")
@Named
public class PlanifieEstime {

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
    public PlanifieEstime(JiraAuthenticationContext authenticationContext,
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
    public Response getProjects(@Context HttpServletRequest request) {


        // la configuration et pret mais n'est pas encore ajouté  ici
        // normalment on recoit le "idBoard" par QueryParam méthode GET
        int idBoard=1;
        JSONArray parentJsonArray = new JSONArray();


        try {


            //cal url api et recoit la reponse en String
            URL url = new URL ("http://localhost:8080/rest/agile/1.0/board/"+idBoard+"/sprint");
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

            for( int i=0; i< size; i ++  )

            {
                JSONObject issues_I = (JSONObject) values.get(i);
                Object name = issues_I.get("name");
                Object id  = issues_I.get("id");
                String nameS = name.toString();
                String idS = id.toString();


                //appel au url http://localhost:8080/rest/greenhopper/latest/rapid/charts/sprintreport?rapidViewId=1&sprintId=1

                URL url2 = new URL("http://localhost:8080/rest/greenhopper/latest/rapid/charts/sprintreport?rapidViewId="+ idBoard+"&sprintId="+idS);
                //  String encoding = Base64.getEncoder().encodeToString(("test1:test1").getBytes(‌"UTF‌​-8"​));
                String encoding2 = new String(org.apache.commons.codec.binary.Base64.encodeBase64(("ramzi:admin").getBytes()));

                HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                connection2.setRequestMethod("GET");
                connection2.setDoOutput(true);
                connection2.setRequestProperty  ("Authorization", "Basic " + encoding2);
                InputStream content2 = (InputStream)connection2.getInputStream();
                BufferedReader in2   =
                        new BufferedReader (new InputStreamReader(content2));
                String line2;
                String result2 = "";
                while ((line2 = in2.readLine()) != null)
                {

                    result2=result2 + line2;
                }

                JSONParser parse2 = new JSONParser();

                //Type caste the parsed json data in json object
                JSONObject jobj2 = (JSONObject) parse2.parse(result2);

                JSONObject contents = (JSONObject) jobj2.get("contents");
                JSONArray completedIssues = (JSONArray) contents.get("completedIssues");
                JSONArray issuesNotCompletedInCurrentSprint = (JSONArray) contents.get("issuesNotCompletedInCurrentSprint");


                int realise = completedIssues.size();
                int nonReal = issuesNotCompletedInCurrentSprint.size();
                int estime = realise+nonReal;
                JSONArray childJsonArray = new JSONArray();
                childJsonArray.add(name);
                childJsonArray.add(estime);
                childJsonArray.add(realise);
                parentJsonArray.add(childJsonArray);


            }

        }

        catch (Exception e) {
            e.printStackTrace();
        }



        return Response.ok(parentJsonArray).build();
    }






}
