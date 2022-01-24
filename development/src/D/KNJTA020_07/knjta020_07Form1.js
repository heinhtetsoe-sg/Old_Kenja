//サブミット
function btn_submit(cmd) {
    if (cmd == 'stk_cancel' && document.forms[0].SHITAKU_CANCEL_CHOKU_FLG.checked) {
        alert('支度金の情報は\n更新されません。');
    }
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    if (cmd == "update") {
        //審査計算書の「判定結果」等のチェック用
        var kanryouFlg = document.forms[0].SHINSEI_KANRYOU_FLG.checked;
        var shinsaHan1 = document.forms[0].SHINSA_HANTEI1.value;
        var shinsaHan2 = document.forms[0].SHINSA_HANTEI2.value;
        var msg = "";
        var shinsayear = document.forms[0].SHINSA_YEAR.value;
        if (kanryouFlg && (shinsaHan1 == "NG" || shinsaHan2 == "NG")) {
            msg += "「入力完了」のチェックはできません。\n\n";
            if (shinsaHan1 == "NG") {
                if (shinsayear != "" && shinsayear < 2015) {
                    msg += "審査計算書１の「世帯状況」が「空白」です。\n";
                } else {
                    msg += "審査計算書１の「保護者等の所得割額」が「空白」です。\n";
                    alert(msg);
                    return false;
                }
            }
            if (shinsaHan2 == "NG") {
                msg += "審査計算書１の「判定結果」が「審査ＮＧ」です。\n";
            }
            alert(msg);
            return false;
        }
        //支直
        var shitakuCancelFlg = document.forms[0].SHITAKU_CANCEL_CHOKU_FLG.checked;
        if (!shitakuCancelFlg) {
            kanryouFlg = document.forms[0].STK_SHINSEI_KANRYOU_FLG.checked;
            msg = "";
            if (kanryouFlg && (shinsaHan1 == "NG" || shinsaHan2 == "NG")) {
                msg += "「入力完了」のチェックはできません。\n\n";
                if (shinsaHan1 == "NG") {
                    if (shinsayear != "" && shinsayear < 2015) {
                        msg += "審査計算書１の「世帯状況」が「空白」です。\n";
                    } else {
                        msg += "審査計算書１の「保護者等の所得割額」が「空白」です。\n";
                        alert(msg);
                        return false;
                    }
                }
                if (shinsaHan2 == "NG") {
                    msg += "審査計算書１の「判定結果」が「審査ＮＧ」です。\n";
                }
                alert(msg);
                return false;
            }
        }

        var ritsu = document.forms[0].RITSUCD.value;
        var jitakuFlg = document.forms[0].TSUUGAKU_DIV[0].checked;
        var kibouGk = parseInt(document.forms[0].YOYAKU_KIBOU_GK.value);
        var errMsg = "";
        if (ritsu == "2" && jitakuFlg && kibouGk > 18000) {
            errMsg = "公立－自宅\n18,000円を超えていますが宜しいですか？";
        } else if (ritsu == "2" && !jitakuFlg && kibouGk > 23000) {
            errMsg = "公立－自宅外\n23,000円を超えていますが宜しいですか？";
        } else if (ritsu == "3" && jitakuFlg && kibouGk > 30000) {
            errMsg = "私立－自宅\n30,000円を超えていますが宜しいですか？";
        } else if (ritsu == "3" && !jitakuFlg && kibouGk > 35000) {
            errMsg = "私立－自宅外\n35,000円を超えていますが宜しいですか？";
        }

        var ritsu = document.forms[0].STK_RITSUCD.value;
        var kokkouRitsuFlg = document.forms[0].STK_SHITAKUKIN_TAIYO_DIV[0].checked;
        var errMsgSep = errMsg == "" ? "" : "\n\n";
        if (ritsu == "2" && !kokkouRitsuFlg) {
            errMsg += errMsgSep + "公立－私立の高等学校\nを選択していますが宜しいですか？";
        } else if (ritsu == "3" && kokkouRitsuFlg) {
            errMsg += errMsgSep + "私立－国公立の高等学校\nを選択していますが宜しいですか？";
        }

        if (errMsg != "" && !confirm(errMsg)) {
            return false;
        }
    }
    //給付
    if (cmd == "kyuhu_delete" && !confirm('{rval MSG103}')){
        return false;
    }
    if (cmd == "kyuhu_update"){
        var maxSeq = document.getElementsByName("KYUHU_MAX_SEQ").value;
        for (var seq = 1; seq <= maxSeq; seq++) {
            var setSeq = seq == 1 ? '' : '_'+seq;
            //兄弟の日付チェックは第２子以降の時だけ
            var shotokuwaridiv = document.getElementsByName("SHOTOKUWARI_DIV" + setSeq);
            var kyuhu_shotokuwaridiv_value = "";
            //所得割区分の値を取得
            for(var i = 0; i < shotokuwaridiv.length; i++) {
                if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '1') {
                    kyuhu_shotokuwaridiv_value = "1";
                } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '2') {
                    kyuhu_shotokuwaridiv_value = "2";
                } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '3') {
                    kyuhu_shotokuwaridiv_value = "3";
                }
            }
            if(kyuhu_shotokuwaridiv_value == '3'){
                var getflg = dateExtra(cmd, setSeq);
                if (getflg == false) {
                    return false;
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//日付を入力させるポップアップ更新画面
function loadCheck(REQUESTROOT) {
    load  = "loadwindow('"+ REQUESTROOT +"/A/KNJTA020_07/knjta020_07index.php?cmd=subForm1";
    load += "',250,200,550,250)";

    eval(load);
}
//終了ボタン
function btnCloseWin() {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    closeWin();
}
//卒業予定月の算出
function keisanGradYm() {
    /************************************************
    var gradYM = 42703  卒業予定月
    var shinseiG = 4    申請年度-元号
    var shinseiY = 24   申請年度-年
    var grade = 01      学年
    var katei = 01      課程コード
    var smcnt = 36      就学月数(課程マスタ)
    ************************************************/
    var gradYM = document.forms[0].H_GRAD_YM.value;
    var shinseiG = document.forms[0].SHINSEI_G.value;
    var shinseiY = document.forms[0].SHINSEI_YY.value;
    var grade = document.forms[0].GRADE.value;
    var katei = document.forms[0].KATEI.value;
    var smcnt;
    //チェック
    if (gradYM != '') {
        //alert('卒業予定月が入力済の場合、算出しません。');
        return false;
    }
    if (shinseiG == '' || shinseiY == '' || grade == '' || katei == '') {
        //alert('申請年度・課程・学年が未入力の場合、算出しません。');
        return false;
    }
    smcntObject = eval("document.forms[0].SMCNT" + katei);
    smcnt = smcntObject.value;
    if (smcnt == '') {
        //alert('就学月数(課程マスタ)が未設定の場合、算出しません。');
        return false;
    }
    //整数値に変換
    shinseiY = parseInt(shinseiY);
    grade = parseInt(grade);
    smcnt = parseInt(smcnt);
    //加算月数を算出
    startM = 3;
    kasanM = smcnt - (grade - 1) * 12;
    if (kasanM <= 0) {
        //alert('加算月数が就学月数(課程マスタ)以上の場合、算出しません。');
        return false;
    }
    //卒業予定月を算出
    n = eval(kasanM);//加算月数
    y = eval(shinseiY);//年
    m = eval(startM);//月
    //年を加算
    y  = y + Math.floor((m + n) / 12);
    //月を加算
    m = (m + (n % 12)) % 12;
    if (m == 0) m = 12;
    if (m < 10) m = "0" + m;
    //alert(n+"ヶ月後は"+y+"年"+m+"月です");
    //卒業予定月にセット
    document.forms[0].H_GRAD_YM.value = shinseiG + y + m;
}

//学校イベント
function schoolSelectEvent() {
    knjAjax('kateiGet');
}

/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax(cmd) { //この区間は送信処理、
    var sendData = '';
    var seq = '';
    var form_datas = document.forms[0];
    form_datas.cmd.value = cmd;

    sendData = "cmd=kateiGet";
    sendData += "&KOJIN_NO=KOJIN_NO";
    sendData += "&PROGRAMID=KNJTA020_07";
    sendData += "&H_SCHOOL_CD="+form_datas.H_SCHOOL_CD.value;
    sendData += "&KATEI="+form_datas.KATEI.value;
    sendData += "&STK_H_SCHOOL_CD="+form_datas.STK_H_SCHOOL_CD.value;
    sendData += "&STK_KATEI="+form_datas.STK_KATEI.value;

    httpObj = new ActiveXObject("Microsoft.XMLHTTP");
    httpObj.onreadystatechange = statusCheck;
    httpObj.open("GET","knjta020_07index.php?" + sendData,true);  //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null);                                        //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck() { //サーバからの応答をチェック
    /******** httpObj.readyState *******/  /********** httpObj.status *********/
    /*  0:初期化されていない           */  /*  200:OK                         */
    /*  1:読込み中                     */  /*  403:アクセス拒否               */
    /*  2:読込み完了                   */  /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */  /***********************************/
    /*  4:準備完了                     */
    /***********************************/
    if ((httpObj.readyState == 4) && (httpObj.status == 200)) {
        var targetKatei = document.getElementById("KATEI_DIV");
        var targetSchoolName = document.getElementById("H_SCHOOL_NAME");
        var targetSchoolRitu = document.getElementById("H_SCHOOL_RITSU");

        var targetStkKatei = document.getElementById("STK_KATEI_DIV");
        var targetStkSchoolName = document.getElementById("STK_H_SCHOOL_NAME");
        var targetStkSchoolRitu = document.getElementById("STK_H_SCHOOL_RITSU");

        var response = httpObj.responseText;
        var responseArray = response.split("::");

        targetKatei.innerHTML = responseArray[0];
        targetSchoolName.innerHTML = responseArray[1];
        targetSchoolRitu.innerHTML = responseArray[2];
        document.forms[0].RITSUCD.value = responseArray[3];

        targetStkKatei.innerHTML = responseArray[4];
        targetStkSchoolName.innerHTML = responseArray[5];
        targetStkSchoolRitu.innerHTML = responseArray[6];
        document.forms[0].STK_RITSUCD.value = responseArray[7];
    }
}
/************************** Ajax ***********************************/

//給付用学校イベント
function schoolSelectEventData(flg) {
    knjAjax2('');
}

//給付用学校イベント
function schoolSelectEventData2(flg) {
    knjAjax2('_2');
}

/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax2(setSeq) { //この区間は送信処理、
    document.forms[0].SCHOOL_SEARCH_SEQ.value = setSeq;
    var sendData = '';
//    var seq = '';
    var form_datas = document.forms[0];
    var setCmd = "kateiGet" + setSeq;

    sendData = "cmd=" + setCmd;
    sendData += "&KOJIN_NO=KOJIN_NO";
    sendData += "&PROGRAMID=KNJTA020_07";
    sendData += "&KYUHU_H_SCHOOL_CD=" + form_datas["KYUHU_H_SCHOOL_CD" + setSeq].value;
    sendData += "&KYUHU_KATEI=" + form_datas["KYUHU_KATEI" + setSeq].value;

    httpObj = new ActiveXObject("Microsoft.XMLHTTP");
    httpObj.onreadystatechange = statusCheck2;
    httpObj.open("GET","knjta020_07index.php?" + sendData,true);  //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null);                                        //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck2() { //サーバからの応答をチェック
    /******** httpObj.readyState *******/  /********** httpObj.status *********/
    /*  0:初期化されていない           */  /*  200:OK                         */
    /*  1:読込み中                     */  /*  403:アクセス拒否               */
    /*  2:読込み完了                   */  /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */  /***********************************/
    /*  4:準備完了                     */
    /***********************************/
    var setSeq = document.forms[0].SCHOOL_SEARCH_SEQ.value;
    if ((httpObj.readyState == 4) && (httpObj.status == 200)) {
        var targetKyuhuKatei = document.getElementById("KYUHU_KATEI_DIV" + setSeq);
        var targetKyuhuSchoolName = document.getElementById("KYUHU_H_SCHOOL_NAME" + setSeq);
        var targetKyuhuSchoolRitu = document.getElementById("KYUHU_H_SCHOOL_RITSU" + setSeq);

        var response = httpObj.responseText;
        var responseArray = response.split("::");

        targetKyuhuKatei.innerHTML = responseArray[0];
        targetKyuhuSchoolName.innerHTML = responseArray[1];
        targetKyuhuSchoolRitu.innerHTML = responseArray[2];
        document.forms[0]["KYUHU_RITSUCD" + setSeq].value = responseArray[3];
    }
}
/************************** Ajax ***********************************/

//担当課チェック
function OnSectionError()
{
    alert('{rval MSG300}' + '\n担当課が設定されていません。');
    closeWin();
}

//併給区分チェック
function heikyuCheck1(setSeq)
{
    var heikyu_flg1 = document.forms[0]["HEIKYUU_SHOUGAKU_FLG1" + setSeq].checked;
    var heikyu_flg2 = document.forms[0]["HEIKYUU_SHOUGAKU_FLG2" + setSeq].checked;
    if (heikyu_flg1) {
        document.forms[0]["HEIKYUU_SHOUGAKU_FLG2" + setSeq].checked = "";
    }
}
function heikyuCheck2(setSeq)
{
    var heikyu_flg1 = document.forms[0]["HEIKYUU_SHOUGAKU_FLG1" + setSeq].checked;
    var heikyu_flg2 = document.forms[0]["HEIKYUU_SHOUGAKU_FLG2" + setSeq].checked;
    if (heikyu_flg2) {
        document.forms[0]["HEIKYUU_SHOUGAKU_FLG1" + setSeq].checked = "";
    }
}

//所得割額確認済みチェック
function shotokuwariCheck(setSeq)
{
    var shotokuwari_flg = document.forms[0]["SHOTOKUWARI_GK_CHECK_FLG" + setSeq].checked;
    var shotokuwari_gk = document.forms[0]["SHOTOKUWARI_GK" + setSeq].value;
    if (shotokuwari_flg) {
        if (shotokuwari_gk == "") {
            document.forms[0]["SHOTOKUWARI_GK" + setSeq].value = 0;
        }
    }
}

//所得割区分テキスト切換
function change_text(setSeq) {
    var shotokuwaridiv = document.getElementsByName("SHOTOKUWARI_DIV" + setSeq);
    var kyuhu_shotokuwaridiv_value = "";
    //所得割区分の値を取得
    for(var i = 0; i < shotokuwaridiv.length; i++) {
        if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '1') {
            kyuhu_shotokuwaridiv_value = "1";
        } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '2') {
            kyuhu_shotokuwaridiv_value = "2";
        } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '3') {
            kyuhu_shotokuwaridiv_value = "3";
        }
    }
    
    if (kyuhu_shotokuwaridiv_value == "3") {
        document.forms[0]["KYOUDAI_BIRTHDAY" + setSeq].disabled = "";
        document.forms[0]["KYOUDAI_TSUZUKIGARA_CD" + setSeq].disabled = "";
        document.forms[0]["KYOUDAI_FAMILY_NAME" + setSeq].disabled = "";
        document.forms[0]["KYOUDAI_FIRST_NAME" + setSeq].disabled = "";
        document.forms[0]["KYOUDAI_FAMILY_NAME_KANA" + setSeq].disabled = "";
        document.forms[0]["KYOUDAI_FIRST_NAME_KANA" + setSeq].disabled = "";
        document.forms[0]["KYOUDAI_BIRTHDAY" + setSeq].style.backgroundColor = '#ffffff';
        document.forms[0]["KYOUDAI_TSUZUKIGARA_CD" + setSeq].style.backgroundColor = '#ffffff';
        document.forms[0]["KYOUDAI_FAMILY_NAME" + setSeq].style.backgroundColor = '#ffffff';
        document.forms[0]["KYOUDAI_FIRST_NAME" + setSeq].style.backgroundColor = '#ffffff';
        document.forms[0]["KYOUDAI_FAMILY_NAME_KANA" + setSeq].style.backgroundColor = '#ffffff';
        document.forms[0]["KYOUDAI_FIRST_NAME_KANA" + setSeq].style.backgroundColor = '#ffffff';
    } else {
        if (!document.forms[0]["KYOUDAI_BIRTHDAY" + setSeq].disabled) {
            document.forms[0]["KYOUDAI_BIRTHDAY" + setSeq].disabled = "disabled";
            document.forms[0]["KYOUDAI_BIRTHDAY" + setSeq].style.backgroundColor = 'silver';
        }
        if (!document.forms[0]["KYOUDAI_TSUZUKIGARA_CD" + setSeq].disabled) {
            document.forms[0]["KYOUDAI_TSUZUKIGARA_CD" + setSeq].disabled = "disabled";
            document.forms[0]["KYOUDAI_TSUZUKIGARA_CD" + setSeq].style.backgroundColor = 'silver';
        }
        if (!document.forms[0]["KYOUDAI_FAMILY_NAME" + setSeq].disabled) {
            document.forms[0]["KYOUDAI_FAMILY_NAME" + setSeq].disabled = "disabled";
            document.forms[0]["KYOUDAI_FAMILY_NAME" + setSeq].style.backgroundColor = 'silver';
        }
        if (!document.forms[0]["KYOUDAI_FIRST_NAME" + setSeq].disabled) {
            document.forms[0]["KYOUDAI_FIRST_NAME" + setSeq].disabled = "disabled";
            document.forms[0]["KYOUDAI_FIRST_NAME" + setSeq].style.backgroundColor = 'silver';
        }
        if (!document.forms[0]["KYOUDAI_FAMILY_NAME_KANA" + setSeq].disabled) {
            document.forms[0]["KYOUDAI_FAMILY_NAME_KANA" + setSeq].disabled = "disabled";
            document.forms[0]["KYOUDAI_FAMILY_NAME_KANA" + setSeq].style.backgroundColor = 'silver';
        }
        if (!document.forms[0]["KYOUDAI_FIRST_NAME_KANA" + setSeq].disabled) {
            document.forms[0]["KYOUDAI_FIRST_NAME_KANA" + setSeq].disabled = "disabled";
            document.forms[0]["KYOUDAI_FIRST_NAME_KANA" + setSeq].style.backgroundColor = 'silver';
        }
    }
    //給付額のマスタのチェック処理実行
    getKyuhuMasterGk(setSeq);
    //貸与限度額のチェック処理実行
    getTaiyoMasterGk();
}

