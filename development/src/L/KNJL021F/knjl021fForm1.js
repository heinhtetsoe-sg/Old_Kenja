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
//計算
function Keisan()
{
    var setSum;
    var setAvg;
    var kyoukasu = eval(document.forms[0].kyouka_count.value);
    
    rpt01 = (document.forms[0].CONFIDENTIAL_RPT01.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT01.value);
    rpt02 = (document.forms[0].CONFIDENTIAL_RPT02.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT02.value);
    rpt03 = (document.forms[0].CONFIDENTIAL_RPT03.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT03.value);
    rpt04 = (document.forms[0].CONFIDENTIAL_RPT04.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT04.value);
    rpt05 = (document.forms[0].CONFIDENTIAL_RPT05.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT05.value);
    rpt06 = (document.forms[0].CONFIDENTIAL_RPT06.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT06.value);
    rpt07 = (document.forms[0].CONFIDENTIAL_RPT07.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT07.value);
    rpt08 = (document.forms[0].CONFIDENTIAL_RPT08.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT08.value);
    rpt09 = (document.forms[0].CONFIDENTIAL_RPT09.value == "") ? 0 : eval(document.forms[0].CONFIDENTIAL_RPT09.value);

    setSum  = rpt01 + rpt02 + rpt03 + rpt04 + rpt05 + rpt06 + rpt07 + rpt08 + rpt09;
    setSum5 = rpt01 + rpt02 + rpt03 + rpt04 + rpt09;
    setSum3 = rpt01 + rpt03 + rpt09;

    document.forms[0].TOTAL_ALL.value = setSum;
    document.forms[0].TOTAL5.value = setSum5;
    document.forms[0].TOTAL3.value = setSum3;

    return
}
