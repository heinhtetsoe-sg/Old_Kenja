<?php

require_once('for_php7.php');

class knjp151kModel extends Model {
    var $field = array();
    var $cmd;
    var $sex;
    var $selectdata;

    function knjp151kModel()
    {
    }

    function init()
    {
        $this->cmd  = VARS::request("cmd");
        $this->div  = VARS::request("radiodiv");

        $this->field = array("HRCLASS"      => VARS::post("HRCLASS"),   //年組
                             "SEX"          => VARS::post("SEX"),       //性別
                             "TOTALCD"      => VARS::post("TOTALCD"),   //入金額
                             "REPAY_SEQ"    => VARS::post("REPAY_SEQ"), //返金回数
                             "REPAY_FLG"    => VARS::post("REPAY_FLG")  //返金済
                             );

        $this->selectdata = $this->cmd == "edit_clear" ? "" : VARS::post("selectdata");
    }

    //一括更新
    function &getAllUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = explode(",", $this->selectdata);
        $backUpDiv = $this->div;
        foreach ($data as $schregno) {
            $query = knjp151kQuery::getUpdateRePayQuery($this, $schregno);
            $db->query($query);
        }

        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

}
?>
