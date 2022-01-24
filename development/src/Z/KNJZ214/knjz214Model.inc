<?php

require_once('for_php7.php');

class knjz214Model extends Model {
    var $leftGrade;
    var $leftGroupDiv;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $cmd;

    function knjz214Model()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");
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
                             "GROUP_NAME"   =>  VARS::post("GROUP_NAME")
                             );

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        if (VARS::get("PROGRAMID")) {
            $this->clean();
            unset($this->leftGrade);
            unset($this->leftGroupDiv);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function &getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //去年度データ取得
        $lastYear = $db->getRow(knjz214Query::getList($this, "LAST"));

        //今年度データ取得
        $nowYear = $db->getRow(knjz214Query::getList($this));

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
        $result = knjz214Query::getCopyQuery($this);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {
        $check = array("GROUP_DIV"    =>  array("NULL" => "ON", "LEN" => "2", "NAME" => "科目数"),
                       "GROUP_NAME"   =>  array("NULL" => "ON", "LEN" => "30", "NAME" => "グループ名")
                       );
        if ($this->cmd == "update") {
            foreach ($check as $key => $val) {
                //必須チェック
                if ($val["NULL"] == "ON") {
                    //必須
                    if (strlen($this->field[$key]) == 0) {
                        $this->setWarning("MSG301","(".$val["NAME"].")");
                        return false;
                    }
                }
                if ($val["LEN"]) {
                    if (strlen($this->field[$key]) > $val["LEN"] ) {
                        $this->setWarning("MSG915", $val["NAME"]." ".$val["LEN"]."バイトまで");
                        return false;
                    }
                }
            }
        } else {
            //必須
            if (strlen($this->field["GROUP_DIV"]) == 0) {
                $this->setWarning("MSG301","(科目数)");
                return false;
            }
        }

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

                knjz214Query::getDeleteQuery($this->field);
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
        $result = knjz214Query::getInsertQuery($this->field);
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

                knjz214Query::getDeleteQuery($this->field, "ALL");
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
