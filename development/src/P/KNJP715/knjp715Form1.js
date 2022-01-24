function btn_submit(cmd) {
    if (cmd == "update") {
        re = new RegExp("^COLLECT_LM_CD_|COLLECT_MONEY_|COLLECT_CNT_" );
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re) && obj_updElement.value == "") {
                obj_updElement.focus();
                alert('入金項目・単価・数量は必須です。');
                return false;
            }
        }
    }

    //指定行削除
    if (cmd == "delLine") {
        var counter = 0;
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.name.match(/^DELCHK_/) && e.checked == false) {
                counter++;
            }
        }

        if (document.forms[0].maxSeq.value == counter) {
            alert('行が指定されていません。');
            return false;
        }

        if (counter == 0) {
            if (document.forms[0].SLIP_NO.value == "") {
                alert('全ての行を削除することはできません。');
                return false;
            } else {
                if (!confirm('伝票を削除します。よろしいでしょうか？')) {
                    return false;
                }
                document.forms[0].cmd.value = 'delete';
                document.forms[0].submit();
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック
function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/^DELCHK_/) && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function changeCollectM(obj, cnt) {
    if (obj.value == "") {
        document.getElementById('TMONEY_DISP_' + cnt).innerHTML = "";
        return false;
    } else {
        //単価セット
        document.forms[0]["COLLECT_MONEY_" + cnt].value = document.forms[0]["PRICE_" + obj.value].value;
        if (document.forms[0]["SET_TEXTBOOKDIV_" + obj.value].value != "") {
            document.forms[0]["COLLECT_MONEY_" + cnt].style.backgroundColor = "\#999999";
            document.forms[0]["COLLECT_MONEY_" + cnt].readOnly = true;
            document.forms[0]["COLLECT_CNT_" + cnt].style.backgroundColor = "\#999999";
            document.forms[0]["COLLECT_CNT_" + cnt].readOnly = true;
            document.forms[0]["HIDDEN_TEXTBOOKDIV_" + cnt].value = document.forms[0]["SET_TEXTBOOKDIV_" + obj.value].value;
        } else {
            document.forms[0]["COLLECT_MONEY_" + cnt].style.backgroundColor = "white";
            document.forms[0]["COLLECT_MONEY_" + cnt].readOnly = false;
            document.forms[0]["COLLECT_CNT_" + cnt].style.backgroundColor = "white";
            document.forms[0]["COLLECT_CNT_" + cnt].readOnly = false;
        }

        //数量セット
        var cre = document.forms[0]["IS_CREDITCNT"].value.split(",");
        var flg = false;
        for (var i=0; i < cre.length; i++) {
            if (cre[i] == obj.value) flg = true;
        }
        if (flg) {
            document.forms[0]["COLLECT_CNT_" + cnt].value = document.forms[0]["CREDITS"].value;
        } else {
            document.forms[0]["COLLECT_CNT_" + cnt].value = 1;
        }
    }

    changeTmoney(obj, cnt);
    return false;
}

function changeTmoney(obj, cnt) {
    var collectCnt = document.forms[0]["COLLECT_CNT_" + cnt].value;
    var moneyDue = document.forms[0]["COLLECT_MONEY_" + cnt].value;
    if (collectCnt == "" || moneyDue == "") {
        document.getElementById('TMONEY_DISP_' + cnt).innerHTML = "";
        return false;
    }
    document.getElementById('TMONEY_DISP_' + cnt).innerHTML = number_format(moneyDue * collectCnt);
    return false;
}

//日付チェック
function checkDate(obj) {
    if (obj.value.length > 0) {
        if (obj.value == 0 || obj.value > 31) {
            alert('引き落とし日の指定が間違っています。\n(1～31まで)');
            obj.focus();
            obj.select();
        } else {
            defVal = toInteger(obj.value);
            if (defVal != obj.value) {
                obj.value = defVal;
                obj.focus();
                obj.select();
            }
        }
    }
}

function checkedMethod(obj) {
    for (var monthVal = 4; monthVal <= 15; monthVal++) {
        setMonth = monthVal > 12 ? monthVal - 12 : monthVal;
        document.forms[0]["COLLECT_MONTH_" + setMonth].value = obj.value;
    }
}

//disabled
function OptionUse(obj) {
    if (!obj.value.length) {
        flg = false;
        document.forms[0].CANCEL_REASON.disabled = true;
    } else {
        flg = true;
        document.forms[0].CANCEL_REASON.disabled = false;
    }

    //対象項目
    var array = ["PAY_DIV", "SLIP_DATE", "CHECKALL", "DELCHK", "COLLECT_LM_CD_", "COLLECT_MONEY_", "COLLECT_CNT_", "btn_addline", "btn_delline"];
    for (var i=0; i < document.forms[0].elements.length; i++) {
        for (var j = 0; j < array.length; j++) {
            var reg = new RegExp("^" + array[j]);
            if (document.forms[0].elements[i].name.match(reg)) {
                document.forms[0].elements[i].disabled = flg;
            }
        }
    }
}
