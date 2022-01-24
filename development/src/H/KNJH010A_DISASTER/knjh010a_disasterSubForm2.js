var frame_name = top.main_frame.right_frame; //メイン画面
var keiro_no; //経路番号
var main_josya;  //メインフレームの乗車駅
var main_rosen;  //メインフレームの路線
var main_gesya;  //メインフレームの下車駅
var area_cd;
var rr_cd;
var tugaku_flg = '1';

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
    eval("main_flg = frame_name.document.forms[0].FLG_" + keiro_no + ".value"); //すでに登録されているFLGを取得1:電車通学 2:その他通学 4:バス通学
    div_tag = document.getElementsByTagName('div');

    if (main_flg == "2") { //メインフレームの通学手段がその他だったら
        document.forms[0].TUGAKU[1].checked = true;
        display = "none";
        disable = false;
    } else if (main_flg == "4") { //メインフレームの通学手段がバス通学だったら
        document.forms[0].TUGAKU[2].checked = true;
        display = "none";
        disable = false;
    } else {
        document.forms[0].TUGAKU[0].checked = true;
        display = "block";
        disable = true;
    }

    for (j = 0; j < div_tag.length; j++) {
        if (div_tag[j].className == 'train_only') { //divタグのクラスが'train_only'のものを抽出
            div_tag[j].style.display = display;
        }
    }
    document.forms[0].ROSEN_TEXT.disabled = disable; //テキストボックスを入力不可にする
    document.forms[0].JOSYA_TEXT.disabled = disable; //テキストボックスを入力不可にする
    document.forms[0].GESYA_TEXT.disabled = disable; //テキストボックスを入力不可にする

    reSelect(main_flg);
}

//エリアを選択したとき
function selectArea(cmd,targetId) {
    keiro_no = document.forms[0].KEIRO_NO.value;
    if (!keiro_no) {
        alert('経路番号が選択されていません');
        document.forms[0].AREA_SENTAKU.value = '';
        return false;
    }

    document.forms[0].ROSEN_TEXT.value = ''; //テキストボックスの中身を空にする(路線)
    document.forms[0].JOSYA_TEXT.value = ''; //テキストボックスの中身を空にする(乗車駅)
    document.forms[0].GESYA_TEXT.value = ''; //テキストボックスの中身を空にする(下車駅)
    document.getElementById("rosen").innerHTML = '<select name="ROSEN_SELECT" class="eki_select_a" size="20"></select>'; //空のコンボを表示
    document.getElementById("josya").innerHTML = '<select name="JOSYA_SELECT" class="eki_select_b" size="20"></select>'; //空のコンボを表示
    document.getElementById("gesya").innerHTML = '<select name="GESYA_SELECT" class="eki_select_b" size="20"></select>'; //空のコンボを表示

    if (document.forms[0].AREA_SENTAKU.value != '') {
        knjAjax(cmd,targetId);
    }
}

