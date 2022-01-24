function btn_submit(cmd)
{
    //戻る際、チェックボックスは外す（ログのエラー対策）
    if (cmd == 'subform6A') {
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'checkbox') {
                document.forms[0].elements[i].checked = false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    //検索
    if (cmd == 'replace6_search') {
        //検索中はテキストボックスとボタンは使用不可
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].type == 'button') {
                document.forms[0].elements[i].disabled = true;
            }
        }
    }

    return false;
}

//Enter押下でサブミット
function keydownEvent(cmd)
{
    if (event.keyCode == 13) {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();

        //検索中はテキストボックスとボタンは使用不可
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].type == 'button') {
                document.forms[0].elements[i].disabled = true;
            }
        }
    }
}

function toInteger2(checkString)
{
    var newString = "";
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || ch == " ") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数字を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}

//全チェック操作
function check_all(obj)
{
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//変更対象行選択（チェックボックスon/off）
var selectedRow = 0;
function selectRowList()
{
    var list = document.getElementById('list');
    var chk  = document.forms[0]["CHECKED\[\]"];

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }

    selectedRow = event.srcElement.parentElement.rowIndex;

    //チェックon/off
    if (chk.length) {
        chk[selectedRow].checked = !chk[selectedRow].checked;
    }
}

//選択（更新）
function doSubmit()
{
    //必須チェック
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked){
            break;
        }
    }
    if (i == document.forms[0].elements.length) {
        alert('{rval MSG304}');
        return true;
    }

    //日付範囲チェック
    var date   = document.forms[0].TOROKU_DATE.value.split('/');
    var sdate  = document.forms[0].SDATE.value.split('/');
    var edate  = document.forms[0].EDATE.value.split('/');
    sdate_show = document.forms[0].SDATE.value;
    edate_show = document.forms[0].EDATE.value;

    if (document.forms[0].TOROKU_DATE.value == "") {
        alert('{rval MSG901}\n（登録日：' + sdate_show + '～' + edate_show + 'の範囲内）');
        return true;
    }

    if(   (new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])))
       || (new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))))
    {
        alert('登録日が入力範囲外です。\n（' + sdate_show + '～' + edate_show + '）');
        return true;
    }

    document.forms[0].cmd.value = 'replace_update6';
    document.forms[0].submit();
    return false;
}
