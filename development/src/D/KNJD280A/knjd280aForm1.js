function btn_submit(cmd) {
    var i;
    if (classSelected().length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].NINTEI.checked) {
        if (!document.forms[0].NINTEI_SEME1.checked && !document.forms[0].NINTEI_SEME2.checked) {
            alert('{rval MSG916}\n単位認定対象を選択してください。');
            return false;
        }
    }
    if (document.forms[0].KARI.checked) {
        if (!document.forms[0].KARI_SEME1.checked && !document.forms[0].KARI_SEME2.checked) {
            alert('{rval MSG916}\n仮評定対象を選択してください。');
            return false;
        }
    }

    for (i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (i = 0; i < classSelected().length; i++) {
        classSelected().options[i].selected = 1;
    }
    document.forms[0].btn_exec.disabled = true;
    document.forms[0].btn_end.disabled = true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function kubun() {
    var dis1, dis2;
    dis1 = !document.forms[0].NINTEI.checked;
    document.forms[0].NINTEI_SEME1.disabled = dis1;
    document.forms[0].NINTEI_SEME2.disabled = dis1;
    document.forms[0].KARI.disabled = dis1;
    if (document.forms[0].KARI.disabled) {
        dis2 = document.forms[0].KARI.disabled;
    } else {
        dis2 = !document.forms[0].KARI.checked;
    }
    document.forms[0].KARI_SEME1.disabled = dis2;
    document.forms[0].KARI_SEME2.disabled = dis2;
}

window.onload = function () {
    kubun();
};

function classSelected() {
    return document.getElementsByName("CLASS_SELECTED[]")[0];
}

function ClearList(OptionList) {
    OptionList.length = 0;
}
function compareValue (a, b) {
    if (a.value < b.value) {
        return -1;
    } else if (a.value > b.value) {
        return 1;
    }
    return 0;
}
function move1(side) {
    var tempaa = [];
    var tempbb = [];
    var i;
    
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = classSelected();
    } else {
        attribute1 = classSelected();
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    for (i = 0; i < attribute2.length; i++) {
        tempaa.push({"value": attribute2.options[i].value, "text": attribute2.options[i].text});
    }

    for (i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            tempaa.push({"value": attribute1.options[i].value, "text": attribute1.options[i].text});
        } else {
            tempbb.push({"value": attribute1.options[i].value, "text": attribute1.options[i].text});
        }
    }

    tempaa.sort(compareValue);

    for (i = 0; i < tempaa.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = tempaa[i].value;
        attribute2.options[i].text =  tempaa[i].text;
    }

    ClearList(attribute1);
    if (tempbb.length > 0) {
        for (i = 0; i < tempbb.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = tempbb[i].value;
            attribute1.options[i].text =  tempbb[i].text;
        }
    }
}

function moves(sides) {
    var tempaa = [];
    var i;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = classSelected();
    } else {
        attribute5 = classSelected();
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    for (i = 0; i < attribute6.length; i++) {
        tempaa.push({"value": attribute6.options[i].value, "text": attribute6.options[i].text});
    }

    for (i = 0; i < attribute5.length; i++) {
        tempaa.push({"value": attribute5.options[i].value, "text": attribute5.options[i].text});
    }

    tempaa.sort(compareValue);

    for (i = 0; i < tempaa.length; i++) {
        attribute6.options[i] = new Option();
        attribute6.options[i].value = tempaa[i].value;
        attribute6.options[i].text =  tempaa[i].text;
    }

    ClearList(attribute5);

}
