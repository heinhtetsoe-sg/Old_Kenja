<?php

require_once('for_php7.php');


class knjd110kModel extends Model {
    
    var $cmd;
    var $year;
    var $month;
    var $day;                //alp 2005/02/03
    var $limit_date;
    var $ctrl = array();
    var $cmd2;
    var $do_run;
    var $school_kind;
    
    function knjd110kModel()
    {        
        common::GetControlMaster_Fnc($this->ctrl);
    }
    
    function init()
    {  
        $this->year       = VARS::post("year");
        $this->month      = VARS::post("month");
        $this->day        = VARS::post("day");                               //alp 2005/02/03
        $this->limit_date = str_replace("/","-",VARS::post("limit_date")); 
        $this->cmd        = VARS::request("cmd");
        $this->cmd2       = VARS::post("cmd2");
        $this->do_run     = VARS::post("DO_RUN");
        $this->school_kind = VARS::post("SCHOOL_KIND");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    
    //入力項目チェック
    function validateInput()
    {
        //日指定チェック NULLと1～31が入力可能 alp 2005/02/03

        if (strlen($this->day) && ((int)$this->day < 1 || (int)$this->day > 31)) {
            $this->setWarning("MSG901", "\\n日指定には 1 ～ 31 まで");
            return false;
        }

        return true;
    }
    
    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    
    function &update()
    {
        $result = knjd110kQuery::getUpdateQuery($this);
                    
        return $result;
    }
    
    function &ChangeAttendDate()
    {
        /*******************************************
            セッション変数の変更(出欠制御日付)
         *******************************************/        
        global $sess;
        //セッション変数に保存
        $session = new APP_Session($sess->id, 'SESS_CTRL');
        $session->unregister("ATTEND_CTRL_DATE");
        $session->register("ATTEND_CTRL_DATE", $this->limit_date);
    }

    //チェック
    function isFileCheck($filename)
    {
        //ファイル存在するか？書き込み可能か？
        if (!is_writable($filename)) {
            $this->setMessage("MSG300", "システム管理者に連絡して下さい。");
            return false;
        }

        return true;
    }

    //DuRun更新
    function &getUpdateDoRun()
    {
        $wrightData = "";
        $filename = DOCUMENTROOT ."/batch/AccumulateSummaryBatch.properties";
        if ($this->isFileCheck($filename)) {
            $fp = @fopen($filename, 'r');
            while ($line = fgets($fp,1024)) {
                $pos = strpos($line, "doRun");
                // === を使用していることに注目しましょう。単純に == を使ったのでは
                // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
                if ($pos === false) {
                    $wrightData .= $line;
                } else {
                    $flg = $this->do_run ? "true" : "false";
                    $wrightData .= "doRun = ".$flg."\n";
                }
            }
            fclose($fp);

            $fp = @fopen($filename, 'w');
            @fwrite($fp, $wrightData, strlen($wrightData)); // ファイルへの書き込み
            fclose($fp);    //ファイルへの書き込みは終了
        }
    }
}
?>
