function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    //入試制度、入試区分の組合せがすでにあったら、処理を中止
    if (cmd == "add") {
        var checker;
        var val = document.forms[0].checker.value;
        checker = val.split(",");
        var nyuryoku = document.forms[0].APPLICANTDIV.value + document.forms[0].TESTDIV.value;
        for (i = 0; i < checker.length; i++) {
            if (checker[i] == nyuryoku) {
                alert("すでに同じ\n『入試制度』・『入試区分』\nがあります");
                return false;
            }
        }
    }

    //入試制度、入試区分の組合せが悪かったら処理を中止
    if (cmd == "add" || cmd == "update") {
        var applicantdiv = document.forms[0].APPLICANTDIV.value;
        var testdiv      = document.forms[0].TESTDIV.value;
        if (!isNaN(testdiv) && testdiv >= 10) {          //2桁以上の時、桁数エラー
            alert('{rval MSG915}'+"\n( 入試区分は1バイトまでです。)");
            return false;
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
