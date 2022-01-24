function btn_submit(cmd) {
    if (cmd == 'execute') {
        var flag;
        flag = "";
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "checkbox" && e.checked && e.name.match("CHECKED_")){
                var val = e.value;
                if (val != ''){
                    flag = "on";
                }
            }
        }
        if (flag == ''){
            alert("チェックボックスが選択されておりません。");
            return;
        }
        if (confirm('{rval MSG102}')) {
        } else {
            return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj){
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match("CHECKED_") && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
