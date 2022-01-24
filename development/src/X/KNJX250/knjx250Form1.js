function btn_submit(cmd) {
    if (cmd == 'del' && !confirm('ファイルを削除します。よろしいでしょうか？')) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function clickPaste(pasteName, pasteType) {
    if (pasteType == "bmp" || pasteType == "jpg") {
        document.forms[0].btn_csvGet.disabled = true;
    } else {
        document.forms[0].btn_csvGet.disabled = false;
    }
    document.forms[0].ZIP_PASS.value = pasteName;
}
