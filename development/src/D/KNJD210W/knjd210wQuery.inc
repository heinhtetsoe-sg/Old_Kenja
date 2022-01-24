<?php

require_once('for_php7.php');


class knjd210wquery extends Query
{

    //満点で偏差値計算を判別（1人でも科目の満点が複数ある場合、合算科目の偏差値は、各科目の偏差値の平均を算出する）
//    function getPerfectCnt($model)
//    {
//        $query  = "";
//        $query .= " SELECT ";
//        $query .= "     COUNT(SCHREGNO) ";
//        $query .= " FROM ";
//        $query .= "     ( ";
//        $query .= "     SELECT ";
//        $query .= "         T1.SCHREGNO, ";
//        $query .= "         COUNT(DISTINCT VALUE(L1.PERFECT, 100)) AS PERFECT_CNT ";
//        $query .= "     FROM ";
//        $query .= "         RECORD_SCORE_DAT T1 ";
//        $query .= "         INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = T1.YEAR ";
//        $query .= "             AND CYDAT.CLASSCD = T1.CLASSCD ";
//        $query .= "             AND CYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
//        $query .= "         INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = T1.YEAR ";
//        $query .= "             AND SYDAT.CLASSCD = T1.CLASSCD ";
//        $query .= "             AND SYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
//        $query .= "             AND SYDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
//        $query .= "             AND SYDAT.SUBCLASSCD = T1.SUBCLASSCD ";
//        $query .= "         INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
//        $query .= "             AND T2.YEAR = '".CTRL_YEAR."' ";
//        $query .= "             AND T2.SEMESTER = '".$model->seme_sch."' ";
//        $query .= "             AND T2.GRADE = '".$model->grade."' ";
//        $query .= "         LEFT JOIN PERFECT_RECORD_SDIV_DAT L1 ON L1.YEAR = T1.YEAR ";
//        $query .= "             AND L1.SEMESTER = T1.SEMESTER ";
//        $query .= "             AND L1.TESTKINDCD = T1.TESTKINDCD ";
//        $query .= "             AND L1.TESTITEMCD = T1.TESTITEMCD ";
//        $query .= "             AND L1.SCORE_DIV = T1.SCORE_DIV ";
//        $query .= "             AND L1.CLASSCD = T1.CLASSCD ";
//        $query .= "             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
//        $query .= "             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
//        $query .= "             AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
//        $query .= "             AND L1.GRADE = CASE WHEN L1.DIV = '01' THEN '00' ELSE T2.GRADE END ";
//        $query .= "             AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN L1.DIV IN ('01','02') THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ";
//        $query .= "     WHERE ";
//        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "         AND T1.SEMESTER = '".$model->seme."' ";
//        $query .= "         AND T1.TESTKINDCD = '".$model->kind."' ";
//        $query .= "         AND T1.TESTITEMCD = '".$model->item."' ";
//        $query .= "         AND T1.SCORE_DIV = '".$model->sdiv."' ";
//        $query .= "     GROUP BY ";
//        $query .= "         T1.SCHREGNO ";
//        $query .= "     ) M1 ";
//        $query .= " WHERE ";
//        $query .= "     PERFECT_CNT > 1 ";
//
//        $db = Query::dbCheckOut();
//        $rtnCnt = $db->getOne($query);
//        Query::dbCheckIn($db);
//        return $rtnCnt;
//    }

