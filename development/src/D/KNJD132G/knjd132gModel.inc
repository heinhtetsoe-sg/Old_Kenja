<?php

require_once('for_php7.php');

class knjd132gModel extends Model {
    var $cmd;
    var $exp_year;
    var $schregno;
    var $name;
    var $field = array();

    function knjd132gModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }
        $this->semester9  = "9";//固定

        $this->field = array("TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),   //総合学習の評価
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),    //通信欄
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //左フレームから取得
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::post("SEMESTER");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //総合学習の評価
        $this->getPro["TOTALSTUDYTIME"]["moji"] = 30;
        $this->getPro["TOTALSTUDYTIME"]["gyou"] = 6;

        //通信欄
        $this->getPro["COMMUNICATION"]["moji"] = 30;
        $this->getPro["COMMUNICATION"]["gyou"] = 7;
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        //文字数チェック
        $chkArr = array();
        $chkArr[] = array("fieldName" => "TOTALSTUDYTIME",      "j_Title" => "総合学習の評価");
        $chkArr[] = array("fieldName" => "COMMUNICATION",       "j_Title" => "通信欄");

        foreach ($chkArr as $key => $valArr) {
            if ($this->validate_row_cnt($this->field["{$valArr["fieldName"]}"], ($this->getPro["{$valArr["fieldName"]}"]["moji"] * 2)) > $this->getPro["{$valArr["fieldName"]}"]["gyou"]) {
                $this->setWarning("MSG915","({$valArr["j_Title"]}は{$this->getPro["{$valArr["fieldName"]}"]["gyou"]}行までです)");
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
        //HREPORTREMARK_DAT
        knjd132gQuery::getInsertQuery($this);

        return ;
    }
}
?>
