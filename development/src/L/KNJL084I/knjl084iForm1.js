function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//カーソルセット
function setCursor(nextId, setval) {
    document.getElementById("mainDiv").scrollTop = setval;

    if (document.getElementById("SCORE-" + nextId)) {
        document.getElementById("SCORE-" + nextId).focus();
        document.getElementById("SCORE-" + nextId).select();
    }
}

//エンター押下時処理
function goEnter(obj) {
    if (window.event.keyCode == '13' || window.event.keyCode == '9') {
        if (window.event.shiftKey == true && window.event.keyCode == '9') {
            document.forms[0].ENTER_FLG.value = '3';
        } else {
            document.forms[0].ENTER_FLG.value = '1';
        }
        obj.blur();
        return false;
    }
}

//得点チェック
function checkScore(obj) {
    //満点チェック
    if (obj.value != "*") {
        obj.value = toInteger(obj.value);
        var perfect = document.forms[0].PERFECT.value;
        if (obj.value > eval(perfect)) {
            alert('{rval MSG901}' + '\n満点：'+perfect+'以下で入力してください。');
            obj.focus();
            return;
        }
    }

    //更新処理
    if (document.forms[0].ENTER_FLG.value != '') {
        setNxt(obj, true);
    }
}

//欠席にチェックを入れた時、得点テキストをdisableにする。
function disScore(obj) {
    var receptno = obj.id.split('-')[1];
    var objScore = document.getElementById("SCORE-" + receptno);
    if (objScore) {
        objScore.style.removeAttribute("backgroundColor");
        objScore.disabled = obj.checked;
        if (!obj.checked) {
            objScore.style.background="yellow";
        }
    }
}

//更新処理
function setNxt(obj, sfFlg) {
    // //更新処理用（受験番号セット）
    var upd_receptno = obj.id.split('-')[1];

    //次の受験番号セット
    var receptnoArray = document.forms[0].HID_RECEPTNO.value.split(',');
    var nextId = '';
    for (var line = 0; line < receptnoArray.length; line++) {
        if (upd_receptno == receptnoArray[line]) {
            if (document.forms[0].ENTER_FLG.value == '3') {
                for (var sline = line - 1; sline >= 0; sline--) {
                    if (document.getElementById("SCORE-" + receptnoArray[sline]).disabled == false) {
                        nextId = receptnoArray[sline];
                        break;
                    }
                }
            } else {
                for (var sline = line + 1; sline < receptnoArray.length; sline++) {
                    if (document.getElementById("SCORE-" + receptnoArray[sline]).disabled == false) {
                        nextId = receptnoArray[sline];
                        break;
                    }
                }
            }
        }
    }

    //スクロール
    var y = document.getElementById("mainDiv").scrollTop;

    //エンター処理終了
    document.forms[0].ENTER_FLG.value = '';

    if (sfFlg) {
        setCursor(nextId, y);
    }

    return false;
}

function changeFlg(obj){

    if (document.forms[0].CHANGE_FLG.value != '1') {
        document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
        document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
        document.forms[0].HID_EXAMHALLCD.value      = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;
        document.forms[0].HID_TESTSUBCLASSCD.value  = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

        document.forms[0].APPLICANTDIV.disabled     = true;
        document.forms[0].TESTDIV.disabled          = true;
        document.forms[0].EXAMHALLCD.disabled       = true;
        document.forms[0].TESTSUBCLASSCD.disabled   = true;
    }
    document.forms[0].CHANGE_FLG.value = '1';

    if (obj.id.indexOf('-') > 0) {
        var idxStr = obj.id.split('-');
        document.getElementById('ROWID' + idxStr[1]).style.background="yellow";
        obj.style.background="yellow";
        if (document.forms[0].UPD_RECEPTNO.value.indexOf(idxStr[1]) < 0) {
            if (document.forms[0].UPD_RECEPTNO.value == "") {
                document.forms[0].UPD_RECEPTNO.value += idxStr[1];
            } else {
                document.forms[0].UPD_RECEPTNO.value += "," + idxStr[1];
            }
        }
    }

}
