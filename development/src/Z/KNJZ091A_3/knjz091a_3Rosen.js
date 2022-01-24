var frame_name = parent; //メイン画面
var keiro_no; //路線番号

function btn_submit(cmd) {
    top.main_frame.closeit();
    top.main_frame.document.forms[0].cmd.value = '';
    top.main_frame.document.forms[0].submit();
    return false;
}
//エリアを選択したとき
function selectArea(cmd,targetId) {
    keiro_no = document.forms[0].KEIRO_NO.value;
    if (!keiro_no) {
        alert('路線番号が選択されていません');
        document.forms[0].AREA_SENTAKU.value = '';
        return false;
    }

    document.getElementById("rosen").innerHTML = '<select name="ROSEN_SELECT" class="eki_select_a" size="20"></select>'; //空のコンボを表示

    if (document.forms[0].AREA_SENTAKU.value != '') {
        knjAjax(cmd,targetId);
    }
}

//入力ボタンがクリックされたとき
//選択した値をメインフレームに入れる
function insertDate() {
    var rosen = document.forms[0].ROSEN_SELECT;
    keiro_no = document.forms[0].KEIRO_NO.value;

    if (!document.forms[0].KEIRO_NO.value) {
            alert('路線番号が選択されていません');
            return false;
    }

    //どれか入力があるところないところがあった場合は未入力があると判断
    if (!document.forms[0].AREA_SENTAKU.value) {
        alert('エリアが選択されていません');
        return false;
    }
    var rosenValue  = rosen.value;
    var rosenText   = rosen.options[rosen.selectedIndex].text;

    eval("frame_name.document.forms[0].ROSEN_"        + keiro_no + ".value = rosenText");
    eval("frame_name.document.forms[0].HIDDEN_ROSEN_" + keiro_no + ".value = rosenValue");
}
/************************** Ajax **************************/
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

    httpObj = createXmlHttp();
    httpObj.onreadystatechange = statusCheckSub;
    httpObj.open("GET","knjz091a_3index.php?" + sendData,true);  //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null);                                        //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function createXmlHttp(){
    if( document.all ){
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
    else if( document.implementation ){
        return new XMLHttpRequest();
    }
    else{
        return null;
    }
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
        var targetElement = document.getElementById(targetId);
        targetElement.innerHTML = httpObj.responseText;
    }
}
/************************** Ajax ***********************************/
