function SearchResult()
{
    alert('データは存在していません。');
}

function GoWin(company_cd)
{
    var programid = document.forms[0].programid.value;
    if (programid == "KNJZ421B") {
        top.opener.document.forms[0].COMPANY_CD.value = company_cd;
    } else {
        top.opener.document.forms[0].STAT_CD.value = company_cd;
        if (top.opener.document.forms[0].SENKOU_NO != undefined) {
            top.opener.document.forms[0].SENKOU_NO.value = "";
        }
    }
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}
