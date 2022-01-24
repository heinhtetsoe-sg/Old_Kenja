<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje064mModel extends Model {
    var $cmd;
    var $control;
    var $data_div;

    /**
     * メンバ変数の初期化
     */
    function knje064mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル        
        $this->cmd = VARS::request("cmd");
        $this->grade = VARS::post("GRADE");
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ログイン年度
        $loginYear = CTRL_YEAR;
        //ＤＢ接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this->hyouka[10] = 5;
        $this->hyouka[9] = 5;
        $this->hyouka[8] = 5;
        $this->hyouka[7] = 4;
        $this->hyouka[6] = 3;
        $this->hyouka[5] = 3;
        $this->hyouka[4] = 2;
        $this->hyouka[3] = 2;

        $query = knje064mQuery::getStudent($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $this->makeSuugaku($db, $schregNo, $this);
            $this->makeOngaku($db, $schregNo, $this);
            $this->makeBijutu($db, $schregNo, $this);
            $this->makeEigo($db, $schregNo, $this);
            if ($loginYear < "2010") {
                $this->makeKokugo($db, $schregNo, $this);
                $this->makeSyakai($db, $schregNo, $this);
                $this->makeRika($db, $schregNo, $this);
                $this->makeTaiiku($db, $schregNo, $this);
                $this->makeGijutuKatei($db, $schregNo, $this);
            } else {
                $this->makeKokugo2010($db, $schregNo, $this);
                $this->makeSyakai2010($db, $schregNo, $this);
                $this->makeRika2010($db, $schregNo, $this);
                $this->makeTaiiku2010($db, $schregNo, $this);
                $this->makeGijutuKatei2010($db, $schregNo, $this);
            }
        }

        $db->commit();
        //ＤＢ切断
        Query::dbCheckIn($db, $schregNo);

        $this->setMessage("MSG202");
    }

    //国語
    function makeKokugo($db, $schregNo, $this) {
        //020010-国語１：１，２
        //020020-国語２：３，４
        //020030-国語３：５

        //国語１、２
        $kanten[10] = array("A", "A");
        $kanten[9] = array("A", "A");
        $kanten[8] = array("A", "A");
        $kanten[7] = array("A", "B");
        $kanten[6] = array("A", "B");
        $kanten[5] = array("B", "B");
        $kanten[4] = array("B", "B");
        $kanten[3] = array("B", "C");
        $kanten[2] = array("", "");
        $kanten[1] = array("", "");
        $kanten[0] = array("", "");

        //国語３
        $kanten2[10] = array("A");
        $kanten2[9] = array("A");
        $kanten2[8] = array("A");
        $kanten2[7] = array("B");
        $kanten2[6] = array("B");
        $kanten2[5] = array("B");
        $kanten2[4] = array("C");
        $kanten2[3] = array("C");
        $kanten2[2] = array("");
        $kanten2[1] = array("");
        $kanten2[0] = array("");

        //削除
        knje064mQuery::deleteData($db, $schregNo, "02");

        //国語１
        $query = knje064mQuery::getScore($schregNo, "020010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "02", 1, $kanten[$score]);

        //国語２
        $query = knje064mQuery::getScore($schregNo, "020020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "02", 3, $kanten[$score]);

        //国語３
        $query = knje064mQuery::getScore($schregNo, "020030", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "02", 5, $kanten2[$score]);

        //評定
        $query = knje064mQuery::getScoreAvg($schregNo, "('020010', '020020', '020030')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 3 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        knje064mQuery::insData($db, $schregNo, "0299", $this->hyouka[$score]);
    }

    //社会
    function makeSyakai($db, $schregNo, $this) {
        //040040-世界史：１，２
        //040050-日本史：３，４

        $kanten[10] = array("A", "A");
        $kanten[9] = array("A", "A");
        $kanten[8] = array("A", "A");
        $kanten[7] = array("A", "B");
        $kanten[6] = array("A", "B");
        $kanten[5] = array("B", "B");
        $kanten[4] = array("B", "B");
        $kanten[3] = array("B", "C");
        $kanten[2] = array("", "");
        $kanten[1] = array("", "");
        $kanten[0] = array("", "");

        //削除
        knje064mQuery::deleteData($db, $schregNo, "04");

        //世界史
        $query = knje064mQuery::getScore($schregNo, "040040", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "04", 1, $kanten[$score]);

        //日本史
        $query = knje064mQuery::getScore($schregNo, "040050", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "04", 3, $kanten[$score]);

        //評定
        $query = knje064mQuery::getScoreAvg($schregNo, "('040040', '040050')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        knje064mQuery::insData($db, $schregNo, "0499", $this->hyouka[$score]);
    }

    //数学
    function makeSuugaku($db, $schregNo, $this) {
        //060010-数学１：平均（小数点切捨て）を使用して、音楽と同様
        //060020-数学２

        $query = knje064mQuery::getScoreAvg($schregNo, "('060010', '060020')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;

        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "06", $kanten[$score], $this->hyouka[$score]);
    }

    //理科
    function makeRika($db, $schregNo, $this) {
        //080030-物理：平均（小数点切捨て）を使用して、音楽と同様
        //080040-化学

        $query = knje064mQuery::getScoreAvg($schregNo, "('080030', '080040')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;

        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "08", $kanten[$score], $this->hyouka[$score]);
    }

    //音楽
    function makeOngaku($db, $schregNo, $this) {
        //140010-音楽-10

        $query = knje064mQuery::getScore($schregNo, "140010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "10", $kanten[$score], $this->hyouka[$score]);
    }

    //美術
    function makeBijutu($db, $schregNo, $this) {
        //140020-美術-12

        $query = knje064mQuery::getScore($schregNo, "140020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "12", $kanten[$score], $this->hyouka[$score]);
    }

    //保健体育
    function makeTaiiku($db, $schregNo, $this) {
        //160010-保健：平均（小数点切捨て）を使用して、音楽と同様
        //160020-体育

        $query = knje064mQuery::getScoreAvg($schregNo, "('160010', '160020')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;

        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "16", $kanten[$score], $this->hyouka[$score]);
    }

    //技術・家庭
    function makeGijutuKatei($db, $schregNo, $this) {
        //080030-物理：成績のいい方を使用して、音楽と同様-18
        //080040-化学

        $query = knje064mQuery::getScoreMax($schregNo, "('080030', '080040')", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;

        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "18", $kanten[$score], $this->hyouka[$score]);
    }

    //外国語(英語)
    function makeEigo($db, $schregNo, $this) {
        //220010-英語１：１
        //220010、220020-英語１と２の平均（小数点切捨て）を使用：２
        //220020-英語２：３
        //220030-英語３：４

        $kanten[10] = array("A");
        $kanten[9] = array("A");
        $kanten[8] = array("A");
        $kanten[7] = array("B");
        $kanten[6] = array("B");
        $kanten[5] = array("B");
        $kanten[4] = array("C");
        $kanten[3] = array("C");
        $kanten[2] = array("");
        $kanten[1] = array("");
        $kanten[0] = array("");

        //削除
        knje064mQuery::deleteData($db, $schregNo, "22");

        //英語１
        $query = knje064mQuery::getScore($schregNo, "220010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "22", 1, $kanten[$score]);

        //英語１、２
        $query = knje064mQuery::getScoreAvg($schregNo, "('220010', '220020')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "22", 2, $kanten[$score]);

        //英語２
        $query = knje064mQuery::getScore($schregNo, "220020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "22", 3, $kanten[$score]);

        //英語３
        $query = knje064mQuery::getScore($schregNo, "220030", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "22", 4, $kanten[$score]);

        //評定
        $query = knje064mQuery::getScoreAvg($schregNo, "('220010', '220020', '220030')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 3 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        knje064mQuery::insData($db, $schregNo, "2299", $this->hyouka[$score]);
    }

    /****************************/
    /* ↓↓↓ 2010年度版 ↓↓↓ */
    /****************************/

    //国語
    function makeKokugo2010($db, $schregNo, $this) {
        //020010-国語１：１，２
        //020020-国語２：３，４
        //020030-国語３：５(中３)は、国語３の成績。(中１・中２)は、国語１と国語２の平均（小数点切捨て）

        //国語１、２
        $kanten[10] = array("A", "A");
        $kanten[9] = array("A", "A");
        $kanten[8] = array("A", "A");
        $kanten[7] = array("A", "B");
        $kanten[6] = array("A", "B");
        $kanten[5] = array("B", "B");
        $kanten[4] = array("B", "B");
        $kanten[3] = array("B", "C");
        $kanten[2] = array("", "");
        $kanten[1] = array("", "");
        $kanten[0] = array("", "");

        //国語３
        $kanten2[10] = array("A");
        $kanten2[9] = array("A");
        $kanten2[8] = array("A");
        $kanten2[7] = array("B");
        $kanten2[6] = array("B");
        $kanten2[5] = array("B");
        $kanten2[4] = array("C");
        $kanten2[3] = array("C");
        $kanten2[2] = array("");
        $kanten2[1] = array("");
        $kanten2[0] = array("");

        //削除
        knje064mQuery::deleteData($db, $schregNo, "02");

        //国語１
        $query = knje064mQuery::getScore($schregNo, "020010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "02", 1, $kanten[$score]);

        //国語２
        $query = knje064mQuery::getScore($schregNo, "020020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "02", 3, $kanten[$score]);

        //国語３
        $score = 0;
        if ($this->grade == "03") {
            $query = knje064mQuery::getScore($schregNo, "020030", $this);
            $score = $db->getOne($query);
        } else {
            $query = knje064mQuery::getScoreAvg($schregNo, "('020010', '020020')", $this);
            $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        }
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "02", 5, $kanten2[$score]);

        //評定
        if ($this->grade == "03") {
            $query = knje064mQuery::getScoreAvg($schregNo, "('020010', '020020', '020030')", $this);
            $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreData["CNT"] < 3 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
            $score = $score ? $score : 0;
        }
        knje064mQuery::insData($db, $schregNo, "0299", $this->hyouka[$score]);
    }

    //社会
    function makeSyakai2010($db, $schregNo, $this) {
        //040020-社会２：１，２
        //040010-社会１：３，４

        $kanten[10] = array("A", "A");
        $kanten[9] = array("A", "A");
        $kanten[8] = array("A", "A");
        $kanten[7] = array("A", "B");
        $kanten[6] = array("A", "B");
        $kanten[5] = array("B", "B");
        $kanten[4] = array("B", "B");
        $kanten[3] = array("B", "C");
        $kanten[2] = array("", "");
        $kanten[1] = array("", "");
        $kanten[0] = array("", "");

        //削除
        knje064mQuery::deleteData($db, $schregNo, "04");

        //社会２
        $query = knje064mQuery::getScore($schregNo, "040020");
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "04", 1, $kanten[$score]);

        //社会１
        $query = knje064mQuery::getScore($schregNo, "040010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData2($db, $schregNo, "04", 3, $kanten[$score]);

        //評定
        $query = knje064mQuery::getScoreAvg($schregNo, "('040010', '040020')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        knje064mQuery::insData($db, $schregNo, "0499", $this->hyouka[$score]);
    }

    //理科
    function makeRika2010($db, $schregNo, $this) {
        //080010-科学Ａ：科学Ａと科学Ｂの平均（小数点切捨て）
        //080020-科学Ｂ

        $query = knje064mQuery::getScoreAvg($schregNo, "('080010', '080020')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;

        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "08", $kanten[$score], $this->hyouka[$score]);
    }

    //保健体育
    function makeTaiiku2010($db, $schregNo, $this) {
        //160010-保健：(中３)は、保健と体育の平均（小数点切捨て）
        //160020-体育：(中１・中２)は、体育の成績

        $score = 0;
        if ($this->grade == "03") {
            $query = knje064mQuery::getScoreAvg($schregNo, "('160010', '160020')", $this);
            $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        } else {
            $query = knje064mQuery::getScore($schregNo, "160020", $this);
            $score = $db->getOne($query);
        }
        $score = $score ? $score : 0;

        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "16", $kanten[$score], $this->hyouka[$score]);
    }

    //技術・家庭
    function makeGijutuKatei2010($db, $schregNo, $this) {
        //180020-家庭科：(中１)は、家庭科の成績
        //080010-科学Ａ：(中２・中３)は、科学Ａと科学Ｂの成績のいい方
        //080020-科学Ｂ

        $score = 0;
        if ($this->grade == "01") {
            $query = knje064mQuery::getScore($schregNo, "180020", $this);
            $score = $db->getOne($query);
        } else {
            $query = knje064mQuery::getScoreMax($schregNo, "('080010', '080020')", $this);
            $score = $db->getOne($query);
        }
        $score = $score ? $score : 0;

        $kanten[10] = array("A", "A", "A", "A");
        $kanten[9] = array("A", "A", "A", "B");
        $kanten[8] = array("A", "A", "B", "B");
        $kanten[7] = array("A", "B", "B", "B");
        $kanten[6] = array("B", "B", "B", "B");
        $kanten[5] = array("B", "B", "B", "C");
        $kanten[4] = array("B", "B", "C", "C");
        $kanten[3] = array("B", "C", "C", "C");
        $kanten[2] = array("", "", "", "");
        $kanten[1] = array("", "", "", "");
        $kanten[0] = array("", "", "", "");

        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje064mQuery::delInsData($db, $schregNo, "18", $kanten[$score], $this->hyouka[$score]);
    }
}
?>
