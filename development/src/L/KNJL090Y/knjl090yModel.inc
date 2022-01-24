<?php

require_once('for_php7.php');


class knjl090yModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $judgement;         //判定区分
    var $judge_kind;        //特奨
    var $procedurediv;      //手続区分
    var $proceduredate;     //入金日
    var $pay_money;         //入金額
    var $testdiv2;          //入試区分
    var $judgediv;          //合否区分
    var $special_measures;  //特別措置区分
    var $entdiv;            //入学区分
    var $checkexam;    
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    function knjl090yModel()
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
            unset($this->pay_money);
            unset($this->judgement);
            unset($this->testdiv2);
            unset($this->judgediv);
            unset($this->special_measures);
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
            $this->examno = sprintf("%05d",$this->examno);
        } else {
            unset($this->judgement);
        }

        $this->judge_kind       = VARS::post("JUDGE_KIND");
        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->proceduredate    = VARS::post("PROCEDUREDATE");
        $this->pay_money        = str_replace(",", "", VARS::post("PAY_MONEY"));
        $this->testdiv2         = VARS::post("TESTDIV2");
        $this->judgediv         = VARS::post("JUDGEDIV");
        $this->special_measures = VARS::post("SPECIAL_MEASURES");
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
                             "PAY_MONEY"            => str_replace(",", "", VARS::post("PAY_MONEY")),                  //入金額
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入学区分
                             "HONORDIV1"            => VARS::post("HONORDIV1"),                  //特待入学
                             "SUCCESS_NOTICENO"     => VARS::post("SUCCESS_NOTICENO"),           //合格通知No.
                             "FAILURE_NOTICENO"     => VARS::post("FAILURE_NOTICENO"),           //不合格通知No.
                             "INTERVIEW_ATTEND_FLG" => VARS::post("INTERVIEW_ATTEND_FLG"),       //面接出欠フラグ
                             "REMARK1"              => VARS::post("REMARK1"),                    //備考１
                             "REMARK2"              => VARS::post("REMARK2"),                    //備考２
                             "TESTDIV2"             => VARS::post("TESTDIV2"),                   //入試区分
                             "APPLICANT_DIV"        => VARS::post("APPLICANT_DIV"),              //志願区分
                             "EXAMINEE_DIV"         => VARS::post("EXAMINEE_DIV"),               //受験区分
                             "JUDGEDIV"             => VARS::post("JUDGEDIV"),                   //合否区分
                             "SPECIAL_MEASURES"     => VARS::post("SPECIAL_MEASURES"),           //特別措置区分
                             "HONORDIV2"            => VARS::post("HONORDIV2"),                  //特待区分
                             "ADJOURNMENTDIV"       => VARS::post("ADJOURNMENTDIV")              //延期区分
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
        if ($this->judgement == "1" || $this->judgement == "3") {
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
        $row = knjl090yQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //スライド合格（スライド希望）チェック
        if ($this->field["JUDGEDIV"] == "3") {
            $row = knjl090yQuery::slideCheck($this);
            if (!isset($row["EXAMNO"])) {
                $this->setWarning("MSG901", "( 合否区分 )\\nスライド合格はスライド希望有りの場合のみです。");
                return false;
            }
        }
        //特別判定合格（特別判定希望）チェック
        if ($this->field["JUDGEDIV"] == "5") {
            $row = knjl090yQuery::specialCheck($this);
            if (!isset($row["EXAMNO"])) {
                $this->setWarning("MSG901", "( 合否区分 )\\n特別判定合格は特別判定希望有りの場合のみです。");
                return false;
            }
        }
        //入金チェック
        if (strlen($this->field["PROCEDUREDATE"]) || strlen($this->field["PAY_MONEY"])) {
            if (!strlen($this->field["PROCEDUREDATE"]) || !strlen($this->field["PAY_MONEY"])) {
                $this->setWarning("MSG901", "( 入金日・入金額 )\\n入金日・入金額を入力する場合は、両方とも入力して下さい。");
                return false;
            }
            if (!strlen($this->field["PROCEDUREDIV"])) {
                $this->setWarning("MSG901", "( 手続区分 )\\n入金日・入金額を入力する場合は、手続区分を選択して下さい。");
                return false;
            }
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 246) {
            $this->setWarning("MSG915","備考1は246バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 122) {
            $this->setWarning("MSG915","備考2は122バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl090yQuery::getUpdateQuery($this);
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