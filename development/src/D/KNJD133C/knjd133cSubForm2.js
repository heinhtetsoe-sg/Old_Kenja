function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function get_element(reg) {
    var i;
    var rtn = [];
    var e;
    for (i = 0; i < parent.document.forms[0].elements.length; i++) {
        e = parent.document.forms[0].elements[i];
        if (e.name.match(reg)) {
            rtn.push(e);
        }
    }
    return rtn;
}

//選択ボタン押し下げ時の処理
function btn_submit2(datacnt, replaceFlg) {
    var i, j;
    var pat;
    if (replaceFlg == "") {
        if (document.forms[0].GRADE.value == "") {
            alert('学年を指定してください。');
            return false;
        }
    }
    if (datacnt == 0) return false;
    var chk = document.forms[0]['CHECK\[\]'];
    var sep = sep1 = sep2 = "";
    var Ch_txt1 = "";
    var taishou;
    var counter;

    if (chk.length) {
        for (i = 0; i < chk.length; i++) {
            if (chk[i].checked) {
                Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
                sep1    = "\n";
            }
        }
    } else {
        if (chk.checked) {
            Ch_txt1 = Ch_txt1 + sep1 + chk.value;
            sep1    = "\n";
        }
    }
    if (replaceFlg != "") {
        taishou = get_element(new RegExp("^" + document.forms[0].COLUMNNAME.value + "$"));
        //選択項目を対象生徒に入れる
        for (i = 0; i < taishou.length; i++) {
            if (taishou[i].value != "") {
                sep2 = "";
            }
            taishou[i].value = taishou[i].value + sep2 + Ch_txt1;
        }
    } else {
        //学年ごとの連番取得
        counterStringElements = get_element(new RegExp("^counter_array-" + document.forms[0].GRADE.value + "$"));
        for (i = 0; i < counterStringElements.length; i++) {
            counter = counterStringElements[i].value.split(',');
        }
        pat = "^" + document.forms[0].TARGETID.value + "$";
        taishou = get_element(new RegExp(pat));
        //選択項目を対象生徒に入れる
        for (i = 0; i < taishou.length; i++) {
            if (taishou[i].value != "") {
                sep2 = "";
            }
            taishou[i].value = taishou[i].value + sep2 + Ch_txt1;
        }

        parent.btn_submit('value_set');
    }
    parent.closeit();
}
