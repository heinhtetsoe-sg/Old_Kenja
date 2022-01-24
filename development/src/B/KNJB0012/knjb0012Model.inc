<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb0012Model extends Model {
    var $cmd;
    var $executedate;                       //カレンダーコントロール用データ
    var $jtype;
    var $semester;
    var $seq;

    /**
     * メンバ変数の初期化
     */
    function knjb0012Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"       =>  VARS::post("SEMESTER"),
                             "RIREKI_CODE"    =>  VARS::post("RIREKI_CODE"),
                             "SCH_PTRN"       =>  VARS::post("SCH_PTRN"),
                             "CHAIR_CNT"      =>  VARS::post("CHAIR_CNT")
                            );
        if ($this->cmd == "chgSeme") {
            $this->field["RIREKI_CODE"] = "";
            $this->field["SCH_PTRN"] = "";
        }

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
        if (VARS::get("SEND_RIREKI_CODE")) {
            $this->field["RIREKI_CODE"] = VARS::get("SEND_RIREKI_CODE");
        }
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $this->updChairArray = array();
        for ($i = 0; $i < $this->field["SCH_PTRN"]; $i++) {
            $setChairCd = VARS::post("CHAIR".$i);
            if ($setChairCd) {
                $this->updChairArray[] = $setChairCd;
            }
        }
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput()) {
            knjb0012Query::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

}
?>
