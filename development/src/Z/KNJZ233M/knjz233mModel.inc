<?php

require_once('for_php7.php');

class knjz233mModel extends Model {
    var $cmd;
    var $subclasscd;        //primary
    var $selectdata;
    var $rightclasscd;  //右画面の教科コンボ
    var $school_kind;
    var $curriculum_Cd;

    //メンバ変数の初期化
    function knjz233mModel()
    {
    }

    function init()
    {   
        if (VARS::get("PROGRAMID")) {
            unset($this->classcd);
            unset($this->subclasscd);
            unset($this->rightclasscd);
            //教育課程用
            unset($this->school_kind);
            unset($this->curriculum_Cd);
            unset($this->rightclasscd);
        }

        $this->getPropertiesAll(); //プロパティファイル
        
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
        
        if ($this->cmd == "check"){
            $this->selectdata   = VARS::post("selectdata");
        }
        if (strlen(VARS::post("classcd"))) $this->classcd = VARS::post("classcd");

        $this->cmd              = VARS::request("cmd");
        
    }

    function &getUpdateModel()
    {
        knjz233mQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
