function btn_submit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }

    if (cmd == 'all_edit2') {
        document.forms[0].MONEY_DUE.value = '';
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}

function check_all(obj){
    var ii = 0;
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "RCHECK"+ii){
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}

function doSubmit(cmd)
{      
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_select.length==0) {
        alert('{rval MSG304}');
        return false;
    }
    if (cmd == 'all_delete' && !confirm('{rval MSG103}')){
        return true;
    }

    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}

function add()
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value;
    
    if (w == "")
        return false;
        
    for (var i = 0; i < v; i++)
    {   
        if (w == document.forms[0].year.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].year.options[v] = new Option();
    document.forms[0].year.options[v].value = w;
    document.forms[0].year.options[v].text = w;
    
    for (var i = 0; i < document.forms[0].year.length; i++)
    {  
        temp1[i] = document.forms[0].year.options[i].value;
        tempa[i] = document.forms[0].year.options[i].text;
    } 
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();
    
    //generating new options
    ClearList(document.forms[0].year,document.forms[0].year);
    if (temp1.length>0)
    {   
        for (var i = 0; i < temp1.length; i++)
        {   
            document.forms[0].year.options[i] = new Option();
            document.forms[0].year.options[i].value = temp1[i];
            document.forms[0].year.options[i].text =  tempa[i];
            if(w==temp1[i]){
                document.forms[0].year.options[i].selected=true;
            }
        }
    } 
    temp_clear();
      
}

function EnableMoney()
{
    if (document.forms[0].EXPENSE_M_CD == undefined)
        return;
    if (s_flg[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value] == '1') {
        document.forms[0].MONEY_DUE.disabled = true;
        document.forms[0].MONEY_DUE.value = '';
    } else {
        document.forms[0].MONEY_DUE.disabled = false;
    }
}

function SetMoney1()
{
    if (document.forms[0].MONEY_DUE.disabled == false && money[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value] != undefined) {
        document.forms[0].MONEY_DUE.value = money[document.forms[0].EXPENSE_M_CD.options[document.forms[0].EXPENSE_M_CD.selectedIndex].value];
    }
    document.forms[0].REDUCTION_REASON.value = '';
}

function SetMoney2()
{
    if (money[document.forms[0].EXPENSE_S_CD.options[document.forms[0].EXPENSE_S_CD.selectedIndex].value] != undefined) {
        document.forms[0].MONEY_DUE.value = money[document.forms[0].EXPENSE_S_CD.options[document.forms[0].EXPENSE_S_CD.selectedIndex].value];
    }
}
