function btn_submit(cmd) {
    if (cmd == 'update') {
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        if (document.forms[0].category_selected.length == 0) {
            alert('{rval MSG916}');
            return;
        } else if (document.forms[0].btn_print.disabled) {
            alert('出力する帳票を選択してください。');
            return;
        } else {
            for (var i = 0; i < document.forms[0].category_selected.length; i++)
            {
                document.forms[0].category_selected.options[i].selected = 1;
                attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
                sep = ",";
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkRisyu() {
    if (document.forms[0].RISYUTOUROKU[0].checked && document.forms[0].MIRISYU[1].checked) {
        alert('履修登録のみ科目が出力される状態になっています。');
        document.forms[0].MIRISYU[0].checked = true;
        document.forms[0].MIRISYU[1].checked = false;
        document.forms[0].RISYU[0].checked = true;
        document.forms[0].RISYU[1].checked = false;
    }
    if (document.forms[0].MIRISYU[0].checked && document.forms[0].RISYU[1].checked) {
        alert('未履修科目が出力される状態になっています。');
        document.forms[0].RISYU[0].checked = true;
        document.forms[0].RISYU[1].checked = false;
    }
}

function newwin(SERVLET_URL){

    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
    } else if (document.forms[0].btn_print.disabled) {
        alert('出力する帳票を選択してください。');
    } else {
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";

        for (var i = 0; i < document.forms[0].category_name.length; i++) {
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            document.forms[0].category_selected.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
            sep = ",";
        }

        action = document.forms[0].action;
        target = document.forms[0].target;

        document.forms[0].action = SERVLET_URL +"/KNJA";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}

function kubun()
{
    var kubun1 = document.forms[0].seito;
    var kubun2 = document.forms[0].simei;
    var kubun4 = document.forms[0].katsudo;
    var kubun5 = document.forms[0].gakushu;
    var kubun6 = document.forms[0].tani;
    var kubun8 = null == document.forms[0].online || document.forms[0].online.checked == false;
    var printdisabled;
    var print1disabled;

    if ((kubun1.checked == false) && (kubun4.checked == false) && (kubun5.checked == false) && (kubun6 && kubun6.checked == false) && kubun8) {
        printdisabled = true;
    } else {
        printdisabled = false;
    }
    document.forms[0].btn_print.disabled = printdisabled;
    
    if (kubun1.checked == true) {
        print1disabled = false;
    } else {
        print1disabled = true;
    }
//    document.forms[0].koseki.disabled = print1disabled;
    document.forms[0].simei.disabled = print1disabled;
    if (document.forms[0].schzip) {
        document.forms[0].schzip.disabled = print1disabled;
    }
//    document.forms[0].addr2.disabled = print1disabled;
    if (document.forms[0].schoolzip) {
        document.forms[0].schoolzip.disabled = print1disabled;
    }

}
function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].category_name;
        ClearList(attribute,attribute);
        attribute = document.forms[0].category_selected;
        ClearList(attribute,attribute);
}
function move1(side, chdt)
{   
    var temp1 = [];
    var temp2 = [];
    var tempSort = [];
    var src, dest;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        src = document.forms[0].category_name;
        dest = document.forms[0].category_selected;
    } else {
        src = document.forms[0].category_selected;
        dest = document.forms[0].category_name;  
    }

    
    for (i = 0; i < dest.length; i++) {
        temp1[temp1.length] = dest.options[i];
    }

    for (i = 0; i < src.length; i++)
    {   
        if (src.options[i].selected)
        {  
            temp1[temp1.length] = src.options[i];
        } else {
            temp2[temp2.length] = src.options[i]; 
        }
    }

    for (i = 0; i < temp1.length; i++) {
        tempSort[i] = { value: temp1[i].value, idx : i};
    }
    tempSort.sort(function (a, b) { if (a.value < b.value) return -1; if (a.value > b.value) return 1; return 0;});

    for (i = 0; i < temp1.length; i++)
    {  
        dest.options[i] = new Option();
        dest.options[i].value = temp1[tempSort[i].idx].value;
        dest.options[i].text =  temp1[tempSort[i].idx].text;
    }

    //generating new options
    ClearList(src, src);
    for (var i = 0; i < temp2.length; i++) {
        src.options[i] = new Option();
        src.options[i].value = temp2[i].value;
        src.options[i].text =  temp2[i].text;
    }

}


function moves(sides, chdt)
{   
    var temp = [];
    var tempSort = [];
    var i;
    var src, dest;
    
    //assign what select attribute treat as src and dest
    if (sides == "left") {
        src = document.forms[0].category_name;
        dest = document.forms[0].category_selected;
    } else {
        src = document.forms[0].category_selected;
        dest = document.forms[0].category_name;  
    }

    
    for (i = 0; i < dest.length; i++) {
        temp[temp.length] = dest.options[i];
    }
    for (i = 0; i < src.length; i++) {
        temp[temp.length] = src.options[i];
    }

    for (i = 0; i < temp.length; i++) {
        tempSort[i] = { value: temp[i].value, idx : i};
    }
    tempSort.sort(function (a, b) { if (a.value < b.value) return -1; if (a.value > b.value) return 1; return 0;});

    for (i = 0; i < temp.length; i++) {
        dest.options[i] = new Option();
        dest.options[i].value = temp[tempSort[i].idx].value;
        dest.options[i].text =  temp[tempSort[i].idx].text;
    }

    ClearList(src,src);

}
