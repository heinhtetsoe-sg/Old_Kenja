<?php

require_once('for_php7.php');

class knjh537aModel extends Model {
    var $cmd;
    var $field      = array();
    var $selectdata = array();

    //メンバ変数の初期化
    function knjh537aModel() {
    }

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::request("cmd") == '') {
            unset($this->semester);
            unset($this->proficiencydiv);
            unset($this->proficiencycd);
            unset($this->div);
        }

        //初期値セット
        if ($this->semester == "") {
            $db = Query::dbCheckOut();
            if ($this->semester == "") $this->semester = $db->getOne(knjh537aQuery::getSemester());
            if ($this->proficiencydiv == "") $this->proficiencydiv = $db->getOne(knjh537aQuery::getProficiencyDiv($this->semester));
            if ($this->proficiencycd == "") $this->proficiencycd = $db->getOne(knjh537aQuery::getProficiencyCd($this->semester, $this->proficiencydiv));
            $default = ($this->Properties["usePerfectCourseGroup"] == "1") ? "2" : "1";
            if ($this->div == "") $this->div = $default;
            Query::dbCheckIn($db);
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),
                             "DIV"              =>  VARS::post("DIV"),
                             "PROFICIENCY_SUBCLASS_CD" =>  VARS::post("PROFICIENCY_SUBCLASS_CD"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "COURSE"           =>  VARS::post("COURSE")
                             );

        if (VARS::request("SEMESTER") != "") {
            $this->semester  = VARS::request("SEMESTER");
        }
        if (VARS::request("PROFICIENCYDIV") != "") {
            $this->proficiencydiv  = VARS::request("PROFICIENCYDIV");
        }
        if (VARS::request("PROFICIENCYCD") != "") {
            $this->proficiencycd  = VARS::request("PROFICIENCYCD");
        }
        if (VARS::request("DIV") != "") {
            $this->div  = VARS::request("DIV");
        }

        if (VARS::get("PROFICIENCY_SUBCLASS_CD") != "") {
            $this->proficiency_subclass_cd  = VARS::get("PROFICIENCY_SUBCLASS_CD");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("COURSE") != "") {
            $this->course   = VARS::get("COURSE");
        }

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        if(VARS::get("PROGRAMID")){
            $this->clean();
        }
    }

    //更新
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //PROFICIENCY_SUBCLASS_YDAT削除
        $db->query(knjh537aQuery::deleteProficiencySubclassYdat($this));

        //PROFICIENCY_SUBCLASS_YDAT追加
        knjh537aQuery::insertProficiencySubclassYdatQuery($db, $this->selectdata, $this);

        //値をセット
        $this->proficiency_subclass_cd = $this->field["PROFICIENCY_SUBCLASS_CD"];
        $this->grade    = $this->field["GRADE"];
        $this->course   = $this->field["COURSE"];

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //変数をクリアします
    function clean() {
        unset($this->semester);
        unset($this->proficiencydiv);
        unset($this->proficiencycd);
        unset($this->proficiency_subclass_cd);
        unset($this->div);
        unset($this->grade);
        unset($this->course);
        unset($this->field);
    }
}
?>
