<?php

require_once('for_php7.php');

class knjz439Model extends Model {

    var $field = array();
    var $data  = array();
    var $cmd;

    function knjz439Model() {}

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("CURRICULUM_CD" => VARS::post("CURRICULUM_CD")
                            );

        $this->cmd = VARS::request("cmd");
    }

    function validateInput()
    {
        return true;
    }

    function &getUpdateModel() {
        //配列
        $term = array("UPDATE_DATA","CREDITS");
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SUBCLASSCD"]); $i++) {
            for ($j = 0; $j < get_count($term); $j++) {
                $term_name = $term[$j]."-".$i;
                $this->fields[$term[$j]][] = VARS::post($term_name);
            }
        }

        if ($this->validateInput()) {
            knjz439Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
