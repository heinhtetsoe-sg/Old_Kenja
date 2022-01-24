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
    xmlhttp.open("POST", "knjp170kindex.php" , true);
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

function btn_submit_update(cmd) {
    setTimeout("btn_submit('" + cmd + "')", 3000);
}
var xmlhttp = null;
var no;
var chgIncome_flg = false;

function kick_chgIncome(obj,schregno) {
    no = schregno;
    var prefecturescd      = document.getElementById("PREFECTURESCD_"+schregno); //軽減対象都道府県設定値
    var prefecturescd2     = document.getElementById("PREFECTURESCD2_"+schregno);//都道府県の表示の部分
    var reduction_seq_1    = document.getElementById("REDUCTION_SEQ_1_"+schregno); //マスタの連番
    var reduction_seq_2    = document.getElementById("REDUCTION_SEQ_2_"+schregno); //マスタの連番
    var reductionmoney_1   = document.getElementById("REDUCTIONMONEY_1_"+schregno);  //支援額(テキストボックス)
    var reductionmoney_2   = document.getElementById("REDUCTIONMONEY_2_"+schregno);  //支援額(テキストボックス)
    var reductionmoney_12  = document.getElementById("REDUCTIONMONEY_12_"+schregno); //支援額(プレーンテキスト)
    var reductionmoney_22  = document.getElementById("REDUCTIONMONEY_22_"+schregno); //支援額(プレーンテキスト)
    var reduc_dec_flg_1    = document.getElementById("REDUC_DEC_FLG_1_"+schregno);
    var reduc_dec_flg_2    = document.getElementById("REDUC_DEC_FLG_2_"+schregno);
    var reduc_income_1     = document.getElementById("REDUC_INCOME_1_"+schregno);   //所得割額
    var siblings1          = document.getElementById("INCOME_SIBLINGS1"+schregno);  //兄弟姉妹
    var reduc_rank_1       = document.getElementById("REDUC_RANK_1_"+schregno);     //ランク
    var reduc_income_2     = document.getElementById("REDUC_INCOME_2_"+schregno);   //所得割額
    var siblings2          = document.getElementById("INCOME_SIBLINGS2"+schregno);  //兄弟姉妹
    var reduc_rank_2       = document.getElementById("REDUC_RANK_2_"+schregno);     //ランク
    var reduc_remark       = '';
    var reduc_rare_case_cd = document.getElementById("REDUC_RARE_CASE_CD_"+schregno);
    var case_cd_flg_array  = reduc_rare_case_cd.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg        = case_cd_flg_array[1];
    var total_money        = document.getElementById("total_money_"+schregno);

    if (case_cd_flg == '1') {
        //軽減額入力あり
        reduc_dec_flg_1.style.display = "";  //チェックボックス
        reduc_dec_flg_2.style.display = "";  //チェックボックス
        reductionmoney_1.value = "";    //プレーンテキスト
        reductionmoney_12.innerText = "";    //プレーンテキスト
        reductionmoney_2.value = "";    //プレーンテキスト
        reductionmoney_22.innerText = "";    //プレーンテキスト
        reductionmoney_1.style.display = ""; //テキストボックス
        reductionmoney_2.style.display = ""; //テキストボックス
        reduc_rank_1.value = '';
        reduc_rank_2.value = '';
        total_money.innerText = "0";
        return;
    } else {
        //軽減額入力なし
        reductionmoney_12.innerText = number_format(reductionmoney_1.value + 0);    //プレーンテキスト
        reductionmoney_22.innerText = number_format(reductionmoney_2.value + 0);    //プレーンテキスト
        reductionmoney_1.style.display = "none"; //テキストボックス
        reductionmoney_2.style.display = "none"; //テキストボックス
    }

    chgIncome_flg = true;
    chgIncome(reduc_income_1, schregno);
}

