function btn_submit(cmd) {
    if (cmd == "print") {
        if (document.forms[0].SLIP_NO.value == "") {
            alert('伝票番号を指定して下さい。');
            return false;
        }
        if (document.forms[0].LIMIT_DATE.value == "") {
            alert('納入期限を指定して下さい。');
            return false;
        }
    }
    if (cmd == "update") {
        re = new RegExp("^COLLECT_LM_CD_|MONEY_DUE_|COLLECT_CNT_" );
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re) && obj_updElement.value == "") {
                obj_updElement.focus();
                alert('商品名・単価・数量は必須です。');
                return false;
            }
        }
    }
    if (cmd == "delete") {
        if (document.forms[0].SLIP_NO.value == "") {
            alert('伝票番号を指定して下さい。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";

    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "DELCHK[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function changeCollectM(obj, cnt) {
    if (obj.value == "") {
        document.getElementById('TMONEY_DISP_' + cnt).innerHTML = "";
        return false;
    } else {
        document.forms[0]["MONEY_DUE_" + cnt].value = document.forms[0]["PRICE_" + obj.value].value;
    }
    changeTmoney(obj, cnt);
    return false;
}

function changeTmoney(obj, cnt) {
    var collectCnt = document.forms[0]["COLLECT_CNT_" + cnt].value;
    var moneyDue = document.forms[0]["MONEY_DUE_" + cnt].value;
    if (collectCnt == "" || moneyDue == "") {
        document.getElementById('TMONEY_DISP_' + cnt).innerHTML = "";
        return false;
    }
    document.getElementById('TMONEY_DISP_' + cnt).innerHTML = number_format(moneyDue * collectCnt);
    return false;
}
