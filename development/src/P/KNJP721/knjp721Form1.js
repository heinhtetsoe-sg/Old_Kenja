
function btn_submit(cmd) {
    bodyWidth  = (window.innerWidth  || document.body.clientWidth || 0);
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.forms[0].windowWidth.value = bodyWidth;
    document.forms[0].windowHeight.value = bodyHeight;

    //取消
    if (cmd == 'cancel') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //再計算
    if (cmd == 'calc') {
        //選択チェック
        var cnt = 0;
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == 'CALC_FLG[]') {
                if (document.forms[0].elements[i].checked) {
                    cnt++
                }
            }
        }
        if (cnt == 0) { 
            alert('{rval MSG304}');
            return;
        }
        if (!confirm('{rval MSG108}')) {
            return;
        }
    }

    //更新
    if (cmd == 'update') {
        //確定
        var kakuteiArr = ["COUNTRY_BASE_FLG_1", "COUNTRY_BASE_FLG_2", "COUNTRY_ADD_FLG_1", "COUNTRY_ADD_FLG_2", "PREF_FLG_1", "PREF_FLG_2"];

        for (var i=0; i < kakuteiArr.length; i++) {
            var org = document.forms[0]["TMP_"+kakuteiArr[i]].value.split(',');

            var arr = [];
            var cnt = 0;
            for (var j=0; j < document.forms[0].elements.length; j++) {
                var el = document.forms[0].elements[j];
                if (el.name == kakuteiArr[i]+'[]' && el.checked == true) {
                    var newcdFlg = true;
                    for (var k=0; k < org.length; k++) {
                        if (org[k] == el.value) {
                            newcdFlg = false;
                        }
                    }
                    if (newcdFlg == true) {
                        arr[cnt] = el.value;
                        cnt++
                    }
                }
            }

            var kakuteiMonth = "";
            var kakuteiMonthLabel = "";
            if (kakuteiArr[i] == "COUNTRY_BASE_FLG_1" || kakuteiArr[i] == "COUNTRY_ADD_FLG_1") {
                kakuteiMonth        = "COUNTRY_DEC_MONTH1";
                kakuteiMonthLabel   = "前期支援金";
            } else if (kakuteiArr[i] == "COUNTRY_BASE_FLG_2" || kakuteiArr[i] == "COUNTRY_ADD_FLG_2") {
                kakuteiMonth        = "COUNTRY_DEC_MONTH2";
                kakuteiMonthLabel   = "後期支援金";

            } else if (kakuteiArr[i] == "PREF_FLG_1") {
                kakuteiMonth        = "PREF_DEC_MONTH1";
                kakuteiMonthLabel   = "補助額1";
            } else if (kakuteiArr[i] == "PREF_FLG_2") {
                kakuteiMonth        = "PREF_DEC_MONTH2";
                kakuteiMonthLabel   = "補助額2";
            }

            //必須選択チェック（新規で確定したとき）
            if (arr.length > 0 && document.forms[0][kakuteiMonth].value == "" && kakuteiMonth != "") {
                alert("{rval MSG304}\n( "+kakuteiMonthLabel+"　確定月 )");
                return;
            }
        }
    }

    if (cmd != 'csv') {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_cancel.disabled = true;
        document.forms[0].btn_close.disabled = true;
        document.forms[0].btn_calc.disabled = true;
        document.forms[0].btn_csv.disabled = true;
        document.forms[0].GRADE_SEND.value = document.forms[0].GRADE.value;
        document.forms[0].HR_CLASS_SEND.value = document.forms[0].HR_CLASS.value;
        document.forms[0].GRADE.disabled = true;
        document.forms[0].HR_CLASS.disabled = true;
    }

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
    xmlhttp.open("POST", "knjp721index.php" , true);
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
function chgIncome(obj, slipno, soeji) {
    no = slipno;
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

    if (obj.name == 'REDUC_INCOME_'+soeji+'[]' && reduc_income.value == "") {
        baseMoney.value         = '';
        addMoney.value          = '';
        baseMoneyDisp.innerText = '';
        addMoneyDisp.innerText  = '';

        prefMoney.value         = '';
        prefMoneyDisp.innerText = '';
        countryRank.value       = '';
        prefRank.value          = '';
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
    postdata += "&COUNTRY_RANK_1="+document.forms[0]["COUNTRY_RANK_1_"+slipno].value;
    postdata += "&COUNTRY_RANK_2="+document.forms[0]["COUNTRY_RANK_2_"+slipno].value;
    postdata += "&COUNTRY_MONEY_1="+document.forms[0]["COUNTRY_MONEY_1_"+slipno].value;
    postdata += "&COUNTRY_ADD_MONEY_1="+document.forms[0]["COUNTRY_ADD_MONEY_1_"+slipno].value;
    postdata += "&COUNTRY_MONEY_2="+document.forms[0]["COUNTRY_MONEY_2_"+slipno].value;
    postdata += "&COUNTRY_ADD_MONEY_2="+document.forms[0]["COUNTRY_ADD_MONEY_2_"+slipno].value;
    postdata += "&PREF_RANK_1="+document.forms[0]["PREF_RANK_1_"+slipno].value;
    postdata += "&PREF_RANK_2="+document.forms[0]["PREF_RANK_2_"+slipno].value;
    postdata += "&PREF_MONEY_1="+document.forms[0]["PREF_MONEY_1_"+slipno].value;
    postdata += "&PREF_MONEY_2="+document.forms[0]["PREF_MONEY_2_"+slipno].value;
    postdata += "&BURDEN_CHARGE_1="+document.forms[0]["BURDEN_CHARGE1_"+slipno].value;
    postdata += "&BURDEN_CHARGE_2="+document.forms[0]["BURDEN_CHARGE2_"+slipno].value;
    postdata += "&COUNTRY_ZUMI="+document.forms[0]["COUNTRY_ZUMI_"+slipno].value;
    postdata += "&PREF_ZUMI="+document.forms[0]["PREF_ZUMI_"+slipno].value;
    postdata += "&OBJ_NAME="+obj.name;
    postdata += "&SOEJI="+soeji;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent(obj, slipno, soeji)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp721index.php" , true);
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

/* レスポンスデータ処理 */
function handleHttpEvent(obj, slipno, soeji) {
    if (xmlhttp.readyState == 4 ) {
        if (xmlhttp.status == 200 ) {
            var reduction_seq           = document.getElementById("REDUCTION_SEQ_" + soeji + "_"+slipno);     //マスタの連番
            var reduc_income            = document.getElementById("REDUC_INCOME_" + soeji + "_"+slipno);
            //支援金
            var countryRank             = document.getElementById("COUNTRY_RANK_" + soeji + "_"+slipno);
            var baseMoney               = document.getElementById("COUNTRY_MONEY_" + soeji + "_"+slipno);
            var baseMoneyDisp           = document.getElementById("COUNTRY_MONEY_" + soeji + "2_"+slipno);
            var addMoney                = document.getElementById("COUNTRY_ADD_MONEY_" + soeji + "_"+slipno);
            var addMoneyDisp            = document.getElementById("COUNTRY_ADD_MONEY_" + soeji + "2_"+slipno);
            //補助金
            var prefRank                = document.getElementById("PREF_RANK_" + soeji + "_"+slipno);
            var prefMoney               = document.getElementById("PREF_MONEY_" + soeji + "_"+slipno);
            var prefMoneyDisp           = document.getElementById("PREF_MONEY_" + soeji + "2_"+slipno);
            //授業料
            var jugyouryou              = document.getElementById("JUGYOURYOU_"+slipno);
            var jugyouryoutou           = document.getElementById("JUGYOURYOUTOU_"+slipno);

            var json                    = xmlhttp.responseText;
            var response;

            eval('response = ' + json); //JSON形式のデータ(オブジェクトとして扱える)

            if (response.result) {
                var setCRank            = response.COUNTRY_RANK;
                var setCMoney           = response.COUNTRY_MONEY;
                var setCAMoney          = response.COUNTRY_ADD_MONEY;
                var setPRank            = response.PREF_RANK;
                var setPrefMoney        = response.PREF_MONEY;
                var setJugyouryou       = response.JUGYOURYOU;
                var setJugyouryoutou    = response.JUGYOURYOUTOU;

                //金額区分
                if (response.MONEY_DIV1 != undefined) {
                    document.forms[0]["MONEY_DIV1_"+slipno].value = response.MONEY_DIV1;
                }
                if (response.MONEY_DIV2 != undefined) {
                    document.forms[0]["MONEY_DIV2_"+slipno].value = response.MONEY_DIV2;
                }
                //補助額
                if (response.REDUCTIONMONEY1 != undefined) {
                    document.forms[0]["REDUCTIONMONEY1_"+slipno].value = response.REDUCTIONMONEY1;
                }
                if (response.REDUCTIONMONEY2 != undefined) {
                    document.forms[0]["REDUCTIONMONEY2_"+slipno].value = response.REDUCTIONMONEY2;
                }

                if (response.COUNTRY_RANK == undefined) {
                    setCRank = "";
                }
                if (response.COUNTRY_MONEY == undefined) {
                    setCMoney = "";
                }
                if (response.COUNTRY_ADD_MONEY == undefined) {
                    setCAMoney = "";
                }

                //支援金（基準額）
                countryRank.value = setCRank;
                baseMoney.value = setCMoney;
                baseMoneyDisp.innerText = number_format2(setCMoney);
                //支援金（加算額）
                addMoney.value   = setCAMoney;
                addMoneyDisp.innerText = number_format2(setCAMoney);

                if (response.PREF_RANK == undefined) {
                    setPRank = "";
                }
                if (response.PREF_MONEY == undefined) {
                    setPrefMoney = "";
                }
                //補助額
                prefRank.value = setPRank;
                prefMoney.value = setPRank == '' ? '' : setPrefMoney;
                prefMoneyDisp.innerText = setPRank == '' ?'' : number_format2(setPrefMoney);

                //授業料
                jugyouryou.innerText    = number_format2(setJugyouryou);
                jugyouryou.value        = setJugyouryou;
                jugyouryoutou.innerText = number_format2(setJugyouryoutou);
                jugyouryoutou.value     = setJugyouryoutou;

                //負担金
                if (response.BURDEN_CHARGE1 != undefined) {
                    document.forms[0]["BURDEN_CHARGE1_"+slipno].value = parseInt(response.BURDEN_CHARGE1);
                }
                if (response.BURDEN_CHARGE2 != undefined) {
                    document.forms[0]["BURDEN_CHARGE2_"+slipno].value = parseInt(response.BURDEN_CHARGE2);
                }

                //減免先
                if (response.REDUCTION_JUGYOURYOU_SAKI != undefined) {
                    document.forms[0]["REDUCTION_JUGYOURYOU_SAKI_"+slipno].value = response.REDUCTION_JUGYOURYOU_SAKI;
                }
                //減免後
                if (response.REDUCTION_JUGYOURYOU_ATO1 != undefined) {
                    document.forms[0]["REDUCTION_JUGYOURYOU_ATO1_"+slipno].value = response.REDUCTION_JUGYOURYOU_ATO1;
                }
                if (response.REDUCTION_JUGYOURYOU_ATO2 != undefined) {
                    document.forms[0]["REDUCTION_JUGYOURYOU_ATO2_"+slipno].value = response.REDUCTION_JUGYOURYOU_ATO2;
                }

                reduction_seq.value    = response.REDUCTION_SEQ;
            } else {
                alert("{rval MSG303}\n授業料軽減マスタ");
                reduction_seq.value     = "";
                addMoney.value          = "";
                reduc_income.value      = "";
                addMoney.innerText      = "";
            }

            setTotalmony(slipno);

            var rydFlg = document.forms[0]["REFER_YEAR_DIV_FLG"].value;
            var setDiv = (soeji == '1') ? '2' : '1';
            var refYearDiv = document.forms[0]["REFER_YEAR_DIV" + setDiv + "_" + slipno].value;
            if (refYearDiv == soeji && rydFlg == true && obj.name == 'REDUC_INCOME_'+soeji+'[]') {
                document.forms[0]["REFER_YEAR_DIV_FLG"].value = false;
                var targetTxt = document.getElementById("REDUC_INCOME_" + setDiv + "_" + slipno);
                targetTxt.onchange();
            }
        } else {
            window.alert("通信エラーが発生しました。");
        }
    }
}
function number_format2(num) {
    return num.toString().replace(/([0-9]+?)(?=(?:[0-9]{3})+$)/g , '$1,');
}

function setTotalmony(slipno) {

    baseMoney1         = document.getElementById('COUNTRY_MONEY_1_' + slipno);
    baseMoney2         = document.getElementById('COUNTRY_MONEY_2_' + slipno);
    thisAddFlg         = document.getElementById('COUNTRY_ADD_FLG_1_' + slipno);
    thisReducAddMoney  = document.getElementById('COUNTRY_ADD_MONEY_1_' + slipno);
    otherAddFlg        = document.getElementById('COUNTRY_ADD_FLG_2_' + slipno);
    otherReducAddMoney = document.getElementById('COUNTRY_ADD_MONEY_2_' + slipno);
    prefMoney1         = document.getElementById("PREF_MONEY_1_" + slipno);
    prefMoney2         = document.getElementById("PREF_MONEY_2_" + slipno);

    var jugyouryou      = document.getElementById("JUGYOURYOU_"+slipno);
    var jugyouryoutou   = document.getElementById("JUGYOURYOUTOU_"+slipno);

    jugyouryou.value = parseInt(jugyouryou.value);
    jugyouryoutou.value = parseInt(jugyouryoutou.value);

    var countryTotal1 = 0;
    var countryTotal2 = 0;
    countryTotal1 += parseInt(!isNaN(baseMoney1.value) && baseMoney1.value ? (baseMoney1.value * 3) : 0);
    countryTotal1 += parseInt(!isNaN(thisReducAddMoney.value) && thisReducAddMoney.value ? (thisReducAddMoney.value * 3) : 0);
    countryTotal2 += parseInt(!isNaN(baseMoney2.value) && baseMoney2.value ? (baseMoney2.value * 9) : 0);
    countryTotal2 += parseInt(!isNaN(otherReducAddMoney.value) && otherReducAddMoney.value ? (otherReducAddMoney.value * 9) : 0);

    //支援金用授業料
    var maxCountryJugyouryou1 = document.forms[0]["MAX_COUNTRY_JUGYOURYOU1_"+slipno].value;
    var maxCountryJugyouryou2 = document.forms[0]["MAX_COUNTRY_JUGYOURYOU2_"+slipno].value;

    countryTotal1 = Math.min(countryTotal1, maxCountryJugyouryou1);
    countryTotal2 = Math.min(countryTotal2, maxCountryJugyouryou2);

    var countryTotal = countryTotal1 + countryTotal2;
    document.getElementById('countyr_total_money_'+slipno).innerText = number_format(String(countryTotal));

    var prefTotal = 0;
    prefTotal = prefTotal + parseInt(prefMoney1.value ? (prefMoney1.value * 1) : 0);
    prefTotal = prefTotal + parseInt(prefMoney2.value ? (prefMoney2.value * 1) : 0);
    document.getElementById('pref_total_money_'+slipno).innerText = number_format(String(prefTotal));

    //所得割額
    var reduc_income1   = document.getElementById("REDUC_INCOME_1_"+slipno);
    var reduc_income2   = document.getElementById("REDUC_INCOME_2_"+slipno);

    //減免先
    var reducJugyouryouSaki = document.forms[0]["REDUCTION_JUGYOURYOU_SAKI_"+slipno];

    //減免後（MAX値）
    var reducJugyouryouAto1 = document.forms[0]["REDUCTION_JUGYOURYOU_ATO1_"+slipno];
    var reducJugyouryouAto2 = document.forms[0]["REDUCTION_JUGYOURYOU_ATO2_"+slipno];

    //金額区分
    var moneyDiv1  = document.forms[0]["MONEY_DIV1_"+slipno];
    var moneyDiv2  = document.forms[0]["MONEY_DIV2_"+slipno];

    //補助額
    var redMoney1  = document.forms[0]["REDUCTIONMONEY1_"+slipno];
    var redMoney2  = document.forms[0]["REDUCTIONMONEY2_"+slipno];

    var numerator = 0;
    var soejiFlg1 =  false;
    if (reduc_income1.value != '' || baseMoney1.value != '' || thisReducAddMoney.value != '' || prefMoney1.value != '') {
        numerator = numerator + 1;
        soejiFlg1 =  true;
    }
    var soejiFlg2 =  false;
    if (reduc_income2.value != '' || baseMoney2.value != '' || otherReducAddMoney.value != '' || prefMoney2.value != '') {
        numerator = numerator + 3;
        soejiFlg2 =  true;
    }

    var countryZumiArray = document.forms[0]["COUNTRY_ZUMI_"+slipno].value.split(',');
    var getArray1 = countryZumiArray.filter( function( value ) {
        return value == 'REDUC_SCHOOL_FLG_1' ;
    })
    var getArray2 = countryZumiArray.filter( function( value ) {
        return value == 'REDUC_SCHOOL_FLG_2' ;
    })

    //減免（前期）
    if (getArray1.length == 0) {
        var reducJugyouryou1 = 0;
        reducJugyouryou1 += parseInt(reducJugyouryouSaki.value ? reducJugyouryouSaki.value / 4 : 0);
        reducJugyouryou1 += parseInt(reducJugyouryouAto1.value ? reducJugyouryouAto1.value : 0);
        document.getElementById('REDUCTION_JUGYOURYOU1_'+slipno).innerText = number_format(String(reducJugyouryou1));
        document.forms[0]["REDUCTION_JUGYOURYOU1_"+slipno].value = reducJugyouryou1;
    }
    //減免（後期）
    if (getArray2.length == 0) {
        var reducJugyouryou2 = 0;
        reducJugyouryou2 += parseInt(reducJugyouryouSaki.value ? reducJugyouryouSaki.value / 4 * 3 : 0);
        reducJugyouryou2 += parseInt(reducJugyouryouAto2.value ? reducJugyouryouAto2.value : 0);
        document.getElementById('REDUCTION_JUGYOURYOU2_'+slipno).innerText = number_format(String(reducJugyouryou2));
        document.forms[0]["REDUCTION_JUGYOURYOU2_"+slipno].value = reducJugyouryou2;
    }

    var prefRank1   = document.getElementById("PREF_RANK_1_"+slipno);
    var prefRank2   = document.getElementById("PREF_RANK_2_"+slipno);

    //補助金チェック
    var burdenChargeFlg = false;
    if  (prefRank1.value != '' || prefRank2.value != '') {
        burdenChargeFlg = true;
    }

    //負担金算出
    var burdenCharge1 = 0;
    var burdenCharge2 = 0;
    if (burdenChargeFlg == true) {
        burdenCharge1 = parseInt(document.forms[0]["BURDEN_CHARGE1_"+slipno].value ? document.forms[0]["BURDEN_CHARGE1_"+slipno].value : 0);
        burdenCharge2 = parseInt(document.forms[0]["BURDEN_CHARGE2_"+slipno].value ? document.forms[0]["BURDEN_CHARGE2_"+slipno].value : 0);
    }

    //負担金
    var burdenCharge = Math.max(burdenCharge1 + burdenCharge2, 0);
    document.getElementById('BURDEN_CHARGE_'+slipno).innerText = number_format(String(burdenCharge));
    document.forms[0]["TOTAL_BURDEN_CHARGE_"+slipno].value = burdenCharge;

    //調整金
    var adjustmentMoney = 0;
    document.getElementById('ADJUSTMENT_MONEY_'+slipno).innerText = number_format(String(adjustmentMoney));
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

//確定チェックボックス
function chkFlg(obj, slipno) {

    baseMoney          = document.getElementById('COUNTRY_MONEY_1_' + slipno);
    baseMoney2         = document.getElementById('COUNTRY_MONEY_2_' + slipno);
    thisAddFlg         = document.getElementById('COUNTRY_ADD_FLG_1_' + slipno);
    thisReducAddMoney  = document.getElementById('COUNTRY_ADD_MONEY_1_' + slipno);
    otherAddFlg        = document.getElementById('COUNTRY_ADD_FLG_2_' + slipno);
    otherReducAddMoney = document.getElementById('COUNTRY_ADD_MONEY_2_' + slipno);

    var countryTotal1 = 0;
    var countryTotal2 = 0;
    countryTotal1 += parseInt(baseMoney.value ? (baseMoney.value * 3) : 0);
    countryTotal1 += parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 3) : 0);
    countryTotal2 += parseInt(baseMoney2.value ? (baseMoney2.value * 9) : 0);
    countryTotal2 += parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 9) : 0);

    //支援金用授業料
    var maxCountryJugyouryou1 = document.forms[0]["MAX_COUNTRY_JUGYOURYOU1_"+slipno].value;
    var maxCountryJugyouryou2 = document.forms[0]["MAX_COUNTRY_JUGYOURYOU2_"+slipno].value;

    countryTotal1 = Math.min(countryTotal1, maxCountryJugyouryou1);
    countryTotal2 = Math.min(countryTotal2, maxCountryJugyouryou2);

    var countryTotal = countryTotal1 + countryTotal2;
    document.getElementById('countyr_total_money_'+slipno).innerText = number_format(String(countryTotal));
    chkFlgReadOnlySet(obj, slipno);
}

