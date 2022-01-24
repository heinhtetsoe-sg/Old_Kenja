function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_VIEWCD.value.length == 0) {
        alert('{rval MSG303}');
        return false;
    }
    //コピー
    if (cmd == 'copy') {
        if (document.forms[0].SCHOOL_KIND.value == "") {
            alert('{rval MSG203}'+'\n'+'校種が未設定です。');
            return false;
        }

        var this_year   = document.forms[0].THIS_YEAR_CNT.value;
        var pre_year    = document.forms[0].PRE_YEAR_CNT.value;

        if (pre_year == 0) {
            alert('{rval MSG203}'+'\n'+'前年度にデータがありません。');
            return false;
        }

        if (this_year > 0) {
            if (!confirm('対象年度に既にデータが存在します。\n対象年度のデータを削除して、コピーします。よろしいですか？')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//チェックボックスのon/off（列）
function changeAllOnOff(id) {
    document.forms[0][id].checked = !document.forms[0][id].checked;

    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        reg = new RegExp("^" + id);
        if (e.name.match(reg)) {
            if (e.name != id) {
                e.checked = document.forms[0][id].checked;
            }
        }
    }
}

//チェックボックスのon/off
function changeOnOff(id) {
    document.forms[0][id].checked = !document.forms[0][id].checked;
}

//カーソルのon/offによる背景色等切替
function changeColor(div, flg, id) {
    if (div == 'on') {
        if (flg == '1') {
            document.getElementById(id).style.color = "black";
        } 
        document.getElementById(id).style.backgroundColor = "pink";
    } else {
        document.getElementById(id).style.backgroundColor = "";
        document.getElementById(id).style.color = "";
    }
}

//画面の切替
function Page_jumper(link) {
    if (confirm('{rval MSG108}')) {
        parent.location.href=link;
    }
}

//パターン反映処理
function reflect() {
    var pattern = document.forms[0].PATTERN_CD.value;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/^PATTERN_CD-/)) {
            document.forms[0].elements[i].value = pattern;
        }
    }
}
