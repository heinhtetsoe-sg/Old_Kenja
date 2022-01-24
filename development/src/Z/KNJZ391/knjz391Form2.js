function btn_submit(cmd) {
    //取消確認
    if (cmd == "clear" && !confirm('{rval MSG106}')) {
        return false;
    }
    //削除確認
    if (cmd == "delete" && !confirm('{rval MSG103}')) {
        return false;
    }

    //更新・削除
    if (cmd == "update" || cmd == "delete") {
        //必須チェック
        if (document.forms[0].SCHEDULE_CD.value == "") {
            alert('{rval MSG301}\n（時間帯）');
            return true;
        }
        if (document.forms[0].COURSE_CD.value == "") {
            alert('{rval MSG301}\n（コード）');
            return true;
        }
        if (document.forms[0].BUS_NAME.value == "") {
            alert('{rval MSG301}\n（名称）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
