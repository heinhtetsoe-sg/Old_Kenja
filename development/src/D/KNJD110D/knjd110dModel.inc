<?php

require_once('for_php7.php');


class knjd110dModel extends Model {
    
    var $cmd;
    var $year;
    var $month;
    var $day;
    var $limit_date;
    var $ctrl = array();
    var $cmd2;
    var $error_msg = "";
    var $del_check = array();
    var $do_run;
    var $school_kind;
    //knjd110Query.phpで利用。インスタンス化してないのでこちらに移行しました。
    public $tmp_semester;   //UDFのTERM_GETの値を一時代入
    public $tmp_month;      //選択月
    public $tmp_sdate;      //データ抽出範囲の開始日付の値を一時代入
    public $tmp_edate;      //データ抽出範囲の終了日付の値を一時代入
    
    function knjd110dModel()
    {        
        common::GetControlMaster_Fnc($this->ctrl);
    }
    
    function init()
    {  
        $this->year       = VARS::post("year");
        $this->month      = VARS::post("month");
        $this->day        = VARS::post("day");
        $this->limit_date = str_replace("/","-",VARS::post("limit_date")); 
        $this->cmd        = VARS::request("cmd");
        $this->cmd2       = VARS::post("cmd2");
        $this->school_kind = VARS::post("SCHOOL_KIND");

        $this->del_check  = VARS::post("DEL_CHECK");
        $this->do_run     = VARS::post("DO_RUN");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    
    //入力項目チェック
    function validateInput()
    {
        //日指定チェック NULLと1～31が入力可能
        if (strlen($this->day) && ((int)$this->day < 1 || (int)$this->day > 31)) {
            $this->setWarning("MSG901", "\\n日指定には 1 ～ 31 まで");
            return false;
        }

        //名称マスタ「C040」に登録されている、年度・学期・月は、実行不可とする。
        $rtnCnt = knjd110dQuery::getNameMstC040($this);
        if (0 < $rtnCnt) {
            $this->setWarning("MSG300", "\\n名称マスタ「C040」に登録されている月は、実行できません。");
            return false;
        }

        return true;
    }
    
    //更新
    function &getUpdateModel()
    {
        $this->error_msg = "";

        //入力チェック
        if ($this->validateInput()){

            if (knjd110dQuery::getUpdateQuery($this)) {
                $this->setMessage("MSG201");
            }
        }
    }
    
    function &update()
    {
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

    //生成済みデータ削除
    function &getDeleteModel()
    {
        if (get_count($this->del_check)) {
            if (knjd110dQuery::getDeleteQuery($this)) {
                $this->setMessage("MSG201");
            }
        }
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