function btn_submit(cmd) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].selected = 1;
        var sel = document.forms[0].category_selected.options[i].value.split('-');
        if (sel.length < 4) {
            attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
        } else {
            attribute3.value = attribute3.value + sep + sel[3];
        }
        sep = ",";
    }


    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

    if(document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    for (var i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].selected = 1;
        var sel = document.forms[0].category_selected.options[i].value.split('-');
        if (sel.length > 1) {
            document.forms[0].category_selected.options[i].value = sel[3];
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
        attribute = document.forms[0].category_name;
        ClearList(attribute,attribute);
        attribute = document.forms[0].category_selected;
        ClearList(attribute,attribute);
}
function move1(side) {
    var tmp1 = [];
    var tmp2 = [];
    var opt;
    var i;
    var attribute1, attribute2;

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
        opt = attribute2.options[i];
        tmp1.push({"value": opt.value, "text": opt.text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        opt = attribute1.options[i];
        if (opt.selected) {
            tmp1.push({"value": opt.value, "text": opt.text});
        } else {
            tmp2.push({"value": opt.value, "text": opt.text});
        }
    }

    tmp1.sort(compareByValue);

    //generating new options
    for (i = 0; i < tmp1.length; i++) {
        opt = new Option();
        opt.value = tmp1[i].value;
        opt.text = tmp1[i].text;
        attribute2.options[i] = opt;
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < tmp2.length; i++) {
        opt = new Option();
        opt.value = tmp2[i].value;
        opt.text = tmp2[i].text;
        attribute1.options[i] = opt;
    }
}

function compareByValue(o1, o2) {
    if (o1.value < o2.value) {
        return -1;
    } else if (o1.value > o2.value) {
        return 1;
    }
    return 0;
}

function moves(sides) {
    var tmp = [];
    var i;
    var opt;
    var attribute5, attribute6;

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
        opt = attribute6.options[i];
        tmp.push({"value": opt.value, "text": opt.text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        opt = attribute5.options[i];
        tmp.push({"value": opt.value, "text": opt.text});
    }

    tmp.sort(compareByValue);

    //generating new options
    for (i = 0; i < tmp.length; i++) {
        opt = new Option();
        opt.value = tmp[i].value;
        opt.text = tmp[i].text;
        attribute6.options[i] = opt;
    }

    //generating new options
    ClearList(attribute5);
}

