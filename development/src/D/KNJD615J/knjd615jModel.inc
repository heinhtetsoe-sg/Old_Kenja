<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd615jModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd615jModel() {
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
        $gradeName = $db->getOne(knjd615jQuery::getGradeName($this->field["GRADE"]));

        //テスト名取得
        $testName = $db->getOne(knjd615jQuery::getTest($this, $this->field["SEMESTER"], $this->field["GRADE"], "meisyou"));

        /**********************/
        /*  タイトル部分出力  */
        /**********************/
        //1行目
        $contents .= "{$nendo}　{$gradeName}　{$testName},,,{$ctrlDate}\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //テスト項目取得
        if ($this->field["PRINT_DIV"] == "1") {
            $this->subclassList = array();
            //クラス毎に科目情報を取得
            foreach ($this->select_data["selectdata"] as $key => $gradeClass) {
                list($grade, $class) = explode("-", $gradeClass);
                $query = knjd615jQuery::getSubclassList($this, $gradeClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $this->subclassList[$class."-".$row["SUBCLASSCD"]] = array($row["SUBCLASSCD"], $row["COURSE"], $row["CLASSNAME"], $row["SUBCLASSNAME"]);
                }
            }
        } else {
            $this->subclassList = array();
            $query = knjd615jQuery::getSubclassList($this);
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
                $header[$class]["HR_NAME"]      = "ホーム";
                $header[$class]["NO"]           = "No";
                $header[$class]["NAME"]         = "氏名";
                $header[$class]["SP"]           = "特待生";
                $header[$class]["DOMITORY"]     = "寮下宿";
                $header[$class]["DIV"]          = "外後転";
                //科目名称セット
                foreach ($this->subclassList as $classSubcd => $subClassArray) {
                    list($hrClass, $subclasscd) = explode("-", $classSubcd);
                    if ($class == $hrClass) {
                        $header[$class][$subClassArray[0]]  = $subClassArray[3];
                    }
                }
                $header[$class]["TOTAL"]        = "総合成績（総点）";
                $header[$class]["AVE"]          = "総合成績（平均）";
                $header[$class]["HR_RANK"]      = "ホーム（順位）";
                $header[$class]["HR_NUM"]       = "ホーム（人数）";
                $header[$class]["GRADE_RANK"]   = "学年（順位）";
                $header[$class]["GRADE_NUM"]    = "学年（人数）";
                $header[$class]["RED_POINT"]    = "赤点";
                $header[$class]["SUBCLASS_NUM"] = "科目数";
            }
        } else {
            $header = array();
            $header["HR_NAME"]      = "ホーム";
            $header["NO"]           = "No";
            $header["NAME"]         = "氏名";
            $header["SP"]           = "特待生";
            $header["DOMITORY"]     = "寮下宿";
            $header["DIV"]          = "外後転";
            //科目名称セット
            foreach ($this->subclassList as $subclassCd => $subClassArray) {
                $header[$subclassCd]  = $subClassArray[2];
            }
            $header["TOTAL"]        = "総合成績（総点）";
            $header["AVE"]          = "総合成績（平均）";
            $header["HR_RANK"]      = "ホーム（順位）";
            $header["HR_NUM"]       = "ホーム（人数）";
            $header["GRADE_RANK"]   = "学年（順位）";
            $header["GRADE_NUM"]    = "学年（人数）";
            $header["RED_POINT"]    = "赤点";
            $header["SUBCLASS_NUM"] = "科目数";

            //科目名称セット
            foreach ($this->subclassList as $subclassCd => $subClassArray) {
                $header2[$subclassCd]  = $subClassArray[2];
            }
            $header2["TOTAL"]  = "総合成績（総点）";
            $header2["AVE"]    = "総合成績（平均）";
        }

        //データセット
        $notSubclassCd = array("333333", "555555", "999999", "99999A", "99999B", "999999AVG");
        if ($this->field["PRINT_DIV"] == "1") {
            //データ取得(ホーム人数、学年人数セット用)
            $this->scoreAvgData = array();
            $query = knjd615jQuery::selectCsvScoreAvgData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $this->scoreAvgData[$row["AVG_DIV"]."-".$row["GRADE"]."-".$row["HR_CLASS"]."-".$row["COURSE"].":".$row["SUBCLASSCD"]] = $row;
            }

            $this->subclassCnt = array();
            foreach ($this->select_data["selectdata"] as $key => $gradeClass) {
                list($grade, $class) = explode("-", $gradeClass);

                //成績データを学籍番号ごとにキープ
                $schScoreData = array();
                $query = knjd615jQuery::selectCsvScoreData($this, $gradeClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    $row["SCORE"] = ($row["VALUE_DI"] == "*") ? $row["VALUE_DI"]: $row["SCORE"];

                    //平均切り上げ、小数第2位
                    $row["AVG"] = sprintf('%.1f', round($row["AVG"], 1));

                    //配列に格納
                    $schScoreData[$row["SCHREGNO"].":".$row["SUBCLASSCD2"]] = $row;

                    //生徒ごとの科目数カウント
                    if (!in_array($row["SUBCLASSCD"], $notSubclassCd)) {
                        $this->subclassCnt[$row["SCHREGNO"]] = ($row["SUBCLASSCD"] != "") ? $this->subclassCnt[$row["SCHREGNO"]] + 1: $this->subclassCnt[$row["SCHREGNO"]];
                    }
                }

                //クラス毎にヘッダーセット
                $checkCnt = 0;
                $checkCnt = $db->getOne(knjd615jQuery::getSutudentList($this, $gradeClass, "COUNT"));
                if ($checkCnt != 0) {
                    $contents .= implode($header[$class], ",")."\n";
                }

                //学籍リスト取得、セット
                $query = knjd615jQuery::getSutudentList($this, $gradeClass);
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
                                $row["HR_NUM"]       = $this->scoreAvgData["2-{$row["GRADE"]}-{$row["HR_CLASS"]}-00000000:999999"]["COUNT"];
                                $row["GRADE_RANK"]   = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["GRADE_RANK"] : $arrData["GRADE_AVG_RANK"];
                                $row["GRADE_NUM"]    = $this->scoreAvgData["1-{$row["GRADE"]}-000-00000000:999999"]["COUNT"];
                            }
                        }
                    }
                    //科目数セット
                    $row["SUBCLASS_NUM"] = $this->subclassCnt[$row["SCHREGNO"]];

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
            //データ取得(ホーム人数、学年人数セット用)
            $this->scoreAvgData = array();
            $query = knjd615jQuery::selectCsvScoreAvgData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                //平均、四捨五入
                $row["AVG"]       = sprintf('%.1f', round($row["AVG"], 1));

                $this->scoreAvgData[$row["AVG_DIV"]."-".$row["GRADE"]."-".$row["HR_CLASS"]."-".$row["COURSE"].":".$row["SUBCLASSCD"]] = $row;
            }

            //成績データを学籍番号ごとにキープ
            $schScoreData      = array();
            $this->subclassCnt = array();
            $query = knjd615jQuery::selectCsvScoreData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $row["SCORE"] = ($row["VALUE_DI"] == "*") ? $row["VALUE_DI"]: $row["SCORE"];

                //平均切り上げ、小数第2位
                $row["AVG"] = sprintf('%.1f', round($row["AVG"], 1));

                //配列に格納
                $schScoreData[$row["SCHREGNO"].":".$row["SUBCLASSCD2"]] = $row;

                //生徒ごとの科目数カウント
                if (!in_array($row["SUBCLASSCD"], $notSubclassCd)) {
                    $this->subclassCnt[$row["SCHREGNO"]] = ($row["SUBCLASSCD"] != "") ? $this->subclassCnt[$row["SCHREGNO"]] + 1: $this->subclassCnt[$row["SCHREGNO"]];
                }

                $schScoreData[$row["SCHREGNO"].":".$row["SUBCLASSCD2"]] = $row;
            }

            //ヘッダーセット
            $contents .= implode($header, ",")."\n";

            //学籍リスト取得、セット
            $this->schRedCnt      = 0;
            $this->totalRedCnt    = array();
            $this->subclassRedCnt = array();
            $query = knjd615jQuery::getSutudentList($this);
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
                            $this->totalRedCnt["TOTAL"] = $subRedCnt + $this->totalRedCnt["TOTAL"];
                        }
                        $kessekiCnt = ($arrData["VALUE_DI"] == "*") ? 1: 0;
                        $row[$subClassCd2] = $arrData["SCORE"];
                        if ($subclassCd == "999999") {
                            $row["TOTAL"]        = $arrData["SCORE"];
                            $row["AVE"]          = $arrData["AVG"];
                            $row["HR_RANK"]      = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["CLASS_RANK"] : $arrData["CLASS_AVG_RANK"];
                            $row["HR_NUM"]       = $this->scoreAvgData["2-{$row["GRADE"]}-{$row["HR_CLASS"]}-00000000:999999"]["COUNT"];
                            $row["GRADE_RANK"]   = ($this->field["OUTPUT_KIJUN"] == "1") ? $arrData["GRADE_RANK"] : $arrData["GRADE_AVG_RANK"];
                            $row["GRADE_NUM"]    = $this->scoreAvgData["1-{$row["GRADE"]}-000-00000000:999999"]["COUNT"];
                        }
                        //科目別赤点カウント
                        $this->subclassRedCnt[$row["HR_CLASS"].":".$subClassCd2] = $subRedCnt + $this->subclassRedCnt[$row["HR_CLASS"].":".$subClassCd2];
                        $this->subclassRedCnt[$subClassCd2]                      = $subRedCnt + $this->subclassRedCnt[$subClassCd2];
                    }
                }
                //赤点人数
                $this->schRedCnt = ($redCount > 0) ? $this->schRedCnt + 1: $this->schRedCnt;

                //科目数セット
                $row["SUBCLASS_NUM"] = $this->subclassCnt[$row["SCHREGNO"]];

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

            // ****** １ブロック目(学年平均等) *******
            //学年平均
            $contents .= "平均,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
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
            //クラス別
            $query = knjd615jQuery::getGradeHrclassData($this);
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
            $contents .= "\n\n";

            // ******２ブロック目(赤点情報)*******
            //赤点合計
            $contents .= "赤点合計,";
            $comma = "";
            foreach ($header2 as $key => $headerName) {
                if ($key == "TOTAL" || $key == "AVE") {
                    $contents .= "";
                } else {
                    $contents .= $comma.sprintf('%d', $this->subclassRedCnt[$key]);
                }
                $comma = ",";
            }
            //赤点総計
            $contents .= ",,赤点合計,".sprintf('%d', $this->totalRedCnt["TOTAL"]);
            //クラス別、科目別赤点合計
            $firstFlg = 1;
            $query = knjd615jQuery::getGradeHrclassData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= "\n";
                $contents .= "{$row["HR_CLASS_NAME1"]}赤点,";
                $comma = "";
                foreach ($header2 as $key => $headerName) {
                    if ($key == "TOTAL" || $key == "AVE") {
                        $contents .= "";
                    } else {
                        $contents .= $comma.sprintf('%d', $this->subclassRedCnt[$row["HR_CLASS"].":".$key]);
                    }
                    $comma = ",";
                }
                if ($firstFlg == 1) {
                    //赤点人数
                    $contents .= ",,赤点人数,".$this->schRedCnt;
                }
                $firstFlg++;
            }
            $contents .= "\n\n";

            // ******３ブロック目(クラス別赤点)*******
            //科目ごとの学年総受験者数
            $contents .= "受験者数,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL" || $key == "AVE") {
                    $contents .= "";
                } else {
                    $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["COUNT"];
                }
                $comma = ",";
            }
            //科目ごとのクラス別総受験者数
            $query = knjd615jQuery::getGradeHrclassData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= "\n";
                $contents .= "{$row["HR_CLASS_NAME1"]}　（{$row["REGD_COUNT"]}）,";
                $comma = "";
                $setKey = "2-{$this->field["GRADE"]}-{$row["HR_CLASS"]}-00000000:";
                foreach ($header2 as $key => $headerName) {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                    if ($key == "TOTAL" || $key == "AVE") {
                        $contents .= "";
                    } else {
                        $contents .= $comma.$this->scoreAvgData[$setKey.$subclassCd]["COUNT"];
                    }
                    $comma = ",";
                }
            }
            $contents .= "\n\n";

            // ******４ブロック目(クラス別受験者数)*******
            //最高点
            $contents .= "最高点,";
            $comma = "";
            $setKey = "1-{$this->field["GRADE"]}-000-00000000:";
            foreach ($header2 as $key => $headerName) {
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $key);
                if ($key == "TOTAL" || $key == "AVE") {
                    $contents .= "";
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
                if ($key == "TOTAL" || $key == "AVE") {
                    $contents .= "";
                } else {
                    $setlowScore = ($this->scoreAvgData[$setKey.$subclassCd]["LOWSCORE"] != "") ? round($this->scoreAvgData[$setKey.$subclassCd]["LOWSCORE"], 0): "";
                    $contents .= $comma.$setlowScore;
                }
                $comma = ",";
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
