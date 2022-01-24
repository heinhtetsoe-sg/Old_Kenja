function btn_submit(cmd) {
    //データ指定チェック
    if (cmd == 'update') {
        if (!document.forms[0].GRADE_HR_CLASS.value) {
            alert('{rval MSG304}\n年組が選択されていません。');
            return false;
        }
        if (!document.forms[0].DATA_CNT.value) {
            alert('{rval MSG303}');
            return false;
        }
    }
    //取消確認
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        updateFrameLockNotMessage();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj){
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//変更対象行選択（チェックボックスon/off）
var selectedRow = 0;
function selectRowList() {
    var list = document.getElementById('list');
    var chk = document.forms[0]["CHECKED[]"];

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }

    selectedRow = event.srcElement.parentElement.rowIndex;

    //チェックon/off
    if (list.rows.length == 1) {
        chk.checked = !chk.checked;
    } else {
        if (chk.length) {
            chk[selectedRow].checked = !chk[selectedRow].checked;
        }
    }
}