//自動で電車の項目を選択する必要があるときはこの関数を使う
function reSelect(main_flg) {
    if (main_flg == "1") { //メインフレームのhiddenにある通学方法が電車通学だったらhiddenのコードを元にDBから値をとってくる
        eval("main_josya = frame_name.document.forms[0].HIDDEN_JOSYA_" + keiro_no + ".value");
        eval("main_rosen = frame_name.document.forms[0].HIDDEN_ROSEN_" + keiro_no + ".value");
        eval("main_gesya = frame_name.document.forms[0].HIDDEN_GESYA_" + keiro_no + ".value");

        if (!main_josya || !main_rosen || !main_gesya) {
            return false;
        }

        area_cd = main_rosen.substr(2,1);
        rr_cd = main_rosen.substr(0,2);

        if (area_cd == '0') {
            if ('21' <= rr_cd && rr_cd <= '29') {
                area_cd = '3';
            } else if (rr_cd == '30') {
                area_cd = '5';
            } else if ('31' <= rr_cd && rr_cd <= '35') {
                area_cd = '6';
            } else if (rr_cd == '36') {
                area_cd = '9';
            } else {
                alert('対応していないコードです。');
                return false;
            }
        }

        document.forms[0].AREA_SENTAKU.value = area_cd;
        selectArea("get_rosen_from_keiro","rosen");
    } else if (main_flg == "2" || main_flg == "4") { //メインフレームのhiddenにある通学方法がその他通学orバス通学だったらメインフレームのテキストボックスの中身をそのまま表示する
        eval("document.forms[0].JOSYA_TEXT.value = frame_name.document.forms[0].JOSYA_" + keiro_no + ".value");
        eval("document.forms[0].ROSEN_TEXT.value = frame_name.document.forms[0].ROSEN_" + keiro_no + ".value");
        eval("document.forms[0].GESYA_TEXT.value = frame_name.document.forms[0].GESYA_" + keiro_no + ".value");
    } else { //まだ選択されていなかった時
        //選択したコンボのindex番号を取得
        var josyaIndex = document.forms[0].JOSYA_SELECT.selectedIndex;
        var rosenIndex = document.forms[0].ROSEN_SELECT.selectedIndex;
        var gesyaIndex = document.forms[0].GESYA_SELECT.selectedIndex;
        if (document.forms[0].JOSYA_SELECT.length != 0) {
            document.forms[0].JOSYA_TEXT.value = document.forms[0].JOSYA_SELECT.options[josyaIndex].text;
        }
        if (document.forms[0].ROSEN_SELECT.length != 0) {
            document.forms[0].ROSEN_TEXT.value = document.forms[0].ROSEN_SELECT.options[rosenIndex].text;
        }
        if (document.forms[0].GESYA_SELECT.length != 0) {
            document.forms[0].GESYA_TEXT.value = document.forms[0].GESYA_SELECT.options[gesyaIndex].text;
        }
    }
}

