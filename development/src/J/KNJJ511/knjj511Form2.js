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

    if (cmd == 'update' || cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].ITEMCD.value == '') {
            alert('{rval MSG301}');
            return false;
        }
        if (document.forms[0].SEX.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//入力チェック
function calc(obj) {

    //小数点の有無チェック
    if(obj.value.match(/^-?[0-9]+$/) || obj.value.match(/^-?[0-9]+\.[0-9]+$/)){
        //小数点チェック
        if(String(obj.value).indexOf(".") >= 0
           && String(obj.value).split(".")[1].length > 3){
            alert('{rval MSG901}\n小数第三位まで');
            obj.focus();
            return;
        }
        // if (parseFloat(obj.value) <= 0) {
        //     alert('{rval MSG901}\n0より大きい値を入力してください。');
        //     return;
        // }
    } else {
        //少数点無しの場合
        CodeCheck(obj);
    }

    //入力範囲チェック
    obj.value = toFloat(obj.value);
    if (parseFloat(obj.value) >= 1000) {
        alert('{rval MSG915}' + '\n【0～999.9】までを入力して下さい。');
        obj.focus();
        return;
    }
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
