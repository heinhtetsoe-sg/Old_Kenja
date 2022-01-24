<?php

require_once('for_php7.php');

class knjz236Model extends Model {
    var $cmd;
    var $subclasscd;        //primary
    var $record_dat_flg = "0";
    var $selectdata;
    var $rightclasscd;  //右画面の教科コンボ
    var $substitution_type_flg;
    var $school_kind;
    var $curriculum_Cd;

    //メンバ変数の初期化
    function knjz236Model() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->subclasscd);
            unset($this->rightclasscd);
            //教育課程用
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_Cd);
            unset($this->rightclasscd);
            
        }
        
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::request("VALUE")) {
                $this->school_kind   = substr(VARS::get("VALUE"),3,1);
                $this->curriculum_Cd = substr(VARS::get("VALUE"),5,1);
                $this->subclasscd    = substr(VARS::get("VALUE"),7,13);
                $this->rightclasscd  = substr(VARS::get("VALUE"),0,2);
            }
        } else {
            if (VARS::request("VALUE")) {
                $this->subclasscd = VARS::request("VALUE");
                $this->rightclasscd = substr(VARS::get("VALUE"),0,2);
            }
        }
        /*if (VARS::request("SUBCLASSCD")) {
            $this->subclasscd = VARS::request("SUBCLASSCD");
            $this->rightclasscd = substr(VARS::get("SUBCLASSCD"),0,2);
        }*/
        if ($this->cmd == "check"){
            $this->selectdata   = VARS::post("selectdata");
        }
        if (strlen(VARS::post("classcd"))) $this->classcd = VARS::post("classcd");

        $this->substitution_type_flg = VARS::post("SUBSTITUTION_TYPE_FLG");

        $this->cmd              = VARS::request("cmd");
        $this->record_dat_flg   = VARS::post("record_dat_flg");
    }

    function &getUpdateModel() {
        knjz236Query::getUpdateQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>