//通学手段をクリックしたとき
function tugakuChange() {
    var tugaku = document.forms[0].TUGAKU;
    var main_flg;
    keiro_no = document.forms[0].KEIRO_NO.value;
    if (!keiro_no) {
        alert('経路番号が選択されていません');
        return false;
    }
    eval("main_flg = frame_name.document.forms[0].FLG_" + keiro_no + ".value"); //すでに登録されているFLGを取得1:電車通学 2:その他通学 4:バス通学
    div_tag = document.getElementsByTagName('div');

    for (i = 0; i < tugaku.length; i++) {
        if (tugaku[i].checked) {
            if (tugaku[i].value == '1') { //通学手段の電車通学をクリックした時
                tugaku_flg = '1';
                for (j = 0; j < div_tag.length; j++) {
                    if (div_tag[j].className == 'train_only') { //divタグのクラスが'train_only'のものを抽出
                        div_tag[j].style.display = "block";
                        document.forms[0].ROSEN_TEXT.value = '';
                        document.forms[0].JOSYA_TEXT.value = '';
                        document.forms[0].GESYA_TEXT.value = '';
                        document.forms[0].ROSEN_TEXT.disabled = true;
                        document.forms[0].JOSYA_TEXT.disabled = true;
                        document.forms[0].GESYA_TEXT.disabled = true;
                        document.forms[0].AREA_SENTAKU.value = '';
                        document.getElementById("rosen").innerHTML = '<select name="ROSEN_SELECT" class="eki_select" size="20"></select>'; //空のコンボを表示
                        document.getElementById("josya").innerHTML = '<select name="JOSYA_SELECT" class="eki_select" size="20"></select>'; //空のコンボを表示
                        document.getElementById("gesya").innerHTML = '<select name="GESYA_SELECT" class="eki_select" size="20"></select>'; //空のコンボを表示
                    }
                }
                if (tugaku_flg == main_flg) {
                    selectKeiro();
                }
            } else if (tugaku[i].value == '2') { //通学手段のその他通学をクリックした時
                tugaku_flg = '2';
                for (j = 0; j < div_tag.length; j++) {
                    if (div_tag[j].className == 'train_only') { //divタグのクラスが'train_only'のものを抽出
                        div_tag[j].style.display = "none";
                        if (tugaku_flg == main_flg) {
                            eval("document.forms[0].ROSEN_TEXT.value = frame_name.document.forms[0].ROSEN_" + keiro_no + ".value");
                            eval("document.forms[0].JOSYA_TEXT.value = frame_name.document.forms[0].JOSYA_" + keiro_no + ".value");
                            eval("document.forms[0].GESYA_TEXT.value = frame_name.document.forms[0].GESYA_" + keiro_no + ".value");
                        } else {
                            document.forms[0].ROSEN_TEXT.value = '';
                            document.forms[0].JOSYA_TEXT.value = '';
                            document.forms[0].GESYA_TEXT.value = '';
                        }
                        document.forms[0].ROSEN_TEXT.disabled = false;
                        document.forms[0].JOSYA_TEXT.disabled = false;
                        document.forms[0].GESYA_TEXT.disabled = false;
                    }
                }
            } else { //通学手段のバス通学をクリックした時
                tugaku_flg = '4';
                for (j = 0; j < div_tag.length; j++) {
                    if (div_tag[j].className == 'train_only') { //divタグのクラスが'train_only'のものを抽出
                        div_tag[j].style.display = "none";
                        if (tugaku_flg == main_flg) {
                            eval("document.forms[0].ROSEN_TEXT.value = frame_name.document.forms[0].ROSEN_" + keiro_no + ".value");
                            eval("document.forms[0].JOSYA_TEXT.value = frame_name.document.forms[0].JOSYA_" + keiro_no + ".value");
                            eval("document.forms[0].GESYA_TEXT.value = frame_name.document.forms[0].GESYA_" + keiro_no + ".value");
                        } else {
                            document.forms[0].ROSEN_TEXT.value = '';
                            document.forms[0].JOSYA_TEXT.value = '';
                            document.forms[0].GESYA_TEXT.value = '';
                        }
                        document.forms[0].ROSEN_TEXT.disabled = false;
                        document.forms[0].JOSYA_TEXT.disabled = false;
                        document.forms[0].GESYA_TEXT.disabled = false;
                    }
                }
            }
        }
    }
}

//路線を選択したとき
function selectRosen(cmd) {
    document.forms[0].JOSYA_TEXT.value = ''; //乗車駅のテキストボックスを空にする
    document.forms[0].GESYA_TEXT.value = ''; //下車駅のテキストボックスを空にする

    //選択した路線のindex番号を取得
    var index = document.forms[0].ROSEN_SELECT.selectedIndex;

    //選択した路線の表示テキストを路線のテキストボックスに表示する
    document.forms[0].ROSEN_TEXT.value = document.forms[0].ROSEN_SELECT.options[index].text;

    knjAjax(cmd,"station");
}

//乗車駅・下車駅を選択したとき
function selectStation(kind,obj) {
    var index = obj.selectedIndex;
    if (kind == 'josya') {
        document.forms[0].JOSYA_TEXT.value = obj.options[index].text;
    } else {
        document.forms[0].GESYA_TEXT.value = obj.options[index].text;
    }
}

