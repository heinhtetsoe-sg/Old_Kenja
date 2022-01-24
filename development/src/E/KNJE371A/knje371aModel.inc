<?php
class knje371aModel extends Model
{
    public $year;
    public $field = array();
    public $cmd;
    public $control;
    public $hopeCnt;

    public function knje371aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        $this->field = array(
            "TEST_CD" => VARS::post("TEST_CD"),
        );
        $this->hopeCnt = "8";

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["SCHREGNO"]         = "生徒コード";
        $header["HR_NAME"]          = "組番";
        $header["NAME"]             = "氏名";
        $header["NAME_KANA"]        = "ふりがな";
        $header["AVG"]              = "加重平均";
        $header["HOPE1"]            = "第１希望コード";
        $header["HOPE2"]            = "第２希望コード";
        $header["HOPE3"]            = "第３希望コード";
        $header["HOPE4"]            = "第４希望コード";
        $header["HOPE5"]            = "第５希望コード";
        $header["HOPE6"]            = "第６希望コード";
        $header["HOPE7"]            = "第７希望コード";
        $header["HOPE8"]            = "第８希望コード";
        $header["KIKOKU"]           = "帰国";
        $header["PBT_TOEFL"]        = "PBT_TOEFL";
        $header["iBT_TOEFL"]        = "iBT_TOEFL";
        $header["TOEIC"]            = "TOEIC";
        $header["JITSU_EIKEN"]      = "実用英検";
        $header["KOKU_EIKEN"]       = "国連英検";
        $header["KIKAIKEI"]         = "機械系資格";
        $header["LANGUAGE"]         = "外国語";
        $header["KOKUGO"]           = "国語";
        $header["SUGAKU"]           = "数学";
        $header["CHIRI"]            = "地理歴史";
        $header["KOMIN"]            = "公民";
        $header["SHINGAKU"]         = "進学先コード";
        $header["TOKUBETSU"]        = "特別枠";
        $header["HOPE_PASS1"]       = "第１希望推薦条件クリア";
        $header["HOPE_PASS2"]       = "第２希望推薦条件クリア";
        $header["HOPE_PASS3"]       = "第３希望推薦条件クリア";
        $header["HOPE_PASS4"]       = "第４希望推薦条件クリア";
        $header["HOPE_PASS5"]       = "第５希望推薦条件クリア";
        $header["HOPE_PASS6"]       = "第６希望推薦条件クリア";
        $header["HOPE_PASS7"]       = "第７希望推薦条件クリア";
        $header["HOPE_PASS8"]       = "第８希望推薦条件クリア";

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //データ出力
        $data = false;
        $query = knje371aQuery::selectQueryCsv($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";

            //加重平均
            $calc = knje371aModel::getWeightedAvg($db, $row);
            foreach ($calc as $field => $val) {
                $row[$field] = $val;
            }

            //各種資格
            $row["PBT_TOEFL"] = $db->getOne(knje371aQuery::getSchregQualifiedHobbyDat($row["YEAR"], $row["SCHREGNO"], "0010", "SCORE"));
            $row["iBT_TOEFL"] = $db->getOne(knje371aQuery::getSchregQualifiedHobbyDat($row["YEAR"], $row["SCHREGNO"], "0008", "SCORE"));
            $row["TOEIC"] = $db->getOne(knje371aQuery::getSchregQualifiedHobbyDat($row["YEAR"], $row["SCHREGNO"], "0011", "SCORE"));
            $row["JITSU_EIKEN"] = $db->getOne(knje371aQuery::getSchregQualifiedHobbyDat($row["YEAR"], $row["SCHREGNO"], "0001", "RANK"));
            $row["KOKU_EIKEN"] = $db->getOne(knje371aQuery::getSchregQualifiedHobbyDat($row["YEAR"], $row["SCHREGNO"], "0024", "RANK"));
            $kikaikei = $db->getOne(knje371aQuery::getSchregQualifiedHobbyDat($row["YEAR"], $row["SCHREGNO"], "0023", "COUNT"));
            if ($kikaikei > 0) {
                $row["KIKAIKEI"] = "TRUE"; //取得できた場合
            } else {
                $row["KIKAIKEI"] = "FALSE"; //取得出来なかった場合
            }

            //推薦条件クリアの設定, 希望コードの変換
            $passData = knje371aModel::getHopePass($db, $row);
            foreach ($passData as $field => $val) {
                $row[$field] = $val;
            }

            //メイン
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->year."年度線引表CSVデータ";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //加重平均の取得
    public function getWeightedAvg($db, $row)
    {
        //RECORD_RANK_SDIV_DATの取得
        $query = knje371aQuery::getRecorRankSdiv($this, $row["SCHREGNO"]);
        $result = $db->query($query);

        $testCd = "9990009";
        $totalCredits = 0; //評定が入力されている科目の単位数の合計
        $calc = array(); //各教科の計算結果を保持(高1～高3の総合計)
        $calcAvg = array();
        $aryCredits = array();
        $aryCreditsGrade = array();
        $calc["AVG"] = 0; //ALL9
        $calc["LANGUAGE"] = 0; //外国語
        $calc["KOKUGO"] = 0; //国語
        $calc["SUGAKU"] = 0; //数学
        $calc["CHIRI"] = 0; //地理歴史
        $calc["KOMIN"] = 0; //公民
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($Row["CREDITS"] == "") {
                continue;
            }
            $testCdAll9 = $this->field["TEST_CD"];
            if ($Row["GRADE_CD"] != "03") {
                $testCdAll9 = "9990008";  //高3以外は 9-9900-08 を参照
            }
            $val = 0;
            if ($Row["SCORE"]) {
                $field = "";
                if ($Row["SUBCLASSCD"] == "999999" && $Row["TEST_CD"] == $testCdAll9) {
                    $field = "AVG"; //ALL9
                    $calcAvg[$Row["GRADE_CD"]]["SCORE"] += $Row["SCORE"];
                    $calcAvg[$Row["GRADE_CD"]]["CREDITS"] += $Row["CREDITS"];
                } elseif ($Row["CLASSCD"] == "50" && $Row["TEST_CD"] == $testCd) {
                    $field = "LANGUAGE"; //外国語
                } elseif ($Row["CLASSCD"] == "15" && $Row["TEST_CD"] == $testCd) {
                    $field = "KOKUGO"; //国語
                } elseif ($Row["CLASSCD"] == "30" && $Row["TEST_CD"] == $testCd) {
                    $field = "SUGAKU"; //数学
                } elseif ($Row["CLASSCD"] == "20" && $Row["TEST_CD"] == $testCd) {
                    $field = "CHIRI"; //地理歴史
                } elseif ($Row["CLASSCD"] == "25" && $Row["TEST_CD"] == $testCd) {
                    $field = "KOMIN"; //公民
                }
                $val = $Row["SCORE"] * $Row["CREDITS"]; //評定 * 単位数
                $totalCredits += $Row["CREDITS"];
            }
            $calc[$field] += $val;

            $aryCredits[$field] += $Row["CREDITS"];
            $aryCreditsGrade[$Row["GRADE_CD"]] += $totalCredits;
        }

        // (評定 * 単位数)の合計  / 評定が入力されている科目の単位数の合計
        if ($totalCredits) {
            foreach ($calc as $field => $val) {
                if ($aryCredits[$field]) {
                    $calc[$field] = $val / $aryCredits[$field];
                }
            }
        }

        $kekka3 = 0;
        foreach ($calcAvg as $grade => $val) {
            $kekka1  = $val["SCORE"] * $val["CREDITS"]; //3 評定 * 単位数

            $gradeTotalCredits = $aryCreditsGrade[$grade]; //4 学年の単位数計

            if ($gradeTotalCredits) {
                $kekka2 = $kekka1 / $gradeTotalCredits; //5 3 / 4
            }
            $omomi = 0;
            if ($grade == "01") {
                $omomi = 2;
            } elseif ($grade == "02") {
                $omomi = 3;
            } elseif ($grade == "03") {
                $omomi = 4;
            }
            $kekka3 += $kekka2 * $omomi; //7 5 * 重み
        }
        $kekka4 = $kekka3 / 9; //9
        $calc["AVG"] = $kekka4;

        return $calc;
    }


