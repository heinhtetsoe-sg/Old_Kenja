function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var i;
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    var chkOutputOrder = document.getElementById("OUTPUT_ORDER");
    var outputRank = document.getElementsByName("OUTPUT_RANK")[0];
    if (chkOutputOrder && chkOutputOrder.checked) {
        outputRank.value = "2";
    } else {
        outputRank.value = "1";
    }

    for (i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move1(side) {
    var tempaa = [];
    var tempbb = [];
    var attribute1;
    var attribute2;
    var i;
    
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    for (i = 0; i < attribute2.length; i++) {
        o = attribute2.options[i];
        tempaa.push({value: o.value, text: o.text});
    }

    for (i = 0; i < attribute1.length; i++) {
        o = attribute1.options[i];
        if (o.selected) {
            tempaa.push({value: o.value, text: o.text});
        } else {
            tempbb.push({value: o.value, text: o.text});
        }
    }

    tempaa.sort(compareByValue);

    for (i = 0; i < tempaa.length; i++) {
        o = new Option();
        o.value = tempaa[i].value;
        o.text =  tempaa[i].text;
        attribute2.options[i] = o;
    }

    ClearList(attribute1);
    if (tempbb.length > 0) {
        for (i = 0; i < tempbb.length; i++) {
            o = new Option();
            o.value = tempbb[i].value;
            o.text =  tempbb[i].text;
            attribute1.options[i] = o;
        }
    }
}

function compareByValue(a, b) {
    if (a.value < b.value) {
        return -1;
    } else if (a.value > b.value) {
        return 1;
    }
    return 0;
}

function moves(sides) {
    var tempaa = [];
    var attribute5;
    var attribute6;
    var i;
    var o;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    for (i = 0; i < attribute6.length; i++) {
        o = attribute6.options[i];
        tempaa.push({value: o.value, text: o.text});
    }

    for (i = 0; i < attribute5.length; i++) {
        o = attribute5.options[i];
        tempaa.push({value: o.value, text: o.text});
    }

    tempaa.sort(compareByValue);

    for (i = 0; i < tempaa.length; i++) {
        o = new Option();
        o.value = tempaa[i].value;
        o.text =  tempaa[i].text;
        attribute6.options[i] = o;
    }

    ClearList(attribute5);

}
