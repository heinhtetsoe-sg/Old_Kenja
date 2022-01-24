<?php

class knjl660hModel extends Model
{
    public $schoolKind;
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjl660hModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->testdiv);
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array(
            "PLUS_CONFIG" => VARS::post("PLUS_CONFIG")
        );

        //入試年度
        $this->examyear = CTRL_YEAR + 1;
        //入試制度
        $this->applicantdiv = "2";
        //入試区分
        $this->testdiv = VARS::post("TESTDIV");
    }

    public function getExecModel()
    {
        $hasData = false;

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //加点自動設定
        if ($this->field["PLUS_CONFIG"] == "1") {

            //類別マスタに基づいて加点を更新
            $query = knjl660hQuery::selectReceptDat($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $query = knjl660hQuery::getUpdateReceptDetailDatQuery($db, $this, $row);
                $db->query($query);

                $hasData = true;
            }
        }

        //成績集計処理
        $query = knjl660hQuery::getSelectMainQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjl660hQuery::getUpdateReceptDatQuery($this, $row);
            $db->query($query);

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
