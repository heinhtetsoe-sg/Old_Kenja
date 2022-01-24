function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

function CheckScore(obj) {
    obj.value = toInteger(obj.value);    
    if (obj.value > 100) {
        alert('{rval MSG901}' + '満点：100以下で入力してください。');
        obj.focus();
        return;
    }
}

function Setflg(obj) {
    change_flg = true;
    document.forms[0].HID_PRE_HR_CLASS.value = document.forms[0].PRE_HR_CLASS.options[document.forms[0].PRE_HR_CLASS.selectedIndex].value;
    document.forms[0].PRE_HR_CLASS.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}