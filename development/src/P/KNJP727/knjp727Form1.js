
function btn_submit(cmd) {
    bodyWidth  = (window.innerWidth  || document.body.clientWidth || 0);
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.forms[0].windowWidth.value = bodyWidth;
    document.forms[0].windowHeight.value = bodyHeight;

    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_cancel.disabled = true;
    document.forms[0].btn_close.disabled = true;
    document.forms[0].GRADE_SEND.value = document.forms[0].GRADE.value;
    document.forms[0].HR_CLASS_SEND.value = document.forms[0].HR_CLASS.value;
    document.forms[0].GRADE.disabled = true;
    document.forms[0].HR_CLASS.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function submit_reSize() {
    bodyWidth  = (window.innerWidth  || document.body.clientWidth || 0);
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.getElementById("table1").style.width = bodyWidth - 36;
    document.getElementById("trow").style.width = bodyWidth - 477;
    document.getElementById("tbody").style.width = bodyWidth - 460;
    document.getElementById("tbody").style.height = bodyHeight - 200;
    document.getElementById("tcol").style.height = bodyHeight - 217;
}

//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

/* ----Tip Message---- */

/* initialize */
var TipTITLE; //title
var St;       //style
var goOffFlg = 0;

function goOffInfo() {
    goOffFlg = 0;
    htm();
}

function setTipText(ttl,msgs) {
    var styleType = "";
    var txt = [ttl,msgs];
    styleType = (St == 1) ? Style : Style2;
    stm(txt,styleType);
}

/* POSTによるデータ送信 */
function getInfo(schregno,mode) {
    no = schregno;
    goOffFlg = 1

    /* XMLHttpRequestオブジェクト作成 */
    if( xmlhttp == null ) {
        xmlhttp = createXmlHttp();
    } else{
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }

    /* 入力フォームデータの処理 */
    var postdata = new String();

    //取得情報変更
    if(mode == 'G') {
        TipTITLE = "減免情報";
        St = 2;
        postdata = "cmd=sendG";
    } else if(mode == 'T') {
        TipTITLE = "異動情報";
        St = 1;
        postdata = "cmd=sendT";
    } else {
        return;
    }

    //問い合わせ表示
    setTipText(TipTITLE,'<font color="red">問い合わせ中・・・</font>');
    postdata += "&SCHREGNO="+schregno;
    postdata += "&SCHOOL_KIND="+document.forms[0].hiddenSchoolKind.value;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = handleHttpEvent2;
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp727index.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);

    //timeLag防止
    if(goOffFlg == 0) htm();
}

/* レスポンスデータ処理 */
function handleHttpEvent2() {
    if( xmlhttp.readyState == 4 ) {
        if( xmlhttp.status == 200 ) {
            var txt = xmlhttp.responseText;

            if (txt == "NOTFOUND") {
                alert("{rval MSG303}\n" + TipTITLE + "マスタ");
            } else {
                setTipText(TipTITLE,txt)
            }
        }
    }

    //timeLag防止
    if(goOffFlg == 0) htm();
}

/* ---- end ---- */
var xmlhttp = null;
var no;

