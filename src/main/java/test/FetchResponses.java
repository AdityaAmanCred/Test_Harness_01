package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FetchStageResponses {
    private String stageId;

    private String pdfLocation;

    public FetchStageResponses(String stageId, String pdfLocation) {
        this.stageId = stageId;
        this.pdfLocation = pdfLocation;
    }

    public void fetch(String pdfFileName) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("template_id", stageId)
                                                      .addFormDataPart("pdf_to_transform", pdfFileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + "/" + pdfFileName))).build();
        Request request = new Request.Builder().url("http://bumblebee.stg.dreamplug.net/xfmr/v1/pdf2data").method("POST", body)
                                               .addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
                                               .addHeader("cache-control", "no-cache")
                                               .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                               .build();
        Response response = client.newCall(request).execute();
        this.writeBytes(response.body().bytes(), pdfFileName);
        // System.out.println("hi");
    }

    private void writeBytes(byte[] bytes, String fileName) {
        File file = new File("/Users/loaner/Desktop/Comparator/src/main/resources/StageResponses/" + fileName.split("\\.")[0] + ".json");
        try {

            // Initialize a pointer
            // in file using OutputStream
            OutputStream os = new FileOutputStream(file);

            // Starts writing the bytes in it
            os.write(bytes);
            System.out.println("Successfully" + " byte inserted for file " + fileName + ".pdf");

            // Close the file
            os.close();
        } catch (Exception e) {
            System.out.println(fileName + ".pdf: Exception: " + e);
        }
    }

    public void fetchAll() throws IOException {
        for (String fileName : Main.fileNames) {
            this.fetch(fileName + ".pdf");
        }
    }
}



