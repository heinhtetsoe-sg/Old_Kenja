<?php

class knjl560iModel extends Model
{
    public $schoolKind;
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjl560iModel()
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
        $this->applicantdiv = "2";
        //入試区分
        $this->testdiv  = VARS::post("TESTDIV");

        $db = Query::dbCheckOut();

        //試験科目数取得
        $this->testsubNum = $db->getOne(knjl560iQuery::getTestSubclassNum($this));

        Query::dbCheckOut($db);
    }

    public function getExecModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //RECEPT更新(欠席者以外)
        $query  = knjl560iQuery::getSelectQuery($this);
        $updCnt =  $this->updateInsertReceptDat($db, $query);

        //RECEPT更新(欠席者)
        $query  = knjl560iQuery::getSelectKessekiQuery($this);
        $updKessekiCnt =  $this->updateInsertReceptDat($db, $query);

        //統計情報更新
        if ($updCnt > 0) { //欠席者以外のデータがある場合のみ更新(欠席者のデータには成績情報が無いため)
            //全削除
            $query = knjl560iQuery::getDeleteAllToukeiQuery($this);
            $db->query($query);
            $toukeiData = array();
            $query = knjl560iQuery::getSelectQuery($this, "TOKEI");
            $result = $db->query($query);
            while ($toukeiData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                //更新 ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT
                $query = knjl560iQuery::getUpdateToukeiQuery($this, $toukeiData);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        if ($updCnt + $updKessekiCnt > 0) {
            $this->setMessage("MSG202");
        } else {
            $this->setMessage("MSG917", "集計対象のデータがありません。");
        }
    }

    //RECEPT更新関数
    public function updateInsertReceptDat($db, $query)
    {
        $count = 0;
        $result = $db->query($query);
        while ($receptData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //更新 ENTEXAM_RECEPT_DAT
            $query = knjl560iQuery::getUpdateReceptDatQuery($db, $this, $receptData);
            $db->query($query);

            $count++;
        }

        return $count;
    }
}
