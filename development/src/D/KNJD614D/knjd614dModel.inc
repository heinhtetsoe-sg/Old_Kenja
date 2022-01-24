<?php

require_once('../../common/mycalendar.php');
require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd614dModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $firstFlg;  //初期処理フラグ。formの最後で切り替え。
    public $selHR = array();
    public $selOutCol = array();
    public $selBene = array();
    public $selSSup = array();
    public $selSund = array();
    public $selKawi = array();
    //以下Java互換変数
    public $gradeName = "";
    public $outcolHeadMap = array();
    public $beneHeadMap = array();
    public $ssupHeadMap = array();
    public $sundHeadMap = array();
    public $kawiHeadMap = array();
    public $semesMap = array();
    public $psMap = array();

    public $PUTTYPE_OUTCOL = "1";
    public $PUTTYPE_BENE = "2";
    public $PUTTYPE_SSUP = "3";
    public $PUTTYPE_SUND = "4";
    public $PUTTYPE_KAWI = "5";
    
    public $OUTCOL0 = "04-0";
    public $OUTCOL1 = "04-1";
    public $BENE0 = "03-0";
    public $BENE1 = "03-1";
    public $SSUP0 = "02-0";
    public $SSUP1 = "02-1";
    public $SUND0 = "01-0";
    public $SUND1 = "01-1";
    public $KAWI0 = "05-0";
    public $KAWI1 = "05-1";
    

    /**
     * メンバ変数の初期化
     */
    public function knjd614dModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array(
                             "YEAR"                           => CTRL_YEAR,                                  //年度
                             "SETTING_SEQ"                    => VARS::post("SETTING_SEQ"),                  //設定番号
                             "SEMESTER"                       => VARS::post("SEMESTER"),                     //学期コンボ
                             "GRADE"                          => VARS::post("GRADE"),                        //学年
                             "HR_CLASS"                       => VARS::post("HR_CLASS"),                     //クラス
                             "TESTCD"                         => VARS::post("TESTCD"),                       //テスト
                             "CATEGORY_IS_INNER"              => VARS::post("CATEGORY_IS_INNER"),            //学内成績CheckBox
                             "CATEGORY_IS_INNER_G1"           => VARS::post("CATEGORY_IS_INNER_G1"),         //高1CheckBox
                             "CATEGORY_IS_INNER_G1S1"         => VARS::post("CATEGORY_IS_INNER_G1S1"),       //高1素点CheckBox
                             "CATEGORY_IS_INNER_G1SH"         => VARS::post("CATEGORY_IS_INNER_G1SH"),       //高1偏差値CheckBox
                             "CATEGORY_IS_INNER_G1S9"         => VARS::post("CATEGORY_IS_INNER_G1S9"),       //高1評定CheckBox
                             "CATEGORY_IS_INNER_G2"           => VARS::post("CATEGORY_IS_INNER_G2"),         //高2CheckBox
                             "CATEGORY_IS_INNER_G2S1"         => VARS::post("CATEGORY_IS_INNER_G2S1"),       //高2素点CheckBox
                             "CATEGORY_IS_INNER_G2SH"         => VARS::post("CATEGORY_IS_INNER_G2SH"),       //高2偏差値CheckBox
                             "CATEGORY_IS_INNER_G2S9"         => VARS::post("CATEGORY_IS_INNER_G2S9"),       //高2評定CheckBox
                             "CATEGORY_IS_INNER_G3"           => VARS::post("CATEGORY_IS_INNER_G3"),         //高3CheckBox
                             "CATEGORY_IS_INNER_G3S1"         => VARS::post("CATEGORY_IS_INNER_G3S1"),       //高3素点CheckBox
                             "CATEGORY_IS_INNER_G3SH"         => VARS::post("CATEGORY_IS_INNER_G3SH"),       //高3偏差値CheckBox
                             "CATEGORY_IS_INNER_G3S9"         => VARS::post("CATEGORY_IS_INNER_G3S9"),       //高3評定CheckBox
                             "CATEGORY_IS_INNER_9_ALL"        => VARS::post("CATEGORY_IS_INNER_9_ALL"),      //評定合算CheckBox
                             "CATEGORY_IS_INNER_9_ALL12_123"  => VARS::post("CATEGORY_IS_INNER_9_ALL12_123"),    //高1～高2、高1～高3Radio
                             "CATEGORY_IS_OUTER_COLLEGE"      => VARS::post("CATEGORY_IS_OUTER_COLLEGE"),    //日本大学基礎学力到達度テストCheckBox
                             "CATEGORY_IS_PROF_TEST"          => VARS::post("CATEGORY_IS_PROF_TEST"),        //実力テストCheckBox
                             "CATEGORY_IS_BENESSE_TEST"       => VARS::post("CATEGORY_IS_BENESSE_TEST"),     //ベネッセCheckBox
                             "CATEGORY_IS_STUDY_SUP"          => VARS::post("CATEGORY_IS_STUDY_SUP"),        //スタディサプリCheckBox
                             "CATEGORY_IS_SUNDAI"             => VARS::post("CATEGORY_IS_SUNDAI"),           //駿台模試CheckBox
                             "CATEGORY_IS_KAWAI"              => VARS::post("CATEGORY_IS_KAWAI"),            //河合塾模試CheckBox
                             "CATEGORY_IS_QUALIFY"            => VARS::post("CATEGORY_IS_QUALIFY"),          //資格CheckBox
                             "CATEGORY_IS_INNER_8020"         => VARS::post("CATEGORY_IS_INNER_8020"),       //学内成績CheckBox
                             "DATE_FROM"                      => VARS::post("DATE_FROM"),                    //出欠集計日付 FROM
                             "DATE_TO"                        => VARS::post("DATE_TO"),                      //出欠集計日付 TO
                             "SETTING_NAME"                   => VARS::post("SETTING_NAME"),                 //保存設定名称
                             "CATEGORY_SELECTED1"             =>  explode(",", VARS::post("CATEGORY_SELECTED1")),
                             "CATEGORY_SELECTED2"             =>  explode(",", VARS::post("CATEGORY_SELECTED2")),
                             "CATEGORY_SELECTED3"             =>  explode(",", VARS::post("CATEGORY_SELECTED3")),
                             "CATEGORY_SELECTED4"             =>  explode(",", VARS::post("CATEGORY_SELECTED4")),
                             "CATEGORY_SELECTED5"             =>  explode(",", VARS::post("CATEGORY_SELECTED5")),
                             "CATEGORY_SELECTED6"             =>  explode(",", VARS::post("CATEGORY_SELECTED6")),
                             "DBNAME"                         => VARS::post("DBNAME")                        //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();

        if ($this->cmd == "") {  //初期処理だけ
            $this->firstFlg = true;
        }
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //「設定読込」はloadSettingで。
        $this->selHR = array();
        $this->selOutCol = array();
        $this->selBene = array();
        $this->selSSup = array();
        $this->selSund = array();
        $this->selKawi = array();
        $this->outcolHeadMap = array();
        $this->beneHeadMap = array();
        $this->ssupHeadMap = array();
        $this->sundHeadMap = array();
        $this->psMap = array();
        $this->gradeName = "";
        $this->semesMap = array();
    }

    public function clean()
    {
    }

    public function saveSetting()
    {
        if (get_count(VARS::post("selHR")) > 0) {
            $this->selHR = explode(",", VARS::post("selHR"));
        } else {
            $this->selHR = array();
        }
        if (get_count(VARS::post("selOutCol")) > 0) {
            $this->selOutCol = explode(",", VARS::post("selOutCol"));
        } else {
            $this->selOutCol = array();
        }
        if (get_count(VARS::post("selBene")) > 0) {
            $this->selBene = explode(",", VARS::post("selBene"));
        } else {
            $this->selBene = array();
        }
        if (get_count(VARS::post("selSSup")) > 0) {
            $this->selSSup = explode(",", VARS::post("selSSup"));
        } else {
            $this->selSSup = array();
        }
        if (get_count(VARS::post("selSund")) > 0) {
            $this->selSund = explode(",", VARS::post("selSund"));
        } else {
            $this->selSund = array();
        }
        if (get_count(VARS::post("selKawi")) > 0) {
            $this->selKawi = explode(",", VARS::post("selKawi"));
        } else {
            $this->selKawi = array();
        }
        if ($this->validation()) {
            $db = Query::dbCheckOut();
            //重複登録か、チェック
            $srchSeq = $db->getOne(knjd614dQuery::chkRecommendationName($this));
            $insFlg = false;
            if ($srchSeq) {
                //重複ならSEQを取得
                $useSeq = $srchSeq;
            } else {
                $useSeq = $db->getOne(knjd614dQuery::getNextRecommendationSeq($this));
                //重複でないならMAX_SEQ+1で登録
                $insFlg = true;
            }
            Query::dbCheckIn($db);
            if (!$insFlg) {
                knjd614dQuery::delSavedInfo($this);
            }
            knjd614dQuery::updateRecommendation($this, $useSeq);
            for ($sCnt = 2; $sCnt <= 6; $sCnt++) {
                knjd614dQuery::updateMockSelectInfo($this, $useSeq, $sCnt);
            }
            $this->setWarning("MSG201");
            return false;
        }
        return false;
    }

    public function loadSetting()
    {
        if ($this->validation()) {
            $db = Query::dbCheckOut();
            $query = knjd614dQuery::getRecommendationInfo($this);
            $info = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $this->field["SEMESTER"] = $info["SEMESTER"];
            $this->field["GRADE"] = $info["GRADE"];
            $this->field["TESTCD"] = $info["TESTCD"];
            $this->field["CATEGORY_IS_INNER"] = $info["SCHOOL_TEST_FLG1"];
            $this->field["CATEGORY_IS_INNER_G1"] = $info["G1_FLG"];
            $this->field["CATEGORY_IS_INNER_G1S1"] = $info["G1_SCORE_FLG"];
            $this->field["CATEGORY_IS_INNER_G1SH"] = $info["G1_DEV_FLG"];
            $this->field["CATEGORY_IS_INNER_G1S9"] = $info["G1_VALUATION_FLG"];
            $this->field["CATEGORY_IS_INNER_G2"] = $info["G2_FLG"];
            $this->field["CATEGORY_IS_INNER_G2S1"] = $info["G2_SCORE_FLG"];
            $this->field["CATEGORY_IS_INNER_G2SH"] = $info["G2_DEV_FLG"];
            $this->field["CATEGORY_IS_INNER_G2S9"] = $info["G2_VALUATION_FLG"];
            $this->field["CATEGORY_IS_INNER_G3"] = $info["G3_FLG"];
            $this->field["CATEGORY_IS_INNER_G3S1"] = $info["G3_SCORE_FLG"];
            $this->field["CATEGORY_IS_INNER_G3SH"] = $info["G3_DEV_FLG"];
            $this->field["CATEGORY_IS_INNER_G3S9"] = $info["G3_VALUATION_FLG"];
            $this->field["CATEGORY_IS_INNER_9_ALL"] = $info["VALUATION_TOTAL_FLG"];
            $this->field["CATEGORY_IS_INNER_9_ALL12_123"] = $info["G1_G3_FLG"];
            $this->field["CATEGORY_IS_QUALIFY"] = $info["QUALIFIED_FLG"];
            $this->field["CATEGORY_IS_PROF_TEST"] = $info["ALL_FLG"];
            $this->field["CATEGORY_IS_INNER_8020"] = $info["SCHOOL_TEST_FLG2"];
            $this->field["DATE_FROM"] = str_replace("-", "/", $info["ATTEND_SDATE"]);
            $this->field["DATE_TO"] = str_replace("-", "/", $info["ATTEND_EDATE"]);

            $query = knjd614dQuery::getPrevSelectedHR($this);
            $result = $db->query($query);
            $this->selHR = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->selHR[] = $row["GR_HR"];
            }
            $result->free();
            
            $query = knjd614dQuery::getPrevCheckedInfo($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                switch ($row["SEQ"]) {
                    case 2:
                        $this->field["CATEGORY_IS_OUTER_COLLEGE"] = $row["CHECK_FLG"];
                        break;
                    case 3:
                        $this->field["CATEGORY_IS_BENESSE_TEST"] = $row["CHECK_FLG"];
                        break;
                    case 4:
                        $this->field["CATEGORY_IS_STUDY_SUP"] = $row["CHECK_FLG"];
                        break;
                    case 5:
                        $this->field["CATEGORY_IS_SUNDAI"] = $row["CHECK_FLG"];
                        break;
                    case 6:
                        $this->field["CATEGORY_IS_KAWAI"] = $row["CHECK_FLG"];
                        break;
                    default:
                        break;
                }
            }
            $result->free();

            $this->selOutCol = array();
            $this->selBene = array();
            $this->selSSup = array();
            $this->selSund = array();
            $this->selKawi = array();
            $query = knjd614dQuery::getPrevSelectMockInfo($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                switch ($row["SEQ"]) {
                    case 2:
                        $this->selOutCol[] = $row["MOCKCD"];
                        break;
                    case 3:
                        $this->selBene[] = $row["MOCKCD"];
                        break;
                    case 4:
                        $this->selSSup[] = $row["MOCKCD"];
                        break;
                    case 5:
                        $this->selSund[] = $row["MOCKCD"];
                        break;
                    case 6:
                        $this->selKawi[] = $row["MOCKCD"];
                        break;
                    default:
                        break;
                }
            }
            $result->free();

            Query::dbCheckIn($db);
        }
    }

    public function validation()
    {
        if ($this->cmd == "load_setting" || $this->cmd == "del_setting") {
            if ($this->field["SETTING_SEQ"] == "") {
                $this->setWarning("MSG301");
                return false;
            }
        }
        if ($this->cmd == "save_setting") {
            if ($this->field["SEMESTER"] == "学期を設定してください。") {
                $this->setWarning("MSG301");
                return false;
            }
            if ($this->field["GRADE"] == "") {
                $this->setWarning("MSG301", "学年を設定してください。");
                return false;
            }
            if ($this->field["TESTCD"] == "" && strlen($this->field["TESTCD"]) < 6) {
                $this->setWarning("MSG301", "考査種別を設定してください。");
                return false;
            }
            if ($this->field["SETTING_NAME"] == "") {
                $this->setWarning("MSG301", "設定保存名称を設定してください。");
                return false;
            }
        }
        if ($this->cmd == "csv") {
            if (get_count($this->field["CATEGORY_SELECTED1"]) == 0 || $this->field["CATEGORY_SELECTED1"][0] == "") {
                $this->setWarning("MSG916", "対象の年組を選択してください。");
                return false;
            }
            if ($this->field["DATE_FROM"] == "") {
                $this->setWarning("MSG902", "出欠集計日付(FROM)を指定してください。");
                return false;
            }

            if ($this->field["DATE_TO"] == "") {
                $this->setWarning("MSG902", "出欠集計日付(TO)を指定してください。");
                return false;
            }
        }

        return true;
    }

    public function delSetting()
    {
        if ($this->validation()) {
            knjd614dQuery::delSavedInfo($this);
            $this->setWarning("MSG201");
        }
    }

    public function getDownloadModel()
    {
        return knjd614dModel::outputCsv();
    }

    //Java同等処理部分 START
    public function outputCsv()
    {
        $db = Query::dbCheckOut();
        $this->gradeName = $db->getOne(knjd614dQuery::getGradeName($this));
        $this->semesMap = knjd614dModel::getSemesterMap($db);
        if ($this->field["CATEGORY_IS_OUTER_COLLEGE"] == "1") {
            $this->outcolHeadMap = knjd614dModel::getOutColHeadInfo($db);
        } else {
            $this->outcolHeadMap = null;
        }
        if ($this->field["CATEGORY_IS_BENESSE_TEST"] == "1") {
            $this->beneHeadMap = knjd614dModel::getBeneHeadInfo($db);
        } else {
            $this->beneHeadMap = null;
        }
        if ($this->field["CATEGORY_IS_STUDY_SUP"] == "1") {
            $this->ssupHeadMap = knjd614dModel::getSSupHeadInfo($db);
        } else {
            $this->ssupHeadMap = null;
        }
        if ($this->field["CATEGORY_IS_SUNDAI"] == "1") {
            $this->sundHeadMap = knjd614dModel::getSundHeadInfo($db);
        } else {
            $this->sundHeadMap = null;
        }
        if ($this->field["CATEGORY_IS_KAWAI"] == "1") {
            $this->kawiHeadMap = knjd614dModel::getKawiHeadInfo($db);
        } else {
            $this->kawiHeadMap = null;
        }

        $contents = "";
        if (knjd614dModel::validation()) {
            for ($idx = 0; $idx < get_count($this->field["CATEGORY_SELECTED1"]); $idx++) {
                $grHrCd = $this->field["CATEGORY_SELECTED1"][$idx];
                // 推薦名簿データ取得
                $students = array();
                $qualifyMap = array();
                $mockMap = array();
                $result8020 = array();
                $students = knjd614dModel::createStudents($db, $grHrCd);
                $qualifyMap = knjd614dModel::getQualifiedInfo($db, $students);
                $mockMap =  knjd614dModel::getMockScore($db, $students);
                $result8020 = knjd614dModel::getCalcResult82($db, $students);

                // 帳票出力のメソッド
                $contents .= knjd614dModel::outPutPrint($db, $students, $qualifyMap, $mockMap, $result8020);
            }
        }
        Query::dbCheckIn($db);

        if ($contents != "") {
            $filename = CTRL_YEAR."年度_個人成績票(".$this->gradeName.") [成績].csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }
        return false;
    }

    public function getOutColHeadInfo($db)
    {
        $retMap = array();
        $query = knjd614dQuery::getOutColHeadInfoSql();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $addWk["puttype"]          = $row["PUTTYPE"];
            $addWk["mock_Subclass_Cd"] = $row["MOCK_SUBCLASS_CD"];
            $addWk["tscore"]           = $row["TSCORE"];
            $addWk["tdeviation"]       = $row["TDEVIATION"];
            $addWk["tNot2DispFlg"]     = $row["TNOT2DISPFLG"];
            if ($retMap[$addWk["puttype"]] == null) {
                $retMap[$addWk["puttype"]] = array();
            }
            $adObj = $retMap[$addWk["puttype"]];
            $adObj[$addWk["mock_Subclass_Cd"]] = $addWk;
            $retMap[$addWk["puttype"]] = $adObj;
        }
        $result->free();
        return $retMap;
    }
    public function getBeneHeadInfo($db)
    {
        $retMap = array();
        $query = knjd614dQuery::getBeneHeadInfoSql();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $addWk["puttype"]          = $row["PUTTYPE"];
            $addWk["mock_Subclass_Cd"] = $row["MOCK_SUBCLASS_CD"];
            $addWk["tscore"]           = $row["TSCORE"];
            $addWk["tall_Dev"]         = $row["TALL_DEV"];
            $addWk["tgtz"]             = $row["TGTZ"];
            $addWk["tschool_Rank"]     = $row["TSCHOOL_RANK"];
            $addWk["tall_Rank"]        = $row["TALL_RANK"];
            if ($retMap[$addWk["puttype"]] == null) {
                $retMap[$addWk["puttype"]] = array();
            }
            $adObj = $retMap[$addWk["puttype"]];
            $adObj[$addWk["mock_Subclass_Cd"]] = $addWk;
            $retMap[$addWk["puttype"]] = $adObj;
        }
        $result->free();
        return $retMap;
    }
    public function getSSupHeadInfo($db)
    {
        $retMap = array();
        $query = knjd614dQuery::getSSupHeadInfoSql();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $addWk["puttype"]          = $row["PUTTYPE"];
            $addWk["mock_Subclass_Cd"] = $row["MOCK_SUBCLASS_CD"];
            $addWk["tscore"]           = $row["TSCORE"];
            $addWk["tall_Dev"]         = $row["TALL_DEV"];
            $addWk["tgtz"]             = $row["TGTZ"];
            $addWk["tschool_Rank"]     = $row["TSCHOOL_RANK"];
            $addWk["tall_Rank"]        = $row["TALL_RANK"];
            if ($retMap[$addWk["puttype"]] == null) {
                $retMap[$addWk["puttype"]] = array();
            }
            $adObj = $retMap[$addWk["puttype"]];
            $adObj[$addWk["mock_Subclass_Cd"]] = $addWk;
            $retMap[$addWk["puttype"]] = $adObj;
        }
        $result->free();
        return $retMap;
    }
    public function getSundHeadInfo($db)
    {
        $retMap = array();
        $query = knjd614dQuery::getSundHeadInfoSql();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $addWk["puttype"]          = $row["PUTTYPE"];
            $addWk["mock_Subclass_Cd"] = $row["MOCK_SUBCLASS_CD"];
            $addWk["tscore"]           = $row["TSCORE"];
            $addWk["tDeviation"]       = $row["TDEVIATION"];
            $addWk["tRank"]            = $row["TRANK"];
            if ($retMap[$addWk["puttype"]] == null) {
                $retMap[$addWk["puttype"]] = array();
            }
            $adObj = $retMap[$addWk["puttype"]];
            $adObj[$addWk["mock_Subclass_Cd"]] = $addWk;
            $retMap[$addWk["puttype"]] = $adObj;
        }
        $result->free();
        return $retMap;
    }
    public function getKawiHeadInfo($db)
    {
        $retMap = array();
        $query = knjd614dQuery::getKawiHeadInfoSql();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $addWk["puttype"]          = $row["PUTTYPE"];
            $addWk["mock_Subclass_Cd"] = $row["MOCK_SUBCLASS_CD"];
            $addWk["tscore"]           = $row["TSCORE"];
            $addWk["tdeviation"]       = $row["TDEVIATION"];
            $addWk["tNot2DispFlg"]     = $row["TNOT2DISPFLG"];
            $adObj = $retMap[$addWk["puttype"]];
            $adObj[$addWk["mock_Subclass_Cd"]] = $addWk;
            $retMap[$addWk["puttype"]] = $adObj;
        }
        $result->free();
        return $retMap;
    }
    public function outPutPrint($db, $students, $qualifyMap, $mockMap, $result8020)
    {
        $test12Cds = array("1-0101", "1-0102", "1-9900", "2-0201", "2-0202", "2-9900", "3-0301", "3-9900", "9-9900");
        $ttl12TestCd = array("9-9900");
        $test3Cds = array("1-0101", "1-0102", "1-9900");
        $ttl3TestCd = array("9-9900");

        $clsScoreMap = $this->field["CATEGORY_IS_INNER_9_ALL"] == "1" ? knjd614dModel::getClsScore($db) : null;
        //1ページ目

        $svfLine = "";

        $prt1Flg = false;
        //$prt2Flg = false;
        foreach ($students as $schregno => $schInfoList) {
            $clsCdMap = array();
            if (get_count($schInfoList) > 0) {
                $schInf = $schInfoList[get_count($schInfoList) - 1];
                $svfLine .= knjd614dModel::setHead($schInf, $schInfoList);
                $prt1Flg = true;
                $svfLine .= "\n";

                ////高1(※javaとは内部の判定処理順序を入れ替え)
                if ($this->field["CATEGORY_IS_INNER_G1"] == "1") {
                    //高1の学籍番号が登録されている?
                    $prtObj = knjd614dModel::findGradeCd("01", $schInfoList);
                    if ($prtObj == null) {
                        $svfLine .= "高1定期テスト";
                        $svfLine .= "\n";
                    }
                    //////素点
                    if ($this->field["CATEGORY_IS_INNER_G1S1"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("1", "01", $test12Cds, $prtObj, "01", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,";
                            $svfLine .= "素点".$fixStr;
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($test12Cds, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                    //////偏差値
                    ////////平均他
                    if ($this->field["CATEGORY_IS_INNER_G1SH"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("2", "01", $test12Cds, $prtObj, "01", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,,,,,,";
                            $svfLine .= "偏差値,,,,,,,,,,,,,,,,平均,組順位,コース順位,学年順位";
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($test12Cds, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                    //////評定
                    ////////評定平均他
                    if ($this->field["CATEGORY_IS_INNER_G1S9"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("3", "01", $ttl12TestCd, $prtObj, "09", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,,,,,";
                            $svfLine .= "評定,,,,,,,,,,,,,,,,評定平均 高1,評定合計,科目数";
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($ttl12TestCd, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                }
                $svfLine .= "\n";

                ////高2(※javaとは内部の判定処理順序を入れ替え)
                if ($this->field["CATEGORY_IS_INNER_G2"] == "1") {
                    //高2の学籍番号が登録されている？
                    $prtObj = knjd614dModel::findGradeCd("02", $schInfoList);
                    if ($prtObj == null) {
                        $svfLine .= "高2定期テスト";
                        $svfLine .= "\n";
                    }
                    //////素点
                    if ($this->field["CATEGORY_IS_INNER_G2S1"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("1", "02", $test12Cds, $prtObj, "01", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,";
                            $svfLine .= "素点".$fixStr;
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($test12Cds, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                    //////偏差値
                    ////////平均他
                    if ($this->field["CATEGORY_IS_INNER_G2SH"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("2", "02", $test12Cds, $prtObj, "01", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,,,,,,";
                            $svfLine .= "偏差値,,,,,,,,,,,,,,,,平均,組順位,コース順位,学年順位";
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($test12Cds, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                    //////評定
                    ////////評定平均他
                    if ($this->field["CATEGORY_IS_INNER_G2S9"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("3", "02", $ttl12TestCd, $prtObj, "09", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,,,,,";
                            $svfLine .= "評定,,,,,,,,,,,,,,,,評定平均 高2,評定合計,科目数";
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($ttl12TestCd, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                }
                $svfLine .= "\n";

                ////高3(※javaとは内部の判定処理順序を入れ替え)
                if ($this->field["CATEGORY_IS_INNER_G3"] == "1") {
                    //高3の学籍番号が登録されている？
                    $prtObj = knjd614dModel::findGradeCd("03", $schInfoList);
                    if ($prtObj == null) {
                        $svfLine .= "高3定期テスト";
                        $svfLine .= "\n";
                    }
                    //////素点
                    if ($this->field["CATEGORY_IS_INNER_G3S1"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("1", "03", $test3Cds, $prtObj, "01", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,";
                            $svfLine .= "素点".$fixStr;
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($test3Cds, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                    //////偏差値
                    if ($this->field["CATEGORY_IS_INNER_G3SH"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("2", "03", $test3Cds, $prtObj, "01", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,,,,,,";
                            $svfLine .= "偏差値,,,,,,,,,,,,,,,,平均,組順位,コース順位,学年順位";
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($test3Cds, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                    //////評定
                    if ($this->field["CATEGORY_IS_INNER_G3S9"] == "1") {
                        if ($prtObj != null) {
                            $svfLine .= knjd614dModel::putTbl1("3", "03", $ttl3TestCd, $prtObj, "09", $clsCdMap);
                            $svfLine .= "\n";
                        } else {
                            $fixStr = ",,,,,,,,,,,,,,,,,,";
                            $svfLine .= "評定,,,,,,,,,,,,,,,,評定平均 高3,評定合計,科目数";
                            $svfLine .= "\n";
                            $svfLine .= knjd614dModel::blankLine($ttl3TestCd, $fixStr);
                            $svfLine .= "\n";
                        }
                    }
                }

                //高1～n評定合算
                $headLine = "";
                $dataLine = array("", "");
                if ($this->field["CATEGORY_IS_INNER_9_ALL"] == "1") {
                    if ($clsScoreMap != null && $clsScoreMap[$schInf["schregno"]] != null) {
                        $getclsMap = $clsScoreMap[$schInf["schregno"]];
                        $svfLine .= ($this->field["CATEGORY_IS_INNER_9_ALL12_123"] == "2") ? "高1～高3評定合算" : "高1～高2評定合算";
                        $clsCol = 1;
                        $hyoteiTtlCnt = 0;
                        $hyoteiTtlVal = 0;
                        foreach ($getclsMap as $clsCd => $clsPutObj) {
                            $hyoteiAvg = $clsPutObj["cnt"] == 0 ? 0 : round($clsPutObj["valuation"] * 1.0 / ($clsPutObj["cnt"] * 1.0), 1);
                            $hyoteiAvg2 = $clsPutObj["cnt"] == 0 ? 0 : round($clsPutObj["valuation"] * 1.0 / ($clsPutObj["cnt"] * 1.0), 2);
                            $headLine .= ",".$clsPutObj["className"]."評定平均";
                            $dataLine[0] .= ",".sprintf("%3.1f", $hyoteiAvg);
                            $dataLine[1] .= ","."(".sprintf("%3.2f", $hyoteiAvg2).")";
                            $hyoteiTtlCnt += $clsPutObj["cnt"];
                            $hyoteiTtlVal += $clsPutObj["valuation"];
                            $clsCol++;
                        }
                        $headLine .= (14 - $clsCol > 0 ? str_repeat(",", 14 - $clsCol) : "") . ",,"."総評定平均,評定合計,科目数";
                        $dataLine[0] .= (14 - $clsCol > 0 ? str_repeat(",", 14 - $clsCol) : "") . ",,";
                        $dataLine[1] .= (14 - $clsCol > 0 ? str_repeat(",", 14 - $clsCol) : "") . ",,";
                        ////総評定平均
                        $dataLine[0] .= $hyoteiTtlCnt == 0 ? "0.0" : sprintf("%3.1f", round($hyoteiTtlVal * 1.0 / ($hyoteiTtlCnt * 1.0), 1));
                        $dataLine[0] .= ",".$hyoteiTtlVal;
                        $dataLine[0] .= ",".$hyoteiTtlCnt;
                        $dataLine[1] .= "(" . ($hyoteiTtlCnt == 0 ? "0.00" : sprintf("%3.2f", round($hyoteiTtlVal * 1.0 / ($hyoteiTtlCnt * 1.0), 2)).")");
                        $svfLine .= $headLine;
                        $svfLine .= "\n";
                        $svfLine .= $dataLine[0];
                        $svfLine .= "\n";
                        $svfLine .= $dataLine[1];
                        $svfLine .= "\n";
                    }
                }

                if ($prt1Flg) {
                    $svfLine .= "\n";
                }

                //2ページ目
                if ($this->field["CATEGORY_IS_PROF_TEST"] == "1" && $mockMap[$schregno] != null) {
                    $mockDatMap = $mockMap[$schregno];
                    //2ページ目
                    //日大基礎学力テスト
                    if ($this->field["CATEGORY_IS_OUTER_COLLEGE"] == "1") {
                        $svfLine .= knjd614dModel::putTbl2($this->PUTTYPE_OUTCOL, $mockDatMap);
                    }

                    //進研模試・総合学力テスト
                    if ($this->field["CATEGORY_IS_BENESSE_TEST"] == "1") {
                        $svfLine .= knjd614dModel::putTbl2($this->PUTTYPE_BENE, $mockDatMap);
                    }

                    //スタディサポート
                    if ($this->field["CATEGORY_IS_STUDY_SUP"] == "1") {
                        $svfLine .= knjd614dModel::putTbl2($this->PUTTYPE_SSUP, $mockDatMap);
                    }

                    //駿台模試
                    if ($this->field["CATEGORY_IS_SUNDAI"] == "1") {
                        $svfLine .= knjd614dModel::putTbl2($this->PUTTYPE_SUND, $mockDatMap);
                    }

                    //河合塾
                    if ($this->field["CATEGORY_IS_KAWAI"] == "1") {
                        $svfLine .= knjd614dModel::putTbl2($this->PUTTYPE_KAWI, $mockDatMap);
                    }
                }

                //資格
                if ($this->field["CATEGORY_IS_QUALIFY"] == "1") {
                    if ($qualifyMap[$schInf["schregno"]] != null) {
                        $svfLine .= "資格";
                        $svfLine .= "\n";
                        //$prt2Flg = true;
                        $putList = $qualifyMap[$schInf["schregno"]];
                        $idxNo = 0;
                        foreach ($putList as $qObj) {
                            $idxNo++;
                            $dataLine = "";
                            $dataLine .= ",".$idxNo;
                            $arr = common::dateconv3($qObj["regddate"]);
                            $dataLine .= ",".($qObj["regddate"] == null ? "" : $arr["wy"]."年".$arr["m"]."月".$arr["d"]."日");
                            $dataLine .= ",".$qObj["qualified_Name"];
                            $dataLine .= ",".$qObj["promoter"];
                            $dataLine .= ",".$qObj["rankname"];
                            $dataLine .= ",".$qObj["score"];
                            $dataLine .= ",".$qObj["remark"];
                            $svfLine .= $dataLine;
                            $svfLine .= "\n";
                        }
                        $svfLine .= "\n";
                    }
                }
                //総合成績
                if ($this->field["CATEGORY_IS_INNER_8020"] == "1") {
                    if ($result8020[$schInf["schregno"]] != null) {
                        $svfLine .= "総合成績";
                        $svfLine .= "\n";
                        $svfLine .= ","."評定平均".",".($result8020[$schInf["schregno"]]["test_Valuation_Avg"] == null ? "" : sprintf("%3.2f", round($result8020[$schInf["schregno"]]["test_Valuation_Avg"], 2)));
                        $svfLine .= "\n";
                        $put2BD = $result8020[$schInf["schregno"]]["test_Valuation_Percent_Score"] == null ? "" : round($result8020[$schInf["schregno"]]["test_Valuation_Percent_Score"], 2);
                        $svfLine .= ","."評定".$result8020[$schInf["schregno"]]["percentage"]."％".",".($result8020[$schInf["schregno"]]["test_Valuation_Percent_Score"] == null ? "" : sprintf("%3.2f", $put2BD));
                        $svfLine .= "\n";
                        $put3BD = $result8020[$schInf["schregno"]]["mock_Total_Percent_Score"] == null ? "" : round($result8020[$schInf["schregno"]]["mock_Total_Percent_Score"], 2);
                        $svfLine .= ","."評定".(100 - intval($result8020[$schInf["schregno"]]["percentage"]))."％".",".($result8020[$schInf["schregno"]]["mock_Total_Percent_Score"] == null ? "" : sprintf("%3.2f", $put3BD));
                        $svfLine .= "\n";
                        $svfLine .= ","."総合成績".",".($put3BD == "" && $put3BD == "" ? "" : sprintf("%3.2f", ($put2BD + $put3BD)));
                        $svfLine .= "\n";
                        $svfLine .= "\n";
                    }
                }
                $svfLine .= "\n";
            }
        }
        return $svfLine;
    }

    private function blankLine($testXCds, $fixStr)
    {
        $testTitleLst = array("１学期中間","１学期期末","１学期成績","２学期中間","２学期期末","２学期成績","３学期期末","３学期成績","年度成績");
        $idx = 0;
        $retLine = "";
        if (get_count($testXCds) == 1) {
            $idx = get_count($testTitleLst) - 1;
        }
        foreach ($testXCds as $ttlStr) {
            $retLine .= $testTitleLst[$idx].$fixStr;
            $retLine .= "\n";
            $idx++;
        }
        return $retLine;
    }

    public function putTbl1($prtType, $grdCd, $testCdStr1, $prtObj, $getScoreDiv, &$clsCdMap)
    {
        $testTitleLst = array("１学期中間","１学期期末","１学期成績","２学期中間","２学期期末","２学期成績","３学期期末","３学期成績","年度成績");

        $grdIdx = intval($grdCd);

        $svfLine = "";

        if ($prtType == "1") {
            $svfLine .= "高".$grdIdx."定期テスト";
            $svfLine .= "\n";
        }
        $printHeadFlg = false;
        $tcCdIdx = 0;
        $ttlVal = 0.0;
        $ttlCnt = 0;

        if (get_count($testCdStr1) == 1) {
            $tcCdIdx = get_count($testTitleLst) - 1;
        }

        $headLine = "";
        if ($prtType == "1") {
            $headLine .= "素点";
            if (get_count($prtObj["subclsMap"]) == 0) {
                $headLine .= ",,,,,,,,,,,,,,";
            }
        } elseif ($prtType == "2") {
            $headLine .= "偏差値";
            if (get_count($prtObj["subclsMap"]) == 0) {
                $headLine .= ",,,,,,,,,,,,,,";
            }
        } elseif ($prtType == "3") {
            $headLine .= "評定";
            if (get_count($prtObj["subclsMap"]) == 0) {
                $headLine .= ",,,,,,,,,,,,,,";
            }
        }
        $subclsNameCsv = array("", "", "", "", "", "", "", "", "", "", "", "", "", "");
        foreach ($testCdStr1 as $testCds1Org) {
            $testCds1 = $testCds1Org.$getScoreDiv;
            $dataLine = "";
            $avgLine = "";
            $ttlLine = "";
            $testDataCsv = array("", "", "", "", "", "", "", "", "", "", "", "", "", "");
            if ($prtObj["scoreMap"][$testCds1] != null) {
                $sc1SubMap = $prtObj["scoreMap"][$testCds1];
                $scCnt = 0;
                foreach ($prtObj["subclsMap"] as $subclsCd => $scObj) {
                    $scCnt++;
                    if (!$printHeadFlg) {
                        $subclsNameCsv[$scCnt - 1] = $scObj["subclassAbbv"];
                    }
                    $scrObj = $sc1SubMap[$subclsCd];
                    if ($prtType == "1") {
                        if ($sc1SubMap[$subclsCd] != null) {
                            $testDataCsv[$scCnt - 1] = $scrObj["score"];
                        }
                    } elseif ($prtType == "2") {
                        if ($sc1SubMap[$subclsCd] != null) {
                            if ($scrObj["grade_Deviation"] != null) {
                                $testDataCsv[$scCnt - 1] = sprintf("%3.2f", floatval($scrObj["grade_Deviation"]));
                            }
                        }
                    } elseif ($prtType == "3") {
                        if ($sc1SubMap[$subclsCd] != null) {
                            if ($scrObj["score"] != "") {
                                $rcalWk = sprintf("%3.2f", round(floatval($scrObj["score"]), 2));
                                $testDataCsv[$scCnt - 1] .= $rcalWk;
                                $ttlVal += $rcalWk;
                                $ttlCnt++;

                                //ページ最後の評定合計の集計
                                if ($this->field["CATEGORY_IS_INNER_9_ALL12_123"] == "2" || ($this->field["CATEGORY_IS_INNER_9_ALL12_123"] == "1" && $grdCd != "03")) {
                                    $clsCdKey = $scObj["classcd"]."-".$scObj["school_Kind"];
                                    if ($clsCdMap[$clsCdKey] == null) {
                                        $clsAddWK = array("clsCdKey"=>$clsCdKey, "className"=>$scObj["classname"], "kamokuCnt"=>"0", "ttlHyouka"=>"0");
                                        $clsCdMap[$clsCdKey] = $clsAddWK;
                                    }
                                    $clsWK = $clsCdMap[$clsCdKey];
                                    $clsCdMap[$clsCdKey]["kamokuCnt"]++;
                                    $clsCdMap[$clsCdKey]["ttlHyouka"] += $rcalWk;
                                }
                            }
                        }
                    }
                }
                if (get_count($subclsNameCsv) > get_count($testDataCsv)) {
                    while (get_count($subclsNameCsv) > get_count($testDataCsv)) {
                        $testDataCsv[] = "";
                    }
                }
                if ($prtType == "2") {
                    ////////平均他
                    $ttlSubclsCd = "99-H-99-999999";
                    if ($sc1SubMap[$ttlSubclsCd] != null) {
                        $scrObj = $sc1SubMap[$ttlSubclsCd];
                        $avgLine = sprintf("%3.2f", floatval(round($scrObj["avg"], 2))).",".$scrObj["class_Rank"].",".$scrObj["course_Rank"].",".$scrObj["grade_Rank"];
                    }
                }
                if ($prtType == "3") {
                    ////////評定平均他
                    $ttlAvg = round($ttlVal / ($ttlCnt * 1.0), 2);
                    $ttlLine .= sprintf("%3.2f", $ttlAvg).",".sprintf("%3.2f", $ttlVal).",".$ttlCnt;
                }
                if (!$printHeadFlg) {
                    $svfLine .= $headLine.",".implode(",", $subclsNameCsv);
                    if ($prtType == "2") {
                        $svfLine .= ",,平均,組順位,コース順位,学年順位";
                    } elseif ($prtType == "3") {
                        $svfLine .= ",,"."評定平均 高".$grdIdx.",評定合計,科目数";
                    }
                    $svfLine .= "\n";
                }
                $printHeadFlg = true;
                $svfLine .= $testTitleLst[$tcCdIdx].",".implode(",", $testDataCsv);
                if ($prtType == "2") {
                    $svfLine .= ",,".($avgLine == "" ? ",,," : $avgLine);
                } elseif ($prtType == "3") {
                    $svfLine .= ",,".($ttlLine == "" ? ",," : $ttlLine);
                }
                $svfLine .= "\n";
            } else {
                if (!$printHeadFlg) {
                    $svfLine .= $headLine.",".implode(",", $subclsNameCsv);
                    if ($prtType == "2") {
                        $svfLine .= ",,平均,組順位,コース順位,学年順位";
                    } elseif ($prtType == "3") {
                        $svfLine .= ",,"."評定平均 高".$grdIdx.",評定合計,科目数";
                    }
                    $svfLine .= "\n";
                }
                $printHeadFlg = true;
                $svfLine .= $testTitleLst[$tcCdIdx].",".implode(",", $testDataCsv);
                if ($prtType == "2") {
                    $svfLine .= ",,,,,";
                } elseif ($prtType == "3") {
                    $svfLine .= ",,,,";
                }
                $svfLine .= "\n";
            }
            $tcCdIdx++;
        }
        if (!$printHeadFlg) {
            if ($prtType == "1") {
                $svfLine .= $headLine;
            } elseif ($prtType == "2") {
                $svfLine .= $headLine.",,平均,組順位,コース順位,学年順位";
            } elseif ($prtType == "3") {
                $svfLine .= $headLine.",,"."評定平均 高".$grdIdx.",評定合計,科目数";
            }
        }
        return $svfLine;
    }

    public function putTbl2($prtType, $mockDatMap)
    {
        $svfLine = "";
        $getKey1 = "";
        $getKey2 = "";
        $putTHScoreFlg = 0;
        $putTHDevFlg = 0;
        $putTHTallDevFlg = 0;
        $putTHTRankFlg = 0;
        $putTDScoreFlg = 0;
        $putTDDevFlg = 0;
        $putTDRankFlg = 0;
        $putSHScoreFlg = 0;
        $putSHDevFlg = 0;
        $putSHallDevFlg = 0;
        $putSHGtzFlg = 0;
        $putSHschoolRankFlg = 0;
        $putSHRankFlg = 0;
        $putSDScoreFlg = 0;
        $putSDDevFlg = 0;
        $putSDGtzFlg = 0;
        $putSDschoolRankFlg = 0;
        $putSDRankFlg = 0;
        $filterStrs = null;
        $useHeadMap = null;
        $ttlStr = "";
        if ($prtType == $this->PUTTYPE_OUTCOL) {
            $getKey1 = $this->OUTCOL0;
            $getKey2 = $this->OUTCOL1;

            $putTHScoreFlg = 1;
            $putTHDevFlg = 1;

            $putTDScoreFlg = 1;
            $putTDDevFlg = 1;

            $putSHScoreFlg = 1;
            $putSHDevFlg = 1;
            $putSHRankFlg = 1;

            $putSDScoreFlg = 1;
            $putSDDevFlg = 1;
            $putSDRankFlg = 1;

            $ttlStr = "日大基礎学力テスト";
            $filterStrs = $this->field["CATEGORY_SELECTED2"];
            $useHeadMap = $this->outcolHeadMap;
        } elseif ($prtType == $this->PUTTYPE_BENE) {
            $getKey1 = $this->BENE0;
            $getKey2 = $this->BENE1;

            $putTHScoreFlg = 1;
            $putTHTallDevFlg = 1;

            $putTDScoreFlg = 1;
            $putTDDevFlg = 1;

            $putSHScoreFlg = 1;
            $putSHallDevFlg = 1;
            $putSHGtzFlg = 1;
            $putSHschoolRankFlg = 1;
            $putSHRankFlg = 1;

            $putSDScoreFlg = 1;
            $putSDDevFlg = 1;
            $putSDGtzFlg = 1;
            $putSDschoolRankFlg = 1;
            $putSDRankFlg = 1;

            $ttlStr = "進研模試・総合学力テスト";
            $filterStrs = $this->field["CATEGORY_SELECTED3"];
            $useHeadMap = $this->beneHeadMap;
        } elseif ($prtType == $this->PUTTYPE_SSUP) {
            $getKey1 = $this->SSUP0;
            $getKey2 = $this->SSUP1;

            $putTHScoreFlg = 1;
            $putTHTallDevFlg = 1;

            $putTDScoreFlg = 1;
            $putTDDevFlg = 1;

            $putSHScoreFlg = 1;
            $putSHallDevFlg = 1;
            $putSHGtzFlg = 1;
            $putSHschoolRankFlg = 1;

            $putSDScoreFlg = 1;
            $putSDDevFlg = 1;
            $putSDGtzFlg = 1;
            $putSDschoolRankFlg = 1;

            $ttlStr = "スタディサポート";
            $filterStrs = $this->field["CATEGORY_SELECTED4"];
            $useHeadMap = $this->ssupHeadMap;
        } elseif ($prtType == $this->PUTTYPE_SUND) {
            $getKey1 = $this->SUND0;
            $getKey2 = $this->SUND1;

            $putTHScoreFlg = 1;
            $putTHDevFlg = 1;
            $putTHTRankFlg = 1;

            $putTDScoreFlg = 1;
            $putTDDevFlg = 1;
            $putTDRankFlg = 1;

            $putSHScoreFlg = 1;
            $putSHDevFlg = 1;
            $putSHRankFlg = 1;

            $putSDScoreFlg = 1;
            $putSDDevFlg = 1;
            $putSDRankFlg = 1;

            $ttlStr = "駿台模試";
            $filterStrs = $this->field["CATEGORY_SELECTED5"];
            $useHeadMap = $this->sundHeadMap;
        } elseif ($prtType == $this->PUTTYPE_KAWI) {
            $getKey1 = $this->KAWI0;
            $getKey2 = $this->KAWI1;

            $putTHScoreFlg = 1;
            $putTHDevFlg = 1;

            $putTDScoreFlg = 1;
            $putTDDevFlg = 1;

            $putSHScoreFlg = 1;
            $putSHDevFlg = 1;
            $putSHRankFlg = 1;

            $putSDScoreFlg = 1;
            $putSDDevFlg = 1;
            $putSDRankFlg = 1;

            $ttlStr = "河合塾";
            $filterStrs = $this->field["CATEGORY_SELECTED6"];
            $useHeadMap = $this->kawiHeadMap;
        } else {
            return $svfLine;
        }
        if (get_count($filterStrs) == 0 || $filterStrs[0] == "") {
            return $svfLine;
        }
        $outcolDatMap = null;
        $outcolSumMap = null;
        if ($mockDatMap[$getKey1] != null) {  //データが無ければヘッダ出力する科目が特定できないので、出力しない※選択科目を全て出すと、フォーマットを超えるはず。それは想定外のはず。集計結果も無いはず。
            $outcolDatMap = $mockDatMap[$getKey1];
            if (array_key_exists($getKey2, $mockDatMap)) {
                $outcolSumMap = $mockDatMap[$getKey2];
            }
            //得点表(ヘッダ)
            $chkDExistFlg = false;
            foreach ($outcolDatMap as $mockCd => $mockDetailMap) {
                if (!in_array("".$mockCd, $filterStrs, true)) {
                    continue;
                }
                if (is_array($mockDetailMap) && get_count($mockDetailMap) > 0) {
                    $chkDExistFlg = true;
                    break;
                }
            }
            if ($chkDExistFlg) {
                $svfLine .= $ttlStr;
                $svfLine .= "\n";
            } else {
                return $svfLine;  //出力するデータが無いので、(ヘッダを出力せずに)終了。
            }
            $putHeadMap = getHeadInfo($useHeadMap, "0", $outcolDatMap, $filterStrs);
            $headLine = "";
            foreach ($putHeadMap as $nouse => $headObj) {
                if ($putTHScoreFlg > 0) {
                    $headLine .= ",".$headObj["tscore"];
                }
                if ($putTHDevFlg > 0) {
                    $headLine .= ",".$headObj["tdeviation"];
                }
                if ($putTHTallDevFlg > 0) {
                    $headLine .= ",".$headObj["tallDev"];
                }
                if ($putTHTRankFlg > 0) {
                    $headLine .= ",".$headObj["trank"];
                }
            }
            $svfLine .= $headLine;
            $svfLine .= "\n";

            //得点表(データ)
            foreach ($outcolDatMap as $mockCd => $mockDetailMap) {
                $dataLine = "";
                if (!in_array("".$mockCd, $filterStrs, true)) {
                    continue;
                }
                $putNameFlg = false;
                foreach ($mockDetailMap as $mockSubclsCd => $prtObj) {
                    $srchIdx = array_search($mockSubclsCd, $putHeadMap) + 1;
                    if (!$putNameFlg) {
                        $dataLine .= $prtObj["mockname1"];
                        $putNameFlg = true;
                    }
                    if ($srchIdx > 0) {
                        if ($putTDScoreFlg > 0) {
                            $dataLine .= $prtObj["score"];
                        }
                        if ($putTDDevFlg > 0) {
                            $dataLine .= $prtObj["deviation"];
                        }
                        if ($putTDRankFlg > 0) {
                            $dataLine .= $prtObj["rank"];
                        }
                    }
                }
                $svfLine .= $dataLine;
                $svfLine .= "\n";
            }

            //集計表(ヘッダ)
            $headLine2 = "";
            $putHead2Map = knjd614dModel::getHeadInfo($useHeadMap, "1", $outcolSumMap, $filterStrs);
            foreach (array_values($putHead2Map) as $headObj) {
                if ($putSHScoreFlg > 0) {
                    $headLine2 .= ",".$headObj["tscore"];
                }
                if ($putSHDevFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                    $headLine2 .= ",".$headObj["tdeviation"];
                }
                if ($putSHallDevFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                    $headLine2 .= ",".$headObj["tallDev"];
                }
                if ($putSHGtzFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                    $headLine2 .= ",".$headObj["tGtz"];
                }
                if ($putSHschoolRankFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                    $headLine2 .= ",".$headObj["tSchoolRank"];
                }
                if ($putSHRankFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                    $headLine2 .= ",".$headObj["trank"];
                }
            }
            $svfLine .= $headLine2;
            $svfLine .= "\n";

            //集計表(データ)
            $putCnt2 = $putSDScoreFlg + $putSDDevFlg + $putSDRankFlg;
            foreach ($outcolSumMap as $mockCd => $mockDetailMap) {
                if (!in_array("".$mockCd, $filterStrs, true)) {
                    continue;
                }
                $dataLine2 = "";
                $putNameFlg = false;
                foreach ($mockDetailMap as $mockSubclsCd => $prtObj) {
                    $srchIdx = array_search($mockSubclsCd, $putHead2Map) + 1;
                    $headObj = $putHead2Map[$mockSubclsCd];
                    if (!$putNameFlg) {
                        $dataLine2 .= $prtObj["mockname1"];
                        $putNameFlg = true;
                    }
                    if ($srchIdx > 0) {
                        if ($headObj["tNot2DispFlg"] == "0") {  //ヘッダの_tNot2DispFlgが"1"(河合塾、日大基礎学力テストで2レコード目に1項目だけ出力があるパターン)の場合は、前回の値を利用。
                        }
                        if ($putSDScoreFlg > 0) {
                            $dataLine2 .= ",".$prtObj["score"];
                        }
                        if ($putSDDevFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                            $dataLine2 .= ",".$prtObj["deviation"];
                        }
                        if ($putSDGtzFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                            $dataLine2 .= ",".$prtObj["gtz"];
                        }
                        if ($putSDschoolRankFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                            $dataLine2 .= ",".$prtObj["inner_Rank"];
                        }
                        if ($putSDRankFlg > 0 && $headObj["tNot2DispFlg"] == "0") {
                            $dataLine2 .= ",".$prtObj["rank"];
                        }
                    }
                }
                $svfLine .= $dataLine2;
                $svfLine .= "\n";
            }
        }
        return $svfLine;
    }

    public function getHeadInfo($outcolHeadMap, $putType, $outcolDatMap, $csStrs)
    {
        $retMap = array();
        if (!array_key_exists($putType, $outcolHeadMap)) {
            return $retMap;
        }

        $putTypeMap = $outcolHeadMap[$putType];
        foreach ($outcolDatMap as $mkMockCd => $mockDetailMap) {
            foreach ($mockDetailMap as $mkSubClsCd => $nouseObj) {
                if ($putTypeMap[$mkSubClsCd] != null) {
                    $chkWk = $putTypeMap[$mkSubClsCd];
                    $chkWk["putTitleFlg"] = true;
                }
            }
        }
        foreach (array_values($putTypeMap) as $chkWk) {
            if ($chkWk["putTitleFlg"]) {
                $retMap[$chkWk["mock_Subclass_Cd"]] = $chkWk;
            }
        }
        return $retMap;
    }

    private function findGradeCd($findGrdCd, $schInfoList)
    {
        $retWk = null;
        foreach ($schInfoList as $chkWk) {
            if ($chkWk["gradeCd"] == $findGrdCd) {
                $retWk = $chkWk;
                break;
            }
        }
        return $retWk;
    }

    /**
     * ヘッダ・フッタ部の出力を行う
     * @param svf        帳票オブジェクト
     * @param sudent    帳票出力対象クラスオブジェクト
     */
    private function setHead($schInf, $schInfoList)
    {
        $retStr = "";
        $retStr .= "個人成績票(".$this->gradeName.") [成績]".",".CTRL_DATE;
        $retStr .= "\n";
        $retStr .= "\n";
        $retStr .= "学籍番号".",".$schInf["schregno"].","."氏名".",".$schInf["name"].",".$schInf["nameKana"];
        $retStr .= "\n";

        $retStrWk1 = "種別".",".($schInf["inoutCd"] == "0" ? "二中" : ($schInf["entDiv"] == "2" ? "推薦" : "一般"));
        $retStrWk2 = "性別".",".$schInf["sex"];
        $retStrWk3 = ",";
        $grd = 1;
        $arryAtt = array();
        foreach ($schInfoList as $schObj) {
            $cnctStr = $schObj["hr_Name"].",".intval($schObj["attendno"])."番".","."担任".",".$schObj["staffname"];
            if (intval($schObj["gradeCd"]) == 1) {
                $retStrWk1 .= ",".$cnctStr;
            } elseif (intval($schObj["gradeCd"]) == 2) {
                $retStrWk2 .= ",".$cnctStr;
            } else {
                $retStrWk3 .= ",".$cnctStr;
            }
            $grHr = $schObj["grade"].$schObj["hr_Class"];
            $att = knjd614dQuery::getAttendInfo($this, $schObj["year"], $schObj["schregno"]);
            if ($att != null) {
                $atWk = $att["9-".$schObj["schregno"]];
                $arryAtt[$grd] = ",".$grd.",".intval($atWk["LESSON"]).",".(intval($atWk["SUSPEND"] + $atWk["MOURNING"])).",".intval($atWk["TRANSDAYS"]);
                $arryAtt[$grd] .= ",".intval($atWk["MLESSON"]).",".intval($atWk["SICK"]).",".intval($atWk["PRESENT"]).",".$schObj["attRemark"];
            } else {
                $arryAtt[$grd] = ",,,,,,,,";
            }
            $grd++;
        }
        $retStr .= $retStrWk1;
        $retStr .= "\n";
        $retStr .= $retStrWk2;
        $retStr .= "\n";
        $retStr .= $retStrWk3;
        $retStr .= "\n";
        $retStr .= "\n";

        $retStr .= "出欠状況,学年,授業日数,出席停止・忌引等の日数,留学中の授業日数,出席しなければならない日数,欠席日数,出席日数,備考";
        $retStr .= "\n";
        $grd = 1;
        foreach ($schInfoList as $schObj) {
            $retStr .= $arryAtt[$grd];
            $retStr .= "\n";
            $grd++;
        }
        $retStr .= "\n";
        return $retStr;
    }

    private function getClsScore($db)
    {
        $retMap = array();
        $query = knjd614dQuery::getClsScoreSql($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $addWk = array();
            $addWk["schregNo"]   = $row["SCHREGNO"];
            $addWk["classCd"]    = $row["CLASSCD"];
            $addWk["schoolKind"] = $row["SCHOOL_KIND"];
            $addWk["className"]  = $row["CLASSNAME"];
            $addWk["cnt"]        = $row["CNT"];
            $addWk["valuation"]  = $row["VALUATION"];
            if ($retMap[$addWk["schregNo"]] == null) {
                $retMap[$addWk["schregNo"]] = array();
            }
            $sKey = $addWk["classCd"]."-".$addWk["schoolKind"];
            $adObj = $retMap[$addWk["schregNo"]];
            $adObj[$sKey] = $addWk;
            $retMap[$addWk["schregNo"]] = $adObj;
        }
        $result->free();
        return $retMap;
    }

    private function createStudents($db, $grHrCls)
    {
        $rtnMap = array();
        $sql = knjd614dQuery::getStudentSql($grHrCls);
        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $addWk = array();
            $addWk["schregno"]    = $row["SCHREGNO"];
            $addWk["name"]        = $row["NAME"];
            $addWk["nameKana"]    = $row["NAME_KANA"];
            $addWk["year"]        = $row["YEAR"];
            $addWk["grade"]       = $row["GRADE"];
            $addWk["gradeCd"]     = $row["GRADE_CD"];
            $addWk["grade_Name2"] = $row["GRADE_NAME2"];
            $addWk["hr_Class"]    = $row["HR_CLASS"];
            $addWk["hr_Name"]     = $row["HR_NAME"];
            $addWk["hr_NameAbbv"] = $row["HR_NAMEABBV"];
            $addWk["attendno"]    = $row["ATTENDNO"];
            $addWk["staffname"]   = $row["STAFFNAME"];
            $addWk["sex"]         = $row["SEX"];
            $addWk["attRemark"]   = $row["ATTENDREC_REMARK"];
            $addWk["inoutCd"]     = $row["INOUTCD"];
            $addWk["entDiv"]      = $row["ENT_DIV"];
            $addWk["scoreMap"]    = array();
            $addWk["subclsMap"]   = array();
            if ($rtnMap[$addWk["schregno"]] == null) {
                $rtnMap[$addWk["schregno"]] = array();
            }
            $rtnMap[$addWk["schregno"]] = array_merge($rtnMap[$addWk["schregno"]], array($addWk));
        }
        $result->free();
        knjd614dModel::getSubCls($db, $rtnMap);
        knjd614dModel::getScore($db, $rtnMap);
        return $rtnMap;
    }

    private function getSubCls($db, &$rtnMap)
    {
        foreach ($rtnMap as $schregno => $subList) {
            $years = array();
            foreach ($subList as $schInf) {
                $years[] = $schInf["year"];
            }

            $psKeyStr = "getSubCls";

            if ($this->psMap[$psKeyStr] == null) {
                $sql = knjd614dQuery::getSubClsSql();
                $this->psMap[$psKeyStr] = $sql;
            } else {
                $sql = $this->psMap[$psKeyStr];
            }

            foreach ($years as $year1) {
                $sqlWk = $sql;
                $sqlWk = str_replace("??", $year1, $sqlWk);
                $sqlWk = str_replace("?", $schregno, $sqlWk);
                $result = $db->query($sqlWk);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $addWk = array();
                    $addWk["year"]          = $row["YEAR"];
                    $addWk["classcd"]       = $row["CLASSCD"];
                    $addWk["school_Kind"]   = $row["SCHOOL_KIND"];
                    $addWk["curriculum_Cd"] = $row["CURRICULUM_CD"];
                    $addWk["subclasscd"]    = $row["SUBCLASSCD"];
                    $addWk["classname"]     = $row["CLASSNAME"];
                    $addWk["subclassAbbv"]  = $row["SUBCLASSABBV"];
                    $fstKey = $addWk["classcd"]."-".$addWk["school_Kind"]."-".$addWk["curriculum_Cd"]."-".$addWk["subclasscd"];
                    if (array_search($addWk["year"], $years, true) >= 0) {
                        $rtnMap[$schregno][array_search($addWk["year"], $years, true)]["subclsMap"][$fstKey] = $addWk;
                    }
                }
                $result->free();
            }
        }
    }

    private function getScore($db, &$rtnMap)
    {
        foreach ($rtnMap as $schregno => $subList) {
            $years = array();
            foreach ($subList as $schInf) {
                $years[] = $schInf["year"];
            }

            $psKeyStr = "getScore";

            if ($this->psMap[$psKeyStr] == null) {
                $sql = knjd614dQuery::getScoreSql();
                $this->psMap[$psKeyStr] = $sql;
            } else {
                $sql = $this->psMap[$psKeyStr];
            }

            foreach ($years as $year1) {
                $sqlWk = $sql;
                $sqlWk = str_replace("??", $year1, $sqlWk);
                $sqlWk = str_replace("?", $schregno, $sqlWk);
                $result = $db->query($sqlWk);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $addWk = array();
                    $addWk["year"]            = $row["YEAR"];
                    $addWk["semester"]        = $row["SEMESTER"];
                    $addWk["testkindcd"]      = $row["TESTKINDCD"];
                    $addWk["testitemcd"]      = $row["TESTITEMCD"];
                    $addWk["score_Div"]       = $row["SCORE_DIV"];
                    $addWk["subclscd"]        = $row["SUBCLSCD"];
                    $addWk["score"]           = $row["SCORE"];
                    $addWk["grade_Deviation"] = $row["GRADE_DEVIATION"];
                    $addWk["avg"]             = $row["AVG"];
                    $addWk["class_Rank"]      = $row["CLASS_RANK"];
                    $addWk["course_Rank"]     = $row["COURSE_RANK"];
                    $addWk["grade_Rank"]      = $row["GRADE_RANK"];
                    $fstKey = $addWk["semester"]."-".$addWk["testkindcd"].$addWk["testitemcd"].$addWk["score_Div"];
                    $sndKey = $addWk["subclscd"];
                    if ($rtnMap[$schregno][array_search($addWk["year"], $years, true)]["scoreMap"] == null) {
                        $rtnMap[$schregno][array_search($addWk["year"], $years, true)]["scoreMap"] = array();
                        $rtnMap[$schregno][array_search($addWk["year"], $years, true)]["scoreMap"][$fstKey] = array($sndKey => $addWk);
                    } else {
                        if ($rtnMap[$schregno][array_search($addWk["year"], $years, true)]["scoreMap"][$fstKey] == null) {
                            $rtnMap[$schregno][array_search($addWk["year"], $years, true)]["scoreMap"][$fstKey] = array($sndKey => $addWk);
                        } else {
                            if (array_search($addWk["year"], $years, true) >= 0) {
                                $rtnMap[$schregno][array_search($addWk["year"], $years, true)]["scoreMap"][$fstKey] = array_merge($rtnMap[$schregno][array_search($addWk["year"], $years, true)]["scoreMap"][$fstKey], array($sndKey => $addWk));
                            }
                        }
                    }
                }
                $result->free();
            }
        }
    }

    private function getQualifiedInfo($db, &$rtnMap)
    {
        $qualifiedMap = array();
        $psKeyStr = "getQualifiedInfo";
        foreach ($rtnMap as $schregnoWk => $noUseObj) {
            if ($this->psMap[$psKeyStr] == null) {
                $sql = knjd614dQuery::getQualifiedInfoSql();
                $this->psMap[$psKeyStr] = $sql;
            } else {
                $sql = $this->psMap[$psKeyStr];
            }

            $sqlWk = $sql;
            $sqlWk = str_replace("?", $schregnoWk, $sqlWk);
            $result = $db->query($sqlWk);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $addWk = array();
                $addWk["year"]           = $row["YEAR"];
                $addWk["schregno"]       = $row["SCHREGNO"];
                $addWk["seq"]            = $row["SEQ"];
                $addWk["regddate"]       = $row["REGDDATE"];
                $addWk["qualified_Cd"]   = $row["QUALIFIED_CD"];
                $addWk["qualified_Name"] = $row["QUALIFIED_NAME"];
                $addWk["promoter"]       = $row["PROMOTER"];
                $addWk["rank"]           = $row["RANK"];
                $addWk["rankname"]       = $row["RANKNAME"];
                $addWk["score"]          = $row["SCORE"];
                $addWk["remark"]         = $row["REMARK"];
                if ($qualifiedMap[$addWk["schregno"]] == null) {
                    $qualifiedMap[$addWk["schregno"]] = array();
                }
                $adWk = $qualifiedMap[$addWk["schregno"]];
                $adWk[] = $addWk;
                $qualifiedMap[$addWk["schregno"]] = $adWk;
            }
            $result->free();
        }
        return $qualifiedMap;
    }

    private function getMockScore($db, $rtnMap)
    {
        $mockDataMap = array();
        foreach ($rtnMap as $schregnoWk => $subWkList) {
            $years = array();
            $prmMaxYear = "";
            $prmMinYear = "";
            
            foreach ($subWkList as $schInf) {
                $years[] = $schInf["year"];
                if ($prmMaxYear == "") {
                    $prmMaxYear = $schInf["year"];
                    $prmMinYear = $schInf["year"];
                } else {
                    if ($prmMaxYear < $schInf["year"]) {
                        $prmMaxYear = $schInf["year"];
                    }
                    if ($prmMinYear > $schInf["year"]) {
                        $prmMinYear = $schInf["year"];
                    }
                }
            }

            $psKeyStr = "getMockScore";
            if ($this->psMap[$psKeyStr] == null) {
                $sql = knjd614dQuery::getMockScoreSql(years);
                $this->psMap[$psKeyStr] = $sql;
            } else {
                $sql = $this->psMap[$psKeyStr];
            }

            $sqlWk = $sql;
            $sqlWk = str_replace("???", $prmMinYear, $sqlWk);
            $sqlWk = str_replace("??", $prmMaxYear, $sqlWk);
            $sqlWk = str_replace("?", $schregnoWk, $sqlWk);
            $result = $db->query($sqlWk);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($years[$row["YEAR"]] == null) {  //留年した年のデータを外す
                    continue;
                }
                $addWk = array();
                $addWk["year"]             = $row["YEAR"];
                $addWk["gyoshatype"]       = $row["GYOSHATYPE"];
                $addWk["mockcd"]           = $row["MOCKCD"];
                $addWk["mockname1"]        = $row["MOCKNAME1"];
                $addWk["schregno"]         = $row["SCHREGNO"];
                $addWk["mosi_Number"]      = $row["MOSI_NUMBER"];
                $addWk["puttype"]          = $row["PUTTYPE"];
                $addWk["mock_Subclass_Cd"] = $row["MOCK_SUBCLASS_CD"];
                $addWk["score"]            = $row["SCORE"];
                $addWk["deviation"]        = $row["DEVIATION"];
                $addWk["gtz"]              = $row["GTZ"];
                $addWk["inner_Rank"]       = $row["INNER_RANK"];
                $addWk["rank"]             = $row["RANK"];

                if ($mockDataMap[$addWk["schregno"]] == null) {
                    $mockDataMap[$addWk["schregno"]] = array();
                }
                $sndKey = $addWk["mosi_Number"]."-".$addWk["puttype"];
                if ($mockDataMap[$addWk["schregno"]][$sndKey] == null) {
                    $mockDataMap[$addWk["schregno"]][$sndKey] = array();
                }
                if ($mockDataMap[$addWk["schregno"]][$sndKey][$addWk["mockcd"]] == null) {
                    $mockDataMap[$addWk["schregno"]][$sndKey][$addWk["mockcd"]] = array();
                }
                $mockDataMap[$addWk["schregno"]][$sndKey][$addWk["mockcd"]][$addWk["mock_Subclass_Cd"]] = $addWk;
            }
            $result->free();
        }
        return $mockDataMap;
    }

    private function getCalcResult82($db, $rtnMap)
    {
        $calcResMap = array();

        foreach ($rtnMap as $schregnoWk => $noUseObj) {
            $psKeyStr = "getCalcResult82";
            if ($this->psMap[$psKeyStr] == null) {
                $sql = knjd614dQuery::getCalcResult82Sql($this);
                $this->psMap[$psKeyStr] = $sql;
            } else {
                $sql = $this->psMap[$psKeyStr];
            }
            $sqlWk = $sql;
            $sqlWk = str_replace("?", $schregnoWk, $sqlWk);
            $result = $db->query($sqlWk);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $addWk["year"] = $row["YEAR"];
                $addWk["schregno"] = $row["SCHREGNO"];
                $addWk["test_Valuation_Avg"] = $row["TEST_VALUATION_AVG"];
                $addWk["test_Valuation_Percent_Score"] = $row["TEST_VALUATION_PERCENT_SCORE"];
                $addWk["mock_Total_Percent_Score"] = $row["MOCK_TOTAL_PERCENT_SCORE"];
                $addWk["percentage"] = $row["PERCENTAGE"];
                $calcResMap[$addWk["schregno"]] = $addWk;
            }
            $result->free();
        }
        return $calcResMap;
    }

    private function getSemesterMap($db)
    {
        $sql = knjd614dQuery::getSemesterMapSql($this);
        $rtn = array();
        $maxStr = "";
        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $wkSems = $row["SEMESTER"];
            if ($maxStr == "") {
                $maxStr = $wkSems;
            } elseif (intval($wkSems) > intval($maxStr)) {
                $maxStr = $wkSems;
            }
            $rtn[$row["YEAR"]] = $row;
        }
        $result->free();
        return $rtn;
    }
    //Java同等処理部分 END
}
