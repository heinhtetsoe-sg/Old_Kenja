function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (cmd != "changeCmb") {
        if (document.forms[0].OUTPUT[1].checked == true) {
            if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
                return true;
            }
        } else if (cmd != ""){
            cmd = "csv";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();

}

//セキュリティーチェック
function OnSecurityError() {
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
//ラジオボタン操作(出力対象, 1:知的障害, 2:知的障害以外)
function changeRadioT(obj) {
    if (obj.value == '2') {
        document.forms[0].PAGEDIV1.checked  = false;
        document.forms[0].PAGEDIV2.checked  = true;
        document.forms[0].DATADIV1.disabled = false;
        document.forms[0].DATADIV2.disabled = false;
        document.forms[0].PAGEDIV1.disabled = true;
    } else {
        document.forms[0].PAGEDIV1.disabled = false;
    }
}
//ラジオボタン操作(要録ページ種類)
function changeRadioP(obj) {
//    document.forms[0].PAGEDIV.value = obj.value;
    if (obj.value == '2') {
        document.forms[0].DATADIV1.disabled = false;
        document.forms[0].DATADIV2.disabled = false;
    } else {
        document.forms[0].DATADIV1.disabled = true;
        document.forms[0].DATADIV2.disabled = true;
    }
}
