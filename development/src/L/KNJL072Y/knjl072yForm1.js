function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_DESIREDIV.value = document.forms[0].DESIREDIV.options[document.forms[0].DESIREDIV.selectedIndex].value;

    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].DESIREDIV.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}

function setName(obj, rowid, flg)
{
    var idx = obj.value;
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('JUDGEKIND_NAME' + rowid, '');
        }
        return;
    }
    if (flg == '0') {
        if (typeof judgekind_name[idx] != "undefined") {
            outputLAYER('JUDGEKIND_NAME' + rowid, judgekind_name[idx]);
        } else {
            alert('{rval MSG901}');
            outputLAYER('JUDGEKIND_NAME' + rowid, '');
            obj.value = '';
        }
    }
}
