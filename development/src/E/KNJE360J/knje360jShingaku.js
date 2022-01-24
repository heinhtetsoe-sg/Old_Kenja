function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }

    if (cmd == "shingaku_clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    //進路相談ボタン押し下げ時
    if (cmd == "shinroSoudan") {
        loadwindow("knje360jindex.php?cmd=shinroSoudan&TYPE=btn", 0, 0, 760, 680);
        return true;
    }

    if (cmd == "shingaku_update" && document.forms[0].SEQ.value == "") {
        alert("{rval MSG308}");
        return true;
    }

    if (cmd == "shingaku_insert" || cmd == "shingaku_update") {
        if (document.forms[0].FINSCHOOLCD.value == "") {
            alert("{rval MSG304}\n　　（学校コード）");
            return true;
        }
        if (document.forms[0].TOROKU_DATE.value == "") {
            alert("データを入力してください。\n　　（登録日）");
            return true;
        }

        var date = document.forms[0].TOROKU_DATE.value.split("/");
        var sdate = document.forms[0].SDATE.value.split("/");
        var edate = document.forms[0].EDATE.value.split("/");
        sdate_show = document.forms[0].SDATE.value;
        edate_show = document.forms[0].EDATE.value;

        if (
            new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])) ||
            new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))
        ) {
            alert("登録日が入力範囲外です。\n（" + sdate_show + "～" + edate_show + "）");
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//メッセージ表示
function showMsg(obj) {
    var org = document.forms[0].ORIGINAL_ISSUE.value;
    if (org == "" && obj.checked) {
        if (!confirm("選択しますか。\n\n　＊取消は証明書交付で削除して下さい。")) {
            obj.checked = false;
        }
    }
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    nextURL = "";

    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
        var search = parent.left_frame.document.links[i].search;
        //searchの中身を&で分割し配列にする。
        arr = search.split("&");

        //学籍番号が一致
        if (arr[1] == "SCHREGNO=" + schregno) {
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length - 1) {
                idx = 0; //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i + 1; //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length - 1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i - 1; //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href.replace("edit", "shingaku"); //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = "shingaku_insert";
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == "0") {
            //クッキー削除
            deleteCookie("nextURL");
            document.location.replace(nextURL);
            alert("{rval MSG201}");
        } else if (cd == "1") {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}

//学校検索画面
function Page_jumper(link) {
    //学校検索画面を開く
    link = link + "/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number=";
    loadwindow(
        link,
        event.clientX +
            (function () {
                var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                return scrollX;
            })(),
        event.clientY +
            (function () {
                var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                return scrollY;
            })(),
        650,
        600
    );

    //学校検索イベント
    collegeSelectEvent();
}

//学校検索イベント
function collegeSelectEvent() {
    knjAjax("shingaku_college");
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax(cmd) {
    //この区間は送信処理、
    var sendData = "";
    var seq = "";
    var form_datas = document.forms[0];

    sendData = "cmd=" + cmd;
    sendData += "&SCHREGNO=" + form_datas.SCHREGNO.value;
    sendData += "&SEQ=" + form_datas.SEQ.value;
    sendData += "&FINSCHOOLCD=" + form_datas.FINSCHOOLCD.value;
    sendData += "&STAT_DATE1=";
    sendData += "&STAT_DATE3=";

    httpObj = new ActiveXObject("Microsoft.XMLHTTP");
    httpObj.onreadystatechange = statusCheck;
    httpObj.open("GET", "knje360jindex.php?" + sendData, true); //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null); //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck() {
    //サーバからの応答をチェック
    /******** httpObj.readyState *******/ /********** httpObj.status *********/
    /*  0:初期化されていない           */ /*  200:OK                         */
    /*  1:読込み中                     */ /*  403:アクセス拒否               */
    /*  2:読込み完了                   */ /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */ /***********************************/
    /*  4:準備完了                     */
    /***********************************/
    if (httpObj.readyState == 4 && httpObj.status == 200) {
        var targetSTAT_DATE1 = document.getElementById("STAT_DATE1");
        var targetSTAT_DATE3 = document.getElementById("STAT_DATE3");

        var response = httpObj.responseText;
        var responseArray = response.split("::");

        targetSTAT_DATE1.innerHTML = responseArray[0];
        targetSTAT_DATE3.innerHTML = responseArray[1];
    }
}
/************************** Ajax ***********************************/

//学校検索イベント（募集区分等）
function collegeSelectEvent2() {
    knjAjax2("shingaku_college");
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax2(cmd) {
    //この区間は送信処理、
    var sendData = "";
    var seq = "";
    var form_datas = document.forms[0];

    sendData = "cmd=" + cmd;
    sendData += "&SCHREGNO=" + form_datas.SCHREGNO.value;
    sendData += "&SEQ=" + form_datas.SEQ.value;
    sendData += "&FINSCHOOLCD=" + form_datas.FINSCHOOLCD.value;
    sendData += "&STAT_DATE1=" + form_datas.STAT_DATE1.value;
    sendData += "&STAT_DATE3=" + form_datas.STAT_DATE3.value;

    httpObj = new ActiveXObject("Microsoft.XMLHTTP");
    httpObj.onreadystatechange = statusCheck2;
    httpObj.open("GET", "knje360jindex.php?" + sendData, true); //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null); //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck2() {
    //サーバからの応答をチェック
    /******** httpObj.readyState *******/ /********** httpObj.status *********/
    /*  0:初期化されていない           */ /*  200:OK                         */
    /*  1:読込み中                     */ /*  403:アクセス拒否               */
    /*  2:読込み完了                   */ /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */ /***********************************/
    /*  4:準備完了                     */
    /***********************************/

    if (httpObj.readyState == 4 && httpObj.status == 200) {
        var targetSTAT_DATE1 = document.getElementById("STAT_DATE1");
        var targetSTAT_DATE3 = document.getElementById("STAT_DATE3");
        var targetSCHOOL_NAME = document.getElementById("label_name");
        var targetDISTDIV_NAME = document.getElementById("RITSU_NAME_ID");
        var targetZIPCD = document.getElementById("ZIPCD");
        var targetADDR1 = document.getElementById("ADDR1");
        var targetADDR2 = document.getElementById("ADDR2");
        var targetTELNO = document.getElementById("TELNO");

        var response = httpObj.responseText;
        var responseArray = response.split("::");

        targetSTAT_DATE1.innerHTML = responseArray[0];
        targetSTAT_DATE3.innerHTML = responseArray[1];
        targetSCHOOL_NAME.innerHTML = responseArray[2];
        targetDISTDIV_NAME.innerHTML = responseArray[3];
        targetZIPCD.innerHTML = responseArray[4];
        targetADDR1.innerHTML = responseArray[5];
        targetADDR2.innerHTML = responseArray[6];
        targetTELNO.innerHTML = responseArray[7];
    }
}
/************************** Ajax ***********************************/

//学校検索イベント（確定）
function collegeSelectEvent3() {
    knjAjax3("shingaku_college");
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax3(cmd) {
    //この区間は送信処理、
    var sendData = "";
    var seq = "";
    var form_datas = document.forms[0];

    sendData = "cmd=" + cmd;
    sendData += "&SCHREGNO=" + form_datas.SCHREGNO.value;
    sendData += "&SEQ=" + form_datas.SEQ.value;
    sendData += "&FINSCHOOLCD=" + form_datas.FINSCHOOLCD.value;
    sendData += "&STAT_DATE1=";
    sendData += "&STAT_DATE3=";

    httpObj = new ActiveXObject("Microsoft.XMLHTTP");
    httpObj.onreadystatechange = statusCheck3;
    httpObj.open("GET", "knje360jindex.php?" + sendData, true); //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null); //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck3() {
    //サーバからの応答をチェック
    /******** httpObj.readyState *******/ /********** httpObj.status *********/
    /*  0:初期化されていない           */ /*  200:OK                         */
    /*  1:読込み中                     */ /*  403:アクセス拒否               */
    /*  2:読込み完了                   */ /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */ /***********************************/
    /*  4:準備完了                     */
    /***********************************/

    if (httpObj.readyState == 4 && httpObj.status == 200) {
        var targetSTAT_DATE1 = document.getElementById("STAT_DATE1");
        var targetSTAT_DATE3 = document.getElementById("STAT_DATE3");
        var targetSCHOOL_NAME = document.getElementById("label_name");
        var targetDISTDIV_NAME = document.getElementById("RITSU_NAME_ID");
        var targetZIPCD = document.getElementById("ZIPCD");
        var targetADDR1 = document.getElementById("ADDR1");
        var targetADDR2 = document.getElementById("ADDR2");
        var targetTELNO = document.getElementById("TELNO");
        var keta = "12" == document.forms[0].useFinschoolcdFieldSize.value ? 12 : 7;

        var response = httpObj.responseText;
        var responseArray = response.split("::");

        targetSTAT_DATE1.innerHTML = responseArray[0];
        targetSTAT_DATE3.innerHTML = responseArray[1];
        targetSCHOOL_NAME.innerHTML = responseArray[2];
        targetDISTDIV_NAME.innerHTML = responseArray[3];
        targetZIPCD.innerHTML = responseArray[4];
        targetADDR1.innerHTML = responseArray[5];
        targetADDR2.innerHTML = responseArray[6];
        targetTELNO.innerHTML = responseArray[7];

        //ゼロ埋め
        if (document.forms[0].FINSCHOOLCD.value != "") {
            document.forms[0].FINSCHOOLCD.value = ((keta == 12 ? "000000000000" : "0000000") + document.forms[0].FINSCHOOLCD.value).slice(-keta);
        }
    }
}

function current_cursor_list() {}

/************************** Ajax ***********************************/
