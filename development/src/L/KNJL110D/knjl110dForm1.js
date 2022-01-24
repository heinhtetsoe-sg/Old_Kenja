function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'changeTest') {
        document.forms[0].VISIT_NO.value = '';
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].VISIT_NO.value == '') {
            alert('{rval MSG301}\n( 事前相談番号 )');
            return true;
        }
    }

    if (cmd == 'disp_clear') {
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].type == 'checkbox') {
                if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else if (document.forms[0].elements[i].type == 'checkbox') {
                    document.forms[0].elements[i].checked = false;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        outputLAYER('FINSCHOOLNAME_ID',  '');
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function toTelNo(checkString) {
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == "-")) {
            newString += ch;
        }
    }

    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n電話(FAX)番号を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}
//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}
//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
//エンターキーをTabに変換
function changeEnterToTab (obj) {
    if (window.event.keyCode == '13') {
        //移動可能なオブジェクト
        var textFieldArray = document.forms[0].setTextField.value.split(",");

        for (var i = 0; i < textFieldArray.length; i++) {
            if (textFieldArray[i] == obj.name) {
                targetObject = eval("document.forms[0][\"" + textFieldArray[(i + 1)] + "\"]");
                targetObject.focus();
                return;
            }
        }
    }
    return;
}
