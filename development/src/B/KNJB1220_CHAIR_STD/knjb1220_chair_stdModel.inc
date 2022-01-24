<?php

require_once('for_php7.php');

class knjb1220_chair_stdModel extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;

    function knjb1220_chair_stdModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"       =>  VARS::post("H_SEMESTER"),        //学期
                             "RIREKI_CODE"    =>  VARS::post("H_RIREKI_CODE"),     //履歴コード
                             "SUBCLASSCD"     =>  VARS::post("H_SUBCLASSCD"),      //科目
                             "SCH_PTRN"       =>  VARS::post("H_SCH_PTRN"),        //基本時間割
                             "CHAIRDIV"       =>  VARS::post("H_CHAIRDIV")         //講座区分
                            );
        $this->selectChair = VARS::post("selectChair");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        if ($this->cmd == "main") {
            $this->schregNo = "";
            $this->exeYear = "";
        }
        if (VARS::get("SCHREGNO")) {
            $this->schregNo = VARS::get("SCHREGNO");
        }
        if (VARS::get("EXE_YEAR")) {
            $this->exeYear = VARS::get("EXE_YEAR");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->field["RIREKI_CODE"] == "") {
            $this->setWarning("MSG203", "履修登録日が選択されていません。");
            return false;
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput()) {
            knjb1220_chair_stdQuery::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }
}
?>
