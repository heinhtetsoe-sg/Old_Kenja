function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.all("SCORE[]") == undefined) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].EXAMHALLCD.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
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

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].TESTSUBCLASSCD.value == '') {
        alert('{rval MSG310}\n( 試験科目 )');
        return;
    }
    if (document.forms[0].EXAMHALLCD.value == '') {
        alert('{rval MSG310}\n( 会場 )');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

//エンターキーで移動
function goEnter(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECEPTNO.value.split(',');
        var index = setArr.indexOf(obj.id);
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
        }
        var targetId = setArr[index];
        document.getElementById(targetId).focus();
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
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    if (document.forms[0].isKeiai.value == "1") {
        document.forms[0].HID_EXAM_TYPE.value       = document.forms[0].EXAM_TYPE.options[document.forms[0].EXAM_TYPE.selectedIndex].value;
    }
    document.forms[0].HID_TESTSUBCLASSCD.value  = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].HID_EXAMHALLCD.value      = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].TESTSUBCLASSCD.disabled   = true;
    document.forms[0].EXAMHALLCD.disabled       = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}
//ボタンの無効化（更新、取消）
function disabledButton(obj) {
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_reset.disabled = true;
}
