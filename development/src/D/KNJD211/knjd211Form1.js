<!--kanji=漢字-->
<!-- <?php # $RCSfile: knjd211Form1.js,v $ ?> -->
<!-- <?php # $Revision: 56580 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:35:29 +0900 (日, 22 10 2017) $ ?> -->

function btn_submit(cmd) {
    if (cmd == 'execute') {
        if (document.forms[0].GRADE.options.length == 0 || document.forms[0].EXAM.options.length == 0) {
            return false;
        }
        if (document.forms[0].DATE.value == '')
        {
            alert("異動対象日付が未入力です。");
            return;
        }

        if (confirm('{rval MSG101}')) {
            document.all('marq_msg').style.color = '#FF0000';//NO001
        } else {
            return;
        }

        document.forms[0].btn_exec.disabled = true;
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