//確定チェックボックス
function chkFlgReadOnlySet(obj, slipno) {
    var reduc_income = '';
    var setRead = false;
    var setColor = "#000000";

    var countryZumiArray = document.forms[0]["COUNTRY_ZUMI_"+slipno].value.split(',');
    var prefZumiArray = document.forms[0]["PREF_ZUMI_"+slipno].value.split(',');
    var getArray = countryZumiArray.concat(prefZumiArray);
    var getArray1 = getArray.filter( function( value ) {
        return value.slice(-1) == '1' ;
    })
    var getArray2 = getArray.filter( function( value ) {
        return value.slice(-1) == '2' ;
    })

    if (obj.name == 'COUNTRY_BASE_FLG_1[]' || obj.name == 'COUNTRY_ADD_FLG_1[]' || obj.name == 'PREF_FLG_1[]') {
        reduc_income = 'REDUC_INCOME_1_';
        if (document.getElementById("COUNTRY_BASE_FLG_1_" + slipno).checked
            || document.getElementById("COUNTRY_ADD_FLG_1_" + slipno).checked
            || document.getElementById("PREF_FLG_1_" + slipno).checked
            || getArray1.length > 0
        ) {
            setRead = true;
            setColor = "#999999";
        }
    } else {
        reduc_income = 'REDUC_INCOME_2_';
        if (document.getElementById("COUNTRY_BASE_FLG_2_" + slipno).checked
            || document.getElementById("COUNTRY_ADD_FLG_2_" + slipno).checked
            || document.getElementById("PREF_FLG_2_" + slipno).checked
            || getArray2.length > 0
        ) {
            setRead = true;
            setColor = "#999999";
        }
    }

    //使用不可
    var targetFlg = false;
    if (obj.name == 'COUNTRY_BASE_FLG_1[]' || obj.name == 'COUNTRY_ADD_FLG_1[]') {
        targetFlg       = true;
        var target      = document.getElementById("COUNTRY_RANK_1_" + slipno);
        if (obj.name == 'COUNTRY_BASE_FLG_1[]') {
            var targetTxt = document.getElementById("COUNTRY_MONEY_1_" + slipno);
        } else {
            var targetTxt = document.getElementById("COUNTRY_ADD_MONEY_1_" + slipno);
        }
        var zumiArray   = countryZumiArray;
    }
    if (obj.name == 'COUNTRY_BASE_FLG_2[]' || obj.name == 'COUNTRY_ADD_FLG_2[]') {
        targetFlg       = true;
        var target      = document.getElementById("COUNTRY_RANK_2_" + slipno);
        if (obj.name == 'COUNTRY_BASE_FLG_2[]') {
            var targetTxt = document.getElementById("COUNTRY_MONEY_2_" + slipno);
        } else {
            var targetTxt = document.getElementById("COUNTRY_ADD_MONEY_2_" + slipno);
        }
        var zumiArray   = countryZumiArray;
    }
    if (obj.name == 'PREF_FLG_1[]') {
        targetFlg       = true;
        var target      = document.getElementById("PREF_RANK_1_" + slipno);
        var targetTxt   = document.getElementById("PREF_MONEY_1_" + slipno);
        var zumiArray   = prefZumiArray;
    }
    if (obj.name == 'PREF_FLG_2[]') {
        targetFlg       = true;
        var target      = document.getElementById("PREF_RANK_2_" + slipno);
        var targetTxt   = document.getElementById("PREF_MONEY_2_" + slipno);
        var zumiArray   = prefZumiArray;
    }

    if (targetFlg) {
        //確定済み
        var search = obj.name.replace('[]', '');
        zumiFlg = (zumiArray.indexOf(search) == -1) ? 0 : 1;
        //ランク
        for (var i = 0; i < target.length; i++) {
            target.options[i].disabled = false;
            if (obj.checked == true || zumiFlg) {
                if (target.options[i].value != target.value) {
                    target.options[i].disabled = true;
                }
            }
        }
    }

    //使用不可（再計算チェック）
    var kakuteiArr = ["COUNTRY_BASE_FLG_", "COUNTRY_ADD_FLG_", "PREF_FLG_", "REDUC_SCHOOL_FLG_"];
    var disabledCalcFlg1 = false;
    var disabledCalcFlg2 = false;
    for (var chkSoeji=1; chkSoeji <= 2; chkSoeji++) {
        var disFlg = false;
        for (var i=0; i < kakuteiArr.length; i++) {
            if (document.getElementById(kakuteiArr[i] + chkSoeji + "_"+ slipno).checked) disFlg = true;
        }
        if (chkSoeji == "1" && getArray1.length > 0) disFlg = true;
        if (chkSoeji == "2" && getArray2.length > 0) disFlg = true;
        if (document.forms[0]["PAID_FLG"+chkSoeji+"_"+slipno].value == "1") disFlg = true;

        if (chkSoeji == "1") {
            disabledCalcFlg1 = disFlg;
        } else {
            disabledCalcFlg2 = disFlg;
        }
    }
    var disabledCalcFlg = (disabledCalcFlg1 && disabledCalcFlg2) ? true : false;
    document.getElementById("CALC_FLG_" + slipno).disabled = disabledCalcFlg;
    if (disabledCalcFlg) document.getElementById("CALC_FLG_" + slipno).checked = false;

    for (var i=1; i <= 4; i++) {
        var setIndex = i + '_';
        document.getElementById(reduc_income + setIndex + slipno).style.color = setColor;
        document.getElementById(reduc_income + setIndex + slipno).readOnly    = setRead;
    }
}

