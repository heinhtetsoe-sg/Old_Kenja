<?php

require_once('for_php7.php');

class knjl771hModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $testdiv;

    /**
     * メンバ変数の初期化
     */
    public function knjl771hModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->examyear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");

        //入試年度
        $this->examyear = CTRL_YEAR + 1;
        //学校種別
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        //入試区分
        $this->testdiv = VARS::post("TESTDIV");
    }

    public function getExecModel()
    {
        $hasData = false;

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //順位更新
        $query = knjl771hQuery::getSelectReceptRankQuery($this);
        $result = $db->query($query);
        while ($rankData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //更新 ENTEXAM_RECEPT_DETAIL_DAT
            $query = knjl771hQuery::getUpdateReceptDetailDatQuery($db, $this, $rankData);
            $db->query($query);

            //更新 ENTEXAM_APPLICANTBASE_DAT
            $query = knjl771hQuery::getUpdateBaseDatQuery($this, $rankData);
            $db->query($query);

            $hasData = true;
        }

        $db->commit();
        Query::dbCheckIn($db);

        if ($hasData) {
            $this->setMessage("MSG202");
        } else {
            $this->setMessage("MSG917", "集計対象のデータがありません。");
        }
    }
}
