<?php

require_once('for_php7.php');


class knjl090uModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $applicantdiv;      //入試制度
    var $testdiv;           //入試区分
    var $examno;            //受験番号
    var $judgediv;          //合否区分
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $entdiv;            //入辞区分
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    function knjl090uModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examno);
            unset($this->procedurediv);
            unset($this->entdiv);
            unset($this->judgement);
            unset($this->judgediv);
            unset($this->checkexam);
            unset($this->cflg);
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;
        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->testdiv          = VARS::post("TESTDIV");

        if (strlen(VARS::post("EXAMNO"))) {
            if ($this->examno != VARS::post("EXAMNO")) {
                $this->cflg = "false";
                unset($this->judgement);
            } elseif ($this->cmd == "change") { //編集中フラグ
                $this->cflg = "true";
            } else {
                $this->cflg = "false";
            }
            $this->examno  = VARS::post("EXAMNO");
        } else {
            $this->examno  = VARS::post("EXAMNO");
            unset($this->judgement);
        }

        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->entdiv           = VARS::post("ENTDIV");
        $this->judgediv         = VARS::post("JUDGEDIV");

        //フィールド
        $this->field = array("KUBUN"                => VARS::post("KUBUN"),                     //受験区分(表示のみ)
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                 //合否判定
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),              //手続区分
                             "PROCEDUREDATE"        => VARS::post("PROCEDUREDATE"),             //手続日
                             "ENTDIV"               => VARS::post("ENTDIV"),                    //入学区分
                             "SCHREGNO"             => VARS::post("SCHREGNO"),                  //学籍番号

                             "JUDGEDIV"             => VARS::post("JUDGEDIV")                   //合否区分
                            );
    }


    //入力項目チェック
    function validateInput() {
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
        $row = knjl090uQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl090uQuery::getUpdateQuery($this);
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