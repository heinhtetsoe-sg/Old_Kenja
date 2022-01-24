<?php

require_once('for_php7.php');

class knjh185Model extends Model {
    var $field = array();
    var $cmd;
    var $selectdataL;
    var $selectdataR;

    function knjh185Model() {}

    function init() {

        $this->field = array("COURSECODE"       =>  VARS::post("COURSECODE"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "CARE_DATE"        =>  VARS::post("CARE_DATE"),
                             "FARE_CD"          =>  VARS::post("FARE_CD"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        $this->selectdataL = VARS::post("selectdataL");
        $this->selectdataR = VARS::post("selectdataR");

        //預かり費用セット
        $db = Query::dbCheckOut();
        $this->fare_array = array();
        $query = knjh185Query::getFareList();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->fare_array[$row["FARE_CD"]] = $row["FARE"];
        }
        $result->free();
        Query::dbCheckIn($db);

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->field["CARE_DATE"]) == "") {
            $this->setWarning("MSG301", "( 預かり日付 )");
            return false;
        }
        //対象有無チェック
        if (!$this->selectdataL && !$this->selectdataR) {
            $this->setWarning("MSG303", "( 園児 )");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjh185Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
