function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'update' && !confirm('{rval MSG102}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function CheckScore(obj, examno)
{
    obj.value = toInteger(obj.value);
    if (obj.value != '' && obj.value != 2 && obj.value != 1) {
        obj.value = '';
        document.getElementById("ID_" + examno).innerHTML = "";
        alert('入力可能な数値は、1又は、2のみです。');
        obj.focus();
        return;
    } else if (obj.value == 2 || obj.value == 1) {
        document.getElementById("ID_" + examno).innerHTML = document.getElementById("L031_" + obj.value).value;
    } else if (obj.value == '') {
        document.getElementById("ID_" + examno).innerHTML = "";
    }
}

function showConfirm()
{
    if(confirm('{rval MSG106}')) return true;
    return false;
}

