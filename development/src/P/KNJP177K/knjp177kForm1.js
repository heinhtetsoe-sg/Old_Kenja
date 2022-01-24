/* ----Tip Message---- */

/* initialize */
var TipTITLE176; //title
var St176;       //style
var goOffFlg176 = 0;

function goOffInfo176() {
    goOffFlg176 = 0;
    htm();
}

function setTipText176(ttl,msgs) {
    var styleType = "";
    var txt = [ttl,msgs];
    styleType = (St176 == 1) ? Style : Style2;
    stm(txt,styleType);
}

/* POSTによるデータ送信 */
function getInfo176(schregno,mode) {
    no = schregno;
    goOffFlg176 = 1

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
        TipTITLE176 = "奨学金情報";
        St176 = 2;
        postdata = "cmd=sendG";
    } else if(mode == 'T') {
        TipTITLE176 = "異動情報";
        St176 = 1;
        postdata = "cmd=sendT";
    } else {
        return;
    }

    //問い合わせ表示
    setTipText176(TipTITLE176,'<font color="red">問い合わせ中・・・</font>');
    postdata += "&SCHREGNO="+schregno;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = handleHttpEvent2_176;
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp177kindex.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);

    //timeLag防止
    if(goOffFlg176 == 0) htm();
}

/* レスポンスデータ処理 */
function handleHttpEvent2_176() {
    if( xmlhttp.readyState == 4 ) {
        if( xmlhttp.status == 200 ) {
            var txt = xmlhttp.responseText;

            if (txt == "NOTFOUND") {
                alert("{rval MSG303}\n" + TipTITLE176 + "マスタ");
            } else {
                setTipText176(TipTITLE176,txt)
            }
        }
    }

    //timeLag防止
    if(goOffFlg176 == 0) htm();
}

/* ---- end ---- */

function btn_submit(cmd) {

    document.forms[0].btn_close.disabled = true;
    document.forms[0].GRADE_SEND.value = document.forms[0].GRADE.value;
    document.forms[0].HR_CLASS_SEND.value = document.forms[0].HR_CLASS.value;
    document.forms[0].GRADE.disabled = true;
    document.forms[0].HR_CLASS.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
var xmlhttp = null;
var no;

/* POSTによるデータ送信 */
function chgIncome176(obj, schregno) {
    no = schregno;
    var reduc1Flg = obj.name == 'REDUC_INCOME_1[]' ? true : false;
    var soeji = obj.name == 'REDUC_INCOME_1[]' ? "1" : "2";
    var reduction_seq       = document.getElementById("REDUCTION_SEQ_" + soeji + "_"+schregno);     //マスタの連番
    var baseMoney           = document.getElementById("BASE_MONEY_" + soeji + "_"+schregno);    //支援額(テキストボックス)
    var baseMoneyDisp       = document.getElementById("BASE_MONEY_" + soeji + "2_"+schregno);   //支援額(プレーンテキスト)
    var addMoney            = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "_"+schregno);    //支援額(テキストボックス)
    var addMoneyDisp        = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "2_"+schregno);   //支援額(プレーンテキスト)
    var reduc_income        = document.forms[0]["REDUC_INCOME_" + soeji + "_"+schregno];      //所得割額
    var setReduc_income     = document.forms[0]["SET_REDUC_INCOME" + soeji + "_"+schregno];   //所得HIDDEN
    setReduc_income.value = reduc_income.value;
    var reduc_remark        = '';
    if (reduc_income.value == "") {
        baseMoney.value = '';
        baseMoneyDisp.innerText = '';
        addMoney.value = '';
        addMoneyDisp.innerText = '';
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
    postdata += "&SCHREGNO="+schregno;
    postdata += "&GRADE="+document.forms[0]["GRADE"].value;
    postdata += "&REDUC_INCOME_1="+document.forms[0]["REDUC_INCOME_1_"+schregno].value; //REDUC_INCOME_1 には必ず何か数字が入ってないとダメ！！TODO
    postdata += "&REDUC_INCOME_2="+document.forms[0]["REDUC_INCOME_2_"+schregno].value;
    postdata += "&OBJ_NAME="+obj.name;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent176(obj, schregno)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp177kindex.php" , true);
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
function handleHttpEvent176(obj, schregno) {
    if( xmlhttp.readyState == 4 ) {
        if( xmlhttp.status == 200 ) {
            var reduc1Flg = obj.name == 'REDUC_INCOME_1[]' ? true : false;
            var soeji = obj.name == 'REDUC_INCOME_1[]' ? "1" : "2";
            var reduction_seq       = document.getElementById("REDUCTION_SEQ_" + soeji + "_"+schregno);     //マスタの連番
            var baseMoney      = document.getElementById("BASE_MONEY_" + soeji + "_"+schregno);    //支援額(テキストボックス)
            var baseMoneyDisp  = document.getElementById("BASE_MONEY_" + soeji + "2_"+schregno);   //支援額(プレーンテキスト)
            var addMoney      = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "_"+schregno);    //支援額(テキストボックス)
            var addMoneyDisp  = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "2_"+schregno);   //支援額(プレーンテキスト)
            var reduc_income        = document.getElementById("REDUC_INCOME_" + soeji + "_"+schregno);      //所得割額
            var reduc_rare_case_cd = document.getElementById("REDUC_RARE_CASE_CD_"+soeji+"_"+schregno);
            var case_cd_flg_array  = reduc_rare_case_cd.value.split(":"); //例 SH:1:軽減額入力有り
            var case_cd_flg        = case_cd_flg_array[1];
            var reduc_remark        = '';
            var json = xmlhttp.responseText;
            var response;

            eval('response = ' + json); //JSON形式のデータ(オブジェクトとして扱える)

            if (response.result) {
                if (case_cd_flg != "1") {
                    baseMoney.value   = response.REDUCTIONMONEY;
                    addMoney.value   = response.REDUCTION_ADD_MONEY;
                    addMoneyDisp.innerText = number_format(response.REDUCTION_ADD_MONEY);
                    baseMoneyDisp.innerText = number_format(response.REDUCTIONMONEY);
                }
                reduction_seq.value    = response.REDUCTION_SEQ;
            } else {
                alert("{rval MSG303}\n授業料軽減マスタ");
                reduction_seq.value           = "";
                addMoney.value          = "";
                reduc_income.value            = "";
                addMoney.innerText     = "";
            }
            setTotalmony176(schregno);
            //入力可否のチェックボックスを
        } else {
            window.alert("通信エラーが発生しました。");
        }
    }
}

