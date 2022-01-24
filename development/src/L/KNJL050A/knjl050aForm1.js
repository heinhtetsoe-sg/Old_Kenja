function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled)) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'next' || cmd == 'back') {
        //必須チェック
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（試験区分）');
            return true;
        }
        if (document.forms[0].TESTSUBCLASSCD.value == ""){
            alert('{rval MSG304}'+ '\n（入力教科）');
            return true;
        }
    }

    //CSV出力
    if (cmd == "csv" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//得点チェック
function CheckScore(obj) {
    obj.value = toInteger(obj.value);
    if (obj.value > eval(aPerfect[obj.id])) {
        alert('{rval MSG901}' + '\n満点：'+aPerfect[obj.id]+'以下で入力してください。');
        obj.focus();
        obj.select();
        return;
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    var rjObj = document.getElementsByName("TESTSUBCLASSCD");
    for (ii = 0;ii < rjObj.length; ii++) {
        if (rjObj[ii].checked) {
            document.forms[0].HID_TESTSUBCLASSCD.value  = rjObj[ii].value;
        }
    }

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    for (ii = 0;ii < rjObj.length; ii++) {
        rjObj[ii].disabled = true;
    }
    document.forms[0].btn_back.disabled         = true;
    document.forms[0].btn_next.disabled         = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";

    //得点更新フラグ
    targetName = "UPD_FLG_" + obj.id;
    targetObject = eval("document.forms[0][\"" + targetName + "\"]");
    if (targetObject) {
        targetObject.value = '1';
    }
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECEPTNO.value.split(',');
        var index = setArr.indexOf(obj.id);
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
            var targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (setArr.length - 1); i++) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}