/* POSTによるデータ送信 */
function chgIncome(obj, slipno, secondProcess) {
    no = slipno;
    var reduc1Flg           = obj.name == 'REDUC_INCOME_1[]' ? true : false;
    var soeji               = obj.name == 'REDUC_INCOME_1[]' ? "1" : "2";
    var reduction_seq       = document.getElementById("REDUCTION_SEQ_" + soeji + "_"+slipno);     //マスタの連番
    var reduc_income        = document.forms[0]["REDUC_INCOME_" + soeji + "_"+slipno];      //所得割額
    var setReduc_income     = document.forms[0]["SET_REDUC_INCOME" + soeji + "_"+slipno];   //所得HIDDEN
    setReduc_income.value   = reduc_income.value;
    //支援金
    var baseMoney           = document.getElementById("COUNTRY_MONEY_" + soeji + "_"+slipno);
    var baseMoneyDisp       = document.getElementById("COUNTRY_MONEY_" + soeji + "2_"+slipno);
    var addMoney            = document.getElementById("COUNTRY_ADD_MONEY_" + soeji + "_"+slipno);
    var addMoneyDisp        = document.getElementById("COUNTRY_ADD_MONEY_" + soeji + "2_"+slipno);
    var countryRank         = document.getElementById("COUNTRY_RANK_" + soeji + "_"+slipno);
    //補助金
    var prefMoney           = document.getElementById("PREF_MONEY_" + soeji + "_"+slipno);
    var prefMoneyDisp       = document.getElementById("PREF_MONEY_" + soeji + "2_"+slipno);
    var prefRank            = document.getElementById("PREF_RANK_" + soeji + "_"+slipno);
    if (reduc_income.value == "") {
        baseMoney.value = '';
        baseMoneyDisp.innerText = '';
        addMoney.value = '';
        addMoneyDisp.innerText = '';
        prefMoney.value = '';
        prefMoneyDisp.innerText = '';
        countryRank.value = '';
        prefRank.value = '';
    }

    if (obj.value == '') {
        return;
    }

    /* XMLHttpRequestオブジェクト作成 */
    if( xmlhttp == null ) {
        xmlhttp = createXmlHttp();
    } else {
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }

    /* 入力フォームデータの処理 */
    var postdata = new String();
    postdata = "cmd=send";
    postdata += "&SLIP_NO="+slipno;
    postdata += "&GRADE="+document.forms[0]["GRADE"].value;
    postdata += "&REDUC_INCOME_1="+document.forms[0]["REDUC_INCOME_1_"+slipno].value; //REDUC_INCOME_1 には必ず何か数字が入ってないとダメ！！TODO
    postdata += "&REDUC_INCOME_2="+document.forms[0]["REDUC_INCOME_2_"+slipno].value;
    postdata += "&G_PREF_CD="+document.forms[0]["G_PREF_CD_"+slipno].value;
    postdata += "&OBJ_NAME="+obj.name;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent(obj, slipno, secondProcess)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp727index.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);
}

//数値カンマ
function number_format(str) {
    if(!str)return;
    var yen="";
    for(i=0;i<str.length;i++) {
        if(i && !((str.length-i) % 3))yen+=",";
        yen+=str.charAt(i);
    }
    return yen;
}

/* POSTによるデータ送信 */
function clickKakutei(obj, slipno) {

    //支援金1
    var baseMoney1      = document.getElementById("COUNTRY_MONEY_1_" + slipno);
    var baseMoney1Disp  = document.getElementById("COUNTRY_MONEY_12_" + slipno);
    var addMoney1       = document.getElementById("COUNTRY_ADD_MONEY_1_" + slipno);
    var addMoney1Disp   = document.getElementById("COUNTRY_ADD_MONEY_12_" + slipno);
    var countryRank1    = document.getElementById("COUNTRY_RANK_1_" + slipno);

    //支援金2
    var baseMoney2      = document.getElementById("COUNTRY_MONEY_2_" + slipno);
    var baseMoney2Disp  = document.getElementById("COUNTRY_MONEY_22_" + slipno);
    var addMoney2       = document.getElementById("COUNTRY_ADD_MONEY_2_" + slipno);
    var addMoney2Disp   = document.getElementById("COUNTRY_ADD_MONEY_22_" + slipno);
    var countryRank2    = document.getElementById("COUNTRY_RANK_2_" + slipno);

    //支援金合計
    var totalCountry    = document.getElementById('countyr_total_money_'+slipno);

    //調整金
    var adjustMent      = document.getElementById("ADJUSTMENT_MONEY_" + slipno);

    if (!obj.checked) {
        baseMoney1.value = '';
        baseMoney1Disp.innerText = '';
        addMoney1.value = '';
        addMoney1Disp.innerText = '';
        countryRank1.value = '';

        baseMoney2.value = '';
        baseMoney2Disp.innerText = '';
        addMoney2.value = '';
        addMoney2Disp.innerText = '';
        countryRank2.value = '';

        adjustMent.innerText = '0';
        totalCountry.innerText = '0';

        return;
    }

    /* XMLHttpRequestオブジェクト作成 */
    if (xmlhttp == null) {
        xmlhttp = createXmlHttp();
    } else {
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }

    /* 入力フォームデータの処理 */
    var postdata = new String();
    postdata = "cmd=send";
    postdata += "&SLIP_NO="+slipno;
    postdata += "&GRADE="+document.forms[0]["GRADE"].value;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent(obj, slipno)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp727index.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);
}

