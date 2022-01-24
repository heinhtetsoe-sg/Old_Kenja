function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;
    var oldcmd = document.forms[0].cmd.value;

    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = oldcmd;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move1(disp, side) {
    var temp1 = [], temp2 = [];
    var attribute1, attribute2;
    
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        o = attribute2.options[i];
        temp1.push({"value": o.value, "text": o.text});
    }

    for (var i = 0; i < attribute1.length; i++) {
        o = attribute1.options[i];
        if ( o.selected ) {
            temp1.push({"value": o.value, "text": o.text});
        } else {
            temp2.push({"value": o.value, "text": o.text});
        }
    }

    temp1.sort(compareByValue(disp));

    for (var i = 0; i < temp1.length; i++) {
        o = temp1[i];
        attribute2.options[i] = new Option();
        attribute2.options[i].value = o.value;
        attribute2.options[i].text =  o.text;
    }

    ClearList(attribute1);
    for (var i = 0; i < temp2.length; i++) {
        o = temp2[i];
        attribute1.options[i] = new Option();
        attribute1.options[i].value = o.value;
        attribute1.options[i].text =  o.text;
    }
}

function compareByValue(disp) {

    return function (a, b) {
        var av;
        var bv;
        if (disp == '2') {
            av = a.text;
            bv = b.text;
        } else {
            av = a.value;
            bv = b.value;
        }
        if (av < bv) {
            return -1;
        } else if (av > bv) {
            return 1;
        }
        return 0;
    }
}

function moves(disp, sides) {
    var temp1 = [];
    var i, o;
    var attribute5, attribute6;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    for (i = 0; i < attribute6.length; i++) {
        o = attribute6.options[i];
        temp1.push({"value": o.value, "text": o.text});
    }

    for (i = 0; i < attribute5.length; i++) {
        o = attribute5.options[i];
        temp1.push({"value": o.value, "text": o.text});
    }

    temp1.sort(compareByValue(disp));

    for (i = 0; i < temp1.length; i++) {
        o = temp1[i];
        attribute6.options[i] = new Option();
        attribute6.options[i].value = o.value;
        attribute6.options[i].text =  o.text;
    }

    ClearList(attribute5);

}
