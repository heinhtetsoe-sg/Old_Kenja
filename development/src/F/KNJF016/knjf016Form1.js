function btn_submit(cmd) {
    //データ指定チェック
    if (cmd == 'update') {
        var dataCnt = document.forms[0].DATA_CNT.value;
        if (dataCnt == 0) {
            alert('{rval MSG304}');
            return false;
        }
        //更新中、サブミットする項目使用不可
        document.forms[0].H_HR_CLASS.value = document.forms[0].HR_CLASS.value;
        document.forms[0].H_NO.value = document.forms[0].NO.value;
        document.forms[0].HR_CLASS.disabled = true;
        document.forms[0].NO.disabled = true;
        document.forms[0].btn_reset.disabled = true;
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //サブミット中、更新ボタン使用不可
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//数値かどうかをチェック
function Num_Check(obj) {
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
//視力(文字)
function Mark_Check(obj) {
    var mark = obj.value;
    switch(mark) {
        case "a":
        case "A":
        case "ａ":
        case "Ａ":
            obj.value = "A";
            break;
        case "b":
        case "B":
        case "ｂ":
        case "Ｂ":
            obj.value = "B";
            break;
        case "c":
        case "C":
        case "ｃ":
        case "Ｃ":
            obj.value = "C";
            break;
        case "d":
        case "D":
        case "ｄ":
        case "Ｄ":
            obj.value = "D";
            break;
        case "":
            obj.value = "";
            break;
        default:
            alert("A～Dを入力して下さい。");
            obj.value = "";
            break;
    }
}
