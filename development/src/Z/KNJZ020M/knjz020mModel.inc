<?php

require_once('for_php7.php');

class knjz020mModel extends Model {
    var $field      = array();
    var $cmd;
    var $special_group_cd;
    var $subclasscd;

    function knjz020mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = $_POST;

        $this->test_year = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    /* UPDATE */
    function &update() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = knjz020mQuery::getEntexamCourseMst($this);
        $entexamCourseMst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjz020mQuery::getDeleteQuery($this, $db);
        foreach ($this->field as $key => $val) {
            if (preg_match('/^PERFECT/', $key)) { //普通のマスタを更新
                $subclass_cd = explode("_", $key);
                $perfect = $this->field["PERFECT_{$subclass_cd[1]}"];
                $rate    = $this->field["RATE_{$subclass_cd[1]}"];
                knjz020mQuery::getInsertMstQuery($entexamCourseMst, $subclass_cd[1], $perfect, $rate, $db);
            }
            if (preg_match('/^DETAIL_PERFECT/', $key)) { //詳細マスタを更新
                $detail_cd = explode("_", $key);
                $detail_perfect = $this->field["DETAIL_PERFECT_{$detail_cd[2]}_{$detail_cd[3]}"];
                knjz020mQuery::getInsertDetailQuery($entexamCourseMst, $detail_cd[2], $detail_cd[3], $detail_perfect, $db);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
