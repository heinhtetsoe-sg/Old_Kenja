function btn_submit(cmd)
{
    if (cmd == 'update') {
        if (document.forms[0].HID_STANDARD.value == '') {
            alert('{rval MSG305}'+'\n『基準の計算方法』。\n学校マスタメンテにて設定して下さい。');
            return false;
        }
        if (document.forms[0].HID_Z017.value == 'NG') {
            alert('{rval MSG305}'+'\n『評定計算方法』。\n名称マスタメンテにて設定して下さい。');
            return false;
        }
        if (!confirm('評定計算方法設定データを更新します\nよろしいですか？')) {
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

function calc(obj) {
    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = "";
        return;
    }
}
