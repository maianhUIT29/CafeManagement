package com.example.cafemanagement.helper;

/**
 * Singleton giữ trạng thái ca làm việc hiện tại trong memory.
 * Sau khi cashier mở ca, shiftId được lưu tại đây để PaymentFragment
 * có thể gắn vào Order mà không cần query Firebase mỗi lần.
 */
public class ShiftManager {

    private static ShiftManager instance;

    private String currentShiftId;    // key trong ActiveShifts (= cashierId)
    private String currentShiftName;  // "Ca Sáng" | "Ca Trưa" | "Ca Tối"
    private boolean isShiftOpen = false;

    private ShiftManager() {}

    public static ShiftManager getInstance() {
        if (instance == null) instance = new ShiftManager();
        return instance;
    }

    /** Gọi khi cashier mở ca thành công */
    public void openShift(String shiftId, String shiftName) {
        this.currentShiftId   = shiftId;
        this.currentShiftName = shiftName;
        this.isShiftOpen      = true;
    }

    /** Gọi khi cashier đóng ca hoặc logout */
    public void closeShift() {
        this.currentShiftId   = null;
        this.currentShiftName = null;
        this.isShiftOpen      = false;
    }

    public boolean isShiftOpen()      { return isShiftOpen; }
    public String  getCurrentShiftId()   { return currentShiftId; }
    public String  getCurrentShiftName() { return currentShiftName; }
}