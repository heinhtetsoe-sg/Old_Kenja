function btn_submit(cmd) {
    if (cmd == 'execute') {
        var flag;
        flag = "";
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "checkbox" && e.checked && e.name == "CHECKED[]") {
                var val = e.value;
                if (val != '') flag = "on";
            }
        }
        if (flag == '') {
            alert("チェックボックスが選択されておりません。");
            return;
        }
        if (!confirm('{rval MSG102}')) {
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//チェックボックスのALL ON/OFF
function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
