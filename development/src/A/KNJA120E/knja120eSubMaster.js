//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var chk = document.forms[0]["CHECK[]"];
    var sep = (sep1 = sep2 = "");
    var Ch_txt1 = "";

    if (datacnt == 1) {
        if (chk.checked) {
            Ch_txt1 = sep1 + chk.value;
        }
    } else {
        for (var cnt = 0; cnt < chk.length; cnt++) {
            if (chk[cnt].checked) {
                Ch_txt1 += sep1 + chk[cnt].value;
                sep1 = "\n";
            }
        }
    }

    if (getcmd === "teikei_act") {
        if (top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT.value != "") {
            sep2 = "";
        }
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT.value = top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT.value + sep2 + Ch_txt1;
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYACT_BG_COLOR_FLG.value = "1";
    } else {
        if (top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL.value != "") {
            sep2 = "";
        }
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL.value = top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL.value + sep2 + Ch_txt1;
        top.main_frame.right_frame.document.forms[0].TOTALSTUDYVAL_BG_COLOR_FLG.value = "1";
    }
    top.main_frame.right_frame.btn_submit("value_set");
    top.main_frame.right_frame.closeit();
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}
