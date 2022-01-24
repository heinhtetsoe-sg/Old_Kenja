function btn_submit(cmd) {
    if (cmd != 'main') {
        if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
            return true;
        }

        if (document.forms[0].OUTPUT[1].checked != true && cmd != '') {
            cmd = 'csv';
        }
    }

    if (cmd == 'csv' && document.forms[0].OUTPUT[3].checked == true) {
        if (document.forms[0].EXAM_SCHOOL_KIND.value == '') {
            alert('{rval MSG301}' + '\n(校種)');
            return false;
        }
        if (document.forms[0].EXAM_ID.value == '') {
            alert('{rval MSG301}' + '\n(試験ID)');
            return false;
        }
        if (document.forms[0].PLACE_ID.value == '') {
            alert('{rval MSG301}' + '\n(会場)');
            return false;
        }
        if (document.forms[0].EXAM_SUBCLASS.value == '') {
            alert('{rval MSG301}' + '\n(科目)');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
