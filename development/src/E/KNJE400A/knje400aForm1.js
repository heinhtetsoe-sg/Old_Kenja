function btn_submit(cmd) {
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == "notify" || cmd == "update") {
        if (document.forms[0].DOC_NUMBER.value == "") {
            alert('{rval MSG304}');
            return false;
        }
    }
    if (cmd == 'notify'){
        if (!confirm('{rval MSG108}' + '\n学校へ通達後は修正することができません。')) {
            return false;
        }
    }
    if (cmd == "add" || cmd == "update") {
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG916}'+ '\n学校を選択して下さい。');
            return false;
        }
    }
    attribute = document.forms[0].selectdata;
    attribute.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        attribute.value = attribute.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//教育委員会チェック
function OnEdboardError()
{
    alert('{rval MSG300}');
    closeWin();
}

function prgDisabled(obj)
{
    if (obj.checked) {
        document.forms[0].REQUEST_ANSWER_PRG.disabled = false;
    } else {
        document.forms[0].REQUEST_ANSWER_PRG.disabled = true;
    }
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
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {  
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
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
        tempaa[i] = temp1[i].text.substring(8, 4) + "," + i;
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
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;  
    }

    for (i = 0; i < attribute6.length; i++) {
        temp5[temp5.length] = { value: attribute6.options[i].value, text: attribute6.options[i].text};
    }

    for (i = 0; i < attribute5.length; i++) {
        temp5[temp5.length] = { value: attribute5.options[i].value, text: attribute5.options[i].text};
    }

    for (i = 0; i < temp5.length; i++) {
        tempaa[i] = temp5[i].text.substring(8, 4) + "," + i;
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
