<?php

require_once('for_php7.php');

//定数定義

class knjl120kModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $csv_data;

    //メンバ変数の初期化
    function knjl120kModel()
    {
    }
    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->ObjYear       = CTRL_YEAR + 1;
        $this->selectdata    = VARS::post("selectdata");    //辞退者一覧
        $this->selectdata2   = VARS::post("selectdata2");   //手続者一覧
        $this->selectdata3   = VARS::post("selectdata3");   //合格者一覧
        $this->testdiv       = VARS::post("TESTDIV");
        $this->shdiv         = VARS::post("SHDIV");
        $this->appli_type    = VARS::post("APPLI_TYPE");
        $this->file          = VARS::file("csvfile");
        $this->header        = VARS::post("chk_header");
        $this->csv_radio     = VARS::post("csv");
    }

    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }

    function &update()
    {
        knjl120kQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return $result;
    }
    
    function CSVModel()
    {
        if ($this->csv_radio == "1") {
            $this->CSVUpload();
        } elseif ($this->csv_radio == "2") {
            $this->CSVDownload();
        }
    }
    
    //CSV取り込み、更新
    function CSVUpload()
    {
        if (is_uploaded_file ($this->file["tmp_name"])){
            $fp = fopen ($this->file["tmp_name"], "r");
            $err = array();
            $header = false;
            $data = $sp = "";
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);

                if (trim($buffer) != ''){
                    //ヘッダ有りの場合は1行目を無視
                    if ($this->header == "1" && !$header) {
                        $header = true;
                        continue;
                    }

                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp = common::csv2array($buffer);
                    $validate = true;

                    //受験番号
                    $tmp[1] = sprintf("%04d", $tmp[1]);

                    //画面上の対象者区分と一致してない場合は取り込まない
//2005.12.30 minei
#                    if ($tmp[0] != ($this->appli_type + 1)) {
                    if ($tmp[0] != ($this->appli_type )) {
                        $validate = false;
                    }
                    if ($validate){
                        $data .= $sp .$tmp[1];
                        $sp = ",";
                    }else{
                        $err[] = $j+1;
                    }
                }
            }
            fclose($fp);
            if ($data != ""){
/* 2005.12.30 minei
                //手続き者
                if ($this->appli_type == 2){
                    $this->selectdata = $data;
                //入学辞退者
                }else if ($this->appli_type == 1){
                    $this->selectdata2 = $data;
                }
*/
                //2005.12.30 minei
                //入学辞退者
                if ($this->appli_type == 3){
                    $this->selectdata = $data;
                //手続き者
                }else if ($this->appli_type == 2){
                    $this->selectdata2 = $data;
                }

                /** ADD 2005/12/01 by ameku */
                //更新される受験番号を取得
                $array2 = knjl120kQuery::getCheckQuery($this,$data);
                knjl120kQuery::getUpdateQuery($this);
                $array1 = explode(",", $data);
                //取り込んだ受験番号と更新される受験番号の差を取得
                $result = array_diff ($array1, $array2);

                $msg = "";
                if (get_count($result) > 0){
                    $msg  = "\\n";
                    $msg .= "　更新データ件数：".get_count($array2)."件　　　　　　　　　\\n";
                    $msg .= "未更新データ件数：".get_count($result)."件\\n\\n";
                    $msg .= "　未更新データ受験番号：\\n";
                    foreach($result as $val){
                    $msg .= "　　　".$val ."\\n";
                    }
                }else{
                    $msg = "\\n更新データ件数：".get_count($array2)."件　　　　　　　　　\\n";
                }
                /** ADD 2005/12/01 by ameku */
                $this->setMessage("MSG202",$msg);

               if (get_count($err) > 0){
                   $this->message .= "　失敗した行数：" .implode($err, ",");
               }
            } else {
               $this->message = "CSV登録に失敗しました。\\n\\n更新可能データが存在しないか、区分が異なっている可能性があります。\\nCSVの中身を再度確認して下さい。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }
    }
    
    //CSV書き出し
    function CSVDownload()
    {
        $db = Query::dbCheckOut();
        $data = $head = array();

        //名称取得
        $result = $db->query(knjl120kQuery::GetName("L003",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD2"] == $this->testdiv) $testname = $row["NAME1"];
        }
        $result = $db->query(knjl120kQuery::GetName("L006",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD2"] == $this->shdiv) $shname = $row["NAME1"];
        }

//2005.12.29 minei
#        $appliname = array("1" => "手続者", "2" => "辞退者");
        $appliname = array("4" => "合格者", "2" => "手続者", "3" => "辞退者");

        //ヘッダ
        $head = array("区分","受験番号");
        
        //対象者取得
        $result = $db->query(knjl120kQuery::GetCsvList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
//2005.12.29 minei
#            $data[] = array(($this->appli_type + 1)          //2:手続者,3:辞退者(CSVフォーマットでコードが２と３なので+1する）
            $data[] = array(($this->appli_type)             //未手続者,手続者,辞退者
                            ,$row["EXAMNO"]);               //受験番号
        }

        if (get_count($data)>0) {
            $contents = "";
            if ($this->header == "1"){
                $contents = implode($head, ",") ."\r\n";
            }

            foreach($data as $csv){
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

            $filename = $this->ObjYear."_".$testname."_".$shname."_".$appliname[$this->appli_type].".csv";
            common::downloadContents($contents, $filename);
            Query::dbCheckIn($db);
            exit;
            
        } else {
            $this->setWarning("MSG304");
        }
    }
}
?>
