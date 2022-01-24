//サブミット
function btn_submit(cmd)
{
    var requestGk = document.forms[0].REQUEST_GK.value;
    var SumTotalPrice = document.forms[0].SUM_TOTAL_PRICE.value;
    var RequestTesuuryou = document.forms[0].REQUEST_TESUURYOU.value;
    var totalzanGk = document.forms[0].TOTAL_ZAN_GK.value;
    //精算票金額
    var seisanSiharaiGk = document.forms[0].SEISAN_SIHARAIGK.value;

    requestGk = (isNaN(requestGk) || !requestGk) ? 0 : parseInt(requestGk, 10);
    SumTotalPrice = (isNaN(SumTotalPrice) || !SumTotalPrice) ? 0 : parseInt(SumTotalPrice, 10);
    RequestTesuuryou = (isNaN(RequestTesuuryou) || !RequestTesuuryou) ? 0 : parseInt(RequestTesuuryou, 10);
    totalzanGk = (isNaN(totalzanGk) || !totalzanGk) ? 0 : parseInt(totalzanGk, 10);
    seisanSiharaiGk = (isNaN(seisanSiharaiGk) || !seisanSiharaiGk) ? 0 : parseInt(seisanSiharaiGk, 10);

    if (cmd == 'cancel') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'delete_update') {
        if (document.forms[0].INCOME_L_CD.value == "") {
            alert('{rval MSG301}' + '(収入科目)');
            return false;
        }
        if (document.forms[0].OUTGO_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(支出項目)');
            return false;
        }
        if (document.forms[0].REQUEST_GK.value == "") {
            alert('{rval MSG301}' + '(支出額)');
            return false;
        }
        if (document.forms[0].REQUEST_DATE.value == "") {
            alert('{rval MSG301}' + '(支出伺い日)');
            return false;
        }
        if (requestGk < SumTotalPrice + RequestTesuuryou) {
            alert('{rval MSG203}' + '\n摘要総額が支出額を超えています。');
            return false;
        }
        if (totalzanGk < 0) {
            alert('{rval MSG203}' + '\n支出額が収入残高を超えています。');
            return false;
        }
        //精算票の支払額と支出額の比較
        if (document.forms[0].SEISAN_REQUEST_NO.value) {
            if (requestGk != seisanSiharaiGk) {
                alert('{rval MSG203}' + '\n支出額と精算票の支払額が一致していません。');
                return false;
            }
        }
        if (!confirm('{rval MSG102}' + '\n生徒返金用チェック、収入科目、支出項目、入金細目(返金用)のいずれかが変更されています。\n変更前に登録されていた支出の振分け情報は全て削除されます。')){
            return false;
        }
    } else if (cmd == 'update') {
        if (document.forms[0].INCOME_L_CD.value == "") {
            alert('{rval MSG301}' + '(収入科目)');
            return false;
        }
        if (document.forms[0].OUTGO_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(支出項目)');
            return false;
        }
        if (document.forms[0].REQUEST_GK.value == "") {
            alert('{rval MSG301}' + '(支出額)');
            return false;
        }
        if (document.forms[0].REQUEST_DATE.value == "") {
            alert('{rval MSG301}' + '(支出伺い日)');
            return false;
        }
        if (requestGk < SumTotalPrice + RequestTesuuryou) {
            alert('{rval MSG203}' + '\n摘要総額が支出額を超えています。');
            return false;
        }
        if (totalzanGk < 0) {
            alert('{rval MSG203}' + '\n支出額が収入残高を超えています。');
            return false;
        }
        //精算票の支払額と支出額の比較
        if (document.forms[0].SEISAN_REQUEST_NO.value) {
            if (requestGk != seisanSiharaiGk) {
                alert('{rval MSG203}' + '\n支出額と精算票の支払額が一致していません。');
                return false;
            }
        }
    } else if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_error(flg)
{
    if (flg == 'huriwake') {
        alert('生徒返金用チェック、収入項目、支出科目、支出額、入金細目(返金用)、業者情報のいずれかが変更されています。更新ボタンまたは取消ボタンで入力内容を確定してください。');
    } else if (flg == 'new') {
        alert('{rval MSG303}' + '\n更新ボタンを押して、入力内容を保存してください。');
    } else if (flg == 'kessai') {
        alert('支出額と摘要総額の金額が異なります。');
    } else if (flg == 'seisan') {
        alert('支出額と精算票の支払額が一致していません。');
    } else if (flg == 'chenge_error') {
        alert('{rval MSG203}' + '購入伺、施行伺、精算票のいずれかが既に作成されています。\n支出項目は変更することができません。');
    } else if (flg == 'henkin_error') {
        alert('{rval MSG203}' + '購入伺、施行伺、精算票のいずれかが既に作成されています。\n生徒返金用の支出伺は新規で作成してください。');
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

//収入残高計算
function keisanTotal(setvalue)
{
    var SumTotalPrice = document.forms[0].SUM_TOTAL_PRICE.value;
    var RequestTesuuryou = document.forms[0].REQUEST_TESUURYOU.value;
    SumTotalPrice = (isNaN(SumTotalPrice) || !SumTotalPrice) ? 0 : parseInt(SumTotalPrice, 10);
    RequestTesuuryou = (isNaN(RequestTesuuryou) || !RequestTesuuryou) ? 0 : parseInt(RequestTesuuryou, 10);
    
    document.getElementById("SUM_TOTAL_PRICE_ALL").innerHTML = SumTotalPrice + RequestTesuuryou;
}
