function btn_submit(cmd) {
    if (cmd == "update") {
        if (document.forms[0].CHK_CLUBCD.value == "") {
            alert("{rval MSG308}");
            return false;
        }

        setUpdSelect = document.forms[0].selectdata;
        setUpdSelect.value = "";

        sep = "";
        for (var i = 0; i < document.forms[0].LEFT_KIND.length; i++) {
            setUpdSelect.value = setUpdSelect.value + sep + document.forms[0].LEFT_KIND.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function EnableBtns(e) {
    document.forms[0].btn_add.disabled = false;
    document.forms[0].btn_udpate.disabled = false;
    document.forms[0].btn_reset.disabled = false;
}
//NO001
function Btn_reset(cmd) {
    result = confirm("{rval MSG107}");
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function move1(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute1;
    var attribute2;
    var attribute3;
    var tmpcmb1;
    var tmpcmb2;
    var tmpcmb3;

    if (side == "right" || side == "sel_del_all" || side == "sel_del_all2" || side == "sel_del_all3") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                y = current1++;
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
            } else {
                y = current2++;
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[tempa[y]] = temp1[y];
        }
    }
    if (sort) {
        tempa = tempa.sort();
        for (var i = 0; i < tempa.length; i++) {
            temp1[i] = a[tempa[i]];
        }
    }

    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text = tempa[i];
    }

    ClearList(attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function jmsg(msg1, msg2) {
    alert("{rval MSG300}" + "\n" + msg1 + "\n" + msg2);
}
