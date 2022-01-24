<?php

require_once('for_php7.php');

class knjz219Model extends Model {

    var $field = array();
    var $cmd;

    function knjz219Model() {}

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
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $combClass  = $this->combClass[$i];//合併先教科コード
                    $combSchoolkind = $this->combSchoolkind[$i];//合併先学校校種
                    $combCurriculumcd = $this->combCurriculumcd[$i];//合併先教育課程コード
                }
                $gvalCalc = VARS::post("GVAL_CALC" .$i);//評定計算方法
                //評定計算方法設定テーブルの存在チェック
                $query = knjz219Query::getCntCombGcalcDat(CTRL_YEAR, $combSubclass, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                $dataCnt = $db->getOne($query);
                if (0 < $dataCnt) {
                    //更新
                    $query = knjz219Query::getUpdateQuery(CTRL_YEAR, $combSubclass, $gvalCalc, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                    $db->query($query);
                } else {
                    //追加
                    $query = knjz219Query::getInsertQuery(CTRL_YEAR, $combSubclass, $gvalCalc, $combClass, $combSchoolkind, $combCurriculumcd, $this);
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