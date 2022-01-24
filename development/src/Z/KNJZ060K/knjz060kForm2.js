function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//ラジオボタンの値によって編集可、編集不可に切り替える
function changetext(obj,s_money,m_money)
{
    var val = obj.value;
    if(val == "1"){
        if (s_money == false) {
            document.forms[0].EXPENSE_M_MONEY.value = "";
        } else {
            document.forms[0].EXPENSE_M_MONEY.value = s_money;
        }
        document.forms[0].EXPENSE_M_MONEY.disabled = "disabled";
    }else{
        if (m_money == false) {
            document.forms[0].EXPENSE_M_MONEY.value = "";
        } else {
            document.forms[0].EXPENSE_M_MONEY.value = m_money;
        }
        document.forms[0].EXPENSE_M_MONEY.disabled = "";
    }
    return;
}
