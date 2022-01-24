<?php
class importXml {
    function htmlcreate(&$objForm,$db,&$model) {
        //パーツ作成
        $arg["parts"] = "";
        if($model->partsCnt > 0){
            //DB接続
            $db = Query::dbCheckOut();
            for($i=0;$i<$model->partsCnt;$i++){
                $val = array();
                $val = $model->parts[$i];
                
                if($val["TYPE"] == "text"){
                    //テキストボックス作成
                    $textBox = knjCreateTextBox($objForm, $model->field[$val["NAME"]], $val["NAME"], $val["SIZE"], $val["MAXLENGTH"], $val["EXTRA"]);
                    
                    $arg["parts"] .= "<tr height=\"30\" bgcolor=\"#ffffff\">";
                    $arg["parts"] .= "  <td class=\"no_search\" width=\"20%\" align=\"right\">{$val["LABEL"]}</td>";
                    $arg["parts"] .= "  <td>{$textBox}</td>";
                    $arg["parts"] .= "</tr>";
                }else if($val["TYPE"] == "combo"){
                    $opt = array();
                    if ($val["BLANK"] == "BLANK") {
                        $opt[] = array("label" => "", "value" => "");
                    }
                    $result = $db->query($val["SQL"]);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $opt[] = array('label' => $row["LABEL"],
                                       'value' => $row["VALUE"]);
                        //if ($value === $row["VALUE"]) $value_flg = true;
                    }
                    //$value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
                    //コンボボックス作成
                    $comboBox = knjCreateCombo($objForm, $val["NAME"], $model->field[$val["NAME"]], $opt, $val["EXTRA"], $val["SIZE"]);

                    $arg["parts"] .= "<tr height=\"30\" bgcolor=\"#ffffff\">";
                    $arg["parts"] .= "  <td class=\"no_search\" width=\"20%\" align=\"right\">{$val["LABEL"]}</td>";
                    $arg["parts"] .= "  <td>{$comboBox}</td>";
                    $arg["parts"] .= "</tr>";

                    $result->free();
                }else if($val["TYPE"] == "calendar"){
                    $calendar = View::popUpCalendar($objForm,$val["NAME"],str_replace("-","/",$model->field[$val["NAME"]]));

                    $arg["parts"] .= "<tr height=\"30\" bgcolor=\"#ffffff\">";
                    $arg["parts"] .= "  <td class=\"no_search\" width=\"20%\" align=\"right\">{$val["LABEL"]}</td>";
                    $arg["parts"] .= "  <td>{$calendar}</td>";
                    $arg["parts"] .= "</tr>";
                }else if($val["TYPE"] == "radio"){
                    //ラジオボタン
                    $value = $val["VALUE"];
                    $extraRadio = $val["EXTRA"];
                    $radioArray = knjCreateRadio($objForm, $val["NAME"], $model->field[$val["NAME"]], $extraRadio, $value, count($value));
                    foreach($radioArray as $rkey => $rval) $radio[$rkey] = $rval;
                    
                    $radioBtn = "";
                    for($j = 0; $j < count($val["R_LABEL"]); $j++){
                        $radioBtn .= $radio[$val["LABELID"][$j]]."<LABEL for=\"".$val["LABELID"][$j]."\">".$val["R_LABEL"][$j]."</LABEL>　";
                    }
                    
                    $arg["parts"] .= "<tr height=\"30\" bgcolor=\"#ffffff\">";
                    $arg["parts"] .= "  <td class=\"no_search\" align=\"right\">".$val["LABEL"]."</td>";
                    $arg["parts"] .= "  <td>{$radioBtn}</td>";
                    $arg["parts"] .= "</tr>";
                }
                
            }
            Query::dbCheckIn($db);

        }
        
        if($model->title != ""){
            $arg["TITLE"] = $model->title;
        }
        
        //xmlファイル読み込みたい
        $arg["file"] = knjCreateFile($objForm, "XML", "", 1024000);
        
        
            $arg["valErr"] = "";
            
            //ファイル名作成
            $filename = explode("/", $model->xmlfilename);
            $errorFile = mb_convert_encoding($filename[count($filename) -1], "UTF-8", "SJIS");
            
