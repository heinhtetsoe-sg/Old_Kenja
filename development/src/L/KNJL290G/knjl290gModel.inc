<?php

require_once('for_php7.php');


class knjl290gModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $procedurediv;      //手続区分
    var $entdiv;            //入辞区分
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl290gModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->procedurediv);
            unset($this->entdiv);
            unset($this->checkexam);
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;

        $this->examno           = VARS::post("EXAMNO");

        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->entdiv           = VARS::post("ENTDIV");

        //フィールド
        $this->field = array("COURSEMAJOR"          => VARS::post("COURSEMAJOR"),                //志望学科
                             "SUC_COURSECD"         => substr(VARS::post("COURSEMAJOR"), 0, 1),  //合格過程コード
                             "SUC_MAJORCD"          => substr(VARS::post("COURSEMAJOR"), 1, 3),  //合格学科コード
                             "SUC_COURSECODE"       => substr(VARS::post("COURSEMAJOR"), 4, 4),  //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入辞区分

                             "APPLICANTDIV"         => VARS::post("APPLICANTDIV"),               //入試制度
                             "PROCEDUREDATE"        => VARS::post("PROCEDUREDATE"),              //入学学科

                             "TESTDIV"              => VARS::post("TESTDIV"),                    //入試区分
                             "ENTER_COURSEMAJOR"    => VARS::post("ENTER_COURSEMAJOR"),                //入学学科
                             "ENTER_COURSECD"       => substr(VARS::post("ENTER_COURSEMAJOR"), 0, 1),  //入学課程コード
                             "ENTER_MAJORCD"        => substr(VARS::post("ENTER_COURSEMAJOR"), 1, 3),  //入学学科コード
                             "ENTER_COURSECODE"     => substr(VARS::post("ENTER_COURSEMAJOR"), 4, 4),  //入学コースコード
                             "ENTRYPAY_LOAN"        => VARS::post("ENTRYPAY_LOAN"),              //入学支度金貸付(利用する)
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
        $row = knjl290gQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 8) {
            $this->setWarning("MSG915","受験番号は8バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl290gQuery::getUpdateQuery($this);
//            $this->testdiv = $this->field["TESTDIV"];
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