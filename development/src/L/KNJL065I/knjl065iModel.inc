<?php

class knjl065iModel extends Model {

    var $schoolKind;
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjl065iModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->testdiv);
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");

        //入試年度
        $this->examyear = CTRL_YEAR + 1;
        //入試制度
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        //入試区分
        $this->testdiv      = "2"; //B固定
    }

    function getExecModel() {
        $hasData = false;

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl065iQuery::selectMainQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $updQuery = knjl065iQuery::getUpdateBaseDatQuery($this, $row["EXAMNO"]);
            $db->query($updQuery);

            $hasData = true;
        }

        $db->commit();
        Query::dbCheckIn($db);

        if ($hasData) {
            $this->setMessage("MSG202");
        } else {
            $this->setMessage("MSG917", "処理対象のデータがありません。");
        }

    }

}
?>
