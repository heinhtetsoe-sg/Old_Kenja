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

    //更新
    if (cmd == 'update') {
        if (!checkVal()) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeFlg(obj){
    document.forms[0].CHANGE_FLG.value = '1';

    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_EXAMHALLCD.value      = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].EXAMHALLCD.disabled       = true;

//    document.getElementById('ROWID' + obj.id).style.background="yellow";
//    obj.style.background="yellow";
}

//チェック処理
function checkVal() {
    var examnoArray = document.forms[0].HID_EXAMNO.value.split(',');
    var examnoError = '';
    var seq = '';
    for (var line = 0; line < examnoArray.length; line++) {
        var cmbInterview = document.getElementById("INTERVIEW_A-" + examnoArray[line]);
        var checkAttend  = document.getElementById("ATTEND_FLG-" + examnoArray[line]);
        //評価、欠席のいづれか1つが有効
        if (cmbInterview.value != '' && checkAttend.checked == true) {
            examnoError += seq + examnoArray[line];
            seq = ',';
//            document.getElementById('ROWID' + examnoArray[line]).style.background = "red";
        } else {
//            document.getElementById('ROWID' + examnoArray[line]).style.background = "white";
        }
    }
    if (examnoError != '') {
        alert('「評価」または「欠席」のいづれか1つを入力して下さい。\n受験番号：' + examnoError);
        return false;
    }
    return true;
}
