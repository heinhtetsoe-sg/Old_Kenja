function btn_submit(cmd) {
    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    //取消確認
    if (cmd == "subform4_clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    //コピー確認
    if (cmd == "subform4_copy") {
        if (!confirm("{rval MSG101}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//disabled
function OptionUse(obj) {
    if (obj.checked == true) {
        flg = false;
    } else {
        flg = true;
    }

    var cd = obj.name.substring(5).split("_");

    if (cd[0] == "01" && cd[1] == "03") {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.match("REASON" + cd[2])) {
                document.forms[0].elements[i].disabled = flg;
            } else if (document.forms[0].elements[i].name == "DETAIL" + obj.name.substring(5)) {
                document.forms[0].elements[i].disabled = flg;
            }
        }
    } else {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "TEXT" + obj.name.substring(5)) {
                document.forms[0].elements[i].disabled = flg;
            }
        }
    }
}

//印刷
function newwin(SERVLET_URL) {
    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }

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
window.onload = function () {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var tagName = document.forms[0].elements[i].tagName;
        var tagType = document.forms[0].elements[i].type;
        var name = document.forms[0].elements[i].name;
        if ((tagName == "INPUT" && name != "btn_back" && tagType != "hidden") || tagName == "TEXTAREA") {
            document.forms[0].elements[i].disabled = true;
            document.forms[0].elements[i].style.backgroundColor = "#FFFFFF";
            if (document.forms[0].elements[i].name == "cmd") {
                alert(tagName);
                alert(tagType);
            }
        }
    }
};
