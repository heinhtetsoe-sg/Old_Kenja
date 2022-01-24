<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje061mModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $annual;            //年次
    var $control;           //コントロールマスタ
    var $hr_class;
    var $coursecode;
    var $RegdTable;         //学校区分別のテーブル名
    var $schoolName;
    var $recordTableDiv;
    var $motoClassCds = array();
    var $sakiClassCds = array();
    var $motoSubClassCds = array();
    var $sakiSubClassCds = array();
    var $chugaku_chosasho;
    var $vSchregRegdDat;    //在籍データ(生徒のMAX学期のレコード)
        
    /**
     * メンバ変数の初期化
    */
    function knje061mModel()
    {
    }
    
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            //コントロールマスタより情報を取得
            common::GetControlMaster_Fnc($this->control);
            if ($this->control["学校区分"] == "0") { //学年制
                $this->RegdTable = "v_regdyear_grade_dat";
            } elseif ($this->control["学校区分"] == "1") { //単位制
                $this->RegdTable = "v_regdyear_unit_dat";
            }
            $this->vSchregRegdDat = knje061mQuery::getViewSchregRegdDat();

            $this->motoClassCds = array("国語"       => "32",
                                        "地理歴史"   => "34",
                                        "公民"       => "36",
                                        "数学"       => "38",
                                        "理科"       => "40",
                                        "保健体育"   => "42",
                                        "芸術"       => "44",
                                        "外国語"     => "46",
                                        "第2外国語"  => "48",
                                        "家庭"       => "50",
                                        "情報"       => "52",
                                        "総合"       => "90"
                                        );
            $this->sakiClassCds = array("国語"       => "62",
                                        "地理歴史"   => "64",
                                        "公民"       => "66",
                                        "数学"       => "68",
                                        "理科"       => "70",
                                        "保健体育"   => "72",
                                        "芸術"       => "74",
                                        "外国語"     => "76",
                                        "第2外国語"  => "76",
                                        "家庭"       => "78",
                                        "情報"       => "80",
                                        "総合"       => "90"
                                        );
            $this->makeSubClassCds();
            $this->makeRikaPattern();
        }

        $this->cmd    = VARS::request("cmd");
        //年次
        $this->annual = VARS::post("ANNUAL");
        //組
        $this->hr_class = VARS::post("HR_CLASS");
        //コース
        $this->coursecode = VARS::post("COURSECODE");
        $this->schregno   = VARS::post("SCHREGNO");
        
        //年次が変更したら初期化する
        if ($this->cmd == "annual") {
            $this->hr_class = "";
            $this->coursecode = "";
        }
        //組を変更したら初期化する
        if ($this->cmd == "hr_class") {
            $this->coursecode = "";
        }
        
        if (VARS::file("FILE")){
            $this->file = VARS::file("FILE");        //ファイルのアップロード
        }
        //画面上のデータ
        $this->field = array("KIND"     => VARS::post("KIND"),         //種別
                            "RANGE"     => "1",        //生成範囲
                            "CREATEDIV" => VARS::post("CREATEDIV"),     //新規作成 OR 追加作成
                            "HEADERFLG" => VARS::post("HEADERFLG"));   //ヘッダ有りフラグ
        // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
        $rtnRow = knje061mQuery::getNameMst();
        $this->schoolName       = $rtnRow["NAME1"];
        $this->recordTableDiv   = $rtnRow["NAMESPARE1"];
        //成績・観点データ(中学)
        //「中学調査書データ作成」チェックボックス
        $this->chugaku_chosasho = VARS::post("CHUGAKU_CHOSASHO");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        if ($this->field["KIND"] == 2){
            /**************************/
            /*  高校(成績：自動生成)  */
            /**************************/
            $this->updateHighSchool();
            $this->message = "学籍学習記録データの生成が完了しました。";

        } elseif ($this->field["KIND"] == 3){
            /**************************/
            /*  中学(成績：自動生成)  */
            /**************************/
            $this->updateJuniorHighSchool();
            $this->message = "学籍学習記録データおよび観点データの生成が完了しました。";

        } elseif ($this->field["KIND"] == 1){
            /********************************/
            /*  中学・高校(出欠：自動生成)  */
            /********************************/
            knje061mQuery::addRecord_Schreg_Attendrec_Dat($this);
            $this->message = "学籍出欠記録データの生成が完了しました。";
        }
    }

    /****************************/
    /* ↓↓↓ 高校(成績) ↓↓↓ */
    /****************************/

    //メイン
    function updateHighSchool()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knje061mQuery::getStudentSql($this);
//echo "query = " .$query ."<BR>";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $grade = $row["GRADE"];
            $year = $row["YEAR"];
            $annual = $row["ANNUAL"];
            $courseCd = $row["COURSECD"];
            $majorCd = $row["MAJORCD"];
            $courseCode = $row["COURSECODE"];
//echo "schregNo = " .$schregNo ."<BR>";
            // $gradeArray KEY：学年、VAL["YEAR"]：年度
            //                        VAL["ANNUAL"]：年次
            $gradeArray = array();
            $gradeArray[$grade] = array("YEAR"          => $year,
                                        "GRADE"         => $grade,
                                        "COURSECD"      => $courseCd,
                                        "MAJORCD"       => $majorCd,
                                        "COURSECODE"    => $courseCode,
                                        "ANNUAL"        => $annual);

            $queryRegd = knje061mQuery::getStudentRegdSql($schregNo, $grade);
            $resultRegd = $db->query($queryRegd);
            while ($rowRegd = $resultRegd->fetchRow(DB_FETCHMODE_ASSOC)) {
                $gradeArray[$rowRegd["GRADE"]] = array("YEAR"       => $rowRegd["YEAR"],
                                                       "GRADE"      => $rowRegd["GRADE"],
                                                       "COURSECD"   => $rowRegd["COURSECD"],
                                                       "MAJORCD"    => $rowRegd["MAJORCD"],
                                                       "COURSECODE" => $rowRegd["COURSECODE"],
                                                       "ANNUAL"     => $rowRegd["ANNUAL"]);
            }
            $resultRegd->free();

            //新規作成の場合、一旦レコードを削除する
            if ($this->field["CREATEDIV"] == 1) {
                $schoolCdStudy   = "0";
                $yearStudy       = $gradeArray[$this->annual]["YEAR"];
                $annualStudy     = $gradeArray[$this->annual]["ANNUAL"];

                $queryStudy = knje061mQuery::deleteAllStudyRecSql($schoolCdStudy, $yearStudy, $schregNo, $annualStudy, $this);
                $db->query($queryStudy);
            }

            //国語
            $this->setKokugo($schregNo, $gradeArray, $db);

            //地理・歴史
            $this->setTiriRekisi($schregNo, $gradeArray, $db);

            //公民
            $this->setKoumin($schregNo, $gradeArray, $db);

            //数学
            $this->setSuugaku($schregNo, $gradeArray, $db);

            //理科
            $this->setRika($schregNo, $gradeArray, $db);

            //保健・体育
            $this->setTaiku($schregNo, $gradeArray, $db);

            //芸術
            $this->setGeijutu($schregNo, $gradeArray, $db);

            //外国語
            $this->setGaikoku($schregNo, $gradeArray, $db);

            //家庭
            $this->setKatei($schregNo, $gradeArray, $db);

            //情報
            $this->setJouhou($schregNo, $gradeArray, $db);

            //総合的な学習
            $this->setSougou($schregNo, $gradeArray, $db);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //国語
    function setKokugo($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["国語"];
        $sakiClassCd = $this->sakiClassCds["国語"];
        $motoSubClass = $this->getMotoSubClass($motoClassCd);
        $sakiSubClass = $this->getSakiSubClass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];

        $recSeme        = "9";
        $scoreIsNotNull = "on";
        $orderDiv       = "score";

        //新規作成の場合、一旦レコードを削除する
        if ($this->field["CREATEDIV"] == 1) {
            $query = knje061mQuery::deleteClasscdStudyRecSql($schoolCd, $year, $schregNo, $annual, $sakiClassCd, $this);
            $db->query($query);
        }

