function btn_submit(cmd) {

    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;

}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//数値かどうかをチェック
function Num_Check(obj) {
    var name = obj.name;
    var checkString = obj.value;
    var newString ="";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9")) {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert('{rval MSG901}\n数値を入力してください。');
        obj.value="";
        obj.focus();
        return false;
    }
}