        if($model->valErrFlg == 1){
            $arg["valErrMess"]  = "<br><br><br>\n";
            $arg["valErrMess"] .= "<table align=\"center\" width=\"45%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n";
            $arg["valErrMess"] .= "    <tbody>\n";
            $arg["valErrMess"] .= "      <tr class=\"no_search_line\">\n";
            $arg["valErrMess"] .= "        <td >\n";
            $arg["valErrMess"] .= "          <table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"3\">\n";
            $arg["valErrMess"] .= "            <tr height=\"30\" class=\"no_search_line\">\n";
            $arg["valErrMess"] .= "              <th align=\"center\" class=\"no_search\" colspan=\"4\">\n";
            $arg["valErrMess"] .= "              <font color=\"#ffffff\"><b>XMLファイルエラー　{$errorFile}</b></font>\n";
            $arg["valErrMess"] .= "              </th>\n";
            $arg["valErrMess"] .= "            </tr>\n";
            $arg["valErrMess"] .= "              <th align=\"center\" class=\"no_search\" width=\"15%\">種類</th>\n";
            $arg["valErrMess"] .= "              <th align=\"center\" class=\"no_search\" width=\"10%\">コード</th>\n";
            $arg["valErrMess"] .= "              <th align=\"center\" class=\"no_search\" width=\"65%\">メッセージ</th>\n";
            $arg["valErrMess"] .= "              <th align=\"center\" class=\"no_search\" width=\"10%\">行数</th>\n";
            $arg["valErrMess"] .= "            </tr>\n";
            
            foreach($model->valErrMess as $key => $val){
                $arg["valErrMess"] .= "            <tr height=\"30\" bgcolor=\"#ffffff\">\n";
                foreach($val as $seckey => $secval){
                    //エラーレベルで種類書き換え
                    if($seckey == "level"){
                        if($secval == "1"){
                            $arg["valErrMess"] .= "              <td>WARNING</td>\n";
                        }else if($secval == "2"){
                            $arg["valErrMess"] .= "              <td>Error</td>\n";
                        }else{
                            $arg["valErrMess"] .= "              <td>Fatal Error</td>\n";
                        }
                    }else if($seckey != "column" && $seckey != "file"){
                        $arg["valErrMess"] .= "              <td>{$secval}</td>\n";
                    }
                }
                
                $arg["valErrMess"] .= "            </tr>\n";
            }
            $arg["valErrMess"] .= "          </table>\n";
            $arg["valErrMess"] .= "        </td>\n";
            $arg["valErrMess"] .= "      </tr>\n";
            $arg["valErrMess"] .= "    </tbody>\n";
            $arg["valErrMess"] .= "</table>\n";

            
        }
        
        //ボタン作成
        makeButton($objForm, $arg, $model, $db);
        
        
$html = <<<EOP

<br><br><br><br><br>
<table align="center" width="35%" border="0" cellspacing="0" cellpadding="0">
    <tbody>
      <tr class="no_search_line">
        <td >
          <table width="100%" border="0" cellspacing="1" cellpadding="3">
            <tr height="30" class="no_search_line">
              <th class="no_search" colspan="2" align="center">
              <font color="#ffffff"><b>{$arg["TITLE"]} 取込</b></font>
              </th>
            </tr>
            
            {$arg["parts"]}
            
            <tr height="30" bgcolor="#ffffff">
              <td class="no_search" width="20%" align="right">xmlファイル</td>
              <td>{$arg["file"]}</td>
            </tr>
            
