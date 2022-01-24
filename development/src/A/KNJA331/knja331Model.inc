<?php

require_once('for_php7.php');


class knja331Model extends Model {

    var $cmd;       //コマンド
    var $year;      //今年度
    var $checkYear; //CONTROL_MSTの年度

    function knja331Model()
    {
    }

    function init()
    {
        $this->year = CTRL_YEAR;
        $this->cmd  = VARS::request("cmd"); //コマンド

        $db = Query::dbCheckOut();
        $this->checkYear   = $db->getOne(knja331Query::getCtrl());  //CONTROL_MSTの年度取得
        Query::dbCheckIn($db);
    }

    //削除のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if (!$this->delCheck()) {
            return false;
        }
        $query = knja331Query::getDeleteQuery($this->year);
        if ($query) {
            $db->query($query);
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //チェック処理
    function delCheck()
    {
        if ($this->checkYear >= $this->year) {
            $this->setWarning("MSG919","コントロールマスタ年度以前の\\n年度は処理できません。");
            return false;
        }
        return true;
    }

}
?>
