/* Add by PP for CurrentCursor 2020-01-10 start */
window.onload = function () {
    if (sessionStorage.getItem("KNJA134pForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJA134pForm1_CurrentCursor")).focus();
    }
}

function current_cursor(para) {
    sessionStorage.setItem("KNJA134pForm1_CurrentCursor", para);
}
/* Add by PP for CurrentCursor 2020-01-17 end */

function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2020-01-10 start */
    if (sessionStorage.getItem("KNJA134pForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJA134pForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-01-17 end */
    if (cmd == 'update') {
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        if (document.forms[0].category_selected.length == 0) {
            alert('{rval MSG916}');
            // Add by PP for CurrentCursor 2020-01-10 start
            document.getElementById(sessionStorage.getItem("KNJA134pForm1_CurrentCursor")).focus();
            // Add by PP for CurrentCursor 2020-01-17 end
            return;
        } else {
            for (var i = 0; i < document.forms[0].category_selected.length; i++) {
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

function newwin(SERVLET_URL) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";

    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
    } else {
        var sep = "";
        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            document.forms[0].category_selected.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
            sep = ",";
        }

        for (var i = 0; i < document.forms[0].category_name.length; i++) {
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            document.forms[0].category_selected.options[i].selected = 1;
        }

        action = document.forms[0].action;
        target = document.forms[0].target;

        //      url = location.hostname;
        //      document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL + "/KNJA";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}

function kubun() {
    var kubun1 = document.forms[0].seito;
    var kubun2 = document.forms[0].gakushu;
    var kubun3 = document.forms[0].koudo;
    var kubun8 = document.forms[0].online == null || document.forms[0].online.checked == false;
    var printdisabled;
    var print1disabled;
    var print3disabled;

    if ((kubun1.checked == false) && (kubun2.checked == false) && (kubun3.checked == false) && kubun8) {
        printdisabled = true;
    } else {
        printdisabled = false;
    }
    document.forms[0].btn_print.disabled = printdisabled;

    if (kubun1.checked == true) {
        print1disabled = false;
    } else {
        print1disabled = true;
    }
    document.forms[0].simei.disabled = print1disabled;
    document.forms[0].schzip.disabled = print1disabled;
    document.forms[0].schoolzip.disabled = print1disabled;

    if (kubun3.checked == true) {
        print3disabled = false;
    } else {
        print3disabled = true;
    }
    document.forms[0].mongon.disabled = print3disabled;
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
    var temp1 = [];
    var temp2 = [];
    var temp3 = false;
    var tempSort = [];
    var src, dest;
    var id, time, label;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        src = document.forms[0].category_name;
        dest = document.forms[0].category_selected;
        /* Add by PP for PC-Talker 読み  2020-01-10 start */
        document.getElementById('td_CATEGORY_SELECTED').focus();
        id = 'td_CATEGORY_SELECTED';
        label = (src.length == 0) ? "移動ための項目がないので移動失敗です" : "";
        document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else if (label == "選択した項目を移動しました") {
            time = 2000;
        } else {
            time = 3000;
        }
        setTimeout(function () {
            if (chdt == 0) {
                document.getElementById('btn_left10').focus();
            } else {
                document.getElementById('btn_left11').focus();
            }
            document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", "");
        }, time);

    } else {
        src = document.forms[0].category_selected;
        dest = document.forms[0].category_name;

        document.getElementById('td_CATEGORY_NAME').focus();
        id = 'td_CATEGORY_NAME';
        label = (src.length == 0) ? "移動ための項目がないので移動失敗です" : "";
        document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else if (label == "選択した項目を移動しました") {
            time = 2000;
        } else {
            time = 3000;
        }
        setTimeout(function () {
            if (chdt == 0) {
                document.getElementById('btn_right10').focus();
            } else {
                document.getElementById('btn_right11').focus();
            }

            document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", "");
        }, time);

    }
    /* Add by PP for PC-Talker 読み 2020-01-17 end */

    for (i = 0; i < dest.length; i++) {
        temp1[temp1.length] = dest.options[i];
    }

    for (i = 0; i < src.length; i++) {
        if (src.options[i].selected) {
            temp3 = true;
            temp1[temp1.length] = src.options[i];
        } else {
            temp2[temp2.length] = src.options[i];
        }
    }
    if (src.length > 0 && temp3) {
        document.getElementById(id).setAttribute("aria-label", "選択した項目を移動しました");
    } else if(src.length > 0){
        document.getElementById(id).setAttribute("aria-label", "項目を選択しないので移動失敗です");
    }
    for (i = 0; i < temp1.length; i++) {
        tempSort[i] = { value: temp1[i].value, idx: i };
    }
    tempSort.sort(function (a, b) { if (a.value < b.value) return -1; if (a.value > b.value) return 1; return 0; });

    for (i = 0; i < temp1.length; i++) {
        dest.options[i] = new Option();
        dest.options[i].value = temp1[tempSort[i].idx].value;
        dest.options[i].text = temp1[tempSort[i].idx].text;
    }

    //generating new options
    ClearList(src, src);
    for (var i = 0; i < temp2.length; i++) {
        src.options[i] = new Option();
        src.options[i].value = temp2[i].value;
        src.options[i].text = temp2[i].text;
    }

}


function moves(sides, chdt) {
    var temp = [];
    var tempSort = [];
    var i;
    var src, dest;
    var label, time;

    //assign what select attribute treat as src and dest
    if (sides == "left") {
        src = document.forms[0].category_name;
        dest = document.forms[0].category_selected;
        /* Add by PP for PC-Talker 読み  2020-01-10 start */
        document.getElementById('td_CATEGORY_SELECTED').focus();
        label = (src.length == 0) ? "移動ための項目がないので移動失敗です" : "すべての項目を移動しました";
        document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else {
            time = 2000;
        }

        setTimeout(function () {
            if (chdt == 0) {
                document.getElementById('btn_lefts0').focus();
            } else {
                document.getElementById('btn_lefts1').focus();
            }

            document.getElementById('td_CATEGORY_SELECTED').setAttribute("aria-label", "");
        }, time);
    } else {
        src = document.forms[0].category_selected;
        dest = document.forms[0].category_name;

        document.getElementById('td_CATEGORY_NAME').focus();
        label = (src.length == 0) ? "移動ための項目がないので移動失敗です" : "すべての項目を移動しました";
        document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", label);
        if (label == "移動ための項目がないので移動失敗です") {
            time = 3500;
        } else {
            time = 2000;
        }
        setTimeout(function () {
            if (chdt == 0) {
                document.getElementById('btn_rights0').focus();
            } else {
                document.getElementById('btn_rights1').focus();
            }

            document.getElementById('td_CATEGORY_NAME').setAttribute("aria-label", "");
        }, time);
    }
    /* Add by PP for PC-Talker 読み  2020-01-17 end */

    for (i = 0; i < dest.length; i++) {
        temp[temp.length] = dest.options[i];
    }
    for (i = 0; i < src.length; i++) {
        temp[temp.length] = src.options[i];
    }

    for (i = 0; i < temp.length; i++) {
        tempSort[i] = { value: temp[i].value, idx: i };
    }
    tempSort.sort(function (a, b) { if (a.value < b.value) return -1; if (a.value > b.value) return 1; return 0; });

    for (i = 0; i < temp.length; i++) {
        dest.options[i] = new Option();
        dest.options[i].value = temp[tempSort[i].idx].value;
        dest.options[i].text = temp[tempSort[i].idx].text;
    }

    ClearList(src, src);

}
