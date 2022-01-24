<?php

require_once('for_php7.php');

//CSVファイル書出し＆取込共通クラス
/****************************************************************
1.ファイルをインクロードする
    //ファイルアップロードオブジェクト
    require_once("csvfilealp.php");

2.オブジェクトを生成
    //オブジェクト作成
    $objUp = new csvFileAlp();

3.ファイル名設定
    $objUp->setFileName("学年末成績処理.csv");

4.CSVを書き出すデータをレコード毎セットする
    $csv = array($row["SCHREGNO"],
                 $row["GRADINGCLASSCD"],
                 $row["VALUATION"],
                 $row["GET_CREDIT"],
                 $row["ADD_CREDIT"],
                 $row["REMARK"]);
             
    //書き出し用CSVデータ
    $objUp->addCsvValue($csv);

5.CSVを取り込むフォームの入力エリアの名前とキーをセットする
    $key = array("SCHREGNO"       =>$row["SCHREGNO"],
                 "GRADINGCLASSCD" =>$row["GRADINGCLASSCD"]);

    //入力エリアとキーをセットする
    $objUp->setElementsValue("VALUATION[]", "VALUATION", $key);
    引数１：入力エリア名
    引数２：取り込むCSVファイルの項目
    引数３：キー

--- added 2004/03/34----
5.5.エクセルを開いて数値として扱われて先頭のゼロが消えている値を取り込むときにゼロ埋めするかのフラグと桁数をセット 
    セットしなければ何もしない（ゼロ埋めしない）
    ※　配列のキー値はsetElementsValueの $key のキー値と同じにする       
	   $flg = array("SCHREGNO"       => array(true,8),
                    "GRADINGCLASSCD" => array(false,6)); ←これはゼロ埋めしない　　

       $objUp->setEmbed_flg($flg);      
    
6.ヘッダ、属性、サイズセット
    $objUp->setHeader(array("SCHREGNO","GRADINGCLASSCD","VALUATION","GET_CREDIT","ADD_CREDIT","REMARK"));
    $objUp->setType(array("S","S","N","N","N","S"));
    S:文字列
    N：数値
    $objUp->setSize(array(8,6,2,2,2,60));

7.HTML作成
    //CSVファイルアップロードコントロール
    $arg["FILE"] = $objUp->toFileHtml($objForm);

****************************************************************/
class csvFileAlp
{
    var $data;
    var $header;
    var $key;
    var $output;
    var $csv = array();
    var $filename = "undefine.csv";
    var $size;
    var $type;
    var $embed_flg;
    
    //ゼロ埋めフラグ
    function setEmbed_flg($f){
    	$this->embed_flg = $f;
    }
    
