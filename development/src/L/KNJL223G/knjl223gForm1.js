function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//戻るボタン
function Page_jumper(link) {
    if (vflg && !confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
