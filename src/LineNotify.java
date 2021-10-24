import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.stream.Collectors;

public class LineNotify {
    private static final String ACCESS_TOKEN = "*****************"; // Line Notifyのアクセストークン
    private static final String NOTIFY_API = "https://notify-api.line.me/api/notify";

    public static void main(String[] args) {
        LineNotify lineNotify = new LineNotify();

        JsonNode numbersapi_result = getResult("http://numbersapi.com/random/year?json"); // 年号APIからJSONを取得
        String text = numbersapi_result.get("text").textValue();
        String ja_text = getTransText(text); // 日本語に翻訳
        lineNotify.notify(ja_text);
        System.out.println("lineへ通知成功");
    }

    public void notify(String message) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(NOTIFY_API);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            try (OutputStream os = connection.getOutputStream(); PrintWriter writer = new PrintWriter(os)) {
                writer.append("message=").append(URLEncoder.encode(message, "UTF-8")).flush();
                try (InputStream is = connection.getInputStream();
                        BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                    String res = r.lines().collect(Collectors.joining());
                    if (!res.contains("\"message\":\"ok\"")) {
                        System.out.println(res);
                    }
                }
            }
        } catch (Exception ignore) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static JsonNode getResult(String urlString) {
        String result = "";
        JsonNode root = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect(); // URL接続
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String tmp = "";

            while ((tmp = in.readLine()) != null) {
                result += tmp;
            }

            ObjectMapper mapper = new ObjectMapper();
            root = mapper.readTree(result);
            in.close();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }

    public static String getTransText(String before) {
        String afterText = null; // 翻訳されたテキスト
        try {
            // URLクラスのインスタンスを生成
            URL exciteURL = new URL("https://www.excite.co.jp/world/english/");
            // 接続
            URLConnection con = exciteURL.openConnection();
            // 出力を行うように設定
            con.setDoOutput(true);
            // 出力ストリームを取得
            PrintWriter out = new PrintWriter(con.getOutputStream());
            // クエリー文の生成・送信
            out.print("before={" + before + "}&wb_lp=ENJA");
            out.close();
            // 入力ストリームを取得
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            // 一行ずつ読み込む
            String aline;
            String matche = "<textarea id=\"after\" class=\"resizable\" cols=\"37\" rows=\"13\" name=\"after\">";
            while ((aline = in.readLine()) != null) {
                if (aline.contains(matche)) {
                    // 不要な文字を削除
                    afterText = aline.replace(matche + "{", "").replace("}</textarea>", "").trim();
                }
            }
            in.close(); // 入力ストリームを閉じる
        } catch (IOException e) {
            e.printStackTrace();
        }
        return afterText;
    }
}