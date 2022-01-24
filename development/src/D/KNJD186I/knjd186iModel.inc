<?php

require_once('for_php7.php');

class knjd186iModel extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $field;

    function knjd186iModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field["SEMESTER"]        = VARS::post("SEMESTER");
        $this->field["GRADE"]           = VARS::post("GRADE");
        $this->field["IBPRG_COURSE"]    = VARS::post("IBPRG_COURSE");
        $this->field["IBSUBCLASSCD"]    = VARS::post("IBSUBCLASSCD");

        for ($subCnt = 1; $subCnt <= $this->subclassCnt; $subCnt++) {
            $this->field["SUBCLASSCD".$subCnt]  = VARS::post("SUBCLASSCD".$subCnt);
            $this->field["CHAIRCD".$subCnt]     = VARS::post("CHAIRCD".$subCnt);
        }

        $this->field["DATE"]            = VARS::post("DATE");
        $this->field["PRINT_SIDE3"]     = VARS::post("PRINT_SIDE3");
        $this->field["DBNAME"]          = VARS::post("DBNAME");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
