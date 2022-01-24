function btn_submit(cmd) {
    //実行前確認
    if (confirm('{rval MSG101}') === false) {
        return;
    }

    //実行ボタン押下時
    if (cmd === "exec") {
        document.getElementById('marq_msg').style.color = '#f00';
    }

    //submit
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//「合計」チェック時
function OnSumCheckChanged(checkbox) {
    document.getElementById("testdiv").disabled = checkbox.checked ? true : false;
}