    //存在チェック（在籍データ）
    function ChecktoSchregno($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   SCHREG_REGD_DAT ";
        $query .= " WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->seme_sch."' AND GRADE='".$model->grade."' ";
        $query .= "    AND HR_CLASS IS NOT NULL ";
        $query .= "    AND COURSECD IS NOT NULL ";
        $query .= "    AND MAJORCD IS NOT NULL ";
        $query .= "    AND COURSECODE IS NOT NULL ";

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //存在チェック（成績データ）
    function ChecktoRecordScore($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   RECORD_SCORE_DAT T1 ";
        $query .= "        INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = T1.YEAR ";
        $query .= "            AND CYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "            AND CYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "        INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = T1.YEAR ";
        $query .= "            AND SYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "            AND SYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "            AND SYDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "            AND SYDAT.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "        T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$model->seme."' AND ";
        $query .= "        T1.TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        T1.TESTITEMCD = '".$model->item."' AND ";
        $query .= "        T1.SCORE_DIV = '".$model->sdiv."' ";

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //処理学期
    function GetSemester($seme = "")
    {
        $query  = " SELECT SEMESTER AS VALUE,SEMESTERNAME AS LABEL,SDATE,EDATE ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ";
        if (strlen($seme)) {
            $query .= "     AND SEMESTER = '{$seme}' ";
        }
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    //処理学年
    function GetGrade($model, $grade = "")
    {
        $query  = " SELECT GRADE AS VALUE, GRADE_NAME1 AS LABEL, SCHOOL_KIND ";
        $query .= " FROM SCHREG_REGD_GDAT ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ";
        if (strlen($grade)) {
            $query .= "     AND GRADE = '{$grade}' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    //処理種別(成績)
    function GetName($model, $seme)
    {
        $query  = "SELECT T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS VALUE,T1.TESTITEMNAME AS LABEL ";
        $query .= "FROM   ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "WHERE  T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$seme}' ";
        $query .= "     AND T1.JYORETSU_FLG = '1' "; //1:序列対象
        $query .= "ORDER BY VALUE ";
        return $query;
    }

    //学校
    function getNameMstZ010()
    {
        $query  = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //合算科目の母集団から除くフラグ 1:合併元除く 2:合併先除く
    function getNameMstD070($model, $namecd1)
    {
        $testcd = $model->seme.$model->kind.$model->item.$model->sdiv;
        $query  = " SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = '{$namecd1}' AND NAME1 = '{$testcd}' ";
        return $query;
    }

    //処理科目
    function getSubclasscd($model, $school_kind)
    {
        $year = CTRL_YEAR;
        $seme = ($model->seme != "9") ? $model->seme : CTRL_SEMESTER;
        $staffcd = STAFFCD;

        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SCHOOL_KIND = '{$school_kind}' ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
            $query .= "         SELECT ";
            $query .= "             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
            $query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '{$year}' ";
            $query .= "             AND T1.SEMESTER   = '{$seme}' ";
            $query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
            $query .= "             AND T2.YEAR       = T1.YEAR ";
            $query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '{$staffcd}' "; //ログインした先生
            $query .= "         GROUP BY ";
            $query .= "             T1.CLASSCD, ";
            $query .= "             T1.SCHOOL_KIND, ";
            $query .= "             T1.CURRICULUM_CD, ";
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //履歴一覧
    function getListRireki($model)
    {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L2.GRADE_NAME1, ";
        $query .= "     L3.SEMESTERNAME, ";
        $query .= "     L4.TESTITEMNAME, ";
        $query .= "     T1.CHAIRDATE, ";
        $query .= "     T1.ELECTDIV_FLG ";
        $query .= " FROM ";
        $query .= " RECORD_RANK_EXEC_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                              AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                              AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SEMESTER_MST L3 ON L3.YEAR = T1.YEAR AND L3.SEMESTER = T1.SEMESTER ";
        $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L4 ON L4.YEAR = T1.YEAR ";
        $query .= "                                                AND L4.SEMESTER = T1.SEMESTER ";
        $query .= "                                                AND L4.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                                AND L4.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                                AND L4.SCORE_DIV = T1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->seme}' ";
        $query .= "     AND T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$model->exam}' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }
    /* 実行履歴 */
    function executeRireki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjd210wQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd210wQuery::getInsertRireki($calcDate, $calcTime, $model);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
    //実行日付・時間を取得
    function getCalcDateTime()
    {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //実行履歴データ・追加
    function getInsertRireki($calcDate, $calcTime, $model)
    {
        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;

        //処理科目(全て または 総合計のみ)
        if ($model->subclasscd == '999999' || $model->subclasscd == '000000') {
            //ダミーデータセット(00)
            $data["CLASSCD"][TEXT]          = "00";
            $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT]    = "00";
            $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '') {
            $subArray = array();
            $subArray = explode("-", $model->subclasscd);
            $data["CLASSCD"][TEXT]          = $subArray[0];
            $data["SCHOOL_KIND"][TEXT]      = $subArray[1];
            $data["CURRICULUM_CD"][TEXT]    = $subArray[2];
            $data["SUBCLASSCD"][TEXT]       = $subArray[3];
        }

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->seme;
        $data["TESTKINDCD"][TEXT]           = $model->kind;
        $data["TESTITEMCD"][TEXT]           = $model->item;
        $data["SCORE_DIV"][TEXT]            = $model->sdiv;
        $data["GRADE"][TEXT]                = $model->grade;
        $data["CHAIRDATE"][TEXT]            = str_replace("/", "-", $model->chairdate);
        $data["ELECTDIV_FLG"][TEXT]         = $model->electdiv;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_RANK_EXEC_SDIV_DAT");
        return $query;
    }


    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // -- アルファベット順に更新するようにした。--

        // RECORD_AVERAGE_CHAIR_DAT---各講座の合計・最高点・最低点・人数・平均・標準偏差を保持
        knjd210wquery::insertSubclassQuery($db, $model, "record_average_chair");
        $db->commit();

        // RECORD_AVERAGE_DAT---各科目の合計・最高点・最低点・人数・平均・標準偏差を保持
        knjd210wquery::insertSubclassQuery($db, $model, "record_average");
        $db->commit();

        // RECORD_RANK_CHAIR_DAT---各講座の順位・偏差値を保持
        knjd210wquery::insertSubclassQuery($db, $model, "record_rank_chair");
        $db->commit();

        // RECORD_RANK_DAT---各科目の順位・偏差値を保持
        knjd210wquery::insertSubclassQuery($db, $model, "record_rank");
        $db->commit();

        // -- アルファベット順に更新するようにした。--

        Query::dbCheckIn($db);
        return true;
    }

    /* 科目別の処理 */
    function insertSubclassQuery(&$db, $model, $table)
    {
        $query  = "";
        //削除クエリー
        $query  = knjd210wQuery::getDeleteQuery($model, $table);
        $model->debugTime("delete :".$table, $query);
        $db->query($query);
        //追加クエリー
        $query  = knjd210wQuery::getInsertSubclassQuery($model, $table);
        $model->debugTime("insert :".$table, $query);
        $result = $db->query($query);
        $model->debugTime("start insert while : ".$table, "");
        $count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //プロパティー「use_school_detail_gcm_dat」が"1"の時、AVG_DIVが"1"のデータをセットしない。
            if ($table == "record_average" || $table == "record_average_chair") {
                if ($model->Properties["use_school_detail_gcm_dat"] == '1' && $row["AVG_DIV"] == "1") {
                    continue;
                }
            }
            //項目
            $data = knjd210wQuery::getFieldData($model, $table, $row);
            //追加
            $query = Query::insertSQL($data, $table .$model->tableStr);
            $db->query($query);
            $count += 1;
        }
        $model->debugTime("end insert while:".$table." / count = ".$count, "");
        return;
    }

    //削除クエリー
    function getDeleteQuery($model, $table)
    {
        $query  = "";
        if ($table == "record_average" || $table == "record_average_chair") {
            $query .= "DELETE FROM " .$table .$model->tableStr ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and SCORE_DIV  = '".$model->sdiv."' ";
            $query .= "  and GRADE      = '".$model->grade."' ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  and SUBCLASSCD in ('333333','555555','999999') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  and CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
        }
        if ($table == "record_rank" || $table == "record_rank_chair") {
            $query .= "DELETE FROM " .$table .$model->tableStr ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and SCORE_DIV  = '".$model->sdiv."' ";
            $query .= "  and SCHREGNO   in (SELECT SCHREGNO FROM ( ";
            $query .= knjd210wQuery::getSchregQuery($model);
            $query .= "  ) t1 ) ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  and SUBCLASSCD in ('333333','555555','999999') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  and CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
        }
        return $query;
    }

    //項目
    function getFieldData($model, $table, $data2)
    {
        $data = array();
        if ($table == "record_average" || $table == "record_average_chair") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->seme;
            $data["TESTKINDCD"][TEXT]   = $model->kind;
            $data["TESTITEMCD"][TEXT]   = $model->item;
            $data["SCORE_DIV"][TEXT]    = $model->sdiv;
            $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]   = $data2["SUBCLASSCD"];
            if ($table == "record_average_chair") {
                $data["CHAIRCD"][TEXT]   = $data2["CHAIRCD"];
            }
            $data["AVG_DIV"][TEXT]      = $data2["AVG_DIV"];
            $data["GRADE"][TEXT]        = $data2["GRADE"];
            $data["HR_CLASS"][TEXT]     = $data2["HR_CLASS"];
            $data["COURSECD"][TEXT]     = $data2["COURSECD"];
            $data["MAJORCD"][TEXT]      = $data2["MAJORCD"];
            $data["COURSECODE"][TEXT]   = $data2["COURSECODE"];
            $data["SCORE"][NUMBER]      = $data2["SCORE"];
            $data["HIGHSCORE"][NUMBER]  = $data2["HIGHSCORE"];
            $data["LOWSCORE"][NUMBER]   = $data2["LOWSCORE"];
            $data["COUNT"][NUMBER]      = $data2["COUNT"];
            $data["AVG"][NUMBER]        = $data2["AVG"];
            $data["STDDEV"][NUMBER]     = $data2["STDDEV"];
            if ($table == "record_average_chair") {
                $data["CHAIRDATE"][TEXT]    = str_replace("/", "-", $model->chairdate);
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        }
        if ($table == "record_rank" || $table == "record_rank_chair") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->seme;
            $data["TESTKINDCD"][TEXT]   = $model->kind;
            $data["TESTITEMCD"][TEXT]   = $model->item;
            $data["SCORE_DIV"][TEXT]    = $model->sdiv;
            $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]   = $data2["SUBCLASSCD"];
            if ($table == "record_rank_chair") {
                $data["CHAIRCD"][TEXT]   = $data2["CHAIRCD"];
            }
            $data["SCHREGNO"][TEXT]     = $data2["SCHREGNO"];
            $data["SCORE"][NUMBER]      = $data2["SCORE"];
            $data["AVG"][NUMBER]        = $data2["AVG"];
            //プロパティー「use_school_detail_gcm_dat」が"1"の時、GRADE_XXXのデータをセットしない。
            $data["GRADE_RANK"][NUMBER]           = $data2["GRADE_RANK"];
            $data["GRADE_AVG_RANK"][NUMBER]       = $data2["GRADE_AVG_RANK"];
            $data["GRADE_DEVIATION"][NUMBER]      = $data2["GRADE_DEVIATION"];
            $data["GRADE_DEVIATION_RANK"][NUMBER] = $data2["GRADE_DEVIATION_RANK"];
            $data["CLASS_RANK"][NUMBER]           = $data2["CLASS_RANK"];
            $data["CLASS_AVG_RANK"][NUMBER]       = $data2["CLASS_AVG_RANK"];
            $data["CLASS_DEVIATION"][NUMBER]      = $data2["CLASS_DEVIATION"];
            $data["CLASS_DEVIATION_RANK"][NUMBER] = $data2["CLASS_DEVIATION_RANK"];
            $data["COURSE_RANK"][NUMBER]          = $data2["COURSE_RANK"];
            $data["COURSE_AVG_RANK"][NUMBER]      = $data2["COURSE_AVG_RANK"];
            $data["COURSE_DEVIATION"][NUMBER]     = $data2["COURSE_DEVIATION"];
            $data["COURSE_DEVIATION_RANK"][NUMBER]= $data2["COURSE_DEVIATION_RANK"];
            //1)プロパティー「useRankCourseGroupVersion2」が"1"の時、
            //    MAJOR_XXXに学科をセットする。
            //    COURSE_GROUP_XXXにコースグループをセットする。
            //2)プロパティー「useRankCourseGroup」が"1"の時、
            //    MAJOR_XXXにコースグループをセットする。
            //3)上記以外の時、
            //    MAJOR_XXXに学科をセットする。
            if ($model->Properties["useRankCourseGroupVersion2"] == '1') {
                $data["MAJOR_RANK"][NUMBER]                  = $data2["MAJOR_RANK"];
                $data["MAJOR_AVG_RANK"][NUMBER]              = $data2["MAJOR_AVG_RANK"];
                $data["MAJOR_DEVIATION"][NUMBER]             = $data2["MAJOR_DEVIATION"];
                $data["MAJOR_DEVIATION_RANK"][NUMBER]        = $data2["MAJOR_DEVIATION_RANK"];
                $data["COURSE_GROUP_RANK"][NUMBER]           = $data2["COURSE_GROUP_RANK"];
                $data["COURSE_GROUP_AVG_RANK"][NUMBER]       = $data2["COURSE_GROUP_AVG_RANK"];
                $data["COURSE_GROUP_DEVIATION"][NUMBER]      = $data2["COURSE_GROUP_DEVIATION"];
                $data["COURSE_GROUP_DEVIATION_RANK"][NUMBER] = $data2["COURSE_GROUP_DEVIATION_RANK"];
            } elseif ($model->Properties["useRankCourseGroup"] == '1') {
                $data["MAJOR_RANK"][NUMBER]             = $data2["COURSE_GROUP_RANK"];
                $data["MAJOR_AVG_RANK"][NUMBER]         = $data2["COURSE_GROUP_AVG_RANK"];
                $data["MAJOR_DEVIATION"][NUMBER]        = $data2["COURSE_GROUP_DEVIATION"];
                $data["MAJOR_DEVIATION_RANK"][NUMBER]   = $data2["COURSE_GROUP_DEVIATION_RANK"];
            } else {
                $data["MAJOR_RANK"][NUMBER]             = $data2["MAJOR_RANK"];
                $data["MAJOR_AVG_RANK"][NUMBER]         = $data2["MAJOR_AVG_RANK"];
                $data["MAJOR_DEVIATION"][NUMBER]        = $data2["MAJOR_DEVIATION"];
                $data["MAJOR_DEVIATION_RANK"][NUMBER]   = $data2["MAJOR_DEVIATION_RANK"];
            }
            if ($table == "record_rank_chair") {
                $data["CHAIRDATE"][TEXT]    = str_replace("/", "-", $model->chairdate);
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        }
        return $data;
    }

    //（共通）追加クエリー
    function getInsertCommonQuery($model, $table)
    {

        $query  = "";
        //在籍
        $query .= "WITH T_SCHREG AS ( ";
        $query .= knjd210wQuery::getSchregQuery($model);
        $query .= "    ) ";
        //３科・５科のグループデータ
        $query .= ",T_GROUP AS ( ";
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T2.GROUP_DIV, "; //3:３科 5:５科
        $query .= "        T2.CLASSCD, ";
        $query .= "        T2.SCHOOL_KIND, ";
        $query .= "        T2.CURRICULUM_CD, ";
        $query .= "        T2.SUBCLASSCD ";
        $query .= "    FROM ";
        $query .= "        T_SCHREG T1 ";
        $query .= "        LEFT JOIN REC_SUBCLASS_GROUP_DAT T2 ";
        $query .= "             ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "            AND T2.GRADE = T1.GRADE ";
        $query .= "            AND T2.COURSECD = T1.COURSECD ";
        $query .= "            AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "            AND T2.COURSECODE = T1.COURSECODE ";
        $query .= "    ) ";
        //KNJD129Qにて、平均点欄を表示・・・入力不可（表示のみ）
        //(9-9900-08)各科目のRECORD_RANK_SDIV_DAT.AVGに保持しておく。
        $testcd = $model->seme.$model->kind.$model->item.$model->sdiv;
        //成績
        $query .= ",T_SCORE AS ( ";
        $query .= knjd210wQuery::getRecordQuery($model);
        $query .= "    ) ";
        if ($table == "record_average" || $table == "record_rank") {
            //SCOREがNULLの学籍番号（序列対象外とする生徒）を取得 (T_SCORE_SUMで使用)
            $query .= ",T_SCORE_NULL AS ( ";
            $query .= knjd210wQuery::getRecordNullQuery($model);
            $query .= "    ) ";
            //各生徒の３科目・５科目・全科目のSCOREの合計を取得
            $query .= ",T_SCORE_SUM AS ( ";
            $query .= knjd210wQuery::getRecordScoreSumQuery($model);
            $query .= "    ) ";
        }
        //在籍&成績２ T_SCOREとT_SCORE_SUMの序列対象の生徒
        $query .= ",T_RANK AS ( ";
        $query .= knjd210wQuery::getRecordSchregQuery($model, $table);
        $query .= "    ) ";

        return $query;
    }//getInsertCommonQuery

    //在籍クエリー
    function getSchregQuery($model)
    {
        $query  = "";
        $query .= "    SELECT ";
        $query .= "        W1.SCHREGNO, ";
        $query .= "        W1.GRADE, ";
        $query .= "        W1.HR_CLASS, ";
        $query .= "        W1.COURSECD,W1.MAJORCD,W1.COURSECODE, ";
        $query .= "        W1.COURSECD||W1.MAJORCD||W1.COURSECODE AS COURSE, ";
        $query .= "        W1.COURSECD||W1.MAJORCD||'0000' AS MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "       ,VALUE(W2.GROUP_CD, '000') AS COURSE_GROUP ";
        }
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_DAT W1 ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "        LEFT JOIN COURSE_GROUP_CD_DAT W2 ";
            $query .= "             ON W2.YEAR = W1.YEAR ";
            $query .= "            AND W2.GRADE = W1.GRADE ";
            $query .= "            AND W2.COURSECD = W1.COURSECD ";
            $query .= "            AND W2.MAJORCD = W1.MAJORCD ";
            $query .= "            AND W2.COURSECODE = W1.COURSECODE ";
        }
        $query .= "    WHERE ";
        $query .= "        W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        W1.SEMESTER = '".$model->seme_sch."' AND ";
        $query .= "        W1.GRADE = '".$model->grade."' ";
        return $query;
    }

    //成績クエリー
    function getRecordQuery($model)
    {
        $query  = "";
        $query .= "    SELECT ";
        $query .= "        W1.CLASSCD, ";
        $query .= "        W1.SCHOOL_KIND, ";
        $query .= "        W1.CURRICULUM_CD, ";
        $query .= "        W1.SUBCLASSCD, ";
        $query .= "        W1.SCHREGNO, ";
        $query .= "        W1.SCORE as SCORE ";
        $query .= "       ,W1.SCORE as SCORE_NULL ";
        $query .= "       ,W1.VALUE_DI ";

        //TODO2の箇所で使用
        if ($model->seme == "9") {
            $query .= "   ,W1.COMP_CREDIT ";
            $query .= "   ,W1.GET_CREDIT ";
        }
        $query .= "       ,VALUE(L1.PERFECT, 100) AS PERFECT ";
        $query .= "    FROM ";
        $query .= "        RECORD_SCORE_DAT W1 ";
        $query .= "        INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = W1.YEAR ";
        $query .= "            AND CYDAT.CLASSCD = W1.CLASSCD ";
        $query .= "            AND CYDAT.SCHOOL_KIND = W1.SCHOOL_KIND ";
        $query .= "        INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = W1.YEAR ";
        $query .= "            AND SYDAT.CLASSCD = W1.CLASSCD ";
        $query .= "            AND SYDAT.SCHOOL_KIND = W1.SCHOOL_KIND ";
        $query .= "            AND SYDAT.CURRICULUM_CD = W1.CURRICULUM_CD ";
        $query .= "            AND SYDAT.SUBCLASSCD = W1.SUBCLASSCD ";
        $query .= "        INNER JOIN T_SCHREG W2 ON W2.SCHREGNO = W1.SCHREGNO ";
        $query .= "        LEFT JOIN PERFECT_RECORD_SDIV_DAT L1 ON L1.YEAR = W1.YEAR ";
        $query .= "             AND L1.SEMESTER = W1.SEMESTER ";
        $query .= "             AND L1.TESTKINDCD = W1.TESTKINDCD ";
        $query .= "             AND L1.TESTITEMCD = W1.TESTITEMCD ";
        $query .= "             AND L1.SCORE_DIV = W1.SCORE_DIV ";
        $query .= "             AND L1.CLASSCD = W1.CLASSCD ";
        $query .= "             AND L1.SCHOOL_KIND = W1.SCHOOL_KIND ";
        $query .= "             AND L1.CURRICULUM_CD = W1.CURRICULUM_CD ";
        $query .= "             AND L1.SUBCLASSCD = W1.SUBCLASSCD ";
        $query .= "             AND L1.GRADE = CASE WHEN L1.DIV = '01' THEN '00' ELSE W2.GRADE END ";
        $query .= "             AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN L1.DIV IN ('01','02') THEN '00000000' ELSE W2.COURSECD || W2.MAJORCD || W2.COURSECODE END ";
        $query .= "    WHERE ";
        $query .= "        W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        W1.SEMESTER = '".$model->seme."' AND ";
        $query .= "        W1.TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        W1.TESTITEMCD = '".$model->item."' AND ";
        $query .= "        W1.SCORE_DIV = '".$model->sdiv."' ";
        return $query;
    }

    //(SCOREがNULL)かつ(VALUE_DIが*)の学籍番号を取得するクエリー （序列対象外とする生徒）
    function getRecordNullQuery($model)
    {
        $query_cls = array();
        foreach (array("3", "5", "9") as $div) {
            $cd2 = "";
            $cd6 = "";
            for ($i = 0; $i < 2; $i++) {
                $cd2 .= $div;
            }
            for ($i = 0; $i < 6; $i++) {
                $cd6 .= $div;
            }

            $query  = "";
            $query .= "    SELECT ";
            $query .= "        '".$cd6."' AS SUBCLASSCD, T1.SCHREGNO ";
            $query .= "    FROM ";
            $query .= "        T_SCORE T1 ";
            $query .= "    WHERE ";
            $query .= "    T1.SCORE IS NULL ";
            $query .= "    AND T1.VALUE_DI IN ('*', '**') ";
            //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
            if ($model->d070Namespare1 == "1") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                $query .= "             AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "             AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "             AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            if ($model->d070Namespare1 == "2") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                $query .= "             AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "             AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "             AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            //選択科目を除く
            if ($model->electdiv == "1") {
                $query .= " AND NOT EXISTS( ";
                $query .= "        SELECT ";
                $query .= "            'X' ";
                $query .= "        FROM ";
                $query .= "            SUBCLASS_MST W1 ";
                $query .= "        WHERE ";
                $query .= "            W1.ELECTDIV = '1' AND ";
                $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
                $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "        ) ";
            }
            //３科目・５科目
            if ($div != "9") {
                $query .= " AND EXISTS( ";
                $query .= "        SELECT ";
                $query .= "            'X' ";
                $query .= "        FROM ";
                $query .= "            T_GROUP W1 ";
                $query .= "        WHERE ";
                $query .= "            W1.GROUP_DIV = '".$div."' AND ";
                $query .= "            W1.SCHREGNO = T1.SCHREGNO AND ";
                $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
                $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "        ) ";
            //全科目
            } else {
                //教科科目名称マスタのマスタ化
    //            if ($model->Properties["useClassDetailDat"] == '1') {
    //                $query .= " AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
    //            } else {
                    //名称マスタ「D017」NAME1に登録されている科目を除く。
                    $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
    //            }
                //名称マスタ「D065」NAME1に登録されている科目を除く。
                $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
                //教科９０以上は除く。つまり、教科９０未満が対象
                $query .= " AND T1.SUBCLASSCD not like '9%' ";
                //「学年末」の場合、履修単位・修得単位の両方 NULL が対象・・・TODO2
                if ($model->seme == "9") {
                    $query .= " AND T1.COMP_CREDIT IS NULL ";
                    $query .= " AND T1.GET_CREDIT IS NULL ";
                }
                //素点・評価の両方 NULL が対象 TODO1
                $query .= " AND T1.SCORE_NULL IS NULL ";
            }
            $query .= "    GROUP BY T1.SCHREGNO ";
            $query_cls[] = $query;
        }
        $query = implode(" UNION ALL ", $query_cls);
        return $query;
    }

    //各生徒の３科目・５科目・全科目のSCOREの合計を取得するクエリー
    function getRecordScoreSumQuery($model)
    {
        $cl_query = array();
        foreach (array("3", "5", "9") as $div) {
            $cd2 = "";
            $cd6 = "";
            for ($i = 0; $i < 2; $i++) {
                $cd2 .= $div;
            }
            for ($i = 0; $i < 6; $i++) {
                $cd6 .= $div;
            }

            $query  = "";
            $query .= "    SELECT  ";
            $query .= "        '".$cd2."' as CLASSCD, ";
            $query .= "        max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "        '99' as CURRICULUM_CD, ";
            $query .= "        '".$cd6."' as SUBCLASSCD, ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        DECIMAL(ROUND(SUM(FLOAT(T1.SCORE))/SUM(FLOAT(T1.PERFECT))*100*100000,0)/100000,9,5) AS AVG, ";
            $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
            $query .= "        SUM(T1.SCORE) AS SCORE ";
            $query .= "    FROM ";
            $query .= "        T_SCORE T1 ";
            $query .= "    WHERE ";
            $query .= "        T1.SCORE IS NOT NULL ";
            if ($div == "3" || $div == "5") {
                $query .= "        AND EXISTS( ";
                $query .= "            SELECT ";
                $query .= "                'X' ";
                $query .= "            FROM ";
                $query .= "                T_GROUP W1 ";
                $query .= "            WHERE ";
                $query .= "                W1.GROUP_DIV = '".$div."' AND ";
                $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
                $query .= "                W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "                W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "                W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
                $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "            ) ";
            } elseif ($div == "9") {
                //選択科目を除く
                if ($model->electdiv == "1") {
                    $query .= "     AND NOT EXISTS( ";
                    $query .= "        SELECT ";
                    $query .= "            'X' ";
                    $query .= "        FROM ";
                    $query .= "            SUBCLASS_MST W1 ";
                    $query .= "        WHERE ";
                    $query .= "            W1.ELECTDIV = '1' AND ";
                    $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
                    $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                    $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
                    $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
                    $query .= "        ) ";
                }

                //教科科目名称マスタのマスタ化
//                if ($model->Properties["useClassDetailDat"] == '1') {
//                    $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
//                } else {
                    ////名称マスタ「D017」NAME1に登録されている科目を除く。
                    //$query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
//                }
                //名称マスタ「D065」NAME1に登録されている科目を除く。
                $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
            }

            //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
            if ($model->d070Namespare1 == "1") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                $query .= "             AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "             AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "             AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            if ($model->d070Namespare1 == "2") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                $query .= "             AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "             AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "             AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            $query .= "    GROUP BY T1.SCHREGNO ";

            $cl_query[] = $query;
        }
        $query = implode(" UNION ALL ", $cl_query);

        return $query;
    }

    //成績平均データクエリー
    function getRecordAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_rank_chair") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_rank_chair") ? "record_average_chair" : "record_average";
        $query  = "";
        $query .= "    SELECT ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "        SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "        AVG_DIV, ";
        $query .= "        GRADE, ";
        $query .= "        HR_CLASS, ";
        $query .= "        COURSECD||MAJORCD||COURSECODE AS COURSE, ";
        $query .= "        COURSECD||MAJORCD||'0000' AS MAJOR, ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "        MAJORCD AS COURSE_GROUP, ";
        }
        $query .= "        DECIMAL(ROUND(FLOAT(AVG)*10,0)/10,5,1) AS AVG,STDDEV ";
        $query .= "    FROM ";
        $query .= $table_name .$model->tableStr;
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER = '".$model->seme."' AND ";
        $query .= "        TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        TESTITEMCD = '".$model->item."' AND ";
        $query .= "        SCORE_DIV = '".$model->sdiv."' AND ";
        $query .= "        GRADE = '".$model->grade."' ";
        return $query;
    }

    //在籍&成績２クエリー  T_SCOREとT_SCORE_SUMの序列対象の生徒
    function getRecordSchregQuery($model, $table)
    {
        $query  = "";
        $query .= "    SELECT ";
        $query .= "        W1.CLASSCD, ";
        $query .= "        W1.SCHOOL_KIND, ";
        $query .= "        W1.CURRICULUM_CD, ";
        $query .= "        W1.SUBCLASSCD, ";
        $query .= "        W1.SCHREGNO, ";
        $query .= "        W1.SCORE, ";
        $query .= "        cast(null as decimal) as AVG, ";
        $query .= "        W2.GRADE, ";
        $query .= "        W2.HR_CLASS, ";
        $query .= "        W2.COURSECD,W2.MAJORCD,W2.COURSECODE, ";
        $query .= "        W2.COURSE, ";
        $query .= "        W2.MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "       ,COURSE_GROUP ";
        }
        $query .= "    FROM ";
        $query .= "        T_SCORE W1 ";
        $query .= "        INNER JOIN T_SCHREG W2 ON W2.SCHREGNO = W1.SCHREGNO ";
        $query .= "    WHERE ";
        $query .= "        W1.SCORE IS NOT NULL ";
        if ($table == "record_average" || $table == "record_rank") {
            $query .= "    UNION ALL ";
            $query .= "    SELECT ";
            $query .= "        W1.CLASSCD, ";
            $query .= "        W1.SCHOOL_KIND, ";
            $query .= "        W1.CURRICULUM_CD, ";
            $query .= "        W1.SUBCLASSCD, ";
            $query .= "        W1.SCHREGNO, ";
            $query .= "        W1.SCORE, ";
            $query .= "        W1.AVG, ";
            $query .= "        W2.GRADE, ";
            $query .= "        W2.HR_CLASS, ";
            $query .= "        W2.COURSECD,W2.MAJORCD,W2.COURSECODE, ";
            $query .= "        W2.COURSE, ";
            $query .= "        W2.MAJOR ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
                $query .= "       ,W2.COURSE_GROUP ";
            }
            $query .= "    FROM ";
            $query .= "        T_SCORE_SUM W1 ";
            $query .= "        INNER JOIN T_SCHREG W2 ON W1.SCHREGNO = W2.SCHREGNO ";
            $query .= "    WHERE ";
            $query .= "     (W1.SUBCLASSCD  = '333333' OR W1.SUBCLASSCD  = '555555' OR W1.SUBCLASSCD  = '999999') ";
            $query .= "     AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL W3 WHERE W3.SCHREGNO = W2.SCHREGNO AND W3.SUBCLASSCD = W1.SUBCLASSCD) ";
        }
        return $query;
    }

    //成績平均データの追加クエリー
    function getInsertAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_average_chair") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_average_chair") ? "_CHAIR" : "";
        $query  = "";
        //1:学年
        $query .= "SELECT ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '1' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    '0' as COURSECD, ";
        $query .= "    '000' as MAJORCD, ";
        $query .= "    '0000' as COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE ";
        //2:クラス
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '2' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS, ";
        $query .= "    '0' as COURSECD, ";
        $query .= "    '000' as MAJORCD, ";
        $query .= "    '0000' as COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS ";
        //3:コース
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '3' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE ";
        //4:学科
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '4' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    '0000' AS COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    '5' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    '000' as HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    COURSE_GROUP as MAJORCD, ";
            $query .= "    '0000' AS COURSECODE, ";
            $query .= "    SUM(SCORE) AS SCORE, ";
            $query .= "    MAX(SCORE) AS HIGHSCORE, ";
            $query .= "    MIN(SCORE) AS LOWSCORE, ";
            $query .= "    COUNT(SCHREGNO) AS COUNT, ";
            $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
            $query .= "FROM ";
            $query .= "    T_RANK" .$table_name ." ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
            $query .= "GROUP BY ";
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    GRADE, ";
            $query .= "    COURSE_GROUP ";
        }
        return $query;
    }//getInsertAverageQuery

    //成績席次データの追加クエリー
    function getInsertRankQuery($model, $table)
    {
        $field_name  = $table_name  = $where_name2 = $where_name3 = $where_name4 = $where_name5 = $where_name6 = $where_name7 = "";
        if ($table == "record_rank_chair") {
            $field_name  = "W1.CHAIRCD,";
            $table_name  = "_CHAIR";
            $where_name2 = "W2.CHAIRCD = W1.CHAIRCD AND ";
            $where_name3 = "W3.CHAIRCD = W1.CHAIRCD AND ";
            $where_name4 = "W4.CHAIRCD = W1.CHAIRCD AND ";
            $where_name5 = "W5.CHAIRCD = W1.CHAIRCD AND ";
            $where_name6 = "W6.CHAIRCD = W1.CHAIRCD AND ";
            $where_name7 = "W7.CHAIRCD = W1.CHAIRCD AND ";
        }
        //教育課程対応
        $fieldCrc = $whereCrc2 = $whereCrc3 = $whereCrc4 = $whereCrc5 = $whereCrc6 = $whereCrc7 = "";
        $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
        $whereCrc2  = "W2.CLASSCD = W1.CLASSCD AND W2.SCHOOL_KIND = W1.SCHOOL_KIND AND W2.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        $whereCrc3  = "W3.CLASSCD = W1.CLASSCD AND W3.SCHOOL_KIND = W1.SCHOOL_KIND AND W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        $whereCrc4  = "W4.CLASSCD = W1.CLASSCD AND W4.SCHOOL_KIND = W1.SCHOOL_KIND AND W4.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        $whereCrc5  = "W5.CLASSCD = W1.CLASSCD AND W5.SCHOOL_KIND = W1.SCHOOL_KIND AND W5.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        $whereCrc6  = "W6.CLASSCD = W1.CLASSCD AND W6.SCHOOL_KIND = W1.SCHOOL_KIND AND W6.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        $whereCrc7  = "W7.CLASSCD = W1.CLASSCD AND W7.SCHOOL_KIND = W1.SCHOOL_KIND AND W7.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        $query  = "";

//        if ($table == "record_rank" && $model->calcDeviationFlg == "1") {
//            //各科目の偏差値
//            $query .= ",T_DEVIATION_SUB AS ( ";
//            $query .= "     SELECT ";
//            $query .= "         {$fieldCrc}W1.SUBCLASSCD, ";
//            $query .= $field_name;
//            $query .= "         W1.SCHREGNO, ";
//            //5:コースグループ
//            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
//                $query .= "     case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W6.AVG)/W6.STDDEV+50)*10,0)/10,5,1) end AS COURSE_GROUP_DEVIATION, ";
//            }
//            $query .= "         case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
//            $query .= "         case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
//            $query .= "         case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
//            $query .= "         case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W5.AVG)/W5.STDDEV+50)*10,0)/10,5,1) end AS MAJOR_DEVIATION ";
//            $query .= "     FROM ";
//            $query .= "         T_RANK" .$table_name ." W1 ";
//            $query .= "         LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
//            $query .= "         LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
//            $query .= "         LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
//            $query .= "         LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '4' AND {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name5 ."W5.GRADE = W1.GRADE AND W5.MAJOR = W1.MAJOR ";
//            //5:コースグループ
//            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
//                $query .= "     LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '5' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.COURSE_GROUP = W1.COURSE_GROUP ";
//            }
//            $query .= "     WHERE ";
//            $query .= "         W1.SUBCLASSCD not in ('333333','555555','999999') ";
//            $query .= "    ) ";
//            //３科・５科・全科の偏差値
//            $query .= ",T_DEVIATION_ALL AS ( ";
//
//            $unionAll = "";
//            foreach (array("3", "5", "9") as $div) {
//                $cd2 = "";
//                $cd6 = "";
//                for ($i = 0; $i < 2; $i++) {
//                    $cd2 .= $div;
//                }
//                for ($i = 0; $i < 6; $i++) {
//                    $cd6 .= $div;
//                }
//
//                $query .= $unionAll;
//                $query .= "    SELECT  ";
//                $query .= "        '".$cd2."' as CLASSCD, ";
//                $query .= "        max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
//                $query .= "        '99' as CURRICULUM_CD, ";
//                $query .= "        '".$cd6."' as SUBCLASSCD, ";
//                $query .= "        T1.SCHREGNO, ";
//                //5:コースグループ
//                if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
//                    $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
//                }
//                $query .= "        DECIMAL(ROUND(AVG(T1.GRADE_DEVIATION) *10,0)/10,5,1) AS GRADE_DEVIATION, ";
//                $query .= "        DECIMAL(ROUND(AVG(T1.CLASS_DEVIATION) *10,0)/10,5,1) AS CLASS_DEVIATION, ";
//                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_DEVIATION)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
//                $query .= "        DECIMAL(ROUND(AVG(T1.MAJOR_DEVIATION) *10,0)/10,5,1) AS MAJOR_DEVIATION ";
//                $query .= "    FROM ";
//                $query .= "        T_DEVIATION_SUB T1 ";
//                $query .= "    WHERE ";
//                if ($div == "3" || $div == "5") {
//                    $query .= "        EXISTS( ";
//                    $query .= "            SELECT ";
//                    $query .= "                'X' ";
//                    $query .= "            FROM ";
//                    $query .= "                T_GROUP W1 ";
//                    $query .= "            WHERE ";
//                    $query .= "                W1.GROUP_DIV = '".$div."' AND ";
//                    $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
//                    $query .= "                W1.CLASSCD = T1.CLASSCD AND ";
//                    $query .= "                W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
//                    $query .= "                W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
//                    $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
//                    $query .= "            ) ";
//                } else {
//                    //教科科目名称マスタのマスタ化
//        //            if ($model->Properties["useClassDetailDat"] == '1') {
//        //                $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
//        //            } else {
//                        //名称マスタ「D017」NAME1に登録されている科目を除く。
//                        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
//        //            }
//                    //名称マスタ「D065」NAME1に登録されている科目を除く。
//                    $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
//
//                    //選択科目を除く
//                    if ($model->electdiv == "1") {
//                        $query .= " AND NOT EXISTS( ";
//                        $query .= "        SELECT ";
//                        $query .= "            'X' ";
//                        $query .= "        FROM ";
//                        $query .= "            SUBCLASS_MST W1 ";
//                        $query .= "        WHERE ";
//                        $query .= "            W1.ELECTDIV = '1' AND ";
//                        $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
//                        $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
//                        $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
//                        $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
//                        $query .= "        ) ";
//                    }
//                }
//                //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
//                if ($model->d070Namespare1 == "1") {
//                    $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
//                    $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
//                    $query .= "             AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
//                    $query .= "             AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
//                    $query .= "             AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
//                    $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
//                }
//                if ($model->d070Namespare1 == "2") {
//                    $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
//                    $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
//                    $query .= "             AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
//                    $query .= "             AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
//                    $query .= "             AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
//                    $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
//                }
//                $query .= "    GROUP BY T1.SCHREGNO ";
//                $unionAll = "    UNION ALL ";
//            }
//            $query .= "    ) ";
//        }

        //各科目
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    W1.SCHREGNO, ";
        $query .= "    W1.SCORE, ";
        $query .= "    W1.AVG, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.SCORE DESC) AS MAJOR_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.SCORE DESC) AS MAJOR_AVG_RANK, ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_AVG_RANK, ";
            $query .= "    case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W6.AVG)/W6.STDDEV+50)*10,0)/10,5,1) end AS COURSE_GROUP_DEVIATION, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY DECIMAL(ROUND((case when 0 < W6.STDDEV then 10*(W1.SCORE-W6.AVG)/W6.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_GROUP_DEVIATION_RANK, ";
        }
        $query .= "    case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
        $query .= "    case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
        $query .= "    case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
        $query .= "    case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W5.AVG)/W5.STDDEV+50)*10,0)/10,5,1) end AS MAJOR_DEVIATION, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE    ORDER BY DECIMAL(ROUND((case when 0 < W2.STDDEV then 10*(W1.SCORE-W2.AVG)/W2.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS GRADE_DEVIATION_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY DECIMAL(ROUND((case when 0 < W3.STDDEV then 10*(W1.SCORE-W3.AVG)/W3.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS CLASS_DEVIATION_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE   ORDER BY DECIMAL(ROUND((case when 0 < W4.STDDEV then 10*(W1.SCORE-W4.AVG)/W4.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_DEVIATION_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR    ORDER BY DECIMAL(ROUND((case when 0 < W5.STDDEV then 10*(W1.SCORE-W5.AVG)/W5.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS MAJOR_DEVIATION_RANK ";

        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." W1 ";
        $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
        $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
        $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
        $query .= "    LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '4' AND {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name5 ."W5.GRADE = W1.GRADE AND W5.MAJOR = W1.MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "    LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '5' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.COURSE_GROUP = W1.COURSE_GROUP ";
        }
        $query .= "WHERE ";
        $query .= "    W1.SUBCLASSCD not in ('333333','555555','999999') ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  and W1.SUBCLASSCD in ('333333','555555','999999') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  and W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD = '".$model->subclasscd."' ";
        }
        if ($table == "record_rank") {
            //３科・５科・全科
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            $query .= "    {$fieldCrc}W1.SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    W1.SCHREGNO, ";
            $query .= "    W1.SCORE, ";
            $query .= "    W1.AVG, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.SCORE DESC) AS MAJOR_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.AVG DESC) AS GRADE_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.AVG DESC) AS CLASS_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.AVG DESC) AS COURSE_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.AVG DESC) AS MAJOR_AVG_RANK, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.AVG DESC) AS COURSE_GROUP_AVG_RANK, ";
//                if ($model->calcDeviationFlg == "1") {
//                    $query .= "    DV.COURSE_GROUP_DEVIATION AS COURSE_GROUP_DEVIATION, ";
//                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY DV.COURSE_GROUP_DEVIATION DESC) AS COURSE_GROUP_DEVIATION_RANK, ";
//                } else {
                    $query .= "    case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W6.AVG)/W6.STDDEV+50)*10,0)/10,5,1) end AS COURSE_GROUP_DEVIATION, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY DECIMAL(ROUND((case when 0 < W6.STDDEV then 10*(W1.SCORE-W6.AVG)/W6.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_GROUP_DEVIATION_RANK, ";
//                }
            }
