<?php

require_once('for_php7.php');

class knjz216Model extends Model {
    var $leftMockcd;
    var $leftGrade;
    var $leftGroupDiv;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $cmd;

    function knjz216Model()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");
        $this->sendFlg = VARS::get("SEND_FLG");

        if ($this->sendFlg) {
            $this->sendField = array("MOCKCD"       =>  VARS::get("MOCKCD"),
                                     "GROUP_DIV"    =>  VARS::get("GROUP_DIV"),
                                     "GRADE"        =>  VARS::get("GRADE"),
                                     "COURSECD"     =>  VARS::get("COURSECD"),
                                     "MAJORCD"      =>  VARS::get("MAJORCD"),
                                     "COURSECODE"   =>  VARS::get("COURSECODE")
                                     );
        }

        if ($this->cmd == "leftChange") {
            $this->leftMockcd = VARS::post("LEFT_MOCKCD");
            $this->leftGrade = VARS::post("LEFT_GRADE");
            $this->leftGroupDiv = VARS::post("LEFT_GROUP_DIV");
        }

        $this->field = array("MOCKCD"       =>  VARS::post("MOCKCD"),
                             "GROUP_DIV"    =>  VARS::post("GROUP_DIV"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "COURSECD"     =>  VARS::post("COURSECD"),
                             "MAJORCD"      =>  VARS::post("MAJORCD"),
                             "GROUP_NAME"   =>  VARS::post("GROUP_NAME")
                             );

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

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
        $lastYear = $db->getRow(knjz216Query::getList($this, "LAST"));

        //今年度データ取得
        $nowYear = $db->getRow(knjz216Query::getList($this));

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
        $result = knjz216Query::getCopyQuery($this);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {
        $check = array("GROUP_DIV"    =>  array("NULL" => "ON", "LEN" => "2", "NAME" => "科目数"),
                       "GROUP_NAME"   =>  array("NULL" => "ON", "LEN" => "30", "NAME" => "グループ名"),
                       "MOCKCD"       =>  array("NULL" => "ON", "LEN" => "9", "NAME" => "模試コード")
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
                if (in_array($key, array("GROUP_DIV", "MOCKCD")) && $val["NULL"] == "ON") {
                    //必須
                    if (strlen($this->field[$key]) == 0) {
                        $this->setWarning("MSG301","(".$val["NAME"].")");
                        return false;
                    }
                }
            }
        }

        //削除の際 MOCK_SUBCLASS_GROUP_DAT にデータが残っていればエラーとする
        if ($this->cmd == 'delete') {
            $db = Query::dbCheckOut();
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["GRADE"] = $setVal[0];
                $this->field["COURSECD"] = $setVal[1];
                $this->field["MAJORCD"] = $setVal[2];
                $this->field["COURSECODE"] = $setVal[3];

                $query = knjz216Query::checkDat($this);
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

                knjz216Query::getDeleteQuery($this->field);
                $this->insert();
            }
            $this->clean();
            $this->setMessage("MSG201");
        }
        return;
    }

    function &insert()
    {
        $result = knjz216Query::getInsertQuery($this->field);
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

                knjz216Query::getDeleteQuery($this->field, "ALL");
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
