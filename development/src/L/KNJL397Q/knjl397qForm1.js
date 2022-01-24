function btn_submit(cmd) {
    if(cmd == 'exec'){
        var i;
        var c_check = 0;
        var cnt = document.forms[0].CHECK_CNT.value;
        for(i=0;i<cnt;i++){
            if(document.getElementById("CHECK"+i).checked){
                c_check = 1;
            }
        }
        if(c_check != 1){
            alert('印刷するリストを選択してください。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//オールチェック
function allCheck(div, obj) {
    var checked = "";
    if (obj.checked) {
        checked = "checked";
    }
    checkedSet(0, document.forms[0].CHECK_CNT.value, "CHECK", checked);
}

//Checkedにする
function checkedSet(chkcnt, cnt, elemchk, checkval) {
    for (; chkcnt < cnt; chkcnt++) {
        document.getElementById(elemchk + chkcnt).checked = checkval;
    }
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

