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
        document.forms[0].H_MONTH.value = document.forms[0].MONTH.value;
        document.forms[0].H_SEMESTER.value = document.forms[0].SEMESTER.value;
        document.forms[0].HR_CLASS.disabled = true;
        document.forms[0].MONTH.disabled = true;
        document.forms[0].SEMESTER.disabled = true;
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
//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}
