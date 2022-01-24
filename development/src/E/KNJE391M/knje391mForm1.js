/* Add by PP for CurrentCursor 2020-01-10 start */
window.onload = new function () {
    setTimeout(function () {
        if (sessionStorage.getItem("KNJE391MForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE391MForm1_CurrentCursor")).focus();
        }
    }, 30);
};

function current_cursor(para) {
    sessionStorage.setItem("KNJE391MForm1_CurrentCursor", para);
}
/* Add by PP for CurrentCursor 2020-01-17 end */

function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2020-01-10 start */
    if (sessionStorage.getItem("KNJE391MForm1_CurrentCursor") != null) {
        document.getElementById(sessionStorage.getItem("KNJE391MForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-01-17 end */
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var i;
    var bk = {};

    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
    } else if (document.forms[0].PRINT_A.checked == false &&
        document.forms[0].PRINT_B.checked == false &&
        document.forms[0].PRINT_C.checked == false &&
        document.forms[0].PRINT_D.checked == false &&
        document.forms[0].PRINT_E.checked == false &&
        document.forms[0].PRINT_F.checked == false &&
        document.forms[0].PRINT_G.checked == false &&
        document.forms[0].PRINT_H.checked == false &&
        document.forms[0].PRINT_I.checked == false
    ) {
        alert('出力する帳票を選択してください。');
    } else {
        for (i = 0; i < document.forms[0].category_name.length; i++) {
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (i = 0; i < document.forms[0].category_selected.length; i++) {
            document.forms[0].category_selected.options[i].selected = 1;
            bk[i] = document.forms[0].category_selected.options[i].value;
            document.forms[0].category_selected.options[i].value = bk[i].split("-")[1];
        }

        action = document.forms[0].action;
        target = document.forms[0].target;

        // url = location.hostname;
        // document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJE";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        for (i = 0; i < document.forms[0].category_selected.length; i++) {
            document.forms[0].category_selected.options[i].value = bk[i];
        }
        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}

function kubun() {
    var flag3 = false;
    var kubun1 = document.forms[0].PRINT_A;
    var kubun4 = document.forms[0].PRINT_B;
    var kubun5 = document.forms[0].PRINT_C;
    var kubun6 = document.forms[0].PRINT_D;
    var kubunE = document.forms[0].PRINT_E;
    var kubunF = document.forms[0].PRINT_F;
    var kubunG = document.forms[0].PRINT_G;
    var kubunH = document.forms[0].PRINT_H;
    var kubunI = document.forms[0].PRINT_I;

    if ((kubun1.checked == false) && (kubun4.checked == false) && (kubun5.checked == false) && (kubun6.checked == false) &&
        (kubunE.checked == false) && (kubunF.checked == false) && (kubunG.checked == false) && (kubunH.checked == false) && 
        (kubunI.checked == false)) {
        flag3 = true;
    } else {
        flag3 = false;
    }
    document.forms[0].btn_print.disabled = flag3;
    printA_kubun();
    printD_kubun();
}
window.onload = kubun;

function printA_kubun() {
    var flag3 = false;
    var kubun1 = document.forms[0].PRINT_A;

    if (kubun1.checked == false) {
        flag3 = true;
    } else {
        flag3 = false;
    }
    document.forms[0].A_OUTPUT1.disabled = flag3;
    document.forms[0].A_OUTPUT2.disabled = flag3;
    document.forms[0].A_OUTPUT3.disabled = flag3;
    document.forms[0].A_OUTPUT4.disabled = flag3;
    document.forms[0].A_OUTPUT5.disabled = flag3;
}
function printD_kubun() {
    var flag3 = false;
    var kubun1 = document.forms[0].PRINT_D;

    if (kubun1.checked == false) {
        flag3 = true;
    } else {
        flag3 = false;
    }
    document.forms[0].D_OUTPUT1.disabled = flag3;
    document.forms[0].D_OUTPUT2.disabled = flag3;
    document.forms[0].D_OUTPUT3.disabled = flag3;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].category_name;
    ClearList(attribute, attribute);
    attribute = document.forms[0].category_selected;
    ClearList(attribute, attribute);
}

function move1(side, chdt) {
    var tempa = [];
    var tempb = [];
    var temp3 = false;
    var attribute1;
    var attribute2;
    var i;
    var id, time, label;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;

        /* Add by PP for PC-Talker 読み  2020-01-10 start */
        document.getElementById('td_CATEGORY_SELECTED').focus();
        id = 'td_CATEGORY_SELECTED';
        label = (attribute1.length == 0) ? "移動ための項目がないので移動失敗です" : "";
        document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else if (label == "選択した項目を移動しました") {
            time = 2000;
        } else {
            time = 3000;
        }

        setTimeout(function () {
            document.getElementById('btn_left1').focus();
            document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", "");
        }, time);
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;

        document.getElementById('td_CATEGORY_NAME').focus();
        id = 'td_CATEGORY_NAME';
        label = (attribute1.length == 0) ? "移動ための項目がないので移動失敗です" : "";
        document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else if (label == "選択した項目を移動しました") {
            time = 2000;
        } else {
            time = 3000;
        }

        setTimeout(function () {
            document.getElementById('btn_right1').focus();
            document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", "");
        }, time);
    }

    /* Add by PP for PC-Talker 読み 2020-01-17 end */
    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        tempa[tempa.length] = { "value": attribute2.options[i].value, "text": attribute2.options[i].text };
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {

        if (attribute1.options[i].selected) {
            temp3 = true;
            tempa[tempa.length] = { "value": attribute1.options[i].value, "text": attribute1.options[i].text };
        } else {
            tempb[tempb.length] = { "value": attribute1.options[i].value, "text": attribute1.options[i].text };
        }
    }
    if (attribute1.length > 0 && temp3) {
        document.getElementById(id).setAttribute("aria-label", "選択した項目を移動しました");
    } else if (attribute1.length > 0) {
        document.getElementById(id).setAttribute("aria-label", "項目を選択しないので移動失敗です");
    }
    tempa.sort(compareSchreg);

    //generating new options
    for (i = 0; i < tempa.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = tempa[i].value;
        attribute2.options[i].text = tempa[i].text;
    }

    //generating new options
    ClearList(attribute1, attribute1);
    for (i = 0; i < tempb.length; i++) {
        attribute1.options[i] = new Option();
        attribute1.options[i].value = tempb[i].value;
        attribute1.options[i].text = tempb[i].text;
    }
}

