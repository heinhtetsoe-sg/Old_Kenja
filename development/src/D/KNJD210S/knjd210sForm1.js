function btn_submit(cmd) {
    if (cmd == 'execute') {
//            alert("工事中！");
//            return false;

        if (document.forms[0].GRADE.options.length == 0 || document.forms[0].EXAM.options.length == 0) {
            return false;
        }

        if (document.forms[0].CHAIRDATE.value == '') {
            alert("日付を入力して下さい。");
            return false;
        }

        var chairdate = document.forms[0].CHAIRDATE.value.split('/');
        var sdate = document.forms[0].SDATE.value.split('/');
        var edate = document.forms[0].EDATE.value.split('/');

        if ((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(chairdate[0]),eval(chairdate[1])-1,eval(chairdate[2])))
           || ((new Date(eval(chairdate[0]),eval(chairdate[1])-1,eval(chairdate[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
        {
            alert("日付が学期の範囲外です");
            return false;
        }

        if (confirm('{rval MSG101}')) {
            document.all('marq_msg').style.color = '#FF0000';
        } else {
            return;
        }
    }
    //読込中は実行ボタンをグレーアウト
    document.forms[0].btn_exec.disabled = true;

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