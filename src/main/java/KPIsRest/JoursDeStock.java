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
@Path("/jstock")
@Named
public class JoursDeStock {

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
    public JoursDeStock(JiraAuthenticationContext authenticationContext,
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
    public Response getProjects(@Context HttpServletRequest request,
                                @QueryParam("ProjectID") String ProjectID,
                                @QueryParam("BacklogGroupe") String BacklogGroupe,
                                @QueryParam("TimeUnit") String TimeUnit

    ) {

        JSONArray parentJsonArray = new JSONArray();
        // String ProjectID="project-10000";
        // String BacklogGroupe="ProjectBG";
        //AllBG
        //ProjectBG
        //SprintCourantBG
        //String TimeUnit="hour";
        //min
        //hour
        //day

        try
        {

            // la valeur recue dans le ProjectID est par exemple "Porject-10001" , donc la ligne suivant sert à extraire
            //seulement l'ID
            int value = Integer.parseInt(ProjectID.replaceAll("[^0-9]", ""));


            // call url api et recoit la reponse en String
            URL url = new URL ("http://localhost:8080/rest/api/2/search");
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

            //**********jobj contient la réponse en json ****************//
            JSONArray issues = (JSONArray) jobj.get("issues");
            int size = issues.size();

            String[] tabS = new String[size];
            int[] tabV = new int[size];
            int it=0;

            switch (BacklogGroupe) {

                case "AllBG":

                    //******************************
                    //******************************
                    //*******case "AllBG": ********
                    //******************************
                    //******************************
                    //******************************

                    for( int i=0; i< size; i ++  )

                    {
                        JSONObject issues_I = (JSONObject) issues.get(i);
                        JSONObject fields = (JSONObject) issues_I.get("fields");
                        JSONObject status = (JSONObject) fields.get("status");
                        JSONObject statusCategory = (JSONObject) status.get("statusCategory");

                        Object key = statusCategory.get("key");
                        String ok = key.toString();

                        if ( ok.equals("done"))
                        {

                            Object duedate = fields.get("duedate");
                            Object updated = fields.get("updated");
                            Object StoryKey = issues_I.get("key");
                            String SK =StoryKey.toString();
                            int Diff= Diff_in_Days( updated ,duedate, TimeUnit);
                            tabS[it]=SK;
                            tabV[it]=Diff;
                            it++;

                        }
                    }


                    for (int j =0; j<it; j++){
                        JSONArray childJsonArray = new JSONArray();
                        childJsonArray.add(tabS[j]);
                        childJsonArray.add(tabV[j]);

                        parentJsonArray.add(childJsonArray);
                    }

                    System.out.println(parentJsonArray);

                    break;
                case "ProjectBG":


                    //******************************
                    //******************************
                    //*******case "ProjectBG": *****
                    //******************************
                    //******************************
                    //******************************


                    for( int i=0; i< size; i ++  )

                    {
                        JSONObject issues_I = (JSONObject) issues.get(i);
                        JSONObject fields = (JSONObject) issues_I.get("fields");
                        JSONObject status = (JSONObject) fields.get("status");
                        JSONObject project = (JSONObject) fields.get("project");
                        JSONObject statusCategory = (JSONObject) status.get("statusCategory");

                        Object key = statusCategory.get("key");
                        Object idProject = project.get("id");
                        String ok = key.toString();
                        String IDPString = idProject.toString();
                        int IDPInt=Integer.parseInt(IDPString);


                        if (  ( ok.equals("done")) && (IDPInt==value)   )
                        {

                            Object duedate = fields.get("duedate");
                            Object updated = fields.get("updated");
                            Object StoryKey = issues_I.get("key");
                            String SK =StoryKey.toString();
                            int Diff= Diff_in_Days( updated ,duedate, TimeUnit);
                            tabS[it]=SK;
                            tabV[it]=Diff;
                            it++;

                        }
                    }

                    for (int j =0; j<it; j++){
                        JSONArray childJsonArray = new JSONArray();
                        childJsonArray.add(tabS[j]);
                        childJsonArray.add(tabV[j]);

                        parentJsonArray.add(childJsonArray);
                    }

                    System.out.println(parentJsonArray);




                    break;

                case "SprintCourantBG": // pas encore terminé

                    //******************************
                    //******************************
                    //**case "SprintCourantBG": ****
                    //******************************
                    //******************************
                    //******************************

                    for( int i=0; i< size; i ++  )

                    {
                        JSONObject issues_I = (JSONObject) issues.get(i);
                        JSONObject fields = (JSONObject) issues_I.get("fields");
                        JSONObject status = (JSONObject) fields.get("status");
                        JSONObject project = (JSONObject) fields.get("project");
                        JSONObject statusCategory = (JSONObject) status.get("statusCategory");

                        Object key = statusCategory.get("key");
                        Object idProject = project.get("id");
                        String ok = key.toString();
                        String IDPString = idProject.toString();
                        int IDPInt=Integer.parseInt(IDPString);


                        if (  ( ok.equals("done")) && (IDPInt==value)   )
                        {

                            Object duedate = fields.get("duedate");
                            Object updated = fields.get("updated");
                            Object StoryKey = issues_I.get("key");
                            String SK =StoryKey.toString();
                            int Diff= Diff_in_Days( updated ,duedate, TimeUnit);
                            tabS[it]=SK;
                            tabV[it]=Diff;
                            it++;

                        }
                    }

                    for (int j =0; j<it; j++){
                        JSONArray childJsonArray = new JSONArray();
                        childJsonArray.add(tabS[j]);
                        childJsonArray.add(tabV[j]);

                        parentJsonArray.add(childJsonArray);
                    }

                    System.out.println(parentJsonArray);


                    break;
                default:
                    ;
                    break;
            }






        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok(parentJsonArray).build();
    }



    public static int Diff_in_Days(Object d1, Object d2, String unit)
    {
        String input1 = d1.toString();
        String input2 = d2.toString();
        int x=0;
        int diff = 0;

        DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date1 = formatter1.parse(input1);
            Date date2 = formatter2.parse(input2);
            x = (int) ( date2.getTime() - date1.getTime()) ;



            switch (unit) {

                case "min":
                    diff = (int) TimeUnit.MILLISECONDS.toMinutes(x);
                    break;
                case "hour":
                    diff = (int) TimeUnit.MILLISECONDS.toHours(x);
                    break;
                default:
                    diff = (int) TimeUnit.MILLISECONDS.toDays(x);
                    break;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        ;
        if ( diff < 0)
            diff=diff* (-1);
        return diff ;

    }



}
