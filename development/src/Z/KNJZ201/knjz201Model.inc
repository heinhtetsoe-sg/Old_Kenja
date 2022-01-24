<?php

require_once('for_php7.php');

class knjz201Model extends Model {
    var $cmd;
    var $control;

    function knjz201Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("ABSENCE_WARN_CHECK"  =>  VARS::post("ABSENCE_WARN_CHECK"),
                             "ABSENCE_WARN_CHECK2" =>  VARS::post("ABSENCE_WARN_CHECK2"),
                             "ABSENCE_WARN_CHECK3" =>  VARS::post("ABSENCE_WARN_CHECK3"),
                             "ABSENCE_WARN"        =>  VARS::post("ABSENCE_WARN"),
                             "ABSENCE_WARN2"       =>  VARS::post("ABSENCE_WARN2"),
                             "ABSENCE_WARN3"       =>  VARS::post("ABSENCE_WARN3")
                             );
    }

    function &getMainModel() {}

    function &getExecModel() {
        $db = Query::dbCheckOut();

        $query = knjz201Query::getRisyuKijun();//一単位あたりの欠課次数の上限値(履修)
        $risyu_kijun = $db->getOne($query);

        $query = knjz201Query::getSyutokuKijun();//一単位あたりの欠課次数の上限値(修得)
        $syutoku_kijun = $db->getOne($query);

        $query = knjz201Query::getRisyuKijunSpecial();//一単位あたりの欠課次数の上限値(履修)
        $risyu_kijun_special = $db->getOne($query);

        $query = knjz201Query::getSyutokuKijunSpecial();//一単位あたりの欠課次数の上限値(修得)
        $syutoku_kijun_special = $db->getOne($query);

        $query = knjz201Query::getSchoolMst();
        $knjSchoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        knjz201Query::executeQuery($risyu_kijun, $syutoku_kijun, $knjSchoolMst, $this);
        knjz201Query::executeQuerySpecial($risyu_kijun_special, $syutoku_kijun_special, $knjSchoolMst, $this);
        knjz201Query::executeQuerySpecial999($this);
        $this->setMessage("MSG202");
    }
}
?>
