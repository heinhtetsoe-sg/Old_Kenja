<?php

require_once('for_php7.php');

class knjp746Model extends Model {

    var $cmd;
    var $field = array();
    var $data  = array();

    function init() {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->schoolKind  = VARS::post("SCHOOL_KIND");
        $this->requestDate = VARS::post("REQUEST_DATE");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //実行
    function &getUpdateModel() {

        $setMassage = knjp746Query::getUpdateQuery($this);

        if (!isset($this->warning)){
            $this->setMessage("MSG201", "\\n".$setMassage);
        }

        return;
    }
}
?>
