function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
        closeWin();
    }

    //一括更新画面へ切り替え
    if (cmd == 'replace') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeFlg() {
    document.forms[0].CHANGE_FLG.value = '1';

    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_EXAMHALLCD.value      = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].EXAMHALLCD.disabled       = true;
}

//欠席にチェックを入れた時、評価ラジオをdisableにする。
function disRadio(obj) {
    var examno = obj.id.split('-');
    var interviewArray = document.forms[0].HID_INTERVIEW.value.split(',');
    for (var i = 0; i < interviewArray.length; i++) {
        document.getElementById("INTERVIEW_A-" + examno[1] + "-" + interviewArray[i]).disabled = (obj.checked == true) ? true : false;
        document.getElementById("INTERVIEW_B-" + examno[1] + "-" + interviewArray[i]).disabled = (obj.checked == true) ? true : false;
        if (document.forms[0].IS_TESTDIV_B.value == true) {
            document.getElementById("INTERVIEW_C-" + examno[1] + "-" + interviewArray[i]).disabled = (obj.checked == true) ? true : false;
        }
    }
}
