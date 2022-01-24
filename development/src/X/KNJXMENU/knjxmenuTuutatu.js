//漢字
function btn_submit(cmd)
{

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

var xmlhttp = null;

/* POSTによるデータ送信 */
function getUpd() {

    /* XMLHttpRequestオブジェクト作成 */
    if( xmlhttp == null ) {
        xmlhttp = createXmlHttp();
    } else{
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }

    /* 入力フォームデータの処理 */
    var postdata = new String();

    postdata += "&cmd=tuutatuUpd";
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = handleHttpEvent;
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "index.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);

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

/* レスポンスデータ処理 */
function handleHttpEvent() {
    if( xmlhttp.readyState == 4 ) {
        if( xmlhttp.status == 200 ) {
            var txt = xmlhttp.responseText;
            top.right_frame.closeit();
        }
    }

}

