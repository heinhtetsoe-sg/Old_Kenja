function btn_submit(cmd) {
   
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }        
        
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            alert('{rval MSG203}');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset() {
   result = confirm('{rval MSG106}');
   if (result == false) {
       return false;
   }
}

/* XMLHttpRequest生成 */
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
var xmlhttp = null;
function SetBranch(obj){
    /* レスポンスデータ処理 */
    var handleHttpEvent = function (){
            debug(xmlhttp.readyState);
        if( xmlhttp.readyState == 4 ){
            var data = [];
            if( xmlhttp.status == 200 ){
                var resdata = xmlhttp.responseText;
                debug(resdata);
                if (resdata.length != ""){
                  //デコードとevalしてJavaScript化
                  eval('data='+ decodeURIComponent(resdata));
                }
                setList(document.forms[0].BRANCHCD, data);
            }
            /* 通信エラー表示
            else{
                window.alert("通信エラーが発生しました。");
            }
            */
        }
    }
    /* XMLHttpRequestオブジェクト作成 */
    if( xmlhttp == null ){
        xmlhttp = createXmlHttp();
    }
    else{
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }
    /* 入力フォームデータの処理 */
    var postdata = new String();
    postdata = "cmd=send";
    postdata += "&BANKCD="+obj.value;
    debug(postdata);
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = handleHttpEvent;
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp040kindex.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);

}
function setList(opt, data)
{
  opt.options.length = 0;
  //generating new options
  var j = 0;
  for (var i in data){
    opt.options[j] = new Option(data[i],i);
    j++;
  }
}
function debug(str){
//  document.getElementById("debug").innerHTML = str;
}