function compareSchreg(s1, s2) {
    var a1 = s1.value.split("-")[0];
    var a2 = s2.value.split("-")[0];
    if (a1 < a2) {
        return -1;
    } else if (a1 > a2) {
        return 1;
    }
    return 0;
}

function moves(sides, chdt) {
    var temp5 = [];
    var i;
    var attribute5;
    var attribute6;
    var label, time;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;

        /* Add by PP for PC-Talker 読み  2020-01-10 start */
        document.getElementById('td_CATEGORY_SELECTED').focus();
        label = (attribute5.length == 0) ? "移動ための項目がないので移動失敗です" : "すべての項目を移動しました";
        document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else {
            time = 2000;
        }

        setTimeout(function () {
            document.getElementById('btn_lefts').focus();
            document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", "");
        }, time);

    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;

        document.getElementById('td_CATEGORY_NAME').focus();
        label = (attribute5.length == 0) ? "移動ための項目がないので移動失敗です" : "すべての項目を移動しました";
        document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else {
            time = 2000;
        }
        setTimeout(function () {
            document.getElementById('btn_rights').focus();
            document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", "");
        }, time);

    }

    /* Add by PP for PC-Talker 読み  2020-01-17 end */
    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {
        temp5[temp5.length] = { "value": attribute6.options[i].value, "text": attribute6.options[i].text };
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        temp5[temp5.length] = { "value": attribute5.options[i].value, "text": attribute5.options[i].text };
    }

    temp5.sort(compareSchreg);

    for (i = 0; i < temp5.length; i++) {
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[i].value;
        attribute6.options[i].text = temp5[i].text;
    }

    ClearList(attribute5, attribute5);

}