//使用不可
function optDisabled() {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var el = document.forms[0].elements[i];
        rankName = '';
        kakuteiName2 = '';
        kakuteiName3 = '';
        kakuteiName4 = '';
        if (el.name == 'COUNTRY_RANK_1[]') {
            rankName        = 'COUNTRY_RANK_1';
            kakuteiName     = 'COUNTRY_BASE_FLG_1_';
            kakuteiName2    = 'COUNTRY_ADD_FLG_1_';
        } else if (el.name == 'COUNTRY_RANK_2[]') {
            rankName        = 'COUNTRY_RANK_2';
            kakuteiName     = 'COUNTRY_BASE_FLG_2_';
            kakuteiName2    = 'COUNTRY_ADD_FLG_2_';
        } else if (el.name == 'PREF_RANK_1[]') {
            rankName        = 'PREF_RANK_1';
            kakuteiName     = 'PREF_FLG_1_';
        } else if (el.name == 'PREF_RANK_2[]') {
            rankName        = 'PREF_RANK_2';
            kakuteiName     = 'PREF_FLG_2_';
        }

        if (rankName) {
            var id = el.id;
            slipno = id.replace(rankName+'_', '');

            var kakutei = document.getElementById(kakuteiName+slipno);
            kakuteiCheck = kakutei.checked;
            if (kakuteiName2 != '') {
                var kakutei2 = document.getElementById(kakuteiName2+slipno);
                if (kakutei2.checked) kakuteiCheck = kakutei2.checked;
            }
            if (kakuteiName3 != '') {
                var kakutei3 = document.getElementById(kakuteiName3+slipno);
                if (kakutei3.checked) kakuteiCheck = kakutei3.checked;
            }
            if (kakuteiName4 != '') {
                var kakutei4 = document.getElementById(kakuteiName4+slipno);
                if (kakutei4.checked) kakuteiCheck = kakutei4.checked;
            }

            //入金済み
            var soeji = rankName.substr(-1, 1);
            var checkPaid = false;
            if (document.forms[0]["PAID_FLG"+soeji+"_"+slipno].value == "1") checkPaid = true;

            if (kakuteiCheck || checkPaid) {
                //ランク
                for (var j = 0; j < el.length; j++) {
                    el.options[j].disabled = false;
                    if (el.options[j].value != el.value) {
                        el.options[j].disabled = true;
                    }
                }
            }
        }
    }
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
    } else if (obj.name == 'PREFALL_1') {
        reduc_dec_flg      = 'PREF_FLG_1';
    } else if (obj.name == 'PREFALL_2') {
        reduc_dec_flg      = 'PREF_FLG_2';
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

//全チェック（再計算）
function checkAll2(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CALC_FLG[]") {
            var id = document.forms[0].elements[i].id;
            slipno = id.replace('CALC_FLG_', '');

            if (document.getElementById("COUNTRY_BASE_FLG_1_" + slipno).checked ||
                document.getElementById("COUNTRY_BASE_FLG_2_" + slipno).checked ||
                document.getElementById("COUNTRY_ADD_FLG_1_" + slipno).checked ||
                document.getElementById("COUNTRY_ADD_FLG_2_" + slipno).checked ||
                document.getElementById("PREF_FLG_1_" + slipno).checked ||
                document.getElementById("PREF_FLG_2_" + slipno).checked
            ) {
                document.forms[0].elements[i].disabled = true;
            }

            if (!document.forms[0].elements[i].disabled) {
                document.forms[0].elements[i].checked = obj.checked;
            }
        }
    }
}

