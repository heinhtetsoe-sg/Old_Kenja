function btn_submit(cmd) {

    //日付範囲、執務者入力チェック
    if (cmd == "add" || cmd == "update" || cmd == "delete") {
        if (document.forms[0].WORK_DATE.value == "" || document.forms[0].STAFFCD.value == "") {
            alert('{rval MSG301}');
            return false;
        }

        chk_date = document.forms[0].WORK_DATE.value.replace("/","-");
        chk_date = chk_date.replace("/","-");
        sdate = document.forms[0].YEAR.value+'-04-01';
        edate = parseInt(document.forms[0].YEAR.value)+1+'-03-31';

        if (chk_date < sdate || edate < chk_date) {

            chk_date = document.forms[0].WORK_DATE.value.replace("-","/");
            chk_date = chk_date.replace("-","/");
            sdate = document.forms[0].YEAR.value+'/04/01';
            edate = parseInt(document.forms[0].YEAR.value)+1+'/03/31';

            alert('{rval MSG916}\n'+sdate+'～'+edate);
            return false;
        }
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {

    //日付、執務者入力チェック
    if (document.forms[0].WORK_DATE.value == "") {
        alert('{rval MSG304}'+'(教務日時)');
        return false;
    }
    if (document.forms[0].STAFFCD.value == "") {
        alert('{rval MSG304}'+'(執務者)');
        return false;
    }
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function etc_check(seq) {
    if (seq === '000') {
        if (document.forms[0].REMARK6_000.checked == true) {
            document.forms[0].REMARK7_000.disabled = "";
        } else {
            document.forms[0].REMARK7_000.disabled = "true";
        }
        return;
    }
    if (seq === '001') {
        if (document.forms[0].REMARK1_001.checked == true) {
            document.forms[0].REMARK2_001.disabled = "";
            document.forms[0].REMARK3_001.disabled = "";
            document.forms[0].REMARK4_001.disabled = "";
            if (document.forms[0].REMARK3_001.checked == true) {
                document.forms[0].REMARK4_001.disabled = "";
            } else {
                document.forms[0].REMARK4_001.disabled = "true";
            }
        } else {
            document.forms[0].REMARK2_001.disabled = "true";
            document.forms[0].REMARK3_001.disabled = "true";
            document.forms[0].REMARK4_001.disabled = "true";
        }
        return;
    }
    
    if (seq === '003') {
        if (document.forms[0].REMARK1_003.checked == true) {
            document.forms[0].REMARK2_003.disabled = "";
            document.forms[0].REMARK3_003.disabled = "";
            document.forms[0].REMARK4_003.disabled = "";
            document.forms[0].REMARK5_003.disabled = "";
            document.forms[0].REMARK6_003.disabled = "";
            if (document.forms[0].REMARK5_003.checked == true) {
                document.forms[0].REMARK6_003.disabled = "";
            } else {
                document.forms[0].REMARK6_003.disabled = "true";
            }
        } else {
            document.forms[0].REMARK2_003.disabled = "true";
            document.forms[0].REMARK3_003.disabled = "true";
            document.forms[0].REMARK4_003.disabled = "true";
            document.forms[0].REMARK5_003.disabled = "true";
            document.forms[0].REMARK6_003.disabled = "true";
        }
        return;
    }
    
    if (seq === '004') {
        if (document.forms[0].REMARK1_004.checked == true) {
            document.forms[0].REMARK2_004.disabled = "";
            document.forms[0].REMARK3_004.disabled = "";
            document.forms[0].REMARK4_004.disabled = "";
            document.forms[0].REMARK5_004.disabled = "";
            if (document.forms[0].REMARK4_004.checked == true) {
                document.forms[0].REMARK5_004.disabled = "";
            } else {
                document.forms[0].REMARK5_004.disabled = "true";
            }
        } else {
            document.forms[0].REMARK2_004.disabled = "true";
            document.forms[0].REMARK3_004.disabled = "true";
            document.forms[0].REMARK4_004.disabled = "true";
            document.forms[0].REMARK5_004.disabled = "true";
        }
        return;
    }
    
    if (seq === '006') {
        if (document.forms[0].REMARK1_006.checked == true) {
            document.forms[0].REMARK2_006.disabled = "";
            document.forms[0].REMARK3_006.disabled = "";
            document.forms[0].REMARK4_006.disabled = "";
            document.forms[0].REMARK5_006.disabled = "";
            document.forms[0].REMARK6_006.disabled = "";
            if (document.forms[0].REMARK5_006.checked == true) {
                document.forms[0].REMARK6_006.disabled = "";
            } else {
                document.forms[0].REMARK6_006.disabled = "true";
            }
        } else {
            document.forms[0].REMARK2_006.disabled = "true";
            document.forms[0].REMARK3_006.disabled = "true";
            document.forms[0].REMARK4_006.disabled = "true";
            document.forms[0].REMARK5_006.disabled = "true";
            document.forms[0].REMARK6_006.disabled = "true";
        }
        return;
    }
    
    if (seq === '009') {
        if (document.forms[0].REMARK1_009.checked == true) {
            document.forms[0].REMARK2_009.disabled = "";
        } else {
            document.forms[0].REMARK2_009.disabled = "true";
        }
        return;
    }
}

function OnAuthError()
{
   alert('{rval MSG300}');
   closeWin();
}
