function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == 'search') {
        if (document.forms[0].JUDGMENT_DIV_SEARCH.value == "" && document.forms[0].AVG_FROM.value == "" && document.forms[0].AVG_TO.value == "") {
            alert('抽出条件を指定した下さい。');
            return true;
        }
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function bgcolorPink(obj, examno) {
    document.getElementById('ROWID' + examno).style.background = obj.checked ? "pink" : "white";
}
