function btn_submit(cmd)
{
    if (cmd == 'update') {
        //事前処理チェック
        if (document.forms[0].HID_STANDARD.value == '') {
            alert('{rval MSG305}'+'\n『基準の計算方法』。\n学校マスタメンテにて設定して下さい。');
            return false;
        }
        if (document.forms[0].HID_Z017.value == 'NG') {
            alert('{rval MSG305}'+'\n『評定計算方法』。\n名称マスタメンテにて設定して下さい。');
            return false;
        }
        if (document.all("CHECK[]") == null) {
            return false;
        }
        //選択チェック
        for (var i=0; i < document.forms[0].elements.length; i++)
        {
            if (document.forms[0].elements[i].name == "CHECK[]" && document.forms[0].elements[i].checked){
                break;
            }
        }
        if (i == document.forms[0].elements.length){
            alert('「実行チェックボックス」を選択してください。');
            return true;
        }
        //実行確認
        if (!confirm('評定自動計算を実行します\nよろしいですか？')) {
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
