/* Add by PP for CurrentCursor 2020-01-10 start */
window.onload = function () {
    if (sessionStorage.getItem("KNJD426NForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJD426NForm1_CurrentCursor")).focus();
    }
};

function current_cursor(para) {
    sessionStorage.setItem("KNJD426NForm1_CurrentCursor", para);
}
/* Add by PP for CurrentCursor 2020-01-17 end */

function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2020-01-10 start */
    if (sessionStorage.getItem("KNJD426NForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJD426NForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-01-17 end */
    selectdata = document.forms[0].selectdata;
    selectdata.value = "";
    sep = "";

    if (cmd != "change" && cmd != "changeHukusiki" && cmd != "clear") {
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            var val = document.forms[0].CATEGORY_SELECTED.options[i].value;
            selectdata.value = selectdata.value + sep + val;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//異動対象日付変更
function tmp_list(cmd, submit) {
    selectdata = document.forms[0].selectdata;
    selectdata.value = "";
    sep = "";

    if (cmd != "change" && cmd != "changeHukusiki" && cmd != "clear") {
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            var val = document.forms[0].CATEGORY_SELECTED.options[i].value;
            selectdata.value = selectdata.value + sep + val;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}

function newwin(SERVLET_URL) {
    var e;
    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    for (i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        var val = document.forms[0].CATEGORY_SELECTED.options[i].value.split("-");
        if (val.length > 1) {
            document.forms[0].CATEGORY_SELECTED.options[i].value = val[2];
        }
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    if (document.forms[0].HUKUSIKI_RADIO) {
        e = document.forms[0].HUKUSIKI_RADIO2;
        document.forms[0].SELECT_GHR.value = e.checked ? "1" : "";
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

if (window.addEventListener) {
    window.addEventListener('load', function () {
        chkcat2();
    });
}


function chkcat2() {
    document.forms[0].SCHOOL_KIND.style.visibility = document.getElementById("HUKUSIKI_RADIO1").checked ? "visible" : "collapse";
}

/****************************************** リストtoリスト ********************************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side, selLeft, selRight, sortFlg) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var temp3 = false;
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute1, attribute2;
    var id, time, label;
    if (side == "left") {
        
        attribute1 = document.getElementsByName(selRight)[0];
        attribute2 = document.getElementsByName(selLeft)[0];
        
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
        attribute1 = document.getElementsByName(selLeft)[0];
        attribute2 = document.getElementsByName(selRight)[0];

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

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value + "," + y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value + "," + y;
            temp3 = true;
        } else {
            y = current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }
    if (attribute1.length > 0 && temp3) {
        document.getElementById(id).setAttribute("aria-label", "選択した項目を移動しました");
    } else if(attribute1.length > 0){
        document.getElementById(id).setAttribute("aria-label", "項目を選択しないので移動失敗です");
    }

    if (sortFlg) {
        tempaa.sort();
    }

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text = tempa[tmp[1]];
    }

    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
}

function moves(side, selLeft, selRight, sortFlg) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;
    var attribute5, attribute5;
    var label, time;

    if (side == "left") {
        attribute5 = document.getElementsByName(selRight)[0];
        attribute6 = document.getElementsByName(selLeft)[0];
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
        attribute5 = document.getElementsByName(selLeft)[0];
        attribute6 = document.getElementsByName(selRight)[0];

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
    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + "," + z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + "," + z;
    }

    if (sortFlg) {
        tempaa.sort();
    }

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    ClearList(attribute5);
}
//親項目がオフの時に全ての子項目をdisabledにする
function switchChildrenState(obj) {
    var kind_seq_array = document.forms[0].kind_seq_array.value.split(',');
    for (var i = 0; i < kind_seq_array.length; i++) {
        var kind_seq = kind_seq_array[i];
        var printSubChk = document.getElementById("PRINT_SUB_CHK"+kind_seq);
        console.log(obj);
        if (obj.checked) {
            printSubChk.disabled = false;
        } else {
            printSubChk.disabled = true;
        }
    }
}


