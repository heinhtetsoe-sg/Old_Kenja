function btn_submit(cmd) {
    if (cmd == "update") {
        //割当グループコードが無い場合
        var grpCdFrom = document.forms[0].GRPCD_FROM.value;
        var grpCdTo = document.forms[0].GRPCD_TO.value;
        if (grpCdFrom == "" || grpCdTo == "") {
            alert("{rval MSG917}" + "\nグループコードの割振り範囲が設定されていません。");
            return;
        }
        var nextGrpCd = document.forms[0].NEXT_GRPCD.value;
        if (nextGrpCd == "") {
            alert("{rval MSG917}" + "\nグループコードが割振り範囲を超えています。");
            return;
        }

        if (document.forms[0].PLAN_PAID_MONEY_DATE.value == "") {
            alert("{rval MSG301}" + "(入金日)");
            return;
        }
        if (document.forms[0].PLAN_PAID_MONEY_DIV.value == "") {
            alert("{rval MSG301}" + "(入金方法)");
            return;
        }
        var paidMoneyList = document.querySelectorAll(".paid_money");

        //必須チェック
        if (document.forms[0].AUTO_KESIKOMI.checked) {
            //自動消込ON
            if (document.forms[0].AUTO_KESIKOMI_MONEY.value == "") {
                alert("{rval MSG301}" + "(入金額合計)");
                return;
            }
        } else {
            //自動消込OFF
            for (var i = 0; i < paidMoneyList.length; i++) {
                if (paidMoneyList[i].value == "") {
                    alert("{rval MSG301}" + "(入金額)");
                    return;
                }
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm("{rval MSG107}");
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calcMoney(obj, rowNo) {
    //数値チェック
    convValue = toInteger(obj.value);
    if (convValue != obj.value) {
        //数値以外が入力された場合は計算しない
        obj.value = convValue;
        return;
    }

    var dispPlanMoney = document.getElementById("DISP_PLAN_MONEY_" + rowNo).textContent.replace(",", "");

    //入金額が徴収予定額を超えた場合に徴収予定額をセット
    obj.value = Math.min(obj.value, dispPlanMoney);

    //残高算出
    var calcBalance = dispPlanMoney - obj.value;
    document.getElementById("BALANCE_" + rowNo).textContent = comma(calcBalance);

    //合計行計算
    calcTotalRow();
}

// 3桁カンマ区切りとする.
function comma(num) {
    return String(num).replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
}

function checkKesikomi(obj) {
    document.forms[0].AUTO_KESIKOMI_MONEY.disabled = !obj.checked;
    var paidMoneyList = document.querySelectorAll(".paid_money");
    for (var i = 0; i < paidMoneyList.length; i++) {
        paidMoneyList[i].readOnly = obj.checked; //POSTしたいのでreadonly
        paidMoneyList[i].style.backgroundColor = obj.checked ? "lightgray" : "white";
    }
}

//自動消込ON時の入金額合計振分け処理
function autoInputPaidMoney(obj) {
    var paidTotalMoney = obj.value;
    var planMoneyList = document.querySelectorAll(".plan_money > div");
    var paidMoneyList = document.querySelectorAll(".paid_money");
    var balanceList = document.querySelectorAll(".balance > div");

    //入金額が徴収予定額を超えた場合に徴収予定額をセット
    var totalPlanMoney = Number(document.getElementById("DISP_PLAN_MONEY_TOTAL").textContent.replace(",", ""));
    obj.value = Math.min(obj.value, totalPlanMoney);

    //初期化処理
    for (var i = 0; i < paidMoneyList.length; i++) {
        paidMoneyList[i].value = "";
        balanceList[i].textContent = planMoneyList[i].textContent;
    }

    //計算処理
    for (var i = 0; i < paidMoneyList.length; i++) {
        var planMoney = planMoneyList[i].textContent.replace(",", "");
        if (paidTotalMoney - planMoney >= 0) {
            paidMoneyList[i].value = planMoney;
            balanceList[i].textContent = 0;
            paidTotalMoney -= planMoney;
        } else {
            paidMoneyList[i].value = paidTotalMoney;
            balanceList[i].textContent = comma(planMoney - paidTotalMoney);
            break;
        }
    }

    //後処理  ※未振分けの項目に入金額0円をセット
    for (var i = 0; i < paidMoneyList.length; i++) {
        if (paidMoneyList[i].value == "") {
            paidMoneyList[i].value = 0;
        }
    }

    //合計行計算
    calcTotalRow();
}

//入金額入力時に合計行の金額を再計算
function calcTotalRow() {
    var planMoneyList = document.querySelectorAll(".plan_money > div");
    var paidMoneyList = document.querySelectorAll(".paid_money");
    var balanceList = document.querySelectorAll(".balance > div");

    var calcPlanMoney = 0;
    var calcPaidMoney = 0;
    var calcBalance = 0;
    for (var i = 0; i < planMoneyList.length; i++) {
        calcPlanMoney += Number(planMoneyList[i].textContent.replace(",", ""));
        calcPaidMoney += Number(paidMoneyList[i].value);
        calcBalance += Number(balanceList[i].textContent.replace(",", ""));
    }

    //合計行更新
    var totalPlanDiv = document.getElementById("DISP_PLAN_MONEY_TOTAL");
    var totalPaidDiv = document.getElementById("PAID_MONEY_TOTAL");
    var totalBalanceDiv = document.getElementById("BALANCE_TOTAL");
    totalPlanDiv.textContent = comma(calcPlanMoney);
    totalPaidDiv.textContent = comma(calcPaidMoney);
    totalBalanceDiv.textContent = comma(calcBalance);

    //hidden更新
    document.forms[0].DISP_PLAN_MONEY_TOTAL.value = calcPlanMoney;
    document.forms[0].PAID_MONEY_TOTAL.value = calcPaidMoney;
    document.forms[0].BALANCE_TOTAL.value = calcBalance;
}
