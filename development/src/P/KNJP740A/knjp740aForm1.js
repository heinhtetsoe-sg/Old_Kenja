//サブミット
function btn_submit(cmd) {
    if (cmd == "csv" || cmd == "csv2") {
        //引落し
        if (document.forms[0].OUTPUT1.checked == true) {
            //返金
        } else {
            if (document.forms[0].HENKIN_DATE.value == "") {
                alert("{rval MSG304}" + "\n( 返金日 )");
                return false;
            }
        }
    }
    document.forms[0].encoding = "multipart/form-data";
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
