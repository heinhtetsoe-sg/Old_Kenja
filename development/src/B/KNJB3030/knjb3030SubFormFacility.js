//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;

    var chk = document.forms[0]["CHECK[]"];
    var sep = "";
    var Ch_val = "";
    var Ch_txt = "";

    for (var i = 0; i < chk.length; i++) {
        if (chk[i].checked) {
            var tmp = chk[i].value.split(",");

            Ch_val = Ch_val + sep + tmp[0];
            Ch_txt = Ch_txt + sep + tmp[1];
            sep = ",";
        }
    }
    if (Ch_val == "") {
        if (chk.checked) {
            var tmp = chk.value.split(",");

            Ch_val = Ch_val + sep + tmp[0];
            Ch_txt = Ch_txt + sep + tmp[1];
            sep = ",";
        }
    }
    top.main_frame.right_frame.document.forms[0].FACCD.value = Ch_val;
    top.main_frame.right_frame.document.forms[0].FACILITYABBV.value = Ch_txt;

    top.main_frame.right_frame.closeit();
}
