package com.example.aes;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Base64;
import java.util.Random;

public class AESController {

    @FXML
    private Button btnDecrypt;

    @FXML
    private Button btnEncrypt;

    @FXML
    private Button btnKey;

    @FXML
    private TextArea outputTextArea;

    @FXML
    private TextArea txtInput;

    @FXML
    private TextField txtKey;

    private int[][] state;  // Trạng thái 4x4 cho AES
    private int[][] keySchedule;  // Lịch trình khóa cho AES

    /**
     * Tạo khóa AES ngẫu nhiên hoặc sử dụng khóa do người dùng nhập và hiển thị trong txtKey
     */
    @FXML
    private void generateKey() {
        String userKey = txtKey.getText().trim();

        // Nếu người dùng nhập khóa ngắn hơn 32 ký tự
        if (!userKey.isEmpty() && userKey.length() < 32) {
            // Lặp lại khóa cho đủ 32 ký tự
            StringBuilder repeatedKey = new StringBuilder(userKey);
            while (repeatedKey.length() < 32) {
                repeatedKey.append(userKey);
            }
            userKey = repeatedKey.substring(0, 32); // Chỉ lấy đúng 32 ký tự sau khi lặp lại
        }

        // Nếu người dùng nhập khóa hợp lệ (32 ký tự hex)
        if (!userKey.isEmpty() && userKey.length() == 32 && userKey.matches("[0-9a-fA-F]+")) {
            keySchedule = new int[4][4];
            for (int i = 0; i < 16; i++) {
                keySchedule[i / 4][i % 4] = Integer.parseInt(userKey.substring(2 * i, 2 * i + 2), 16);
            }
        } else if (userKey.isEmpty()) {
            // Sinh khóa ngẫu nhiên nếu người dùng không nhập khóa
            int[][] key = new int[4][4];
            Random random = new Random();
            StringBuilder keyHex = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                key[i / 4][i % 4] = random.nextInt(256); // Giá trị ngẫu nhiên từ 0-255
                keyHex.append(String.format("%02x", key[i / 4][i % 4]));
            }
            keySchedule = key;
            txtKey.setText(keyHex.toString()); // Hiển thị khóa đã sinh ra
        } else {
            // Báo lỗi nếu khóa không hợp lệ
            outputTextArea.setText("Khóa không hợp lệ! Vui lòng nhập khóa 128-bit (32 ký tự hex).");
            keySchedule = null;  // Đặt keySchedule thành null để tránh lỗi
        }
    }

    @FXML
    private void clearAll() {
        txtInput.clear();
        txtKey.clear();
        outputTextArea.clear();
    }

    /**
     * Mã hóa văn bản trong txtInput và hiển thị kết quả trong outputTextArea
     */
    @FXML
    private void encrypt() {
        try {
            // Gọi hàm generateKey để lấy khóa nếu chưa có
            generateKey();

            // Kiểm tra nếu keySchedule là null
            if (keySchedule == null) {
                outputTextArea.setText("Khóa không hợp lệ! Vui lòng nhập khóa hợp lệ.");
                return;
            }

            // Lấy dữ liệu từ txtInput
            String inputText = txtInput.getText();
            byte[] inputBytes = inputText.getBytes();

            // Chuẩn bị state từ dữ liệu đầu vào (dữ liệu sẽ được cắt và truyền vào state)
            state = new int[4][4];
            for (int i = 0; i < Math.min(16, inputBytes.length); i++) {
                state[i / 4][i % 4] = inputBytes[i];
            }

            // Mã hóa
            AES.aesEncrypt(state, keySchedule);

            // Chuyển đổi dữ liệu đã mã hóa thành chuỗi Base64
            byte[] encryptedBytes = new byte[16];
            for (int i = 0; i < 16; i++) {
                encryptedBytes[i] = (byte) state[i / 4][i % 4];
            }

            // Hiển thị dữ liệu mã hóa dưới dạng Base64
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
            outputTextArea.setText(encryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Giải mã văn bản trong outputTextArea và hiển thị kết quả trong txtInput
     */
    @FXML
    private void decrypt() {
        try {
            // Gọi hàm generateKey để lấy khóa nếu chưa có
            generateKey();

            // Kiểm tra nếu keySchedule là null
            if (keySchedule == null) {
                outputTextArea.setText("Khóa không hợp lệ! Vui lòng nhập khóa hợp lệ.");
                return;
            }

            // Lấy dữ liệu mã hóa từ outputTextArea
            String encryptedText = outputTextArea.getText();
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

            // Chuẩn bị state từ dữ liệu mã hóa
            state = new int[4][4];
            for (int i = 0; i < 16; i++) {
                state[i / 4][i % 4] = encryptedBytes[i];
            }

            // Giải mã
            AES.aesDecrypt(state, keySchedule);

            // Chuyển đổi dữ liệu giải mã thành chuỗi văn bản
            byte[] decryptedBytes = new byte[16];
            for (int i = 0; i < 16; i++) {
                decryptedBytes[i] = (byte) state[i / 4][i % 4];
            }

            // Hiển thị văn bản giải mã
            String decryptedText = new String(decryptedBytes);
            txtInput.setText(decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
