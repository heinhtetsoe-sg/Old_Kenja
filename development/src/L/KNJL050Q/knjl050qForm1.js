function btn_submit(cmd) {
    //更新
    if (cmd == "update" && document.forms[0].NYURYOKU1.checked == true && document.all("SCORE[]") == undefined) {
        return false;
    }
    if (cmd == "update" && document.forms[0].NYURYOKU2.checked == true && document.all("SCORE2[]") == undefined) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    //画面切換（前後）
    if (cmd == 'back' || cmd == 'next') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
            if(!confirm('{rval MSG108}')) {
                return false;
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

//カーソルセット
function setCursor(nextId, setval, enterFlg) {
    document.getElementById("mainDiv").scrollTop = setval;

    if (enterFlg == "1" && document.getElementById(nextId)) {
        document.getElementById(nextId).focus();
        document.getElementById(nextId).select();
    }
}

//エンター押下時処理
function goEnter(obj) {
    if (window.event.keyCode == '13' || window.event.keyCode == '9') {
        document.forms[0].ENTER_FLG.value = "1";
        obj.blur();
        return false;
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

    //更新処理用（得点、受験番号セット）
    document.forms[0].HID_RECEPTNO2.value = obj.id;
    document.forms[0].HID_SCORE.value = obj.value;

    //次の受験番号セット
    var setArr = document.forms[0].HID_RECEPTNO.value.split(obj.id);
    if (document.forms[0].HID_SCHOOLKIND.value == 'P') {
        var nextId = setArr[1].substr(1, 4);
    } else {
        var nextId = setArr[1].substr(1, 5);
    }
    document.forms[0].NEXT_ID.value = nextId;

    var y = document.getElementById("mainDiv").scrollTop;
    document.forms[0].SET_SC_VAL.value = y;
    if (document.forms[0].ENTER_FLG.value != "1") {
        document.forms[0].ENTER_FLG.value = "2";
    }

    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}

function Setflg(obj, check){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;
    if (check == "2") {
        document.forms[0].NYURYOKU1.disabled = true;
    } else {
        document.forms[0].NYURYOKU2.disabled = true;
    }

    if (check == "2") {
        if (obj.value != document.getElementById('SCORE' + obj.id).innerHTML) {
            document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
            document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
            document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

            document.forms[0].APPLICANTDIV.disabled = true;
            document.forms[0].TESTDIV.disabled = true;
            document.forms[0].TESTSUBCLASSCD.disabled = true;

        }
    }
}
