function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( 入試制度 )');
        return;
    }
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 入試区分 )');
        return;
    }
    if (document.forms[0].RESALT1.checked == true) {
        if (document.forms[0].ORIHOUR.value == '' || document.forms[0].ORIMINUTE.value == '') {
            alert('{rval MSG301}\n( 帳票種類、時間 )');
            return;
        }
    }

    if (document.forms[0].ALLFLG2.checked == true) {
        if (document.forms[0].TEXTEXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function disAbled(obj) {
    if (obj.name == "RESALT") {
        if (obj.value == 1) {
            flgR = false;
        } else {
            flgR = true;
        }
        document.forms[0].JIZEN.disabled       = flgR;
        document.forms[0].ORIDATE.disabled     = flgR;
        document.forms[0].elements[7].disabled = flgR;
        document.forms[0].ORIHOUR.disabled     = flgR;
        document.forms[0].ORIMINUTE.disabled   = flgR;
        document.forms[0].JIZEN_UNPASS.disabled = !flgR;
    }
    if (obj.name == "ALLFLG") {
        if (obj.value == 1) {
            flgE = true;
        } else {
            flgE = false;
        }
        document.forms[0].TEXTEXAMNO.disabled = flgE;
    }
}
//数値チェック
function to_Integer(obj) {
    var checkString = obj.value;
    var newString = "";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= "0" && ch <= "9") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数値を入力してください。");
        obj.value="";
        return false;
    }

    switch(obj.name) {
        case "ORIHOUR":
        if((0 > obj.value || obj.value > 24) && (obj.value!="")) {
            alert("0から24の値を入力してください。");
            obj.value="";
            return false;
        }
        case "ORIMINUTE":
        if((0 > obj.value || obj.value > 59) && (obj.value!="")) {
            alert("0から59の値を入力してください。");
            obj.value="";
            return false;
        }
    }

    return true;
}
