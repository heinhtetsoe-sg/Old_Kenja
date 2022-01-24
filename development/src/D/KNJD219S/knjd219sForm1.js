function btn_submit(cmd) {
    if (cmd == 'execute') {

        if (document.forms[0].SEMESTER.value == '') {
            alert("学期を選択して下さい。");
            return false;
        }

        if (document.forms[0].GRADE.value == '') {
            alert("学年を選択して下さい。");
            return false;
        }

        if (document.forms[0].EXAM.value == '') {
            alert("種別を選択して下さい。");
            return false;
        }

        if (document.forms[0].SUBCLASSCD.value == '') {
            alert("科目を選択して下さい。");
            return false;
        }

        if (confirm('{rval MSG101}')) {
            document.all('marq_msg').style.color = '#FF0000';
        } else {
            return;
        }

        document.forms[0].btn_exec.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
