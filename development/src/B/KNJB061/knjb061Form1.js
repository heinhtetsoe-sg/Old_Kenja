function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].RADIO[1].checked == true) {
        if (document.forms[0].DATE.value == "") {
            alert("指定範囲が正しく有りません。");
            document.forms[0].DATE.focus();
            return;
        }
    }
    if (document.forms[0].RADIO[0].checked == true) {
        if(document.forms[0].TITLE.selectedIndex == 0) {
            alert("指定範囲が正しく有りません。");
            return;
        }
        var d = document.forms[0].TITLE.value;
        var tmp = d.split(',');
        document.forms[0].T_YEAR.value = tmp[0];
        document.forms[0].T_BSCSEQ.value = tmp[1];
        document.forms[0].T_SEMESTER.value = tmp[2];
    }

    var irekae_class   = "";
    var irekae_section = "";
    var irekae_faccd   = "";
    var check_from_class   = document.forms[0].GRADE_HR_CLASS1;
    var check_to_class     = document.forms[0].GRADE_HR_CLASS2;
    var check_from_section = document.forms[0].SECTION_CD_NAME1;
    var check_to_section   = document.forms[0].SECTION_CD_NAME2;
    var check_from_faccd   = document.forms[0].FACCD_NAME1;
    var check_to_faccd     = document.forms[0].FACCD_NAME2;

    if(check_from_class.value > check_to_class.value) {
        irekae_class           = check_to_class.value;
        check_to_class.value   = check_from_class.value;
        check_from_class.value = irekae_class;
    }

    if(check_from_section.value > check_to_section.value) {
        irekae_section           = check_to_section.value;
        check_to_section.value   = check_from_section.value;
        check_from_section.value = irekae_section;
    }

    if(check_from_faccd.value > check_to_faccd.value) {
        irekae_faccd           = check_to_faccd.value;
        check_to_faccd.value   = check_from_faccd.value;
        check_from_faccd.value = irekae_faccd;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function jikanwari(num) {
    if (num.value == 1) {
        flag1 = false;
        flag2 = true;
        document.forms[0].submit();
    } else {
        flag1 = true;
        flag2 = false;
        document.forms[0].DATE2.value = "";
        document.forms[0].TITLE.value = "";
        var gaku = document.forms[0].GAKUSEKI.value;
        document.forms[0].DATE.value = gaku;
        document.forms[0].submit();
    }
    document.forms[0].TITLE.disabled = flag1;
    document.forms[0].DATE.disabled  = flag2;
    document.forms[0].btn_calen.disabled  = flag2;
}

function conbo_select() {
    var sdate;  //
    var fdate;  //

    sdate = fdate = "";
        var e = document.forms[0].TITLE;
            var val = e.value;
            if (val != '') {
                var tmp = val.split(',');
            
                sdate = tmp[2].substr(0,4) + "/" + tmp[2].substr(5,2) + "/" + tmp[2].substr(8,2);
                fdate = tmp[3].substr(0,4) + "/" + tmp[3].substr(5,2) + "/" + tmp[3].substr(8,2);
            }

    if (sdate != '' && fdate != '') {
        document.forms[0].DATE.value  = sdate;
        document.forms[0].DATE2.value = fdate;
    }

}
function shutu_kubun(num1){
    if(num1.value == 1){
        flag3 = false;
        flag4 = true;
        flag5 = true;
    }
    if(num1.value == 2){
        flag3 = true;
        flag4 = false;
        flag5 = true;
    }
    if(num1.value == 3){
        flag3 = true;
        flag4 = true;
        flag5 = false;
    }
    document.forms[0].GRADE_HR_CLASS1.disabled  = flag3;
    document.forms[0].GRADE_HR_CLASS2.disabled  = flag3;
    document.forms[0].SECTION_CD_NAME1.disabled = flag4;
    document.forms[0].SECTION_CD_NAME2.disabled = flag4;
    document.forms[0].FACCD_NAME1.disabled      = flag5;
    document.forms[0].FACCD_NAME2.disabled      = flag5;
}

function dis_date(flag) {
    document.forms[0].DATE.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}
