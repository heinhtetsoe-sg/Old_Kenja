<?php

require_once('for_php7.php');
class knjd122xQuery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJD122X' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度
    public function getSelectYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T1.YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期
    public function getSelectSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別
    public function getSelectTestkind($table, $year, $semester, $value = "")
    {
        $query  = "";
        //テスト種別
        $query .= " WITH T_TEST AS ( ";
        $query .= "     SELECT ";
        $query .= "         TESTITEMNAME, ";
        $query .= "         TESTKINDCD, ";
        $query .= "         TESTITEMCD ";
        $query .= "     FROM ";
        $query .=           $table ;
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        if ($table == 'TESTITEM_MST_COUNTFLG_NEW') {
            $query .= "     AND SEMESTER = '{$semester}' ";
        }
        $query .= "         AND TESTKINDCD || TESTITEMCD IN ('0101','0201','0202','9900') ";
        $query .= " ) ";
        if ($value != "") {
            $query .= " , T_TEST_ALL AS ( ";
        }
        //考査素点
        $query .= " SELECT ";
        $query .= "     TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || '-' || TESTITEMCD || '-' || '1' AS VALUE ";
        $query .= " FROM ";
        $query .= "     T_TEST ";
        $query .= " WHERE ";
        $query .= "     TESTKINDCD IN ('01','02') ";
        //考査評価
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     TESTITEMNAME || '評価' AS LABEL, ";
        $query .= "     TESTKINDCD || '-' || TESTITEMCD || '-' || '2' AS VALUE ";
        $query .= " FROM ";
        $query .= "     T_TEST ";
        $query .= " WHERE ";
        $query .= "     TESTKINDCD IN ('01','02') ";
        //学期評価・学年評定
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || '-' || TESTITEMCD || '-' || '2' AS VALUE ";
        $query .= " FROM ";
        $query .= "     T_TEST ";
        $query .= " WHERE ";
        $query .= "     TESTKINDCD IN ('99') ";
        if ($value != "") {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     T_TEST_ALL ";
            $query .= " WHERE ";
            $query .= "     VALUE = '{$value}' ";
        }
        //ソート
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //MAX学期
    public function getMaxSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER < '9' ";

        return $query;
    }

    //科目
    public function getSubclassList($year, $semester, $maxSemester, $model)
    {
        $gakunenmatuSemester = ($year < CTRL_YEAR) ? $maxSemester : CTRL_SEMESTER;
        $chairSemester = ($semester == "9") ? $gakunenmatuSemester : $semester;

        $query  = " WITH T_CHAIR AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "              ON T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "         INNER JOIN CHAIR_STF_DAT T4 ";
        $query .= "              ON T4.YEAR     = T1.YEAR ";
        $query .= "             AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T4.CHAIRCD  = T1.CHAIRCD ";
        //参照・更新可（制限付）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "         AND T4.STAFFCD  = '".STAFFCD."' ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$chairSemester}' ";
        $query .= "         AND SUBSTR(T1.SUBCLASSCD,1,2) <= '90' ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     L1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     L1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     T_CHAIR T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND L1.CLASSCD = T1.CLASSCD  ";
            $query .= "                           AND L1.SCHOOL_KIND = T1.SCHOOL_KIND  ";
            $query .= "                           AND L1.CURRICULUM_CD = T1.CURRICULUM_CD  ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座
    public function getChairList($year, $semester, $subclasscdArray, $maxSemester, $model)
    {
        $gakunenmatuSemester = ($year < CTRL_YEAR) ? $maxSemester : CTRL_SEMESTER;
        $chairSemester = ($semester == "9") ? $gakunenmatuSemester : $semester;

        $query  = " WITH T_CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CHAIRNAME ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "              ON T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "         INNER JOIN CHAIR_STF_DAT T4 ";
        $query .= "              ON T4.YEAR     = T1.YEAR ";
        $query .= "             AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T4.CHAIRCD  = T1.CHAIRCD ";
        //参照・更新可（制限付）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "         AND T4.STAFFCD  = '".STAFFCD."' ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$chairSemester}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ('".implode("','", $subclasscdArray)."') ";
        } else {
            $query .= "         AND T1.SUBCLASSCD IN ('".implode("','", $subclasscdArray)."') ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CHAIRNAME ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     T_CHAIR T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座名簿データ存在チェックSQL
    public function checkMeiboData($year, $semester, $chaircd, $chairDate, $schregno)
    {
        $chairDate = str_replace("/", "-", $chairDate);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "         T2.YEAR     = '{$year}' ";
        if ($semester != "9") {
            $query .= "     AND T2.SEMESTER = '{$semester}' ";
        }
        $query .= "     AND T2.CHAIRCD  = '{$chaircd}' ";
        $query .= "     AND T2.SCHREGNO = '{$schregno}' ";
        $query .= "     AND DATE('{$chairDate}') BETWEEN T2.APPDATE AND T2.APPENDDATE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数取得
        list($testkindcd, $testitemcd, $scoreDiv) = explode("-", $model->field["IN_TESTKIND"]);
        $scoreField = "";
        $chaircd = "";
        if ($model->field["IN_SEMESTER"] == "9") {
            $scoreField = "GRAD_VALUE";
        } else {
            //学期
            $scoreField = "SEM".$model->field["IN_SEMESTER"];
            //テスト種別
            if ($testkindcd == "01" && $testitemcd == "01") {
                $scoreField .= "_INTR";
            }
            if ($testkindcd == "02" && $testitemcd == "01") {
                $scoreField .= "_TERM";
            }
            if ($testkindcd == "02" && $testitemcd == "02") {
                $scoreField .= "_TERM2";
            }
            //講座コード
            $chaircd = $scoreField."_CHAIRCD";
            //区分（1:素点、2:評価or評定）
            if ($scoreDiv == "1") {
                $scoreField .= "_SCORE";
            }
            if ($scoreDiv == "2") {
                $scoreField .= "_VALUE";
            }
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();
            //「学年末評定」のを登録した場合は、履修単位・修得単位も登録する。
            //単位の登録方法は成績入力画面と全く同じになるようにする。
            if ($model->field["IN_SEMESTER"] == "9") {
                $data = knjd122xQuery::getFieldCredit($model, $data_arr, $i, $db);
            }
            //出欠情報が入ってたら成績は空
            if ($scoreDiv == "2" && ($data_arr[$i]["SCORE"] == "-" || $data_arr[$i]["SCORE"] == "=")) {
                $data[$scoreField][NUMBER]      = "";
                $data[$scoreField."_DI"][TEXT]  = $data_arr[$i]["SCORE"];
            } elseif ($scoreDiv == "2") {
                $data[$scoreField][NUMBER]      = $data_arr[$i]["SCORE"];
                $data[$scoreField."_DI"][TEXT]  = "";
            } elseif ($scoreDiv == "1") {
                $data[$scoreField][NUMBER]      = $data_arr[$i]["SCORE"];
            }
            if ($scoreDiv == "1") {
                $data[$chaircd][TEXT]           = $data_arr[$i]["CHAIRCD"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            //条件
            $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "       CLASSCD          = '".$data_arr[$i]["CLASSCD"]."' AND ";
                $where .= "       SCHOOL_KIND      = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                $where .= "       CURRICULUM_CD    = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
            }
            $where .= "       SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
            $where .= "       SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
            //更新
            if (1 > $db->getOne("SELECT COUNT(*) FROM RECORD_DAT " .$where)) {
                //NOT NULL 項目
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                    $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                    $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                }
                $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["TAKESEMES"][TEXT]        = "0";
                $query = Query::insertSQL($data, "RECORD_DAT");
            } else {
                $query = Query::updateSQL($data, "RECORD_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /******************/
    /* 単位の自動計算 */
    /******************/
    //履修・修得単位
    public function getFieldCredit($model, $data_arr, $i, $db)
    {
        $data = array();
        //履修／修得単位を一旦セットする
        $data["COMP_CREDIT"][NUMBER] = $data_arr[$i]["COMP_CREDIT"];
        $data["GET_CREDIT"][NUMBER]  = $data_arr[$i]["GET_CREDIT"];
        /******************************/
        /* 単位の自動計算に必要な情報 */
        /******************************/
        //名称マスタ「D008」
        $isClasscdD008 = 0 < $db->getOne(knjd122xQuery::getNameMstD008($data_arr[$i]["YEAR"], substr($data_arr[$i]["SUBCLASSCD"], 0, 2), $model, $data_arr[$i]["SCHOOL_KIND"]));
        //名称マスタ「D015」
        $repGetCredit = $db->getOne(knjd122xQuery::getNameMstD015($data_arr[$i]["YEAR"]));
        //合併設定データ
        $subRepRow = $db->getRow(knjd122xQuery::getSubRepCombinedDat($data_arr[$i]["YEAR"], $data_arr[$i]["CLASSCD"], $data_arr[$i]["SCHOOL_KIND"], $data_arr[$i]["CURRICULUM_CD"], $data_arr[$i]["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
        //科目別出欠データ
        $schoolRow = $db->getRow(knjd122xQuery::getSchoolMst($data_arr[$i]["YEAR"]), DB_FETCHMODE_ASSOC);
        $attendRow = $db->getRow(knjd122xQuery::getAttendSubclassDat($data_arr[$i]["YEAR"], $data_arr[$i]["CLASSCD"], $data_arr[$i]["SCHOOL_KIND"], $data_arr[$i]["CURRICULUM_CD"], $data_arr[$i]["SUBCLASSCD"], $data_arr[$i]["SCHREGNO"], $schoolRow, $model), DB_FETCHMODE_ASSOC);
        /********/
        /* 単位 */
        /********/
        //合併先科目の単位を抽出
        $recordRow = $db->getRow(knjd122xQuery::getRecordCredit($data_arr[$i]["YEAR"], $data_arr[$i]["CLASSCD"], $data_arr[$i]["SCHOOL_KIND"], $data_arr[$i]["CURRICULUM_CD"], $data_arr[$i]["SUBCLASSCD"], $data_arr[$i]["SCHREGNO"], $model), DB_FETCHMODE_ASSOC);
        //単位マスタ
        $semester = $db->getOne(knjd122xQuery::getSemesterChairDate($data_arr[$i]["YEAR"], $data_arr[$i]["CHAIR_DATE"]));
        $creditRow = $db->getRow(knjd122xQuery::getCreditMst($data_arr[$i]["YEAR"], $data_arr[$i]["CLASSCD"], $data_arr[$i]["SCHOOL_KIND"], $data_arr[$i]["CURRICULUM_CD"], $data_arr[$i]["SUBCLASSCD"], $data_arr[$i]["SCHREGNO"], $semester, $model), DB_FETCHMODE_ASSOC);
        /******************/
        /* 単位の自動計算 */
        /******************/
        if (($data_arr[$i]["SCORE"] != "") && ($data_arr[$i]["COMP_CREDIT"] == "" && $data_arr[$i]["GET_CREDIT"] == "")) {
            // 履修単位・修得単位が入力可で、学年評定が入力されていて、履修単位・修得単位がNULLの時
            // 合併先科目である
            if (isset($subRepRow["CALCULATE_CREDIT_FLG"])) {
                // $subRepRow["CALCULATE_CREDIT_FLG"] ---> 1:単位固定タイプ 2:単位加算タイプ
                if ($data_arr[$i]["SCORE"] > "1") {
                    // 学年評定:２以上、履修・修得に登録
                    $getCredit = ($repGetCredit == "Y") ? $recordRow["GET_CREDIT_SUM_Y"] : $recordRow["GET_CREDIT_SUM"];
                    $data["COMP_CREDIT"][NUMBER] = ($subRepRow["CALCULATE_CREDIT_FLG"] == "2") ? $recordRow["COMP_CREDIT_SUM"] : $creditRow["CREDITS"];
                    $data["GET_CREDIT"][NUMBER]  = ($subRepRow["CALCULATE_CREDIT_FLG"] == "2") ? $getCredit : $creditRow["CREDITS"];
                } elseif ($data_arr[$i]["SCORE"] == "1") {
                    // 学年評定:１、履修のみに登録
                    $data["COMP_CREDIT"][NUMBER] = ($subRepRow["CALCULATE_CREDIT_FLG"] == "2") ? $recordRow["COMP_CREDIT_SUM"] : $creditRow["CREDITS"];
                    $data["GET_CREDIT"][NUMBER]  = 0;
                }

                // 合併先科目でない--->今までと同じ計算処理をする
            } else {
                //単位数・学年評定がある場合のみ処理
                if ($creditRow["CREDITS"] != "" && ($data_arr[$i]["SCORE"] != "")) {
                    $hanki_credit = $creditRow["CREDITS"] / 2;
                    if ($data_arr[$i]["SCORE"] > "1" && $attendRow["NOTICE_LATE"] > $creditRow["ABSENCE_HIGH"]) {
                        //①学年評定:２以上、欠課数上限値オーバー
                        //半期認定科目
                        if ($creditRow["AUTHORIZE_FLG"] == "1") {
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER]  = floor($hanki_credit);
                            if ($model->fields["SEM1_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "1";
                            }
                            if ($model->fields["SEM2_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "2";
                            }
                            if ($model->fields["SEM3_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "3";
                            }
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $creditRow["CREDITS"];
                            $data["GET_CREDIT"][NUMBER]  = $creditRow["CREDITS"];
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    } elseif ($data_arr[$i]["SCORE"] > "1" && $attendRow["NOTICE_LATE"] <= $creditRow["ABSENCE_HIGH"]) {
                        //②学年評定:２以上、欠課数上限値内
                        $data["COMP_CREDIT"][NUMBER] = $creditRow["CREDITS"];
                        $data["GET_CREDIT"][NUMBER]  = $creditRow["CREDITS"];
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    } elseif ($data_arr[$i]["SCORE"] == "1" && $attendRow["NOTICE_LATE"] > $creditRow["ABSENCE_HIGH"]) {
                        //③学年評定:１、欠課数上限値オーバー
                        //半期認定科目
                        if ($creditRow["AUTHORIZE_FLG"] == "1") {
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER]  = 0;
                            if ($model->fields["SEM1_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "1";
                            }
                            if ($model->fields["SEM2_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "2";
                            }
                            if ($model->fields["SEM3_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "3";
                            }
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $creditRow["CREDITS"];
                            $data["GET_CREDIT"][NUMBER]  = 0;
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    } elseif ($data_arr[$i]["SCORE"] == "1" && $attendRow["NOTICE_LATE"] <= $creditRow["ABSENCE_HIGH"]) {
                        //④学年評定:１、欠課数上限値内
                        $data["COMP_CREDIT"][NUMBER] = $creditRow["CREDITS"];
                        $data["GET_CREDIT"][NUMBER]  = 0;
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    } elseif ($data_arr[$i]["SCORE"] != "" && $attendRow["NOTICE_LATE"] > $creditRow["ABSENCE_HIGH"]) {
                        //⑤学年評定:'-''='、欠課数上限値オーバー
                        $data["COMP_CREDIT"][NUMBER] = 0;
                        $data["GET_CREDIT"][NUMBER]  = 0;
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    } else {
                        //⑥その他
                        $data["COMP_CREDIT"][NUMBER] = "";
                        $data["GET_CREDIT"][NUMBER]  = "";
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    }
                }
            }
        } elseif ($data_arr[$i]["SCORE"] == "") {
            // 履修単位・修得単位が入力可で、学年評定が入力されていない
            // 「評定を指定しない科目」の合併先科目である
            if (isset($subRepRow["CALCULATE_CREDIT_FLG"]) && ($isClasscdD008 || substr($data_arr[$i]["SUBCLASSCD"], 0, 2) == "90")) {
                // 1:単位固定タイプ
                // ・RECORD_DATに合併元科目のレコードが「全て」存在している
                // ・そのレコードの中に単位数が登録されているレコードが「最低でも１件」存在している
                if ($subRepRow["CALCULATE_CREDIT_FLG"] == "1" && $subRepRow["MOTO_CNT"] == $recordRow["MOTO_CNT"] && 0 < $recordRow["CREDIT_CNT"]) {
                    // 単位マスタの単位数
                    $data["COMP_CREDIT"][NUMBER] = $creditRow["CREDITS"];
                    $data["GET_CREDIT"][NUMBER]  = $creditRow["CREDITS"];
                }
                // 2:単位加算タイプ
                // ・RECORD_DATに合併元科目のレコードが「最低でも１件」存在している
                // ・そのレコードの中に単位数が登録されているレコードが「最低でも１件」存在している
                if ($subRepRow["CALCULATE_CREDIT_FLG"] == "2" && 0 < $recordRow["MOTO_CNT"] && 0 < $recordRow["CREDIT_CNT"]) {
                    // 合併元科目の単位数の合計
                    $data["COMP_CREDIT"][NUMBER] = $recordRow["COMP_CREDIT_SUM"];
                    $data["GET_CREDIT"][NUMBER]  = $recordRow["GET_CREDIT_SUM"];
                }
            }
        }
        /* 履修単位・修得単位の自動計算処理 終わり */
        return $data;
    }

    //合併先科目の単位固定／加算フラグ
    public function getSubRepCombinedDat($year, $classcd, $school_kind, $curriculum_cd, $subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = "SELECT CALCULATE_CREDIT_FLG, COUNT(ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD) AS MOTO_CNT, ";
        } else {
            $query  = "SELECT CALCULATE_CREDIT_FLG, COUNT(ATTEND_SUBCLASSCD) AS MOTO_CNT ";
        }
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "WHERE  YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  COMBINED_CLASSCD       = '".$classcd."' ";
            $query .= "  AND  COMBINED_SCHOOL_KIND   = '".$school_kind."' ";
            $query .= "  AND  COMBINED_CURRICULUM_CD = '".$curriculum_cd."' ";
        }
        $query .= "  AND  COMBINED_SUBCLASSCD = '{$subclasscd}' ";
        $query .= "GROUP BY CALCULATE_CREDIT_FLG ";
        return $query;
    }

    /* 合併先科目の単位を抽出 */
    /* 合併先科目の単位は、生徒が選択した合併元科目の単位数を加算した値である */
    /* 「名称予備１＝Y」の場合、合併元科目の「評定＝１（履修のみ）」を合併先科目では「修得」として扱う */
    // COMP_CREDIT_SUM  :合併元科目の履修単位の合計
    // GET_CREDIT_SUM   :合併元科目の修得単位の合計
    // GET_CREDIT_SUM_Y :合併元科目の修得単位の合計「名称予備１＝Y」の場合
    // MOTO_CNT         :合併元科目のレコード数
    // CREDIT_CNT       :単位数が登録されている合併元科目のレコード数
    public function getRecordCredit($year, $classcd, $school_kind, $curriculum_cd, $subclasscd, $schregno, $model)
    {
        $query  = "";
        $query .= "SELECT T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       COUNT(T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD) AS MOTO_CNT, ";
        } else {
            $query .= "       COUNT(T2.SUBCLASSCD) AS MOTO_CNT, ";
        }
        $query .= "       SUM(case when T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL then 1 else 0 end) AS CREDIT_CNT, ";
        $query .= "       SUM(T2.COMP_CREDIT) AS COMP_CREDIT_SUM, ";
        $query .= "       SUM(T2.GET_CREDIT) AS GET_CREDIT_SUM, ";
        $query .= "       SUM(case when 1 < T2.GRAD_VALUE then T2.GET_CREDIT ";
        $query .= "                when 1 = T2.GRAD_VALUE then T2.COMP_CREDIT end) AS GET_CREDIT_SUM_Y ";
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "       RECORD_DAT T2 ";
        $query .= "WHERE  T1.YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  T1.COMBINED_CLASSCD       = '".$classcd."' ";
            $query .= "  AND  T1.COMBINED_SCHOOL_KIND   = '".$school_kind."' ";
            $query .= "  AND  T1.COMBINED_CURRICULUM_CD = '".$curriculum_cd."' ";
        }
        $query .= "  AND  T1.COMBINED_SUBCLASSCD = '{$subclasscd}' ";
        $query .= "  AND  T2.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  T2.CLASSCD       = T1.ATTEND_CLASSCD ";
            $query .= "       AND  T2.SCHOOL_KIND   = T1.ATTEND_SCHOOL_KIND ";
            $query .= "       AND  T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "  AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "  AND  T2.SCHREGNO = '{$schregno}' ";
        $query .= "GROUP BY T2.SCHREGNO ";
        return $query;
    }

    // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
    // 名称区分：D015　名称コード：01　名称予備１：「Y」もしくは「NULL」
    public function getNameMstD015($year)
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMESPARE1 "; // Y:, null:
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    NAMECD1 = 'D015' AND ";
        $query .= "    NAMECD2 = '01' ";
        return $query;
    }

    //学年末の学期（講座基準日から取得）
    public function getSemesterChairDate($year, $chairDate)
    {
        $chairDate = str_replace("/", "-", $chairDate);
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= "     AND DATE('{$chairDate}') BETWEEN SDATE AND EDATE ";
        return $query;
    }

    //単位マスタ情報(単位数、欠課数上限値、半期認定フラグ)
    public function getCreditMst($year, $classcd, $school_kind, $curriculum_cd, $subclasscd, $schregno, $semester, $model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T4.CREDITS, ";
        $query .= "     value(T4.ABSENCE_HIGH,99) as ABSENCE_HIGH, ";
        $query .= "     T4.AUTHORIZE_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                            AND T4.CLASSCD          = '".$classcd."' ";
            $query .= "                            AND T4.SCHOOL_KIND      = '".$school_kind."' ";
            $query .= "                            AND T4.CURRICULUM_CD    = '".$curriculum_cd."' ";
        }
        $query .= "                            AND T4.SUBCLASSCD = '{$subclasscd}' ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = '{$schregno}' ";
        $query .= "     AND T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        return $query;
    }

    //学校マスタ
    public function getSchoolMst($year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        return $query;
    }

    //科目別出欠データ（欠課数を取得）
    public function getAttendSubclassDat($year, $classcd, $school_kind, $curriculum_cd, $subclasscd, $schregno, $schoolRow, $model)
    {
        //変数
        $late_early = "SUM(value(LATE,0)) + SUM(value(EARLY,0))";
        $notice = "SUM(value(NOTICE,0)) + SUM(value(NONOTICE,0)) + SUM(value(SICK,0)) + SUM(value(NURSEOFF,0))";
        if ($schoolRow["SUB_OFFDAYS"] == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_ABSENT"] == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($schoolRow["SUB_SUSPEND"] == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"] == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"] == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        //SQL
        $query  = "SELECT  SCHREGNO ";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE ";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.SCHREGNO ";
        if ($schoolRow["ABSENT_COV"] == "1" || $schoolRow["ABSENT_COV"] == "3") {
            $query .= "           ,T1.SEMESTER ";
        }
        if (($schoolRow["ABSENT_COV"] == "3" || $schoolRow["ABSENT_COV"] == "4") && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "           ,DECIMAL((FLOAT({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}),4,1) AS NOTICE_LATE ";
        } elseif (($schoolRow["ABSENT_COV"] == "1" || $schoolRow["ABSENT_COV"] == "2") && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "           ,(({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}) AS NOTICE_LATE ";
        } else {
            $query .= "           ,{$notice} AS NOTICE_LATE ";
        }
        $query .= "          FROM ATTEND_SUBCLASS_DAT T1 ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.YEAR       = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND T1.CLASSCD          = '".$classcd."' ";
            $query .= "           AND T1.SCHOOL_KIND      = '".$school_kind."' ";
            $query .= "           AND T1.CURRICULUM_CD    = '".$curriculum_cd."' ";
        }
        $query .= "           AND T1.SUBCLASSCD = '{$subclasscd}' ";
        $query .= "           AND T1.SCHREGNO   = '{$schregno}' ";
        $query .= "         GROUP BY T1.SCHREGNO ";
        if ($schoolRow["ABSENT_COV"] == "1" || $schoolRow["ABSENT_COV"] == "3") {
            $query .= "             ,T1.SEMESTER ";
        }
        $query .= "         ) T1 ";
        $query .= " GROUP BY SCHREGNO ";
        return $query;
    }

    //「評定を指定しない科目」が設定されている
    public function getNameMstD008($year, $classcd, $model, $schoolKind)
    {
        if ($model->Properties["useClassDetailDat"] == "1") {
            $query  = "";
            $query .= " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CLASS_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND CLASSCD = '{$classcd}' ";
            $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "     AND CLASS_SEQ = '003' ";
        } else {
            $query  = "";
            $query .= " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' AND ";
            $query .= "     NAMECD1 = 'D008' AND ";
            $query .= "     NAMECD2 = '{$classcd}' ";
        }
        return $query;
    }
    /******************/
    /******************/
    /******************/

    //成績入力完了フラグの更新処理
    public function updateRecordChkfin($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        list($testkindcd, $testitemcd, $scoreDiv) = explode("-", $model->field["IN_TESTKIND"]);
        $updateTable = ($scoreDiv == "1") ? "SCH_CHR_TEST" : "RECORD_CHKFIN_DAT"; //更新テーブル

        $cnt = 0;
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //条件
            $where  = " WHERE ";
            $where .= "         YEAR        = '".$data_arr[$i]["YEAR"]."' ";
            $where .= "     AND SEMESTER    = '".$model->field["IN_SEMESTER"]."' ";
            $where .= "     AND TESTKINDCD  = '".$testkindcd."' ";
            $where .= "     AND TESTITEMCD  = '".$testitemcd."' ";
            $where .= "     AND CHAIRCD     = '".$data_arr[$i]["CHAIRCD"]."' ";
            if ($scoreDiv == "2") {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND CLASSCD          = '".$data_arr[$i]["CLASSCD"]."' ";
                    $where .= "     AND SCHOOL_KIND      = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                    $where .= "     AND CURRICULUM_CD    = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                }
                $where .= "     AND SUBCLASSCD  = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                $where .= "     AND RECORD_DIV  = '2' ";
            }
            //レコードがあれば更新
            if (0 < $db->getOne("SELECT COUNT(*) FROM " .$updateTable .$where)) {
                $data = array();
                $data["EXECUTED"][TEXT]         = ($model->field["CHK_COMP"] == "on")? "1" : "0";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                $query = Query::updateSQL($data, $updateTable, $where);
                $db->query($query);
                $cnt++;
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM RECORD_DAT ";
            $query .= " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       CLASSCD          = '".$data_arr[$i]["CLASSCD"]."' AND ";
                $query .= "       SCHOOL_KIND      = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                $query .= "       CURRICULUM_CD    = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
            }
            $query .= "       SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
            $query .= "       SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        //変数
        $gakunenmatuSemester = ($model->field["OUT_YEAR"] < CTRL_YEAR) ? $model->maxSemester : CTRL_SEMESTER;
        $chairSemester = ($model->field["OUT_SEMESTER"] == "9") ? $gakunenmatuSemester : $model->field["OUT_SEMESTER"];
        $chairDate = str_replace("/", "-", $model->field["CHAIR_DATE"]);
        $chaircdArray = array();
        if (strlen($model->selectdata["CHAIRCD"])) {
            $chaircdArray = explode(",", $model->selectdata["CHAIRCD"]);
        }
        //SQL
        $query  = "";
        //名簿
        $query .= " WITH T_CHAIR AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         MIN(T2.CHAIRCD) AS CHAIRCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T2 ";
        $query .= "         INNER JOIN CHAIR_DAT T1 ";
        $query .= "              ON T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR     = '{$model->field["OUT_YEAR"]}' ";
        $query .= "         AND T2.SEMESTER = '{$chairSemester}' ";
        $query .= "         AND T2.CHAIRCD IN ('".implode("','", $chaircdArray)."') ";
        $query .= "         AND DATE('{$chairDate}') BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L5.SUBCLASSNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L6.CHAIRNAME, ";
        $query .= "     L7.GRADE, ";
        $query .= "     L7.HR_CLASS, ";
        $query .= "     L4.HR_NAME, ";
        $query .= "     L7.ATTENDNO, ";
        $query .= "     L8.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        //素点
        $query .= "     L1.SEM1_INTR_SCORE, ";
        $query .= "     L1.SEM1_TERM_SCORE, ";
        $query .= "     L1.SEM1_TERM2_SCORE, ";
        $query .= "     L1.SEM2_INTR_SCORE, ";
        $query .= "     L1.SEM2_TERM_SCORE, ";
        $query .= "     L1.SEM2_TERM2_SCORE, ";
        $query .= "     L1.SEM3_INTR_SCORE, ";
        $query .= "     L1.SEM3_TERM_SCORE, ";
        //評価・評定
        $query .= "     L1.SEM1_INTR_VALUE, ";
        $query .= "     L1.SEM1_TERM_VALUE, ";
        $query .= "     L1.SEM1_TERM2_VALUE, ";
        $query .= "     L1.SEM1_VALUE, ";
        $query .= "     L1.SEM2_INTR_VALUE, ";
        $query .= "     L1.SEM2_TERM_VALUE, ";
        $query .= "     L1.SEM2_TERM2_VALUE, ";
        $query .= "     L1.SEM2_VALUE, ";
        $query .= "     L1.SEM3_INTR_VALUE, ";
        $query .= "     L1.SEM3_TERM_VALUE, ";
        $query .= "     L1.SEM3_VALUE, ";
        $query .= "     L1.GRAD_VALUE, ";
        //出欠記号
        $query .= "     L1.SEM1_INTR_VALUE_DI, ";
        $query .= "     L1.SEM1_TERM_VALUE_DI, ";
        $query .= "     L1.SEM1_TERM2_VALUE_DI, ";
        $query .= "     L1.SEM1_VALUE_DI, ";
        $query .= "     L1.SEM2_INTR_VALUE_DI, ";
        $query .= "     L1.SEM2_TERM_VALUE_DI, ";
        $query .= "     L1.SEM2_TERM2_VALUE_DI, ";
        $query .= "     L1.SEM2_VALUE_DI, ";
        $query .= "     L1.SEM3_INTR_VALUE_DI, ";
        $query .= "     L1.SEM3_TERM_VALUE_DI, ";
        $query .= "     L1.SEM3_VALUE_DI, ";
        $query .= "     L1.GRAD_VALUE_DI, ";
        //学年末の場合、履修単位と修得単位を追加する。
        if ($model->field["OUT_SEMESTER"] == "9") {
            $query .= "     L1.COMP_CREDIT, ";
            $query .= "     L1.GET_CREDIT, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     T_CHAIR T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_BASE_MST L8 ON L8.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT L7 ON  L7.SCHREGNO = T1.SCHREGNO ";
        $query .= "                        AND L7.YEAR     = '{$model->field["OUT_YEAR"]}' ";
        $query .= "                        AND L7.SEMESTER = '{$chairSemester}' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L4 ON  L4.YEAR     = L7.YEAR ";
        $query .= "                         AND L4.SEMESTER = L7.SEMESTER ";
        $query .= "                         AND L4.GRADE    = L7.GRADE ";
        $query .= "                         AND L4.HR_CLASS = L7.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     RECORD_DAT L1 ON  L1.YEAR       = '{$model->field["OUT_YEAR"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "               AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "               AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                   AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                   AND L1.SCHREGNO   = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L5 ON L5.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND L5.CLASSCD = T1.CLASSCD ";
            $query .= "               AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "               AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     CHAIR_DAT L6 ON L6.YEAR     = '{$model->field["OUT_YEAR"]}' ";
        $query .= "                 AND L6.SEMESTER = '{$chairSemester}' ";
        $query .= "                 AND L6.CHAIRCD  = T1.CHAIRCD ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L7.GRADE, ";
        $query .= "     L7.HR_CLASS, ";
        $query .= "     L7.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        //echo $query;
        return $query;
    }
}
