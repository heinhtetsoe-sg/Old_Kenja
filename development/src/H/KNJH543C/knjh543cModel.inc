<?php

require_once('for_php7.php');

class knjh543cModel extends Model {
    var $cmd;
    var $field      = array();
    var $selectdata = array();

    //メンバ変数の初期化
    function knjh543cModel() {
    }

    function init() {

        if (VARS::request("cmd") == '') {
            unset($this->semester);
            unset($this->proficiencydiv);
            unset($this->proficiencycd);
            unset($this->div);
        }

        if (VARS::post("cmd") == "list") {
            unset($this->semester);
            unset($this->proficiencydiv);
            unset($this->proficiencycd);
            unset($this->div);
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),
                             "DIV"              =>  VARS::post("DIV"),
                             "PROFICIENCY_SUBCLASS_CD" =>  VARS::post("PROFICIENCY_SUBCLASS_CD"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "COURSE"           =>  VARS::post("COURSE"),
                             "SUBCLASS_CD"      =>  VARS::post("SUBCLASS_CD")
                             );

        if (is_array($this->field["SUBCLASS_CD"])) {
            foreach ($this->field["SUBCLASS_CD"] as $key => $val) {
                $this->field["WEIGHTING".$val] = VARS::post("WEIGHTING".$val);
            }
        }

        if (VARS::get("cmd") == "sel"){
            $this->send_flg                 = VARS::get("SEND_FLG");
            $this->semester                 = VARS::get("SEMESTER");
            $this->proficiencydiv           = VARS::get("PROFICIENCYDIV");
            $this->proficiencycd            = VARS::get("PROFICIENCYCD");
            $this->div                      = VARS::get("DIV");
            $this->proficiency_subclass_cd  = VARS::get("PROFICIENCY_SUBCLASS_CD");
            $this->grade                    = VARS::get("GRADE");
            $this->course                   = VARS::get("COURSE");
        }

        if (VARS::get("cmd") == "sel" || $this->cmd == "clear"){
            $this->field["SEND_FLG"]                = $this->send_flg;
            $this->field["SEMESTER"]                = $this->semester;
            $this->field["PROFICIENCYDIV"]          = $this->proficiencydiv;
            $this->field["PROFICIENCYCD"]           = $this->proficiencycd;
            $this->field["DIV"]                     = $this->div;
            $this->field["PROFICIENCY_SUBCLASS_CD"] = $this->proficiency_subclass_cd;
            $this->field["GRADE"]                   = $this->grade;
            $this->field["COURSE"]                  = $this->course;
        }

        if (VARS::request("SEMESTER")) {
            $this->semester         = VARS::request("SEMESTER");
            $this->proficiencydiv   = VARS::request("PROFICIENCYDIV");
            $this->proficiencycd    = VARS::request("PROFICIENCYCD");
            $this->div              = VARS::request("DIV");
        }

        if (VARS::get("cmd") == "list_update") {
            $this->field["SEMESTER"]                = $this->semester;
            $this->field["PROFICIENCYDIV"]          = $this->proficiencydiv;
            $this->field["PROFICIENCYCD"]           = $this->proficiencycd;
            $this->field["DIV"]                     = $this->div;
            $this->field["PROFICIENCY_SUBCLASS_CD"] = $this->proficiency_subclass_cd;
        }

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        if(VARS::get("PROGRAMID")){
            $this->clean();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //更新
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT削除
        $db->query(knjh543cQuery::deleteProficiencySubclassRepCombDat($this));

        //PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT追加
        knjh543cQuery::insertProficiencySubclassRepCombDatQuery($db, $this->selectdata, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        $this->send_flg = "A_SUB";

        return;
    }

    //変数をクリアします
    function clean() {
        unset($this->send_flg);
        unset($this->semester);
        unset($this->proficiencydiv);
        unset($this->proficiencycd);
        unset($this->proficiency_subclass_cd);
        unset($this->div);
        unset($this->grade);
        unset($this->course);
        unset($this->field);
    }

    //編集完了用のモデルを返します
    function &getUpdateModelWeight()
    {
        knjh543cQuery::getUpdWeight($this);
        $this->setMessage("MSG201");
    }
}
?>
