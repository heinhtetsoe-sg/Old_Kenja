var frame_name = top.main_frame.right_frame; //メイン画面
var keiro_no; //経路番号
var main_josya;  //メインフレームの乗車駅
var main_rosen;  //メインフレームの路線
var main_gesya;  //メインフレームの下車駅
var area_cd;
var rr_cd;
var tugaku_flg = '3';

window.onload = selectKeiro;

function btn_submit(cmd) {
    top.main_frame.closeit();
    top.main_frame.document.forms[0].cmd.value = '';
    top.main_frame.document.forms[0].submit();
    return false;
}

//経路番号を選択したとき
function selectKeiro() {
    var main_flg;
    keiro_no = document.forms[0].KEIRO_NO.value;
    eval("main_flg = frame_name.document.forms[0].FLG_" + keiro_no + ".value"); //すでに登録されているFLGを取得1:電車通学 2:その他通学 3.スクールバス

    document.forms[0].ROSEN_TEXT.disabled = true;   //テキストボックスを入力不可にする
    document.forms[0].JOSYA_TEXT.disabled = false;  //テキストボックスを入力不可にする
    document.forms[0].GESYA_TEXT.disabled = false;  //テキストボックスを入力不可にする

    reSelect(main_flg);
}

//自動で電車の項目を選択する必要があるときはこの関数を使う
function reSelect(main_flg) {
    eval("main_josya = frame_name.document.forms[0].HIDDEN_JOSYA_" + keiro_no + ".value");
    eval("main_rosen = frame_name.document.forms[0].HIDDEN_ROSEN_" + keiro_no + ".value");
    eval("main_gesya = frame_name.document.forms[0].HIDDEN_GESYA_" + keiro_no + ".value");
}

//路線を選択したとき
function selectRosen() {
    //選択した路線のindex番号を取得
    var index = document.forms[0].ROSEN_SELECT.selectedIndex;
    //選択した路線の表示テキストを路線のテキストボックスに表示する
    document.forms[0].ROSEN_TEXT.value = document.forms[0].ROSEN_SELECT.options[index].text;
}

//入力ボタンがクリックされたとき
//選択した値をメインフレームに入れる
function insertDate() {
    tugaku_flg = '3';

    var rosen = document.forms[0].ROSEN_SELECT;

    if (document.forms[0].ROSEN_TEXT.value == "" &&
        document.forms[0].JOSYA_TEXT.value == "" &&
        document.forms[0].GESYA_TEXT.value == ""
    ) { //テキストボックスが全てからだったらクリアする。
        eval("frame_name.document.forms[0].ROSEN_" + keiro_no + ".value = ''");
        eval("frame_name.document.forms[0].JOSYA_" + keiro_no + ".value = ''");
        eval("frame_name.document.forms[0].GESYA_" + keiro_no + ".value = ''");
        eval("frame_name.document.forms[0].FLG_"   + keiro_no + ".value = ''");
    } else { //どれか入力があるところないところがあった場合は未入力があると判断
        if (!document.forms[0].ROSEN_TEXT.value) {
            alert('路線が選択されていません');
            return false;
        }
        if (!document.forms[0].JOSYA_TEXT.value) {
            alert('乗車駅が選択されていません');
            return false;
        }

        if (!document.forms[0].GESYA_TEXT.value) {
            alert('降車駅が選択されていません');
            return false;
        }
        if (15 < document.forms[0].JOSYA_TEXT.value.length) {
            alert('15文字までです');
            return false;
        }
        if (15 < document.forms[0].GESYA_TEXT.value.length) {
            alert('15文字までです');
            return false;
        }

        var rosenValue  = rosen.value;
        var rosenText   = rosen.options[rosen.selectedIndex].text;

        eval("frame_name.document.forms[0].ROSEN_"        + keiro_no + ".value = rosenText");
        eval("frame_name.document.forms[0].HIDDEN_ROSEN_" + keiro_no + ".value = rosenValue");
        eval("frame_name.document.forms[0].JOSYA_"        + keiro_no + ".value = document.forms[0].JOSYA_TEXT.value");
        eval("frame_name.document.forms[0].GESYA_"        + keiro_no + ".value = document.forms[0].GESYA_TEXT.value");
        eval("frame_name.document.forms[0].FLG_"          + keiro_no + ".value = tugaku_flg");

    }
}


/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax(cmd,targetId) { //この区間は送信処理、
    var sendData = '';
    var seq = '';
    var form_datas = document.forms[0];
    form_datas.cmd.value = cmd;
    for (var i = 0; i < form_datas.length; i++) {
        sendData += seq;
        sendData += form_datas[i].name + "=" + form_datas[i].value;
        seq = '&';
    }

    statusCheckSub = function () {statusCheck(cmd,targetId);} //引数を使いたいので関数変数(?)を使う

    httpObj = new ActiveXObject("Microsoft.XMLHTTP");
    httpObj.onreadystatechange = statusCheckSub;
    httpObj.open("GET","knjh010a_disasterindex.php?" + sendData,true);  //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null);                                        //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck(cmd,targetId) { //サーバからの応答をチェック
    /******** httpObj.readyState *******/  /********** httpObj.status *********/
    /*  0:初期化されていない           */  /*  200:OK                         */
    /*  1:読込み中                     */  /*  403:アクセス拒否               */
    /*  2:読込み完了                   */  /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */  /***********************************/
    /*  4:準備完了                     */
    /***********************************/
    if ((httpObj.readyState == 4) && (httpObj.status == 200)) {
        displayData(cmd,targetId); //サーバで無事処理が終了したらこの関数が呼ばれる
    }
}
function displayData(cmd,targetId) { //サーバーで処理が終わったらこの部分を実行
    if (httpObj.responseText != '') {
        if (targetId == "station") {
            var response = httpObj.responseText;
            var responseArray = response.split("::");
            var targetElement1 = document.getElementById("josya");
            var targetElement2 = document.getElementById("gesya");

            targetElement1.innerHTML = responseArray[0];
            targetElement2.innerHTML = responseArray[1];
        } else {
            var targetElement = document.getElementById(targetId);
            targetElement.innerHTML = httpObj.responseText;
        }
    }
}
/************************** Ajax ***********************************/
