function btn_submit(cmd) {
    if (cmd == "reset") {
        if (confirm("{rval MSG106}")) {
            cmd = "form2";
        } else {
            return true;
        }
    }

    if (cmd == "form2") {
        tmp_list();
    }

    if (cmd == "reload2") {
        if (confirm("OK　　　  ・・・　全てクリアして読込します\nキャンセル　・・・　追加読込します")) {
            cmd = "reload2_ok";
        } else {
            cmd = "reload2_cancel";
        }
    }

    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == "update2") {
            updateFrameLocks();
        }
    } else if (cmd == "update2") {
        document.forms[0].btn_update.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//コメント差し込み
function insertComment(obj, target, label) {
    if (obj.checked == true) {
        document.forms[0][target].value = document.forms[0][label].value;
        document.forms[0][target].disabled = true;
    } else {
        document.forms[0][target].disabled = false;
    }
}

function add(cmd) {
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].ANNUAL.length;
    var w = document.forms[0].ADD_YEAR.value + "," + document.forms[0].ADD_YEAR_GRADE.value;
    var x = document.forms[0].ADD_YEAR.value + "年度　" + Number(document.forms[0].ADD_YEAR_GRADE.value) + "学年(年次)　★";

    if (document.forms[0].ADD_YEAR.value == "" || document.forms[0].ADD_YEAR_GRADE.value == "") return false;

    for (var i = 0; i < v; i++) {
        if (w.substr(0, 4) == document.forms[0].ANNUAL.options[i].value.substr(0, 4)) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].ANNUAL.options[v] = new Option();
    document.forms[0].ANNUAL.options[v].value = w;
    document.forms[0].ANNUAL.options[v].text = x;

    for (var i = 0; i < document.forms[0].ANNUAL.length; i++) {
        temp1[i] = document.forms[0].ANNUAL.options[i].value;
        tempa[i] = document.forms[0].ANNUAL.options[i].text;
    }

    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();

    //generating new options
    ClearList(document.forms[0].ANNUAL, document.forms[0].ANNUAL);
    if (temp1.length > 0) {
        for (var i = 0; i < temp1.length; i++) {
            document.forms[0].ANNUAL.options[i] = new Option();
            document.forms[0].ANNUAL.options[i].value = temp1[i];
            document.forms[0].ANNUAL.options[i].text = tempa[i];
            if (w == temp1[i]) {
                document.forms[0].ANNUAL.options[i].selected = true;
            }
        }
    }

    tmp_list();

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function tmp_list() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    attribute4 = document.forms[0].selectdataText;
    attribute4.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].ANNUAL.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].ANNUAL.options[i].value;
        attribute4.value = attribute4.value + sep + document.forms[0].ANNUAL.options[i].text;
        sep = "-";
    }
}
window.onload = function () {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var tagName = document.forms[0].elements[i].tagName;
        var tagType = document.forms[0].elements[i].type;
        if ((tagName == "INPUT" && tagType != "button" && tagType != "hidden") || tagName == "TEXTAREA") {
            document.forms[0].elements[i].disabled = true;
            document.forms[0].elements[i].style.backgroundColor = "#FFFFFF";
            if (document.forms[0].elements[i].name == "cmd") {
                alert(tagName);
                alert(tagType);
            }
        }
    }
};
