function btn_submit(cmd){
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

//数値かどうかをチェック
function Num_Check(obj){
    var name = obj.name;
    var checkString = obj.value;
    var newString ="";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == ".")) {
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