/* レスポンスデータ処理 */
function handleHttpEvent(obj, slipno) {
    if (xmlhttp.readyState == 4 ) {
        if (xmlhttp.status == 200 ) {
            var reduction_seq1      = document.getElementById("REDUCTION_SEQ_1_" + slipno);     //マスタの連番1
            var reduction_seq2      = document.getElementById("REDUCTION_SEQ_2_" + slipno);     //マスタの連番2
            var reduc_rare_case_cd  = document.getElementById("COUNTRY_RARE_CASE_CD_" + slipno);
            //支援金1
            var countryRank1        = document.getElementById("COUNTRY_RANK_1_" + slipno);
            var baseMoney1          = document.getElementById("COUNTRY_MONEY_1_" + slipno);
            var baseMoneyDisp1      = document.getElementById("COUNTRY_MONEY_12_" + slipno);
            var addMoney1           = document.getElementById("COUNTRY_ADD_MONEY_1_" + slipno);
            var addMoneyDisp1       = document.getElementById("COUNTRY_ADD_MONEY_12_" + slipno);

            //支援金2
            var countryRank2        = document.getElementById("COUNTRY_RANK_2_" + slipno);
            var baseMoney2          = document.getElementById("COUNTRY_MONEY_2_" + slipno);
            var baseMoneyDisp2      = document.getElementById("COUNTRY_MONEY_22_" + slipno);
            var addMoney2           = document.getElementById("COUNTRY_ADD_MONEY_2_" + slipno);
            var addMoneyDisp2       = document.getElementById("COUNTRY_ADD_MONEY_22_" + slipno);

            //授業料
            var jugyouryou          = document.getElementById("JUGYOURYOU_" + slipno);

            var case_cd_flg_array   = reduc_rare_case_cd.value.split(":"); //例 SH:1:軽減額入力有り
            var case_cd_flg         = case_cd_flg_array[1];
            var json                = xmlhttp.responseText;
            var response;

            eval('response = ' + json); //JSON形式のデータ(オブジェクトとして扱える)

            if (response.result) {
                if (case_cd_flg != "1") {
                    var setCRank1   = response.COUNTRY_RANK1;
                    var setCMoney1  = response.COUNTRY_MONEY1;
                    var setCAMoney1 = response.COUNTRY_ADD_MONEY1;

                    var setCRank2   = response.COUNTRY_RANK2;
                    var setCMoney2  = response.COUNTRY_MONEY2;
                    var setCAMoney2 = response.COUNTRY_ADD_MONEY2;

                    var setJugyouryou   = response.JUGYOURYOU;

                    //調整金
                    if (response.ADJUSTMENT_MONEY != undefined) {
                        document.getElementById("ADJUSTMENT_MONEY_" + slipno).innerText = response.ADJUSTMENT_MONEY;
                    }

                    if (response.COUNTRY_MONEY1 == undefined) {
                        setCMoney1 = 0;
                    }
                    if (response.COUNTRY_ADD_MONEY1 == undefined) {
                        setCAMoney1 = 0;
                    }
                    if (response.COUNTRY_MONEY2 == undefined) {
                        setCMoney2 = 0;
                    }
                    if (response.COUNTRY_ADD_MONEY2 == undefined) {
                        setCAMoney2 = 0;
                    }
                    //基準額1
                    countryRank1.value = setCRank1;
                    baseMoney1.value = setCMoney1;
                    baseMoneyDisp1.innerText = number_format2(setCMoney1);
                    //加算額1
                    addMoney1.value   = setCAMoney1;
                    addMoneyDisp1.innerText = number_format2(setCAMoney1);

                    //基準額2
                    countryRank2.value = setCRank2;
                    baseMoney2.value = setCMoney2;
                    baseMoneyDisp2.innerText = number_format2(setCMoney2);
                    //加算額2
                    addMoney2.value   = setCAMoney2;
                    addMoneyDisp2.innerText = number_format2(setCAMoney2);

                    //授業料
                    jugyouryou.innerText    = number_format2(setJugyouryou);
                    jugyouryou.value        = setJugyouryou;
                }
                reduction_seq1.value    = response.REDUCTION_SEQ;
                reduction_seq2.value    = response.REDUCTION_SEQ;
            } else {
                alert("{rval MSG303}\n授業料軽減マスタ");
                reduction_seq1.value    = "";
                reduction_seq2.value    = "";
                addMoney1.value         = "";
                addMoney1.innerText     = "";
                addMoney2.value         = "";
                addMoney2.innerText     = "";
            }
            setTotalmony(slipno);
        } else {
            window.alert("通信エラーが発生しました。");
        }
    }
}

function number_format2(num) {
    return num.toString().replace(/([0-9]+?)(?=(?:[0-9]{3})+$)/g , '$1,');
}

