//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;

    var chkTate = document.forms[0]["CHECK_TATE[]"];
    var chkYoko = document.forms[0]["CHECK_YOKO[]"];
    var sep = "";
    var sep2 = "";
    var Ch_txt1 = "";

    //横チェック

    if (chkTate.length == undefined || chkTate.length == undefined) {
        Ch_txt1 = chkTate.value;
    } else {
        if (document.forms[0].SHEET_PATTERN.value == "2") {
            for (var i = 0; i < chkTate.length; i++) {
                var dataDiv = chkTate[i].value;
                for (var j = 0; j < chkYoko.length; j++) {
                    var yokoName = chkYoko[j].value;
                    var text = document.forms[0]["DIV" + dataDiv + "_" + yokoName].value;
                    if (chkTate[i].checked && chkYoko[j].checked && text != "") {
                        Ch_txt1 = Ch_txt1 + sep + text;
                        sep = ",";
                    }
                }
            }
        } else {
            chkYoko = document.forms[0]["CHECK_YOKO[]"];
            var yokoName = chkYoko.value;
            for (var i = 0; i < chkTate.length; i++) {
                var dataDiv = chkTate[i].value;
                var text = document.forms[0]["DIV" + dataDiv + "_" + yokoName].value;
                if (chkTate[i].checked && chkYoko.checked && text != "") {
                    Ch_txt1 = Ch_txt1 + sep + text;
                    sep = ",";
                }
            }
        }
    }

    //呼び出し画面に応じた処理を行う
    var parentGamen = document.forms[0].ASSESS_TORIKOMI_PARENT.value;
    var target = document.forms[0].ASSESS_TORIKOMI_TARGET.value;

    if (parentGamen === "SubForm_Zittai") {
        if (top.main_frame.right_frame.document.forms[0][target].value != "" && Ch_txt1 != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0][target].value = top.main_frame.right_frame.document.forms[0][target].value + sep2 + Ch_txt1;
    }
    top.main_frame.right_frame.closeit();
}
