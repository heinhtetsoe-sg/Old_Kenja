function btn_submit(cmd) {
    if (cmd == "entGrdHistDel" && !confirm("{rval MSG103}")) {
        return true;
    }
    if (
        document.forms[0].GRD_DATE.value != "" &&
        document.forms[0].ENT_DATE.value != "" &&
        document.forms[0].ENT_DATE.value >= document.forms[0].GRD_DATE.value
    ) {
        var dayList = document.forms[0].ENT_DATE.value.split("/");
        dayList[2] = parseInt(dayList[2]) + 1;
        if (dayList[2] < 10) {
            dayList[2] = "0" + dayList[2];
        }
        var dayText = dayList.join("/") + "以降";
        alert("卒業日付範囲不正です。\n\n範囲：" + dayText);
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
