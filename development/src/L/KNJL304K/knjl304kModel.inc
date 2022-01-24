<?php

require_once('for_php7.php');

//定数定義

class knjl304kModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $selectdata3;
    var $csv_data;
    var $base;
    var $jhflg;
    var $baseexamno;
    var $baseaccept;
    var $center_title;  //NO001

    //メンバ変数の初期化
    function knjl304kModel()
    {
    }
    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->ObjYear       = CTRL_YEAR + 1;
        $this->selectdata    = preg_split("/,/",VARS::post("selectdata"));    //志願者データで上書き
        $this->selectdata2   = preg_split("/,/",VARS::post("selectdata2"));   //変更なし
        $this->selectdata3   = preg_split("/,/",VARS::post("selectdata3"));   //事前相談データで上書き
        $this->testdiv       = VARS::post("TESTDIV");
        $this->shdiv         = VARS::post("SHDIV");
        $this->jhflg         = VARS::post("JHFLG");                     //NO003
        $this->appli_type    = VARS::post("APPLI_TYPE");
        $this->center_title  = VARS::post("CENTER_TITLE");              //NO001
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
            if ($this->jhflg == "1"){   //NO003
                for ($i = 0;$i < get_count($this->selectdata);$i++){
                    $base = preg_split("/-/",$this->selectdata[$i]);
                    $baseexamno = $base[1];
                    $testdiv1   = $base[2];
                    $baseaccept = $base[3];
                    $testdiv2   = $base[4];
                    if ($baseexamno != "ERROR" && $baseaccept != "ERROR" && $baseexamno != "" && $baseaccept != ""){
                        $this->updatej($baseexamno,$baseaccept,$testdiv1,$testdiv2,1);
                    }
                }

                for ($i = 0;$i < get_count($this->selectdata3);$i++){
                    $base = preg_split("/-/",$this->selectdata3[$i]);
                    $baseexamno = $base[1];
                    $testdiv1   = $base[2];
                    $baseaccept = $base[3];
                    $testdiv2   = $base[4];
                    if ($baseexamno != "ERROR" && $baseaccept != "ERROR" && $baseexamno != "" && $baseaccept != ""){
                        $this->updatej($baseexamno,$baseaccept,$testdiv1,$testdiv2,2);
                    }
                }
            }else {
                for ($i = 0;$i < get_count($this->selectdata);$i++){
                    $base = preg_split("/-/",$this->selectdata[$i]);
                    $baseexamno = $base[1];
                    $baseaccept = $base[2];
                    if ($baseexamno != "ERROR" && $baseaccept != "ERROR" && $baseexamno != "" && $baseaccept != ""){
                        $this->updateh($baseexamno,$baseaccept,1);
                    }
                }

                for ($i = 0;$i < get_count($this->selectdata3);$i++){
                    $base = preg_split("/-/",$this->selectdata3[$i]);
                    $baseexamno = $base[1];
                    $baseaccept = $base[2];
                    if ($baseexamno != "ERROR" && $baseaccept != "ERROR" && $baseexamno != "" && $baseaccept != ""){
                        $this->updateh($baseexamno,$baseaccept,2);
                    }
                }
            }
        }
    }

    function &updateh($exam,$accept,$flg)
    {
        knjl304kQuery::getUpdateQueryHs($this,$exam,$accept,$flg);

        $this->setMessage("MSG202");

        return $result;
    }
    
    function &updatej($exam,$accept,$testdiv1,$testdiv2,$flg)
    {
        knjl304kQuery::getUpdateQueryJs($this,$exam,$accept,$testdiv1,$testdiv2,$flg);

        $this->setMessage("MSG202");

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
                    if ($tmp[0] != ($this->appli_type + 1)) {
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
                //手続き者
                if ($this->appli_type == 2){
                    $this->selectdata = $data;
                //辞退者
                }else if ($this->appli_type == 1){
                    $this->selectdata2 = $data;
                }
                knjl304kQuery::getUpdateQuery($this);
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
        $result = $db->query(knjl304kQuery::GetName("L003",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD2"] == $this->testdiv) $testname = $row["NAME1"];
        }
        $result = $db->query(knjl304kQuery::GetName("L006",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD2"] == $this->shdiv) $shname = $row["NAME1"];
        }
        $appliname = array("1" => "手続者", "2" => "辞退者");

        //ヘッダ
        $head = array("区分","受験番号");
        
        //対象者取得
        $result = $db->query(knjl304kQuery::GetLeftList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data[] = array(($this->appli_type + 1)             //2:手続者,3:辞退者(CSVフォーマットでコードが２と３なので+1する）
                            ,$row["EXAMNO"]);                    //受験番号
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