//給付額のマスタ登録チェック
function getKyuhuMasterGk(setSeq)
{
    var shotokuwaridiv = document.getElementsByName("SHOTOKUWARI_DIV" + setSeq);
    var kyuhu_shotokuwaridiv_value = "";
    //所得割区分の値を取得
    for(var i = 0; i < shotokuwaridiv.length; i++) {
        if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '1') {
            kyuhu_shotokuwaridiv_value = "1";
        } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '2') {
            kyuhu_shotokuwaridiv_value = "2";
        } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '3') {
            kyuhu_shotokuwaridiv_value = "3";
        }
    }
    var kyuhu_flg = "false";
    var cnt = document.forms[0]["KYUHU_GET_COUNT" + setSeq].value;
    if (cnt > 0) {
        for (var i = 0; i < cnt; i++) {
            var KYUHU_GET_SHOTOKUWARI_DIV = "KYUHU_GET_SHOTOKUWARI_DIV" + setSeq + i;
            var KYUHU_GET_KYUHU_GK = "KYUHU_GET_KYUHU_GK" + setSeq + i;
            var KYUHU_YOTEI_GK = "KYUHU_YOTEI_GK" + setSeq;
            //給付額のマスタの値を取得
            //給付額
            if (document.forms[0][KYUHU_GET_SHOTOKUWARI_DIV].value == kyuhu_shotokuwaridiv_value) {
                if (document.forms[0][KYUHU_GET_KYUHU_GK].value) {
                    document.forms[0][KYUHU_YOTEI_GK].value = document.forms[0][KYUHU_GET_KYUHU_GK].value;
                    kyuhu_flg = "true";
                }
            }
            //取得できない場合
            if (i == cnt-1) {
                if (kyuhu_flg == "false") {
                    document.getElementById('MASTER_COMMENT' + setSeq).innerHTML = '<font color="red">　(マスタより奨学給付金が取得できません)</font>';
                }
            }
        }
    } else {
        document.getElementById('MASTER_COMMENT' + setSeq).innerHTML = '<font color="red">　(マスタより奨学給付金が取得できません)</font>';
    }
}

