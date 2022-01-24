function btn_submit(cmd) {
    if (cmd == "update") {
        var checkFlg = false;
        re = new RegExp("PAID_FLG-");
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re) && obj_updElement.checked == true) {
                checkFlg = true;
            }
        }
        if (!checkFlg) {
            alert('最低ひとつチェックを入れてください。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
