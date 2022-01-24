<?php

require_once('for_php7.php');

class knjz215Model extends Model {
    var $leftGrade;
    var $leftGroupDiv;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $selectSublass = array();
    var $cmd;

    function knjz215Model()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->sendFlg = VARS::get("SEND_FLG");

        if ($this->sendFlg) {
            $this->sendField = array("GROUP_DIV"    =>  VARS::get("GROUP_DIV"),
                                     "GRADE"        =>  VARS::get("GRADE"),
                                     "COURSECD"     =>  VARS::get("COURSECD"),
                                     "MAJORCD"      =>  VARS::get("MAJORCD"),
                                     "COURSECODE"   =>  VARS::get("COURSECODE")
                                     );
        }

        if ($this->cmd == "leftChange") {
            $this->leftGrade = VARS::post("LEFT_GRADE");
            $this->leftGroupDiv = VARS::post("LEFT_GROUP_DIV");
        }

        $this->field = array("GROUP_DIV"    =>  VARS::post("GROUP_DIV"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "COURSECD"     =>  VARS::post("COURSECD"),
                             "MAJORCD"      =>  VARS::post("MAJORCD"),
                             "CLASSCD"      =>  VARS::post("CLASSCD")
                             );

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        $this->selectSublass = array();
        $this->selectSublass = explode(",", VARS::post("selectSublass"));

        if (VARS::get("PROGRAMID")) {
            $this->clean();
            unset($this->leftGrade);
            unset($this->leftGroupDiv);
        }
    }

    function &getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //去年度データ取得
        $lastYear = $db->getRow(knjz215Query::getCopyList($this, "LAST"));

        //今年度データ取得
        $nowYear = $db->getRow(knjz215Query::getCopyList($this));

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
        $result = knjz215Query::getCopyQuery($this);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["GRADE"] = $setVal[0];
                $this->field["COURSECD"] = $setVal[1];
                $this->field["MAJORCD"] = $setVal[2];
                $this->field["COURSECODE"] = $setVal[3];

                knjz215Query::getDeleteQuery($this->field);
                $this->insert();
            }
            $this->field["GRADE"] = "";
            $this->clean();
            $this->setMessage("MSG201");
        }
        return;
    }

    function &insert()
    {
        $result = knjz215Query::getInsertQuery($this->field, $this->selectSublass, $this);
        return $result;
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["GRADE"] = $setVal[0];
                $this->field["COURSECD"] = $setVal[1];
                $this->field["MAJORCD"] = $setVal[2];
                $this->field["COURSECODE"] = $setVal[3];

                knjz215Query::getDeleteQuery($this->field);
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
