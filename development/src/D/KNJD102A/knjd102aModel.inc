<?php

require_once('for_php7.php');
class knjd102aModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $exp_year = "";
    var $exp_semester = "";
    var $testTable;         //テストコンボ使用テーブル
    var $field = array();
    var $control;           //コントロールマスタ

    function knjd102aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_year);
            unset($this->exp_semester);
        }

        $this->field = array("TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別コンボ
                             "REMARK1"          =>  VARS::post("REMARK1")           //連絡欄
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name       = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 458) {
            $this->setWarning("MSG915","( 連絡欄は458バイトまでです )");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        $db = Query::dbCheckOut();

        knjd102aQuery::getDeleteQuery($db, $this);
        knjd102aQuery::getInsertQuery($db, $this);
        return $result;

        Query::dbCheckIn($db);
    }
}
?>
