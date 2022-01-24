function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//選択ボタン押し下げ時の処理(呼出元画面のテキストボックスへ設定)
function btn_submit2(datacnt) {
    if (document.forms[0].GRADE.value == '') {
        alert('学年を指定してください。');
        return false;
    }

    if (datacnt == 0) return false;

    var chk = document.forms[0]['CHECK[]'];
    var sep1 = '';
    var Ch_txt1 = '';
    if (datacnt == 1) {
        if (chk.checked) {
            Ch_txt1 = sep1 + chk.value;
        }
    } else {
        for (var i = 0; i < chk.length; i++) {
            if (chk[i].checked) {
                Ch_txt1 += sep1 + chk[i].value;
                sep1 = '\n';
            }
        }
    }

    if (document.forms[0].TARGETTEXT.value) {
        var targetText = parent.document.getElementsByName(document.forms[0].TARGETTEXT.value)[0];
        if (targetText) {
            //駒沢の学習内容(DATA_DIV=03)の場合
            if (document.forms[0].isKomazawa.value == '1' && document.forms[0].DATA_DIV.value == '03') {
                targetText.value = Ch_txt1;
            } else {
                targetText.value += Ch_txt1;
            }
        }
    }
    if (document.forms[0].CALLFUNC.value) {
        var callFunc = document.forms[0].CALLFUNC.value;
        if (parent[callFunc]) {
            parent[callFunc].call(this, document.forms[0].DATA_DIV.value, Ch_txt1);
        }
    }

    parent.closeit();
}