    //推薦条件クリアの設定
    public function getHopePass($db, $row)
    {

        //第1希望コード ～ 第8希望コード
        $passData = array();
        for ($i=1; $i<=$this->hopeCnt; $i++) {
            $hope = "HOPE".$i;
            $hopePass = "HOPE_PASS".$i;

            //希望コードを「学校」「学部」「学科」に分割
            $schoolCd     = substr($row[$hope], 0, 8);
            $facultycd    = substr($row[$hope], 8, 3);
            $departmentcd = substr($row[$hope], 11, 3);

            //大学4桁コードの取得
            $query = knje371aQuery::getConvertHopeCd($row[$hope]);
            $convertHope = $db->getOne($query);
            $passData[$hope] = $convertHope; //14桁の希望コードを、4桁の大学コードに変換して設定
            $hopePassVal = $convertHope; //推薦条件クリアには、大学コードを設定する

            //COLLEGE_RECOMMEND_CONDITION_MAIN_DAT の取得
            $query = knje371aQuery::getCollegeRecommendConditionMainDat($row, $schoolCd, $facultycd, $departmentcd);
            $rowMain = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($rowMain) {
                //履修コース要件
                if ($rowMain["COURSE_CONDITION_FLG"] == "1") {
                    //COLLEGE_RECOMMEND_CONDITION_COURSE_DAT の取得
                    $query = knje371aQuery::getCollegeRecommendConditionCourseDat($row, $schoolCd, $facultycd, $departmentcd);
                    $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (!$Row) {
                        $hopePassVal = ""; //推薦条件を満たさなかった
                    }
                }

                //履修科目要件
                if ($rowMain["SUBCLASS_CONDITION_FLG"] == "1") {
                    //高1～高3の年度を設定
                    $schYear = array(); // '01'⇒高1の年度, '02'⇒高2の年度, '03'⇒高3の年度
                    $query = knje371aQuery::getSchregYear($this, $row["SCHREGNO"]);
                    $result = $db->query($query);
                    while ($yearRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $schYear[$yearRow["GRADE_CD"]] = $yearRow["YEAR"];
                    }

                    $passCnt = 0; //推薦に必要な科目を履修している数
                    //COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT の取得
                    $query = knje371aQuery::getCollegeRecommendConditionSubclassDat($row, $schoolCd, $facultycd, $departmentcd);
                    $result = $db->query($query);
                    while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                        //対象学年 == 高1
                        if ($Row["GRADE1_FLG"] == "1") {
                            //推薦条件クリア(履修科目要件) の取得
                            $query = knje371aQuery::getHopePassSubclass($schYear["01"], $row["SCHREGNO"], $Row);
                            $hopePassRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                            if (!$hopePassRow) {
                                $flg = false; //取得できなかった場合、履修していない科目として扱う
                            } elseif ($hopePassRow["COURSECD"] != "") {
                                if ($Row["COURSECD"] != $row["COURSECD"] || $Row["MAJORCD"] != $row["MAJORCD"] || $Row["COURSECODE"] != $row["COURSECODE"]) {
                                    //コース条件が設定されている場合、1つでも不一致の場合、履修していない科目として扱う
                                    $flg = false;
                                }
                            }
                        }

                        //対象学年 == 高2
                        if ($Row["GRADE2_FLG"] == "1") {
                            //推薦条件クリア(履修科目要件) の取得
                            $query = knje371aQuery::getHopePassSubclass($schYear["02"], $row["SCHREGNO"], $Row);
                            $hopePassRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                            if (!$hopePassRow) {
                                $flg = false; //取得できなかった場合、履修していない科目として扱う
                            } elseif ($hopePassRow["COURSECD"] != "") {
                                if ($Row["COURSECD"] != $row["COURSECD"] || $Row["MAJORCD"] != $row["MAJORCD"] || $Row["COURSECODE"] != $row["COURSECODE"]) {
                                    //コース条件が設定されている場合、1つでも不一致の場合、履修していない科目として扱う
                                    $flg = false;
                                }
                            }
                        }

                        //対象学年 == 高3
                        if ($Row["GRADE3_FLG"] == "1") {
                            //推薦条件クリア(履修科目要件) の取得
                            $query = knje371aQuery::getHopePassSubclass($schYear["03"], $row["SCHREGNO"], $Row);
                            $hopePassRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                            if (!$hopePassRow) {
                                $flg = false; //取得できなかった場合、履修していない科目として扱う
                            } elseif ($hopePassRow["COURSECD"] != "") {
                                if ($Row["COURSECD"] != $row["COURSECD"] || $Row["MAJORCD"] != $row["MAJORCD"] || $Row["COURSECODE"] != $row["COURSECODE"]) {
                                    //コース条件が設定されている場合、1つでも不一致の場合、履修していない科目として扱う
                                    $flg = false;
                                }
                            }
                        }

                        if ($flg) {
                            //推薦に必要な科目を履修している場合
                            $passCnt++;
                        } elseif ($rowMain["REQUIRED_FLG"] == "1") {
                            //必須科目を履修していなかった場合
                            $hopePassVal = ""; //推薦条件を満たさなかった
                            break;
                        }
                    }

                    //必要科目数の判定
                    if ($rowMain["SUBCLASS_NUM"] > $passCnt) {
                        $hopePassVal = ""; //推薦条件を満たさなかった
                    }
                }

                //履修資格要件
                if ($rowMain["QUALIFIED_CONDITION_FLG"] == "1") {
                    //COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT の取得
                    $query = knje371aQuery::getCollegeRecommendConditionQualifiedDat($row, $schoolCd, $facultycd, $departmentcd);
                    $result = $db->query($query);
                    $flg = false;
                    while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        //推薦条件クリア(資格要件) の取得
                        $query = knje371aQuery::getHopePassQualified($row, $Row);
                        $hopePassRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        if ($hopePassRow) {
                            $flg = true; //推薦条件を満たした
                            break;
                        }
                    }
                    if (!$flg) {
                        $hopePassVal = ""; //推薦条件を満たさなかった
                    }
                }
            } else {
                //推薦条件が取得できなかった場合
                $hopePassVal = "";
            }
            //推薦条件クリアを設定
            $passData[$hopePass] = $hopePassVal;
        }
        return $passData;
    }
}
