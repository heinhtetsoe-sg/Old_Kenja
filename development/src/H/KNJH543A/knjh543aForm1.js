function btn_submit(cmd)
{
    if (cmd == 'update') {
        if (document.forms[0].HID_STANDARD.value == '') {
            alert('{rval MSG305}'+'\n『基準の計算方法』。\n名称マスタメンテにて設定して下さい。');
            return false;
        }
        if (document.forms[0].HID_Z027.value == 'NG') {
            alert('{rval MSG305}'+'\n『計算方法』。\n名称マスタメンテにて設定して下さい。');
            return false;
        }
        if (!confirm('計算方法設定データを更新します\nよろしいですか？')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closing_window()
{
    alert('{rval MSG300}');
    closeWin();
    return true;
}
