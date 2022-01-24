function btn_submit(cmd)
{

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
