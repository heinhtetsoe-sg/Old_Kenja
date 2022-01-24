function btn_submit(cmd) {
    if (cmd == "update") {
        re = new RegExp("^COLLECT_LM_CD_|MONEY_DUE_|COLLECT_CNT_" );
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re) && obj_updElement.value == "") {
                obj_updElement.focus();
                alert('商品名・単価・数量は必須です。');
                return false;
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

