//サブミット
function btn_submit(cmd) {
    if (cmd == "csv" || cmd == "print") {
        //教員給食費
        if (   document.forms[0].FILE.value != ""
            && document.forms[0].STAFF_LUNCH.checked == false
            && !confirm('チェックは入っていません。よろしいでしょうか？')) {
            return true;
        }

        //引落し
        if (document.forms[0].OUTPUT1.checked == true) {
            //入力チェック
            if (document.forms[0].RETRANSFER_DATE.value == "") {
                alert("{rval MSG304}" + "\n( 再振替日 )");
                return false;
            }
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
function setCheckBox(obj) {
    if (obj.value != "") {
        document.forms[0].STAFF_LUNCH.checked = true;
    } else {
        document.forms[0].STAFF_LUNCH.checked = false;
    }
    return false;
}

//印刷
function newwin(SERVLET_URL, printKouzaKigou, printKyokuBan, printLimitDate, printSaiHuri, printTotalCnt, printTotalMoney, printSyubetsu, printJigyounushi, setLimitDay, toriKyouName) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
    document.forms[0].printKouzaKigou.value = printKouzaKigou;
    document.forms[0].printKyokuBan.value = printKyokuBan;
    document.forms[0].printLimitDate.value = printLimitDate;
    document.forms[0].printSaiHuri.value = printSaiHuri;
    document.forms[0].printTotalCnt.value = printTotalCnt;
    document.forms[0].printTotalMoney.value = printTotalMoney;
    document.forms[0].printSyubetsu.value = printSyubetsu;
    document.forms[0].printJigyounushi.value = printJigyounushi;
    document.forms[0].setLimitDay.value = setLimitDay;
    document.forms[0].toriKyouName.value = toriKyouName;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}