/* POSTによるデータ送信 */
function chgIncome(obj,schregno) {

    no = schregno;
    var grade              = document.forms[0].GRADE.value;
    var prefecturescd      = document.getElementById("PREFECTURESCD_"+schregno);     //軽減対象都道府県設定値
    var prefecturescd2     = document.getElementById("PREFECTURESCD2_"+schregno);    //都道府県の表示の部分
    var reduction_seq_1    = document.getElementById("REDUCTION_SEQ_1_"+schregno);   //マスタの連番
    var reduction_seq_2    = document.getElementById("REDUCTION_SEQ_2_"+schregno);   //マスタの連番
    var reductionmoney_1   = document.getElementById("REDUCTIONMONEY_1_"+schregno);  //支援額(テキストボックス)
    var reductionmoney_2   = document.getElementById("REDUCTIONMONEY_2_"+schregno);  //支援額(テキストボックス)
    var reductionmoney_12  = document.getElementById("REDUCTIONMONEY_12_"+schregno); //支援額(プレーンテキスト)
    var reductionmoney_22  = document.getElementById("REDUCTIONMONEY_22_"+schregno); //支援額(プレーンテキスト)
    var reduc_dec_flg_1    = document.getElementById("REDUC_DEC_FLG_1_"+schregno);
    var reduc_dec_flg_2    = document.getElementById("REDUC_DEC_FLG_2_"+schregno);
    var reduc_income_1     = document.getElementById("REDUC_INCOME_1_"+schregno);    //所得割額
    var reduc_siblings1    = document.getElementById("INCOME_SIBLINGS1_"+schregno);  //兄弟姉妹
    var reduc_rank_1       = document.getElementById("REDUC_RANK_1_"+schregno);      //ランク
    var reduc_income_2     = document.getElementById("REDUC_INCOME_2_"+schregno);    //所得割額
    var reduc_siblings2    = document.getElementById("INCOME_SIBLINGS2_"+schregno);  //兄弟姉妹
    var reduc_rank_2       = document.getElementById("REDUC_RANK_2_"+schregno);      //ランク
    var reduc_remark       = '';
    var reduc_rare_case_cd = document.getElementById("REDUC_RARE_CASE_CD_"+schregno);
    var case_cd_flg_array  = reduc_rare_case_cd.value.split(":"); //例 SH:1:軽減額入力有り
    var case_cd_flg        = case_cd_flg_array[1];
    var prefcdName   = "PREF_CD";
    prefcdObject = eval("document.forms[0][\"" + prefcdName + "_" + schregno + "\"]");
    var pref_cd = prefcdObject.value;
    var rankFlg1           = document.forms[0]["RANK_FLG1_"+schregno];
    var rankFlg2           = document.forms[0]["RANK_FLG2_"+schregno];
    var siblings1Flg = obj.name == 'INCOME_SIBLINGS1[]' ? true : false;
    var siblings2Flg = obj.name == 'INCOME_SIBLINGS2[]' ? true : false;

    if (case_cd_flg == '1' && (obj.name.match(/^REDUC_INCOME_/) || obj.name.match(/^INCOME_SIBLINGS/) || obj.name.match(/^REDUC_RANK_/))) {
        if (case_cd_flg_array[0] != 'SH' || pref_cd != '28') {
            return;
        }
    }
    if (rankFlg1.value == "2" && reduc_income_1.value == "") {
        reductionmoney_12.innerText = '';
        reductionmoney_1.value = '';
    }
    if (rankFlg1.value == "1" && reduc_rank_1.value == "") {
        reductionmoney_12.innerText = '';
        reductionmoney_1.value = '';
    }
    if (rankFlg2.value == "2" && reduc_income_2.value == "") {
        reductionmoney_22.innerText = '';
        reductionmoney_2.value = '';
    }
    if (rankFlg2.value == "1" && reduc_rank_2.value == "") {
        reductionmoney_22.innerText = '';
        reductionmoney_2.value = '';
    }

    var pref;

    if (obj.value == '') {
        if (reduc_income_1.value == "" && reduc_rank_1.value == "" && reduc_income_2.value == "" && reduc_rank_2.value == "") {
            prefecturescd.value             = "";
            prefecturescd2.innerText        = "";
            reduction_seq_1.value           = "";
            reduction_seq_2.value           = "";
        }
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
    postdata += "&REDUC_INCOME_1="+document.getElementById("REDUC_INCOME_1_"+schregno).value; //REDUC_INCOME_1 には必ず何か数字が入ってないとダメ！！TODO
    postdata += "&INCOME_SIBLINGS1="+document.getElementById("INCOME_SIBLINGS1_"+schregno).value;
    postdata += "&REDUC_RANK_1="+document.getElementById("REDUC_RANK_1_"+schregno).value;
    postdata += "&RANK_FLG_1="+rankFlg1.value;
    postdata += "&REDUC_INCOME_2="+document.getElementById("REDUC_INCOME_2_"+schregno).value;
    postdata += "&INCOME_SIBLINGS2="+document.getElementById("INCOME_SIBLINGS2_"+schregno).value;
    postdata += "&REDUC_RANK_2="+document.getElementById("REDUC_RANK_2_"+schregno).value;
    postdata += "&RANK_FLG_2="+rankFlg2.value;
    postdata += "&OBJ_NAME="+obj.name;
    postdata += "&PREFECTURESCD="+pref;
    postdata += "&GRADE="+grade;
    postdata += "&GRADE2="+grade;
    postdata += "&PREF_CD2="+pref_cd;
    postdata += "&CASE_CD2="+case_cd_flg_array[0];
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent(obj, schregno)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp170kindex.php" , true);
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
            var prefecturescd      = document.getElementById("PREFECTURESCD_"+schregno); //軽減対象都道府県設定値
            var prefecturescd2     = document.getElementById("PREFECTURESCD2_"+schregno);//都道府県の表示の部分
            var reduction_seq_1    = document.getElementById("REDUCTION_SEQ_1_"+schregno); //マスタの連番
            var reduction_seq_2    = document.getElementById("REDUCTION_SEQ_2_"+schregno); //マスタの連番
            var reductionmoney_1   = document.getElementById("REDUCTIONMONEY_1_"+schregno);  //支援額(テキストボックス)
            var reductionmoney_2   = document.getElementById("REDUCTIONMONEY_2_"+schregno);  //支援額(テキストボックス)
            var reductionmoney_12  = document.getElementById("REDUCTIONMONEY_12_"+schregno); //支援額(プレーンテキスト)
            var reductionmoney_22  = document.getElementById("REDUCTIONMONEY_22_"+schregno); //支援額(プレーンテキスト)
            var reduc_dec_flg      = '';
            var reduc_income_1     = document.getElementById("REDUC_INCOME_1_"+schregno);   //所得割額
            var siblings_1         = document.getElementById("INCOME_SIBLINGS1_"+schregno);//兄弟姉妹
            var reduc_rank_1       = document.getElementById("REDUC_RANK_1_"+schregno);     //ランク
            var reduc_income_2     = document.getElementById("REDUC_INCOME_2_"+schregno);   //所得割額
            var siblings_2         = document.getElementById("INCOME_SIBLINGS2_"+schregno);//兄弟姉妹
            var reduc_rank_2       = document.getElementById("REDUC_RANK_2_"+schregno);     //ランク
            var reduc_remark       = '';
            var json = xmlhttp.responseText;
            var response;
            var reduc_rare_case_cd = document.getElementById("REDUC_RARE_CASE_CD_"+schregno);
            var case_cd_flg_array  = reduc_rare_case_cd.value.split(":"); //例 SH:1:軽減額入力有り
            var prefcdName   = "PREF_CD";
            prefcdObject = eval("document.forms[0][\"" + prefcdName + "_" + schregno + "\"]");
            var pref_cd = prefcdObject.value;
            var rankFlg1           = document.forms[0]["RANK_FLG1_"+schregno];
            var rankFlg2           = document.forms[0]["RANK_FLG2_"+schregno];

//var debug = document.getElementById('debug');
//debug.innerHTML = json;
            eval('response = ' + json); //JSON形式のデータ(オブジェクトとして扱える)

            if (response.result) {
                if (response.SET_ERR_FLG) {
                    alert("{rval MSG303}\n入学年度がありません。");
                    prefecturescd.value             = "";
                    prefecturescd2.innerText        = "";
                    reduction_seq_1.value           = "";
                    reductionmoney_1.value          = "";
                    reductionmoney_1.style.display  = "none";
                    reduc_income_1.value            = "";
                    siblings_1.value                = "";
                    reduc_rank_1.value              = "";
                    reductionmoney_12.innerText     = "";
                    rankFlg1.value                  = "";
                    reductionmoney_2.value          = "";
                    reductionmoney_2.style.display  = "none";
                    reduc_income_2.value            = "";
                    siblings_2.value                = "";
                    reduc_rank_2.value              = "";
                    reductionmoney_22.innerText     = "";
                    rankFlg2.value                  = "";
                } else {
                    //全てNULLの場合
                    prefecturescd.value      = response.PREFECTURESCD;
                    prefecturescd2.innerText = response.PREF;
                    if (obj.name == 'REDUC_INCOME_1[]' || obj.name == 'REDUC_RANK_1[]') {
                        reduction_seq_1.value    = response.REDUCTION_SEQ;
                        reductionmoney_1.value   = response.REDUCTIONMONEY_1;
                        if (case_cd_flg_array[0] != 'SH' || pref_cd != '28') {
                            reductionmoney_12.innerText = number_format(response.REDUCTIONMONEY_1);
                        } else {
                            reductionmoney_12.innerText = "";
                        }
                    }
                    if (response.REDUCTIONMONEY_2 != "") {
                        if (obj.name == 'REDUC_INCOME_2[]' || obj.name == 'REDUC_RANK_2[]') {
                            reduction_seq_2.value    = response.REDUCTION_SEQ;
                            reductionmoney_2.value = response.REDUCTIONMONEY_2;
                            if (case_cd_flg_array[0] != 'SH' || pref_cd != '28') {
                                reductionmoney_22.innerText = number_format(response.REDUCTIONMONEY_2);
                            } else {
                                reductionmoney_22.innerText = "";
                            }
                        }
                    }
                }
            } else {
                alert("{rval MSG303}\n授業料軽減マスタ");
                prefecturescd.value             = "";
                prefecturescd2.innerText        = "";
                reduction_seq_1.value           = "";
                if (obj.name == 'REDUC_INCOME_1[]' || obj.name == 'REDUC_RANK_1[]') {
                    reductionmoney_1.value          = "";
                    reductionmoney_1.style.display  = "none";
                    reductionmoney_12.innerText     = "";
                }
                if (obj.name == 'REDUC_INCOME_2[]' || obj.name == 'REDUC_RANK_2[]') {
                    reductionmoney_2.value          = "";
                    reductionmoney_2.style.display  = "none";
                    reductionmoney_22.innerText     = "";
                }
                if (obj.name == 'REDUC_INCOME_1[]') {
                    reduc_income_1.value            = "";
                }
                if (obj.name == 'REDUC_RANK_1[]') {
                    reduc_rank_1.value              = "";
                }
                if (obj.name == 'REDUC_INCOME_2[]') {
                    reduc_income_2.value            = "";
                }
                if (obj.name == 'REDUC_RANK_2[]') {
                    reduc_rank_2.value              = "";
                }
            }
            setTotalmony(schregno);
            //入力可否のチェックボックスを
            //
            if (chgIncome_flg) {
                chgIncome(reduc_income_2, schregno);
//                chgIncome(reduc_rank_1, schregno);
//                chgIncome(reduc_rank_2, schregno);
            }
            chgIncome_flg = false;
        } else {
            window.alert("通信エラーが発生しました。");
        }
    }
}

