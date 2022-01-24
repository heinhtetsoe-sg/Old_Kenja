<?php

require_once('for_php7.php');

class knjl130kModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $testdiv;           //試験区分
    var $desirediv;         //志望区分
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $fs_cd;             //学校コード
    var $field = array();           //フィールドデータ
    var $cflg;
    var $checkexam;
    var $befJudgement;      //DBの合格コード

    function knjl130kModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->testdiv);
            unset($this->desirediv);
            unset($this->procedurediv);
            unset($this->judgement);
            unset($this->fs_cd);
            unset($this->cflg);
            unset($this->checkexam);            
        }

        $this->cmd          = VARS::request("cmd");
        $this->year         = CTRL_YEAR + 1;
        $this->desirediv    = VARS::post("DESIREDIV");
        $this->procedurediv = VARS::post("PROCEDUREDIV");
        $this->judgement    = VARS::post("JUDGEMENT");
        
        if (VARS::request("EXAMNO")) {
            $this->examno = VARS::request("EXAMNO");
        }
        if (VARS::request("TESTDIV")) {
            $this->testdiv = VARS::request("TESTDIV");
        }
        if (strlen(VARS::get("fs_cd"))) {
            $this->fs_cd = VARS::get("fs_cd");
        }
        if (strlen($this->examno)) {
            $this->examno = sprintf("%04d",$this->examno);
        }
        if ($this->cmd == "change_testdiv") {
            $this->examno = "";
        }

        //フィールド
        $this->field = array("TESTDIV"              => VARS::post("TESTDIV"),                    //試験区分
                             "SEX"                  => VARS::post("SEX"),                        //性別
                             "SHDIV"                => VARS::post("SHDIV"),                      //専併区分
                             "APPLICANTDIV"         => VARS::post("APPLICANTDIV"),               //出願区分
                             "DESIREDIV"            => VARS::post("DESIREDIV"),                  //志望区分
                             "COURSEMAJOR"          => VARS::post("COURSEMAJOR"),                //志望学科
                             "SUC_COURSECD"         => substr(VARS::post("COURSEMAJOR"), 0, 1),  //合格過程コード
                             "SUC_MAJORCD"          => substr(VARS::post("COURSEMAJOR"), 1, 3),  //合格学科コード
                             "SUC_COURSECODE"       => substr(VARS::post("COURSEMAJOR"), 4, 4),  //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "JUDGEMENT_GROUP_NO"   => VARS::post("JUDGEMENT_GROUP_NO"),         //追加繰上合格No.
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入学区分
                             "SUCCESS_NOTICENO"     => VARS::post("SUCCESS_NOTICENO"),           //合格通知No.
                             "OLD_SUCCESS_NOTICENO" => VARS::post("OLD_SUCCESS_NOTICENO"),       //旧合格通知No. 2005.12.30 minei
                             "FAILURE_NOTICENO"     => VARS::post("FAILURE_NOTICENO"),           //不合格通知No.
                             "INTERVIEW_ATTEND_FLG" => VARS::post("INTERVIEW_ATTEND_FLG"),       //面接出欠フラグ
                             "SCALASHIPDIV"         => VARS::post("SCALASHIPDIV")                //スカラシップ区分
                            );

        if ($this->cmd == "change" || $this->cmd == "change_judge") {
            $this->cflg = VARS::post("cflg");
        } else {
            $this->cflg = "";
        }

        $db = Query::dbCheckOut();
        $result = $db->query(knjl130kQuery::get_examcourse($this->year));
        $i = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $this->field["JUDGEMENT1_".$i]  = VARS::post("JUDGEMENT1_".$i);
            $this->field["JUDGEMENT2_".$i]  = VARS::post("JUDGEMENT2_".$i);
            $this->field["COURSECD".$i]     = VARS::post("COURSECD".$i);
            $this->field["MAJORCD".$i]      = VARS::post("MAJORCD".$i);
            $this->field["EXAMCOURSECD".$i] = VARS::post("EXAMCOURSECD".$i);
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if ((!strlen($this->examno))) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if ((!strlen($this->field["APPLICANTDIV"]))) {
            $this->setWarning("MSG301", "( 出願区分 )");
            return false;
        }
        if ((!strlen($this->field["TESTDIV"]))) {
            $this->setWarning("MSG301", "( 試験区分 )");
            return false;
        }
        if ((!strlen($this->field["DESIREDIV"]))) {
            $this->setWarning("MSG301", "( 志望区分 )");
            return false;
        }
        if ((!strlen($this->field["SEX"]))) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }
        if ((!strlen($this->field["SHDIV"]))) {         #2005/11/22
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        if (($this->field["JUDGEMENT"] >= 1 && $this->field["JUDGEMENT"] <= 6) || $this->field["JUDGEMENT"] == 9) {
            if ((!strlen($this->field["COURSEMAJOR"]))) {
                $this->setWarning("MSG301", "( 学科 )");
                return false;
            }
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }                
        //存在チェック
        $row = knjl130kQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 4) {
            $this->setWarning("MSG915","受験番号は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["SUCCESS_NOTICENO"]) > 4) {
            $this->setWarning("MSG915","合格通知No.は4バイトまでです。");
            return false;
        }
        //合格通知No重複チェック  2005/08/16
        if (strlen($this->field["SUCCESS_NOTICENO"]) > 0) {
            $row = knjl130kQuery::Existdata_Sno($this,$this->field["SUCCESS_NOTICENO"]); #2006/01/13
            if (0 < $row["COUNT"]) {
                $this->setWarning("MSG302","合格通知No.");
                return false;
            }
        }
        if (strlen($this->field["FAILURE_NOTICENO"]) > 4) {
            $this->setWarning("MSG915","不合格通知No.は4バイトまでです。");
            return false;
        }
        //不合格通知No重複チェック  2005/08/16 
        if (strlen($this->field["FAILURE_NOTICENO"]) > 0) {
            $row = knjl130kQuery::Existdata_Sno($this,$this->field["FAILURE_NOTICENO"]); #2006/01/13
            if (0 < $row["COUNT"]) {
                $this->setWarning("MSG302","不合格通知No.");
                return false;
            }
        }
        if (strlen($this->field["JUDGEMENT_GROUP_NO"]) > 2) {
            $this->setWarning("MSG915","追加繰上合格No.は2バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl130kQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        } else {
            $this->cmd = "main";
        }   
        return;
    } 
}
?>