//入力ボタンがクリックされたとき
//選択した値をメインフレームに入れる
function insertDate() {
    var tugaku = document.forms[0].TUGAKU;
    for (i = 0; i < tugaku.length; i++) {
        if (tugaku[i].checked) {
            if (tugaku[i].value == '1') { //通学手段の電車通学をクリックした時
                tugaku_flg = '1';
            } else if (tugaku[i].value == '2') {
                tugaku_flg = '2';
            } else {
                tugaku_flg = '4';
            }
        }
    }

    var rosen = document.forms[0].ROSEN_SELECT;
    var josya = document.forms[0].JOSYA_SELECT;
    var gesya = document.forms[0].GESYA_SELECT;

    if (!document.forms[0].KEIRO_NO.value) {
            alert('経路番号が選択されていません');
            return false;
    }

    if (document.forms[0].ROSEN_TEXT.value == "" &&
        document.forms[0].JOSYA_TEXT.value == "" &&
        document.forms[0].GESYA_TEXT.value == ""
    ) { //テキストボックスが全てからだったらクリアする。
        eval("frame_name.document.forms[0].ROSEN_" + keiro_no + ".value = ''");
        eval("frame_name.document.forms[0].JOSYA_" + keiro_no + ".value = ''");
        eval("frame_name.document.forms[0].GESYA_" + keiro_no + ".value = ''");
        eval("frame_name.document.forms[0].FLG_"   + keiro_no + ".value = ''");
    } else { //どれか入力があるところないところがあった場合は未入力があると判断
        if (tugaku_flg == '1') {
            if (!document.forms[0].AREA_SENTAKU.value) {
                alert('エリアが選択されていません');
                return false;
            }
        }

        if (!document.forms[0].ROSEN_TEXT.value) {
                alert('路線が選択されていません');
                return false;
        }

        if (document.forms[0].ROSEN_TEXT.value && !document.forms[0].JOSYA_TEXT.value && !document.forms[0].GESYA_TEXT.value) {
                alert('乗車駅または下車駅が選択されていません');
                return false;
        }

        if (tugaku_flg == '2' || tugaku_flg == '4') { //通学手段がその他通学orバス通学だったら文字数チェック(15文字まで)
            if (15 < document.forms[0].ROSEN_TEXT.value.length) {
                alert('15文字までです');
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
        }

        if (tugaku_flg == "1") {
            var rosenValue  = rosen.value;
            var rosenText   = rosen.options[rosen.selectedIndex].text;

            var josyaValue  = josya.value;
            var josyaText   = josya.options[josya.selectedIndex].text;

            var gesyaValue  = gesya.value;
            var gesyaText   = gesya.options[gesya.selectedIndex].text;

            eval("frame_name.document.forms[0].ROSEN_"        + keiro_no + ".value = rosenText");
            eval("frame_name.document.forms[0].JOSYA_"        + keiro_no + ".value = josyaText");
            eval("frame_name.document.forms[0].GESYA_"        + keiro_no + ".value = gesyaText");
            eval("frame_name.document.forms[0].HIDDEN_ROSEN_" + keiro_no + ".value = rosenValue");
            eval("frame_name.document.forms[0].HIDDEN_JOSYA_" + keiro_no + ".value = josyaValue");
            eval("frame_name.document.forms[0].HIDDEN_GESYA_" + keiro_no + ".value = gesyaValue");
            eval("frame_name.document.forms[0].FLG_"          + keiro_no + ".value = tugaku_flg");
        } else {
            eval("frame_name.document.forms[0].ROSEN_" + keiro_no + ".value = document.forms[0].ROSEN_TEXT.value");
            eval("frame_name.document.forms[0].JOSYA_" + keiro_no + ".value = document.forms[0].JOSYA_TEXT.value");
            eval("frame_name.document.forms[0].GESYA_" + keiro_no + ".value = document.forms[0].GESYA_TEXT.value");
            eval("frame_name.document.forms[0].FLG_"   + keiro_no + ".value = tugaku_flg");
        }
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

    //経路番号を選択することによってここを通る
    if (cmd == "get_rosen_from_keiro") {
        document.forms[0].ROSEN_SELECT.value = main_rosen;
        selectRosen("get_station_from_keiro");
    }
    if (cmd == "get_station_from_keiro") {
        document.forms[0].JOSYA_SELECT.value = main_josya;
        document.forms[0].GESYA_SELECT.value = main_gesya;

        var index = document.forms[0].JOSYA_SELECT.selectedIndex;
        document.forms[0].JOSYA_TEXT.value = document.forms[0].JOSYA_SELECT.options[index].text;

        var index = document.forms[0].GESYA_SELECT.selectedIndex;
        document.forms[0].GESYA_TEXT.value = document.forms[0].JOSYA_SELECT.options[index].text;
    }
}
/************************** Ajax ***********************************/