//echo "schregNo = " .$schregNo .", year = " .$year ."<BR>";
        //高1
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            //対象データ取得
            $scoreArray = $scoreArray2 = array();
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, "", $scoreIsNotNull, $orderDiv, $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($motoSubClass["古典高1"]   == $row["SUBCLASSCD"] || 
                    $motoSubClass["漢文高1"]   == $row["SUBCLASSCD"]) {
                    if (strlen($row["SCORE"])) $scoreArray[] = $row["SCORE"];
                }
                if ($motoSubClass["現代文高1"] == $row["SUBCLASSCD"]) {
                    if (strlen($row["SCORE"])) {
                        if (CTRL_YEAR < "2012") {
                            $scoreArray2[] = $row["SCORE"];
                        } else {
                            $scoreArray[] = $row["SCORE"];
                        }
                    }
                }
            }
            //更新処理・・・１科目でもあれば処理する。
            if (0 < get_count($scoreArray)) {
                $subclassCd = $sakiSubClass["国語総合高1"];
                $score      = $this->getAvg($scoreArray);//古典と漢文の平均//2012年度：現代文、古典、漢文の平均
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
            //更新処理・・・１科目でもあれば処理する。
            if (0 < get_count($scoreArray2)) {
                $subclassCd = $sakiSubClass["現代文高1"];
                $score      = $this->getAvg($scoreArray2);//現代文の得点//2012年度：読み替えない(作成しない)
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
        }
        //高2
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            //対象データ取得
            $scoreArray = array();
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, "", $scoreIsNotNull, $orderDiv, $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //現代文(2013年度から)
                if ($motoSubClass["現代文高2"] == $row["SUBCLASSCD"] && "2013" <= CTRL_YEAR) {
                    //現代文B(2014年度から)
                    if ("2014" <= CTRL_YEAR) {
                        $scoreArray[$sakiSubClass["現代文B高2"]] = $row["SCORE"];
                    } else {
                        $scoreArray[$sakiSubClass["現代文高2"]] = $row["SCORE"];
                    }
                }
                if ($motoSubClass["古典高2"] == $row["SUBCLASSCD"]) {
                    //古典B(2014年度から)
                    if ("2014" <= CTRL_YEAR) {
                        $scoreArray[$sakiSubClass["古典B高2"]] = $row["SCORE"];
                    } else {
                        $scoreArray[$sakiSubClass["古典高2"]] = $row["SCORE"];
                    }
                }
                if ($motoSubClass["漢文高2"] == $row["SUBCLASSCD"]) {
                    //古典A(2014年度から)
                    if ("2014" <= CTRL_YEAR) {
                        $scoreArray[$sakiSubClass["古典A高2"]] = $row["SCORE"];
                    } else {
                        $scoreArray[$sakiSubClass["古典講読高2"]] = $row["SCORE"];
                    }
                }
            }
            //更新処理
            if (0 < get_count($scoreArray)) {
                foreach ($scoreArray as $subclassCd => $score) {
                    $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                    $data = array();
                    $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                    $this->updateStudyRec($data, $db);
                }
            }
        }
        //高3
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3") {
//echo "schregNo = " .$schregNo .", year = " .$year ."<BR>";
            //対象データ取得
            $dataFlg1 = $dataFlg2 = $dataFlg3 = false;
            $scoreArray = array();
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, "", $scoreIsNotNull, $orderDiv, $this);
//echo "query = " .$query ."<BR><BR>";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($motoSubClass["現代文高3"] == $row["SUBCLASSCD"]) {
                    $dataFlg1 = true;
                    $motoScore["現代文高3"] = $row["SCORE"];
                }
                if ($motoSubClass["古典高3"] == $row["SUBCLASSCD"]) {
                    $dataFlg2 = true;
                    $motoScore["古典高3"] = $row["SCORE"];
                    if (strlen($row["SCORE"])) $scoreArray[] = $row["SCORE"];
                }
                if ($motoSubClass["漢文高3"] == $row["SUBCLASSCD"]) {
                    $dataFlg3 = true;
                    $motoScore["漢文高3"] = $row["SCORE"];
                    if (strlen($row["SCORE"])) $scoreArray[] = $row["SCORE"];
                }
            }
            //パターン番号
            $pattern = 0;
            if ($dataFlg1 && $dataFlg2 && $dataFlg3) {$pattern = 1;}
            else if ($dataFlg1 && $dataFlg2) {$pattern = 2;}
            else if ($dataFlg2 && $dataFlg3) {$pattern = 3;}
            else if ($dataFlg1 && $dataFlg3) {$pattern = 4;}
            else if ($dataFlg1) {$pattern = 5;}//不要？現代文のみ
            else if ($dataFlg2) {$pattern = 6;}//不要？古典のみ
            //更新処理
            //現代文高3
            if ($pattern == 1 || $pattern == 2 || $pattern == 3 || $pattern == 4 || $pattern == 5) {
                $subclassCd = $sakiSubClass["現代文高3"];
                $score  = 0;//初期化
                if ($pattern == 3) {
                    $score  = $motoScore["漢文高3"];
                } else {
                    $score  = $motoScore["現代文高3"];
                }
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
            //古典高3
            if ($pattern == 1 || $pattern == 2 || $pattern == 3 || $pattern == 4 || $pattern == 6) {
                $subclassCd = $sakiSubClass["古典高3"];
                $score  = 0;//初期化
                if ($pattern == 1) {
                    $score  = $this->getAvg($scoreArray);//２科目（古典高3・漢文高3）の平均
                } else if ($pattern == 4) {
                    $score  = $motoScore["漢文高3"];
                } else {
                    $score  = $motoScore["古典高3"];
                }
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
        }//高3
    }

    //地理・歴史
    function setTiriRekisi($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["地理歴史"];
        $sakiClassCd = $this->sakiClassCds["地理歴史"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //新規作成の場合、一旦レコードを削除する
        if ($this->field["CREATEDIV"] == 1) {
            $query = knje061mQuery::deleteClasscdStudyRecSql($schoolCd, $year, $schregNo, $annual, $sakiClassCd, $this);
            $db->query($query);
        }

        //高1
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            //対象データ取得
            $dataFlg = false;
            $subClassCd = $motoSubClass["世界史高1"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subClassCd, "", "", $this);
            $result = $db->query($query);
            $score = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $score = $row["SCORE"];
                $dataFlg = true;
            }
            //更新処理
            if ($dataFlg) {
                $subclassCd = $sakiSubClass["世界史B高1"];
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
        }

        //高2
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            $syoriSbuclass[] = $motoSubClass["世界史高2"];
            $syoriSbuclass[] = $motoSubClass["日本史高2"];
            $syoriSbuclass[] = $motoSubClass["地理高2"];

            $setSbuclass[] = $sakiSubClass["世界史B高2"];
            $setSbuclass[] = $sakiSubClass["日本史B高2"];
            $setSbuclass[] = $sakiSubClass["地理B高2"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高3
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3") {
            $yearH2 = $gradeArray["0".($this->annual - 1)]["YEAR"];
            //日本史高2
            $dataFlg1 = false;
            $subClassCd = $motoSubClass["日本史高2"];
            $query = knje061mQuery::getRecordScoreSql($yearH2, $recSeme, $schregNo, $motoClassCd, $subClassCd, "on", "", $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataFlg1 = true;
            }
            //地理高2
            $dataFlg2 = false;
            $subClassCd = $motoSubClass["地理高2"];
            $query = knje061mQuery::getRecordScoreSql($yearH2, $recSeme, $schregNo, $motoClassCd, $subClassCd, "on", "", $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataFlg2 = true;
            }
            //更新処理・・・高2で取らなかった科目は作成しない
            $syoriSbuclass[] = array();
            $setSbuclass[] = array();
            if ($dataFlg1) {
                $syoriSbuclass[] = $motoSubClass["日本史高3"];
                $setSbuclass[] = $sakiSubClass["日本史B高3"];
            }
            if ($dataFlg2) {
                $syoriSbuclass[] = $motoSubClass["地理高3"];
                $setSbuclass[] = $sakiSubClass["地理B高3"];
            }
            if ($dataFlg1 || $dataFlg2) {
                $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
            }
        }
    }

    //公民
    function setKoumin($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["公民"];
        $sakiClassCd = $this->sakiClassCds["公民"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高1
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $syoriSbuclass[] = $motoSubClass["倫理高1"];

            $setSbuclass[] = $sakiSubClass["倫理高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高2
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            $syoriSbuclass[] = $motoSubClass["政経高2"];

            $setSbuclass[] = $sakiSubClass["政経高2"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }
    }

    //数学
    function setSuugaku($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["数学"];
        $sakiClassCd = $this->sakiClassCds["数学"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高1
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $syoriSbuclass[] = $motoSubClass["数1高1"];
            $syoriSbuclass[] = $motoSubClass["数2高1"];

            $setSbuclass[] = $sakiSubClass["数Ⅰ高1"];
            $setSbuclass[] = $sakiSubClass["数A高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高2
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            $syoriSbuclass[] = $motoSubClass["数1高2"];
            $syoriSbuclass[] = $motoSubClass["数2高2"];
            $syoriSbuclass[] = $motoSubClass["数3高2"];

            $setSbuclass[] = $sakiSubClass["数B高2"];
            $setSbuclass[] = $sakiSubClass["数Ⅱ高2"];
            $setSbuclass[] = $sakiSubClass["数Ⅱ高2"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高3(2013年度まで)
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3" && CTRL_YEAR < "2014") {
            $syoriSbuclass[] = $motoSubClass["数1高3"];
            $syoriSbuclass[] = $motoSubClass["数2高3"];
            $syoriSbuclass[] = $motoSubClass["数3高3"];

            $setSbuclass[] = $sakiSubClass["数C高3"];
            $setSbuclass[] = $sakiSubClass["数Ⅲ高3"];
            $setSbuclass[] = $sakiSubClass["数Ⅲ高3"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);

            $checkSub[] = $motoSubClass["数2高3"];
            $checkSub[] = $motoSubClass["数3高3"];
            $dataFlg = false;
            foreach ($checkSub as $key => $subClassCd) {
                //対象データ取得
                $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subClassCd, "on", "", $this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $dataFlg = true;
                }
            }

            if (!$dataFlg) {
                $cpSyoriSbuclass[] = $motoSubClass["数1高3"];
                $cpSetSbuclass[] = $sakiSubClass["数Ⅲ高3"];
                $this->kakuKamokuSetData($db, $cpSyoriSbuclass, $cpSetSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
            }
        //高3(2014年度から)
        } else if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3") {
            //新規作成の場合、一旦レコードを削除する
            if ($this->field["CREATEDIV"] == 1) {
                $query = knje061mQuery::deleteClasscdStudyRecSql($schoolCd, $year, $schregNo, $annual, $sakiClassCd, $this);
                $db->query($query);
            }

            $checkSub[] = $motoSubClass["数1高3"];
            $checkSub[] = $motoSubClass["数2高3"];
            $checkSub[] = $motoSubClass["数3高3"];
            $dataFlg = false;
            $scoreArray = array();
            foreach ($checkSub as $key => $subClassCd) {
                //対象データ取得
                $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subClassCd, "on", "", $this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $scoreArray[] = $row["SCORE"];
                    //数2または数3に点がある
                    if ($motoSubClass["数2高3"] == $subClassCd || $motoSubClass["数3高3"] == $subClassCd) {
                        $dataFlg = true;
                    }
                }
            }

            //数2または数3に点がある
            if ($dataFlg) {
                $subclassCd = $sakiSubClass["数Ⅲ高3"];
                $score = $this->getMaxScore($scoreArray); //良い方の点
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            //数2かつ数3ともに点がない
            } else {
                $cpSyoriSbuclass[] = $motoSubClass["数1高3"];
                $cpSetSbuclass[] = $sakiSubClass["数活高3"];
                $this->kakuKamokuSetData($db, $cpSyoriSbuclass, $cpSetSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
            }
        }
    }

    //理科
    function setRika($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["理科"];
        $sakiClassCd = $this->sakiClassCds["理科"];
        $motoSubClass = $this->getMotoSubClass($motoClassCd);
        $sakiSubClass = $this->getSakiSubClass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //新規作成の場合、一旦レコードを削除する
        if ($this->field["CREATEDIV"] == 1) {
            $query = knje061mQuery::deleteClasscdStudyRecSql($schoolCd, $year, $schregNo, $annual, $sakiClassCd, $this);
            $db->query($query);
        }

        //高1(2011年度まで)
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1" && CTRL_YEAR < "2012") {
            //対象データ取得
            $dataFlg = false;
            $subclassCd = $motoSubClass["高1物理"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            $scoreArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $scoreArray[] = $row["SCORE"];
                $dataFlg = true;
            }
            $subclassCd = $motoSubClass["高1化学1"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $scoreArray[] = $row["SCORE"];
                $dataFlg = true;
            }
            //更新処理
            if ($dataFlg) {
                $subclassCd = $sakiSubClass["高1理科総合A"];
                $score = $this->getAvg($scoreArray);
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
            //対象データ取得
            $dataFlg = false;
            $subclassCd = $motoSubClass["高1生物"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            $score = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $score = $row["SCORE"];
                $dataFlg = true;
            }
            //更新処理
            if ($dataFlg) {
                $subclassCd = $sakiSubClass["高1生物Ⅰ"];
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
        //高1(2012年度から)
        } else if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $syoriSbuclass[] = $motoSubClass["高1物理"];
            $syoriSbuclass[] = $motoSubClass["高1化学"];
            $syoriSbuclass[] = $motoSubClass["高1生物"];

            $setSbuclass[] = $sakiSubClass["高1物理基礎"];
            $setSbuclass[] = $sakiSubClass["高1化学基礎"];
            $setSbuclass[] = $sakiSubClass["高1生物基礎"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高2(110～119)(2012年度まで)
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2" && CTRL_YEAR < "2013") {
            //対象データ取得
            $dataFlgSeibutuOnly = false;
            $subclassCd = $motoSubClass["高2生物"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataFlgSeibutuOnly = true;
            }
            //対象データ取得
            $dataFlg = false;
            $subclassCd = $motoSubClass["高2物理"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            $score = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $score = $row["SCORE"];
                $dataFlg = true;
                $dataFlgSeibutuOnly = false;
            }
            //更新処理
            if ($dataFlg) {
                $subclassCd = $sakiSubClass["高2物理Ⅰ"];
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
            //対象データ取得
            $dataFlg = false;
            $subclassCd = $motoSubClass["高2地学"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            $score = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $score = $row["SCORE"];
                $dataFlg = true;
                $dataFlgSeibutuOnly = false;
            }
            //更新処理
            if ($dataFlg) {
                $subclassCd = $sakiSubClass["高2地学Ⅰ"];
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
            //対象データ取得
            $dataFlg = false;
            $subclassCd = $motoSubClass["高2化学2"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            $scoreArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $scoreArray[] = $row["SCORE"];
                $dataFlg = true;
                $dataFlgSeibutuOnly = false;
            }
            $subclassCd = $motoSubClass["高2化学1"];
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subclassCd, "on", "", $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $scoreArray[] = $row["SCORE"];
            }
            //更新処理
            if (0 < get_count($scoreArray) && ($dataFlg || $dataFlgSeibutuOnly)) {
                $subclassCd = $sakiSubClass["高2化学Ⅰ"];
                $score = $this->getAvg($scoreArray);
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
        //高2(2013年度から)
        } else if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            $syoriSbuclass[] = $motoSubClass["高2物理"];
            $syoriSbuclass[] = $motoSubClass["高2化学"];
            $syoriSbuclass[] = $motoSubClass["高2生物"];
            $syoriSbuclass[] = $motoSubClass["高2地学"];

            $setSbuclass[] = $sakiSubClass["高2物理"];
            $setSbuclass[] = $sakiSubClass["高2化学"];
            $setSbuclass[] = $sakiSubClass["高2生物"];
            $setSbuclass[] = $sakiSubClass["高2地学基礎"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高3(2013年度まで)
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3" && CTRL_YEAR < "2014") {
            $gradeH3    = $this->annual;
            $yearH3     = $gradeArray[$gradeH3]["YEAR"];
            $annualH3   = $gradeArray[$gradeH3]["ANNUAL"];
            $gradeH2    = sprintf("%02d", ((int) $gradeH3 - 1));
            $yearH2     = $gradeArray[$gradeH2]["YEAR"];
            $annualH2   = $gradeArray[$gradeH2]["ANNUAL"];
            //新規作成の場合、一旦レコードを削除する
            if ($this->field["CREATEDIV"] == 1) {
                $query = knje061mQuery::deleteClasscdStudyRecSql($schoolCd, $yearH2, $schregNo, $annualH2, $sakiClassCd, $this);
                $db->query($query);
            }
            //パターン番号取得（高3）
            $syoriSbuclassH3[] = $motoSubClass["高3物理"];  //0
            $syoriSbuclassH3[] = $motoSubClass["高3化学2"]; //1
            $syoriSbuclassH3[] = $motoSubClass["高3生物"];  //2
            $syoriSbuclassH3[] = $motoSubClass["高3地学"];  //3
            $syoriScoreH3 = $this->rikaGetData($db, $syoriSbuclassH3, $yearH3, $recSeme, $schregNo, $motoClassCd);
            $patternH3 = $this->getPatternNo($syoriScoreH3, "H3");
            //パターン番号取得（高2）
            $syoriSbuclassH2[] = $motoSubClass["高2物理"];  //0
            $syoriSbuclassH2[] = $motoSubClass["高2化学2"]; //1
            $syoriSbuclassH2[] = $motoSubClass["高2生物"];  //2
            $syoriSbuclassH2[] = $motoSubClass["高2地学"];  //3
            $syoriSbuclassH2[] = $motoSubClass["高2化学1"]; //4
            $syoriScoreH2 = $this->rikaGetData($db, $syoriSbuclassH2, $yearH2, $recSeme, $schregNo, $motoClassCd);
            $patternH2 = $this->getPatternNo($syoriScoreH2, "H2");
            $patternNo = "";
            if (strlen($patternH3) && strlen($patternH2)) {
                $patternNo = $patternH3 .$patternH2; //パターン番号
            }
//if ($schregNo == "20010001") {
//echo "schregNo:" .$schregNo .", patternNo:" .$patternNo ."<BR>";
//echo "schregNo:" .$schregNo .", patternH3:" .$patternH3 .", patternH2:" .$patternH2 ."<BR>";
//var_dump($syoriScoreH2);
//}
            //更新処理（高3）
            $patternFlg = in_array($patternNo, $this->rikaPattern["高3物理Ⅱ"]["高3物理"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高3物理Ⅱ"];
                $score      = $syoriScoreH3[0]["score"];
                $this->rikaSetData($db, $yearH3, $gradeH3, $recSeme, $score, $annualH3, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高3化学Ⅱ"]["高3化学2"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高3化学Ⅱ"];
                $score      = $syoriScoreH3[1]["score"];
                $this->rikaSetData($db, $yearH3, $gradeH3, $recSeme, $score, $annualH3, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高3生物Ⅱ"]["高2生物と高3生物の平均"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高3生物Ⅱ"];
                $scoreArray = array();
                $scoreArray[] = $syoriScoreH2[2]["score"];
                $scoreArray[] = $syoriScoreH3[2]["score"];
                $score = $this->getAvg($scoreArray);
                $this->rikaSetData($db, $yearH3, $gradeH3, $recSeme, $score, $annualH3, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高3生物Ⅱ"]["高3生物"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高3生物Ⅱ"];
                $score      = $syoriScoreH3[2]["score"];
                $this->rikaSetData($db, $yearH3, $gradeH3, $recSeme, $score, $annualH3, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高3地学Ⅰ"]["高3地学"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高3地学Ⅰ"];
                $score      = $syoriScoreH3[3]["score"];
                $this->rikaSetData($db, $yearH3, $gradeH3, $recSeme, $score, $annualH3, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高3地学Ⅱ"]["高3地学"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高3地学Ⅱ"];
                $score      = $syoriScoreH3[3]["score"];
                $this->rikaSetData($db, $yearH3, $gradeH3, $recSeme, $score, $annualH3, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            //更新処理（高2）
            $patternFlg = in_array($patternNo, $this->rikaPattern["高2物理Ⅰ"]["高2物理"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高2物理Ⅰ"];
                $score      = $syoriScoreH2[0]["score"];
                $this->rikaSetData($db, $yearH2, $gradeH2, $recSeme, $score, $annualH2, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高2物理Ⅰ"]["高3物理1学期"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高2物理Ⅰ"];
                $score      = $this->rikaGetData2($db, $motoSubClass["高3物理"], $yearH3, "1", $schregNo, $motoClassCd);
                $this->rikaSetData($db, $yearH2, $gradeH2, $recSeme, $score, $annualH2, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高2化学Ⅰ"]["高2化学1と化学2の平均"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高2化学Ⅰ"];
                $scoreArray = array();
                $scoreArray[] = $syoriScoreH2[4]["score"];
                $scoreArray[] = $syoriScoreH2[1]["score"];
                $score = $this->getAvg($scoreArray);
                $this->rikaSetData($db, $yearH2, $gradeH2, $recSeme, $score, $annualH2, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高2化学Ⅰ"]["高2化学1"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高2化学Ⅰ"];
                $score      = $syoriScoreH2[4]["score"];
                $this->rikaSetData($db, $yearH2, $gradeH2, $recSeme, $score, $annualH2, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
            $patternFlg = in_array($patternNo, $this->rikaPattern["高2地学Ⅰ"]["高2地学"]);
            if ($patternFlg) {
                $subclassCd = $sakiSubClass["高2地学Ⅰ"];
                $score      = $syoriScoreH2[3]["score"];
                $this->rikaSetData($db, $yearH2, $gradeH2, $recSeme, $score, $annualH2, $schregNo, $sakiClassCd, $subclassCd, $gradeArray);
            }
        //高3(2014年度から)
        } else if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3") {
            $syoriSbuclass[] = $motoSubClass["高3物理"];
            $syoriSbuclass[] = $motoSubClass["高3化学"];
            $syoriSbuclass[] = $motoSubClass["高3生物"];
            $syoriSbuclass[] = $motoSubClass["高3地学"];

            $setSbuclass[] = $sakiSubClass["高3物理"];
            $setSbuclass[] = $sakiSubClass["高3化学"];
            $setSbuclass[] = $sakiSubClass["高3生物"];
            $setSbuclass[] = $sakiSubClass["高3地学"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }
    }

    //理科共通更新処理
    function rikaSetData($db, $year, $grade, $recSeme, $score, $annual, $schregNo, $sakiClassCd, $subclassCd, $gradeArray)
    {
        $schoolCd   = "0";

        $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
        $data = array();
        $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$grade]);
        $this->updateStudyRec($data, $db);
    }

    //理科共通対象データ取得
    function rikaGetData($db, $syoriSbuclass, $year, $recSeme, $schregNo, $motoClassCd)
    {
        $rtnArray = array();
        foreach ($syoriSbuclass as $key => $subClassCd) {
            $rtnArray[$key]["score"]    = "";
            $rtnArray[$key]["dataFlg"]  = false;
            //対象データ取得
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subClassCd, "on", "", $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rtnArray[$key]["score"]    = $row["SCORE"];
                $rtnArray[$key]["dataFlg"]  = true;
            }
        }
        return $rtnArray;
    }

    //理科共通対象データ取得
    function rikaGetData2($db, $subClassCd, $year, $recSeme, $schregNo, $motoClassCd)
    {
        $score = "";
        //対象データ取得
        $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subClassCd, "on", "", $this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $score = $row["SCORE"];
        }
        return $score;
    }

    //パターン番号取得
    // $dataFlg1:物理、$dataFlg2:化学、$dataFlg3:生物、$dataFlg4:地学
    // 1:物理・化学
    // 2:化学・生物
    // 3:化学・地学
    // 4:物理・生物
    // 5:物理・地学
    // 6:生物・地学
    // 7:化学のみ
    // 8:物理のみ
    // 9:生物のみ
    //0or10:地学のみ
    //11:選択なし
    function getPatternNo($syoriScore, $gradeFlg)
    {
        $dataFlg1 = $syoriScore[0]["dataFlg"];
        $dataFlg2 = $syoriScore[1]["dataFlg"];
        $dataFlg3 = $syoriScore[2]["dataFlg"];
        $dataFlg4 = $syoriScore[3]["dataFlg"];

        $rtnNo = "";
        //２選択
        if ( $dataFlg1 &&  $dataFlg2 && !$dataFlg3 && !$dataFlg4) return 1;
        if (!$dataFlg1 &&  $dataFlg2 &&  $dataFlg3 && !$dataFlg4) return 2;
        if (!$dataFlg1 &&  $dataFlg2 && !$dataFlg3 &&  $dataFlg4) return 3;
        if ( $dataFlg1 && !$dataFlg2 &&  $dataFlg3 && !$dataFlg4) return 4;
        if ( $dataFlg1 && !$dataFlg2 && !$dataFlg3 &&  $dataFlg4) return 5;
        if (!$dataFlg1 && !$dataFlg2 &&  $dataFlg3 &&  $dataFlg4) return 6;
        //１選択
        if (!$dataFlg1 &&  $dataFlg2 && !$dataFlg3 && !$dataFlg4) return 7;
        if ( $dataFlg1 && !$dataFlg2 && !$dataFlg3 && !$dataFlg4) return 8;
        if (!$dataFlg1 && !$dataFlg2 &&  $dataFlg3 && !$dataFlg4) return 9;
        if (!$dataFlg1 && !$dataFlg2 && !$dataFlg3 &&  $dataFlg4) return ($gradeFlg == "H2") ? 0 : 10;
        //０選択
        if (!$dataFlg1 && !$dataFlg2 && !$dataFlg3 && !$dataFlg4 && $gradeFlg == "H3") return 11;

        return $rtnNo;
    }

    //理科のパターン番号の配列　$this->rikaPattern["先科目"]["元科目"]
    function makeRikaPattern()
    {
        $this->rikaPattern["高2物理Ⅰ"]["高2物理"] = array(11, 14, 15, 18, 21, 24, 31, 34, 38, 41, 28, 
                                                           44, 45, 48, 51, 54, 55, 58, 61, 64, 65, 
                                                           68, 71, 74, 78, 81, 84, 85, 88, 91, 94, 
                                                           95, 98, 101, 104, 105, 108, 111, 114, 115, 118);

        $this->rikaPattern["高2物理Ⅰ"]["高3物理1学期"] = array(10, 40, 50, 80, 12, 13, 16, 17, 19, 42, 
                                                                43, 46, 47, 49, 52, 53, 56, 57, 59, 82, 
                                                                83, 86, 87, 89);

        $this->rikaPattern["高3物理Ⅱ"]["高3物理"] = array(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 
                                                           40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 
                                                           50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
                                                           80, 81, 82, 83, 84, 85, 86, 87, 88, 89);

        $this->rikaPattern["高2化学Ⅰ"]["高2化学1と化学2の平均"] = array(11, 12, 13, 17, 21, 22, 23, 27, 31, 32, 
                                                                         33, 37, 41, 42, 47, 51, 52, 57, 61, 62, 
                                                                         63, 67, 71, 72, 73, 77, 81, 82, 87, 91, 
                                                                         92, 93, 97, 101, 102, 103, 107, 111, 112, 113, 117);

        $this->rikaPattern["高2化学Ⅰ"]["高2化学1"] = array(10, 14, 15, 16, 18, 19, 20, 24, 25, 26, 69, 
                                                            28, 29, 30, 34, 35, 36, 38, 39, 70, 74, 
                                                            75, 76, 78, 79, 99, 109, 119);

        $this->rikaPattern["高3化学Ⅱ"]["高3化学2"] = array(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 
                                                            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
                                                            30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 
                                                            70, 71, 72, 73, 74, 75, 76, 77, 78, 79);

        $this->rikaPattern["高3生物Ⅱ"]["高2生物と高3生物の平均"] = array(22, 24, 26, 29, 42, 44, 46, 49, 62, 64, 
                                                                          66, 69, 92, 94, 96, 99);

        $this->rikaPattern["高3生物Ⅱ"]["高3生物"] = array(20, 21, 23, 25, 27, 28, 40, 41, 43, 45, 
                                                           47, 48, 60, 61, 63, 65, 67, 68, 90, 91, 
                                                           93, 95, 97, 98);

        $this->rikaPattern["高2地学Ⅰ"]["高2地学"] = array(20, 23, 25, 26, 30, 33, 35, 36, 40, 43, 
                                                           45, 46, 50, 53, 55, 56, 60, 63, 65, 66, 
                                                           70, 73, 75, 76, 80, 83, 85, 86, 90, 93, 
                                                           95, 96, 100, 103, 105, 106, 110, 113, 115, 116);

        $this->rikaPattern["高3地学Ⅰ"]["高3地学"] = array(31, 32, 34, 37, 38, 39, 51, 52, 54, 57, 
                                                           58, 59, 61, 62, 64, 67, 68, 69, 101, 102, 
                                                           104, 107, 108, 109);

        $this->rikaPattern["高3地学Ⅱ"]["高3地学"] = array(30, 33, 35, 36, 50, 53, 55, 56, 60, 63, 
                                                           65, 66, 100, 103, 105, 106);
    }

    //保健・体育
    function setTaiku($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["保健体育"];
        $sakiClassCd = $this->sakiClassCds["保健体育"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高1
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $syoriSbuclass[] = $motoSubClass["体育高1"];
            $syoriSbuclass[] = $motoSubClass["体育高1"];

            $setSbuclass[] = $sakiSubClass["保健高1"];
            $setSbuclass[] = $sakiSubClass["体育高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高2
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            $syoriSbuclass[] = $motoSubClass["体育高2"];
            $syoriSbuclass[] = $motoSubClass["体育高2"];

            $setSbuclass[] = $sakiSubClass["保健高2"];
            $setSbuclass[] = $sakiSubClass["体育高2"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }

        //高3
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3") {
            $syoriSbuclass[] = $motoSubClass["体育1高3"];

            $setSbuclass[] = $sakiSubClass["体育高3"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }
    }

    //芸術
    function setGeijutu($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["芸術"];
        $sakiClassCd = $this->sakiClassCds["芸術"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高1
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $syoriSbuclass[] = $motoSubClass["音楽高1"];
            $syoriSbuclass[] = $motoSubClass["美術高1"];
            $syoriSbuclass[] = $motoSubClass["書道高1"];

            $setSbuclass[] = $sakiSubClass["音楽Ⅰ高1"];
            $setSbuclass[] = $sakiSubClass["美術Ⅰ高1"];
            $setSbuclass[] = $sakiSubClass["書道Ⅰ高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }
    }

    //外国語
    function setGaikoku($schregNo, $gradeArray, $db)
    {
        $motoClassCd   = $this->motoClassCds["外国語"];
        $motoClassCd2  = $this->motoClassCds["第2外国語"];
        $sakiClassCd   = $this->sakiClassCds["外国語"];
        $sakiClassCd2  = $this->sakiClassCds["第2外国語"];
        $motoSubClass  = $this->getMotoSubclass($motoClassCd);
        $motoSubClass2 = $this->getMotoSubclass($motoClassCd2);
        $sakiSubClass  = $this->getSakiSubclass($sakiClassCd);
        $sakiSubClass2 = $this->getSakiSubclass($sakiClassCd2);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高1(2012年度まで)
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1" && CTRL_YEAR < "2013") {
            $syoriSbuclass[] = $motoSubClass["英語1高1"];

            $setSbuclass[] = $sakiSubClass["OC高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);

            //対象データ取得
            $scoreArray = array();
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, "", $scoreIsNotNull, $orderDiv, $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($motoSubClass["英語2高1"] == $row["SUBCLASSCD"] || 
                    $motoSubClass["英語3高1"] == $row["SUBCLASSCD"]) {
                    if (strlen($row["SCORE"])) {
                        $scoreArray[] = $row["SCORE"];
                    }
                }
            }
            //更新処理・・・１科目でもあれば処理する。
            if (0 < get_count($scoreArray)) {
                $subclassCd = $sakiSubClass["英語Ⅰ高1"];
                $score      = $this->getAvg($scoreArray);//３科目のうちの上位２科目の平均
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
        //高1(2013年度から)
        } else if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $syoriSbuclass[] = $motoSubClass["英語3高1"];

            $setSbuclass[] = $sakiSubClass["英語表現Ⅰ高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);

            //対象データ取得
            $scoreArray = array();
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, "", $scoreIsNotNull, $orderDiv, $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($motoSubClass["英語2高1"] == $row["SUBCLASSCD"] || 
                    $motoSubClass["英語1高1"] == $row["SUBCLASSCD"]) {
                    if (strlen($row["SCORE"])) {
                        $scoreArray[] = $row["SCORE"];
                    }
                }
            }
            //更新処理・・・１科目でもあれば処理する。
            if (0 < get_count($scoreArray)) {
                $subclassCd = $sakiSubClass["C英語Ⅰ高1"];
                $score      = $this->getAvg($scoreArray);//３科目のうちの上位２科目の平均
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                $this->updateStudyRec($data, $db);
            }
        }

        //高2
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            //英語(2014年度から)
            if ("2014" <= CTRL_YEAR) {
                $syoriSbuclass[] = $motoSubClass["英語3高2"];

                $setSbuclass[] = $sakiSubClass["英語表現Ⅱ高2"];
                $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);

                //対象データ取得
                $scoreArray = array();
                $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, "", $scoreIsNotNull, $orderDiv, $this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($motoSubClass["英語2高2"] == $row["SUBCLASSCD"] || 
                        $motoSubClass["英語1高2"] == $row["SUBCLASSCD"]) {
                        if (strlen($row["SCORE"])) {
                            $scoreArray[] = $row["SCORE"];
                        }
                    }
                }
                //更新処理・・・１科目でもあれば処理する。
                if (0 < get_count($scoreArray)) {
                    $subclassCd = $sakiSubClass["C英語Ⅱ高2"];
                    $score      = $this->getAvg($scoreArray);//平均
                    $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                    $data = array();
                    $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                    $this->updateStudyRec($data, $db);
                }
            } else {
                $syoriSbuclass[] = $motoSubClass["英語1高2"];
                $syoriSbuclass[] = $motoSubClass["英語2高2"];
                $syoriSbuclass[] = $motoSubClass["英語3高2"];

                $setSbuclass[] = $sakiSubClass["英語Ⅱ高2"];
                $setSbuclass[] = $sakiSubClass["リーディング高2"];
                $setSbuclass[] = $sakiSubClass["ライティング高2"];
                $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
            }

            //外国第２
            $syoriSbuclass2[] = $motoSubClass2["独高2"];
            $syoriSbuclass2[] = $motoSubClass2["仏高2"];
            $syoriSbuclass2[] = $motoSubClass2["中高2"];
            $syoriSbuclass2[] = $motoSubClass2["韓・朝高2"];

            $setSbuclass2[] = $sakiSubClass2["ドイツ語高2"];
            $setSbuclass2[] = $sakiSubClass2["フランス語高2"];
            $setSbuclass2[] = $sakiSubClass2["中国語高2"];
            $setSbuclass2[] = $sakiSubClass2["韓国・朝鮮語高2"];
            $this->kakuKamokuSetData($db, $syoriSbuclass2, $setSbuclass2, $year, $recSeme, $annual, $schregNo, $motoClassCd2, $sakiClassCd2, $gradeArray[$this->annual]);

            $year1 = $gradeArray["0".($this->annual - 1)]["YEAR"];
            $annual1 = $gradeArray["0".($this->annual - 1)]["ANNUAL"];
            $syoriSbuclass1[] = $motoSubClass2["独高1"];
            $syoriSbuclass1[] = $motoSubClass2["仏高1"];
            $syoriSbuclass1[] = $motoSubClass2["中高1"];
            $syoriSbuclass1[] = $motoSubClass2["韓・朝高1"];

            $setSbuclass1[] = $sakiSubClass2["ドイツ語高1"];
            $setSbuclass1[] = $sakiSubClass2["フランス語高1"];
            $setSbuclass1[] = $sakiSubClass2["中国語高1"];
            $setSbuclass1[] = $sakiSubClass2["韓国・朝鮮語高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass1, $setSbuclass1, $year1, $recSeme, $annual1, $schregNo, $motoClassCd2, $sakiClassCd2, $gradeArray["0".($this->annual - 1)]);
        }

        //高3
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "3") {
            $english1 = false;
            $english2 = false;
            $english3 = false;

            //対象データ取得
            $scoreArray = array();
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, "", $scoreIsNotNull, $orderDiv, $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($motoSubClass["英語1高3"] == $row["SUBCLASSCD"]) {
                    if (strlen($row["SCORE"])) {
                        $scoreArray[] = $row["SCORE"];
                        $english1 = true;
                    }
                }
                if ($motoSubClass["英語2高3"] == $row["SUBCLASSCD"]) {
                    if (strlen($row["SCORE"])) {
                        $scoreArray[] = $row["SCORE"];
                        $english2 = true;
                    }
                }
                if ($motoSubClass["英語3高3"] == $row["SUBCLASSCD"]) {
                    if (strlen($row["SCORE"])) {
                        $scoreArray[] = $row["SCORE"];
                        $english3 = true;
                    }
                }
            }
            if ($english1 && $english2 && $english3) {
                $syoriSbuclass[] = $motoSubClass["英語1高3"];
                $syoriSbuclass[] = $motoSubClass["英語2高3"];
                $syoriSbuclass[] = $motoSubClass["英語3高3"];

                $setSbuclass[] = $sakiSubClass["英語Ⅱ高3"];
                $setSbuclass[] = $sakiSubClass["リーディング高3"];
                $setSbuclass[] = $sakiSubClass["ライティング高3"];
                $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
            } else if ($english1 && $english2) {
                $syoriSbuclass[] = $motoSubClass["英語1高3"];
                $syoriSbuclass[] = $motoSubClass["英語2高3"];

                $setSbuclass[] = $sakiSubClass["英語Ⅱ高3"];
                $setSbuclass[] = $sakiSubClass["リーディング高3"];
                $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);

                //更新処理・・・１科目でもあれば処理する。
                if (0 < get_count($scoreArray)) {
                    $subclassCd = $sakiSubClass["ライティング高3"];
                    $score      = $this->getAvg($scoreArray);//３科目のうちの上位２科目の平均
                    $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                    $data = array();
                    $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                    $this->updateStudyRec($data, $db);
                }
            } else if ($english2 && $english3) {
                $syoriSbuclass[] = $motoSubClass["英語2高3"];
                $syoriSbuclass[] = $motoSubClass["英語3高3"];

                $setSbuclass[] = $sakiSubClass["リーディング高3"];
                $setSbuclass[] = $sakiSubClass["ライティング高3"];
                $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);

                //更新処理・・・１科目でもあれば処理する。
                if (0 < get_count($scoreArray)) {
                    $subclassCd = $sakiSubClass["英語Ⅱ高3"];
                    $score      = $this->getAvg($scoreArray);//３科目のうちの上位２科目の平均
                    $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                    $data = array();
                    $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                    $this->updateStudyRec($data, $db);
                }
            } else if ($english1 && $english3) {
                $syoriSbuclass[] = $motoSubClass["英語1高3"];
                $syoriSbuclass[] = $motoSubClass["英語3高3"];

                $setSbuclass[] = $sakiSubClass["英語Ⅱ高3"];
                $setSbuclass[] = $sakiSubClass["ライティング高3"];
                $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);

                //更新処理・・・１科目でもあれば処理する。
                if (0 < get_count($scoreArray)) {
                    $subclassCd = $sakiSubClass["リーディング高3"];
                    $score      = $this->getAvg($scoreArray);//３科目のうちの上位２科目の平均
                    $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                    $data = array();
                    $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
                    $this->updateStudyRec($data, $db);
                }
            }

            //外国第２
            $syoriSbuclass3[] = $motoSubClass2["独高3"];
            $syoriSbuclass3[] = $motoSubClass2["仏高3"];
            $syoriSbuclass3[] = $motoSubClass2["中高3"];
            $syoriSbuclass3[] = $motoSubClass2["韓・朝高3"];

            $setSbuclass3[] = $sakiSubClass2["ドイツ語高3"];
            $setSbuclass3[] = $sakiSubClass2["フランス語高3"];
            $setSbuclass3[] = $sakiSubClass2["中国語高3"];
            $setSbuclass3[] = $sakiSubClass2["韓国・朝鮮語高3"];
            $this->kakuKamokuSetData($db, $syoriSbuclass3, $setSbuclass3, $year, $recSeme, $annual, $schregNo, $motoClassCd2, $sakiClassCd2, $gradeArray[$this->annual]);

            $year2 = $gradeArray["0".($this->annual - 1)]["YEAR"];
            $annual2 = $gradeArray["0".($this->annual - 1)]["ANNUAL"];
            $syoriSbuclass2[] = $motoSubClass2["独高2"];
            $syoriSbuclass2[] = $motoSubClass2["仏高2"];
            $syoriSbuclass2[] = $motoSubClass2["中高2"];
            $syoriSbuclass2[] = $motoSubClass2["韓・朝高2"];

            $setSbuclass2[] = $sakiSubClass2["ドイツ語高2"];
            $setSbuclass2[] = $sakiSubClass2["フランス語高2"];
            $setSbuclass2[] = $sakiSubClass2["中国語高2"];
            $setSbuclass2[] = $sakiSubClass2["韓国・朝鮮語高2"];
            $this->kakuKamokuSetData($db, $syoriSbuclass2, $setSbuclass2, $year2, $recSeme, $annual2, $schregNo, $motoClassCd2, $sakiClassCd2, $gradeArray["0".($this->annual - 1)]);

            $year1 = $gradeArray["0".($this->annual - 2)]["YEAR"];
            $annual1 = $gradeArray["0".($this->annual - 2)]["ANNUAL"];
            $syoriSbuclass1[] = $motoSubClass2["独高1"];
            $syoriSbuclass1[] = $motoSubClass2["仏高1"];
            $syoriSbuclass1[] = $motoSubClass2["中高1"];
            $syoriSbuclass1[] = $motoSubClass2["韓・朝高1"];

            $setSbuclass1[] = $sakiSubClass2["ドイツ語高1"];
            $setSbuclass1[] = $sakiSubClass2["フランス語高1"];
            $setSbuclass1[] = $sakiSubClass2["中国語高1"];
            $setSbuclass1[] = $sakiSubClass2["韓国・朝鮮語高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass1, $setSbuclass1, $year1, $recSeme, $annual1, $schregNo, $motoClassCd2, $sakiClassCd2, $gradeArray["0".($this->annual - 2)]);
        }
    }

    //家庭
    function setKatei($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["家庭"];
        $sakiClassCd = $this->sakiClassCds["家庭"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高1
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $syoriSbuclass[] = $motoSubClass["家庭科基礎高1"];

            $setSbuclass[] = $sakiSubClass["家庭科基礎高1"];
            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }
    }

    //情報
    function setJouhou($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["情報"];
        $sakiClassCd = $this->sakiClassCds["情報"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高2
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "2") {
            //社会と情報(2014年度から)
            if ("2014" <= CTRL_YEAR) {
                $syoriSbuclass[] = $motoSubClass["社会と情報高2"];
                $setSbuclass[] = $sakiSubClass["社会と情報高2"];
            } else {
                $syoriSbuclass[] = $motoSubClass["情報C高2"];
                $setSbuclass[] = $sakiSubClass["情報C高2"];
            }

            $this->kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeArray[$this->annual]);
        }
    }

    //総合的な学習
    function setSougou($schregNo, $gradeArray, $db)
    {
        $motoClassCd = $this->motoClassCds["総合"];
        $sakiClassCd = $this->sakiClassCds["総合"];
        $motoSubClass = $this->getMotoSubclass($motoClassCd);
        $sakiSubClass = $this->getSakiSubclass($sakiClassCd);

        $schoolCd   = "0";
        $year       = $gradeArray[$this->annual]["YEAR"];
        $annual     = $gradeArray[$this->annual]["ANNUAL"];
        $recSeme    = "9";

        //高1・・・総合的な学習の時間（3単位）を強制的に作成する
        if ($this->regdGdat[$this->annual]["GRADE_CD"] == "1") {
            $subclassCd = $sakiSubClass["総合高1"];
            $valuation = "";
            $data = array();
            $data = $this->getSougouFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeArray[$this->annual]);
            $this->updateStudyRec($data, $db);
        }
    }

    //共通処理
    function kakuKamokuSetData($db, $syoriSbuclass, $setSbuclass, $year, $recSeme, $annual, $schregNo, $motoClassCd, $sakiClassCd, $gradeData)
    {
        $schoolCd = "0";
        foreach ($syoriSbuclass as $key => $subClassCd) {
            $dataFlg = false;
            //対象データ取得
            $query = knje061mQuery::getRecordScoreSql($year, $recSeme, $schregNo, $motoClassCd, $subClassCd, "", "", $this);
            $result = $db->query($query);
            $score = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $score = $row["SCORE"];
                $dataFlg = true;
            }
            //更新処理
            if ($dataFlg) {
                $subclassCd = $setSbuclass[$key];
                $valuation  = $this->getAssessData($score, $db); //10段階を5段階に自動換算
                $data = array();
                $data = $this->getFieldData($db, $schoolCd, $year, $schregNo, $annual, $sakiClassCd, $subclassCd, $valuation, $gradeData);
                $this->updateStudyRec($data, $db);
            }
        }
    }

    //平均処理
    function getAvg($scoreArray)
    {
        $total = 0;
        foreach ($scoreArray as $key => $val) {
            $total += $val;
        }
        if (get_count($scoreArray) > 0) {
            $ret = floor($total / get_count($scoreArray) * 10 / 10);
            return $ret;
        } else {
            return "";
        }
    }

    //最高点処理
    function getMaxScore($scoreArray)
    {
        $maxScore = 0;
        foreach ($scoreArray as $key => $val) {
            if ($maxScore < $val) $maxScore = $val;
        }
        if (get_count($scoreArray) > 0) {
            $ret = $maxScore;
            return $ret;
        } else {
            return "";
        }
    }

    //フィールド処理
    function getFieldData($db, $schoolCd, $year, $schregNo, $annual, $classCd, $subclassCd, $valuation, $gradeData)
    {
        $query = knje061mQuery::getCredits($year, $gradeData, $classCd, $subclassCd, $this);
        $credits = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["SCHOOLCD"]       = $schoolCd;
        $data["YEAR"]           = $year;
        $data["SCHREGNO"]       = $schregNo;
        $data["ANNUAL"]         = $annual;
        $data["CLASSCD"]        = $classCd;
        $data["SUBCLASSCD"]     = $subclassCd;
        $data["VALUATION"]      = $valuation;
        if ($valuation > 0) {
            $data["GET_CREDIT"]     = $valuation > 1 ? $credits["CREDITS"] : 0;
            $data["COMP_CREDIT"]    = $credits["CREDITS"];
        } else {
            $data["GET_CREDIT"]     = "";
            $data["COMP_CREDIT"]    = "";
        }
        $data["ADD_CREDIT"]     = "";
        return $data;
    }

    //フィールド処理(総合的な学習)
    function getSougouFieldData($db, $schoolCd, $year, $schregNo, $annual, $classCd, $subclassCd, $valuation, $gradeData) {
        $data = array();
        $data["SCHOOLCD"]       = $schoolCd;
        $data["YEAR"]           = $year;
        $data["SCHREGNO"]       = $schregNo;
        $data["ANNUAL"]         = $annual;
        $data["CLASSCD"]        = $classCd;
        $data["SUBCLASSCD"]     = $subclassCd;
        $data["VALUATION"]      = $valuation;
        $data["GET_CREDIT"]     = 3;
        $data["COMP_CREDIT"]    = 3;
        $data["ADD_CREDIT"]     = "";
        return $data;
    }

    //10段階を5段階に自動換算する。換算テーブル（ASSESS_MST）
    function getAssessData($score, $db)
    {
        $query = knje061mQuery::getAssessMstSql($score);
        $assess = $db->getOne($query);
        return $assess;
    }

    function getMotoSubclass($classCd)
    {
        return $this->motoSubClassCds[$classCd];
    }

    function getSakiSubclass($classCd)
    {
        return $this->sakiSubClassCds[$classCd];
    }

    //元・先の科目配列
    function makeSubClassCds()
    {
        $this->motoSubClassCds[$this->motoClassCds["国語"]] = array("現代文高1"  => "320010",
                                                                    "古典高1"    => "320020",
                                                                    "漢文高1"    => "320030",
                                                                    "現代文高2"  => "320010",
                                                                    "古典高2"    => "320020",
                                                                    "漢文高2"    => "320030",
                                                                    "現代文高3"  => "320010",
                                                                    "古典高3"    => "320020",
                                                                    "漢文高3"    => "320030"
                                                                   );

        $this->sakiSubClassCds[$this->sakiClassCds["国語"]] = array("現代文高1"      => "620020",
                                                                    "国語総合高1"    => "620010",
                                                                    "現代文高2"      => "620020",
                                                                    "古典高2"        => "620030",
                                                                    "古典講読高2"    => "620040",
                                                                    "現代文B高2"     => "620020",
                                                                    "古典A高2"       => "620030",
                                                                    "古典B高2"       => "620040",
                                                                    "現代文高3"      => "620020",
                                                                    "古典高3"        => "620030",
                                                                   );

        $this->motoSubClassCds[$this->motoClassCds["地理歴史"]] = array("世界史高1"     => "340010",
                                                                        "世界史高2"     => "340010",
                                                                        "日本史高2"     => "340020",
                                                                        "地理高2"       => "340030",
                                                                        "世界史高3"     => "340010",
                                                                        "日本史高3"     => "340020",
                                                                        "地理高3"       => "340030"
                                                                       );

        $this->sakiSubClassCds[$this->sakiClassCds["地理歴史"]] = array("世界史B高1"  => "640010",
                                                                        "世界史B高2"  => "640010",
                                                                        "日本史B高2"  => "640020",
                                                                        "地理B高2"    => "640030",
                                                                        "世界史B高3"  => "640010",
                                                                        "日本史B高3"  => "640020",
                                                                        "地理B高3"    => "640030"
                                                                       );

        $this->motoSubClassCds[$this->motoClassCds["公民"]] = array("倫理高1"  => "360010",
                                                                    "政経高1"  => "360020",
                                                                    "政経高2"  => "360020"
                                                                   );

        $this->sakiSubClassCds[$this->sakiClassCds["公民"]] = array("倫理高1"  => "660010",
                                                                    "政経高2"  => "660020"
                                                                   );

        $this->motoSubClassCds[$this->motoClassCds["数学"]] = array("数1高1"  => "380010",
                                                                    "数2高1"  => "380020",
                                                                    "数1高2"  => "380010",
                                                                    "数2高2"  => "380020",
                                                                    "数3高2"  => "380030",
                                                                    "数1高3"  => "380010",
                                                                    "数2高3"  => "380020",
                                                                    "数3高3"  => "380030"
                                                                   );

        $this->sakiSubClassCds[$this->sakiClassCds["数学"]] = array("数Ⅰ高1" => "680010",
                                                                    "数A高1"  => "680040",
                                                                    "数Ⅱ高2" => "680020",
                                                                    "数B高2"  => "680050",
                                                                    "数Ⅲ高3" => "680030",
                                                                    "数C高3"  => "680060",
                                                                    "数活高3" => "680070"
                                                                   );

        $this->motoSubClassCds[$this->motoClassCds["理科"]] = array("高1物理"     => "400010",
                                                                    "高1化学1"    => "400020",
                                                                    "高1生物"     => "400030",
                                                                    "高1化学"     => "400060",
                                                                    "高2化学1"    => "400020",
                                                                    "高2物理"     => "400010",
                                                                    "高2化学2"    => "400050",
                                                                    "高2生物"     => "400030",
                                                                    "高2地学"     => "400040",
                                                                    "高2化学"     => "400060",
                                                                    "高3物理"     => "400010",
                                                                    "高3化学2"    => "400050",
                                                                    "高3生物"     => "400030",
                                                                    "高3地学"     => "400040",
                                                                    "高3化学"     => "400060"
                                                                   );

        $this->sakiSubClassCds[$this->sakiClassCds["理科"]] = array("高1理科総合A"    => "700010",
                                                                    "高1生物Ⅰ"       => "700060",
                                                                    "高1物理基礎"     => "701020",
                                                                    "高1化学基礎"     => "701040",
                                                                    "高1生物基礎"     => "701060",
                                                                    "高2物理Ⅰ"       => "700020",
                                                                    "高2化学Ⅰ"       => "700040",
                                                                    "高2地学Ⅰ"       => "700080",
                                                                    "高2物理"         => "701030",
                                                                    "高2化学"         => "701050",
                                                                    "高2生物"         => "701070",
                                                                    "高2地学基礎"     => "701080",
                                                                    "高3物理Ⅱ"       => "700030",
                                                                    "高3化学Ⅱ"       => "700050",
                                                                    "高3生物Ⅱ"       => "700070",
                                                                    "高3地学Ⅰ"       => "700080",
                                                                    "高3地学Ⅱ"       => "700090",
                                                                    "高3物理"         => "701030",
                                                                    "高3化学"         => "701050",
                                                                    "高3生物"         => "701070",
                                                                    "高3地学"         => "701090"
                                                                   );

        $this->motoSubClassCds[$this->motoClassCds["保健体育"]] = array("体育高1"     => "420020",
                                                                        "体育高2"     => "420020",
                                                                        "体育1高3"    => "420020"
                                                                       );

        $this->sakiSubClassCds[$this->sakiClassCds["保健体育"]] = array("保健高1"   => "720010",
                                                                        "体育高1"   => "720020",
                                                                        "保健高2"   => "720010",
                                                                        "体育高2"   => "720020",
                                                                        "体育高3"   => "720020"
                                                                       );

        $this->motoSubClassCds[$this->motoClassCds["芸術"]] = array("音楽高1"  => "440010",
                                                                    "美術高1"  => "440020",
                                                                    "書道高1"  => "440030"
                                                                   );

        $this->sakiSubClassCds[$this->sakiClassCds["芸術"]] = array("音楽Ⅰ高1"  => "740010",
                                                                    "美術Ⅰ高1"  => "740020",
                                                                    "書道Ⅰ高1"  => "740030"
                                                                   );

        $this->motoSubClassCds[$this->motoClassCds["外国語"]] = array("英語1高1"     => "460010",
                                                                      "英語2高1"     => "460020",
                                                                      "英語3高1"     => "460030",
                                                                      "英語1高2"     => "460010",
                                                                      "英語2高2"     => "460020",
                                                                      "英語3高2"     => "460030",
                                                                      "英語1高3"     => "460010",
                                                                      "英語2高3"     => "460020",
                                                                      "英語3高3"     => "460030"
                                                                     );

        $this->motoSubClassCds[$this->motoClassCds["第2外国語"]] = array("独高1"        => "480010",
                                                                         "仏高1"        => "480020",
                                                                         "中高1"        => "480030",
                                                                         "韓・朝高1"    => "480040",
                                                                         "独高2"        => "480010",
                                                                         "仏高2"        => "480020",
                                                                         "中高2"        => "480030",
                                                                         "韓・朝高2"    => "480040",
                                                                         "独高3"        => "480010",
                                                                         "仏高3"        => "480020",
                                                                         "中高3"        => "480030",
                                                                         "韓・朝高3"    => "480040"
                                                                        );

        $this->sakiSubClassCds[$this->sakiClassCds["外国語"]] = array("OC高1"           => "760110",
                                                                      "英語Ⅰ高1"       => "760120",
                                                                      "C英語Ⅰ高1"      => "763010",
                                                                      "英語表現Ⅰ高1"   => "763020",
                                                                      "英語Ⅱ高2"       => "760130",
                                                                      "リーディング高2" => "760140",
                                                                      "ライティング高2" => "760160",
                                                                      "C英語Ⅱ高2"      => "763030",
                                                                      "英語表現Ⅱ高2"   => "763040",
                                                                      "英語Ⅱ高3"       => "760130",
                                                                      "リーディング高3" => "760140",
                                                                      "ライティング高3" => "760160",
                                                                      "ドイツ語高1"     => "760210",
                                                                      "フランス語高1"   => "760310",
                                                                      "中国語高1"       => "760410",
                                                                      "韓国・朝鮮語高1" => "760510",
                                                                      "ドイツ語高2"     => "760210",
                                                                      "フランス語高2"   => "760310",
                                                                      "中国語高2"       => "760410",
                                                                      "韓国・朝鮮語高2" => "760510",
                                                                      "ドイツ語高3"     => "760210",
                                                                      "フランス語高3"   => "760310",
                                                                      "中国語高3"       => "760410",
                                                                      "韓国・朝鮮語高3" => "760510"
                                                                     );

        $this->motoSubClassCds[$this->motoClassCds["家庭"]] = array("家庭科基礎高1"  => "500010");

        $this->sakiSubClassCds[$this->sakiClassCds["家庭"]] = array("家庭科基礎高1"  => "780010");

        $this->motoSubClassCds[$this->motoClassCds["情報"]] = array("情報C高2"      => "520010",
                                                                    "社会と情報高2" => "520020"
                                                                   );

        $this->sakiSubClassCds[$this->sakiClassCds["情報"]] = array("情報C高2"      => "800010",
                                                                    "社会と情報高2" => "800020"
                                                                   );

        $this->motoSubClassCds[$this->motoClassCds["総合"]] = array("総合高1"  => "900200");

        $this->sakiSubClassCds[$this->sakiClassCds["総合"]] = array("総合高1"  => "900200");
    }

    //更新処理
    function updateStudyRec($data, $db)
    {
        //新規作成
        if ($this->field["CREATEDIV"] == 1) {
            $query = knje061mQuery::deleteStudyRecSql($data, $this);
            $db->query($query);
            $query = knje061mQuery::insertStudyRecSql($data, $this);
            $db->query($query);
        //追加作成
        } elseif ($this->field["CREATEDIV"] == 2) {
            $query = knje061mQuery::existsStudyRecSql($data, $this);
            if ($db->getOne($query) == 0) {
                $query = knje061mQuery::insertStudyRecSql($data, $this);
                $db->query($query);
            }
        //上書作成
        } else {
            $query = knje061mQuery::existsStudyRecSql($data, $this);
            if ($db->getOne($query) == 0) {
                $query = knje061mQuery::insertStudyRecSql($data, $this);
                $db->query($query);
            } else {
                $query = knje061mQuery::updateStudyRecSql($data, $this);
                $db->query($query);
            }
        }
    }


    /****************************/
    /* ↓↓↓ 中学(成績) ↓↓↓ */
    /****************************/
    /*   KNJE064Mの処理を移行   */
    /****************************/

    //メイン
    function updateJuniorHighSchool()
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

        //成績データを参照する学期
        $semester = ($this->chugaku_chosasho == "on") ? "2" : "9";

        $query = knje061mQuery::getStudent($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $this->makeSuugaku($db, $semester, $schregNo);
            $this->makeOngaku($db, $semester, $schregNo);
            $this->makeBijutu($db, $semester, $schregNo);
            $this->makeEigo($db, $semester, $schregNo);
            $this->makeSyakai2010($db, $semester, $schregNo);
            $this->makeRika2010($db, $semester, $schregNo);
            $this->makeTaiiku2010($db, $semester, $schregNo);
            $this->makeKokugo2010($db, $semester, $schregNo);
            if ($loginYear < "2010") {
                $this->makeGijutuKatei($db, $semester, $schregNo);
            } else {
                $this->makeGijutuKatei2010($db, $semester, $schregNo);
            }
        }

        $db->commit();
        //ＤＢ切断
        Query::dbCheckIn($db);
    }

    //国語
    function makeKokugo($db, $semester, $schregNo) {
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
        knje061mQuery::deleteData($db, $schregNo, "02", $this);

        //国語１
        $query = knje061mQuery::getScore($semester, $schregNo, "020010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "02", 1, $kanten[$score], $this);

        //国語２
        $query = knje061mQuery::getScore($semester, $schregNo, "020020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "02", 3, $kanten[$score], $this);

        //国語３
        $query = knje061mQuery::getScore($semester, $schregNo, "020030", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "02", 5, $kanten2[$score], $this);

        //評定
        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('020010', '020020', '020030')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 3 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        //knje061mQuery::insData($db, $schregNo, "0299", $this->hyouka[$score]);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "02", $this->hyouka[$score], $this);
    }

    //社会
    function makeSyakai($db, $semester, $schregNo) {
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
        knje061mQuery::deleteData($db, $schregNo, "04", $this);

        //世界史
        $query = knje061mQuery::getScore($semester, $schregNo, "040040", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "04", 1, $kanten[$score], $this);

        //日本史
        $query = knje061mQuery::getScore($semester, $schregNo, "040050", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "04", 3, $kanten[$score], $this);

        //評定
        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('040040', '040050')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        //knje061mQuery::insData($db, $schregNo, "0499", $this->hyouka[$score]);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "04", $this->hyouka[$score], $this);
    }

    //数学
    function makeSuugaku($db, $semester, $schregNo) {
        //060010-数学１：平均（小数点切捨て）を使用して、音楽と同様
        //060020-数学２

        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('060010', '060020')", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "06", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "06", $this->hyouka[$score], $this);
    }

    //理科
    function makeRika($db, $semester, $schregNo) {
        //080030-物理：平均（小数点切捨て）を使用して、音楽と同様
        //080040-化学

        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('080030', '080040')", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "08", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "08", $this->hyouka[$score], $this);
    }

    //音楽
    function makeOngaku($db, $semester, $schregNo) {
        //140010-音楽-10

        $query = knje061mQuery::getScore($semester, $schregNo, "140010", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "10", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "10", $this->hyouka[$score], $this);
    }

    //美術
    function makeBijutu($db, $semester, $schregNo) {
        //140020-美術-12

        $query = knje061mQuery::getScore($semester, $schregNo, "140020", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "12", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "12", $this->hyouka[$score], $this);
    }

    //保健体育
    function makeTaiiku($db, $semester, $schregNo) {
        //160010-保健：平均（小数点切捨て）を使用して、音楽と同様
        //160020-体育

        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('160010', '160020')", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "16", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "16", $this->hyouka[$score], $this);
    }

    //技術・家庭
    function makeGijutuKatei($db, $semester, $schregNo) {
        //180020-家庭科：(中１)は、家庭科と技術家庭の成績のいい方
        //180001-技術家庭：
        //080010-科学Ａ：(中２・中３)は、科学Ａと科学Ｂの成績のいい方
        //080020-科学Ｂ

        $score = 0;
        if ($this->annual == "01") {
            $query = knje061mQuery::getScoreMax($semester, $schregNo, "('180020', '180001')", $this);
            $score = $db->getOne($query);
        } else {
            $query = knje061mQuery::getScoreMax($semester, $schregNo, "('080010', '080020')", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "18", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "18", $this->hyouka[$score], $this);
    }

    //外国語(英語)
    function makeEigo($db, $semester, $schregNo) {
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
        knje061mQuery::deleteData($db, $schregNo, "22", $this);

        //英語１
        $query = knje061mQuery::getScore($semester, $schregNo, "220010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "22", 1, $kanten[$score], $this);

        //英語１、２
        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('220010', '220020')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "22", 2, $kanten[$score], $this);

        //英語２
        $query = knje061mQuery::getScore($semester, $schregNo, "220020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "22", 3, $kanten[$score], $this);

        //英語３
        $query = knje061mQuery::getScore($semester, $schregNo, "220030", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "22", 4, $kanten[$score], $this);

        //評定
        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('220010', '220020', '220030')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 3 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        //knje061mQuery::insData($db, $schregNo, "2299", $this->hyouka[$score]);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "22", $this->hyouka[$score], $this);
    }

    /****************************/
    /* ↓↓↓ 中学(成績) ↓↓↓ */
    /****************************/
    /* ↓↓↓ 2010年度版 ↓↓↓ */
    /****************************/

    //国語
    function makeKokugo2010($db, $semester, $schregNo) {
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
        knje061mQuery::deleteData($db, $schregNo, "02", $this);

        //国語１
        $query = knje061mQuery::getScore($semester, $schregNo, "020010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "02", 1, $kanten[$score], $this);

        //国語２
        $query = knje061mQuery::getScore($semester, $schregNo, "020020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "02", 3, $kanten[$score], $this);

        //国語３
        $score = 0;
        if ($this->annual == "03") {
            $query = knje061mQuery::getScore($semester, $schregNo, "020030", $this);
            $score = $db->getOne($query);
        } else {
            $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('020010', '020020')", $this);
            $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        }
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "02", 5, $kanten2[$score], $this);

        //評定
        if ($this->annual == "03") {
            $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('020010', '020020', '020030')", $this);
            $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreData["CNT"] < 3 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
            $score = $score ? $score : 0;
        }
        //knje061mQuery::insData($db, $schregNo, "0299", $this->hyouka[$score]);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "02", $this->hyouka[$score], $this);
    }

    //社会
    function makeSyakai2010($db, $semester, $schregNo) {
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
        knje061mQuery::deleteData($db, $schregNo, "04", $this);

        //社会２
        $query = knje061mQuery::getScore($semester, $schregNo, "040020", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "04", 1, $kanten[$score], $this);

        //社会１
        $query = knje061mQuery::getScore($semester, $schregNo, "040010", $this);
        $score = $db->getOne($query);
        $score = $score ? $score : 0;
        //乱数生成器を初期化
        //PHP4.2以前は、必要らしいのでsrandを使用する。
        srand((double)microtime()*1000000);
        shuffle($kanten[$score]);
        knje061mQuery::delInsData2($db, $schregNo, "04", 3, $kanten[$score], $this);

        //評定
        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('040010', '040020')", $this);
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        $score = $score ? $score : 0;
        //knje061mQuery::insData($db, $schregNo, "0499", $this->hyouka[$score]);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "04", $this->hyouka[$score], $this);
    }

    //理科
    function makeRika2010($db, $semester, $schregNo) {
        //080010-科学Ａ：科学Ａと科学Ｂの平均（小数点切捨て）
        //080020-科学Ｂ

        $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('080010', '080020')", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "08", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "08", $this->hyouka[$score], $this);
    }

    //保健体育
    function makeTaiiku2010($db, $semester, $schregNo) {
        //160010-保健：(中３)は、保健と体育の平均（小数点切捨て）
        //160020-体育：(中１・中２)は、体育の成績

        $score = 0;
        if ($this->annual == "03") {
            $query = knje061mQuery::getScoreAvg($semester, $schregNo, "('160010', '160020')", $this);
            $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreData["CNT"] < 2 ? 0 : floor($scoreData["SCORE"] / $scoreData["CNT"]);
        } else {
            $query = knje061mQuery::getScore($semester, $schregNo, "160020", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "16", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "16", $this->hyouka[$score], $this);
    }

    //技術・家庭
    function makeGijutuKatei2010($db, $semester, $schregNo) {
        //180020-家庭科：(中１)は、家庭科の成績
        //080010-科学Ａ：(中２・中３)は、科学Ａと科学Ｂの成績のいい方
        //080020-科学Ｂ

        $score = 0;
        if ($this->annual == "01") {
            $query = knje061mQuery::getScore($semester, $schregNo, "180020", $this);
            $score = $db->getOne($query);
        } else {
            $query = knje061mQuery::getScoreMax($semester, $schregNo, "('080010', '080020')", $this);
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
        knje061mQuery::delInsData($db, $schregNo, "18", $kanten[$score], $this->hyouka[$score], $this);

        //評定(SCHREG_STUDYREC_DAT)
        knje061mQuery::delInsDataStudyrec($db, $schregNo, $this->annual, "18", $this->hyouka[$score], $this);
    }

}
?>
