<?php

require_once('for_php7.php');

class knja110a_remarkModel extends Model {
    var $field = array();
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $control;
    var $exp_year;      //送られてくる年度
    var $exp_semester;  //送られてくる学期
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限
    var $grade;         //送られてくる年組
    var $sep = "";

    function knja110a_remarkModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("CODE"                 =>  VARS::post("CODE"),                 //項目1
                             "SEQ"                  =>  VARS::post("SEQ"),                  //項目2
                             "QUESTION_CONTENTS"    =>  VARS::post("QUESTION_CONTENTS"),    //設問
                             "ANSWER_PATTERN"       =>  VARS::post("ANSWER_PATTERN"),       //パターン
                             "ANSWER_SELECT_COUNT"  =>  VARS::post("ANSWER_SELECT_COUNT"),  //回答数
                             "REMARK"               =>  VARS::post("REMARK"),               //回答
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //前画面から送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //年組
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        //入力データ格納
        $this->sep = "";
        if ($this->field["ANSWER_PATTERN"] == "2") {
            for ($idx = 1;$idx <= $this->field["ANSWER_SELECT_COUNT"];$idx++) {
                if (VARS::post("REMARK".$idx) == "1" ) {
                    $this->field["REMARK"] = $this->field["REMARK"].$this->sep.$idx;
                    $this->sep = ",";
                }
            }
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->exp_year == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["CODE"] == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["SEQ"] == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数・行数チェック
        if ($this->field["ANSWER_PATTERN"] == "3") {
            if ($this->validate_row_cnt($this->field["REMARK"], (45 * 2)) > 3) {
                $this->setWarning("MSG915","(入力は3行までです)");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knja110a_remarkQuery::getUpdateQuery($this);
    }
}
?>
