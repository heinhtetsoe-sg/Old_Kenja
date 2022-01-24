function btn_submit(cmd) {

    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);
    document.forms[0].windowHeight.value = bodyHeight;

    if (cmd == "update") {
        var checkFlg = false;
        re = new RegExp("CHECK_BOX:");
        var allCnt = 0;
        var checkedCnt = 0;
        if (document.forms[0].OUTGO_DATE.value == "") {
            alert('返金日付を指定してください。');
            return false;
        }
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re)) {
                if (obj_updElement.checked == true) {
                    checkFlg = true;
                    checkedCnt++;
                }
                allCnt++;
            }
        }
        if (!checkFlg) {
            alert('最低ひとつチェックを入れてください。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//画面リサイズ
function submit_reSize() {
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.getElementById("tbody").style.height = bodyHeight - 200;
}
//全てチェックボックス
function allCheck(obj) {
    var lmcd = obj.name.split(':')[1].split('-')[0] + '-' + obj.name.split(':')[1].split('-')[1];

    re = new RegExp("CHECK_BOX:" + lmcd);

    for (var i=0; i < document.forms[0].elements.length; i++) {
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {
            obj_updElement.checked = obj.checked;
            textOpen(obj_updElement, '1');
        }
    }

    //合計、差額計算
    calcTotal();

    return true;
}
//チェックボックス
function textOpen(obj, allFlg) {
    var lmsCd = obj.getAttribute('data-name').split(':')[1];
    var checkBoxName = 'CHECK_BOX:' + lmsCd;
    var textBoxName  = 'KYUFU:' + lmsCd;

    if (document.forms[0][checkBoxName].checked == true) {
        document.forms[0][textBoxName].disabled = false;
        //給付上限をセット
        var setMoney = document.getElementById("OUTGO_MONEY_ID:" + lmsCd).innerHTML.replace(',', '');
        document.forms[0][textBoxName].value = setMoney;
    } else {
        document.forms[0][textBoxName].value = '';
        document.forms[0][textBoxName].disabled = true;
    }
    if (allFlg == '') {
        //合計、差額計算
        calcTotal();
    }

    return true;
}
//数値チェック
function checkValue(obj) {
    var lmsCd = obj.name.split(':')[1];
    var jougenMoney = document.getElementById("OUTGO_MONEY_ID:" + lmsCd).innerHTML.replace(',', '')

    if (jougenMoney < obj.value) {
        alert('上限は ' + jougenMoney + '円です。');
        obj.value = '';
    }
    //合計、差額計算
    calcTotal();

    return;
}
//項目合計、給付合計、差引計算
function calcTotal() {
    var kyufuLMTotal = 0;
    var kyufuTotal   = 0;
    var lmsCd = '';
    var befLM = '';

    var lmsList = document.forms[0].textBoxList.value.split(',');

    for (var i=0; i < lmsList.length; i++) {
        lmsCd = lmsList[i];
        var lmCd = lmsList[i].split('-')[0] + lmsList[i].split('-')[1];

        if (befLM != lmCd) {
            kyufuLMTotal = 0;
        }

        if (document.forms[0]["KYUFU:" + lmsCd]) {
            var obj = document.forms[0]["KYUFU:" + lmsCd];

            if (obj.value != '') {
                //給付合計
                kyufuTotal += parseInt(obj.value);

                //項目合計
                kyufuLMTotal += parseInt(obj.value);
            }
        }
        document.getElementById("KYUFU_LM_TOTAL_ID:" + lmCd).innerHTML = kyufuLMTotal.toLocaleString();
        document.forms[0]["HID_KYUFU_LM_TOTAL:" + lmCd].value = kyufuLMTotal;
        befLM = lmCd;
    }

    //給付合計
    document.getElementById("KYUFU_TOTAL").innerHTML = kyufuTotal.toLocaleString();
    document.forms[0].KYUFU_TOTAL.value              = kyufuTotal;
    document.forms[0].HID_KYUFU_TOTAL.value           = kyufuTotal;

    //差引
    var sagaku = document.forms[0].MAX_BENE.value - kyufuTotal;
    document.getElementById("KYUFU_SAGAKU").innerHTML = sagaku.toLocaleString();
    document.forms[0].KYUFU_SAGAKU.value              = sagaku;
    document.forms[0].HID_KYUFU_SAGAKU.value          = sagaku;
    if (sagaku != '') {
        if (sagaku < 0) {
            document.getElementById("KYUFU_SAGAKU").style.color = 'red';
        } else {
            document.getElementById("KYUFU_SAGAKU").style.color = 'black';
        }
    }
}
//帳票印刷
function newwin(SERVLET_URL) {

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
