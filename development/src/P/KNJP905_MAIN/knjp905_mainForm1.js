//サブミット
function btn_submit(cmd) {
    var requestGk     = document.forms[0].REQUEST_GK.value;
    var SumTotalPrice = document.forms[0].SUM_TOTAL_PRICE.value;
    var totalzanGk    = document.forms[0].TOTAL_ZAN_GK.value;
    //精算票金額
    var seisanSiharaiGk = document.forms[0].SEISAN_SIHARAIGK.value;

    requestGk       = (isNaN(requestGk)       || !requestGk)       ? 0 : parseInt(requestGk, 10);
    SumTotalPrice   = (isNaN(SumTotalPrice)   || !SumTotalPrice)   ? 0 : parseInt(SumTotalPrice, 10);
    totalzanGk      = (isNaN(totalzanGk)      || !totalzanGk)      ? 0 : parseInt(totalzanGk, 10);
    seisanSiharaiGk = (isNaN(seisanSiharaiGk) || !seisanSiharaiGk) ? 0 : parseInt(seisanSiharaiGk, 10);

    //手数料／備考入力
    if (cmd == 'tesuryo_bikou') {
        var setParam = '';
        setParam = setParam + '&SUB1_SCHOOLCD='+document.forms[0].SCHOOLCD.value;
        setParam = setParam + '&SUB1_SCHOOL_KIND='+document.forms[0].SCHOOL_KIND.value;
        setParam = setParam + '&SUB1_YEAR='+document.forms[0].GET_YEAR.value;
        setParam = setParam + '&SUB1_OUTGO_L_CD='+document.forms[0].GET_OUTGO_L_CD.value;
        setParam = setParam + '&SUB1_OUTGO_M_CD='+document.forms[0].GET_OUTGO_M_CD.value;
        setParam = setParam + '&SUB1_REQUEST_NO='+document.forms[0].REQUEST_NO.value;

        var attBttn = document.forms[0].tesuryo_bikou.getBoundingClientRect();
        var setX    = attBttn.left + window.pageXOffset - 650;
        var setY    = attBttn.top + window.pageYOffset - 200;

        loadwindow('knjp905_mainindex.php?cmd=' + cmd + setParam, setX, setY, 650, 200);
        return true;
    }

    if (cmd == 'cancel') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'delete_update') {
        if (document.forms[0].INCOME_LM_CD.value == "") {
            alert('{rval MSG301}' + '(収入項目)');
            return false;
        }
        if (document.forms[0].OUTGO_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(支出項目)');
            return false;
        }
        if (document.forms[0].REQUEST_DATE.value == "") {
            alert('{rval MSG301}' + '(支出伺い日)');
            return false;
        }
        //精算票の支払額と支出額の比較
        if (document.forms[0].SEISAN_REQUEST_NO.value) {
            if (requestGk != seisanSiharaiGk) {
                alert('{rval MSG203}' + '\n支出額と精算票の支払額が一致していません。');
                return false;
            }
        }
        if (!confirm('{rval MSG102}' + '\n収入項目、支出項目のいずれかが変更されています。\n変更前に登録されていた支出の振分け情報は全て削除されます。')){
            return false;
        }
    } else if (cmd == 'update') {
        if (document.forms[0].INCOME_LM_CD.value == "") {
            alert('{rval MSG301}' + '(収入項目)');
            return false;
        }
        if (document.forms[0].OUTGO_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(支出項目)');
            return false;
        }
        if (document.forms[0].REQUEST_DATE.value == "") {
            alert('{rval MSG301}' + '(支出伺い日)');
            return false;
        }
        //精算票の支払額と支出額の比較
        if (document.forms[0].SEISAN_REQUEST_NO.value) {
            if (requestGk != seisanSiharaiGk) {
                alert('{rval MSG203}' + '\n支出額と精算票の支払額が一致していません。');
                return false;
            }
        }
    } else if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    } else if (cmd == 'line_copy') {
        var koumoku       = document.forms[0].koumoku.value;
        var chkCntKoumoku = document.forms[0].chkCntKoumoku.value;
        var cntKoumoku    = 0;

        if (!confirm('{rval MSG101}')) {
            alert('{rval MSG203}');
            return false;
        }

        if (document.forms[0].INCOME_LM_CD.value == "") {
            alert('{rval MSG301}' + '(収入項目)');
            return false;
        }

        for (var i=1; i <= koumoku; i++) {
            if (document.forms[0]['COPY_CHECK' + i] && document.forms[0]['COPY_CHECK' + i].checked == true) {
                cntKoumoku++;
            }
        }
        if (cntKoumoku == 0) {
            alert('{rval MSG203}' + '\n最低ひとつチェックを入れてください。');
            return false;
        }
        var totalCnt = parseInt(cntKoumoku) + parseInt(chkCntKoumoku);
        if (koumoku < totalCnt) {
            alert('{rval MSG203}' + '\n合計項目数が10を超えています。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_error(flg) {
    if (flg == 'huriwake') {
        alert('収入項目、支出項目、支出額、業者情報のいずれかが変更されています。更新ボタンまたは取消ボタンで入力内容を確定してください。');
    } else if (flg == 'new') {
        alert('{rval MSG303}' + '\n更新ボタンを押して、入力内容を保存してください。');
    } else if (flg == 'kessai') {
        alert('支出額と摘要総額の金額が異なります。');
    } else if (flg == 'seisan') {
        alert('支出額と精算票の支払額が一致していません。');
    } else if (flg == 'chenge_error') {
        alert('{rval MSG203}' + '購入伺、施行伺、精算票のいずれかが既に作成されています。\n支出項目は変更することができません。');
    }
}

function newwin(SERVLET_URL) {

    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//収入残高計算
function keisanTotal(setvalue) {
    var SumTotalPrice = document.forms[0].SUM_TOTAL_PRICE.value;
    SumTotalPrice = (isNaN(SumTotalPrice) || !SumTotalPrice) ? 0 : parseInt(SumTotalPrice, 10);

    document.getElementById("SUM_TOTAL_PRICE_ALL").innerHTML = SumTotalPrice;
}
function chkAll(obj) {
    var koumoku = document.forms[0].koumoku.value;
    var lineCnt = document.forms[0].chkCntKoumoku.value;

    if (obj.checked == true) {
        for (var i=1; i <= lineCnt; i++) {
            if (document.forms[0]['COPY_CHECK' + i]) {
                document.forms[0]['COPY_CHECK' + i].checked = true;
            }
        }
    } else {
        for (var i=1; i <= koumoku; i++) {
            if (document.forms[0]['COPY_CHECK' + i]) {
                document.forms[0]['COPY_CHECK' + i].checked = false;
            }
        }
    }
}
