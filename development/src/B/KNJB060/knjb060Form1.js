function jikanwari(num){
    if(num.value == 1){
        flag1 = false;
        flag2 = true;
        document.forms[0].cmd.value = "";
        document.forms[0].submit();
    } else {
        flag1 = true;
        flag2 = false;
        document.forms[0].DATE2.value = "";
        document.forms[0].TITLE.value = "";
        document.forms[0].TITLE2.value = "";
        var gaku = document.forms[0].GAKUSEKI.value;
        document.forms[0].DATE.value = gaku;
        document.forms[0].cmd.value = "";
        document.forms[0].submit();
    }
    document.forms[0].TITLE.disabled = flag1;
    document.forms[0].TITLE2.disabled = flag1;
    document.forms[0].DATE.disabled  = flag2;
    document.forms[0].btn_calen.disabled  = flag2;
}

function shutu_kubun(num1){
    if(num1.value == 1){
        flag3 = false;
        flag4 = true;
        flag5 = true;
        flag6 = true;
    }
    if(num1.value == 2){
        flag3 = true;
        flag4 = false;
        flag5 = true;
        flag6 = true;
    }
    if(num1.value == 3){
        flag3 = true;
        flag4 = true;
        flag5 = false;
        flag6 = true;
    }
    if(num1.value == 4){
        flag3 = true;
        flag4 = true;
        flag5 = true;
        flag6 = false;
    }
    document.forms[0].SECTION_CD_NAME1.disabled = flag3;
    document.forms[0].SECTION_CD_NAME2.disabled = flag3;
    document.forms[0].GRADE_HR_CLASS1.disabled  = flag4;
    document.forms[0].GRADE_HR_CLASS2.disabled  = flag4;
    document.forms[0].COURSE.disabled  = flag4;
    document.forms[0].GRADE_HR_CLASS3.disabled  = flag5;
    document.forms[0].GRADE_HR_CLASS4.disabled  = flag5;
    document.forms[0].FACCD_NAME1.disabled = flag6;
    document.forms[0].FACCD_NAME2.disabled = flag6;
}

function btn_submit(cmd) {
    if (cmd == 'csv') {
        //共通チェック：印刷およびＣＳＶ出力
        if (!checkPrintCsv()) return;
    }

//alert("工事中です。");
//return false;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){
    //共通チェック：印刷およびＣＳＶ出力
    if (!checkPrintCsv()) return;

    action = document.forms[0].action;
    target = document.forms[0].target;
    // document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function checkPrintCsv() {
    //単位制の場合、学級別は出力不可とする---2005.05.19
    if (document.forms[0].KUBUN[1].checked == true)
    {
        if (document.forms[0].SCHOOLDIV.value == 1 && document.forms[0].knjb060NoCheckTanisei.value != '1') {
            alert("単位制は、学級別を出力できません。");
            return false;
        }
    }
    if (document.forms[0].RADIO[1].checked == true)
    {
        if (document.forms[0].DATE.value == "") {
            alert("指定範囲が正しく有りません。");
            document.forms[0].DATE.focus();
            return false;
        }
    }
    if (document.forms[0].RADIO[0].checked == true)
    {
        if(document.forms[0].TITLE.selectedIndex == 0)
        {
            alert("指定範囲が正しく有りません。");
            return false;
        } else {
            var d = document.forms[0].TITLE.value;
            var tmp = d.split(',');
            document.forms[0].T_YEAR.value = tmp[0];
            document.forms[0].T_BSCSEQ.value = tmp[1];
            document.forms[0].T_SEMESTER.value = tmp[2];
        }
        if (document.forms[0].TITLE2.selectedIndex == 0) {
        } else {
            var d = document.forms[0].TITLE2.value;
            var tmp = d.split(',');
            document.forms[0].T_YEAR2.value = tmp[0];
            document.forms[0].T_BSCSEQ2.value = tmp[1];
            document.forms[0].T_SEMESTER2.value = tmp[2];
        }
    }

    var check_from   = document.forms[0].SECTION_CD_NAME1;
    var check_to     = document.forms[0].SECTION_CD_NAME2;
    var check_from12 = document.forms[0].GRADE_HR_CLASS1;
    var check_to12   = document.forms[0].GRADE_HR_CLASS2;
    var check_from34 = document.forms[0].GRADE_HR_CLASS3;
    var check_to34   = document.forms[0].GRADE_HR_CLASS4;
    var check_from_faccd = document.forms[0].FACCD_NAME1;
    var check_to_faccd   = document.forms[0].FACCD_NAME2;
    var irekae   = "";
    var irekae12 = "";
    var irekae34 = "";
    var irekae_faccd = "";
    
    if(check_from.value > check_to.value) {
        irekae           = check_to.value;
        check_to.value   = check_from.value;
        check_from.value = irekae;
    }

    if(check_from12.value > check_to12.value) {
        irekae12           = check_to12.value;
        check_to12.value   = check_from12.value;
        check_from12.value = irekae12;
    }

    if(check_from34.value > check_to34.value) {
        irekae34           = check_to34.value;
        check_to34.value   = check_from34.value;
        check_from34.value = irekae34;
    }

    if(check_from_faccd.value > check_to_faccd.value) {
        irekae_faccd           = check_to_faccd.value;
        check_to_faccd.value   = check_from_faccd.value;
        check_from_faccd.value = irekae_faccd;
    }

    return true;
}

function dis_date(flag)
{
    document.forms[0].DATE.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}