    //ヘッダ
    function setHeader($h){
        $this->header = $h;
    }
    //キー
    function setKey($k){
        $this->key = $k;
    }
    //サイズ入力
    function setSize($s){
        $this->size = $s;
    }
    //データタイプ
    function setType($t){
        $this->type = $t;
    }
    //書き出し用CSVデータ
    function addCsvValue($csv){
        $this->csv[] = $csv;
    }
    //ファイル名セット
    function setFileName($f){
        $this->filename = $f;
    }
    //入力エリアに値をセットする
    function setElementsValue($name, $header, $key){
        if (is_array($key)){
            $this->key = array_keys($key);
            $k = implode($key, ":");
            $this->output[$header] = $header;
            $this->data[$name][] = array($header, $k);
        }
    }
    //CSVファイルアップロードコントロール
    function toFileHtml(&$form)
    {
        global $sess;
        $session = new APP_Session($sess->id, 'csvFileAlp');
        $session->clear();
        if ($this->data){            
            $session->unregister("data");
            $session->unregister("header");
            $session->unregister("key");
            $session->unregister("output");
            $session->unregister("csv");
            $session->unregister("filename");
            $session->unregister("size");
            $session->unregister("type");
            
            $session->unregister("embed_flg");

            $session->register("data", $this->data);
            $session->register("header", $this->header);
            $session->register("key",$this->key);
            $session->register("output", array_values($this->output));
            $session->register("csv", $this->csv);
            $session->register("filename", $this->filename);
            $session->register("size", $this->size);
            $session->register("type", $this->type);
            
            $session->register("embed_flg",$this->embed_flg);

$js = <<<EOP
<script language="javascript">
    var id;
    function setData(){
        upiframe.document.forms[0].session.value = '{$sess->id}';
    }
    window.onload = setData;
    //インラインフレームが再描画
    function setFrameObj(){
        //取り込み
        if (document.forms[0].csv[0].checked && confirm("CSVデータを取込ますか？")){
            id = setInterval(checkReload, 500);
        }
    }
    var idx = 0;
    function checkReload(){
        if (upiframe.success){      //データ生成成功か？
            var msg = "CSVデータを取り込みました。";
            if (upiframe.err1){
                msg += "\\nエラー行："+upiframe.err1 +"行目";
            }
            if (upiframe.err2){
                msg += "\\n取込不可："+upiframe.err2 +"行目";;
            }
            upiframe.f = self;
            upiframe.setUpData();
            clearInterval(id);
            alert(msg);
        }else if (idx >= 3){        //反映タイムアウト
            clearInterval(id);
            //取り込み
            if (document.forms[0].csv[0].checked){
                alert("CSVデータを取込みに失敗しました。");
            }
        }else{
            idx++;
        }
    }
    function click_exec(){
        upiframe.f=self;
        upiframe.document.forms[0].CHKHEADER.checked = document.forms[0].CHKHEADER.checked;
        upiframe.btn_submit();
        setFrameObj();
    }
    function clickRadio(val){
        if (val == 1){
            document.forms[0].userfile.disabled =false;
            document.forms[0].btn_refer.disabled =false;
        }else{
            document.forms[0].userfile.disabled =true;
            document.forms[0].btn_refer.disabled =true;
        }
    }
</script>
EOP;
        }else{
            $disabled = "disabled";
        }
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => "userfile",
                        "size"        => 15,
                        "maxlength"   => 15,
                        "extrahtml"   => $disabled." onchange=\"this.value=upiframe.document.forms[0].userfile.value\"" ,
                        "value"       => ""));

        //参照ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_refer",
                        "value"       => "参照",
                        "extrahtml"   => $disabled ." onclick=\"upiframe.f=self;upiframe.document.forms[0].userfile.click();\"") );

        //実行ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_exec",
                        "value"       => "実行",
                        "extrahtml"   => $disabled ." onclick=\"click_exec()\"") );

        //ヘッダ有無
        $form->ae(array("type"      => "checkbox",
                         "name"     => "CHKHEADER",
                         "value"    => 1,
                        "checked"   => true,
                        "extrahtml"   => "id=\"chkheader\"") );

        $if = "<iframe name=\"upiframe\" style=\"display:none\" src=\"".REQUESTROOT ."/common/csvfilealp.php?cmd=upload\"></iframe>\n";
        $radio = "<input type=\"radio\" name=\"csv\" value=\"1\" id=\"csv1\" onclick=\"clickRadio(1)\"".$disabled." checked><label for=\"csv1\">取込</label>　";
        $radio .= "<input type=\"radio\" name=\"csv\" value=\"2\" id=\"csv2\" onclick=\"clickRadio(2)\"".$disabled."><label for=\"csv2\">書出</label>";
        return $if .$radio ."　ファイル：" .$form->ge("userfile") .$form->ge("btn_refer") .$form->ge("btn_exec") ."　".$form->ge("CHKHEADER") . "<label for=\"chkheader\">ヘッダ有り</label>" .$js;
    }
}
if ($_POST["session"]){
    $session = new APP_Session($_POST["session"], 'csvFileAlp');
    $sessData = $session->getData();
}

//ファイル取込
if ($_REQUEST["cmd"] == "upload"){
    if (is_uploaded_file($_FILES['userfile']['tmp_name']) && is_array($sessData)) {

        $fp = fopen ($_FILES['userfile']['tmp_name'], "r");  
        //ヘッダ有無
        if ($_POST["CHKHEADER"]){
            $buffer = fgets($fp, 4096);
            if (trim($buffer) != ''){
                $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                $tmp = common::csv2array($buffer);
            }
        }
        if ($tmp === $sessData["header"] && $_POST["CHKHEADER"] || !isset($_POST["CHKHEADER"])){
            //ヘッダチェック
            $row_err = $row_key = array();
            $j = ($_POST["CHKHEADER"])? 2 : 1;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != ''){
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp = common::csv2array($buffer);
                    $tmp = str_replace("'","\'", $tmp);
                    //キー作成
                    $k = $sp = "";
                    for ($i = 0; $i < get_count($sessData["key"]); $i++){
                        $arr = array_keys($sessData["header"], $sessData["key"][$i]);
                        
                        //ゼロ埋め
                        if ($sessData["embed_flg"][$sessData["key"][$i]][0]) {
                            $tmp[$arr[0]] = sprintf("%0".$sessData["embed_flg"][$sessData["key"][$i]][1]."d", $tmp[$arr[0]]);
                        }
                        
                        $k .= $sp .$tmp[$arr[0]];
                        $sp = ":";
                    }
                    
                    //項目数がヘッダと違う
                    if (get_count($tmp) != get_count($sessData["header"])){
                        $row_err[] = $j;
                        $j++;
                        continue;
                    }
                    for ($i = 0; $i < get_count($tmp); $i++){
                        if (in_array($sessData["header"][$i], $sessData["output"])){

                            //ゼロ埋め
                            if ($sessData["embed_flg"][$sessData["header"][$i]][0]) {
                              $tmp[$i] = sprintf("%0".$sessData["embed_flg"][$sessData["header"][$i]][1]."d", $tmp[$i]);
                            }

                            if (($sessData["type"][$i] == "S" && $sessData["size"][$i] >= strlen($tmp[$i])) ||
                                (($sessData["type"][$i] == "N" && is_numeric($tmp[$i]) || $tmp[$i] == ""))){
                                  	
                                	$data[$i][$k] = $tmp[$i];
                            }else{
                                $row_err[] = $j;
                                break;
                            }
                        }
                    }
                    $row_key[$k] = $j;
                }
                $j++;
            }
        }else{
            $alert = "CSVファイルのヘッダが不正です";
        }
        fclose($fp);
    }
