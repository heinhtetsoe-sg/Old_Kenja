<!--kanji=漢字-->
<!-- <?php # $RCSfile: knjb130Form1.js,v $ ?> -->
<!-- <?php # $Revision: 56585 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:47:53 +0900 (日, 22 10 2017) $ ?> -->

function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){
    if (document.forms[0].RADIO[1].checked == true)
    {
        if (document.forms[0].DATE1.value == "") {
            alert("指定範囲が正しく有りません。");
            document.forms[0].DATE1.focus();
            return;
        }
    }
    if (document.forms[0].RADIO[0].checked == true)
    {
        if(document.forms[0].TITLE.selectedIndex == 0)
        {
            alert("指定範囲が正しく有りません。");
            return;
        }
        var d = document.forms[0].TITLE.value;
        var tmp = d.split(',');
        document.forms[0].T_YEAR.value = tmp[0];
        document.forms[0].T_BSCSEQ.value = tmp[1];
        document.forms[0].T_SEMESTER.value = tmp[2];
    }
    var irekae = "";
    var check_from = document.forms[0].SECTION_CD_NAME1;
    var check_to   = document.forms[0].SECTION_CD_NAME2;
    
    if(check_from.value > check_to.value) {
        irekae           = check_to.value;
        check_to.value   = check_from.value;
        check_from.value = irekae;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function jikanwari(num){
    if(num.value == 1){
        flag1 = false;
        flag2 = true;
        document.forms[0].submit();
    } else {
        flag1 = true;
        flag2 = false;
        document.forms[0].DATE2.value = "";
        document.forms[0].TITLE.value = "";
        document.forms[0].submit();
    }
    document.forms[0].TITLE.disabled = flag1;
    document.forms[0].DATE1.disabled  = flag2;
    document.forms[0].btn_calen.disabled  = flag2;
}


function conbo_select(){
    var sdate;
    var fdate;
    
    sdate = fdate = "";
        var e = document.forms[0].TITLE;
            var val = e.value;
            if (val != ''){
                var tmp = val.split(',');
            
                sdate = tmp[2].substr(0,4) + "/" + tmp[2].substr(5,2) + "/" + tmp[2].substr(8,2);
                fdate = tmp[3].substr(0,4) + "/" + tmp[3].substr(5,2) + "/" + tmp[3].substr(8,2);
            }

    if (sdate != '' && fdate != ''){
        document.forms[0].DATE1.value = sdate;
        document.forms[0].DATE2.value = fdate;
    }

}
function dis_date(flag)
{
    document.forms[0].DATE1.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}

