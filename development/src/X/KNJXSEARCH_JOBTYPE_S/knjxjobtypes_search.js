function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function getFrame() {
    return document.forms[0].frame.value;
}
function apply_jobtypes(obj) {
    if (obj.selectedIndex >= 0) {
        var val  = obj.options[obj.selectedIndex].text;
        var arr  = val.split(" | ");

        var jobtype_Scd   = parent.document.forms[0].JOBTYPE_SCD;
        var jobtype_Sname = parent.document.getElementById('label_jobtype_name');

        //職業種別（小）コード
        if (jobtype_Scd) {
            jobtype_Scd.value = arr[0];
        }

        //産業種別名称innerHTML
        if (jobtype_Sname) {
            jobtype_Sname.innerHTML = arr[1];
        }

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }

}
