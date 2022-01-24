<?php

require_once('for_php7.php');

class knjh543aModel extends Model {

    var $field = array();
    var $cmd;

    function knjh543aModel() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),   //実力区分
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),    //実力コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "DIV"              =>  sprintf('%02d', VARS::post("DIV")), //区分(03:コース 04:グループ)
                             "COURSE"           =>  VARS::post("COURSE"),           //課程学科コース
                             "GROUP_CD"         =>  VARS::post("GROUP_CD")          //コースグループ
                             );
        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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
                $gvalCalc = VARS::post("GVAL_CALC" .$i);//計算方法
                //計算方法設定テーブルの存在チェック
                $query = knjh543aQuery::getCntCombGcalcDat($combSubclass, $this);
                $dataCnt = $db->getOne($query);
                if (0 < $dataCnt) {
                    //更新
                    $query = knjh543aQuery::getUpdateQuery($combSubclass, $gvalCalc, $this);
                    $db->query($query);
                } else {
                    //追加
                    $query = knjh543aQuery::getInsertQuery($combSubclass, $gvalCalc, $this);
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
