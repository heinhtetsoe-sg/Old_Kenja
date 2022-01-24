function btn_submit(cmd) {
    //前年度コピー
    if (cmd == 'copy' && !confirm('{rval MSG101}' + '\n※対象年度にない、前年度データのみ処理します。')) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//右フレームの再読込
function reload_window(){
    var str = 'knjz221index.php?cmd=edit&ini=1&YEAR=';
    var numb = document.forms[0].YEAR.value;
    str = str + numb;
    window.open(str,'right_frame');
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
