<?php

require_once('for_php7.php');

//定数定義

class knjl080rModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $csv_data;

    //メンバ変数の初期化
    function knjl080rModel()
    {
    }
    function init()
    {
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd           = VARS::request("cmd");
        $this->ObjYear       = CTRL_YEAR + 1;
        $this->selectdata    = VARS::post("selectdata");    //対象者一覧
        $this->selectdata2   = VARS::post("selectdata2");   //合格者一覧
        $this->appli_type    = VARS::post("APPLI_TYPE");
        $this->applicantdiv  = VARS::post("APPLICANTDIV");
        $this->testdiv       = VARS::post("TESTDIV");
        $this->pro_date      = VARS::post("PRO_DATE");
        $this->file          = VARS::file("csvfile");
        $this->header        = VARS::post("chk_header");
        $this->csv_radio     = VARS::post("csv");
        $this->tetuduki_zumi_check    = VARS::post("TETUDUKI_ZUMI_CHECK");
        $this->tetuduki_all_check     = VARS::post("TETUDUKI_ALL_CHECK");
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
        knjl080rQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }
    
    function CSVModel()
    {
        if ($this->csv_radio == "1") {
            $this->CSVUproad();
        } elseif ($this->csv_radio == "2") {
            $this->CSVDownload();
        }
    }
    
    //CSV取り込み、更新
    /************* CSVフォーマット（☆印がキー）********************************************************
       ☆入試年度(4桁)、☆入試制度(1桁)、☆対象者区分(1桁）、☆入試区分(1桁）、受験番号(4桁0埋め)、氏名
    ****************************************************************************************************/
    function CSVUproad()
    {
        $db = Query::dbCheckOut();
        
        $valids = array();
        //左と右のリストに存在する受験番号のみを取り込むために取得
        $result = $db->query(knjl080rQuery::GetLeftList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $valids[] = $row["EXAMNO"];
        }
        $valids = array_unique($valids);
        Query::dbCheckIn($db);

        //続けて更新するので以下の変数を初期化する
        $this->selectdata    = array();   //CSVで取り込んだ受験番号
        $this->selectdata2   = array();   //CSVで取り込んだ以外の受験番号

        if (is_uploaded_file ($this->file["tmp_name"])){
            $fp = fopen ($this->file["tmp_name"], "r");

            $i = $j = 0;
            $err = array();
            $header = false;
            
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
                    if (strlen($tmp[5]) < 5) {
                        $tmp[5] = sprintf("%05d", $tmp[5]);
                    }
                    //画面上のキーと一致してない場合は取り込まない
                    if ($tmp[0] != $this->ObjYear | $tmp[1] != $this->appli_type | $tmp[3] != $this->testdiv) {
                        $validate = false;
                    }
                    //合格者のみ取り込む
                    if (!in_array($tmp[5], $valids)) {
                        $validate = false;
                    }

                    if ($validate){
                        $this->selectdata[] = str_replace("'", "''", $tmp[5]);
                        $i++;
                    }else{
                        $err[] = $j+1;
                    }
                    $j++;
                }
            }
            fclose($fp);

            if ($i > 0){
            
                //CSVから取り込んだ受験番号以外の合格している受験番号をセット
                $this->selectdata2 = array_diff($valids, $this->selectdata);
                $this->selectdata = implode(",",$this->selectdata);
                $this->selectdata2 = implode(",",$this->selectdata2);
                
                knjl080rQuery::getUpdateQuery($this);
                $this->setMessage("MSG202");
                
                if (get_count($err) > 0){
                    $this->message .= "失敗した行：" .implode($err, ",");
                }
            } else {
                $this->message = "CSV登録に失敗しました。";
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
        $result = $db->query(knjl080rQuery::GetName("L004",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD2"] == $this->testdiv) $testname = $row["NAME1"];
        }
        //2:一般入試
        if ($this->testdiv === '2') {
            $appliname = array("1" => "一次手続者" , "2" => "二次手続者");
        //1:推薦入試
        } else {
            $appliname = array("2" => "推薦者手続");
        }

        //ヘッダ
        $head = array("入試年度","対象者コード","対象者","入試区分コード","入試区分","受験番号","氏名","手続日付");

        //対象者取得
        $result = $db->query(knjl080rQuery::GetLeftList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->tetuduki_zumi_check === '1') {
                if ($row["DIV"] != "1") continue;
            }
            $row["PRODATE"] = str_replace("-","/",$row["PRODATE"]);
            $data[] = array( $this->ObjYear                    //入試年度
                            ,$this->appli_type                 //対象者コード
                            ,$appliname[$this->appli_type]     //対象者名称
                            ,$this->testdiv                    //入試区分
                            ,$testname                         //入試区分名称
                            ,$row["EXAMNO"]                    //受験番号
                            ,$row["NAME"]                      //氏名
                            ,$row["PRODATE"]);                 //手続日付
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

            $filename = $this->ObjYear."_".$appliname[$this->appli_type]."_".$testname.".csv";
            common::downloadContents($contents, $filename);
            Query::dbCheckIn($db);
            exit;
            
        } else {
            $this->setWarning("MSG304");
        }
    }
}
?>