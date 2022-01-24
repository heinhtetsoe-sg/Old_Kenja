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

    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].AGE.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    } else if (cmd == 'delete') {
        if (document.forms[0].AGE.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function CodeCheck(obj) {
    //数値チェック
    var checkString = obj.value;
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= "0" && ch <= "999") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数字を入力してください。\n入力された文字列は削除されます。");
        obj.value = newString;
        obj.focus();
        return;
    }

    //入力範囲チェック
    if(checkString > 999) {
        alert('{rval MSG914}'+"\n(00～999)");
        obj.value = "";
        obj.focus();
        return;
    }

    obj.value = checkString;
    return;
}
