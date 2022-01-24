function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//選択ボタン押し下げ時の処理
function btn_submit2(datacnt) {
    if (document.forms[0].GRADE.value == "") {
        alert('学年を指定してください。');
        return false;
    }

    if (datacnt == 0) return false;

    var chk = document.forms[0]['CHECK\[\]'];
    var sep = sep1 = sep2 = "";
    var Ch_txt1 = "";
    var i;

    for (i=0; i < chk.length; i++) {
        if (chk[i].checked) {
            Ch_txt1 += sep1 + chk[i].value;
            sep1    = "\n";
        }
    }

    parent.document.getElementById("TOTALSTUDYACT").value += Ch_txt1;
}