            <tr height="30" bgcolor="#ffffff">
              <td colspan="2" align="center">{$arg["btn_read"]} {$arg["btn_end"]}</td>
            </tr>
          </table>
        </td>
      </tr>
    </tbody>
</table>

{$arg["valErrMess"]}
EOP;
return $html;
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model, $db)
{
    //読み込み実行ボタン
    //$extra = "onclick=\"return btn_submit('read');\"";
    $extra = " ";   //jqueryの使用に伴い削除
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "xmlファイルを取込", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//xml読み込み
function getReadModel(&$model)
{
    //if (is_uploaded_file($model->xmlfilename)) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる


$replace = array(
    "lgxml:"   => ""
);

$buff = file_get_contents($model->xmlfilename);
$buff = strtr($buff, $replace);
file_put_contents($model->xmlfilename, $buff);

        $xml = simplexml_load_file($model->xmlfilename);
        $data = json_decode(str_replace("lgxml:","",json_encode($xml)),true);
        //$data = get_object_vars($xml);
        return $data;
/*    }else{
        echo "lll";
    }*/

}
//xmlファイルをチェックする
function libxml_display_error($error) 
{ 
    $message .= "<br/>\n"; 
    switch ($error->level) { 
        case LIBXML_ERR_WARNING: 
        $message .= "<b>Warning $error->code</b>: "; 
        break; 
        case LIBXML_ERR_ERROR: 
        $message .= "<b>Error $error->code</b>: "; 
        break; 
        case LIBXML_ERR_FATAL: 
        $message .= "<b>Fatal Error $error->code</b>: "; 
        break; 
    } 
    $message .= trim($error->message); 
    if ($error->file) { 
        $message .= " in <b>$error->file</b>"; 
    } 
    $message .= " on line <b>$error->line</b>\n"; 
    
    $validationError = 1;
    
    return array($validationError, $message); 
} 

function libxml_display_errors() { 
    $errors = libxml_get_errors(); 
    //echo "<b>Errors Found!</b>"; 
    /*foreach ($errors as $error) { 
        list($valError, $mess) = libxml_display_error($error); 
        //echo $mess;
        $validate = $valError;
        $messageA .= $mess;
    }*/
    $validate = 1; 
    libxml_clear_errors(); 
//echo $messageA."<BR>";
    return array($validate, $errors);
} 

function readxml(&$model) {
    // Enable user error handling 
    libxml_use_internal_errors(true); 

    $xml = new DOMDocument(); 
    $xml->load("{$model->xmlfilename}"); 
    if (!$xml->schemaValidate("{$model->xsd}")) { 
        list($valError, $message) = libxml_display_errors(); 
        return array($valError, $message);
    } else { 
        $validationError = 0;
        $message = "";
        return array($validationError, $message); 
    } 
}


    function _update($table,$col="",$where="",$field,$db,$mode=2)
    {
        //更新モードで、whereと列名が両方無ければエラー
        if($mode==""){
            $mode=2;
        }
        if($where=="" && $col=="" && $mode!=2){
            $query = "function Update error<br>";
            return $query;
        }

        //列の型を取得。data配列に格納
        $query  = "SELECT NAME,TBNAME,TYPENAME  FROM SYSIBM.SYSCOLUMNS ";
        $query .= "WHERE TBNAME = '".$table."' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $type = trim($row["TYPENAME"]);
            if($type=="DOUBLE" || $type=="INTEGER" || $type=="TIMESTAMP" ||
               $type=="TIME" || $type=="SMALLINT"){
                if($field[$row["NAME"]]!=""){
                    $data[$row["NAME"]][NUMBER] = $field[$row["NAME"]];
                }
                $flg = 1;
            }else{
                if($field[$row["NAME"]]!=""){
                    $data[$row["NAME"]][TEXT]   = $field[$row["NAME"]];
                }
                $flg = 0;
            }
            if($col==$row["NAME"]){ 
                $keytype = $flg;    //指定行が数字か文字か判別し、条件クエリ生成に利用
            }
        }

        //whereが無くcolが有り＝列名を判別して条件クエリを作成
        if($where=="" && $col !=""){
            if($keytype==1){
                $where = " WHERE ".$col." = ".$field[$col];
            }else{
                $where = " WHERE ".$col." = '".$field[$col]."' ";
            }
        }
        if($mode != "2"){
            $query = updateSQL($data, $table, $where);
        }else{
            $query = insertSQL($data, $table);
        }

        ob_start();
        var_dump($query);
        $dump = ob_get_contents();
        ob_end_clean();
        file_put_contents( '/tmp/_update_query.log', "[".date('Y-m-d H:i:s')."]".$dump, FILE_APPEND );
        $db->query($query);
        // echo "<pre>";
        // var_dump($query);
        // echo "<pre>";
        // exit();
        return $query;
    }
    
    function insertSQL($arg, $table)
    {
        $fields = array_keys($arg);
        $sql = "insert into $table(" .implode($fields, ",") .") ";

        $sql .= "values(";
        $sp = "";
        foreach($fields as $f){
            $key = key($arg[$f]);
            if(is_array($arg[$f][$key])){
                $sql .= $sp ."NULL";
            }else if (trim($arg[$f][$key]) == ''){
                $sql .= $sp ."NULL";
            }else{
                switch($key){
                    case TEXT:
                        $sql .= $sp ."'" .Query::addquote($arg[$f][$key]) ."'";
                        break;
                    case NUMBER:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case FUNC:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case DATE:
                        $sql .= $sp ."'" .Query::date2sql($arg[$f][$key]) ."'";
                        break;
                    default;
                        $sql .= $sp .$arg[$f][$key];
                        break;
                }
            }
            $sp = ",";
        }
        return $sql .= ")";
    }
    function updateSQL($arg, $table, $where)
    {
        $fields = array_keys($arg);
        $sql = "UPDATE $table SET ";
        $sp = "";
        foreach($fields as $f){
            $key = key($arg[$f]);
            if (trim($arg[$f][$key]) == ''){
                $sql .= $sp ."$f = NULL";
            }else{
                switch($key){
                    case TEXT:
                        $sql .= $sp ."$f = '" .Query::addquote($arg[$f][$key]) ."'";
                        break;
                    case NUMBER:
                        $sql .= $sp ."$f = " .$arg[$f][$key];
                        break;
                    case FUNC:
                        $sql .= $sp ."$f = " .$arg[$f][$key];
                        break;
                    case DATE:
                        $sql .= $sp ."$f = '" .Query::date2sql($arg[$f][$key]) ."'";
                        break;
                    default;
                        $sql .= $sp ."$f = " .$arg[$f][$key];
                        break;
                }
            }
            $sp = ",";
        }

        return $sql . " " . $where;
    }

?>