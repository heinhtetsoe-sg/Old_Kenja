function btn_submit(cmd) {
    //項目更新
    if (cmd == "dueUpd") {
        if (document.forms[0].COLLECT_GRP_CD.value == "") {
            alert('グループコードを指定して下さい。');
            return false;
        }
        if (document.forms[0].COLLECT_LM_CD.value == "") {
            alert('会計項目を指定して下さい。');
            return false;
        }
        if (document.forms[0].MONEY_DUE.value === "") {
            alert('入金必要額を指定して下さい。');
            return false;
        }
        if (document.forms[0].PAY_DATE.value == "") {
            alert('納入期限を指定して下さい。');
            return false;
        }
    }
    //項目削除
    if (cmd == "dueDel") {
        if (document.forms[0].TMP_COLLECT_GRP_CD.value == "" || document.forms[0].TMP_EXP_LCD.value == "" || document.forms[0].TMP_EXP_MCD.value == "") {
            alert('データを指定して下さい。');
            return false;
        }
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function blockDispChange(obj)
{
    var genmen = document.getElementById('TR_GENMEN');
    var sinseiDate = document.getElementById('TR_SINSEI_DATE');
    if (obj.value == "01") {
        sinseiDate.style.display = '';
        genmen.style.display = 'none';
        document.forms[0].GENMEN_MONEY.value = '';
        document.forms[0].GENMEN_CNT.value = '';
    } else if (obj.value == "02") {
        sinseiDate.style.display = 'none';
        genmen.style.display = '';
        document.forms[0].SINSEI_DATE.value = '';
    } else {
        sinseiDate.style.display = 'none';
        genmen.style.display = 'none';
        document.forms[0].SINSEI_DATE.value = '';
        document.forms[0].GENMEN_MONEY.value = '';
        document.forms[0].GENMEN_CNT.value = '';
    }
    return;
}

function moneyDue_check1()
{
    var collect_cnt = document.forms[0].COLLECT_CNT.value != "" ? parseInt(document.forms[0].COLLECT_CNT.value) : 0;
    var tanka = document.forms[0].TANKA.value != "" ? parseInt(document.forms[0].TANKA.value) : 0;
    var setDue = tanka * collect_cnt;
    document.forms[0].MONEY_DUE.value = setDue;
    return;
}

function money_check1()
{
    var paidMoney = document.forms[0].PAID_MONEY.value != "" ? parseInt(document.forms[0].PAID_MONEY.value) : 0;
    var moneyDue = document.forms[0].TMP_MONEY_DUE.value != "" ? parseInt(document.forms[0].TMP_MONEY_DUE.value) : 0;
    var repayAll = document.forms[0].REPAY_MONEY_ALL.value != "" ? parseInt(document.forms[0].REPAY_MONEY_ALL.value) : 0;
    var paidTotal = document.forms[0].PAID_MONEY_TOTAL.value != "" ? parseInt(document.forms[0].PAID_MONEY_TOTAL.value) : 0;
    paidTotal += paidMoney;

    if (paidTotal > moneyDue) {
        alert('入金必要額を超えています。\n入金必要額:' + moneyDue + '\n合計入金額:' + paidTotal);
        document.forms[0].PAID_MONEY.value = '';
        document.forms[0].PAID_MONEY.focus();
        return true;
    }

    if (paidTotal < repayAll) {
        alert('返金額より少ないです。\n返金額:' + repayAll + '\n合計入金額:' + paidTotal);
        document.forms[0].PAID_MONEY.value = '';
        document.forms[0].PAID_MONEY.focus();
        return true;
    }
    return;
}

function money_check2(obj)
{
    var repayMoney = document.forms[0].REPAY_MONEY.value != "" ? parseInt(document.forms[0].REPAY_MONEY.value) : 0;
    var paidAll = document.forms[0].PAID_MONEY_ALL.value != "" ? parseInt(document.forms[0].PAID_MONEY_ALL.value) : 0;
    var repayTotal = document.forms[0].REPAY_MONEY_TOTAL.value != "" ? parseInt(document.forms[0].REPAY_MONEY_TOTAL.value) : 0;
    repayTotal += repayMoney;

    if (repayTotal > paidAll) {
        alert('入金額を超えています。\n入金額:' + paidAll + '\n合計返金額:' + repayTotal);
        document.forms[0].REPAY_MONEY.value = '';
        document.forms[0].REPAY_MONEY.focus();
        return true;
    }

    return;
}
function init(){
    try{    
        parent.top_frame.document.getElementById("btn_end").style.display = "none";
    }catch(e){
    }
}
window.onload = init;