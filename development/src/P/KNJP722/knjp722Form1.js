
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
        var org = document.forms[0]["TMP_PREF_FLG"].value.split(',');

        var arr = [];
        var cnt = 0;
        for (var j=0; j < document.forms[0].elements.length; j++) {
            var el = document.forms[0].elements[j];
            if (el.name == 'PREF_FLG[]' && el.checked == true) {
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
        //必須選択チェック（新規で確定したとき）
        if (arr.length > 0 && document.forms[0]["PREF_DEC_MONTH"].value == "") {
            alert("{rval MSG304}\n( 補助額　確定月 )");
            return;
        }
    }

    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_cancel.disabled = true;
    document.forms[0].btn_close.disabled = true;
    document.forms[0].btn_calc.disabled = true;
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
    xmlhttp.open("POST", "knjp722index.php" , true);
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
    var reduction_seq       = document.getElementById("REDUCTION_SEQ_"+slipno); //マスタの連番
    var reduc_income        = document.forms[0]["REDUC_INCOME_"+slipno];        //所得割額
    var setReduc_income     = document.forms[0]["SET_REDUC_INCOME_"+slipno];    //所得HIDDEN
    setReduc_income.value   = reduc_income.value;
    //補助金
    var prefMoney           = document.getElementById("PREF_MONEY_"+slipno);
    var prefMoneyDisp       = document.getElementById("PREF_DISP_"+slipno);
    var prefRank            = document.getElementById("PREF_RANK_"+slipno);
    var pref_rare_case_cd       = document.getElementById("PREF_RARE_CASE_CD_"+slipno);
    var pref_rare_flg_array     = pref_rare_case_cd.value.split(":");   //例 T:1
    var pref_rare_flg           = pref_rare_flg_array[1];

    if (obj.name == 'REDUC_INCOME[]' && reduc_income.value == "") {
        if (pref_rare_flg != '1') {
            prefMoney.value = '';
        }
        prefMoneyDisp.innerText = '';
        prefRank.value = '';
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
    postdata += "&REDUC_INCOME="+document.forms[0]["REDUC_INCOME_"+slipno].value;   //必ず何か数字が入ってないとダメ！！TODO
    postdata += "&G_PREF_CD="+document.forms[0]["G_PREF_CD_"+slipno].value;
    postdata += "&PREF_RANK="+document.forms[0]["PREF_RANK_"+slipno].value;
    postdata += "&PREF_RARE_CASE_CD="+document.forms[0]["PREF_RARE_CASE_CD_"+slipno].value;
    postdata += "&PREF_MONEY="+document.forms[0]["PREF_MONEY_"+slipno].value;
    postdata += "&OBJ_NAME="+obj.name;
    postdata += "&SOEJI="+soeji;

    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent(obj, slipno)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp722index.php" , true);
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
function handleHttpEvent(obj, slipno) {
    if (xmlhttp.readyState == 4 ) {
        if (xmlhttp.status == 200 ) {
            var reduction_seq           = document.getElementById("REDUCTION_SEQ_"+slipno);     //マスタの連番
            var reduc_income            = document.getElementById("REDUC_INCOME_"+slipno);
            //補助金
            var prefRank                = document.getElementById("PREF_RANK_"+slipno);
            var prefMoney               = document.getElementById("PREF_MONEY_"+slipno);
            var prefMoneyDisp           = document.getElementById("PREF_DISP_"+slipno);
            var pref_rare_case_cd       = document.getElementById("PREF_RARE_CASE_CD_"+slipno);
            var pref_rare_flg_array     = pref_rare_case_cd.value.split(":");   //例 T:1
            var pref_rare_flg           = pref_rare_flg_array[1];
            //入学金
            var nyugakukin              = document.getElementById("NYUGAKUKIN_"+slipno);

            var json                    = xmlhttp.responseText;
            var response;

            eval('response = ' + json); //JSON形式のデータ(オブジェクトとして扱える)

            if (response.result) {
                var setPRank            = response.PREF_RANK;
                var setPrefMoney        = response.PREF_MONEY;
                var setNyugakukin       = response.NYUGAKUKIN;

                //金額区分
                if (response.MONEY_DIV != undefined) {
                    document.forms[0]["MONEY_DIV_"+slipno].value = response.MONEY_DIV;
                }
                //補助額
                if (response.REDUCTIONMONEY != undefined) {
                    document.forms[0]["REDUCTIONMONEY_"+slipno].value = response.REDUCTIONMONEY;
                }

                if (pref_rare_flg != "1") {
                    if (response.PREF_RANK == undefined) {
                        setPRank = "";
                    }
                    if (response.PREF_MONEY == undefined) {
                        setPrefMoney = "";
                    }
                    //補助額
                    prefRank.value = setPRank;
                    prefMoney.value = setPrefMoney;
                    prefMoneyDisp.innerText = number_format2(setPrefMoney);
                } else {
                    setPrefMoney   = document.forms[0]["PREF_MONEY_"+slipno].value;
                }

                //入学金
                nyugakukin.innerText    = number_format2(setNyugakukin);
                nyugakukin.value        = setNyugakukin;

                //減免
                if (response.REDUCTION_NYUGAKUKIN != undefined) {
                    document.forms[0]["REDUCTION_NYUGAKUKIN_"+slipno].value = response.REDUCTION_NYUGAKUKIN;
                }

                reduction_seq.value     = response.REDUCTION_SEQ;
            } else {
                alert("{rval MSG303}\n授業料軽減マスタ");
                reduction_seq.value     = "";
                reduc_income.value      = "";
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

    var prefMoney       = document.getElementById("PREF_MONEY_" + slipno);

    var prefRareCase    = document.getElementById("PREF_RARE_CASE_CD_" + slipno);
    var prefCasearray   = prefRareCase.value.split(":");       //例 T:1
    var prefCaseflg     = prefCasearray[1];

    var nyugakukin      = document.getElementById("NYUGAKUKIN_"+slipno);
    nyugakukin.value    = parseInt(nyugakukin.value);

    //所得割額
    var reduc_income    = document.getElementById("REDUC_INCOME_"+slipno);

    //減免先
    var reducNyugakukinSaki = document.forms[0]["REDUCTION_NYUGAKUKIN_SAKI_"+slipno];

    //減免
    var reducNyugakukin = document.forms[0]["REDUCTION_NYUGAKUKIN_"+slipno];
    reducNyugakukin  = parseInt(reducNyugakukin.value ? reducNyugakukin.value : 0);
    reducNyugakukin += parseInt(reducNyugakukinSaki.value ? reducNyugakukinSaki.value : 0);
    //表示
    document.getElementById('REDUCTION_NYUGAKUKIN_'+slipno).innerText = number_format(String(reducNyugakukin));
    //値セット
    document.forms[0]["REDUCTION_NYUGAKUKIN_"+slipno].value = reducNyugakukin;

    //負担金
    var burdenCharge = 0;
    document.getElementById('BURDEN_CHARGE_'+slipno).innerText = number_format(String(burdenCharge));

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
    chkFlgReadOnlySet(obj, slipno);
}

//確定チェックボックス
function chkFlgReadOnlySet(obj, slipno) {
    var reduc_income = '';
    var setRead = false;
    var setColor = "#000000";

    var prefZumiArray = document.forms[0]["PREF_ZUMI_"+slipno].value.split(',');
    var getArray = prefZumiArray.filter( function( value ) {
        return value.slice(-1) == '1' ;
    })

    if (obj.name == 'PREF_FLG[]') {
        reduc_income = 'REDUC_INCOME_';
        if (document.getElementById("PREF_FLG_" + slipno).checked
            || getArray.length > 0
        ) {
            setRead = true;
            setColor = "#999999";
        }
    }

    var prefRareCase        = document.getElementById("PREF_RARE_CASE_CD_" + slipno);
    var prefCasearray       = prefRareCase.value.split(":");    //例 T:1
    var prefCaseflg         = prefCasearray[1];

    //使用不可
    var targetFlg = false;
    if (obj.name == 'PREF_FLG[]') {
        targetFlg       = true;
        var target      = document.getElementById("PREF_RANK_" + slipno);
        var target2     = document.getElementById("PREF_RARE_CASE_CD_" + slipno);
        var rare        = prefCaseflg;
        var targetTxt   = document.getElementById("PREF_MONEY_" + slipno);
        var zumiArray   = prefZumiArray;
    }

    if (targetFlg) {
        //確定済み
        var search = obj.name.replace('[]', '');
        zumiFlg = (zumiArray.indexOf(search) == -1) ? 0 : 1;
        //ランク
        for (var i = 0; i < target.length; i++) {
            target.options[i].disabled = false;
            if (obj.checked == true || zumiFlg || rare == "1") {
                if (target.options[i].value != target.value) {
                    target.options[i].disabled = true;
                }
            }
        }
        //特殊フラグ
        for (var i = 0; i < target2.length; i++) {
            target2.options[i].disabled = false;
            if (obj.checked == true || zumiArray.length > 0) {
                if (target2.options[i].value != target2.value) {
                    target2.options[i].disabled = true;
                }
            }
        }
        if (rare == "1") {
            targetTxt.style.color = "#000000";
            targetTxt.readOnly = false;
            if (obj.checked == true || zumiFlg) {
                targetTxt.style.color = "#999999";
                targetTxt.readOnly = true;
            }
        }
    }

    //使用不可（再計算チェック）
    if (obj.checked) {
        document.getElementById("CALC_FLG_" + slipno).disabled = true;
        document.getElementById("CALC_FLG_" + slipno).checked = false;
    } else {
        if (!document.getElementById("PREF_FLG_" + slipno).checked &&
            !document.forms[0]["PREF_ZUMI_"+slipno].value
        ) {
            document.getElementById("CALC_FLG_" + slipno).disabled = false;
        }
    }
}

//使用不可
function optDisabled() {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var el = document.forms[0].elements[i];
        rankName = '';
        if (el.name == 'PREF_RANK[]') {
            rankName        = 'PREF_RANK';
            kakuteiName     = 'PREF_FLG_';
            rareName        = 'PREF_RARE_CASE_CD_';
        } else if (el.name == 'PREF_RARE_CASE_CD[]') {
            rankName        = 'PREF_RARE_CASE_CD';
            kakuteiName     = 'PREF_FLG_';
            rareName        = '';
        }

        if (rankName) {
            var id = el.id;
            slipno = id.replace(rankName+'_', '');

            var kakutei = document.getElementById(kakuteiName+slipno);
            kakuteiCheck = kakutei.checked;

            rare_flg = '0';
            if (rareName != '') {
                var rare_case_cd    = document.getElementById(rareName+slipno);
                var rare_flg_array  = rare_case_cd.value.split(':');
                var rare_flg        = rare_flg_array[1];
            }

            if (kakuteiCheck || rare_flg == '1') {
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

function chkByte(obj) {}

function checkAll(obj) {
    var reduc_dec_flg      = '';
    if (obj.name == 'PREFALL') {
        reduc_dec_flg      = 'PREF_FLG';
    } else if (obj.name == 'REDUC_SCHOOL_ALL') {
        reduc_dec_flg      = 'REDUC_SCHOOL_FLG';
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

            if (document.getElementById("PREF_FLG_" + slipno).checked) {
                document.forms[0].elements[i].disabled = true;
            }

            if (!document.forms[0].elements[i].disabled) {
                document.forms[0].elements[i].checked = obj.checked;
            }
        }
    }
}

function kick_chgIncome(obj, slipno, div) {
    var rare_case_cd        = document.getElementById(div + "RARE_CASE_CD_" + slipno);
    var case_cd_flg_array   = rare_case_cd.value.split(":"); //例 T:1
    var case_cd_flg         = case_cd_flg_array[1];

    var baseMoney   = document.getElementById(div + "MONEY_" + slipno);
    var baseDisp    = document.getElementById(div + "DISP_" + slipno);
    var reducMoney  = document.forms[0]["REDUCTION_NYUGAKUKIN_"+slipno];

    if (case_cd_flg == '1') {
        baseDisp.innerText = "";    //プレーンテキスト
        baseMoney.style.display = ""; //テキストボックス

        //ランクのクリア＆使用不可
        var rank = document.getElementById(div + "RANK_" + slipno);
        rank.value = "";
        for (var i = 0; i < rank.length; i++) {
            rank.options[i].disabled = false;
            if (rank.options[i].value != rank.value) {
                rank.options[i].disabled = true;
            }
        }
    } else {
        //軽減額入力なし
        baseDisp.innerText = baseMoney.value ? number_format(baseMoney.value) : "";     //プレーンテキスト
        baseMoney.style.display = "none";   //テキストボックス

        //ランクの使用不可解除
        var rank = document.getElementById(div + "RANK_" + slipno);
        for (var i = 0; i < rank.length; i++) {
            rank.options[i].disabled = false;
        }

        baseMoney.value = "";
        baseDisp.innerText = "";
        reducMoney.value = "";
    }
    setTotalmony(slipno);
    return;
}

//画面の切替
function Page_jumper(link, prgId) {

    var prgIdLow = prgId.toLowerCase();
    link = link + "/P/" + prgId + "/" + prgIdLow + "index.php?SENDPRGID=KNJP722";
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
