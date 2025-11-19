package com.quokka.jobmate_connect.constant;

public enum SalaryUnitType {
    /* === Công việc phổ biến === */
    VND_PER_HOUR,      // Lương theo giờ
    VND_PER_SHIFT,     // Lương theo ca (4–6h)
    VND_PER_DAY,       // Lương theo ngày
    VND_PER_WEEK,      // Lương theo tuần
    VND_PER_MONTH,     // Lương theo tháng

    /* === Công việc freelancer / dịch vụ === */
    VND_PER_PROJECT,   // Lương theo dự án
    VND_PER_PRODUCT,   // Lương theo sản phẩm (thiết kế, in ấn, sản xuất)
    VND_PER_TASK,      // Lương theo nhiệm vụ / job

    /* === Công việc vận chuyển === */
    VND_PER_ORDER,     // Giao hàng theo đơn
    VND_PER_KM,        // Tính theo km (shipper, grab)

    /* === Công việc giáo dục / đào tạo === */
    VND_PER_SESSION,   // Theo buổi (gia sư, lớp học)
    VND_PER_STUDENT,   // Theo học sinh / người tham gia

    /* === Công việc bán hàng / tư vấn === */
    COMMISSION,        // Hoa hồng %
    BONUS,             // Thưởng cố định

    /* === Công việc đặc biệt === */
    NEGOTIABLE         // Thỏa thuận
}
