function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var i;
    if (document.forms[0].SCHREG_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    for (i = 0; i < document.forms[0].SCHREG_NAME.length; i++) {
        document.forms[0].SCHREG_NAME.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].SCHREG_SELECTED.length; i++) {
        document.forms[0].SCHREG_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function move1(side)
{   
    var temp1 = [];
    var temp2 = [];
    var tempaa = [];
    var attribute1;
    var attribute2;
    var i, j;
    if (side == "left") {
        attribute1 = document.forms[0].SCHREG_NAME;
        attribute2 = document.forms[0].SCHREG_SELECTED;
    } else {  
        attribute1 = document.forms[0].SCHREG_SELECTED;
        attribute2 = document.forms[0].SCHREG_NAME;
    }

    for (i = 0; i < attribute2.length; i++) {  
        temp1[temp1.length] = { value : attribute2.options[i].value, text : attribute2.options[i].text};
    }

    for (i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            temp1[temp1.length] = { value : attribute1.options[i].value, text : attribute1.options[i].text};
        } else {
            temp2[temp2.length] = { value : attribute1.options[i].value, text : attribute1.options[i].text}; 
        }
    }

    for (i = 0; i < temp1.length; i++) {
        tempaa[i] = temp1[i].text.substring(9, 12) + "," + i;
    }

    tempaa.sort();

    for (i = 0; i < temp1.length; i++) {
        j = tempaa[i].split(',')[1];

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[j].value;
        attribute2.options[i].text =  temp1[j].text;
    }

    ClearList(attribute1, attribute1);

    for (var i = 0; i < temp2.length; i++) {   
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i].value;
        attribute1.options[i].text =  temp2[i].text;
    }

}

function moves(sides)
{   
    var temp5 = [];
    var tempaa = [];
    var attribute5;
    var attribute6;
    var i, j;
    if (sides == "left") {
        attribute5 = document.forms[0].SCHREG_NAME;
        attribute6 = document.forms[0].SCHREG_SELECTED;
    } else {
        attribute5 = document.forms[0].SCHREG_SELECTED;
        attribute6 = document.forms[0].SCHREG_NAME;  
    }

    for (i = 0; i < attribute6.length; i++) {
        temp5[temp5.length] = { value: attribute6.options[i].value, text: attribute6.options[i].text};
    }

    for (i = 0; i < attribute5.length; i++) {
        temp5[temp5.length] = { value: attribute5.options[i].value, text: attribute5.options[i].text};
    }

    for (i = 0; i < temp5.length; i++) {
        tempaa[i] = temp5[i].text.substring(9, 12) + "," + i;
    }

    tempaa.sort();

    for (i = 0; i < temp5.length; i++) {
        j = tempaa[i].split(',')[1];

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[j].value;
        attribute6.options[i].text =  temp5[j].text;
    }

    ClearList(attribute5, attribute5);

}
