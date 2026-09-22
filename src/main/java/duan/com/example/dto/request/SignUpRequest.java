package duan.com.example.dto.request;


import lombok.Data;

@Data
public class SignUpRequest {

    private String hoTen;

    private String email;

    private String password;

    private String diaChi;

    private String sdt;
}
