function btn_submit(cmd) {

    if (cmd == "csv") {
        if (document.forms[0].category_selected.length == 0) {
            alert('{rval MSG916}');
            return;
        }
        if ((document.forms[0].seito.checked == false) && (document.forms[0].katsudo.checked == false) && (document.forms[0].gakushu.checked == false) && (document.forms[0].tani.checked == false) && (document.forms[0].shukketsu == null || document.forms[0].shukketsu.checked == false)) {
            alert('出力する帳票を選択してください。');
            return;
        }

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
    } else if (document.forms[0].btn_print.disabled == true) {
        alert('出力する帳票を選択してください。');
    }

    else {
        for (var i = 0; i < document.forms[0].category_name.length; i++) {
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            document.forms[0].category_selected.options[i].selected = 1;
        }

        action = document.forms[0].action;
        target = document.forms[0].target;

        document.forms[0].cmd.value = "print";
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
    var kubun4 = document.forms[0].katsudo;
    var kubun5 = document.forms[0].gakushu;
    var kubun6 = document.forms[0].tani;
    var kubun7 = document.forms[0].shukketsu == null || document.forms[0].shukketsu.checked == false;
    var kubun8 = document.forms[0].online == null || document.forms[0].online.checked == false;
    var printdisabled;
    var print1disabled;
    if ((kubun1.checked == false) && (kubun4.checked == false) && (kubun5.checked == false) && (kubun6.checked == false) && kubun7 && kubun8) {
        printdisabled = true;
    } else {
        printdisabled = false;
    }
    document.forms[0].btn_print.disabled = printdisabled;
    
    print1disabled = !kubun1.checked;
    if (document.forms[0].simei) {
        document.forms[0].simei.disabled = print1disabled;
    }
    if (document.forms[0].schzip) {
        document.forms[0].schzip.disabled = print1disabled;
    }
    if (document.forms[0].schoolzip) {
        document.forms[0].schoolzip.disabled = print1disabled;
    }
    if (document.forms[0].hanki) {
        document.forms[0].hanki.disabled = !kubun5.checked;
    }
    if (document.forms[0].ib_course) {
        document.forms[0].ib_course.disabled = !kubun6.checked;
    }

}
function ClearList(OptionList) 
{
    OptionList.length = 0;
}
    
function AllClearList() 
{
        ClearList(document.forms[0].category_name);
        ClearList(document.forms[0].category_selected);
}
function move1(side,chdt)
{   
    var temp1 = [];
    var temp2 = [];
    var attribute1;
    var attribute2;
    var i;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;  
    }

    
    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        o = attribute2.options[i];
        if (chdt == 1){
            temp1[temp1.length] = {"cv": o.text.substring(9,12), "value": o.value, "text": o.text};
        }else {
            temp1[temp1.length] = {"cv": o.value, "value": o.value, "text": o.text};
        }
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {   
        o = attribute1.options[i];
        if (o.selected) {  
            if (chdt == 1){
                temp1[temp1.length] = {"cv": o.text.substring(9,12), "value": o.value, "text": o.text};
            }else {
                temp1[temp1.length] = {"cv": o.value, "value": o.value, "text": o.text};
            }
        } else {
            temp2[temp2.length] = {"value": o.value, "text": o.text};
        }
    }

    temp1.sort(cmp("cv"));

    //generating new options 
    for (i = 0; i < temp1.length; i++) {  
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i].value;
        attribute2.options[i].text =  temp1[i].text;
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i].value;
        attribute1.options[i].text =  temp2[i].text;
    }

}

function cmp(k) {
    return function (o1, o2) {
        if (o1[k] < o2[k]) {
            return -1;
        } else if (o1[k] > o2[k]) {
            return 1;
        }
        return 0;
    };
}

function moves(sides, chdt) {   
    var temp = [];
    var i;
    var o;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;  
    }
    
    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {
        o = attribute6.options[i];
        if (chdt == 1){
            temp[temp.length] = {"cv": o.text.substring(9,12), "value": o.value, "text": o.text};
        }else {
            temp[temp.length] = {"cv": o.value, "value": o.value, "text": o.text};
        }
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        o = attribute5.options[i];
        if (chdt == 1) {
            temp[temp.length] = {"cv": o.text.substring(9,12), "value": o.value, "text": o.text};
        } else {
            temp[temp.length] = {"cv": o.value, "value": o.value, "text": o.text};
        }
    }

    temp.sort(cmp("cv"));

    for (i = 0; i < temp.length; i++) {
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp[i]["value"];
        attribute6.options[i].text =  temp[i]["text"];
    }

    ClearList(attribute5);

}
