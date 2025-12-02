package com.quokka.jobmate_connect.constant;

public enum AuditAction {
    USER_CREATE_ACCOUNT("Tạo tài khoản"),
    USER_UPDATE_PROFILE("Cập nhật hồ sơ người dùng"),
    USER_ENABLE_2FA("Bật 2FA"),
    USER_DISABLE_2FA("Tắt 2FA"),
    USER_PASSWORD_CHANGE("Đổi mật khẩu"),
    USER_STATUS_CHANGE("Cập nhật trạng thái người dùng"),
    USER_VERIFICATION_APPROVED("Duyệt xác minh người dùng"),
    USER_VERIFICATION_REJECTED("Từ chối xác minh người dùng"),
    USER_PROMOTED_EMPLOYER("Nâng cấp quyền nhà tuyển dụng"),

    JOB_CREATE("Tạo công việc"),
    JOB_UPDATE("Cập nhật công việc"),
    JOB_CLOSE("Đóng công việc"),
    JOB_DELETE("Xóa công việc"),
    JOB_STATUS_CHANGE("Thay đổi trạng thái công việc"),

    APPLICATION_CREATE("Nộp đơn ứng tuyển"),
    APPLICATION_UPDATE_STATUS("Cập nhật trạng thái đơn ứng tuyển"),
    APPLICATION_CANCEL("Hủy đơn ứng tuyển"),

    AUTH_LOGIN_SUCCESS("Đăng nhập thành công"),
    AUTH_LOGIN_FAILED("Đăng nhập thất bại"),
    AUTH_LOGOUT("Đăng xuất"),
    AUTH_TOKEN_REFRESH("Làm mới token"),
    AUTH_PASSWORD_SET("Thiết lập mật khẩu"),
    AUTH_OTP_RESEND("Gửi lại OTP"),
    AUTH_FORGOT_PASSWORD("Yêu cầu quên mật khẩu"),
    AUTH_RESET_PASSWORD("Đặt lại mật khẩu"),
    AUTH_RESET_PASSWORD_FAILED("Đặt lại mật khẩu thất bại"),

    REPORT_CREATE("Tạo báo cáo"),
    REPORT_REVIEW_APPROVE("Phê duyệt báo cáo"),
    REPORT_REVIEW_REJECT("Từ chối báo cáo"),
    REPORT_AUTO_REVIEW("Tự động duyệt báo cáo");

    private final String label;

    AuditAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