function setTotalmony(slipno) {

    baseMoney          = document.getElementById('COUNTRY_MONEY_1_' + slipno);
    baseMoney2         = document.getElementById('COUNTRY_MONEY_2_' + slipno);
    thisAddFlg         = document.getElementById('COUNTRY_ADD_FLG_1_' + slipno);
    thisReducAddMoney  = document.getElementById('COUNTRY_ADD_MONEY_1_' + slipno);
    otherAddFlg        = document.getElementById('COUNTRY_ADD_FLG_2_' + slipno);
    otherReducAddMoney = document.getElementById('COUNTRY_ADD_MONEY_2_' + slipno);

    var countryRareCase  = document.getElementById("COUNTRY_RARE_CASE_CD_" + slipno);
    var countryCasearray = countryRareCase.value.split(":"); //例 SH:1:軽減額入力有り
    var countryCaseflg   = countryCasearray[1];

    var jugyouryou      = document.getElementById("JUGYOURYOU_"+slipno);

    var countryTotal = 0;
    if (countryCaseflg == "1") {
        countryTotal = countryTotal + parseInt(baseMoney.value ? (baseMoney.value * 1) : 0);
        countryTotal = countryTotal + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 1) : 0);
        countryTotal = countryTotal + parseInt(baseMoney2.value ? (baseMoney2.value * 1) : 0);
        countryTotal = countryTotal + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 1) : 0);
    }
    if (countryCaseflg != "1") {
        countryTotal = countryTotal + parseInt(baseMoney.value ? (baseMoney.value * 3) : 0);
        countryTotal = countryTotal + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 3) : 0);
        countryTotal = countryTotal + parseInt(baseMoney2.value ? (baseMoney2.value * 9) : 0);
        countryTotal = countryTotal + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 9) : 0);
    }
    document.getElementById('countyr_total_money_'+slipno).innerText = number_format(String(countryTotal));

    //算出範囲（前期・後期）
    var numerator = 0;
    numerator = numerator + 1;
    numerator = numerator + 4;

    //減免先
    var scoolDivFirstSaki1  = document.forms[0]["SCOOL_DIV_FIRST_SAKI_1_"+slipno];
    var scoolDivLastSaki1   = document.forms[0]["SCOOL_DIV_LAST_SAKI_1_"+slipno];

    //減免後（MAX値）
    var scoolDivFirstAto1   = document.forms[0]["SCOOL_DIV_FIRST_ATO_1_"+slipno];
    var scoolDivLastAto1    = document.forms[0]["SCOOL_DIV_LAST_ATO_1_"+slipno];

    //金額区分
    var moneyDiv1  = document.forms[0]["MONEY_DIV1_"+slipno];
    var moneyDiv2  = document.forms[0]["MONEY_DIV2_"+slipno];

    //基礎計算
    var baseCalc = 0;
    baseCalc = baseCalc + ((numerator > 0 && parseInt(jugyouryou.value) > 0) ? (parseInt(jugyouryou.value) / 4 * numerator) : 0);
    baseCalc = baseCalc - countryTotal;

    //減免後の換算
    var scoolDivFirst1 = 0;
    var scoolDivLast1 = 0;
    var hasuu = 0;

    var denominator = 0;
    denominator = denominator + parseInt(scoolDivFirstAto1.value ? (scoolDivFirstAto1.value * 1) : 0);
    denominator = denominator + parseInt(scoolDivLastAto1.value ? (scoolDivLastAto1.value * 1) : 0);

    if (baseCalc > 0 && denominator > 0) {
        //減免後
        scoolDivFirst1  = parseInt(baseCalc / denominator * parseInt(scoolDivFirstAto1.value ? (scoolDivFirstAto1.value * 1) : 0));
        scoolDivLast1   = parseInt(baseCalc / denominator * parseInt(scoolDivLastAto1.value ? (scoolDivLastAto1.value * 1) : 0));

        //端数
        hasuu = baseCalc - (scoolDivFirst1 + scoolDivLast1);

        if (scoolDivFirst1 > 0) {
            scoolDivFirst1 = scoolDivFirst1 + hasuu;
        } else if (scoolDivLast1 > 0) {
            scoolDivLast1 = scoolDivLast1 + hasuu;
        }
    }

    //減免後のMAX値
    maxScoolDivFirstAto1 = parseInt(scoolDivFirstAto1.value ? (scoolDivFirstAto1.value * 1) : 0);
    maxScoolDivLastAto1 = parseInt(scoolDivLastAto1.value ? (scoolDivLastAto1.value * 1) : 0);

    //減免後のMAX値との比較
    scoolDivFirst1  = (scoolDivFirst1 > maxScoolDivFirstAto1) ? maxScoolDivFirstAto1 : scoolDivFirst1;
    scoolDivLast1   = (scoolDivLast1 > maxScoolDivLastAto1) ? maxScoolDivLastAto1 : scoolDivLast1;

    //減免先の値を加算
    scoolDivFirst1  = scoolDivFirst1 + parseInt(scoolDivFirstSaki1.value ? (scoolDivFirstSaki1.value * 1) : 0);
    scoolDivLast1   = scoolDivLast1 + parseInt(scoolDivLastSaki1.value ? (scoolDivLastSaki1.value * 1) : 0);

    //表示
    document.getElementById('SCOOL_DIV_FIRST_1_'+slipno).innerText = number_format(String(scoolDivFirst1));
    document.getElementById('SCOOL_DIV_LAST_1_'+slipno).innerText = number_format(String(scoolDivLast1));

    //値セット
    document.forms[0]["SCOOL_DIV_FIRST_1_"+slipno].value = scoolDivFirst1;
    document.forms[0]["SCOOL_DIV_LAST_1_"+slipno].value = scoolDivLast1;
}

