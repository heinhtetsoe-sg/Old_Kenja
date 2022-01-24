function btn_submit(cmd) {
    bodyHeight = window.innerHeight || document.body.clientHeight || 0;
    document.forms[0].windowHeight.value = bodyHeight;

    if (cmd == "update") {
        var checkFlg = false;
        re = new RegExp("PAID_FLG-");
        var allCnt = 0;
        var checkedCnt = 0;
        if (document.forms[0].PLAN_PAID_MONEY_DIV.value == "") {
            alert("入金方法を指定してください。");
            return false;
        }
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re)) {
                if (obj_updElement.checked == true) {
                    checkFlg = true;
                    checkedCnt++;
                }
                allCnt++;
            }
        }
        if (!checkFlg) {
            alert("最低ひとつチェックを入れてください。");
            return false;
        }
        var idx = document.forms[0].PLAN_PAID_MONEY_DIV.selectedIndex;
        var confTitle = document.forms[0].PLAN_PAID_MONEY_DIV.options[idx].text;
        if (!confirm(confTitle + "　" + checkedCnt + "/" + allCnt + "件\n\n更新してもよろしいでしょうか？")) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//画面リサイズ
function submit_reSize() {
    bodyHeight = window.innerHeight || document.body.clientHeight || 0;

    document.getElementById("tbody").style.height = bodyHeight - 150;
}

function allCheck(obj) {
    re = new RegExp("PAID_FLG-");
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {
            obj_updElement.checked = obj.checked;
        }
    }
    return true;
}

function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
