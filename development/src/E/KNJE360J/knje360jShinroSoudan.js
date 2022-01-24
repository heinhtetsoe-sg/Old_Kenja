//サブミット
function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }

    if (cmd == "shinroSoudan_clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    if (cmd == "shinroSoudan_update" && document.forms[0].SEQ.value == "") {
        alert("{rval MSG308}");
        return true;
    }

    if (cmd == "shinroSoudan_insert" || cmd == "shinroSoudan_update") {
        if (document.forms[0].ENTRYDATE.value == "") {
            alert("登録日を入力してください");
            return true;
        }

        var date = document.forms[0].ENTRYDATE.value.split("/");
        var sdate = document.forms[0].SDATE.value.split("/");
        var edate = document.forms[0].EDATE.value.split("/");
        sdate_show = document.forms[0].SDATE.value;
        edate_show = document.forms[0].EDATE.value;

        if (
            new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])) ||
            new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))
        ) {
            alert("登録日が入力範囲外です。\n（" + sdate_show + "～" + edate_show + "）");
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
