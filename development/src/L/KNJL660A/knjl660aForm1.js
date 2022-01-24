var marq_msg;
function btn_submit(cmd) {
    //実行
    if (cmd == 'exec') {
        if (!confirm('{rval MSG101}')) {
            return;
        } else {
            //データ格納
            document.forms[0].HIDDEN_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.value;
            document.forms[0].HIDDEN_TESTDIV.value = document.forms[0].TESTDIV.value;

            //使用不可項目
            document.forms[0].APPLICANTDIV.disabled = true;
            document.forms[0].TESTDIV.disabled = true;
            document.forms[0].btn_exec.disabled = true;
            document.forms[0].btn_end.disabled = true;

            //メッセージ表示
            marq_msg = document.getElementById("marq_msg");
            marq_msg.style.display = "block";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
