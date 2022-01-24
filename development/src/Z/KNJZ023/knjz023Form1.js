function btn_submit(cmd)
{
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    if (cmd == 'copy') {
        if (document.forms[0].COURSE.value == document.forms[0].COPY_COURSE.value) {
            alert('{rval MSG203}'+'\n同じ学科です。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function allCheck(obj)
{
    for (var i=0; i < document.forms[0].elements.length; i++) {
        re = new RegExp("^AUTO_CHECK" );
        if (document.forms[0].elements[i].name.match(re)) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
    return false;
}