//貸与限度額チェック
function getTaiyoMasterGk()
{
    var setSeq = '';
    //申請日が入力されている処理状況３を特定する(※特定できない場合は１回目を採用)
    var maxSeq = document.forms[0].KYUHU_MAX_SEQ.value;
    for (var seq = 1; seq <= maxSeq; seq++) {
        var shinseiDate = document.getElementsByName("KYUHU_SHINSEI_DATE_" + seq);
        if (shinseiDate && shinseiDate.value) {
            if (seq != 1) {
                setSeq = '_' + seq;
            }
        }
    }

    var tsuugakudiv = document.getElementsByName("TSUUGAKU_DIV");
    var shotokuwaridiv = document.getElementsByName("SHOTOKUWARI_DIV" + setSeq);
    var heikyu_flg1 = document.forms[0]["HEIKYUU_SHOUGAKU_FLG1" + setSeq].checked;
    var tsuugakudiv_value = "";
    var taiyo_shotokuwaridiv_value = "";
    //通学区分の値を取得
    for(var i = 0; i < tsuugakudiv.length; i++) {
        if (tsuugakudiv[i].checked && tsuugakudiv[i].value == '1') {
            tsuugakudiv_value = "1";
        } else if (tsuugakudiv[i].checked && tsuugakudiv[i].value == '2') {
            tsuugakudiv_value = "2";
        }
    }
    //所得割区分の値を取得
    for(var i = 0; i < shotokuwaridiv.length; i++) {
        if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '1') {
            taiyo_shotokuwaridiv_value = "1";
        } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '2') {
            if (heikyu_flg1) {
                taiyo_shotokuwaridiv_value = "4";
            } else {
                taiyo_shotokuwaridiv_value = "2";
            }
        } else if (shotokuwaridiv[i].checked && shotokuwaridiv[i].value == '3') {
            if (heikyu_flg1) {
                taiyo_shotokuwaridiv_value = "5";
            } else {
                taiyo_shotokuwaridiv_value = "3";
            }
        }
    }
    var taiyo_flg = "false";
    var cnt = document.forms[0]["TAIYO_GET_COUNT" + setSeq].value;
    if (cnt > 0) {
        for (var i = 0; i < cnt; i++) {
            var TAIYO_GET_TSUUGAKU_DIV = "TAIYO_GET_TSUUGAKU_DIV" + setSeq + i;
            var TAIYO_GET_SHOTOKUWARI_DIV = "TAIYO_GET_SHOTOKUWARI_DIV" + setSeq + i;
            var TAIYO_GET_GENDO_GK = "TAIYO_GET_GENDO_GK" + setSeq + i;
            //貸与限度額, 給付額のマスタの値を取得
            //貸与限度額
            if (document.forms[0][TAIYO_GET_TSUUGAKU_DIV].value == tsuugakudiv_value && document.forms[0][TAIYO_GET_SHOTOKUWARI_DIV].value == taiyo_shotokuwaridiv_value) {
                if (document.forms[0][TAIYO_GET_GENDO_GK].value) {
                    document.getElementById('TAIYO_GENDO_GK').innerHTML = document.forms[0][TAIYO_GET_GENDO_GK].value;
                    taiyo_flg = "true";
                }
            }
            //取得できない場合
            if (i == cnt-1) {
                if (taiyo_flg == "false") {
                    document.getElementById('TAIYO_GENDO_GK').innerHTML = '<font color="red">マスタの貸与限度額が取得できません</font>';
                }
            }
        }
    } else {
        document.getElementById('TAIYO_GENDO_GK').innerHTML = '<font color="red">マスタの貸与限度額が取得できません</font>';
    }
}

