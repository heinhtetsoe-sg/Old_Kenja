<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd615iModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd615iModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "MAJOR"                =>  VARS::post("MAJOR"),                //学科
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "PRINT_DIV"            =>  VARS::post("PRINT_DIV"),            //出力方法
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "SORT_DIV"             =>  VARS::post("SORT_DIV"),             //出力順
                             "RYO_ONLY"             =>  VARS::post("RYO_ONLY"),             //寮生のみ
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

    }

    function clean() {}

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //初期化
        $contents  = "";

        /****************************/
        /*  タイトル部分データ取得  */
        /****************************/
        //年度（和暦）
        $nendo = common::DateConv1($this->field["YEAR"]."/04/01",10);

        //出力年月日
        $ctrlDate = common::DateConv1(str_replace('-', '/', CTRL_DATE), 0);

        //学年名称取得
        $gradeName = $db->getOne(knjd615iQuery::getGradeName($this->field["GRADE"]));

        //学期名取得
        $semeName = $db->getOne(knjd615iQuery::getSemester($this, "meisyou"));

        //テスト名取得
        $testName = $db->getOne(knjd615iQuery::getTest($this, $this->field["SEMESTER"], $this->field["GRADE"], "meisyou"));

        /**********************/
        /*  タイトル部分出力  */
        /**********************/
        //1行目
        $contents .= "{$nendo}　{$gradeName}　{$semeName}　{$testName},,,{$ctrlDate}\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //テスト項目取得
        if ($this->field["PRINT_DIV"] == "1") {
            $this->subclassList = array();
            //クラス毎に科目情報を取得
            foreach ($this->select_data["selectdata"] as $key => $gradeClass) {
                list($grade, $class) = explode("-", $gradeClass);
                $query = knjd615iQuery::getSubclassList($this, $gradeClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $this->subclassList[$class."-".$row["SUBCLASSCD"]] = array($row["SUBCLASSCD"], $row["COURSE"], $row["CLASSNAME"], $row["SUBCLASSNAME"]);
                }
            }
        } else {
            $this->subclassList = array();
            $query = knjd615iQuery::getSubclassList($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->subclassList[$row["SUBCLASSCD"]] = array($row["COURSE"], $row["CLASSNAME"], $row["SUBCLASSNAME"]);
            }
        }

        $header_data = false;   //出力フラグ

        if ($this->field["PRINT_DIV"] == "1") {
            $header = array();
            foreach ($this->select_data["selectdata"] as $key => $gradeClass) {
                list($grade, $class) = explode("-", $gradeClass);
                $header[$class]["DOMITORY"]     = "寮下";
                $header[$class]["SP"]           = "特待";
                $header[$class]["DIV"]          = "区分";
                $header[$class]["HR_NAME"]      = "ホーム";
                $header[$class]["NO"]           = "No";
                $header[$class]["NAME"]         = "氏名";
                $header[$class]["COURSE_NAME"]  = "系";
                //科目名称セット
                foreach ($this->subclassList as $classSubcd => $subClassArray) {
                    list($hrClass, $subclasscd) = explode("-", $classSubcd);
                    if ($class == $hrClass) {
                        $header[$class][$subClassArray[0]]  = $subClassArray[3];
                    }
                }
                $header[$class]["TOTAL"]        = "合計";
                $header[$class]["AVE"]          = "平均";
                $header[$class]["HR_RANK"]      = "ホーム順位";
                $header[$class]["COURSE_RANK"]  = "文理順位";
                $header[$class]["GRADE_RANK"]   = "学年順位";
                $header[$class]["RED_POINT"]    = "赤点";
            }
        } else {
            $header = array();
            $header["DOMITORY"]     = "寮下";
            $header["SP"]           = "特待";
            $header["DIV"]          = "区分";
            $header["HR_NAME"]      = "ホーム";
            $header["NO"]           = "No";
            $header["NAME"]         = "氏名";
            $header["COURSE_NAME"]  = "系";
            //科目名称セット
            foreach ($this->subclassList as $subclassCd => $subClassArray) {
                $header[$subclassCd]  = $subClassArray[2];
            }
            $header["TOTAL"]        = "合計";
            $header["AVE"]          = "平均";
            $header["HR_RANK"]      = "ホーム順位";
            $header["COURSE_RANK"]  = "文理順位";
            $header["GRADE_RANK"]   = "学年順位";
            $header["RED_POINT"]    = "赤点";

            //科目名称セット
            foreach ($this->subclassList as $subclassCd => $subClassArray) {
                $header2[$subclassCd]  = $subClassArray[2];
            }
            $header2["TOTAL"]  = "合計";
            $header2["AVE"]    = "平均";
            $header2["CNT"]    = "";
        }

        //データセット
        $notSubclassCd = array("333333", "555555", "999999", "99999A", "99999B", "999999AVG");
        if ($this->field["PRINT_DIV"] == "1") {
            foreach ($this->select_data["selectdata"] as $key => $gradeClass) {
                list($grade, $class) = explode("-", $gradeClass);

                //成績データを学籍番号ごとにキープ
                $schScoreData = array();
                $query = knjd615iQuery::selectCsvScoreData($this, $gradeClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    $row["SCORE"] = ($row["VALUE_DI"] == "*") ? $row["VALUE_DI"]: $row["SCORE"];

                    //平均切り上げ、小数第2位
                    $row["AVG"] = sprintf('%.1f', round($row["AVG"], 1));

                    //配列に格納
                    $schScoreData[$row["SCHREGNO"].":".$row["SUBCLASSCD2"]] = $row;
                }

                //クラス毎にヘッダーセット
                $checkCnt = 0;
                $checkCnt = $db->getOne(knjd615iQuery::getSutudentList($this, $gradeClass, "COUNT"));
                if ($checkCnt != 0) {
                    $contents .= implode($header[$class], ",")."\n";
                }

                //学籍リスト取得、セット
                $query = knjd615iQuery::getSutudentList($this, $gradeClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    $redCount = 0;
                    foreach ($schScoreData as $schNoSubCd => $arrData) {
                    list($schNo, $subClassCd2) = explode(":", $schNoSubCd);
                    list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subClassCd2);
                        if ($row["SCHREGNO"] == $schNo) {
                            //赤点
                            if (!in_array($subclassCd, $notSubclassCd)) {
                                $redCount = (($arrData["SCORE"] < 30) && $arrData["SCORE"] != "*") ? $redCount + 1: $redCount;
                                $row["RED_POINT"]    = $redCount;
                            }
                            $row[$subClassCd2] = $arrData["SCORE"];
                            if ($subclassCd == "999999") {
                                $row["TOTAL"]        = $arrData["SCORE"];
                                $row["AVE"]          = $arrData["AVG"];
                                $row["HR_RANK"]      = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["CLASS_RANK"] : $arrData["CLASS_AVG_RANK"];
                                $row["COURSE_RANK"]  = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["COURSE_RANK"]: $arrData["COURSE_AVG_RANK"];
                                $row["GRADE_RANK"]   = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["GRADE_RANK"] : $arrData["GRADE_AVG_RANK"];
                            }
                        }
                    }
                    $comma = "";
                    foreach ($header[$class] as $key => $headerName) {
                        if ($key == "RED_POINT") {
                            $contents .= $comma.sprintf('%d', $row[$key]);
                        } else {
                            $contents .= $comma.$row[$key];
                        }
                        $comma = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                //クラス毎に改行
                if ($checkCnt != 0) {
                    $contents .= "\n";
                }
            }
        } else {
            //学年（PRINT_DIV == "2"）
            //成績データを学籍番号ごとにキープ
            $schScoreData = array();
            $query = knjd615iQuery::selectCsvScoreData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $row["SCORE"] = ($row["VALUE_DI"] == "*") ? $row["VALUE_DI"]: $row["SCORE"];

                //平均、四捨五入
                $row["AVG"] = round($row["AVG"], 1);

                $schScoreData[$row["SCHREGNO"].":".$row["SUBCLASSCD2"]] = $row;
            }

            //ヘッダーセット
            $contents .= implode($header, ",")."\n";

            //学籍リスト取得、セット
            $this->schRedCnt      = 0;
            $this->totalRedCnt    = array();
            $this->subclassRedCnt = array();
            $this->schKessekiCnt  = array();
            $query = knjd615iQuery::getSutudentList($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $redCount   = 0;
                $subRedCnt  = 0;
                $kessekiCnt = 0;
                foreach ($schScoreData as $schNoSubCd => $arrData) {
                    list($schNo, $subClassCd2) = explode(":", $schNoSubCd);
                    list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subClassCd2);
                    if ($row["SCHREGNO"] == $schNo) {
                        //赤点
                        if (!in_array($subclassCd, $notSubclassCd)) {
                            $redCount   = (($arrData["SCORE"] < 30) && $arrData["SCORE"] != "*") ? $redCount + 1: $redCount;
                            $subRedCnt  = (($arrData["SCORE"] < 30) && $arrData["SCORE"] != "*") ? 1: 0;
                            $row["RED_POINT"] = $redCount;
                            $this->totalRedCnt[$row["HR_CLASS"]] = $subRedCnt + $this->totalRedCnt[$row["HR_CLASS"]];
                            $this->totalRedCnt["TOTAL"]          = $subRedCnt + $this->totalRedCnt["TOTAL"];
                            //寮生赤点合計
                            if ($row["RESIDENTCD_NAMESPARE1"] == "4") {
                                $this->totalRedCnt["RYO_TOTAL"]  = $subRedCnt + $this->totalRedCnt["RYO_TOTAL"];
                            }
                        }
                        $kessekiCnt = ($arrData["VALUE_DI"] == "*") ? 1: 0;
                        $row[$subClassCd2] = $arrData["SCORE"];
                        if ($subclassCd == "999999") {
                            $row["TOTAL"]        = $arrData["SCORE"];
                            $row["AVE"]          = $arrData["AVG"];
                            $row["HR_RANK"]      = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["CLASS_RANK"] : $arrData["CLASS_AVG_RANK"];
                            $row["COURSE_RANK"]  = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["COURSE_RANK"]: $arrData["COURSE_AVG_RANK"];
                            $row["GRADE_RANK"]   = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["GRADE_RANK"] : $arrData["GRADE_AVG_RANK"];
                        }
                        //科目別赤点カウント
                        $this->subclassRedCnt[$row["HR_CLASS"].":".$subClassCd2] = $subRedCnt + $this->subclassRedCnt[$row["HR_CLASS"].":".$subClassCd2];
                        $this->subclassRedCnt[$subClassCd2]                      = $subRedCnt + $this->subclassRedCnt[$subClassCd2];
                        //寮生赤点、科目別
                        if ($row["RESIDENTCD_NAMESPARE1"] == "4") {
                            $this->subclassRedCnt["RYO:".$subClassCd2]           = $subRedCnt + $this->subclassRedCnt["RYO:".$subClassCd2];
                        }
                    }
                }
                //赤点人数
                $this->schRedCnt     = ($redCount > 0) ? $this->schRedCnt + 1: $this->schRedCnt;
                //欠席者カウント
                $this->schKessekiCnt["TOTAL"] = ($kessekiCnt > 0) ? $this->schKessekiCnt["TOTAL"] + 1: $this->schKessekiCnt["TOTAL"];
                $this->schKessekiCnt["TOTAL".$row["HR_CLASS"]] = ($kessekiCnt > 0) ? $this->schKessekiCnt["TOTAL".$row["HR_CLASS"]] + 1: $this->schKessekiCnt["TOTAL".$row["HR_CLASS"]];
                $comma = "";
                foreach ($header as $key => $headerName) {
                    if ($key == "RED_POINT") {
                        $contents .= $comma.sprintf('%d', $row[$key]);
                    } else {
                        $contents .= $comma.$row[$key];
                    }
                    $comma = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }

            //タイトル再セット
            $contents .= "\n{$nendo}　{$gradeName}　{$semeName}　{$testName},,,{$ctrlDate}\n";

            //ヘッダー２セット
            $contents .= ",".implode($header2, ",")."\n";

            //データ取得
            $this->scoreAvgData = array();
            $query = knjd615iQuery::selectCsvScoreAvgData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $row["AVG"]       = sprintf('%.1f', round($row["AVG"], 1));
                $row["STDDEV"]    = sprintf('%.1f', round($row["STDDEV"], 1));
                $this->scoreAvgData[$row["AVG_DIV"]."-".$row["GRADE"]."-".$row["HR_CLASS"]."-".$row["COURSE"].":".$row["SUBCLASSCD"]] = $row;
            }

            /****** １ブロック目(学年平均等) *******/
            //学年平均
            $contents .= "学年平均,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["AVG"];
                } else if ($key == "AVE") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999AVG"]["AVG"];
                } else if ($key == "CNT") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["COUNT"];
                } else {
                    $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["AVG"];
                }
                $comma = ",";
            }
            $contents .= "\n";

            //コースごと
            //コース取得
            $this->courseList = array();
            $query = knjd615iQuery::getCourseData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->courseList[$row["COURSE"]] = $row;
            }
            foreach ($this->courseList as $courseCode => $cdAndName) {
                $contents .= "{$cdAndName["COURSECODENAME"]},";
                $comma = "";
                $setKey = "3-{$this->field["GRADE"]}-000-{$courseCode}:";
                foreach ($header2 as $key => $headerName) {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                    if ($key == "TOTAL") {
                        $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["AVG"];
                    } else if ($key == "AVE") {
                        $contents .= $comma.$this->scoreAvgData[$setKey."999999AVG"]["AVG"];
                    } else if ($key == "CNT") {
                        $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["COUNT"];
                    } else {
                        $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["AVG"];
                    }
                    $comma = ",";
                }
                $contents .= "\n";
            }

            //最高点
            $contents .= "最高点,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL") {
                    $contents .= $comma.round($this->scoreAvgData[$setKey."999999"]["HIGHSCORE"], 0);
                } else if ($key == "AVE") {
                    $contents .= $comma.sprintf('%.1f', round($this->scoreAvgData[$setKey."999999AVG"]["HIGHSCORE"], 1));
                } else {
                    $setHiScore = ($this->scoreAvgData[$setKey.$subclassCd]["HIGHSCORE"] != "") ? round($this->scoreAvgData[$setKey.$subclassCd]["HIGHSCORE"], 0): "";
                    $contents .= $comma.$setHiScore;
                }
                $comma = ",";
            }
            $contents .= "\n";

            //最低点
            $contents .= "最低点,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL") {
                    $contents .= $comma.round($this->scoreAvgData[$setKey."999999"]["LOWSCORE"], 0);
                } else if ($key == "AVE") {
                    $contents .= $comma.sprintf('%.1f', round($this->scoreAvgData[$setKey."999999AVG"]["LOWSCORE"], 1));
                } else {
                    $setlowScore = ($this->scoreAvgData[$setKey.$subclassCd]["LOWSCORE"] != "") ? round($this->scoreAvgData[$setKey.$subclassCd]["LOWSCORE"], 0): "";
                    $contents .= $comma.$setlowScore;
                }
                $comma = ",";
            }
            $contents .= "\n";

            //中央値
            $contents .= "中央値,";
            $comma = "";
            //成績データをSUBCLASSCDごとにキープ
            $befMidScoreData = array();
            $query = knjd615iQuery::selectCsvScoreData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($row["SUBCLASSCD2"] == "99-H-99-999999") {
                    $befMidScoreData["TOTAL"][] = $row["SCORE"];
                    $befMidScoreData["AVE"][] = $row["AVG"];
                } else {
                    $befMidScoreData[$row["SUBCLASSCD2"]][] = $row["SCORE"];
                }
            }
            $median = array();
            //合計、平均セット
            $koumokuName = array("TOTAL", "AVE");
            foreach ($koumokuName as $key => $koumoku) {
                if (is_array($befMidScoreData[$koumoku])) sort($befMidScoreData[$koumoku]);//得点データを昇順に並べ替え
                $setMedVal = "";
                $tCnt = get_count($befMidScoreData[$koumoku]);
                if ($tCnt == 1) {
                    $setMedVal = "0";
                    $setMedian = $befMidScoreData[$koumoku][$setMedVal];
                } else if ($tCnt % 2 == 1) {
                    $setMedVal = $tCnt / 2;
                    $setMedian = $befMidScoreData[$koumoku][$setMedVal];
                } else { // $tCnt % 2 == 0) {
                    $setMedVal = $tCnt / 2;
                    $sVal1     = $befMidScoreData[$koumoku][$setMedVal];
                    $sVal2     = $befMidScoreData[$koumoku][$setMedVal -1];
                    $setMedian = ($sVal1 + $sVal2) / 2;
                }
                $median[$koumoku] = sprintf('%.1f', round($setMedian, 1));
            }
            //科目ごとにセット
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL") {
                    $contents .= $comma.$median["TOTAL"];
                } else if ($key == "AVE") {
                    $contents .= $comma.$median["AVE"];
                } else if ($key == "CNT") {
                    $contents .= $comma."";
                } else {
                    //各科目ごと
                    if (is_array($befMidScoreData[$key])) sort($befMidScoreData[$key]);//得点データを昇順に並べ替え
                    $setMedSVal = "";
                    $tCnt = get_count($befMidScoreData[$key]);
                    if ($tCnt == 1) {
                        $setMedVal = "0";
                        $setMedian = $befMidScoreData[$key][$setMedVal];
                    } else if ($tCnt % 2 == 1) {
                        $setMedVal = $tCnt / 2;
                        $setMedian = $befMidScoreData[$key][$setMedVal];
                    } else { // $tCnt % 2 == 0) 
                        $setMedVal = $tCnt / 2;
                        $sVal1     = $befMidScoreData[$key][$setMedVal];
                        $sVal2     = $befMidScoreData[$key][$setMedVal -1];
                        $setMedian = ($sVal1 + $sVal2) / 2;
                    }
                    $setMedian = ($setMedian == "") ? "": sprintf('%.1f', round($setMedian, 0));
                    $contents .= $comma.$setMedian;
                }
                $comma = ",";
            }
            $contents .= "\n";

            //標準偏差
            $contents .= "標準偏差,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["STDDEV"];
                } else if ($key == "AVE") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999AVG"]["STDDEV"];
                } else {
                    $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["STDDEV"];
                }
                $comma = ",";
            }
            $contents .= "\n";

            /******２ブロック目(クラス別平均)*******/
            //学年情報取得＋セット
            $query = knjd615iQuery::getGradeHrclassData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= "\n";
                $contents .= "{$row["HR_CLASS_NAME1"]}平均,";
                $comma = "";
                $setKey = "2-{$this->field["GRADE"]}-{$row["HR_CLASS"]}-00000000:";
                foreach ($header2 as $key => $headerName) {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                    if ($key == "TOTAL") {
                        $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["AVG"];
                    } else if ($key == "AVE") {
                        $contents .= $comma.$this->scoreAvgData[$setKey."999999AVG"]["AVG"];
                    } else {
                        $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["AVG"];
                    }
                    $comma = ",";
                }
            }
            //寮生
            $contents .= "\n";
            $contents .= "寮生平均,";
            $comma = "";
            $setKey = "7-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["AVG"];
                } else if ($key == "AVE") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999AVG"]["AVG"];
                } else {
                    $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["AVG"];
                }
                $comma = ",";
            }
            $contents .= "\n";

            /******３ブロック目(クラス別赤点)*******/
            //科目ごとの学年全体の赤点カウント
            $contents .= "\n";
            $contents .= "赤点,";
            $comma = "";
            foreach ($header2 as $key => $headerName) {
                if ($key == "TOTAL") {
                    $contents .= $comma.sprintf('%d', $this->totalRedCnt["TOTAL"]);
                } else if ($key == "AVE") {
                    $contents .= $comma.$this->schRedCnt;
                } else if ($key == "CNT") {
                    $contents .= $comma."";
                } else {
                    $contents .= $comma.sprintf('%d', $this->subclassRedCnt[$key]);
                }
                $comma = ",";
            }
            $contents .= "赤点／人数";
            //学年情報取得＋セット
            $query = knjd615iQuery::getGradeHrclassData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= "\n";
                $contents .= "{$row["HR_CLASS_NAME1"]}赤点,";
                $comma = "";
                foreach ($header2 as $key => $headerName) {
                    if ($key == "TOTAL") {
                        $contents .= $comma.sprintf('%d', $this->totalRedCnt[$row["HR_CLASS"]]);
                    } else if ($key == "AVE" || $key == "CNT") {
                        $contents .= $comma."";
                    } else {
                        $contents .= $comma.sprintf('%d', $this->subclassRedCnt[$row["HR_CLASS"].":".$key]);
                    }
                    $comma = ",";
                }
            }
            //寮生
            $contents .= "\n";
            $contents .= "寮生赤点,";
            $comma = "";
            foreach ($header2 as $key => $headerName) {
                if ($key == "TOTAL") {
                    $contents .= $comma.sprintf('%d', $this->totalRedCnt["RYO_TOTAL"]);
                } else if ($key == "AVE" || $key == "CNT") {
                    $contents .= $comma."";
                } else {
                    $contents .= $comma.sprintf('%d', $this->subclassRedCnt["RYO:".$key]);
                }
                $comma = ",";
            }
            $contents .= "\n";

            /******４ブロック目(クラス別受験者数)*******/
            //科目ごとの学年総受験者数
            $contents .= "\n";
            $contents .= "総受験者数,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL") {
                    $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["COUNT"];
                } else if ($key == "AVE") {
                    //欠席者
                    $contents .= $comma.sprintf('%d', $this->schKessekiCnt["TOTAL"]);
                } else {
                    $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["COUNT"];
                }
                $comma = ",";
            }
            $contents .= "欠席者";
            //学年ごとの総受験者数
            $query = knjd615iQuery::getGradeHrclassData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= "\n";
                $contents .= "{$row["HR_CLASS_NAME1"]}受験者数 {$row["REGD_COUNT"]},";
                $comma = "";
                $setKey = "2-{$this->field["GRADE"]}-{$row["HR_CLASS"]}-00000000:";
                foreach ($header2 as $key => $headerName) {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                    if ($key == "TOTAL") {
                        $contents .= $comma.$this->scoreAvgData[$setKey."999999"]["COUNT"];
                    } else if ($key == "AVE") {
                        //欠席者
                        $contents .= $comma.sprintf('%d', $this->schKessekiCnt["TOTAL".$row["HR_CLASS"]]);
                    } else {
                        $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["COUNT"];
                    }
                    $comma = ",";
                }
            }


            $header_data = true;
        }
        $result->free();

        //ファイル名
        $title = "成績一覧";

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
