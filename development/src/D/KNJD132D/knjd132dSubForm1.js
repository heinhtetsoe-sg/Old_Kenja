function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//選択ボタン押し下げ時の処理
function btn_submit2(datacnt, teikeiCmd) {
    if (document.forms[0].GRADE.value == "") {
        alert('学年を指定してください。');
        return false;
    }

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

    var targetname;;
    if (teikeiCmd == "teikei1") {
        targetname = "TOTALSTUDYTIME";
    } else if (teikeiCmd == "teikei2") {
        targetname = "REMARK1";
    } else {
        return;
    }
    parent.document.getElementById(targetname).value += Ch_txt1;
}
