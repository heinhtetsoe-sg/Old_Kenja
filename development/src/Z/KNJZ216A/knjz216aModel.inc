<?php

require_once('for_php7.php');

class knjz216aModel extends Model {
    var $leftSemester;
    var $leftProficiencyCd;
    var $leftProficiencyDiv;
    var $leftGrade;
    var $leftGroupDiv;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $cmd;

    function knjz216aModel()
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
            $this->leftSemester = VARS::post("LEFT_SEMESTER");
            $this->leftProficiencyCd = VARS::post("LEFT_PROFICIENCYCD");
            $this->leftProficiencyDiv = VARS::post("LEFT_PROFICIENCYDIV");
            $this->leftGrade = VARS::post("LEFT_GRADE");
            $this->leftGroupDiv = VARS::post("LEFT_GROUP_DIV");
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "COURSECD"         =>  VARS::post("COURSECD"),
                             "MAJORCD"          =>  VARS::post("MAJORCD"),
                             "GROUP_NAME"       =>  VARS::post("GROUP_NAME")
                             );

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        if (VARS::get("PROGRAMID")) {
            $this->clean();
            unset($this->leftSemester);
            unset($this->leftProficiencyCd);
            unset($this->leftProficiencyDiv);
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
        $lastYear = $db->getRow(knjz216aQuery::getList($this, "LAST"));

        //今年度データ取得
        $nowYear = $db->getRow(knjz216aQuery::getList($this));

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
        $result = knjz216aQuery::getCopyQuery($this);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {
        $check = array("SEMESTER"      =>  array("NULL" => "ON", "LEN" => "1", "NAME" => "学期"),
                       "GROUP_DIV"      =>  array("NULL" => "ON", "LEN" => "2", "NAME" => "科目数"),
                       "GROUP_NAME"     =>  array("NULL" => "ON", "LEN" => "30", "NAME" => "グループ名"),
                       "PROFICIENCYDIV" =>  array("NULL" => "ON", "LEN" => "2", "NAME" => "区分"),
                       "PROFICIENCYCD"  =>  array("NULL" => "ON", "LEN" => "4", "NAME" => "実力コード")
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
            foreach ($check as $key => $val) {
                //必須チェック
                if (in_array($key, array("SEMESTER", "GROUP_DIV", "PROFICIENCYDIV", "PROFICIENCYCD")) && $val["NULL"] == "ON") {
                    //必須
                    if (strlen($this->field[$key]) == 0) {
                        $this->setWarning("MSG301","(".$val["NAME"].")");
                        return false;
                    }
                }
            }
        }

        //削除の際 PROFICIENCY_SUBCLASS_GROUP_DAT にデータが残っていればエラーとする
        if ($this->cmd == 'delete') {
            $db = Query::dbCheckOut();
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["GRADE"] = $setVal[0];
                $this->field["COURSECD"] = $setVal[1];
                $this->field["MAJORCD"] = $setVal[2];
                $this->field["COURSECODE"] = $setVal[3];

                $query = knjz216aQuery::checkDat($this);
                $count = $db->getOne($query);
                if ($count > 0) {
                    $this->setWarning("MSG305","模試科目グループデータを削除してから行って下さい。");
                    return false;
                }
            }
            Query::dbCheckIn($db);
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

                knjz216aQuery::getDeleteQuery($this->field);
                $this->insert();
            }
            $this->clean();
            $this->setMessage("MSG201");
        }
        return;
    }

    function &insert()
    {
        $result = knjz216aQuery::getInsertQuery($this->field);
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

                knjz216aQuery::getDeleteQuery($this->field, "ALL");
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
