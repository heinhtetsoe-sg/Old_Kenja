<?php

require_once('for_php7.php');


class knjl090bModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $judgement;         //判定区分
    var $judge_kind;        //特奨
    var $procedurediv;      //手続区分
    var $proceduredate;     //入金日
    var $procedurediv1;      //手続区分
    var $proceduredate1;     //入金日
    var $testdiv2;          //入試区分
    var $judgediv;          //合否区分
    var $entdiv;            //入辞区分
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    function knjl090bModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->judge_kind);
            unset($this->procedurediv);
            unset($this->proceduredate);
            unset($this->procedurediv1);
            unset($this->proceduredate1);
            unset($this->judgement);
            unset($this->testdiv2);
            unset($this->judgediv);
            unset($this->checkexam);
            unset($this->cflg);
            unset($this->entdiv);
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;

        if (strlen(VARS::post("EXAMNO"))) {
            if ($this->examno != VARS::post("EXAMNO")) {
                $this->cflg = "false";
                unset($this->judgement);
            } elseif ($this->cmd == "change" || $this->cmd == "change_testdiv2") { //編集中フラグ
                $this->cflg = "true";
            } else {
                $this->cflg = "false";
            }
            $this->examno  = VARS::post("EXAMNO");
            //$this->examno = sprintf("%05d",$this->examno);
        } else {
            unset($this->judgement);
        }

        $this->judge_kind       = VARS::post("JUDGE_KIND");
        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->proceduredate    = VARS::post("PROCEDUREDATE");
        $this->procedurediv1    = VARS::post("PROCEDUREDIV1");
        $this->proceduredate1   = VARS::post("PROCEDUREDATE1");
        $this->testdiv2         = VARS::post("TESTDIV2");
        $this->judgediv         = VARS::post("JUDGEDIV");
        $this->entdiv           = VARS::post("ENTDIV");

        //フィールド
        $this->field = array("COURSEMAJOR"          => VARS::post("COURSEMAJOR"),                //志望学科
                             "SUC_COURSECD"         => substr(VARS::post("COURSEMAJOR"), 0, 1),  //合格過程コード
                             "SUC_MAJORCD"          => substr(VARS::post("COURSEMAJOR"), 1, 3),  //合格学科コード
                             "SUC_COURSECODE"       => substr(VARS::post("COURSEMAJOR"), 4, 4),  //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "JUDGE_KIND"           => VARS::post("JUDGE_KIND"),                 //特奨
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "PROCEDUREDATE"        => VARS::post("PROCEDUREDATE"),              //入金日
                             "PROCEDUREDIV1"        => VARS::post("PROCEDUREDIV1"),              //１次手続区分
                             "PROCEDUREDATE1"       => VARS::post("PROCEDUREDATE1"),             //１次入金日
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入辞区分
                             "TESTDIV2"             => VARS::post("TESTDIV2"),                   //入試区分
                             "REMARK1"              =>  VARS::post("REMARK1"),                   //当日チェック
                             "TANGAN_CD"            =>  VARS::post("TANGAN_CD"),                 //単願コース
                             "ENTER_COURSEMAJOR"    => VARS::post("ENTER_COURSEMAJOR"),                //入学学科
                             "ENTER_COURSECD"       => substr(VARS::post("ENTER_COURSEMAJOR"), 0, 1),  //入学課程コード
                             "ENTER_MAJORCD"        => substr(VARS::post("ENTER_COURSEMAJOR"), 1, 3),  //入学学科コード
                             "ENTER_COURSECODE"     => substr(VARS::post("ENTER_COURSEMAJOR"), 4, 4),  //入学コースコード
                             "JUDGEDIV"             => VARS::post("JUDGEDIV")                    //合否区分
                            );
    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["TESTDIV2"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if ($this->judgement == "1") {
            if (!strlen($this->field["COURSEMAJOR"])) {
                $this->setWarning("MSG301", "( 入学区分 )");
                return false;
            }
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }        
        //存在チェック
        $row = knjl090bQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1"], (20 * 2)) > 2) {
            $this->setWarning("MSG915","(当日チェックは2行までです)");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl090bQuery::getUpdateQuery($this);
            $this->testdiv2 = $this->field["TESTDIV2"];
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