<?php

require_once('for_php7.php');

//定数定義

class knjl082oModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $csv_data;

    //メンバ変数の初期化
    function knjl082oModel()
    {
    }
    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->ObjYear       = CTRL_YEAR + 1;
        $this->selectdata    = VARS::post("selectdata");    //対象者一覧
        $this->selectdata2   = VARS::post("selectdata2");   //合格者一覧
        $this->appli_type    = VARS::post("APPLI_TYPE");
        $this->testdiv       = VARS::post("TESTDIV");
        $this->applicantdiv  = VARS::post("APPLICANTDIV");
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
        knjl082oQuery::getUpdateQuery($this);
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
       ☆入試年度(4桁)、☆入試制度(1桁)、☆対象者区分(1桁）、☆入試区分(1桁）、受験番号(5桁0埋め)、氏名
    ****************************************************************************************************/
    function CSVUproad()
    {
        $db = Query::dbCheckOut();
        
        $valids = array();
        //左と右のリストに存在する受験番号のみを取り込むために取得
        $result = $db->query(knjl082oQuery::GetRightList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $valids[] = $row["EXAMNO"];
        }
        $result = $db->query(knjl082oQuery::GetLeftList($this));
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
                    $tmp[7] = sprintf("%05d", $tmp[7]);

                    //画面上のキーと一致してない場合は取り込まない
                    if ($tmp[0] != $this->ObjYear | $tmp[1] != $this->applicantdiv | $tmp[3] != $this->appli_type | $tmp[5] != $this->testdiv) {
                        $validate = false;
                    }

                    //合格者のみ取り込む
                    if (!in_array($tmp[7], $valids)) {
                        $validate = false;
                    }

                    if ($validate){
                        $this->selectdata[] = str_replace("'", "''", $tmp[7]);
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
                
                knjl082oQuery::getUpdateQuery($this);
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
        $result = $db->query(knjl082oQuery::GetName("L003",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD2"] == $this->applicantdiv) $applicantname = $row["NAME1"];
        }
        $result = $db->query(knjl082oQuery::getTestdivMst($this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if($row["NAMESPARE2"]=='1') {
                if ($row["NAMECD2"] == $this->testdiv) $testname = $row["NAME1"];
            }
        }
        $appliname = array("1" => "特待対象者", "2" => "手続延期者", "3" => "手続者");

        //ヘッダ
        $head = array("入試年度","入試制度コード","入試制度","対象者コード","対象者","入試区分コード","入試区分","受験番号","氏名");
        
        //対象者取得
        $result = $db->query(knjl082oQuery::GetLeftList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data[] = array( $this->ObjYear                    //入試年度
                            ,$this->applicantdiv               //入試制度
                            ,$applicantname                    //入試制度名称
                            ,$this->appli_type                 //対象者コード
                            ,$appliname[$this->appli_type]     //対象者名称
                            ,$this->testdiv                    //入試区分
                            ,$testname                         //入試区分名称
                            ,$row["EXAMNO"]                    //受験番号
                            ,$row["NAME"]);                    //氏名
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

            if($this->appli_type == 3){
                $filename = $this->ObjYear.$applicantname."_".$appliname[$this->appli_type]."_全体.csv";
            }else{
                $filename = $this->ObjYear.$applicantname."_".$appliname[$this->appli_type]."_".$testname.".csv";
            }
            common::downloadContents($contents, $filename);

#            $fp = fopen($filename,"w");
#            if (is_writable($filename)) {
#                fwrite($fp, $contents);
#            }
#            fclose($fp);
#           echo "<script language=\"javascript\">";
#           echo "top.main_frame.bottom_frame.location.href = '".REQUESTROOT."/L/knjl082o/$filename';";
#           echo "</script>";

            Query::dbCheckIn($db);
            exit;
            
        } else {
            $this->setWarning("MSG304");
        }
    }
}
?>
