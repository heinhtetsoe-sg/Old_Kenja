function btn_submit(cmd)
{
    if (cmd == 'update') {
//alert('工事中！！！');
//return false;
        //事前処理チェック
        if (document.forms[0].HID_STANDARD.value == '') {
            alert('{rval MSG305}'+'\n『基準の計算方法』。\n名称マスタメンテにて設定して下さい。');
            return false;
        }
        if (document.forms[0].HID_Z027.value == 'NG') {
            alert('{rval MSG305}'+'\n『計算方法』。\n名称マスタメンテにて設定して下さい。');
            return false;
        }
        //選択チェック
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            var nam = e.name;
            if (e.type == 'checkbox' && nam.match(/CHECK./) && e.checked) {
                break;
            }
        }
        if (i == document.forms[0].elements.length){
            alert('「実行チェックボックス」を選択してください。');
            return true;
        }
        //実行確認
        if (!confirm('自動計算を実行します\nよろしいですか？')) {
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
