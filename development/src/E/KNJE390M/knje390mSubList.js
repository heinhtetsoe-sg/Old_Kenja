function btn_submit() {
    var getcmd = document.forms[0].GET_CMD.value;
    if (getcmd === "subform2_check_B_sanshou_set") {
        top.main_frame.right_frame.document.forms[0].CENTERCD.value = document.forms[0].CENTERCD.value;
        top.main_frame.right_frame.document.forms[0].CHECK_NAME.value = document.forms[0].CHECK_NAME.value;
        top.main_frame.right_frame.document.forms[0].CHECKER.value = document.forms[0].CHECKER.value;
    }
    top.main_frame.right_frame.document.forms[0].CHECK_DATE.value = document.forms[0].CHECK_DATE.value;
    top.main_frame.right_frame.document.forms[0].CHECK_REMARK.value = document.forms[0].CHECK_REMARK.value;
    top.main_frame.right_frame.closeit();
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}
