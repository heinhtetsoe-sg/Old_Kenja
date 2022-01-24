function btn_submit(cmd) 
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(link)
{
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

function Keisan()
{
    var setSum;
    var setAvg;
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
    
    setSum = eval(document.forms[0].CONFIDENTIAL_RPT01.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT02.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT03.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT04.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT05.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT06.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT07.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT08.value)
    + eval(document.forms[0].CONFIDENTIAL_RPT09.value);
    
    setAvg = setSum/kyoukasu;

    document.forms[0].TOTAL_ALL.value = setSum;
    document.forms[0].AVERAGE_ALL.value = setAvg;

    return
}
