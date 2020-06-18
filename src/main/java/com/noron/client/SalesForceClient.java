package com.noron.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.noron.client.exception.SalesForce;
import java.io.*;
import java.net.URISyntaxException;

import com.noron.client.payload.QueryResponse;

public class SalesForceClient {

    private String sessionId;

    private String salesForceInstance;

    private final String version;

    public SalesForceClient(String username, String password, String securityToken, String domain, String version)
        throws IOException, ParserConfigurationException, SAXException {
        this.version = version;
        String soapUrl = String.format("https://%s.salesforce.com/services/Soap/u/%s", domain, version);
        String clientId = "RestForce";
        String loginSoapRequestBody = String.format("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
            "            <env:Envelope\n" +
            "                    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "                    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                    xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "                    xmlns:urn=\"urn:partner.soap.sforce.com\">\n" +
            "                <env:Header>\n" +
            "                    <urn:CallOptions>\n" +
            "                        <urn:client>%s</urn:client>\n" +
            "                        <urn:defaultNamespace>sf</urn:defaultNamespace>\n" +
            "                    </urn:CallOptions>\n" +
            "                </env:Header>\n" +
            "                <env:Body>\n" +
            "                    <n1:login xmlns:n1=\"urn:partner.soap.sforce.com\">\n" +
            "                        <n1:username>%s</n1:username>\n" +
            "                        <n1:password>%s%s</n1:password>\n" +
            "                    </n1:login>\n" +
            "                </env:Body>\n" +
            "            </env:Envelope>", clientId, username, password, securityToken);
        HttpPost httpPost = new HttpPost(soapUrl);
        httpPost.addHeader("content-type", "text/xml");
        httpPost.addHeader("charset", "UTF-8");
        httpPost.addHeader("SOAPAction", "login");
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        httpPost.setEntity(new StringEntity(loginSoapRequestBody));
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(httpPost, httpContext);
        validateResponse(response);
        InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String xmlResponse = bufferedReader.readLine();
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(
            xmlResponse.getBytes("UTF-8"));
        Document doc = builder.parse(input);
        sessionId = doc.getElementsByTagName("sessionId").item(0).getTextContent();
        String serverUrl = doc.getElementsByTagName("serverUrl").item(0).getTextContent();
        salesForceInstance = serverUrl.replace("http://", "").replace("https://", "").split("/")[0].replace("-api", "");
    }

    public QueryResponse query(String query) throws URISyntaxException, IOException {
        String url = "https://" + salesForceInstance + "/services/data/v" + version + "/query";
        URIBuilder builder = new URIBuilder(url);
        builder.setParameter("q", query);
        HttpGet httpGet = new HttpGet(builder.build());
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Bearer " + sessionId);
        httpGet.setHeader("X-PrettyPrint", "1");
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(httpGet, httpContext);
        validateResponse(response);
        InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(bufferedReader, QueryResponse.class);
    }

    private void validateResponse(HttpResponse response) throws IOException {
        if (response == null) {
            throw new SalesForce("Response is empty.");
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String errorResponse;
            StringBuilder message = new StringBuilder();
            while ((errorResponse = bufferedReader.readLine()) != null) {
                message.append(errorResponse);
            }
            throw new SalesForce(message.toString());
        }
    }
}