function setTotalmony176(schregno) {

    baseMoney          = document.getElementById('BASE_MONEY_1_' + schregno);
    baseMoney2         = document.getElementById('BASE_MONEY_2_' + schregno);
    thisAddFlg         = document.getElementById('REDUC_ADD_FLG_1_' + schregno);
    thisReducAddMoney  = document.getElementById('REDUCTION_ADD_MONEY_1_' + schregno);
    otherAddFlg        = document.getElementById('REDUC_ADD_FLG_2_' + schregno);
    otherReducAddMoney = document.getElementById('REDUCTION_ADD_MONEY_2_' + schregno);

    var reduc_rare_case_cd1 = document.getElementById("REDUC_RARE_CASE_CD_1_" + schregno);
    var case_cd_flg_array1  = reduc_rare_case_cd1.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg1        = case_cd_flg_array1[1];

    var reduc_rare_case_cd2 = document.getElementById("REDUC_RARE_CASE_CD_2_" + schregno);
    var case_cd_flg_array2  = reduc_rare_case_cd2.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg2        = case_cd_flg_array2[1];
    var total = 0;
    if (case_cd_flg1 == "1") {
        total = total + parseInt(baseMoney.value ? (baseMoney.value * 1) : 0);
    }
    if (case_cd_flg1 != "1") {
        total = total + parseInt(baseMoney.value ? (baseMoney.value * 3) : 0);
    }
    if (case_cd_flg1 == "1") {
        total = total + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 1) : 0);
    }
    if (case_cd_flg1 != "1") {
        total = total + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 3) : 0);
    }
    if (case_cd_flg2 == "1") {
        total = total + parseInt(baseMoney2.value ? (baseMoney2.value * 1) : 0);
    }
    if (case_cd_flg2 != "1") {
        total = total + parseInt(baseMoney2.value ? (baseMoney2.value * 9) : 0);
    }
    if (case_cd_flg2 == "1") {
        total = total + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 1) : 0);
    }
    if (case_cd_flg2 != "1") {
        total = total + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 9) : 0);
    }
    document.getElementById('total_money_'+schregno).innerText = number_format(String(total));
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
function chkFlg176(obj, schregno) {
    var thisSoeji   = '1_';
    var otherSoeji  = '2_';
    var monthThis   = 3;
    var monthOther  = 9;
    if (obj.name == 'REDUC_ADD_FLG_2[]') {
        thisSoeji   = '2_';
        otherSoeji  = '1_';
        monthThis   = 9;
        monthOther  = 3;
    }

    baseMoney          = document.getElementById('BASE_MONEY_1_' + schregno);
    baseMoney2         = document.getElementById('BASE_MONEY_2_' + schregno);
    thisAddFlg         = document.getElementById('REDUC_ADD_FLG_1_' + schregno);
    thisReducAddMoney  = document.getElementById('REDUCTION_ADD_MONEY_1_' + schregno);
    otherAddFlg        = document.getElementById('REDUC_ADD_FLG_2_' + schregno);
    otherReducAddMoney = document.getElementById('REDUCTION_ADD_MONEY_2_' + schregno);

    var reduc_rare_case_cd1 = document.getElementById("REDUC_RARE_CASE_CD_1_" + schregno);
    var case_cd_flg_array1  = reduc_rare_case_cd1.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg1        = case_cd_flg_array1[1];

    var reduc_rare_case_cd2 = document.getElementById("REDUC_RARE_CASE_CD_2_" + schregno);
    var case_cd_flg_array2  = reduc_rare_case_cd2.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg2        = case_cd_flg_array2[1];

    var total = 0;
    if (case_cd_flg1 == "1") {
        total = total + parseInt(baseMoney.value ? (baseMoney.value * 1) : 0);
    }
    if (case_cd_flg1 != "1") {
        total = total + parseInt(baseMoney.value ? (baseMoney.value * 3) : 0);
    }
    if (case_cd_flg1 == "1") {
        total = total + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 1) : 0);
    }
    if (case_cd_flg1 != "1") {
        total = total + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 3) : 0);
    }
    if (case_cd_flg2 == "1") {
        total = total + parseInt(baseMoney2.value ? (baseMoney2.value * 1) : 0);
    }
    if (case_cd_flg2 != "1") {
        total = total + parseInt(baseMoney2.value ? (baseMoney2.value * 9) : 0);
    }
    if (case_cd_flg2 == "1") {
        total = total + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 1) : 0);
    }
    if (case_cd_flg2 != "1") {
        total = total + parseInt(otherReducAddMoney.value ? (otherReducAddMoney.value * 9) : 0);
    }
    document.getElementById('total_money_'+schregno).innerText = number_format(String(total));
}

