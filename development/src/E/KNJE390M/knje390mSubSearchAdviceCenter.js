function btn_submit(cmd) {
    if (cmd == "welfare_advice_center_search" && document.forms[0].AREACD.value == "" && document.forms[0].NAME.value == "" && document.forms[0].SERVICECD.value == "") {
        alert("検索条件には最低一つ選択または入力してください。");
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//選択ボタン押し下げ時の処理
function btn_check_submit(datacnt) {
    var comma = ",";
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var chk = document.forms[0]["CHECK[]"];
    var sep = "";
    var Ch_txt = "";
    var setText = "";
    var i;
    if (chk.length == undefined) {
        var getText = chk.value;
        setText = Ch_txt + sep + getText;
        sep = comma;
    } else {
        for (i = 0; i < chk.length; i++) {
            if (chk[i].checked && chk[i].value) {
                setText += sep + chk[i].value;
                sep = comma;
            }
        }
    }

    if (parent.document.forms[0].ADVICE_SERVICE_CENTER_TEXT.value) {
        parent.document.forms[0].ADVICE_SERVICE_CENTER_TEXT.value += comma;
    }
    parent.document.forms[0].ADVICE_SERVICE_CENTER_TEXT.value += setText;
    parent.closeit();
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}
