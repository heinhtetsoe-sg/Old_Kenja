<?php

require_once('for_php7.php');

class knjl760hModel extends Model
{
    public $schoolKind;
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjl760hModel()
    {
    }

    public function init()
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
        $this->testdiv = VARS::post("TESTDIV");
    }

    public function getExecModel()
    {
        $hasData = false;

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl760hQuery::getReceptDataQuery($this);
        $result = $db->query($query);
        $receptData = array();
        while ($receptData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //総合点算出
            $receptData["SOUGOU_POINT"] = $this->getSougouPoint($receptData);

            //データカウント(ENTEXAM_RECEPT_DETAIL_DAT)
            $seq = "009";
            $getCount = $db->getOne(knjl760hQuery::getCountReceptDetaildata($this, $receptData["RECEPTNO"], $seq));
    
            //更新 ENTEXAM_RECEPT_DETAIL_DAT
            $query = knjl760hQuery::getUpdateReceptDetailDatQuery($this, $receptData, $seq, $getCount);
            $db->query($query);

            $hasData = true;
        }

        //順位更新
        $query = knjl760hQuery::getSelectReceptRankQuery($this);
        $result = $db->query($query);
        $rankData = array();
        while ($rankData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //更新 ENTEXAM_RECEPT_DAT
            $query = knjl760hQuery::getUpdateReceptRankQuery($this, $rankData);
            $db->query($query);

            $hasData = true;
        }

        //統計情報更新
        //全削除
        $query = knjl760hQuery::getDeleteAllToukeiQuery($this);
        $db->query($query);
        $toukeiData = array();
        //追加
        $query = knjl760hQuery::getSelectToukeiQuery($this);
        $result = $db->query($query);
        while ($toukeiData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //更新 ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT
            $query = knjl760hQuery::getUpdateToukeiQuery($this, $toukeiData);
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

    //総合点取得
    public function getSougouPoint($receptData)
    {
        $sougouPoint = 0;
        if ($this->applicantdiv == "1") { //中学一般
            $sougouPoint += $receptData["HIKKI_SCORE_SUM"];
        } else {
            if ($this->testdiv == "01") { //高校推薦
                $sougouPoint += $receptData["TOTAL_ALL"];
                $sougouPoint += $receptData["QUALIFIED_PLUS_POINT"];
                $sougouPoint += $receptData["ESSAY_SCORE_AVG"];
            } else { //高校一般
                $sougouPoint += $receptData["QUALIFIED_PLUS_POINT"];
                $sougouPoint += $receptData["HIKKI_SCORE_SUM"];
                $sougouPoint += $receptData["RECOM_PLUS_POINT"];
            }
        }

        return $sougouPoint;
    }
}
