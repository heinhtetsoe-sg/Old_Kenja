window.onload = init;
function init() {
    document.forms[0].SIMEBI.value = getSimebi();
}
function btn_submit(cmd)
{
    if (cmd=='reset') {
        document.forms[0].SIMEBI.value = getSimebi();
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closing_window()
{
    alert('{rval MSG300}');
    closeWin();
    return true;
}
function upDateText() {
    document.forms[0].SIMEBI.value = getSimebi();
}
function getSimebi() {
    var val = document.forms[0].TARGET_MONTH.value;
    var listarray = new Array();
    listarray = val.split(",");
    if (listarray[3] == undefined) {
        listarray[3] = '';
    }
    return listarray[3];
}
