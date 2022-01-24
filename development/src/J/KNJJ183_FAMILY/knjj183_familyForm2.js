function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
        return false;
    }
    if (cmd == 'kakutei'){
        if (document.forms[0].RELA_SCHREGNO.value == "") {
            alert('兄弟姉妹学籍番号が未入力です。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;

    for (var i = 0; i < document.forms[0].length; i++) {
        if (document.forms[0][i].disabled) {
            document.forms[0][i].disabled = false;
        }
    }

    document.forms[0].submit();
    return false;
}
function Page_jumper(jump,sno) {
    var cd;
    if(sno == ''){
        alert('{rval MSG304}');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.replace(jump);
}
