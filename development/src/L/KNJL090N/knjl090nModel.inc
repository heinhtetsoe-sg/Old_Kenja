<?php

require_once('for_php7.php');


class knjl090nModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $examno;            //受験番号
    public $judgement;         //判定区分
    public $procedurediv;      //手続区分
    public $proceduredate;     //入金日
    public $pay_money;         //入金額
    public $testdiv2;          //入試区分
    public $judgediv;          //合否区分
    public $entdiv;            //入辞区分
    public $checkexam;
    public $field = array();           //フィールドデータ
    public $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    public function knjl090nModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->procedurediv);
            unset($this->proceduredate);
            unset($this->pay_money);
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
        //$this->examno = sprintf("%05d",$this->examno);
        } else {
            unset($this->judgement);
        }

        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->proceduredate    = VARS::post("PROCEDUREDATE");
        $this->pay_money        = str_replace(",", "", VARS::post("PAY_MONEY"));
        $this->entdiv           = VARS::post("ENTDIV");
        $this->testdiv2         = VARS::post("TESTDIV2");
        $this->judgediv         = VARS::post("JUDGEDIV");

        //フィールド
        $this->field = array("COURSEMAJOR"          => VARS::post("COURSEMAJOR"),                //志望学科
                             "SUC_COURSECD"         => substr(VARS::post("COURSEMAJOR"), 0, 1),  //合格過程コード
                             "SUC_MAJORCD"          => substr(VARS::post("COURSEMAJOR"), 1, 3),  //合格学科コード
                             "SUC_COURSECODE"       => substr(VARS::post("COURSEMAJOR"), 4, 4),  //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "PROCEDUREDATE"        => VARS::post("PROCEDUREDATE"),              //入金日
                             "PAY_MONEY"            => str_replace(",", "", VARS::post("PAY_MONEY")),                  //入金額
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入辞区分
                             "TESTDIV2"             => VARS::post("TESTDIV2"),                   //入試区分
                             "ENTER_COURSEMAJOR"    => VARS::post("ENTER_COURSEMAJOR"),                //入学学科
                             "ENTER_COURSECD"       => substr(VARS::post("ENTER_COURSEMAJOR"), 0, 1),  //入学課程コード
                             "ENTER_MAJORCD"        => substr(VARS::post("ENTER_COURSEMAJOR"), 1, 3),  //入学学科コード
                             "ENTER_COURSECODE"     => substr(VARS::post("ENTER_COURSEMAJOR"), 4, 4),  //入学コースコード
                             "JUDGEDIV"             => VARS::post("JUDGEDIV")                    //合否区分
                            );
    }


    //入力項目チェック
    public function validateInput()
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
        $row = knjl090nQuery::existData($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //入金チェック
        if (strlen($this->field["PROCEDUREDATE"]) || strlen($this->field["PAY_MONEY"])) {
            if (!strlen($this->field["PROCEDUREDATE"]) || !strlen($this->field["PAY_MONEY"])) {
                $this->setWarning("MSG901", "( 手続日・入金額 )\\n手続日・入金額を入力する場合は、両方とも入力して下さい。");
                return false;
            }
            if (!strlen($this->field["PROCEDUREDIV"])) {
                $this->setWarning("MSG901", "( 手続区分 )\\n手続日・入金額を入力する場合は、手続区分を選択して下さい。");
                return false;
            }
        }
        //文字数チェック
        if (strlen($this->examno) > 4) {
            $this->setWarning("MSG915", "受験番号は4バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl090nQuery::getUpdateQuery($this);
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