//年齢チェック
function dateExtra(cmd, setSeq) {
    if (!setSeq) {
        return;
    }
    var shinseiG = "KYUHU_SHINSEI_G" + setSeq;
    var shinseiY = "KYUHU_SHINSEI_YY" + setSeq;
    var birthDay = "KYOUDAI_BIRTHDAY" + setSeq;
    var setDate = document.forms[0][shinseiG].value + document.forms[0][shinseiY].value + "0702";
    var dNengo = setDate.substr(0, 1);
    var dYear = setDate.substr(1, 2);
    var setBirthday = document.forms[0][birthDay].value;
    var bNengo = setBirthday.substr(0, 1);
    var bYear = setBirthday.substr(1, 2);
    if (document.forms[0][shinseiG].value == "" || document.forms[0][shinseiY].value == "") {
        alert('{rval MSG901}' + '\n申請年度が取得できません。申請年度を入力して下さい。');
        return false;
    }
    if ((dNengo == "5") && (dYear > 0)) {
        dYear = parseInt(dYear, 10) + 2018;
    } else if ((dNengo == "4") && (dYear > 0) && (dYear <= 31)) {
        dYear = parseInt(dYear, 10) + 1988;
    } else if ((dNengo == "3") && (dYear > 0) && (dYear <= 64)) {
        dYear = parseInt(dYear, 10) + 1925;
    } else if ((dNengo == "2") && (dYear > 0) && (dYear <= 15)) {
        dYear = parseInt(dYear, 10) + 1911;
    } else if ((dNengo == "1") && (dYear > 0) && (dYear <= 45)) {
        dYear = parseInt(dYear, 10) + 1867;
    } else {
        //dYear = 0;
        alert('{rval MSG901}' + '\n申請年度が取得できません。申請年度を入力して下さい。');
        return false;
    }
    if ((bNengo == "5") && (bYear > 0)) {
        bYear = parseInt(bYear, 10) + 2018;
    } else if ((bNengo == "4") && (bYear > 0) && (bYear <= 31)) {
        bYear = parseInt(bYear, 10) + 1988;
    } else if ((bNengo == "3") && (bYear > 0) && (bYear <= 64)) {
        bYear = parseInt(bYear, 10) + 1925;
    } else if ((bNengo == "2") && (bYear > 0) && (bYear <= 15)) {
        bYear = parseInt(bYear, 10) + 1911;
    } else if ((bNengo == "1") && (bYear > 0) && (bYear <= 45)) {
        bYear = parseInt(bYear, 10) + 1867;
    } else {
        if (document.forms[0][birthDay].value != "") {
            alert('{rval MSG901}' + '\n正しい生年月日を入力して下さい。');
	        document.forms[0][birthDay].value = "";
	        return false;
	        //bYear = 0;
        }
    }
    checkDate = dYear + setDate.substr(3);
    checkBirthday = bYear + setBirthday.substr(3);
    //第2子以降の申請開始日の有効期間
    startShinsei = (dYear - 23) + "0703";
    endShinsei = (dYear - 15) + "0401";
    if (cmd == "kyuhu_update"){
        if (checkBirthday < startShinsei || checkBirthday > endShinsei) {
            alert('{rval MSG901}' + '\n所得割区分の第2子以降は高校生以上で満15歳以上23歳未満以外は入力できません。');
            return false;
        }
    }
    var test = parseInt((parseInt(checkDate) - parseInt(checkBirthday) ) / 10000 );
    var targetNenrei = document.getElementById("NENREI_HYOUJI_ID" + setSeq);
    if (parseInt(test) < 0) {
        test = 0;
    }
    if (parseInt(test) < 10) {
        test = "&nbsp;" + test;
    }
    targetNenrei.innerHTML = test + "才";
}

