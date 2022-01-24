<?php

require_once('for_php7.php');

class knjz219aModel extends Model {

    var $field = array();
    var $cmd;

    function knjz219aModel() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->field = array("SCHKIND"     =>   VARS::post("SCHKIND")  //校種
                            );
    }

    function validateInput($db)
    {
        return true;
    }

    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput($db)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            for ($i = 0; $i < get_count($this->combSubclass); $i++) {
                $combSubclass = $this->combSubclass[$i];//合併先科目コード
                $combClass  = $this->combClass[$i];//合併先教科コード
                $combSchoolkind = $this->combSchoolkind[$i];//合併先学校校種
                $combCurriculumcd = $this->combCurriculumcd[$i];//合併先教育課程コード
                $gvalCalc = VARS::post("GVAL_CALC" .$i);//評定計算方法
                //評定計算方法設定テーブルの存在チェック
                $query = knjz219aQuery::getCntCombGcalcDat(CTRL_YEAR, $combSubclass, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                $dataCnt = $db->getOne($query);
                if (0 < $dataCnt) {
                    //更新
                    $query = knjz219aQuery::getUpdateQuery(CTRL_YEAR, $combSubclass, $gvalCalc, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                    $db->query($query);
                } else {
                    //追加
                    $query = knjz219aQuery::getInsertQuery(CTRL_YEAR, $combSubclass, $gvalCalc, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                    $db->query($query);
                }
                if ($gvalCalc != "3") {
                    $query = knjz219aQuery::getUpdateWeightClearQuery(CTRL_YEAR, $combClass.$combSchoolkind.$combCurriculumcd.$combSubclass, $this);
                    $db->query($query);
                }
            }

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function &getUpdateModelAttend() {
        //入力チェック
        if ($this->validateInput($db)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $this->warnAtte = array();
            $errFlg = false;
            foreach ($this->weight as $combSubclass => $val) {
                $totalWeight = 0;
                foreach ($val as $key => $valAttend) {
                    //合併元科目
                    $attendClass  = $valAttend["ATTEND_CLASSCD"];
                    $attendSchoolkind = $valAttend["ATTEND_SCHOOL_KIND"];
                    $attendCurriculumcd = $valAttend["ATTEND_CURRICULUM_CD"];
                    $attendSubclass = $valAttend["ATTEND_SUBCLASSCD"];
                    //割合
                    $weightSoeji = $combSubclass."_".$attendClass.$attendSchoolkind.$attendCurriculumcd.$attendSubclass;
                    $setWeight = VARS::post("WEIGHTING_{$weightSoeji}");
                    $totalWeight = ($totalWeight * 1) + ($setWeight * 1);
                    $this->warnAtte[$weightSoeji] = $setWeight;
                }
                if ($totalWeight != 1) {
                    $errFlg = true;
                }
            }
            if ($errFlg) {
                $this->setWarning("割合の合算値が、1になるよう調整して下さい。");
                return false;
            }

            foreach ($this->weight as $combSubclass => $val) {
                $totalWeight = 0;
                foreach ($val as $key => $valAttend) {
                    //合併元科目
                    $attendClass  = $valAttend["ATTEND_CLASSCD"];
                    $attendSchoolkind = $valAttend["ATTEND_SCHOOL_KIND"];
                    $attendCurriculumcd = $valAttend["ATTEND_CURRICULUM_CD"];
                    $attendSubclass = $valAttend["ATTEND_SUBCLASSCD"];

                    //割合
                    $weightSoeji = $combSubclass."_".$attendClass.$attendSchoolkind.$attendCurriculumcd.$attendSubclass;
                    $setWeight = VARS::post("WEIGHTING_{$weightSoeji}");

                    //更新
                    $query = knjz219aQuery::getUpdateAttendQuery(CTRL_YEAR, $combSubclass, $attendClass.$attendSchoolkind.$attendCurriculumcd.$attendSubclass, $setWeight, $this);
                    $db->query($query);
                }
            }

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
