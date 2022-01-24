function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    if (document.forms[0].DATE.value == "")
    {
        alert("日付が不正です。");
        document.forms[0].DATE.focus();
        return;
    }

    if (document.forms[0].PAGE && document.forms[0].PAGE.value == "")
    {
        alert("ページ番号初期値が不正です。");
        document.forms[0].PAGE.focus();
        return;
    }


    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJG";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//印刷ページ番号条件---NO002
function disopt3(output_val){
    if (output_val == 1) {
        document.forms[0].PAGE.disabled = true;
    }
    if (output_val == 2) {
        document.forms[0].PAGE.disabled = false;
    }
}
