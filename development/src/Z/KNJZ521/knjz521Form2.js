function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //必須入力チェック
    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].NAMECD.value == '') {
            alert('{rval MSG301}\n　　　　　　　(　コード　)');
            return false;
        }
        if (document.forms[0].NAME.value == '') {
            alert('{rval MSG301}\n　　　　　　　(　名 称　)');
            return false;
        }
    } else if (cmd == 'delete') {
        if (document.forms[0].NAMECD.value == '') {
            alert('{rval MSG301}\n　　　　　　　(　コード　)');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//電話・FAX番号チェック
function toTelNo(obj, name){
    var newString = "";
    var count = 0;
    var checkString = obj.value;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == "-")) {
            newString += ch;
        }
    }

    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n"+name+"を入力してください。\n入力された文字列は削除されます。");
        obj.focus();
        // 文字列を返す
        return newString;
    }
    return checkString;
}
