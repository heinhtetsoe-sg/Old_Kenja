function btn_submit(cmd) {
    if ((cmd == 'exec') && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (cmd == 'exec') {
        //必須チェック
        if (document.forms[0].YEAR_SEMESTER.value == '') {
            alert('{rval MSG310}\n（対象年度・学期）');
            return;
        }

        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = 'head';
        }
        if (document.forms[0].OUTPUT[2].checked == true) {
            cmd = 'error';
        }
        if (document.forms[0].OUTPUT[3].checked == true) {
            //必須チェック
            if (document.forms[0].GRADE_COURSE.value == '') {
                alert('{rval MSG310}\n（コース）');
                return;
            }
            cmd = 'data';
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
