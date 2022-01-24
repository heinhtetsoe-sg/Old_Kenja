<?php

require_once('for_php7.php');


class knjp374Model extends Model {

    var $cmd;       //コマンド
    var $year;      //今年度
    var $year_add;  //次年度

    function knjp374Model()
    {
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");

        $this->grade        = VARS::request("GRADE");
        $this->hrClass      = VARS::request("HR_CLASS");
        $this->transferDiv  = VARS::request("TRANSFER_DIV");
        if(VARS::get("SEND_PRGID")){
            $this->grade        = VARS::get("SEND_GRADE");
            $this->hrClass      = VARS::get("SEND_HR_CLASS");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->schregNoArray as $key => $schregNo) {
            $updCheck = VARS::post("CHECKED_{$schregNo}");
            if ($updCheck) {
                $setMoney = VARS::post("TRANSFER_MONEY_{$schregNo}");
                knjp374Query::getInsertQuery($db, $this, $schregNo, $setMoney);
            }
        }

        $this->setMessage("MSG201");
        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
