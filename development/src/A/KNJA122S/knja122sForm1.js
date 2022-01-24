function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function notPemClose() {
    alert('証明書ファイルがありません。');
    deleteCookie();
    closeWin();
}

function btn_signSubmit(cmd, randm, staff) {

    if (!document.forms[0].PASSWD.value) {
        alert('パスワードを入力して下さい。');
        return false;
    }
	//2010.02.10
    document.forms[0].GOSIGN.value="gosign";
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//HTTPSで自身を呼び出す
function collHttps(requestRoot, cmd) {
    //現在のURL
    urlVal = document.URL;
    //HTTPをHTTPSにする
    setUrl = urlVal.replace("http:", "https:");
    parent.location = setUrl + "&cmd=" + cmd + "&setUrl=" + setUrl;
    window.close();
}

//クッキーの削除
function deleteCookie() {
    cName = "I9e58bUn";   // 削除するクッキー名(証明書)
    cPass = "hoj3RG8t";   // 削除するクッキー名(パスワード)
    dTime = new Date();
    dTime.setYear(dTime.getYear() - 1);
    document.cookie = cName + "=;expires=" + dTime.toGMTString() + ";path=/";
    document.cookie = cPass + "=;expires=" + dTime.toGMTString() + ";path=/";
}

//デバック用 key：クッキー名
function ReadCookie(key) {
     var sCookie = document.cookie;   // Cookie文字列
     var aData = sCookie.split(";");  // ";"で区切って"キー=値"の配列にする
     var oExp = new RegExp(" ", "g"); // すべての半角スペースを表す正規表現
     key = key.replace(oExp, "");     // 引数keyから半角スペースを除去

     var i = 0;
     while (aData[i]) {                           /* 語句ごとの処理 : マッチする要素を探す */
          var aWord = aData[i].split("=");        // さらに"="で区切る
          aWord[0] = aWord[0].replace(oExp, "");  // 半角スペース除去
          if (key == aWord[0]) return unescape(aWord[1]); // マッチしたら値を返す
          if (++i >= aData.length) break;         // 要素数を超えたら抜ける
     }
     return "aa";                                 // 見つからない時は空文字を返す
}
//2010.02.10
//Appletから呼出されて、署名を格納。
function recvValue(signature, result) {
    document.forms[0].SIGNATURE.value=signature;
    document.forms[0].GOSIGN.value=result;
    document.forms[0].cmd.value = 'sslExe';
    document.forms[0].submit();
}
//Appletから呼出されて、指定INPUT値を戻す
function getvalue(name) {
     return document.getElementsByName(name)[0].value;
}
