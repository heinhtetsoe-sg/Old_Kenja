<?php

require_once('for_php7.php');

class knjd107Model extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    function knjd107Model() {}

    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        $this->semester    = VARS::post("SEMESTER");
        $this->hrClass     = VARS::post("GRADE_HRCLASS");

        $this->selectdata  = VARS::post("selectdata");    //対象者一覧
        $this->selectdata2 = VARS::post("selectdata2");   //生徒一覧

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();

        $db = Query::dbCheckOut();
        $query = knjd107Query::getSchregRegdGdat();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->field["JORETU_DIV{$row["GRADE"]}"] = VARS::post("JORETU_DIV{$row["GRADE"]}");
        }
        Query::dbCheckIn($db);
    }
}
?>
