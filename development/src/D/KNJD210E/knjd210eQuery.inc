<?php

require_once('for_php7.php');


class knjd210equery extends Query {

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
        $query .= " FROM   RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER = '".$model->seme."' AND ";
        $query .= "        TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        TESTITEMCD = '".$model->item."' ";

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //処理学期
    function GetSemester()
    {
        $query  = " SELECT SEMESTER,SEMESTERNAME,SDATE,EDATE FROM SEMESTER_MST ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ORDER BY SEMESTER ";
        return $query;
    }

    //処理学年
    function GetGrade()
    {
        $query  = " SELECT DISTINCT GRADE AS VALUE, GRADE_NAME1 AS LABEL ";
        $query .= " FROM   SCHREG_REGD_GDAT ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    //処理種別(成績)
    function GetName($seme)
    {
        $query  = "SELECT TESTKINDCD||'-'||TESTITEMCD AS VALUE,TESTITEMNAME AS LABEL ";
        $query .= "FROM   TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' AND SEMESTER = '$seme' ";
        $query .= "ORDER BY TESTKINDCD,TESTITEMCD ";
        return $query;
    }


    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // -- アルファベット順に更新するようにした。--

        // RECORD_AVERAGE_CHAIR_DAT---各講座の合計・最高点・最低点・人数・平均・標準偏差を保持
        knjd210equery::insertSubclassQuery($db, $model, "record_average_chair_dat");
        $db->commit();

        // RECORD_AVERAGE_DAT---各科目の合計・最高点・最低点・人数・平均・標準偏差を保持
        knjd210equery::insertSubclassQuery($db, $model, "record_average_dat");
        $db->commit();

        // RECORD_RANK_CHAIR_DAT---各講座の順位・偏差値を保持
        knjd210equery::insertSubclassQuery($db, $model, "record_rank_chair_dat");
        $db->commit();

        // RECORD_RANK_DAT---各科目の順位・偏差値を保持
        knjd210equery::insertSubclassQuery($db, $model, "record_rank_dat");
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
        $query  = knjd210eQuery::getDeleteQuery($model, $table);
        $db->query($query);
        //追加クエリー
        $query  = knjd210eQuery::getInsertSubclassQuery($model, $table);

        $result = $db->query($query); 
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //項目
            $data = knjd210eQuery::getFieldData($model, $table, $row);
            //追加
            $query = Query::insertSQL($data, $table);
            $db->query($query);
        }
        return;
    }

    //削除クエリー
    function getDeleteQuery($model, $table)
    {
        $query  = "";
        if ($table == "record_average_dat" || $table == "record_average_chair_dat") {
            $query .= "DELETE FROM " .$table ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and GRADE      = '".$model->grade."' ";
        }
        if ($table == "record_rank_dat" || $table == "record_rank_chair_dat") {
            $query .= "DELETE FROM " .$table ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and SCHREGNO   in (SELECT SCHREGNO FROM ( ";
            $query .= knjd210eQuery::getSchregQuery($model);
            $query .= "  ) t1 ) ";
        }
        return $query;
    }

    //項目
    function getFieldData($model, $table, $data2)
    {
        $data = array();
        if ($table == "record_average_dat" || $table == "record_average_chair_dat") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->seme;
            $data["TESTKINDCD"][TEXT]   = $model->kind;
            $data["TESTITEMCD"][TEXT]   = $model->item;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]   = $data2["SUBCLASSCD"];
            if ($table == "record_average_chair_dat") {
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
            if ($table == "record_average_chair_dat") {
                $data["CHAIRDATE"][TEXT]    = str_replace("/", "-", $model->chairdate);
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        }
        if ($table == "record_rank_dat" || $table == "record_rank_chair_dat") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->seme;
            $data["TESTKINDCD"][TEXT]   = $model->kind;
            $data["TESTITEMCD"][TEXT]   = $model->item;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]   = $data2["SUBCLASSCD"];
            if ($table == "record_rank_chair_dat") {
                $data["CHAIRCD"][TEXT]   = $data2["CHAIRCD"];
            }
            $data["SCHREGNO"][TEXT]     = $data2["SCHREGNO"];
            $data["SCORE"][NUMBER]      = $data2["SCORE"];
            $data["AVG"][NUMBER]        = $data2["AVG"];
            $data["GRADE_RANK"][NUMBER]         = $data2["GRADE_RANK"];
            $data["GRADE_AVG_RANK"][NUMBER]     = $data2["GRADE_AVG_RANK"];
            $data["GRADE_DEVIATION"][NUMBER]    = $data2["GRADE_DEVIATION"];
            $data["CLASS_RANK"][NUMBER]         = $data2["CLASS_RANK"];
            $data["CLASS_AVG_RANK"][NUMBER]     = $data2["CLASS_AVG_RANK"];
            $data["CLASS_DEVIATION"][NUMBER]    = $data2["CLASS_DEVIATION"];
            $data["COURSE_RANK"][NUMBER]        = $data2["COURSE_RANK"];
            $data["COURSE_AVG_RANK"][NUMBER]    = $data2["COURSE_AVG_RANK"];
            $data["COURSE_DEVIATION"][NUMBER]   = $data2["COURSE_DEVIATION"];
            if ($table == "record_rank_chair_dat") {
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
        $query .= knjd210eQuery::getSchregQuery($model);
        $query .= "    ) ";
        //名称マスタ
        $query .= ",T_GROUP AS ( ";
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T2.GROUP_DIV, "; //3:３科 5:５科
        $query .= "        T2.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T2.SCHOOL_KIND, ";
            $query .= "    T2.CURRICULUM_CD, ";
        }
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
        //成績
        $query .= ",T_RECORD AS ( ";
        $query .= knjd210eQuery::getRecordQuery($model);
        $query .= "    ) ";
        //成績２
        $query .= ",T_SCORE AS ( ";
        if ($model->kind != "99") {
            //SCORE（後期英語数学は、共通とﾚｯｽﾝ。その他は、素点）
            $query .= knjd210eQuery::getRecordScoreQuery($model);
        } else {
            //VALUE
            $query .= knjd210eQuery::getRecordValueQuery($model);
        }
        $query .= "    ) ";
        if ($table == "record_average_dat" || $table == "record_rank_dat") {
            //SCOREがNULLの学籍番号を取得
            $query .= ",T_SCORE_NULL AS ( ";
            $query .= knjd210eQuery::getRecordNullQuery($model, "9");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULL3 AS ( ";
            $query .= knjd210eQuery::getRecordNullQuery($model, "3");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULL5 AS ( ";
            $query .= knjd210eQuery::getRecordNullQuery($model, "5");
            $query .= "    ) ";
            //各生徒の３科目・５科目・全科目のSCOREの合計を取得
            $query .= ",T_SCORE_SUM AS ( ";
            $query .= knjd210eQuery::getRecordScoreSumQuery($model);
            $query .= "    ) ";
        }
        //在籍&成績２
        $query .= ",T_RANK AS ( ";
        $query .= knjd210eQuery::getRecordSchregQuery($model, $table, "off");
        $query .= "    ) ";
        if ($table == "record_average_dat" || $table == "record_rank_dat") {
            $query .= ",T_RANK_NOT_NULL AS ( ";
            $query .= knjd210eQuery::getRecordSchregQuery($model, $table, "on");
            $query .= "    ) ";
        }

        return $query;
    }//getInsertCommonQuery

    //在籍クエリー
    function getSchregQuery($model)
    {
        $query  = "";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO, ";
        $query .= "        GRADE, ";
        $query .= "        HR_CLASS, ";
        $query .= "        COURSECD,MAJORCD,COURSECODE, ";
        $query .= "        COURSECD||MAJORCD||COURSECODE AS COURSE ";
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER = '".$model->seme_sch."' AND ";
        $query .= "        GRADE = '".$model->grade."' ";
        return $query;
    }

    //成績クエリー
    function getRecordQuery($model)
    {
        $grade = (int) $model->grade;

        $query  = "";
        $query .= "    SELECT ";
        $query .= "        SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        SCHREGNO, ";
        $query .= "        SCORE, ";
        $query .= "        VALUE ";
        $query .= "    FROM ";
        $query .= "        RECORD_SCORE_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER = '".$model->seme."' AND ";
        $query .= "        TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        TESTITEMCD = '".$model->item."' AND ";
        $query .= "        SUBSTR(SUBCLASSCD, 1, 2) not in ('90') ";
        if (4 <= $grade && $grade <= 6) { //智辯高校
            $query .= "    UNION ";
            $query .= "    SELECT ";
            $query .= "        SCORE_DIV, ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    max(SCHOOL_KIND) as SCHOOL_KIND, ";
                $query .= "    '99' as CURRICULUM_CD, ";
            }
            $query .= "        SUBSTR(SUBCLASSCD, 1, 2) || '0000' AS SUBCLASSCD, ";
            $query .= "        SCHREGNO, ";
            $query .= "        smallint(round(avg(float(SCORE)),0)) as SCORE, ";
            $query .= "        smallint(round(avg(float(VALUE)),0)) as VALUE ";
            $query .= "    FROM ";
            $query .= "        RECORD_SCORE_DAT T1 ";
            $query .= "    WHERE ";
            $query .= "        YEAR = '".CTRL_YEAR."' AND ";
            $query .= "        SEMESTER = '".$model->seme."' AND ";
            $query .= "        TESTKINDCD = '".$model->kind."' AND ";
            $query .= "        TESTITEMCD = '".$model->item."' AND ";
            $query .= "        SUBSTR(SUBCLASSCD, 1, 2) not in ('90') AND ";
            $query .= "        EXISTS( ";
            $query .= "            SELECT ";
            $query .= "                'X' ";
            $query .= "            FROM ";
            $query .= "                T_GROUP W1 ";
            $query .= "            WHERE ";
            $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "            ) ";
            $query .= "    GROUP BY ";
            $query .= "        SCORE_DIV, ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
            }
            $query .= "        SUBSTR(SUBCLASSCD, 1, 2), ";
            $query .= "        SCHREGNO ";
            //社会（地歴と公民）
            $query .= "    UNION ";
            $query .= "    SELECT ";
            $query .= "        SCORE_DIV, ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    '98' AS CLASSCD, ";
                $query .= "    max(SCHOOL_KIND) as SCHOOL_KIND, ";
                $query .= "    '99' as CURRICULUM_CD, ";
            }
            $query .= "        '98' || '0000' AS SUBCLASSCD, ";
            $query .= "        SCHREGNO, ";
            $query .= "        smallint(round(avg(float(SCORE)),0)) as SCORE, ";
            $query .= "        smallint(round(avg(float(VALUE)),0)) as VALUE ";
            $query .= "    FROM ";
            $query .= "        RECORD_SCORE_DAT T1 ";
            $query .= "    WHERE ";
            $query .= "        YEAR = '".CTRL_YEAR."' AND ";
            $query .= "        SEMESTER = '".$model->seme."' AND ";
            $query .= "        TESTKINDCD = '".$model->kind."' AND ";
            $query .= "        TESTITEMCD = '".$model->item."' AND ";
            $query .= "        SUBSTR(SUBCLASSCD, 1, 2) in ('32','33') AND ";
            $query .= "        EXISTS( ";
            $query .= "            SELECT ";
            $query .= "                'X' ";
            $query .= "            FROM ";
            $query .= "                T_GROUP W1 ";
            $query .= "            WHERE ";
            $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "            ) ";
            $query .= "    GROUP BY ";
            $query .= "        SCORE_DIV, ";
            $query .= "        SCHREGNO ";
        }
        return $query;
    }

    //成績１つにまとめる（後期英語数学は、共通とﾚｯｽﾝ。その他は、素点）クエリー
    function getRecordScoreQuery($model)
    {
        $query  = "";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        SCHREGNO, ";
        $query .= "        SCORE ";
        $query .= "    FROM ";
        $query .= "        T_RECORD ";
        $query .= "    WHERE ";
        $query .= "        SCORE_DIV = '01' AND ";
        $query .= "        SUBCLASSCD not in ( ";
        $query .= "            SELECT ";
        $query .= "                NAME1 ";
        $query .= "            FROM ";
        $query .= "                V_NAME_MST ";
        $query .= "            WHERE ";
        $query .= "                YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                NAMECD1 = 'D002' ";
        $query .= "            ) ";
        $query .= "    UNION ALL  ";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        SCHREGNO, ";
        $query .= "        sum(SCORE) as SCORE ";
        $query .= "    FROM ";
        $query .= "        T_RECORD ";
        $query .= "    WHERE ";
        $query .= "        SCORE_DIV in ('01','02') AND ";
        $query .= "        SUBCLASSCD in ( ";
        $query .= "            SELECT ";
        $query .= "                NAME1 ";
        $query .= "            FROM ";
        $query .= "                V_NAME_MST ";
        $query .= "            WHERE ";
        $query .= "                YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                NAMECD1 = 'D002' ";
        $query .= "            ) ";
        $query .= "    GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        SCHREGNO ";
        return $query;
    }

    //評定クエリー
    function getRecordValueQuery($model)
    {
        $query  = "";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        SCHREGNO, ";
        if ($model->seme == "9") {
                $query .= "SCORE ";
        } else {
            $query .= "    VALUE as SCORE ";
        }
        $query .= "    FROM ";
        $query .= "        T_RECORD ";
        $query .= "    WHERE ";
        $query .= "        SCORE_DIV = '00' ";
        return $query;
    }

    //SCOREがNULLの学籍番号を取得するクエリー
    function getRecordNullQuery($model, $div)
    {
        $query  = "";
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        T1.SCORE IS NULL ";
        //選択科目を除く
        if ($model->electdiv == "1") {
            $query .= " AND NOT EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            SUBCLASS_MST W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.ELECTDIV = '1' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        }
        //名称マスタ「D065」科目を除く
        $query .= " AND NOT EXISTS( ";
        $query .= "        SELECT ";
        $query .= "            'X' ";
        $query .= "        FROM ";
        $query .= "            V_NAME_MST W1 ";
        $query .= "        WHERE ";
        $query .= "            W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "            W1.NAMECD1 = 'D065' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        } else {
            $query .= "            W1.NAME1 = T1.SUBCLASSCD ";
        }
        $query .= "        ) ";
        if ($div != "9") { //智辯
            //３科目・５科目
            $query .= " AND EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            T_GROUP W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.GROUP_DIV = '".$div."' AND ";
            $query .= "            W1.SCHREGNO = T1.SCHREGNO AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        return $query;
    }

    //各生徒の３科目・５科目・全科目のSCOREの合計を取得するクエリー
    function getRecordScoreSumQuery($model)
    {
        $grade = (int) $model->grade;

        $query  = "";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '33' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '333333' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'X' ";
        $query .= "            FROM ";
        $query .= "                T_GROUP W1 ";
        $query .= "            WHERE ";
        $query .= "                W1.GROUP_DIV = '3' AND ";
        $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
            $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "            ) AND ";
        $query .= "        T1.SCORE IS NOT NULL ";
        $query .= "    GROUP BY T1.SCHREGNO ";
        $query .= "    UNION ALL ";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '55' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '555555' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'X' ";
        $query .= "            FROM ";
        $query .= "                T_GROUP W1 ";
        $query .= "            WHERE ";
        $query .= "                W1.GROUP_DIV = '5' AND ";
        $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
            $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "            ) AND ";
        $query .= "        T1.SCORE IS NOT NULL ";
        $query .= "    GROUP BY T1.SCHREGNO ";
        $query .= "    UNION ALL ";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '99' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '999999' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        if (4 <= $grade && $grade <= 6) { //智辯高校
            $query .= "    SUBSTR(T1.SUBCLASSCD, 3) != '0000' AND ";
        } else {
            $query .= "    ( ";
            $query .= "    EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            T_GROUP W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.SCHREGNO = T1.SCHREGNO AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) OR ( ";
            $query .= "    EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            T_GROUP W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.SCHREGNO = T1.SCHREGNO ";
            $query .= "        ) AND ";
            $query .= "    NOT EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            T_GROUP W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "            W1.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ";
            $query .= "        )) ";
            $query .= "    ) AND ";
        }
        $query .= "        T1.SCORE IS NOT NULL ";
        //選択科目を除く
        if ($model->electdiv == "1") {
            $query .= " AND NOT EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            SUBCLASS_MST W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.ELECTDIV = '1' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        }
        //名称マスタ「D065」科目を除く
        $query .= " AND NOT EXISTS( ";
        $query .= "        SELECT ";
        $query .= "            'X' ";
        $query .= "        FROM ";
        $query .= "            V_NAME_MST W1 ";
        $query .= "        WHERE ";
        $query .= "            W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "            W1.NAMECD1 = 'D065' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        } else {
            $query .= "            W1.NAME1 = T1.SUBCLASSCD ";
        }
        $query .= "        ) ";
        $query .= "    GROUP BY T1.SCHREGNO ";
        return $query;
    }

    //成績平均データクエリー
    function getRecordAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_rank_chair_dat") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_rank_chair_dat") ? "RECORD_AVERAGE_CHAIR_DAT" : "RECORD_AVERAGE_DAT";
        $query  = "";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "        AVG_DIV, ";
        $query .= "        GRADE, ";
        $query .= "        HR_CLASS, ";
        $query .= "        COURSECD||MAJORCD||COURSECODE AS COURSE, ";
        $query .= "        DECIMAL(ROUND(FLOAT(AVG)*10,0)/10,5,1) AS AVG,STDDEV ";
        $query .= "    FROM ";
        $query .= $table_name;
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER = '".$model->seme."' AND ";
        $query .= "        TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        TESTITEMCD = '".$model->item."' AND ";
        $query .= "        GRADE = '".$model->grade."' ";
        return $query;
    }

    //在籍&成績２クエリー
    function getRecordSchregQuery($model, $table, $div)
    {
        $query  = "";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    W1.CLASSCD, ";
            $query .= "    W1.SCHOOL_KIND, ";
            $query .= "    W1.CURRICULUM_CD, ";
        }
        $query .= "        W1.SUBCLASSCD, ";
        $query .= "        W1.SCHREGNO, ";
        $query .= "        SCORE, ";
        $query .= "        cast(null as decimal) as AVG, ";
        $query .= "        GRADE, ";
        $query .= "        HR_CLASS, ";
        $query .= "        COURSECD,MAJORCD,COURSECODE, ";
        $query .= "        COURSE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE W1, ";
        $query .= "        T_SCHREG W2 ";
        $query .= "    WHERE ";
        $query .= "        W1.SCHREGNO = W2.SCHREGNO AND ";
        $query .= "        SCORE IS NOT NULL ";
        if ($table == "record_average_dat" || $table == "record_rank_dat") {
            $query .= "    UNION ALL ";
            $query .= "    SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    W1.CLASSCD, ";
                $query .= "    W1.SCHOOL_KIND, ";
                $query .= "    W1.CURRICULUM_CD, ";
            }
            $query .= "        W1.SUBCLASSCD, ";
            $query .= "        W1.SCHREGNO, ";
            $query .= "        SCORE, ";
            $query .= "        AVG, ";
            $query .= "        GRADE, ";
            $query .= "        HR_CLASS, ";
            $query .= "        COURSECD,MAJORCD,COURSECODE, ";
            $query .= "        COURSE ";
            $query .= "    FROM ";
            $query .= "        T_SCORE_SUM W1, ";
            $query .= "        T_SCHREG W2 ";
            $query .= "    WHERE ";
            $query .= "        W1.SCHREGNO = W2.SCHREGNO ";
            if ($div == "on") {
                $query .= " AND ( ";
                $query .= "     (W1.SUBCLASSCD  = '333333' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL3 W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
                $query .= "  OR (W1.SUBCLASSCD  = '555555' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL5 W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
                $query .= "  OR (W1.SUBCLASSCD  = '999999' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
                $query .= "     ) ";
            }
        }
        return $query;
    }

    //成績平均データの追加クエリー
    function getInsertAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_average_chair_dat") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_average_chair_dat") ? "_CHAIR" : "";
        $query  = "";
        //学年
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
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
        if ($table == "record_average_dat") {
            $query .= "    T_RANK_NOT_NULL ";
        } else {
            $query .= "    T_RANK" .$table_name ." ";
        }
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE ";
        //クラス
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
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
        if ($table == "record_average_dat") {
            $query .= "    T_RANK_NOT_NULL ";
        } else {
            $query .= "    T_RANK" .$table_name ." ";
        }
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS ";
        //コース
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
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
        if ($table == "record_average_dat") {
            $query .= "    T_RANK_NOT_NULL ";
        } else {
            $query .= "    T_RANK" .$table_name ." ";
        }
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE ";
        return $query;
    }//getInsertAverageQuery

    //成績席次データの追加クエリー
    function getInsertRankQuery($model, $table)
    {
        $field_name  = $table_name  = $where_name2 = $where_name3 = $where_name4 = "";
        if ($table == "record_rank_chair_dat") {
            $field_name  = "W1.CHAIRCD,";
            $table_name  = "_CHAIR";
            $where_name2 = "W2.CHAIRCD = W1.CHAIRCD AND ";
            $where_name3 = "W3.CHAIRCD = W1.CHAIRCD AND ";
            $where_name4 = "W4.CHAIRCD = W1.CHAIRCD AND ";
        }
        //教育課程対応
        $fieldCrc = $whereCrc2 = $whereCrc3 = $whereCrc4 = $whereCrc5 = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
            $whereCrc2  = "W2.CLASSCD = W1.CLASSCD AND W2.SCHOOL_KIND = W1.SCHOOL_KIND AND W2.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc3  = "W3.CLASSCD = W1.CLASSCD AND W3.SCHOOL_KIND = W1.SCHOOL_KIND AND W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc4  = "W4.CLASSCD = W1.CLASSCD AND W4.SCHOOL_KIND = W1.SCHOOL_KIND AND W4.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc5  = "W5.CLASSCD = W1.CLASSCD AND W5.SCHOOL_KIND = W1.SCHOOL_KIND AND W5.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        }
        $query  = "";
        //各科目
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    W1.SCHREGNO, ";
        $query .= "    W1.SCORE, ";
        $query .= "    W1.AVG, ";
        if ($table == "record_rank_dat") {
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY value(W5.SCORE,-1) DESC) end AS GRADE_RANK, ";
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY value(W5.SCORE,-1) DESC) end AS CLASS_RANK, ";
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY value(W5.SCORE,-1) DESC) end AS COURSE_RANK, ";
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY value(W5.SCORE,-1) DESC) end AS GRADE_AVG_RANK, ";
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY value(W5.SCORE,-1) DESC) end AS CLASS_AVG_RANK, ";
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY value(W5.SCORE,-1) DESC) end AS COURSE_AVG_RANK, ";
            $query .= "    case when W5.SCORE is not null and 0 < W2.STDDEV then DECIMAL(ROUND((10*(W5.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "    case when W5.SCORE is not null and 0 < W3.STDDEV then DECIMAL(ROUND((10*(W5.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "    case when W5.SCORE is not null and 0 < W4.STDDEV then DECIMAL(ROUND((10*(W5.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION ";
        } else {
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_AVG_RANK, ";
            $query .= "    case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "    case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "    case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION ";
        }
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." W1 ";
        $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
        $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
        $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
        if ($table == "record_rank_dat") {
            $query .= "LEFT JOIN T_RANK_NOT_NULL W5 ON {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND W5.SCHREGNO = W1.SCHREGNO ";
        }
        $query .= "WHERE ";
        $query .= "    W1.SUBCLASSCD not in ('333333','555555','999999') ";
        //３科・５科・全科
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    W1.SCHREGNO, ";
        $query .= "    W1.SCORE, ";
        $query .= "    W1.AVG, ";
        if ($table == "record_rank_dat") {
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY value(W5.SCORE,-1) DESC) end AS GRADE_RANK, ";
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY value(W5.SCORE,-1) DESC) end AS CLASS_RANK, ";
            $query .= "    case when W5.SCORE is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY value(W5.SCORE,-1) DESC) end AS COURSE_RANK, ";
            $query .= "    case when W5.AVG is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY value(W5.AVG,-1) DESC) end AS GRADE_AVG_RANK, ";
            $query .= "    case when W5.AVG is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY value(W5.AVG,-1) DESC) end AS CLASS_AVG_RANK, ";
            $query .= "    case when W5.AVG is not null then RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY value(W5.AVG,-1) DESC) end AS COURSE_AVG_RANK, ";
            $query .= "    case when W5.AVG is not null and 0 < W2.STDDEV then DECIMAL(ROUND((10*(W5.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "    case when W5.AVG is not null and 0 < W3.STDDEV then DECIMAL(ROUND((10*(W5.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "    case when W5.AVG is not null and 0 < W4.STDDEV then DECIMAL(ROUND((10*(W5.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION ";
        } else {
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.AVG DESC) AS GRADE_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.AVG DESC) AS CLASS_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.AVG DESC) AS COURSE_AVG_RANK, ";
            $query .= "    case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "    case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "    case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION ";
        }
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." W1 ";
        $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
        $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
        $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
        if ($table == "record_rank_dat") {
            $query .= "LEFT JOIN T_RANK_NOT_NULL W5 ON {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND W5.SCHREGNO = W1.SCHREGNO ";
        }
        $query .= "WHERE ";
        $query .= "    W1.SUBCLASSCD in ('333333','555555','999999') ";
        return $query;
    }//getInsertRankQuery

    //科目別の処理の追加クエリー
    function getInsertSubclassQuery($model, $table)
    {
        $query  = "";
        //（共通）追加クエリー
        $query .= knjd210eQuery::getInsertCommonQuery($model, $table);

        if ($table == "record_average_chair_dat" || $table == "record_rank_chair_dat") {
            $query .= knjd210eQuery::getChairQuery($model);
        }

        //メイン
        if ($table == "record_average_dat" || $table == "record_average_chair_dat") {
            $query .= knjd210eQuery::getInsertAverageQuery($model, $table);

        } else if ($table == "record_rank_dat" || $table == "record_rank_chair_dat") {
            //成績平均データ
            $query .= ",T_AVERAGE AS ( ";
            $query .= knjd210eQuery::getRecordAverageQuery($model, $table);
            $query .= "    ) ";

            $query .= knjd210eQuery::getInsertRankQuery($model, $table);
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
        $query .= "    W1.COURSE ";
        $query .= "FROM ";
        $query .= "    T_RANK W1 ";
        $query .= "    INNER JOIN T_CHAIR W2 ON {$whereCrc2}W2.SUBCLASSCD=W1.SUBCLASSCD AND W2.SCHREGNO = W1.SCHREGNO ";
        $query .= ")  ";
        return $query;
    }

}
?>