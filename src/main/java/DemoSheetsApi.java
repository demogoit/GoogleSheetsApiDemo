import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class DemoSheetsApi {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String APPLICATION_PROPERTIES_PATH = "/application.properties";

    private static Properties appProps;

    private static HttpRequestInitializer getCredentials() throws IOException {
        // Load client secrets.
        InputStream in = DemoSheetsApi.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Lists.newArrayList(SCOPES));

        return new HttpCredentialsAdapter(credentials);
    }

    private static Properties getProperties() throws IOException {
        if (appProps != null) {
            return appProps;
        }

        // Load app properties.
        InputStream in = DemoSheetsApi.class.getResourceAsStream(APPLICATION_PROPERTIES_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + APPLICATION_PROPERTIES_PATH);
        }


        appProps = new Properties();
        appProps.load(in);

        return appProps;
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1sQ2VAblsYmJO6HE_JA7Rf958s2FjTQO57jnVMWOBTq8/edit
     */
    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        final String spreadsheetId = getProperties().getProperty("spreadsheet_id");
        final String range = getProperties().getProperty("cell_range");

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet sheetMetadata = service.spreadsheets().get(spreadsheetId).execute();

        List<Sheet> sheets = sheetMetadata.getSheets();

        sheets.forEach(sheet -> System.out.println(((SheetProperties) (sheet.get("properties"))).get("title")));

        String range1 = (String) ((SheetProperties) (sheets.get(0).get("properties"))).get("title");

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range1 + range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                if (row.isEmpty()) {
                    continue;
                }
                System.out.printf("%s, %s, %s\n", row.get(0), row.get(1), row.get(2));
            }
        }
    }

}