function chkByte176(obj) {}

function checkAll176(obj) {
    var reduc_dec_flg      = '';
    if (obj.name == 'CHACKALL_1') {
        reduc_dec_flg      = 'REDUC_ADD_FLG_1';
    } else if (obj.name == 'CHACKALL_2') {
        reduc_dec_flg      = 'REDUC_ADD_FLG_2';
    } else if (obj.name == 'BASEALL_2') {
        reduc_dec_flg      = 'REDUC_DEC_FLG_2';
    } else {
        reduc_dec_flg      = 'OFFSET_FLG';
    }

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == reduc_dec_flg+"[]") {
            var id = document.forms[0].elements[i].id;
            if (!document.forms[0].elements[i].disabled) {
                document.forms[0].elements[i].checked = obj.checked;
            }
            if (reduc_dec_flg != 'OFFSET_FLG') {
                schregno = id.replace(reduc_dec_flg + '_', '');
                chkFlg176(document.forms[0].elements[i], schregno);
            }
        }
    }
}
function kick_chgIncome176(obj, schregno, div, soeji) {
    var reduc_rare_case_cd = document.getElementById("REDUC_RARE_CASE_CD_" + div + "_" + schregno);
    var case_cd_flg_array  = reduc_rare_case_cd.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg        = case_cd_flg_array[1];

    baseMoney          = document.getElementById("BASE_MONEY_" + div + "_" + schregno);
    addMoney          = document.getElementById("REDUCTION_ADD_MONEY_" + div + "_" + schregno);
    var baseDisp_12  = document.getElementById("BASE_MONEY_" + div + "2_" + schregno);
    var addDisp_12  = document.getElementById("REDUCTION_ADD_MONEY_" + div + "2_" + schregno);

    if (case_cd_flg == '1') {
        baseDisp_12.innerText = "";    //プレーンテキスト
        baseMoney.style.display = ""; //テキストボックス
        addDisp_12.innerText = "";    //プレーンテキスト
        addMoney.style.display = ""; //テキストボックス
    } else {
        //軽減額入力なし
        baseDisp_12.innerText = baseMoney.value ? number_format(baseMoney.value) : "";    //プレーンテキスト
        baseMoney.style.display = "none"; //テキストボックス
        addDisp_12.innerText = addMoney.value ? number_format(addMoney.value) : "";    //プレーンテキスト
        addMoney.style.display = "none"; //テキストボックス
        var reducName = "REDUC_INCOME_" + div + "[]";
        chgIncome176(document.forms[0][reducName][soeji], schregno);
    }
    setTotalmony176(schregno);
    return;
}

//画面の切替
function Page_jumper(link, prgId) {

    var prgIdLow = prgId.toLowerCase();
    link = link + "/P/" + prgId + "/" + prgIdLow + "index.php?SENDPRGID=KNJP177K";
    link = link + "&S_GRADE=" + document.forms[0].GRADE.value;
    link = link + "&S_HR_CLASS=" + document.forms[0].HR_CLASS.value;
    var setRadPref = document.forms[0].RAD_PREF[0].checked ? 1 : 2;
    link = link + "&S_RAD_PREF=" + setRadPref;
    link = link + "&S_PREF=" + document.forms[0].PREF.value;

    parent.location.href=link;
}
