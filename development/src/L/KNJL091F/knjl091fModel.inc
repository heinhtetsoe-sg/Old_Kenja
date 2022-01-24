<?php

require_once('for_php7.php');


class knjl091fModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $testdiv2;          //入試区分
    var $judgediv;          //合否区分
    var $entdiv;            //入辞区分
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    function knjl091fModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->procedurediv);
            unset($this->entdiv);
            unset($this->judgement);
            unset($this->testdiv2);
            unset($this->judgediv);
            unset($this->checkexam);
            unset($this->cflg);
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
            //$this->examno = sprintf("%04d",$this->examno);
        } else {
            unset($this->judgement);
        }

        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->entdiv           = VARS::post("ENTDIV");
        $this->testdiv2         = VARS::post("TESTDIV2");
        $this->judgediv         = VARS::post("JUDGEDIV");

        //フィールド
        $this->field = array("COURSEMAJOR"          => VARS::post("COURSEMAJOR"),                //志望学科
                             "SUC_COURSECD"         => substr(VARS::post("COURSEMAJOR"), 0, 1),  //合格過程コード
                             "SUC_MAJORCD"          => substr(VARS::post("COURSEMAJOR"), 1, 3),  //合格学科コード
                             "SUC_COURSECODE"       => substr(VARS::post("COURSEMAJOR"), 4, 4),  //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "PROCEDUREDATE"        => VARS::post("PROCEDUREDATE"),              //手続日
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入辞区分
                             "ENTDIV2_DATE"         => VARS::post("ENTDIV2_DATE"),               //辞退日

                             "APPLICANTDIV"         => VARS::post("APPLICANTDIV"),                //入学学科
                             "ENTRANCE_FLG"         => VARS::post("ENTRANCE_FLG"),                //入学学科
                             "ENT_PAY_DIV"          => VARS::post("ENT_PAY_DIV"),                //入学学科
                             "ENT_PAY_DATE"         => VARS::post("ENT_PAY_DATE"),                //入学学科
                             "ENT_PAY_CHAK_DATE"    => VARS::post("ENT_PAY_CHAK_DATE"),                //入学学科
                             "EXP_PAY_DIV"          => VARS::post("EXP_PAY_DIV"),                //入学学科
                             "EXP_PAY_DATE"         => VARS::post("EXP_PAY_DATE"),                //入学学科
                             "EXP_PAY_CHAK_DATE"    => VARS::post("EXP_PAY_CHAK_DATE"),                //入学学科

                             "TESTDIV2"             => VARS::post("TESTDIV2"),                   //入試区分
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
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }        
        //存在チェック
        $row = knjl091fQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 4) {
            $this->setWarning("MSG915","受験番号は4バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl091fQuery::getUpdateQuery($this);
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
