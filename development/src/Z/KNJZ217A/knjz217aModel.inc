<?php

require_once('for_php7.php');

class knjz217aModel extends Model {
    var $leftSemester;
    var $leftGrade;
    var $leftGroupDiv;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $selectSublass = array();
    var $cmd;

    function knjz217aModel()
    {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");
        $this->sendFlg = VARS::get("SEND_FLG");

        if ($this->sendFlg) {
            $this->sendField = array("SEMESTER"         =>  VARS::get("SEMESTER"),
                                     "PROFICIENCYDIV"   =>  VARS::get("PROFICIENCYDIV"),
                                     "PROFICIENCYCD"    =>  VARS::get("PROFICIENCYCD"),
                                     "GROUP_DIV"        =>  VARS::get("GROUP_DIV"),
                                     "GRADE"            =>  VARS::get("GRADE"),
                                     "COURSECD"         =>  VARS::get("COURSECD"),
                                     "MAJORCD"          =>  VARS::get("MAJORCD"),
                                     "COURSECODE"       =>  VARS::get("COURSECODE")
                                     );
        }

        if ($this->cmd == "leftChange") {
            $this->leftSemester         = VARS::post("LEFT_SEMESTER");
            $this->leftProficiencyDiv   = VARS::post("LEFT_PROFICIENCYDIV");
            $this->leftProficiencyCd    = VARS::post("LEFT_PROFICIENCYCD");
            $this->leftGrade            = VARS::post("LEFT_GRADE");
            $this->leftGroupDiv         = VARS::post("LEFT_GROUP_DIV");
        }

        $this->field = array("SEMESTER"                 =>  VARS::post("SEMESTER"),
                             "PROFICIENCYDIV"           =>  VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD"            =>  VARS::post("PROFICIENCYCD"),
                             "GROUP_DIV"                =>  VARS::post("GROUP_DIV"),
                             "GRADE"                    =>  VARS::post("GRADE"),
                             "COURSECD"                 =>  VARS::post("COURSECD"),
                             "MAJORCD"                  =>  VARS::post("MAJORCD"),
                             "COURSECODE"               =>  VARS::post("COURSECODE"),
                             "PROFICIENCY_SUBCLASS_CD"  =>  VARS::post("PROFICIENCY_SUBCLASS_CD")
                             );

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        $this->selectSublass = array();
        $this->selectSublass = explode(",", VARS::post("selectSublass"));

        if (VARS::get("PROGRAMID")) {
            $this->clean();
            unset($this->leftSemester);
            unset($this->leftProficiencyDiv);
            unset($this->leftProficiencyCd);
            unset($this->leftGrade);
            unset($this->leftGroupDiv);
        }
        //初期値セット
        if (!$this->leftSemester) {
            $this->leftSemester = CTRL_SEMESTER;
        }
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
    }

    function &getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //去年度データ取得
        $lastYear = $db->getRow(knjz217aQuery::getCopyList($this, "LAST"));

        //今年度データ取得
        $nowYear = $db->getRow(knjz217aQuery::getCopyList($this));

        //DB切断
        Query::dbCheckIn($db);

        //データ有無
        if (!is_array($lastYear)){
            $this->setWarning("前年度データが、ありません。");
            return false;
        }

        //データ有無
        if (is_array($nowYear)){
            $this->setWarning("今年度データが、存在しています。");
            return false;
        }

        $this->copy();
        $this->setMessage("MSG201");

        return;
    }

    function &copy()
    {
        $result = knjz217aQuery::getCopyQuery($this);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {
        if (strlen($this->field["SEMESTER"]) == 0) {
            $this->setWarning("MSG301","(学期)");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["GRADE"]           = $setVal[0];
                $this->field["GROUP_DIV"]       = $setVal[1];
                $this->field["COURSECD"]        = $setVal[2];
                $this->field["MAJORCD"]         = $setVal[3];
                $this->field["COURSECODE"]      = $setVal[4];
                $this->field["PROFICIENCYDIV"]  = $setVal[5];
                $this->field["PROFICIENCYCD"]   = $setVal[6];

                knjz217aQuery::getDeleteQuery($this->field);
                $this->insert();
            }
            $this->clean();
            $this->setMessage("MSG201");
        }
        return;
    }

    function &insert()
    {
        $result = knjz217aQuery::getInsertQuery($this->field, $this->selectSublass);
        return $result;
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["GRADE"]           = $setVal[0];
                $this->field["GROUP_DIV"]       = $setVal[1];
                $this->field["COURSECD"]        = $setVal[2];
                $this->field["MAJORCD"]         = $setVal[3];
                $this->field["COURSECODE"]      = $setVal[4];
                $this->field["PROFICIENCYDIV"]  = $setVal[5];
                $this->field["PROFICIENCYCD"]   = $setVal[6];

                foreach ($this->selectSublass as $subkey => $subval) {
                    $this->field["PROFICIENCY_SUBCLASS_CD"] = $subval;
                    knjz217aQuery::getDeleteQuery($this->field);
                }
            }
        }
        $this->clean();
        $this->setMessage("MSG201");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
        $this->sendField = array();
    }
}
?>
