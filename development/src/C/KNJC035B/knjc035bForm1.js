function btn_submit(cmd) {
    //実行削除
    if (cmd == "histdel") {
        //必須チェック
        if (document.forms[0].GRADE.value == "") {
            alert('{rval MSG310}\n（学年）');
            return true;
        }
        if (document.forms[0].SEM_MONTH.value == "") {
            alert('{rval MSG310}\n（削除月）');
            return true;
        }
    }

    //履歴削除
    if (cmd == "histdel") {
        var y = 0;
        var CounterArray = new Array();
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked) {
                CounterArray[y] = document.forms[0].elements[i].value;
                y++;
            }
        }
        //削除対象件数チェック
        if (CounterArray.length < 1) {
            alert('{rval MSG304}');
            return true;
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

//全チェック操作
function check_all(obj){
    //チェックon/off
    obj.checked = !obj.checked;

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }

    //ボタンの使用不可
    document.forms[0].btn_histdel.disabled = obj.checked;
}

//ボタンの使用不可
function OptionUse() {
    var check_flg = false;

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked == true) {
            check_flg = true;
        }
    }

	if (check_flg == true) {
        document.forms[0].btn_histdel.disabled = false;
	} else {
	    document.forms[0].btn_histdel.disabled = true;
	}
}

//変更対象行選択（チェックボックスon/off）
var selectedRow = 0;
function selectRowList() {
    var list = document.getElementById('list');
    var chk = document.forms[0]["CHECKED\[\]"];

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }

    selectedRow = event.srcElement.parentElement.rowIndex;

    //チェックon/off
    if (chk.length) {
        chk[selectedRow].checked = !chk[selectedRow].checked;
    }

    //ボタンの使用不可
    OptionUse();
}

//全チェック選択（チェックボックスon/off）
function selectRowAll() {
    var chk = document.forms[0]["CHECKALL"];

    //チェックon/off
    chk.checked = !chk.checked;

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = chk.checked;
        }
    }

    //ボタンの使用不可
	if (chk.checked == true) {
        document.forms[0].btn_histdel.disabled = false;
	} else {
	    document.forms[0].btn_histdel.disabled = true;
	}
}
