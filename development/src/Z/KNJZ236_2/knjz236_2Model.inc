<?php

require_once('for_php7.php');

class knjz236_2Model extends Model {

    var $cmd;
    var $sub_subclass;        //primary
    var $att_subclass;        //primary
    var $selectdata;
    //教育課程用
    var $sub_class;
    var $att_class;
    var $sub_schoolkind;
    var $att_schoolkind;
    var $sub_curriculumcd;
    var $att_curriculumcd;
    //親画面からのパラメータ（代替先科目）
    var $param_subclass;
    var $param_class;
    var $param_schoolkind;
    var $param_curriculumcd;
    

    //メンバ変数の初期化
    function knjz236_2Model() {}

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID") != "") {
            unset($this->sub_subclass);
            unset($this->att_subclass);

            unset($this->param_subclass);
            unset($this->param_class);
            unset($this->param_schoolkind);
            unset($this->param_curriculumcd);
            //親画面からのパラメータ（代替先科目）
            if (VARS::get("PARAM_SUBCLASSCD")) {
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $this->param_subclass     = substr(VARS::get("PARAM_SUBCLASSCD"),7,13);
                    $this->param_class        = substr(VARS::get("PARAM_SUBCLASSCD"),0,2);
                    $this->param_schoolkind   = substr(VARS::get("PARAM_SUBCLASSCD"),3,1);
                    $this->param_curriculumcd = substr(VARS::get("PARAM_SUBCLASSCD"),5,1);
                } else {
                    $this->param_subclass = VARS::get("PARAM_SUBCLASSCD");
                }
            }
        }
        
        if (VARS::get("SUBSTITUTION_SUBCLASSCD")) {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->sub_subclass     = substr(VARS::get("SUBSTITUTION_SUBCLASSCD"),7,13);
                $this->sub_class        = substr(VARS::get("SUBSTITUTION_SUBCLASSCD"),0,2);
                $this->sub_schoolkind   = substr(VARS::get("SUBSTITUTION_SUBCLASSCD"),3,1);
                $this->sub_curriculumcd = substr(VARS::get("SUBSTITUTION_SUBCLASSCD"),5,1);                
            } else {
                $this->sub_subclass = VARS::get("SUBSTITUTION_SUBCLASSCD");
            }
        }
        if (VARS::get("ATTEND_SUBCLASSCD")) {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->att_subclass     = substr(VARS::get("ATTEND_SUBCLASSCD"),7,13);
                $this->att_class        = substr(VARS::get("ATTEND_SUBCLASSCD"),0,2);
                $this->att_schoolkind   = substr(VARS::get("ATTEND_SUBCLASSCD"),3,1);
                $this->att_curriculumcd = substr(VARS::get("ATTEND_SUBCLASSCD"),5,1);                
            } else {
                $this->att_subclass = VARS::get("ATTEND_SUBCLASSCD");
            }
        }

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
    }

    function &getUpdateModel()
    {
        knjz236_2Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>