function setTotalmony(schregno) {
    var total_money;
    var money1;
    var money2;
    var reductionmoney_1;
    var reductionmoney_2;

    reductionmoney_1 = document.getElementById("REDUCTIONMONEY_1_"+schregno);
    reductionmoney_2 = document.getElementById("REDUCTIONMONEY_2_"+schregno);
    total_money = document.getElementById("total_money_"+schregno);

    reductionmoney_1.value = toInteger(reductionmoney_1.value);
    reductionmoney_2.value = toInteger(reductionmoney_2.value);

    if (reductionmoney_1.value != "") {
        money1 = parseInt(reductionmoney_1.value);
    } else {
        money1 = 0;
    }
    if (reductionmoney_2.value != "") {
        money2 = parseInt(reductionmoney_2.value);
    } else {
        money2 = 0;
    }
    total = money1 + money2;
    total_money.innerText = number_format(String(total));
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
function chkFlg(obj,schregno) {
    var reduc_income       = '';
    var reductionmoney     = '';
    var reductionrank      = '';
    if (obj.name == 'REDUC_DEC_FLG_1[]') {
        reduc_income       = 'REDUC_INCOME_1_';
        reductionmoney     = 'REDUCTIONMONEY_1_';
        reductionrank      = 'REDUC_RANK_1_';
    } else {
        reduc_income       = 'REDUC_INCOME_2_';
        reductionmoney     = 'REDUCTIONMONEY_2_';
        reductionrank      = 'REDUC_RANK_2_';
    }

    if (obj.checked) {
        document.getElementById(reduc_income+schregno).style.color = "#999999";
        document.getElementById(reductionmoney+schregno).style.color = "#999999";
        document.getElementById(reductionrank+schregno).style.color = "#999999";
        document.getElementById(reductionmoney+schregno).readOnly = true;
    } else {
        document.getElementById(reduc_income+schregno).style.color = "#000000";
        document.getElementById(reductionmoney+schregno).style.color = "#000000";
        document.getElementById(reductionrank+schregno).style.color = "#000000";
        document.getElementById(reductionmoney+schregno).readOnly = false;
    }
}

function focusText_1(obj,schregno) {
    if (document.getElementById("REDUC_DEC_FLG_1_"+schregno).checked == true) {
        document.getElementById(obj.id).style.color      = "#999999";
        obj.blur();
    }
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
        reduc_dec_flg      = 'REDUC_DEC_FLG_1';
    } else {
        reduc_dec_flg      = 'REDUC_DEC_FLG_2';
    }

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == reduc_dec_flg+"[]") {
            var id = document.forms[0].elements[i].id;
            if (document.getElementById(id).style.display != "none") {
                document.forms[0].elements[i].checked = obj.checked;
                var schregno = id.replace(/REDUC_DEC_FLG_[0-9]_/,"");
                chkFlg(document.forms[0].elements[i],schregno);
            }
        }
    }
}

