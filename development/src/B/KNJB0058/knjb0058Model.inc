<?php

require_once('for_php7.php');

class knjb0058Model extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;

    function knjb0058Model() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),
                             "RIREKI_CODE"      =>  VARS::post("RIREKI_CODE"),
                             "COURSECODE"       =>  VARS::post("COURSECODE"),
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),
                             "SYORI"            =>  VARS::post("SYORI"),
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),
                            );
        $this->selectStd = VARS::post("selectStd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput()) {
            if ($this->field["SYORI"] == "2") {
                $setDate = knjb0058Query::getUpdateQuery($db, $this);
                $setDate["SDATE"] = str_replace("-", "/", $setDate["SDATE"]);
                $setDate["EDATE"] = str_replace("-", "/", $setDate["EDATE"]);
            } else {
                $query = knjb0058Query::getDeleteQuery($this);
                $db->query($query);
            }
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }
}
?>
