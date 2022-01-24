function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return;
    }

    //更新
    if (cmd == 'update') {
        if (document.forms[0].GRADE_HR_CLASS.value == '') {
            alert('{rval MSG916}\n　　　( 年組 )');
            return;
        }
        if (document.forms[0].SUBCLASSCD.value == '') {
            alert('{rval MSG916}\n　　　( 教科 )');
            return;
        }
    }

    //コピー確認
    if (cmd == 'copy' && !confirm('{rval MSG101}')) {
        return false;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//数字チェック
function NumCheck(num) {
    num = toInteger(num);

    //範囲チェック
    if (num.length > 0 && !(1 <= num && num <= 999)) {
        alert('{rval MSG916}\n( 1 ～ 999 )');
        num = '';
    }
    return num;
}

//変更対象行選択（チェックボックスon/off）
var selectedRow = 0;
function selectRowList() {
    var list = document.getElementById('list');
    var chk = document.forms[0]["CHECK\[\]"];

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }

    selectedRow = event.srcElement.parentElement.rowIndex;

    //チェックon/off
    if (chk.length) {
        if (chk[selectedRow].disabled == false) {
            chk[selectedRow].checked = !chk[selectedRow].checked;
        }
    }

    //チェックボックスが全offなら変更ボタン使用不可
    var check_flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECK[]") {
            if (document.forms[0].elements[i].checked == true) {
                check_flg = true;
            }
        }
    }
	if (check_flg == true) {
        document.forms[0].btn_update.disabled = false;
	} else {
	    document.forms[0].btn_update.disabled = true;
	}
}

//ボタンの使用不可
function OptionUse(obj) {
    var check_flg = false;

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECK[]" && document.forms[0].elements[i].checked == true) {
            check_flg = true;
        }
    }

	if (check_flg == true) {
        document.forms[0].btn_update.disabled = false;
	} else {
	    document.forms[0].btn_update.disabled = true;
	}
}