//データ挿入用オブジェクトを入れる
var setObj;

function kirikae2(obj, schregno) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    if ((obj.name == "REDUC_RANK_1[]" && document.getElementById("REDUC_DEC_FLG_1_"+schregno).checked != true)
        || (obj.name == "REDUC_RANK_2[]" && document.getElementById("REDUC_DEC_FLG_2_"+schregno).checked != true)
    ) {
        event.cancelBubble = true
        event.returnValue = false;
        clickList(obj);
    }
}

function clickList(obj) {
    setObj = obj;
    myObj = document.getElementById("myID_Menu").style;
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top  = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function setClickValue(val) {
    if (val != '999') {
        if (val == '888') {
            setObj.value = "";
        } else {
            setObj.value = val;
        }
    }
    myHidden();
    if (val != '999') {
        if (setObj.onchange) {
            setObj.onchange();
        }
        setObj.focus();
    }
}

function myHidden() {
    document.getElementById("myID_Menu").style.visibility = "hidden";
}

//画面の切替
function Page_jumper(link, prgId) {

    var prgIdLow = prgId.toLowerCase();
    link = link + "/P/" + prgId + "/" + prgIdLow + "index.php?SENDPRGID=KNJP170K";
    link = link + "&S_GRADE=" + document.forms[0].GRADE.value;
    link = link + "&S_HR_CLASS=" + document.forms[0].HR_CLASS.value;
    var setRadPref = document.forms[0].RAD_PREF[0].checked ? 1 : 2;
    link = link + "&S_RAD_PREF=" + setRadPref;
    link = link + "&S_PREF=" + document.forms[0].PREF.value;

    parent.location.href=link;
}
