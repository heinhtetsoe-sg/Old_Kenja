<?php

require_once('for_php7.php');


class knjl091rModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $applicantdiv;      //入試制度
    var $examno;            //受験番号
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $proceduredate;     //入金日
    var $testdiv2;          //入試区分
    var $entdiv;            //入学区分
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $testDivArr = array();
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    function knjl091rModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->procedurediv);
            unset($this->proceduredate);
            unset($this->judgement);
            unset($this->testdiv2);
            unset($this->checkexam);
            unset($this->cflg);
            unset($this->entdiv);
        }

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;
        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->exam_type        = '1';

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

        $this->judgement        = VARS::post("JUDGEMENT");
        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->proceduredate    = VARS::post("PROCEDUREDATE");
        $this->testdiv2         = VARS::post("TESTDIV2");
        $this->entdiv           = VARS::post("ENTDIV");

        //フィールド
        $this->field = array("COURSEMAJOR"          => VARS::post("COURSEMAJOR"),                //志望学科
                             "SUC_COURSECD"         => substr(VARS::post("COURSEMAJOR"), 0, 1),  //合格課程コード
                             "SUC_MAJORCD"          => substr(VARS::post("COURSEMAJOR"), 1, 3),  //合格学科コード
                             "SUC_COURSECODE"       => substr(VARS::post("COURSEMAJOR"), 4, 4),  //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "PROCEDUREDATE"        => VARS::post("PROCEDUREDATE"),              //入金日
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入学区分
                             "TESTDIV2"             => VARS::post("TESTDIV2"),                   //入試区分
                             "ENTER_COURSEMAJOR"    => VARS::post("ENTER_COURSEMAJOR"),                //入学学科
                             "ENTER_COURSECD"       => substr(VARS::post("ENTER_COURSEMAJOR"), 0, 1),  //入学課程コード
                             "ENTER_MAJORCD"        => substr(VARS::post("ENTER_COURSEMAJOR"), 1, 3),  //入学学科コード
                             "ENTER_COURSECODE"     => substr(VARS::post("ENTER_COURSEMAJOR"), 4, 4),  //入学コースコード
                            );
    }


    //入力項目チェック
    function validateInput() {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["TESTDIV2"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }

        $db = Query::dbCheckOut();
        $result = $db->query(knjl091rQuery::getName($this->year, array("L013")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD1"] == "L013") $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
        }
        Query::dbCheckIn($db);

        if ($judgeNameSpare1[$this->judgement] == "1") {
            if (!strlen($this->field["COURSEMAJOR"])) {
                $this->setWarning("MSG301", "( 合格コース )");
                return false;
            }
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }        
        //存在チェック
        $row = knjl091rQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","管理番号は5バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl091rQuery::getUpdateQuery($this);
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