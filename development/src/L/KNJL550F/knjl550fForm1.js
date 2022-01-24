function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.all("SCORE[]") == undefined) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].APPLICANTDIV.value == "1") {
            if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
                if (confirm('{rval MSG108}')) {
                    closeWin();
                }
                return false;
            }
        } else {
            if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
                if (confirm('{rval MSG108}')) {
                    closeWin();
                }
                return false;
            }
        }
        closeWin();
    }

    //画面切換（前後）
    if (cmd == 'back' || cmd == 'next') {
        if (document.forms[0].APPLICANTDIV.value == "1") {
            if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
                if(!confirm('{rval MSG108}')) {
                    return false;
                }
            }
        } else {
            if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
                if(!confirm('{rval MSG108}')) {
                    return false;
                }
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

function MoveFocus(idx) {
   if (window.event.keyCode == 13) {
       idx++;
       document.all("SCORE"+idx.toString()).focus();
   }
}

//受験番号
function ExamNoVisible(obj) {
    //EXAMNO

    // 数値変換
    obj.value = toInteger(obj.value);

    var examNoFrom = document.forms[0].RECEPTNO_FROM.value;
    var examNoTo = document.forms[0].RECEPTNO_TO.value;

    var hidReceptNo = document.forms[0].HID_RECEPTNO.value;
    if (!hidReceptNo) {
        return;
    }
    var receptList = hidReceptNo.split(',');

    for (let i = 0; i < receptList.length; i++) {
        var recept = receptList[i].split('-');
        var receptNo = recept[0];
        var examNo = recept[1];

        var isVisibled = true;
        // From (Fromより小さい受験番号は表示しない)
        if (examNoFrom && Number(examNo) < Number(examNoFrom)) {
            isVisibled = false;
        }
        // To (Toより大きい受験番号は表示しない)
        if (examNoTo && Number(examNo) > Number(examNoTo)) {
            isVisibled = false;
        }
        var rowId = 'ROWID' + receptNo;
        document.getElementById(rowId).style.display ="block";
        if (!isVisibled) {
            document.getElementById(rowId).style.display ="none";
        }

    }

}

//得点チェック
function CheckScore(obj) {
    if (obj.value != "*") {
        obj.value = toInteger(obj.value);
        if (obj.value > eval(aPerfect[obj.id])) {
            alert('{rval MSG901}' + '\n満点：'+aPerfect[obj.id]+'以下で入力してください。');
            obj.focus();
            return;
        }
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}

function Setflg2(obj, receptno){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value   = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value        = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled   = true;
    document.forms[0].TESTDIV.disabled        = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;

    document.getElementById('ROWID' + receptno).style.background="yellow";
    obj.style.background="yellow";
}