function kick_chgIncome(obj, slipno, div, soeji) {
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
        var prefRank1   = document.getElementById("PREF_RANK_1_" + slipno);
        var prefMoney1  = document.getElementById("PREF_MONEY_1_" + slipno);
        var prefDisp12  = document.getElementById("PREF_MONEY_12_" + slipno);
        var prefFlg1    = document.getElementById("PREF_FLG_1_" + slipno);
        var prefRank2   = document.getElementById("PREF_RANK_2_" + slipno);
        var prefMoney2  = document.getElementById("PREF_MONEY_2_" + slipno);
        var prefDisp22  = document.getElementById("PREF_MONEY_22_" + slipno);
        var prefFlg2    = document.getElementById("PREF_FLG_2_" + slipno);

        if (!prefFlg1.checked) {
            prefRank1.value = "";
            prefMoney1.value = "";
            prefDisp12.innerText = "";
        }
        if (!prefFlg2.checked) {
            prefRank2.value = "";
            prefMoney2.value = "";
            prefDisp22.innerText = "";
        }
    }

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

    //ランクの使用不可解除
    for (var s = 1; s <= 2; s++) {
        var rank = document.getElementById(div + "RANK_" + s + "_" + slipno);
        for (var i = 0; i < rank.length; i++) {
            rank.options[i].disabled = false;
        }
    }

    baseMoney1.value = "";
    baseDisp_12.innerText = "";
    baseMoney2.value = "";
    baseDisp_22.innerText = "";
    if (isCountry) {
        addMoney1.value = "";
        addDisp_12.innerText = "";
        addMoney2.value = "";
        addDisp_22.innerText = "";
    } else {
        document.forms[0]["BURDEN_CHARGE1_"+slipno].value = "";
        document.forms[0]["BURDEN_CHARGE2_"+slipno].value = "";
        document.forms[0]["TOTAL_BURDEN_CHARGE_"+slipno].value = "";
    }
    setTotalmony(slipno);
    return;
}

//画面の切替
function Page_jumper(link, prgId) {

    var prgIdLow = prgId.toLowerCase();
    link = link + "/P/" + prgId + "/" + prgIdLow + "index.php?SENDPRGID=KNJP721";
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

    var setDiv = (div == '1') ? '2' : '1';
    var refYearDiv = document.forms[0]["REFER_YEAR_DIV" + setDiv + "_" + slipno].value;
    document.forms[0]["REFER_YEAR_DIV_FLG"].value = false;
    if (refYearDiv == div) {
        document.forms[0]["REFER_YEAR_DIV_FLG"].value = true;
    }

    var targetTxt = document.getElementById("REDUC_INCOME_" +div + "_" + slipno);

    targetTxt.value = stringSum([income1, income2, income3, income4]);
    targetTxt.onchange();
}
