function btn_submit(cmd) {
    if (cmd == "select_school") {
        var sch = document.forms[0].SCHOOLCD;
        sch.value = "";
        var sep = "";
        var chkCnt = 0;

        for (var i=0; i < document.forms[0].elements.length; i++) {
            //選択した学校のみ対象
            if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked == true) {
                sch.value = sch.value + sep + document.forms[0].elements[i].value;
                sep = ",";
                chkCnt++;
            }
        }

        //選択件数チェック
        if (chkCnt > 100) {
            alert('{rval MSG915}\n選択件数は100件までです\n（選択件数： '+chkCnt+'件）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    //検索
    if (cmd == "select_search") {
        //検索中はテキストボックスとボタンは使用不可
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].type == 'button') {
                document.forms[0].elements[i].disabled = true;
            }
        }
    }

    return false;
}

//Enter押下でサブミット
function keydownEvent(cmd){
    if (event.keyCode == 13) {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();

        //検索中はテキストボックスとボタンは使用不可
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].type == 'button') {
                document.forms[0].elements[i].disabled = true;
            }
        }
    }
}
function toInteger2(checkString){
    var newString = "";
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || ch == " ") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数字を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}

//全チェック操作
function check_all(obj){
    obj.checked = !obj.checked;
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
        document.forms[0].btn_select.disabled = false;
	} else {
	    document.forms[0].btn_select.disabled = true;
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
    OptionUse();
}
