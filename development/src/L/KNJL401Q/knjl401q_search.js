function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function apply_examno(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].value;

        var examno = parent.document.forms[0].EXAMNO;

        if (examno) {
            examno.value = val;
        }
        parent.document.forms[0].cmd.value = 'search';
        parent.document.forms[0].submit();
        parent.closeit();
    } else {
        alert("データが選択されていません");
    }

}
