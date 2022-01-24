
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function getFrame() {
    return document.forms[0].frame.value;
}
function apply_industrys(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");

        var industry_scd = parent.document.forms[0].INDUSTRY_SCD;
        var industry_sname = parent.document.getElementById('label_name');

        //産業種別（小）コード
        if (industry_scd) {
            industry_scd.value = arr[0];
        }

        //産業種別名称innerHTML
        if (industry_sname) {
            industry_sname.innerHTML = arr[1];
        }

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }

}