/* XMLHttpRequest生成 */
function createXmlHttp() {
    if( document.all ) {
        return new ActiveXObject("Microsoft.XMLHTTP");
    } else if( document.implementation ) {
        return new XMLHttpRequest();
    } else{
        return null;
    }
}

//軽減額チェックボックス
function chkFlg(obj, slipno) {

    baseMoney          = document.getElementById('COUNTRY_MONEY_1_' + slipno);
    baseMoney2         = document.getElementById('COUNTRY_MONEY_2_' + slipno);
    thisAddFlg         = document.getElementById('COUNTRY_ADD_FLG_1_' + slipno);
    thisReducAddMoney  = document.getElementById('COUNTRY_ADD_MONEY_1_' + slipno);
    otherAddFlg        = document.getElementById('COUNTRY_ADD_FLG_2_' + slipno);
    otherReducAddMoney = document.getElementById('COUNTRY_ADD_MONEY_2_' + slipno);

    var countryRareCase     = document.getElementById("COUNTRY_RARE_CASE_CD_" + slipno);
    var countryCasearray    = countryRareCase.value.split(":"); //例 SH:1:軽減額入力有り
    var countryCaseflg      = countryCasearray[1];

    var countryTotal = 0;
    if (countryCaseflg == "1") {
        countryTotal = countryTotal + parseInt(baseMoney.value ? (baseMoney.value * 1) : 0);
        countryTotal = countryTotal + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 1) : 0);
        countryTotal = countryTotal + parseInt(baseMoney2.value ? (baseMoney2.value * 1) : 0);
        countryTotal = countryTotal + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 1) : 0);
    }
    if (countryCaseflg != "1") {
        countryTotal = countryTotal + parseInt(baseMoney.value ? (baseMoney.value * 3) : 0);
        countryTotal = countryTotal + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 3) : 0);
        countryTotal = countryTotal + parseInt(baseMoney2.value ? (baseMoney2.value * 9) : 0);
        countryTotal = countryTotal + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 9) : 0);
    }
    document.getElementById('countyr_total_money_'+slipno).innerText = number_format(String(countryTotal));
}

function focusText_2(obj,slipno) {
    if (document.getElementById("REDUC_DEC_FLG_2_"+slipno).checked == true) {
        document.getElementById(obj.id).style.color      = "#999999";
        obj.blur();
    }
}

function chkByte(obj) {}

function checkAll(obj) {
    var reduc_dec_flg      = '';
    if (obj.name == 'COUNTRY_BASE_ALL_1') {
        reduc_dec_flg      = 'COUNTRY_BASE_FLG_1';
    } else if (obj.name == 'COUNTRY_BASE_ALL_2') {
        reduc_dec_flg      = 'COUNTRY_BASE_FLG_2';
    } else if (obj.name == 'COUNTRY_ADD_ALL_1') {
        reduc_dec_flg      = 'COUNTRY_ADD_FLG_1';
    } else if (obj.name == 'COUNTRY_ADD_ALL_2') {
        reduc_dec_flg      = 'COUNTRY_ADD_FLG_2';
    } else if (obj.name == 'REDUC_SCHOOL_ALL1') {
        reduc_dec_flg      = 'REDUC_SCHOOL_FLG_1';
    } else if (obj.name == 'REDUC_SCHOOL_ALL2') {
        reduc_dec_flg      = 'REDUC_SCHOOL_FLG_2';
    }

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == reduc_dec_flg+"[]") {
            var id = document.forms[0].elements[i].id;
            if (!document.forms[0].elements[i].disabled) {
                document.forms[0].elements[i].checked = obj.checked;
            }
            slipno = id.replace(reduc_dec_flg + '_', '');
            chkFlg(document.forms[0].elements[i], slipno);
        }
    }
}

