function btn_submit(cmd) {
    if (cmd == 'update') {
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        if (document.forms[0].category_selected.length == 0) {
            alert('{rval MSG916}');
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

function newwin(SERVLET_URL){
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";

    if (document.forms[0].category_selected.length == 0)
    {
        alert('{rval MSG916}');
    } else {
        var sep = "";
        for (var i = 0; i < document.forms[0].category_selected.length; i++)
        {
            document.forms[0].category_selected.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
            sep = ",";
        }

        for (var i = 0; i < document.forms[0].category_name.length; i++)
        {  
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].category_selected.length; i++)
        {  
            document.forms[0].category_selected.options[i].selected = 1;
        }

        action = document.forms[0].action;
        target = document.forms[0].target;

    //      url = location.hostname;
    //      document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
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
    var kubun3 = document.forms[0].gakushu;
    var kubun4 = document.forms[0].koudo;
    var kubun8 = document.forms[0].online == null || document.forms[0].online.checked == false;

    var nocheck = (kubun1.checked == false) && (kubun3.checked == false) && (kubun4.checked == false) && kubun8;
    document.forms[0].btn_print.disabled = nocheck;
    
    document.forms[0].simei.disabled = !kubun1.checked;
    document.forms[0].schzip.disabled = !kubun1.checked;
    document.forms[0].schoolzip.disabled = !kubun1.checked;
    if (document.forms[0].inei_print) {
        document.forms[0].inei_print.disabled = !kubun1.checked;
    }
    if (document.forms[0].inei_print2) {
        document.forms[0].inei_print2.disabled = !kubun1.checked;
    }
    document.forms[0].mongon.disabled = !kubun4.checked;
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

function cmp(chdt) {
    if (chdt == 1) {
        return function(a, b) {
            var a = a.text.substring(9, 12);
            var b = b.text.substring(9, 12);
            if (a < b) {
                return -1;
            } else if (a > b) {
                return 1;
            }
            return 0;
        };
    }
    return function(a, b) {
        if (a.value < b.value) {
            return -1;
        } else if (a.value > b.value) {
            return 1;
        }
        return 0;
    };
}

function move1(side,chdt) {   
    var temp1 = [];
    var temp2 = [];
    var o;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {  
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {  
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;  
    }
    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {  
        o = attribute2.options[i];
        temp1.push({"value" : o.value, "text" : o.text});
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {   
        o = attribute1.options[i];
        if ( o.selected ) {  
            temp1.push({"value" : o.value, "text" : o.text});
        } else {  
            temp2.push({"value" : o.value, "text" : o.text});
        }
    }

    temp1.sort(cmp(chdt));

    //generating new options
    for (var i = 0; i < temp1.length; i++) {  
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i].value;
        attribute2.options[i].text =  temp1[i].text;
    }

    //generating new options
    ClearList(attribute1,attribute1);
    for (var i = 0; i < temp2.length; i++) {   
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i].value;
        attribute1.options[i].text =  temp2[i].text;
    }
}

function moves(sides,chdt) {   
    var temp = [];
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
    for (var i = 0; i < attribute6.length; i++) {  
        o = attribute6.options[i];
        temp.push({"value" : o.value, "text" : o.text});
    }
    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {   
        o = attribute5.options[i];
        temp.push({"value" : o.value, "text" : o.text});
    }

    temp.sort(cmp(chdt));

    //generating new options
    for (var i = 0; i < temp.length; i++) {  
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp[i].value;
        attribute6.options[i].text =  temp[i].text;
    }

    //generating new options
    ClearList(attribute5,attribute5);
}

window.addEventListener("load", function(e) {
    kubun();
});