?>
<html>
<head>
<title>CSVアップロード</title>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo CHARSET ?>">
<link rel="stylesheet" href="gk.css">
<script language="javascript">
var f;
var success = false;
var err1,err2;

function setUpData(){
<?php
    $row_alert = array();
    if (is_array($data) && is_array($sessData["data"])){
    	$reload = false;

        foreach($sessData["data"] as $field => $val){
        	foreach($val as $i => $v){
                $arr = array_keys($sessData["header"], $v[0]);
                if (!isset($data[$arr[0]][$v[1]])){
                     continue;
                }
                //取り込んだ行を削除
                if (isset($row_key[$v[1]])) unset($row_key[$v[1]]);
                $reload = true;
                if (get_count($val) == 1){
                    echo "    f.document.forms[0]['$field'].value = '" .$data[$arr[0]][$v[1]] ."'\n";
                    if (strlen($data[$arr[0]][$v[1]])){
                        echo "    f.document.forms[0]['$field'].checked = true\n";
                    } else {
                        echo "    f.document.forms[0]['$field'].checked = false\n";
                    }
                }else{
                    echo "    f.document.forms[0]['$field'][$i].value = '" .$data[$arr[0]][$v[1]] ."'\n";
                    if (strlen($data[$arr[0]][$v[1]])){
                        echo "    f.document.forms[0]['$field'][$i].checked = true\n";
                    } else {
                        echo "    f.document.forms[0]['$field'][$i].checked = false\n";
                    }
                }
                $success = true;
            }
        }
    }
?>
    f.document.forms[0].userfile.value = "";
}
<?php
    if ($alert) echo "alert('$alert');\n";
    if ($success)   echo "success = true\n";
    if (is_array($row_err)) echo "var err1 = '".implode($row_err, ',')."'\n";
    if (is_array($row_key)) echo "var err2 = '".implode($row_key, ',')."'\n";
?>
function btn_submit(){
    //取り込み
    if (f.document.forms[0].csv[0].checked){
        document.forms[0].cmd.value = "upload";
    }else if (f.document.forms[0].csv[1].checked){ //書き出し
        document.forms[0].cmd.value = "download";
    }
    document.forms[0].submit();
    return false;
}
function setFileName(){
    f.document.forms[0].userfile.value = document.forms[0].userfile.value;
}
</script>
</head>
<body>
<form enctype="multipart/form-data" action="csvfilealp.php" method="post">
<input name="userfile" type="file" onchange="setFileName()">
<input type="hidden"+ name="MAX_FILE_SIZE" value="409600">
<input type="hidden" name="cmd">
<input type="checkbox" name="CHKHEADER">
<input type="hidden" name="session" value='<?php echo $_REQUEST["session"] ?>' >
</body>
</html>
<?php
//ファイル書出し
}else if ($_REQUEST["cmd"] == "download"){
    if (is_array($sessData["csv"]) && is_array($sessData["header"])){
        $contents = "";
        if ($_POST["CHKHEADER"]){
            $contents = implode($sessData["header"], ",") ."\r\n";
        }
        foreach($sessData["csv"] as $csv){
            $sp = "";
            for($i = 0; $i < get_count($csv); $i++){
                if (strpos($csv[$i], ",")){
                    $contents .= $sp ."\"" .str_replace('"', '""', $csv[$i]) ."\"";
                }else{
                    $contents .= $sp .str_replace('"', '""', $csv[$i]);
                }
                $sp = ",";
            }
            $contents .= "\r\n";
        }
        
        $filename = $sessData["filename"];
        $filename = mb_convert_encoding($filename, "SJIS-win", mb_internal_encoding());
        $contents = mb_convert_encoding($contents, "SJIS-win", mb_internal_encoding());

        /* HTTPヘッダの出力 */
        mb_http_output("pass");
        header("Accept-Ranges: none");
        header("Content-Disposition: inline; filename=$filename");
        header("Content-Transfer-Encoding: binary");
        header("Content-Length: ". strlen($contents) );
        header("Content-Type: text/octet-stream");
//        header("Content-Type: application/octet-stream");

        echo $contents;

    }
}
?>
