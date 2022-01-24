function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//選択ボタン押し下げ時の処理
function btn_submit2(datacnt) {
    if (datacnt == 0) return false;

    var chk = document.forms[0]['CHECK\[\]'];
    var sep = sep1 = sep2 = "";
    var Ch_txt1 = "";

    if (datacnt == 1) {
        if (chk.checked) {
            Ch_txt1 = sep1 + chk.value;
        }
    } else {
        for (var cnt = 0; cnt < chk.length; cnt++) {
            if (chk[cnt].checked) {
                Ch_txt1 += sep1 + chk[cnt].value;
                sep1    = "\n";
            }
        }
    }

    //選択項目を対象に入れる
    var e = parent.document.forms[0].elements[1];
    var nam = e.name;
    target = "COMMUNICATION";
    if (nam == target) {
        if (e.value != "") {
            sep2 = "";
        }
        e.value = e.value + sep2 + Ch_txt1;
    }

    parent.closeit();
}
