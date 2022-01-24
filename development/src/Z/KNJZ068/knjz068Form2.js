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

    //必須入力チェック（登録・削除）
    if (cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].CHK_IBYEAR.value == '') {
            alert('{rval MSG304}\n（年度）');
            return false;
        }
        if (document.forms[0].CHK_IBGRADE.value == '') {
            alert('{rval MSG304}\n（学年）');
            return false;
        }
        if (document.forms[0].CHK_IBPRG_COURSE.value == '') {
            alert('{rval MSG304}\n（IBコース）');
            return false;
        }
        if (document.forms[0].IBSUBCLASS.value == '') {
            alert('{rval MSG301}\n（IB科目）');
            return false;
        }
        if (document.forms[0].IBEVAL_DIV1.value == '') {
            alert('{rval MSG301}\n（評価区分1）');
            return false;
        }
        if (document.forms[0].IBEVAL_DIV2.value == '') {
            alert('{rval MSG301}\n（評価区分2）');
            return false;
        }
        if (document.forms[0].IBEVAL_MARK.value == '') {
            alert('{rval MSG301}\n（評価規準記号）');
            return false;
        }
    }

    //必須入力チェック（CSV出力）
    if (cmd == 'csv') {
        if (document.forms[0].CHK_IBYEAR.value == '') {
            alert('{rval MSG304}\n（年度）');
            return false;
        }
        if (document.forms[0].CHK_IBPRG_COURSE.value == '') {
            alert('{rval MSG304}\n（IBコース）');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//英字チェック&大文字変換
function ValueCheck(checkString) {

    newString = "";    // 正しい文字列
    count = 0;         // ループ用カウンタ
    // 渡された文字列の長さを引数としてループ
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);

        // ENSURE CHARACTER IS AN ALPHA CHARACTER
        if ((ch >= "a" && ch <= "z") || (ch >= "A" && ch <= "Z" )) {
            newString += ch;
        }
    }

    if (checkString != newString) {
        // VERIFY WITH USER THAT IT IS OKAY TO REMOVE INVALID CHARACTERS
        alert("入力された値は不正な文字列です。\n半角英字を入力してください。\n入力された文字列は削除されます。");
        //大文字に変換
        newString = newString.toUpperCase()
        // 文字列を返す
        return newString;
    }

    //大文字に変換
    checkString = checkString.toUpperCase()

    return checkString;
}
