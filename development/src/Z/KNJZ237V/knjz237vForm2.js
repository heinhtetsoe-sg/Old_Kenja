function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }
    //削除確認
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    }

    if (cmd == 'add' || cmd == 'update' || cmd == 'delete') {
        //必須入力チェック
        if (document.forms[0].SEMESTER.value == ""){
            alert('{rval MSG301}\n(学期)');
            return false;
        }
        if (document.forms[0].TESTKINDCD.value == "") {
            alert('{rval MSG301}\n(考査)');
            return false;
        }
        if (document.forms[0].SUBCLASSCD.value == "") {
            alert('{rval MSG301}\n(科目)');
            return false;
        }
        if (document.forms[0].DIV.value == "") {
            alert('{rval MSG301}\n(区分)');
            return false;
        }
    }

    if (cmd == 'add' || cmd == 'update') {
        //必須入力チェック
        if (document.forms[0].PERFECT.value == "") {
            alert('満点を入力してください。');
            return false;
        }
        if (!document.forms[0].DIV[0].checked && document.forms[0].GRADE.value == "") {
            alert('{rval MSG301}\n(学年)');
            return false;
        }
    }

    if (cmd == 'add') {
        //確認
        if (document.forms[0].DIV[0].checked && !confirm('科目以外の設定は、全て削除されます。(全学年)')) {
            return false;
        }
        if (!document.forms[0].DIV[0].checked && !confirm('科目設定が削除されます。')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
