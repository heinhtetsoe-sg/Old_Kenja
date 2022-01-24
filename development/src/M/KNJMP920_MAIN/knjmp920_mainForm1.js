//サブミット
function btn_submit(cmd)
{
    var trader_cd1 = document.forms[0].TRADER_CD1.value;
    var trader_name1 = document.forms[0].TRADER_NAME1.value;
    var trader_kakutei1 = document.forms[0].TRADER_KAKUTEI1.checked;
    var trader_cd2 = document.forms[0].TRADER_CD2.value;
    var trader_name2 = document.forms[0].TRADER_NAME2.value;
    var trader_kakutei2 = document.forms[0].TRADER_KAKUTEI2.checked;
    var trader_cd3 = document.forms[0].TRADER_CD3.value;
    var trader_name3 = document.forms[0].TRADER_NAME3.value;
    var trader_kakutei3 = document.forms[0].TRADER_KAKUTEI3.checked;
    var trader_cd4 = document.forms[0].TRADER_CD4.value;
    var trader_name4 = document.forms[0].TRADER_NAME4.value;
    var trader_kakutei4 = document.forms[0].TRADER_KAKUTEI4.checked;
    if (cmd == 'cancel') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'delete_update') {
        if ((trader_cd1 != "" && trader_name1 != "") || (trader_cd2 != "" && trader_name2 != "") || 
            (trader_cd3 != "" && trader_name3 != "") || (trader_cd4 != "" && trader_name4 != "")) {
            alert('{rval MSG302}' + '\n業者名は選択か入力のどちらか一つにして下さい。');
            return false;
        }
        if ((trader_cd1 == "" && trader_name1 == "" && trader_kakutei1 == true) || (trader_cd2 == "" && trader_name2 == "" && trader_kakutei2 == true) || 
            (trader_cd3 == "" && trader_name3 == "" && trader_kakutei3 == true) || (trader_cd4 == "" && trader_name4 == "" && trader_kakutei4 == true)) {
            alert('{rval MSG203}' + '\n業者名が空白の箇所には決定できません。');
            return false;
        }
        if (document.forms[0].KOUNYU_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(購入項目)');
            return false;
        }
        if (!confirm('{rval MSG102}' + '\購入項目が変更されています。\n変更前に登録されていた購入の振分け情報は全て削除されます。')){
            return false;
        }
    } else if (cmd == 'update') {
        if ((trader_cd1 != "" && trader_name1 != "") || (trader_cd2 != "" && trader_name2 != "") || 
            (trader_cd3 != "" && trader_name3 != "") || (trader_cd4 != "" && trader_name4 != "")) {
            alert('{rval MSG302}' + '\n業者名は選択か入力のどちらか一つにして下さい。');
            return false;
        }
        if ((trader_cd1 == "" && trader_name1 == "" && trader_kakutei1 == true) || (trader_cd2 == "" && trader_name2 == "" && trader_kakutei2 == true) || 
            (trader_cd3 == "" && trader_name3 == "" && trader_kakutei3 == true) || (trader_cd4 == "" && trader_name4 == "" && trader_kakutei4 == true)) {
            alert('{rval MSG203}' + '\n業者名が空白の箇所には決定できません。');
            return false;
        }
        if (document.forms[0].KOUNYU_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(購入項目)');
            return false;
        }
    } else if (cmd == 'delete' && !confirm('{rval MSG103}' + '\nただし、支出伺伝票は作成されていても削除されません。')){
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_error(flg)
{
    if (flg == 'huriwake') {
        alert('購入項目が変更されています。\n更新ボタンまたは取消ボタンで入力内容を確定してください。');
    } else if (flg == 'new') {
        alert('{rval MSG303}' + '\n更新ボタンを押して、入力内容を保存してください。');
    }
}

function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//購入金額計算
function keisanTotal(setvalue)
{
    var SumTotalPrice = document.forms[0].SUM_TOTAL_PRICE.value;
    var RequestTesuuryou = document.forms[0].REQUEST_TESUURYOU.value;
    SumTotalPrice = (isNaN(SumTotalPrice) || !SumTotalPrice) ? 0 : parseInt(SumTotalPrice, 10);
    RequestTesuuryou = (isNaN(RequestTesuuryou) || !RequestTesuuryou) ? 0 : parseInt(RequestTesuuryou, 10);
    
    document.getElementById("SUM_TOTAL_PRICE_ALL").innerHTML = SumTotalPrice + RequestTesuuryou;
}

//業者決定チェック
function tradercheckFlg(number)
{
    if (number == '1') {
        document.forms[0].TRADER_KAKUTEI2.checked = false;
        document.forms[0].TRADER_KAKUTEI3.checked = false;
        document.forms[0].TRADER_KAKUTEI4.checked = false;
    } else if (number == '2') {
        document.forms[0].TRADER_KAKUTEI1.checked = false;
        document.forms[0].TRADER_KAKUTEI3.checked = false;
        document.forms[0].TRADER_KAKUTEI4.checked = false;
    } else if (number == '3') {
        document.forms[0].TRADER_KAKUTEI1.checked = false;
        document.forms[0].TRADER_KAKUTEI2.checked = false;
        document.forms[0].TRADER_KAKUTEI4.checked = false;
    } else if (number == '4') {
        document.forms[0].TRADER_KAKUTEI1.checked = false;
        document.forms[0].TRADER_KAKUTEI2.checked = false;
        document.forms[0].TRADER_KAKUTEI3.checked = false;
    }
}
