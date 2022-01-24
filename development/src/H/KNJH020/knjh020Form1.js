function btn_submit(cmd) {
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
//    if (cmd == 'update' && !confirm('{rval MSG102}')){
//        return true;
//    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(jump,sno,nd)
{
    var cd;
    var cd2;
    cd = '?SCHREGNO=';
    cd2 = '&NEWAD=';
    if(sno == ''){
        alert('{rval MSG304}');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.right_frame.location.replace(jump + cd + sno + cd2 + nd);
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
        alert("入力された値は不正な文字列です。\n電話(FAX)番号を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}
/*
function checkEmail(checkString){
    var newstr = "";
    var at = false;
    var dot = false;

    if (checkString.length > 0) {

        if (checkString.indexOf("@") != -1) {
            at = true;
        } else if (checkString.indexOf(".") != -1) {
            dot = true;
        }
        for (var i = 0; i < checkString.length; i++) {
            ch = checkString.substring(i, i + 1)
            if ((ch >= "A" && ch <= "Z") || (ch >= "a" && ch <= "z")
                || (ch == "@") || (ch == ".") || (ch == "_")
                || (ch == "-") || (ch >= "0" && ch <= "9")) {
                newstr += ch;
                if (ch == "@") {
                    at=true;
                }
                if (ch == ".") {
                    dot=true;
                }
            }
        }
        if ((at == true) && (dot == true)) {
            return newstr;
        } else {
            alert("入力された値は不正な文字列です。\nE-MAILを入力してください。\n入力された文字列は削除されます。");
            // 文字列を返す
            return newstr;
        }
    }
    return checkString;
}
*/
