function btn_submit(cmd) {
   
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }           
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "all_edit") {
        window.open('knjp121kindex.php?cmd=all_edit&mode=ALL', '_top');
        return;
    }
    document.forms[0].TMP_PAID_MONEY.value = document.forms[0].PAID_MONEY.value;
    document.forms[0].TMP_REPAY_MONEY.value = document.forms[0].REPAY_MONEY.value;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function money_check1()
{
    if (document.forms[0].PAID_MONEY.value != "" && document.forms[0].MONEY_DUE.value != "") {
        if (parseInt(document.forms[0].PAID_MONEY.value, 10) != parseInt(document.forms[0].MONEY_DUE.value, 10)) {
            alert('入金必要額と一致していません。');
            return true;
        }
    }
    return;
}

function money_check2(obj)
{
    paid_money  = (document.forms[0].MONEY_DUE.value != "")  ? parseInt(document.forms[0].MONEY_DUE.value, 10) : 0;
    repay_money = (document.forms[0].REPAY_MONEY.value != "")  ? parseInt(document.forms[0].REPAY_MONEY.value, 10) : 0;

    if (paid_money < repay_money) {
        alert('{rval MSG901}'+'\n'+'必要額、返金額');
        obj.value = '';
        return true;
    }
    return;
}
function init(){
    try{    
        parent.top_frame.document.getElementById("btn_end").style.display = "none";
    }catch(e){
    }
}
window.onload = init;