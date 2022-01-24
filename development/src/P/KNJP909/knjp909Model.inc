<?php

require_once('for_php7.php');

class knjp909Model extends Model {

    var $cmd;
    var $field = array();
    var $data  = array();

    function init() {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->lastYear = CTRL_YEAR - 1;

        $this->grade         = VARS::post("GRADE");
        $this->hasuuCd       = VARS::post("HASUU_CD");
        $this->hasuuFrikaeCd = VARS::post("HASUU_FURIKAE_CD"); //最終端数振替先
        $this->henkinDate    = VARS::post("HENKIN_DATE");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //更新チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        //前年度の繰越伝票が全て収入伺いに移行出来てるか。
        $query = knjp909Query::getCarryOverIncomeFlgLast($this);
        $cnt1 = $db->getOne($query);
        if ($cnt1 > 0) {
            $this->setWarning("前年度繰越伝票の収入伺移行が完了していません。");
            return false;
        }

        //今年度の繰越伝票が収入伺に移行してるか。一つでも移行してたらエラー
        $query = knjp909Query::getCarryOverIncomeFlg($this);
        $cnt2 = $db->getOne($query);
        if ($cnt2 > 0) {
            $this->setWarning("今年度繰越伝票が収入伺に移行しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //実行
    function &getUpdateModel() {
        if ($this->validateInput()) {

            $setMassage = knjp909Query::getUpdateQuery($this);

            if (!isset($this->warning)){
                $this->setMessage("MSG201", "\\n".$setMassage);
            }
        }

        return;
    }
}
?>
