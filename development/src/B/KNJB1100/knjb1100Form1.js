function btn_submit(cmd)
{
    if (cmd == 'update') {
        //事前処理チェック
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
        if (!confirm('名簿自動生成を実行します\nよろしいですか？')) {
            return false;
        }
        document.getElementById("marq_msg").style.color = '#FF0000';
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
