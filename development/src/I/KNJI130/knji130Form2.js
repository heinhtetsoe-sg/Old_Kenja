function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    if (cmd == 'fukugaku') {
        if (!document.forms[0].FUKUGAKU.value) {
            alert('既に復学されています。\nデータの変更は基礎データ入力画面で行ってください。');
            return false;
        }
    }
    if (cmd == 'subEnd') {
        alert('subEnd');
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
