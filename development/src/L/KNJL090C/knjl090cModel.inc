<?php

require_once('for_php7.php');


class knjl090cModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $testdiv2;          //入試区分
    var $judgediv;          //合否区分
    var $special_measures;  //特別措置区分
    var $entdiv;            //入学区分
    var $checkexam;    
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）
    var $isCollege;

    function knjl090cModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->procedurediv);
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
                unset($this->judgememt);
            } elseif ($this->cmd == "change" || $this->cmd == "change_testdiv2") { //編集中フラグ
                $this->cflg = "true";
            } else {
                $this->cflg = "false";
            }
            $this->examno  = VARS::post("EXAMNO");
            $this->examno = sprintf("%04d",$this->examno);
        } else {
            unset($this->judgememt);
        }

        $this->procedurediv     = VARS::post("PROCEDUREDIV");
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
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
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

        //学校コードを取得：カレッジ・五条・和歌山を判断するため
        $rtnRow = knjl090cQuery::getNameMstZ010();
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
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
        if (strlen($this->field["APPLICANT_DIV"]) || strlen($this->field["EXAMINEE_DIV"])) {
            if ((!strlen($this->field["APPLICANT_DIV"]))) {
                $this->setWarning("MSG301", "( 志願区分 )");
                return false;
            }
            if ((!strlen($this->field["EXAMINEE_DIV"]))) {
                $this->setWarning("MSG301", "( 受験区分 )");
                return false;
            }
        }
        if ($this->field["JUDGEMENT"] == 1) {
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
        $row = knjl090cQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //移行合格（移行希望）チェック
        if ($this->field["JUDGEDIV"] == "4") {
            $row = knjl090cQuery::shiftCheck($this);
            if (!isset($row["EXAMNO"])) {
                $this->setWarning("MSG901", "( 合否区分 )\\n移行合格は移行希望有りの場合のみです。");
                return false;
            }
        }
        //特併合格チェック
        if ($this->field["JUDGEDIV"] == "7") {
            $row = knjl090cQuery::slideCheck($this);
            if (!isset($row["EXAMNO"])) {
                $this->setWarning("MSG901", "( 合否区分 )\\n特併合格は特進併願希望有りの場合のみです。");
                return false;
            }
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
        if (strlen($this->field["FAILURE_NOTICENO"]) > 4) {
            $this->setWarning("MSG915","不合格通知No.は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915","兄弟情報は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 60) {
            $this->setWarning("MSG915","備考は60バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl090cQuery::getUpdateQuery($this);
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