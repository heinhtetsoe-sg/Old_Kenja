function btn_submit(cmd)
{
    var str = new Object();
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == 'pre' || cmd == 'next') {
        if (!confirm('{rval MSG108}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//入力可/不可
function textDisabledChange(obj, textName) {
    if (obj.checked) {
        document.forms[0][textName].disabled = false;
    } else {
        document.forms[0][textName].disabled = true;
    }
}

//スクロール
function scrollRC() {
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}
