
function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } 

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == '0') {
            //クッキー削除
            deleteCookie("nextURL");
            document.location.replace(nextURL);
            alert('{rval MSG201}');
        } else if (cd == '1') {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}

