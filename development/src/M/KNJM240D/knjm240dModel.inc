<?php

require_once('for_php7.php');

class knjm240dModel extends Model {
    var $data       = array();
    var $field      = array();
    var $cmd;
    var $year;
    var $semester;
    var $subclassCd;            //科目コード
    var $chairCd;               //講座コード
    var $chairName;             //講座名

    //更新するフィールド
    var $item = array("KAMOKU", "SCHCNT", "CHECKCNT");

    var $control_data = array();    //コントロールマスタデータを保持

    function knjm240dModel() {
       common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        if (VARS::get("CHAIRCD") != "")     $this->chairCd      = VARS::get("CHAIRCD");
        if (VARS::get("SUBCLASSCD") != "")  $this->subclassCd   = VARS::get("SUBCLASSCD");
        if (VARS::get("CHAIRNAME") != "")   $this->chairName    = VARS::get("CHAIRNAME");

        if (VARS::post("GrYEAR")) {
            $this->year = VARS::post("GrYEAR");
        }

        if (VARS::get("GetYear")) {
            $this->year = VARS::get("GetYear");
        }

        if (VARS::request("SEMESTER") != "") {
            $this->semester = VARS::request("SEMESTER");
        }

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == 'init' || VARS::get("PROGRAMID")) {
            unset($this->chairCd);
            unset($this->subclassCd);
            unset($this->chairName);
        }

        $this->getPropertiesAll(); //プロパティファイル
    }

    //入力項目チェック
    function validateInput() {
        if ($this->subclassCd == '' || $this->chairCd == '') {
            $this->setWarning("リストよりデータを選択してから行ってください。");
            return false;
        }
        for ($i = 0; $i < get_count($this->data["SEMESTER"]); $i++) {
            $semester = $this->data["SEMESTER"][$i];
            foreach ($this->setFieldName as $key => $fieldName) {
                if (trim($this->fields[$fieldName][$semester]) == "") {
                    $this->setWarning('MSG301');
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SEMESTER"]); $i++) {
            $semester = $this->data["SEMESTER"][$i];
            foreach ($this->setFieldName as $key => $fieldName) {
                $field_name = $fieldName."-".$semester;
                $this->fields[$fieldName][$semester] = VARS::post($field_name);
            }
        }

        //入力チェック
        if ($this->validateInput()) {
            //追加、更新
            knjm240dQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
