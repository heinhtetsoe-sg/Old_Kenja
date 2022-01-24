function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //必須入力チェック
    if (cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].IBGRADE.value == '') {
            alert('{rval MSG301}\n（学年）');
            return false;
        }
        if (document.forms[0].IBPRG_COURSE.value == '') {
            alert('{rval MSG301}\n（IBコース）');
            return false;
        }
        if (document.forms[0].IBSUBCLASS.value == '') {
            alert('{rval MSG301}\n（IB科目）');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//Grade数チェック
function level(max) {
    var level;
    level = document.forms[0].MAX_GRADE_LEVEL.value;

    if (level > 0) {
    } else {
        alert('{rval MSG901}'+'\n1以上を入力してください。\n( Grade )');
        document.forms[0].MAX_GRADE_LEVEL.focus();
        return false;
    }

    if (level == max) {
        return false;
    }

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}