function copyDefDataTo2nd() {
    //基本的には、下記cpList配列にIDを記載。ラジオだけは特別に処理を記載する事。
    var cpList = ["KYUHU_SHINSEI_G", "KYUHU_SHINSEI_YY", "KYUHU_H_SCHOOL_CD", "KYUHU_KATEI", "KYUHU_GRADE", "KYUHU_HR_CLASS", "KYUHU_ATTENDNO", "KYUHU_KAISUU", "HOGOSHA_CD", "HOGOSHA2_CD", "SHOTOKUWARI_DIV", "KYUHU_REMARK"];
    for (var idx = 0;idx < cpList.length;idx++) {
        if (typeof(document.forms[0][cpList[idx]]) != "undefined") {
            var chkObj = document.forms[0][cpList[idx]];
            var chkTyp = chkObj.tagName;
            //ラジオの物だけは特殊。個別に記載する事。
            if (cpList[idx] == 'SHOTOKUWARI_DIV') {
                if (!document.getElementById("SHOTOKUWARI_DIV_21").disabled && document.getElementById("SHOTOKUWARI_DIV1").checked) {
                    document.getElementById("SHOTOKUWARI_DIV_21").checked = document.getElementById("SHOTOKUWARI_DIV1").checked;
                }
                if (!document.getElementById("SHOTOKUWARI_DIV_22").disabled && document.getElementById("SHOTOKUWARI_DIV2").checked) {
                    document.getElementById("SHOTOKUWARI_DIV_22").checked = document.getElementById("SHOTOKUWARI_DIV2").checked;
                }
                if (!document.getElementById("SHOTOKUWARI_DIV_23").disabled && document.getElementById("SHOTOKUWARI_DIV3").checked) {
                    document.getElementById("SHOTOKUWARI_DIV_23").checked = document.getElementById("SHOTOKUWARI_DIV3").checked;
                }
            } else {
                if (chkObj.type == "checkbox") {
                    document.forms[0][cpList[idx]+"_2"].checked = chkObj.checked;
                } else {
                    if (chkTyp == "INPUT" || chkTyp == "SELECT" || chkTyp == "TEXTAREA") {
                        document.forms[0][cpList[idx]+"_2"].value = chkObj.value
                    }
                }
            }
        }
    }
}
