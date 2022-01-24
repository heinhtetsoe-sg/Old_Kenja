function btn_submit(cmd) {
//    var attribute3 = document.forms[0].selectdata;
//    attribute3.value = "";
//    sep = "";
//    for (var i = 0; i < document.forms[0].category_name.length; i++) {
//        document.forms[0].category_name.options[i].selected = 0;
//    }
//
//    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
//        document.forms[0].category_selected.options[i].selected = 1;
//        var sel = document.forms[0].category_selected.options[i].value.split('-');
//        if (sel.length < 4) {
//            attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
//        } else {
//            attribute3.value = attribute3.value + sep + sel[3];
//        }
//        sep = ",";
//    }


    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function includes(l, e) {
    if (l.include) {
        return l.include(e);
    } else if (l.includes) {
        return l.includes(e);
    }
    return false;
}

function check1(o) {
    var en = [];
    var dis = [];
    if (o.id == "REGDDIV1") {
        if (o.checked) {
            en = ["ATTEND_START_DATE", "ATTEND_START_DATE_btn_calen", "ATTEND_END_DATE", "ATTEND_END_DATE_btn_calen", "NOT_PRINT_RISHUTYU", "PRINT_RI", "PRINT_SOTSU"];
        }
    } else if (o.id == "REGDDIV2") {
        if (o.checked) {
            dis = ["ATTEND_START_DATE", "ATTEND_START_DATE_btn_calen", "ATTEND_END_DATE", "ATTEND_END_DATE_btn_calen", "NOT_PRINT_RISHUTYU", "PRINT_RI", "PRINT_SOTSU"];
        }
    } else if (o.id == "NOT_PRINT_CERTIFNO") {
        if (o.checked) {
            dis = ["USE_CERTIFNO_START", "CERTIFNO_START"];
        } else {
            dis = ["USE_CERTIFNO_START"];
        }
    } else if (o.id == "USE_CERTIFNO_START") {
        en = ["CERTIFNO_START"];
        dis = ["NOT_PRINT_CERTIFNO"];
    } else if (o.id == "NOT_PRINT_RISHUTYU") {
        dis = ["PRINT_RI"];
    } else if (o.id == "PRINT_RI") {
        dis = ["NOT_PRINT_RISHUTYU"];
    } else if (o.id == "PRINT_SOTSU") {
        en = ["PRINT_GRD_DATE", "PRINT_GRD_DATE_btn_calen"];
    }
    en.forEach(function (id) {
        var el = document.getElementById(id);
        if (el) {
            el.disabled = !o.checked;
        }
    });
    dis.forEach(function (id) {
        var el = document.getElementById(id);
        if (el) {
            if (el.checked) {
                el.checked = !o.checked;
            }
            el.disabled = o.checked;
        }
    });
}

//印刷
function newwin(SERVLET_URL) {
    var i;
    var action, target;
    var optionsvalue = {};

    if (document.forms[0].ATTEND_START_DATE.value == '' || document.forms[0].ATTEND_END_DATE.value == '') {
        alert('{rval MSG304}\n出欠集計範囲');
        return false;
    }
    if (document.forms[0].ATTEND_START_DATE.value < document.forms[0].ATTEND_START_DATE_MIN.value) {
        alert('{rval MSG901}\n出欠集計範囲開始日');
        return false;
    }
    if (document.forms[0].ATTEND_END_DATE_MAX.value < document.forms[0].ATTEND_END_DATE.value) {
        alert('{rval MSG901}\n出欠集計範囲終了日');
        return false;
    }

    if(document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    for (i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].selected = 1;
        var sel = document.forms[0].category_selected.options[i].value.split('-');
        if (sel.length > 0) {
            optionsvalue[sel[1]] = document.forms[0].category_selected.options[i].value;
            document.forms[0].category_selected.options[i].value = sel[1];
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

    for (i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].value = optionsvalue[document.forms[0].category_selected.options[i].value];
    }

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
function cmp(a, b) {
    if (a.value < b.value) {
        return -1;
    } else if (a.value > b.value) {
        return 1;
    }
    return 0;
}

function move1(side) {
    var temp1 = [];
    var temp2 = [];
    var attribute1;
    var attribute2;
    var i;
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
    for (i = 0; i < attribute2.length; i++) {
        o = attribute2.options[i];
        temp1.push({ "value" : o.value, "text" : o.text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        o = attribute1.options[i];
        if (attribute1.options[i].selected) {
            temp1.push({ "value" : o.value, "text" : o.text});
        } else {
            temp2.push({ "value" : o.value, "text" : o.text});
        }
    }

    temp1.sort(cmp);

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i].value;
        attribute2.options[i].text =  temp1[i].text;
    }

    //generating new options
    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i].value;
            attribute1.options[i].text =  temp2[i].text;
        }
    }
}

function moves(sides) {
    var temp1 = [];
    var i;
    var attribute5;
    var attribute6;
    
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
        o = attribute6[i];
        temp1.push({ "value" : o.value, "text" : o.text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        o = attribute5[i];
        temp1.push({ "value" : o.value, "text" : o.text});
    }

    temp1.sort(cmp);

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp1[i].value;
        attribute6.options[i].text =  temp1[i].text;
    }

    //generating new options
    ClearList(attribute5, attribute5);
}

function setTextAreaCheckId(elName) {
    var e = document.getElementsByName(elName)[0];
    if (e && !e.id) {
        e.id = elName;
    }
    ["_KETA", "_GYO", "_STAT"].forEach(function(sfx) {
        var sfxel = document.getElementsByName(elName + sfx)[0];
        if (sfxel) {
            sfxel.id = elName + sfx;
        }
    });
}

function setDateId(dateElName) {
    var dateElement = document.getElementsByName(dateElName)[0];
    if (!dateElement) {
        return;
    }
    dateElement.id = dateElName;
    var btn = dateElement.nextSibling;
    if (btn && btn.name == "btn_calen") {
        btn.id = dateElName + "_btn_calen";
    }
}

window.addEventListener('load', function() {
    ["BIKO"].forEach(setTextAreaCheckId);
    ["ATTEND_START_DATE", "ATTEND_END_DATE", "PRINT_GRD_DATE"].forEach(setDateId);
    check1(document.getElementById("NOT_PRINT_CERTIFNO"));
    check1(document.getElementById("NOT_PRINT_RISHUTYU"));
    check1(document.getElementById("REGDDIV1"));
    check1(document.getElementById("REGDDIV2"));
    check1(document.getElementById("PRINT_SOTSU"));
});