//            if ($model->calcDeviationFlg == "1") {
//                $query .= "    DV.GRADE_DEVIATION  AS GRADE_DEVIATION, ";
//                $query .= "    DV.CLASS_DEVIATION  AS CLASS_DEVIATION, ";
//                $query .= "    DV.COURSE_DEVIATION AS COURSE_DEVIATION, ";
//                $query .= "    DV.MAJOR_DEVIATION  AS MAJOR_DEVIATION, ";
//                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE    ORDER BY DV.GRADE_DEVIATION  DESC) AS GRADE_DEVIATION_RANK, ";
//                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY DV.CLASS_DEVIATION  DESC) AS CLASS_DEVIATION_RANK, ";
//                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE   ORDER BY DV.COURSE_DEVIATION DESC) AS COURSE_DEVIATION_RANK, ";
//                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR    ORDER BY DV.MAJOR_DEVIATION  DESC) AS MAJOR_DEVIATION_RANK ";
//            } else {
                $query .= "    case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
                $query .= "    case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
                $query .= "    case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
                $query .= "    case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W5.AVG)/W5.STDDEV+50)*10,0)/10,5,1) end AS MAJOR_DEVIATION, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE    ORDER BY DECIMAL(ROUND((case when 0 < W2.STDDEV then 10*(W1.SCORE-W2.AVG)/W2.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS GRADE_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY DECIMAL(ROUND((case when 0 < W3.STDDEV then 10*(W1.SCORE-W3.AVG)/W3.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS CLASS_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE   ORDER BY DECIMAL(ROUND((case when 0 < W4.STDDEV then 10*(W1.SCORE-W4.AVG)/W4.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR    ORDER BY DECIMAL(ROUND((case when 0 < W5.STDDEV then 10*(W1.SCORE-W5.AVG)/W5.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS MAJOR_DEVIATION_RANK ";
            //}

            $query .= "FROM ";
            $query .= "    T_RANK" .$table_name ." W1 "; // 対象はT_RANK、T_RANK_CHAIRで決定している
