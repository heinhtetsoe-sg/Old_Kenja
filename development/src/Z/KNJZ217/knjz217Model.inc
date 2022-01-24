<?php

require_once('for_php7.php');

class knjz217Model extends Model {
    var $leftGrade;
    var $leftGroupDiv;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $selectSublass = array();
    var $cmd;

    function knjz217Model()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");
        $this->sendFlg = VARS::get("SEND_FLG");

        if ($this->sendFlg) {
            $this->sendField = array("MOCKCD"           =>  VARS::get("MOCKCD"),
                                     "GROUP_DIV"        =>  VARS::get("GROUP_DIV"),
                                     "GRADE"            =>  VARS::get("GRADE"),
                                     "COURSECD"         =>  VARS::get("COURSECD"),
                                     "MAJORCD"          =>  VARS::get("MAJORCD"),
                                     "COURSECODE"       =>  VARS::get("COURSECODE")
                                     );
        }

        if ($this->cmd == "leftChange") {
            $this->leftMockcd   = VARS::post("LEFT_MOCKCD");
            $this->leftGrade    = VARS::post("LEFT_GRADE");
            $this->leftGroupDiv = VARS::post("LEFT_GROUP_DIV");
        }

        $this->field = array("MOCKCD"           =>  VARS::post("MOCKCD"),
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "COURSECD"         =>  VARS::post("COURSECD"),
                             "MAJORCD"          =>  VARS::post("MAJORCD"),
                             "COURSECODE"       =>  VARS::post("COURSECODE"),
                             "MOCK_SUBCLASS_CD" =>  VARS::post("MOCK_SUBCLASS_CD")
                             );

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        $this->selectSublass = array();
        $this->selectSublass = explode(",", VARS::post("selectSublass"));

        if (VARS::get("PROGRAMID")) {
            $this->clean();
            unset($this->leftMockcd);
            unset($this->leftGrade);
            unset($this->leftGroupDiv);
        }
    }

    function &getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //去年度データ取得
        $lastYear = $db->getRow(knjz217Query::getCopyList("LAST"));

        //今年度データ取得
        $nowYear = $db->getRow(knjz217Query::getCopyList());

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
        $result = knjz217Query::getCopyQuery();
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
                $setVal                    = explode(":", $val);
                $this->field["GRADE"]      = $setVal[0];
                $this->field["GROUP_DIV"]  = $setVal[1];
                $this->field["COURSECD"]   = $setVal[2];
                $this->field["MAJORCD"]    = $setVal[3];
                $this->field["COURSECODE"] = $setVal[4];
                $this->field["MOCKCD"]     = $setVal[5];

                knjz217Query::getDeleteQuery($this->field);
                $this->insert();
            }
            $this->clean();
            $this->setMessage("MSG201");
        }
        return;
    }

    function &insert()
    {
        $result = knjz217Query::getInsertQuery($this->field, $this->selectSublass);
        return $result;
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["GRADE"]      = $setVal[0];
                $this->field["GROUP_DIV"]  = $setVal[1];
                $this->field["COURSECD"]   = $setVal[2];
                $this->field["MAJORCD"]    = $setVal[3];
                $this->field["COURSECODE"] = $setVal[4];
                $this->field["MOCKCD"]     = $setVal[5];

                foreach ($this->selectSublass as $subkey => $subval) {
                    $this->field["MOCK_SUBCLASS_CD"] = $subval;
                    knjz217Query::getDeleteQuery($this->field);
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
