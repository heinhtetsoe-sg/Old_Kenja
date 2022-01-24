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
        
        for (var i = 0; i < document.forms[0].elements.length; i++)
        {
            var div = document.forms[0].elements[i];
            if (div.name == 'radiodiv' && div.checked) {
                window.open('knjp070kindex.php?cmd=all_edit&radiodiv='+document.forms[0].elements[i].value,'_top');
                return;
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset() {
   result = confirm('{rval MSG106}');
   if (result == false) {
       return false;
   }
}

function ChgEdit(flg) {
    if (flg == '1') {
        window.open('knjp070kindex.php?cmd=edit1','_self');
    }else if (flg=='2') {
        window.open('knjp070kindex.php?cmd=edit2','_self');
    }
}

function selcheck(that) {

    //全角から半角
    that.value = toHankakuNum(that.value);
    //数値型へ変換
    that.value = toInteger(that.value);
    //セルが空の時０
    if(that.value == '' ){
        that.value = 0;
        return;
    }
}

function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].classyear.length==0 && document.forms[0].classmaster.length==0) {
        alert('指定範囲が正しく有りません。');
        return false;
    }
    for (var i = 0; i < document.forms[0].classyear.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'replace_update';
    if (document.forms[0].TRANSFER_SDATE.value == "") {
        alert('{rval MSG901}\n(異動期間開始日が未入力)');
        return true;
    }
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function EnableMoney()
{
    if (s_flg[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value] == '1') {
        document.forms[0].MONEY_DUE.disabled = true;
        document.forms[0].MONEY_DUE.value = '';
    } else {
        document.forms[0].MONEY_DUE.disabled = false;
    }
}

function SetMoney()
{
    //金額の設定
    if (document.forms[0].MONEY_DUE.disabled == false && m_money[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value] != undefined) {
        document.forms[0].MONEY_DUE.value = m_money[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value];
    }
    //減免事由の設定
    if (reason[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value] != undefined) {
         document.forms[0].REDUCTION_REASON.value = reason[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value];
    }
}
