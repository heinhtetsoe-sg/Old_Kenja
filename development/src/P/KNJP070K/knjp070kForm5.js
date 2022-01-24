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

function SetMoney()
{
    //金額の設定
    if (s_money[document.forms[0].EXPENSE_S_CD.value] != undefined) {
        document.forms[0].MONEY_DUE.value = s_money[document.forms[0].EXPENSE_S_CD.options[document.forms[0].EXPENSE_S_CD.selectedIndex].value];
    }
}
