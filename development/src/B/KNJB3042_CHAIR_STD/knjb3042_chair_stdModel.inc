<?php

require_once('for_php7.php');

class knjb3042_chair_stdModel extends Model {
    var $cmd;
    var $selectSchoolKind;

    function knjb3042_chair_stdModel() {
    }

    function init() {
        $this->compatible();

        $this->cmd = VARS::request("cmd");

        if (VARS::post("START_DATE")) {
            $this->field = array(
                "YEAR"           =>  VARS::post("YEAR"),           //年度
                "SEMESTER"       =>  VARS::post("SEMESTER"),       //学期
                "START_DATE"     =>  VARS::post("START_DATE"),     //開始日付(適用開始日)
                "SCH_PTRN"       =>  VARS::post("SCH_PTRN"),       //基本時間割(1:基本時間割/2:通常時間割)
                "BSCSEQ"         =>  VARS::post("BSCSEQ"),         //基本時間割(テンプレート番号)
                "SELECT_CHAIRCD" =>  VARS::post("SELECT_CHAIRCD")  //選択講座コード
            );
        } else {
            $this->field = array(
                "YEAR"           =>  VARS::get("YEAR"),           //年度
                "SEMESTER"       =>  VARS::get("SEMESTER"),       //学期
                "START_DATE"     =>  VARS::get("START_DATE"),     //開始日付(適用開始日)
                "SCH_PTRN"       =>  VARS::get("SCH_PTRN"),       //基本時間割(1:基本時間割/2:通常時間割)
                "BSCSEQ"         =>  VARS::get("BSCSEQ"),         //基本時間割(テンプレート番号)
                "SELECT_CHAIRCD" =>  VARS::get("SELECT_CHAIRCD")  //選択講座コード
            );
        }

        $this->field['OVERLAP_CHAIR'] = VARS::post("OVERLAP_CHAIR");
        $this->field['SELECT_TYPE'] = VARS::post("SELECT_TYPE");

        if (!$this->field["YEAR"]) {
            $this->field["YEAR"] = CTRL_YEAR;
        }
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        if (!$this->field["START_DATE"]) {
            $this->field["START_DATE"] = CTRL_DATE;
        }

        $this->updateChairList = VARS::post("UPDATE_CHAIRLIST");
        if (isset($this->updateChairList)) {
            $this->updateChairList = json_decode($this->updateChairList, true);
        }

        $this->ajaxParam = VARS::request("AJAX_PARAM");
        if (isset($this->ajaxParam)) {
            $this->ajaxParam = json_decode($this->ajaxParam, true);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function compatible() {
        if(!function_exists("json_encode")) {
            function json_encode($param) {
                require_once("JSON.php");
                $json = new Services_JSON();
                return $json->encode($param); 
            }
            function json_decode($param, $assoc = false) {
                require_once("JSON.php");
                $json = new Services_JSON($assoc ? SERVICES_JSON_LOOSE_TYPE : 0);
                return $json->decode($param);
            }
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
            knjb3042_chair_stdQuery::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }
}
?>
