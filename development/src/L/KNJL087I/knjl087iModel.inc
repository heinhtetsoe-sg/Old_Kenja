<?php

class knjl087iModel extends Model
{
    public $schoolKind;
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjl087iModel()
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
        if (VARS::post("TESTDIV")) {
            $this->testdiv = VARS::post("TESTDIV");
        }

        //加算点取得(中学:名称マスタL014, 高校:固定値)
        $db = Query::dbCheckOut();

        $this->kasanScore = 0;
        $this->kasanScore = $db->getOne(knjl087iQuery::getKasanScore($this));
        $this->kasanScore = ($this->kasanScore && is_numeric($this->kasanScore)) ? intval($this->kasanScore) : 0;

        Query::dbCheckIn($db);
    }

    public function getExecModel()
    {
        $hasData = false;

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受験科目取得
        $this->testSubclassCdArray = array();
        $query = knjl087iQuery::getTestSubclasscd($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->testSubclassCdArray[] = $row["TESTSUBCLASSCD"];
        }

        //全科目未受験者に欠席フラグを立てる
        knjl087iQuery::getUpdateAttend($this, $db);

        //処理対象のRECEPTNOを取得(1つでも未入力or欠席があれば対象外)
        $query = knjl087iQuery::getReceptSubclassScoreQuery($this, "RECEPT");

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $receptData = array();
            $receptData["RECEPTNO"]      = $row["RECEPTNO"];
            $receptData["TOTAL4"]        = $row["ALL_TESTSUB_SUM"];
            $receptData["AVARAGE4"]      = $row["ALL_TESTSUB_AVG"];
            $receptData["TOTAL_RANK4"]   = $row["ALL_TESTSUB_SUM_RANK"];
            $receptData["SEX_RANK4"]     = $row["ALL_TESTSUB_SUM_SEX_RANK"];
            $receptData["TOTAL1"]        = $row["ALL_TESTSUB_KASAN_SUM"];
            $receptData["AVARAGE1"]      = $row["ALL_TESTSUB_KASAN_AVG"];
            $receptData["TOTAL_RANK1"]   = $row["ALL_TESTSUB_KASAN_SUM_RANK"];
            $receptData["SEX_RANK1"]     = $row["ALL_TESTSUB_KASAN_SUM_SEX_RANK"];

            $query = knjl087iQuery::getUpdateReceptDatQuery($this, $receptData);

            $db->query($query);
        }

        //志願者毎、科目毎の情報用
        $query = knjl087iQuery::getReceptSubclassScoreQuery($this, "TESTSUB");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testSubData = array();
            $testSubData["RECEPTNO"]                      = $row["RECEPTNO"];
            $testSubData["TESTSUBCLASSCD"]                = $row["TESTSUBCLASSCD"];
            $testSubData["TESTSUBCLASS_SCORE_RANK"]       = $row["TESTSUBCLASS_SCORE_RANK"];
            $testSubData["TESTSUBCLASS_SCORE_SEX_RANK"]   = $row["TESTSUBCLASS_SCORE_SEX_RANK"];

            $query = knjl087iQuery::getUpdateEntexamScoreDat($this, $receptno, $testSubcd, $testSubData);
            $db->query($query);

            $hasData = true;
        }

        if ($hasData) {
            //科目毎成績
            $totalData = array();
            $query = knjl087iQuery::getSubclassScoreQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!array_key_exists($row["TESTSUBCLASSCD"], $this->testSubclassCdArray) && $row["TESTSUBCLASSCD"] != 'A') {
                    continue;
                }
                $data3 = array();
                $data3["TESTSUBCLASSCD"]    = $row["TESTSUBCLASSCD"];
                $data3["TOTAL"]             = $row["TESTSUB_SUM"];
                $data3["AVARAGE_MEN"]       = $row["TESTSUB_MEN_AVG"];
                $data3["AVARAGE_WOMEN"]     = $row["TESTSUB_WOMEN_AVG"];
                $data3["AVARAGE_TOTAL"]     = $row["TESTSUB_AVG"];
                $data3["MAX_SCORE"]         = $row["TESTSUB_MAX"];
                $data3["MIN_SCORE"]         = $row["TESTSUB_MIN"];
                $data3["COUNT"]             = $row["TESTSUB_CNT"];
                $data3["COUNT_MEN"]         = $row["TESTSUB_MEN_CNT"];
                $data3["COUNT_WOMEN"]       = $row["TESTSUB_WOMEN_CNT"];
                knjl087iQuery::getUpdateJudgeAvgDatQuery($this, $db, $data3);
            }
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
