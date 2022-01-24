function btn_submit(cmd) 
{
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].PRE_RECEPTNO.value == '') {
            alert('{rval MSG301}\n( 受付番号 )');
            return true;
        }
        if (vflg == true) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }
    if (cmd == 'addnew') {
        if (document.forms[0].PRE_TESTDIV.value == '') {
            alert('{rval MSG301}\n( プレテスト区分 )');
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
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function toTelNo(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == "-")) {
            newString += ch;
        }
    }

    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n電話番号を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}

//学園バス利用する場合、有効。(乗降地、ご利用人数)
function disBusUse(obj) {
    useFlg = true;
    if (obj.checked) {
        useFlg = false;
    }
    document.forms[0].STATIONDIV[0].disabled = useFlg;
    document.forms[0].STATIONDIV[1].disabled = useFlg;
    document.forms[0].STATIONDIV[2].disabled = useFlg;
    document.forms[0].BUS_USER_COUNT.disabled = useFlg;
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
