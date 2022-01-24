function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == 'update') {
        if (!checkVal()) {
            return false;
        }
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
        closeWin();
    }

    //読込
    if (cmd == 'read') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }

    //実行
    if (cmd == "exec") {
        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "csvInput";
        } else {
            cmd = "csvOutput";
        }
    }

    //CSV取込
    if (cmd == "csvInput") {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }

    if (cmd == 'update' || cmd == "csvInput") {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeFlg(obj){
    document.forms[0].CHANGE_FLG.value = '1';
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled          = true;

//    document.getElementById('ROWID' + obj.id).style.background="yellow";
//    obj.style.background="yellow";
}

//チェック処理
function checkVal() {
    var receptnoArray = document.forms[0].HID_RECEPTNO.value.split(',');
    var receptnoError = '';
    var seq = '';
    for (var line = 0; line < receptnoArray.length; line++) {
        var cmbInterview = document.getElementById("INTERVIEW_A-" + receptnoArray[line]);
        var checkAttend  = document.getElementById("ATTEND_FLG-" + receptnoArray[line]);
        //評価、欠席のいづれか1つが有効
        if (cmbInterview.value != '' && checkAttend.checked == true) {
            receptnoError += seq + receptnoArray[line];
            seq = ',';
//            document.getElementById('ROWID' + receptnoArray[line]).style.background = "red";
        } else {
//            document.getElementById('ROWID' + receptnoArray[line]).style.background = "white";
        }
    }
    if (receptnoError != '') {
        alert('「評価」または「欠席」のいづれか1つを入力して下さい。\n受験番号：' + receptnoError);
        return false;
    }
    return true;
}