function kick_chgIncome(obj, slipno, div, soeji) {
    var reduc_rare_case_cd = document.getElementById(div + "RARE_CASE_CD_" + slipno);
    var case_cd_flg_array  = reduc_rare_case_cd.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg        = case_cd_flg_array[1];

    var isCountry   = div == "COUNTRY_";
    baseMoney1      = document.getElementById(div + "MONEY_1_" + slipno);
    var baseDisp_12 = document.getElementById(div + "MONEY_12_" + slipno);
    baseMoney2      = document.getElementById(div + "MONEY_2_" + slipno);
    var baseDisp_22 = document.getElementById(div + "MONEY_22_" + slipno);
    if (isCountry) {
        addMoney1       = document.getElementById(div + "ADD_MONEY_1_" + slipno);
        var addDisp_12  = document.getElementById(div + "ADD_MONEY_12_" + slipno);
        addMoney2       = document.getElementById(div + "ADD_MONEY_2_" + slipno);
        var addDisp_22  = document.getElementById(div + "ADD_MONEY_22_" + slipno);
    }
    if (case_cd_flg == '1') {
        baseDisp_12.innerText = "";    //プレーンテキスト
        baseMoney1.style.display = ""; //テキストボックス
        baseDisp_22.innerText = "";    //プレーンテキスト
        baseMoney2.style.display = ""; //テキストボックス

        if (isCountry) {
            addDisp_12.innerText = "";    //プレーンテキスト
            addMoney1.style.display = ""; //テキストボックス
            addDisp_22.innerText = "";    //プレーンテキスト
            addMoney2.style.display = ""; //テキストボックス
        }
    } else {
        //軽減額入力なし
        baseDisp_12.innerText = baseMoney1.value ? number_format(baseMoney1.value) : "";    //プレーンテキスト
        baseMoney1.style.display = "none"; //テキストボックス

        baseDisp_22.innerText = baseMoney2.value ? number_format(baseMoney2.value) : "";    //プレーンテキスト
        baseMoney2.style.display = "none"; //テキストボックス
        if (isCountry) {
            addDisp_12.innerText = addMoney1.value ? number_format(addMoney1.value) : "";    //プレーンテキスト
            addMoney1.style.display = "none"; //テキストボックス
            addDisp_22.innerText = addMoney2.value ? number_format(addMoney2.value) : "";    //プレーンテキスト
            addMoney2.style.display = "none"; //テキストボックス
        }
    }
    setTotalmony(slipno);
    return;
}

//画面の切替
function Page_jumper(link, prgId) {

    var prgIdLow = prgId.toLowerCase();
    link = link + "/P/" + prgId + "/" + prgIdLow + "index.php?SENDPRGID=KNJP727";
    link = link + "&S_GRADE=" + document.forms[0].GRADE.value;
    link = link + "&S_HR_CLASS=" + document.forms[0].HR_CLASS.value;

    parent.location.href=link;
}

function stringSum(array) {
    var i;
    var sum = 0;
    var nums = array.filter(function (e) { return /[0-9]+/.test(e); });
    if (nums.length == 0) {
        return "";
    }
    for (i = 0; i < nums.length; i++) {
        sum += parseInt(nums[i]);
    }
    return sum;

}

//所得割額へ合計を反映
function setSumIncome(div, slipno) {
    var income1 = document.getElementById("REDUC_INCOME_" +div + "_1_" + slipno).value;
    var income2 = document.getElementById("REDUC_INCOME_" +div + "_2_" + slipno).value;
    var income3 = document.getElementById("REDUC_INCOME_" +div + "_3_" + slipno).value;
    var income4 = document.getElementById("REDUC_INCOME_" +div + "_4_" + slipno).value;

    var targetTxt = document.getElementById("REDUC_INCOME_" +div + "_" + slipno);

    targetTxt.value = stringSum([income1, income2, income3, income4]);
    targetTxt.onchange();
}
