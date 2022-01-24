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
        TipTITLE = "奨学金情報";
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
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = handleHttpEvent2;
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp171kindex.php" , true);
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

function btn_submit(cmd) {

    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_cancel.disabled = true;
    document.forms[0].btn_close.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
var xmlhttp = null;
var no;

/* POSTによるデータ送信 */
function chgIncome(obj,schregno) {
    no = schregno;
    var reduc1Flg = obj.name == 'REDUC_INCOME_1[]' ? true : false;
    var soeji = obj.name == 'REDUC_INCOME_1[]' ? "1" : "2";
    var reduction_seq       = document.getElementById("REDUCTION_SEQ_" + soeji + "_"+schregno);     //マスタの連番
    var reductionmoney      = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "_"+schregno);    //支援額(テキストボックス)
    var reductionmoneyDisp  = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "2_"+schregno);   //支援額(プレーンテキスト)
    var reduc_income        = document.forms[0]["REDUC_INCOME_" + soeji + "_"+schregno];      //所得割額
    var setReduc_income     = document.forms[0]["SET_REDUC_INCOME" + soeji + "_"+schregno];   //所得HIDDEN
    setReduc_income.value = reduc_income.value;
    var reduc_remark        = '';
    if (reduc_income.value == "") {
        reductionmoney.value = '';
        reductionmoneyDisp.innerText = '';
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
    postdata += "&REDUC_INCOME_1="+document.forms[0]["REDUC_INCOME_1_"+schregno].value; //REDUC_INCOME_1 には必ず何か数字が入ってないとダメ！！TODO
    postdata += "&REDUC_INCOME_2="+document.forms[0]["REDUC_INCOME_2_"+schregno].value;
    postdata += "&OBJ_NAME="+obj.name;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent(obj, schregno)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp171kindex.php" , true);
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
function handleHttpEvent(obj, schregno) {
    if( xmlhttp.readyState == 4 ) {
        if( xmlhttp.status == 200 ) {
            var reduc1Flg = obj.name == 'REDUC_INCOME_1[]' ? true : false;
            var soeji = obj.name == 'REDUC_INCOME_1[]' ? "1" : "2";
            var reduction_seq       = document.getElementById("REDUCTION_SEQ_" + soeji + "_"+schregno);     //マスタの連番
            var reductionmoney      = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "_"+schregno);    //支援額(テキストボックス)
            var reductionmoneyDisp  = document.getElementById("REDUCTION_ADD_MONEY_" + soeji + "2_"+schregno);   //支援額(プレーンテキスト)
            var reduc_income        = document.getElementById("REDUC_INCOME_" + soeji + "_"+schregno);      //所得割額
            var reduc_remark        = '';
            var json = xmlhttp.responseText;
            var response;

            eval('response = ' + json); //JSON形式のデータ(オブジェクトとして扱える)

            if (response.result) {
                reduction_seq.value    = response.REDUCTION_SEQ;
                reductionmoney.value   = response.REDUCTION_ADD_MONEY;
                reductionmoneyDisp.innerText = number_format(response.REDUCTION_ADD_MONEY);
            } else {
                alert("{rval MSG303}\n授業料軽減マスタ");
                reduction_seq.value           = "";
                reductionmoney.value          = "";
                reduc_income.value            = "";
                reductionmoney.innerText     = "";
            }
            setTotalmony(schregno);
            //入力可否のチェックボックスを
        } else {
            window.alert("通信エラーが発生しました。");
        }
    }
}

function setTotalmony(schregno) {

    decFlg             = document.getElementById('REDUC_DEC_FLG_1_' + schregno);
    baseMoney          = document.getElementById('BASE_MONEY_1_' + schregno);
    thisAddFlg         = document.getElementById('REDUC_ADD_FLG_1_' + schregno);
    thisReducAddMoney  = document.getElementById('REDUCTION_ADD_MONEY_1_' + schregno);
    otherAddFlg        = document.getElementById('REDUC_ADD_FLG_2_' + schregno);
    otherReducAddMoney = document.getElementById('REDUCTION_ADD_MONEY_2_' + schregno);

    var total = 0;
    if (decFlg.checked) {
        total = total + parseInt(baseMoney.value ? (baseMoney.value * 12) : 0);
    }
    if (thisAddFlg.checked) {
        total = total + parseInt(thisReducAddMoney.value ? (thisReducAddMoney.value * 3) : 0);
    }
    if (otherAddFlg.checked) {
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
function chkFlg(obj, schregno) {
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

    decFlg             = document.getElementById('REDUC_DEC_FLG_1_' + schregno);
    baseMoney          = document.getElementById('BASE_MONEY_1_' + schregno);
    thisAddFlg         = document.getElementById('REDUC_ADD_FLG_' + thisSoeji + schregno);
    thisReducMoney     = document.getElementById('REDUCTION_ADD_MONEY_' + thisSoeji + schregno);
    otherAddFlg        = document.getElementById('REDUC_ADD_FLG_' + otherSoeji + schregno);
    otherReducMoney    = document.getElementById('REDUCTION_ADD_MONEY_' + otherSoeji + schregno);

    var total = 0;
    if (decFlg.checked) {
        total = total + parseInt(baseMoney.value ? (baseMoney.value * 12) : 0);
    }
    if (thisAddFlg.checked) {
        total = total + parseInt(thisReducMoney.value ? (thisReducMoney.value * monthThis) : 0);
    }
    if (otherAddFlg.checked) {
        total = total + parseInt(otherReducMoney.value ? (otherReducMoney.value * monthOther) : 0);
    }
    document.getElementById('total_money_'+schregno).innerText = number_format(String(total));
}

function focusText_2(obj,schregno) {
    if (document.getElementById("REDUC_DEC_FLG_2_"+schregno).checked == true) {
        document.getElementById(obj.id).style.color      = "#999999";
        obj.blur();
    }
}

function chkByte(obj) {}

function checkAll(obj) {
    var reduc_dec_flg      = '';
    if (obj.name == 'CHACKALL_1') {
        reduc_dec_flg      = 'REDUC_ADD_FLG_1';
    } else if (obj.name == 'CHACKALL_2') {
        reduc_dec_flg      = 'REDUC_ADD_FLG_2';
    } else if (obj.name == 'BASEALL_1') {
        reduc_dec_flg      = 'REDUC_DEC_FLG_1';
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
                chkFlg(document.forms[0].elements[i], schregno);
            }
        }
    }
}

