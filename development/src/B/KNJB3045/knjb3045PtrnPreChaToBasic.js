//選択ボタン押し下げ時の処理
function btn_submit() {

    // チェックボックスのチェック取得
    var chk = document.forms[0]['CHECK'];
    var parentFrame = this.parent.document;
    var existChecked = false;
    var bscSeq = '';

    if (chk.length) {
        for (var i=0; i < chk.length; i++) {
            if (chk[i].checked) {
                bscSeq = chk[i].value;
                existChecked = true;
            }
        }
    } else {
        if (chk.checked) {
            bscSeq = chk.value;
            existChecked = true;
        }
    }
    if (!existChecked) {
        parent.closeit();
        return false;
    }
    var parentText = parentFrame.getElementsByName(document.forms[0].TEXTBOX.value)[0];
    if (parentText) {
        parentText.value = bscSeq;
    }
    parent.closeit();
    parent.btn_submit('prevRead');
}

// 選択されたチェックボックス以外の選択を外す
function chkChange(obj) {
    if (!obj) { return; }
    var chk = document.forms[0]['CHECK'];
    if (chk.length) {
        for (var i=0; i < chk.length; i++) {
            if (obj != chk[i]) {
                chk[i].checked = false;
            }
        }
    }
    return;
}
