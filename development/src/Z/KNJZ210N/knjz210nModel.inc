<?php

require_once('for_php7.php');

class knjz210nModel extends Model {
    var $field = array();
    var $field2 = array();
    var $cmd;
    var $year;
    var $grade;
    var $ibprg_course;
    var $classcd;
    var $school_kind;
    var $curriculum_cd;
    var $subclasscd;

    function knjz210nModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->grade);
            unset($this->ibprg_course);
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
            unset($this->subclasscd);
            $this->field = array();
            $this->field2 = array();
        }

        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', VARS::post("SUBCLASS"));

        $this->field = array("GRADE"            =>  VARS::post("GRADE"),
                             "IBPRG_COURSE"     =>  VARS::post("IBPRG_COURSE"),
                             "CLASSCD"          =>  $classcd,
                             "SCHOOL_KIND"      =>  $school_kind,
                             "CURRICULUM_CD"    =>  $curriculum_cd,
                             "SUBCLASSCD"       =>  $subclasscd
                             );

        $this->field2 = array();
        for ($i = 1; $i <= 5; $i++) {
            $this->field2["CUTTING_HIGH_".$i] =  VARS::post("CUTTING_HIGH_".$i);
        }

        $this->cmd = VARS::request("cmd");

        //年度（初期値）
        $this->year = ($this->year == "") ? CTRL_YEAR : $this->year;

        if (VARS::post("YEAR") != "") {
            $this->year = VARS::post("YEAR");
        }

        //左フレームからのパラメータ
        if(VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if(VARS::get("IBPRG_COURSE") != "") {
            $this->ibprg_course = VARS::get("IBPRG_COURSE");
        }
        if(VARS::get("CLASSCD") != "") {
            $this->classcd = VARS::get("CLASSCD");
        }
        if(VARS::get("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::get("SCHOOL_KIND");
        }
        if(VARS::get("CURRICULUM_CD") != "") {
            $this->curriculum_cd = VARS::get("CURRICULUM_CD");
        }
        if(VARS::get("SUBCLASSCD") != "") {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->year) == "") {
            $this->setWarning("MSG301", "(年度)");
            return false;
        }
        if (trim($this->field["GRADE"]) == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if (trim($this->field["IBPRG_COURSE"]) == "") {
            $this->setWarning("MSG301", "(IBコース)");
            return false;
        }
        if (trim($this->field["CLASSCD"]) == "" || trim($this->field["SCHOOL_KIND"]) == "" || trim($this->field["CURRICULUM_CD"]) == "" || trim($this->field["SUBCLASSCD"]) == "") {
            $this->setWarning("MSG301", "(科目)");
            return false;
        }

        if ($this->cmd == "delete") {
            //キーの更新チェック
            if ($this->grade != $this->field["GRADE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibprg_course != $this->field["IBPRG_COURSE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->classcd != $this->field["CLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->school_kind != $this->field["SCHOOL_KIND"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->curriculum_cd != $this->field["CURRICULUM_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->subclasscd != $this->field["SUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if (!strlen($this->grade) || !strlen($this->ibprg_course) || !strlen($this->classcd) || !strlen($this->school_kind) || !strlen($this->curriculum_cd) || !strlen($this->subclasscd)) {
                $this->setWarning("MSG308");
                return false;
            }
        } else {
            $seq = 0;
            for ($i = 1; $i <= 5; $i++) {
                //必須チェック
                if (trim($this->field2["CUTTING_HIGH_".$i]) == "") {
                    $this->setWarning("MSG301", "(評定".$i.")");
                    return false;
                }
                //範囲チェック
                if ($i != 5) {
                    if (strlen(trim($this->field2["CUTTING_HIGH_".$i])) > 4) {
                        $this->setWarning("MSG916", "(評定段階".$i.")");
                        return false;
                    }
                }
                if (trim($this->field2["CUTTING_HIGH_".$i]) <= $seq) {
                    $this->setWarning("MSG916", "(評定段階".$i.")");
                    return false;
                }
                $seq = $this->field2["CUTTING_HIGH_".$i];
            }
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz210nQuery::getIBViewCuttingDat($this->year, $this->field["GRADE"], $this->field["IBPRG_COURSE"], $this->field["CLASSCD"], $this->field["SCHOOL_KIND"], $this->field["CURRICULUM_CD"], $this->field["SUBCLASSCD"], "", "check");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //対象がなければエラー
        if ($this->cmd == "delete" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            //登録
            knjz210nQuery::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->grade            = $this->field["GRADE"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->classcd          = $this->field["CLASSCD"];
            $this->school_kind      = $this->field["SCHOOL_KIND"];
            $this->curriculum_cd    = $this->field["CURRICULUM_CD"];
            $this->subclasscd       = $this->field["SUBCLASSCD"];

            $this->setCmd("edit");
        } else {
            $this->setCmd("check");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()) {
            //削除
            knjz210nQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");

            unset($this->grade);
            unset($this->ibprg_course);
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
            unset($this->subclasscd);
            unset($this->field);
            unset($this->field2);
        }
    }

    /********/
    /* COPY */
    /********/
    function &getCopyModel() {
        $db = Query::dbCheckOut();

        //存在チェック（前年度）
        $pre_year = $this->year - 1;
        $cnt_pre = $db->getOne(knjz210nQuery::checkIBViewCuttingDat($pre_year));
        if ($cnt_pre == 0) {
            $this->setWarning("MSG917","前年度のデータが存在しません。");
            return false;
        }
        //存在チェック（対象年度）
        $cnt_this = $db->getOne(knjz210nQuery::checkIBViewCuttingDat($this->year));
        if ($cnt_this > 0) {
            $this->setWarning("MSG917","対象年度にデータが存在します。");
            return false;
        }

        Query::dbCheckIn($db);

        //コピー
        knjz210nQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
