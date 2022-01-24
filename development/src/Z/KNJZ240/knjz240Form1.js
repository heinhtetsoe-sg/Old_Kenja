function btn_submit(cmd)
{

    if(cmd == 'clear'){
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//数値かどうかをチェック
function Num_Check(obj){
    var name = obj.name;
    var checkString = obj.value;
    var newString ="";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= "0" && ch <= "9") {
            newString += ch;
        }
    }
    if (checkString != newString || checkString == '0') {
        alert('{rval MSG901}\n数値を入力してください。');
        obj.value="";
        obj.focus();
        return false;
    }
}