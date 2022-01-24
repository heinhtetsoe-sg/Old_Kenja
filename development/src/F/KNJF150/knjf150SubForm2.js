function btn_submit(cmd) {
    //チェック
    if (cmd == "update" || cmd == "insert") {
        if (document.forms[0].SCHREGNO.value == "") {
            alert("{rval MSG304}");
            return true;
        } else if (document.forms[0].VISIT_DATE.value == "" || document.forms[0].VISIT_HOUR.value == "" || document.forms[0].VISIT_MINUTE.value == "") {
            alert("来室日時が入力されていません。\n　　　　（必須入力）");
            return true;
        } else if (document.forms[0].VISIT_REASON1.value == "") {
            alert("来室理由１が入力されていません。\n　　　　（必須入力）");
            return true;
        } else if (document.forms[0].TREATMENT1.value == "") {
            alert("処置１が入力されていません。\n　　　　（必須入力）");
            return true;
        } else if (document.forms[0].LEAVE_HOUR.value == "" || document.forms[0].LEAVE_MINUTE.value == "") {
            alert("退出時間が入力されていません。\n　　　　（必須入力）");
            return true;
        }
    } else if (cmd == "subform2_clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    alert("登録された内容が印刷されます。");

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//disabled
function OptionUse(obj) {
    if (document.forms[0].CONDITION4[0].checked == true) {
        document.forms[0].MEAL.disabled = false;
        document.forms[0].MEAL.style.backgroundColor = "#ffffff";
    } else {
        document.forms[0].MEAL.disabled = true;
        document.forms[0].MEAL.style.backgroundColor = "#D3D3D3";
    }
}

//disabled
function OptionUse2(obj, checkText) {
    var array = document.forms[0][checkText].value.split(",");

    flg = true;
    for (var i = 0; i < array.length; i++) {
        if (array[i] == obj.value) {
            flg = false;
        }
    }

    document.forms[0][obj.name + "_TEXT"].disabled = flg;
}
window.onload = function () {
    if (document.forms[0].SEND_PROGRAMID.value != "KNJF150G") {
        return;
    }
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var tagName = document.forms[0].elements[i].tagName;
        var tagType = document.forms[0].elements[i].type;
        var propName = document.forms[0].elements[i].name;
        if ((tagName == "INPUT" && tagType != "button") || tagName == "SELECT" || tagName == "TEXTAREA" || propName == "btn_calen") {
            document.forms[0].elements[i].disabled = true;
            document.forms[0].elements[i].style.backgroundColor = "#FFFFFF";
        }
    }
};
