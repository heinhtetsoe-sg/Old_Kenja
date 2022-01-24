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

    if (datacnt == 1) {
        if (chk.checked) {
            Ch_txt1 = sep1 + chk.value;
        }
    } else {
        for (var cnt = 0; cnt < chk.length; cnt++) {
            if (chk[cnt].checked) {
                Ch_txt1 +=sep1 + chk[cnt].value;
                sep1 = "\n";
            }
        }
    }

    //学年ごとの連番取得
    reg1 = new RegExp("^counter_array-" + document.forms[0].GRADE.value + "$");
    for (var i=0; i < parent.document.forms[0].elements.length; i++) {
        var e = parent.document.forms[0].elements[i];
        var nam = e.name;
        if (nam.match(reg1)) {
            var counter = e.value.split(',');
        }
    }

    if (document.forms[0].DATA_DIV.value == '03') {
        var targetX = 'TOTALSTUDYACT';
    } else if (document.forms[0].DATA_DIV.value == '04') {
        var targetX = 'TOTALSTUDYTIME';
    }
    //選択項目を対象生徒に入れる
    for (var i=0; i < parent.document.forms[0].elements.length; i++) {
        var e = parent.document.forms[0].elements[i];
        var nam = e.name;

        for (var j=0; j < counter.length; j++) {
            reg2 = new RegExp("^" + targetX + "-" + counter[j] + "$");
            if (nam.match(reg2)) {
                if (e.value != "") {
                    sep2 = "";
                }
                e.value = e.value + sep2 + Ch_txt1;
            }
        }
    }

    parent.btn_submit('value_set');
    parent.closeit();
}
