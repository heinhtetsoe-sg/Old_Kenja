function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//戻る
function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
//計算(3年)
function Keisan3() {
    var setSum5;
    var setSum;
    var setAvg5;
    var setAvg;
    var kyoukasu5 = 5;
    var kyoukasu = eval(document.forms[0].kyouka_count.value);
    
    if (document.forms[0].CONFIDENTIAL_RPT01.value == "") document.forms[0].CONFIDENTIAL_RPT01.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT02.value == "") document.forms[0].CONFIDENTIAL_RPT02.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT03.value == "") document.forms[0].CONFIDENTIAL_RPT03.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT04.value == "") document.forms[0].CONFIDENTIAL_RPT04.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT05.value == "") document.forms[0].CONFIDENTIAL_RPT05.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT06.value == "") document.forms[0].CONFIDENTIAL_RPT06.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT07.value == "") document.forms[0].CONFIDENTIAL_RPT07.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT08.value == "") document.forms[0].CONFIDENTIAL_RPT08.value = 0;
    if (document.forms[0].CONFIDENTIAL_RPT09.value == "") document.forms[0].CONFIDENTIAL_RPT09.value = 0;

    setSum5 = eval(document.forms[0].CONFIDENTIAL_RPT01.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT02.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT03.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT04.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT09.value);

    setSum = eval(document.forms[0].CONFIDENTIAL_RPT01.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT02.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT03.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT04.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT05.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT06.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT07.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT08.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT09.value);

    setAvg5 = setSum5/kyoukasu5;
    setAvg = setSum/kyoukasu;

    var num5 = Math.round(setAvg5 * 10) / 10;
    var num9 = Math.round(setAvg * 10) / 10;
    document.forms[0].AVERAGE5.value = num5.toFixed(1);
    document.forms[0].AVERAGE_ALL.value = num9.toFixed(1);

    return
}