//            if ($model->calcDeviationFlg == "1") {
//                $query .= "    LEFT JOIN T_DEVIATION_ALL DV ON DV.SUBCLASSCD = W1.SUBCLASSCD AND DV.SCHREGNO = W1.SCHREGNO ";
//            } else {
                $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
                $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
                $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
                $query .= "    LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '4' AND {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name5 ."W5.GRADE = W1.GRADE AND W5.MAJOR = W1.MAJOR ";
                //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
                $query .= "    LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '5' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.COURSE_GROUP = W1.COURSE_GROUP ";
            }
//            }
            $query .= "WHERE ";
            $query .= "    W1.SUBCLASSCD in ('333333','555555','999999') ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  and W1.SUBCLASSCD in ('333333','555555','999999') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  and W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD = '".$model->subclasscd."' ";
            }
        }
        return $query;
    }//getInsertRankQuery

    //科目別の処理の追加クエリー
    function getInsertSubclassQuery($model, $table)
    {
        $query  = "";
        //（共通）追加クエリー
        // T_SCHREG, T_GROUP, T_SCORE, T_SCORE_SUM, T_RANK
        $query .= knjd210wQuery::getInsertCommonQuery($model, $table);

        if ($table == "record_average_chair" || $table == "record_rank_chair") {
            $query .= knjd210wQuery::getChairQuery($model);
        }

        //メイン
        if ($table == "record_average" || $table == "record_average_chair") {
            $query .= knjd210wQuery::getInsertAverageQuery($model, $table);
        } elseif ($table == "record_rank" || $table == "record_rank_chair") {
            //成績平均データ
            $query .= ",T_AVERAGE AS ( ";
            $query .= knjd210wQuery::getRecordAverageQuery($model, $table);
            $query .= "    ) ";

            $query .= knjd210wQuery::getInsertRankQuery($model, $table);
        }
        return $query;
    }

    //講座クエリー
    function getChairQuery($model)
    {
        //教育課程対応
        $fieldCrc = $whereCrc2 = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
            $whereCrc2  = "W2.CLASSCD = W1.CLASSCD AND W2.SCHOOL_KIND = W1.SCHOOL_KIND AND W2.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        }
        $query  = "";
        $query .= ",T_CHAIR AS ( ";
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, W2.CHAIRCD, W2.SCHREGNO ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT W1, ";
        $query .= "    CHAIR_STD_DAT W2 ";
        $query .= "WHERE ";
        $query .= "    W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    W2.YEAR = W1.YEAR AND ";
        $query .= "    W2.SEMESTER = W1.SEMESTER AND ";
        $query .= "    W2.CHAIRCD = W1.CHAIRCD AND ";
        $query .= "    '".str_replace("/", "-", $model->chairdate)."' BETWEEN W2.APPDATE AND W2.APPENDDATE ";
        $query .= "GROUP BY ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, W2.CHAIRCD, W2.SCHREGNO ";
        $query .= ")  ";
        $query .= ",T_RANK_CHAIR AS ( ";
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, W2.CHAIRCD, W1.SCHREGNO, ";
        $query .= "    W1.SCORE, W1.AVG, ";
        $query .= "    W1.GRADE, W1.HR_CLASS, ";
        $query .= "    W1.COURSECD, W1.MAJORCD, W1.COURSECODE, ";
        $query .= "    W1.COURSE, ";
        $query .= "    W1.MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1') {
            $query .= "   ,W1.COURSE_GROUP ";
        }
        $query .= "FROM ";
        $query .= "    T_RANK W1 ";
        $query .= "    INNER JOIN T_CHAIR W2 ON {$whereCrc2}W2.SUBCLASSCD=W1.SUBCLASSCD AND W2.SCHREGNO = W1.SCHREGNO ";
        $query .= ")  ";
        return $query;
    }

    //テーブル存在するか？
    function existsTable($tbname)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = '{$tbname}' ";
        return $query;
    }
}
?>
