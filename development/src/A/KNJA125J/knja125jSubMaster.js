//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var chk = document.forms[0]["CHECK[]"];
    var sep = (sep1 = sep2 = "");
    var Ch_txt1 = "";

    if (chk.length) {
        for (var i = 0; i < chk.length; i++) {
            if (chk[i].checked) {
                Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
                sep1 = "";
            }
        }
    } else {
        if (chk.checked) {
            Ch_txt1 = Ch_txt1 + sep1 + chk.value;
            sep1 = "";
        }
    }
    if (getcmd === "teikei") {
        if (top.main_frame.right_frame.document.forms[0].REMARK1.value != "") {
            sep2 = "";
        }
        top.main_frame.right_frame.document.forms[0].REMARK1.value =
            top.main_frame.right_frame.document.forms[0].REMARK1.value +
            sep2 +
            Ch_txt1;
        top.main_frame.right_frame.document.forms[0].REMARK1_BG_COLOR_FLG.value =
            "1";
    } else if (getcmd === "teikei_act") {
        if (
            top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT.value !=
            ""
        ) {
            sep2 = "";
        }
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT.value =
            top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT.value +
            sep2 +
            Ch_txt1;
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT_BG_COLOR_FLG.value =
            "1";
    } else {
        if (
            top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL.value !=
            ""
        ) {
            sep2 = "";
        }
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL.value =
            top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL.value +
            sep2 +
            Ch_txt1;
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL_BG_COLOR_FLG.value =
            "1";
    }
    top.main_frame.right_frame.btn_submit("value_set");
    top.main_frame.right_frame.closeit();
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}
