<?php

require_once('for_php7.php');

class knjb3052Model extends Model {
    var $semester;
    var $field      = array();
    var $cmd;
    var $selectData;
    var $control;        //コントロールマスタ

    function knjb3052Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->selectData   = VARS::post("selectData");

        $this->field = array("YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),   //年度 学期
                             "EXECUTEDATE"      =>  VARS::post("EXECUTEDATE"),     //対象日付
                             "OUT_DIV"          =>  VARS::post("OUT_DIV"),         //対象
                             "ORDER_DIV"        =>  VARS::post("ORDER_DIV"),       //出力順
                             "SDATE"            =>  VARS::post("SDATE"),           //学期開始日
                             "EDATE"            =>  VARS::post("EDATE"),           //学期終了日
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
                            );

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //年度内の処理か？チェック
    function checkCtrlDay($sday, $eday, $day) {
        $day = str_replace("/", "-", $day);
        //年度内の処理に限る
        if ($sday <= $day && $day <= $eday) {
            return true;
        } else {
            $sday = str_replace("-", "/", $sday);
            $eday = str_replace("-", "/", $eday);
            $this->setWarning("MSG915","(処理学期範囲外{$sday}～{$eday})");
            return false;
        }
        
    }

    function getUpdateModel() {

        $chairArray = explode(",", $this->selectData);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($chairArray as $key => $chairCd) {
            $query = knjb3052Query::getChairStd($this, $chairCd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $updSql = knjb3052Query::getUpdSql($this, $chairCd, $row);
                $db->query($updSql);
            }
        }
        $db->commit(); // トランザクションをコミットする。
        $this->setMessage('MSG201');

        Query::dbCheckIn($db);
        return;
    }
}
?>
