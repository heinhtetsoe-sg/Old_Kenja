<?php

require_once('for_php7.php');

class knjz213Model extends Model {
    var $field      = array();
    var $leftField  = array();
    var $cmd;
    var $combined_subclasscd;
    var $grade;

    function knjz213Model()
    {
    }

    function init()
    {              
        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "list") {
            $this->leftField = array("CLASSCD"  => VARS::post("CLASSCD"),
                                     "GRADE"    => VARS::post("GRADE")
                                     );
        }
        $this->field = array("SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),
                             "WEIGHTING"        =>  VARS::post("WEIGHTING"),
                             
                             "CLASSCD"          =>  VARS::post("CLASSCD"),
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),
                             "CURRICULUM_CD"    =>  VARS::post("CURRICULUM_CD")
                             );

        $this->getPropertiesAll(); //プロパティファイル        

        if (VARS::get("PROGRAMID")){
            unset($this->combined_subclasscd);
            unset($this->grade);
            unset($this->combined_classcd);
            unset($this->combined_schoolkind);
            unset($this->combined_curriculumcd);
            
        }
        $temp = VARS::get("COMBINED_SUBCLASSCD_SEND");
        if (isset($temp)){
            $this->combined_subclasscd = VARS::get("COMBINED_SUBCLASSCD_SEND");
            $this->grade = VARS::get("GRADE_SEND");
        }
        
        if ($this->Properties["useCurriculumcd"] == '1') {
            $temp2 = VARS::get("COMBINED_CLASSCD_SEND");
            if (isset($temp2)){
                $this->combined_classcd = VARS::get("COMBINED_CLASSCD_SEND");
            }
            $temp3 = VARS::get("COMBINED_SCHOOL_KIND_SEND");
            if (isset($temp3)){
                $this->combined_schoolkind = VARS::get("COMBINED_SCHOOL_KIND_SEND");
            }
            $temp4 = VARS::get("COMBINED_CURRICULUM_CD_SEND");
            if (isset($temp4)){
                $this->combined_curriculumcd = VARS::get("COMBINED_CURRICULUM_CD_SEND");
            }
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->combined_subclasscd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjz213Query::getDeleteQuery($this);
            knjz213Query::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->combined_subclasscd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        knjz213Query::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
