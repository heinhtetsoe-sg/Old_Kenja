<?php

require_once('for_php7.php');


class knjd210pquery extends Query {

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
    function GetGrade($seme)
    {
        $query  = " SELECT DISTINCT GRADE FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' AND SEMESTER = '$seme' ";
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
        // knjd210pquery::insertSubclassQuery($db, $model, "record_average_chair");
        // $db->commit();

        // RECORD_AVERAGE_DAT---各科目の合計・最高点・最低点・人数・平均・標準偏差を保持
        knjd210pquery::insertSubclassQuery($db, $model, "record_average");
        $db->commit();

        // RECORD_RANK_CHAIR_DAT---各講座の順位・偏差値を保持
        // knjd210pquery::insertSubclassQuery($db, $model, "record_rank_chair");
        // $db->commit();

        // RECORD_RANK_DAT---各科目の順位・偏差値を保持
        knjd210pquery::insertSubclassQuery($db, $model, "record_rank");
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
        $query  = knjd210pQuery::getDeleteQuery($model, $table);
        $db->query($query);
        //追加クエリー
        $query  = knjd210pQuery::getInsertSubclassQuery($model, $table);
        $result = $db->query($query); 
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //項目
            $data = knjd210pQuery::getFieldData($model, $table, $row);
            //追加
            $query = Query::insertSQL($data, $table .$model->tableStr);
            $db->query($query);
        }
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
            $query .= "  and GRADE      = '".$model->grade."' ";
        }
        if ($table == "record_rank" || $table == "record_rank_chair") {
            $query .= "DELETE FROM " .$table .$model->tableStr ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and SCHREGNO   in (SELECT SCHREGNO FROM ( ";
            $query .= knjd210pQuery::getSchregQuery($model);
            $query .= "  ) t1 ) ";
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
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            }
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
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]   = $data2["SUBCLASSCD"];
            if ($table == "record_rank_chair") {
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
            $data["MAJOR_RANK"][NUMBER]         = $data2["MAJOR_RANK"];
            $data["MAJOR_AVG_RANK"][NUMBER]     = $data2["MAJOR_AVG_RANK"];
            $data["MAJOR_DEVIATION"][NUMBER]    = $data2["MAJOR_DEVIATION"];
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
        $query .= knjd210pQuery::getSchregQuery($model);
        $query .= "    ) ";
        //３科・５科のグループデータ
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
        //講座グループ
        $query .= knjd210pQuery::getChairGroup($model);
        //成績
        $query .= ",T_SCORE AS ( ";
        $query .= knjd210pQuery::getRecordQuery($model);
        $query .= "    ) ";
        if ($table == "record_average" || $table == "record_rank") {
            //SCOREがNULLの学籍番号を取得
            $query .= ",T_SCORE_NULL AS ( ";
            $query .= knjd210pQuery::getRecordNullQuery($model, "9");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULL3 AS ( ";
            $query .= knjd210pQuery::getRecordNullQuery($model, "3");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULL5 AS ( ";
            $query .= knjd210pQuery::getRecordNullQuery($model, "5");
            $query .= "    ) ";
            //各生徒の３科目・５科目・全科目のSCOREの合計を取得
            $query .= ",T_SCORE_SUM AS ( ";
            $query .= knjd210pQuery::getRecordScoreSumQuery($model);
            $query .= "    ) ";
        }
        //在籍&成績２
        $query .= ",T_RANK AS ( ";
        $query .= knjd210pQuery::getRecordSchregQuery($model, $table);
        $query .= "    ) ";

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
        //1:素点 2:評価
        $scoreStr = ($model->kind == "99" || $model->item == "00") ? "W1.VALUE" : "W1.SCORE";
        $scoreDiv = ($model->kind != "99") ? "01" : "00";

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
        $query .= "        ".$scoreStr." as SCORE ";
        //TODO1の箇所で使用
        $query .= "       ,W1.SCORE as SCORE_NULL ";
        $query .= "       ,W1.VALUE as VALUE_NULL ";
        //TODO2の箇所で使用
        if ($model->seme == "9") {
            $query .= "   ,W1.COMP_CREDIT ";
            $query .= "   ,W1.GET_CREDIT ";
        }
        $query .= "       ,W2.GRADE,W2.COURSECD,W2.MAJORCD,W2.COURSECODE "; //単位マスタ参照するため追加
        $query .= "       ,VALUE(CG.CHAIR_GROUP_CD, '000') AS CHAIR_GROUP_CD "; //MIN講座グループ（下３桁）
        $query .= "    FROM ";
        $query .= "        RECORD_SCORE_DAT W1 ";
        $query .= "        LEFT JOIN T_SCHREG W2 ON W2.SCHREGNO = W1.SCHREGNO ";
        $query .= "        LEFT JOIN T_CHAIR_GROUP CG ON ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CG.CLASSCD = W1.CLASSCD AND  ";
            $query .= "    CG.SCHOOL_KIND = W1.SCHOOL_KIND AND  ";
            $query .= "    CG.CURRICULUM_CD = W1.CURRICULUM_CD AND  ";
        }
        $query .= "        CG.SUBCLASSCD = W1.SUBCLASSCD AND CG.SCHREGNO = W1.SCHREGNO ";
        $query .= "    WHERE ";
        $query .= "        W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        W1.SEMESTER = '".$model->seme."' AND ";
        $query .= "        W1.TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        W1.TESTITEMCD = '".$model->item."' AND ";
        $query .= "        W1.SCORE_DIV = '{$scoreDiv}' AND ";
        $query .= "        EXISTS(SELECT 'X' FROM T_SCHREG W2 WHERE W2.SCHREGNO = W1.SCHREGNO) ";
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
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        //全科目
        } else {
            //教科科目名称マスタのマスタ化
            if ($model->Properties["useClassDetailDat"] == '1') {
                $query .= " AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
            } else {
                //名称マスタ「D017」NAME1に登録されている科目を除く。
                $query .= " AND T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
            }
            //教科９０以上は除く。つまり、教科９０未満が対象
            $query .= " AND T1.SUBCLASSCD not like '9%' ";
            //「学年末」の場合、履修単位・修得単位の両方 NULL が対象・・・TODO2
            if ($model->seme == "9") {
                $query .= " AND T1.COMP_CREDIT IS NULL ";
                $query .= " AND T1.GET_CREDIT IS NULL ";
            }
            //素点・評価の両方 NULL が対象 TODO1
            $query .= " AND T1.SCORE_NULL IS NULL ";
            $query .= " AND T1.VALUE_NULL IS NULL ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        return $query;
    }

    //各生徒の３科目・５科目・全科目のSCOREの合計を取得するクエリー
    function getRecordScoreSumQuery($model)
    {
        $query  = "";
        //３科（ALL3）
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
        //５科（ALL5）
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
        //共通科目（ALL8）
        //★共通科目とは    
        //  1)科目マスタの、選択（ELECTDIV）’1’以外
        //    且つ
        //  2)単位マスタの、必履修区分（REQUIRE_FLG）が「3:選択」以外
        //    且つ
        //  3)講座グループデータ（CHAIR_GROUP_DAT）設定している科目は除く
        $query .= "    UNION ALL ";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '88' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '888888' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NULL THEN 1 ELSE 0 END) AS SCORE_CNT, "; //SCOREがNULLをカウント
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        //$query .= "        T1.SCORE IS NOT NULL ";
        //1)選択科目以外
        $query .= "        NOT EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'X' ";
        $query .= "            FROM ";
        $query .= "                SUBCLASS_MST W1 ";
        $query .= "            WHERE ";
        $query .= "                W1.ELECTDIV = '1' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
            $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "            ) ";
        //2)単位マスタの必履修区分（REQUIRE_FLG）が「3:選択」以外
        $query .= "        AND NOT EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'X' ";
        $query .= "            FROM ";
        $query .= "                CREDIT_MST W1 ";
        $query .= "            WHERE ";
        $query .= "                W1.REQUIRE_FLG = '3' AND ";
        $query .= "                W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                W1.GRADE = T1.GRADE AND ";
        $query .= "                W1.COURSECD = T1.COURSECD AND ";
        $query .= "                W1.MAJORCD = T1.MAJORCD AND ";
        $query .= "                W1.COURSECODE = T1.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
            $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "            ) ";
        //3)講座グループ設定科目以外
        $query .= "        AND T1.CHAIR_GROUP_CD = '000' "; //000:未設定
        $query .= "    GROUP BY T1.SCHREGNO ";
        //全科（ALL9）
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
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
        } else {
            //名称マスタ「D017」NAME1に登録されている科目を除く。
            $query .= "     AND T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        return $query;
    }

    //成績平均データクエリー
    function getRecordAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_rank_chair") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_rank_chair") ? "record_average_chair" : "record_average";
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
        $query .= "        MAJORCD AS CHAIR_GROUP_CD, ";
        $query .= "        DECIMAL(ROUND(FLOAT(AVG)*10,0)/10,5,1) AS AVG,STDDEV ";
        $query .= "    FROM ";
        $query .= $table_name .$model->tableStr;
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER = '".$model->seme."' AND ";
        $query .= "        TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        TESTITEMCD = '".$model->item."' AND ";
        $query .= "        GRADE = '".$model->grade."' ";
        return $query;
    }

    //在籍&成績２クエリー
    function getRecordSchregQuery($model, $table)
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
        $query .= "        W1.SCORE, ";
        $query .= "        cast(null as decimal) as AVG, ";
        $query .= "        W2.GRADE, ";
        $query .= "        W2.HR_CLASS, ";
        $query .= "        W2.COURSECD,W2.MAJORCD,W2.COURSECODE, ";
        $query .= "        W2.COURSE, ";
        $query .= "        W1.CHAIR_GROUP_CD ";
        $query .= "    FROM ";
        $query .= "        T_SCORE W1, ";
        $query .= "        T_SCHREG W2 ";
        $query .= "    WHERE ";
        $query .= "        W1.SCHREGNO = W2.SCHREGNO AND ";
        $query .= "        W1.SCORE IS NOT NULL ";
        if ($table == "record_average" || $table == "record_rank") {
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
            $query .= "        W1.SCORE, ";
            $query .= "        W1.AVG, ";
            $query .= "        W2.GRADE, ";
            $query .= "        W2.HR_CLASS, ";
            $query .= "        W2.COURSECD,W2.MAJORCD,W2.COURSECODE, ";
            $query .= "        W2.COURSE, ";
            $query .= "        '000' AS CHAIR_GROUP_CD ";
            $query .= "    FROM ";
            $query .= "        T_SCORE_SUM W1, ";
            $query .= "        T_SCHREG W2 ";
            $query .= "    WHERE ";
            $query .= "        W1.SCHREGNO = W2.SCHREGNO ";
            $query .= "     AND ( ";
            $query .= "         (W1.SUBCLASSCD  = '333333' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL3 W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "      OR (W1.SUBCLASSCD  = '555555' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL5 W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "      OR (W1.SUBCLASSCD  = '999999' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "      OR (W1.SUBCLASSCD  = '888888' AND SCORE_CNT = 0) ";
            $query .= "         ) ";
        }
        return $query;
    }

    //成績平均データの追加クエリー
    function getInsertAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_average_chair") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_average_chair") ? "_CHAIR" : "";
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
        $query .= "    T_RANK" .$table_name ." ";
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
        $query .= "    T_RANK" .$table_name ." ";
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
        $query .= "    T_RANK" .$table_name ." ";
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
        //講座グループ
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
        $query .= "    '6' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    '0' as COURSECD, ";
        $query .= "    CHAIR_GROUP_CD as MAJORCD, ";
        $query .= "    '0000' as COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
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
        $query .= "    CHAIR_GROUP_CD ";
        return $query;
    }//getInsertAverageQuery

    //成績席次データの追加クエリー
    function getInsertRankQuery($model, $table)
    {
        $field_name  = $table_name  = $where_name2 = $where_name3 = $where_name4 = "";
        if ($table == "record_rank_chair") {
            $field_name  = "W1.CHAIRCD,";
            $table_name  = "_CHAIR";
            $where_name2 = "W2.CHAIRCD = W1.CHAIRCD AND ";
            $where_name3 = "W3.CHAIRCD = W1.CHAIRCD AND ";
            $where_name4 = "W4.CHAIRCD = W1.CHAIRCD AND ";
            $where_name6 = "W6.CHAIRCD = W1.CHAIRCD AND ";
        }
        //教育課程対応
        $fieldCrc = $whereCrc2 = $whereCrc3 = $whereCrc4 = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
            $whereCrc2  = "W2.CLASSCD = W1.CLASSCD AND W2.SCHOOL_KIND = W1.SCHOOL_KIND AND W2.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc3  = "W3.CLASSCD = W1.CLASSCD AND W3.SCHOOL_KIND = W1.SCHOOL_KIND AND W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc4  = "W4.CLASSCD = W1.CLASSCD AND W4.SCHOOL_KIND = W1.SCHOOL_KIND AND W4.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc6  = "W6.CLASSCD = W1.CLASSCD AND W6.SCHOOL_KIND = W1.SCHOOL_KIND AND W6.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        }
        $query  = "";
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
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.SCORE DESC) AS MAJOR_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.SCORE DESC) AS MAJOR_AVG_RANK, ";
        $query .= "    case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
        $query .= "    case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
        $query .= "    case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
        $query .= "    case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W6.AVG)/W6.STDDEV+50)*10,0)/10,5,1) end AS MAJOR_DEVIATION ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." W1 ";
        $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
        $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
        $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
        $query .= "    LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '6' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
        $query .= "WHERE ";
        $query .= "    W1.SUBCLASSCD not in ('333333','555555','999999','888888') ";
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
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.SCORE DESC) AS MAJOR_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.AVG DESC) AS GRADE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.AVG DESC) AS CLASS_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.AVG DESC) AS COURSE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.AVG DESC) AS MAJOR_AVG_RANK, ";
        $query .= "    case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
        $query .= "    case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
        $query .= "    case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
        $query .= "    case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W6.AVG)/W6.STDDEV+50)*10,0)/10,5,1) end AS MAJOR_DEVIATION ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." W1 ";
        $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
        $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
        $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
        $query .= "    LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '6' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
        $query .= "WHERE ";
        $query .= "    W1.SUBCLASSCD in ('333333','555555','999999','888888') ";
        return $query;
    }//getInsertRankQuery

    //科目別の処理の追加クエリー
    function getInsertSubclassQuery($model, $table)
    {
        $query  = "";
        //（共通）追加クエリー
        $query .= knjd210pQuery::getInsertCommonQuery($model, $table);

        if ($table == "record_average_chair" || $table == "record_rank_chair") {
            $query .= knjd210pQuery::getChairQuery($model);
        }

        //メイン
        if ($table == "record_average" || $table == "record_average_chair") {
            $query .= knjd210pQuery::getInsertAverageQuery($model, $table);

        } else if ($table == "record_rank" || $table == "record_rank_chair") {
            //成績平均データ
            $query .= ",T_AVERAGE AS ( ";
            $query .= knjd210pQuery::getRecordAverageQuery($model, $table);
            $query .= "    ) ";

            $query .= knjd210pQuery::getInsertRankQuery($model, $table);
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
        $query .= "    W1.COURSE, W1.CHAIR_GROUP_CD ";
        $query .= "FROM ";
        $query .= "    T_RANK W1 ";
        $query .= "    INNER JOIN T_CHAIR W2 ON {$whereCrc2}W2.SUBCLASSCD=W1.SUBCLASSCD AND W2.SCHREGNO = W1.SCHREGNO ";
        $query .= ")  ";
        return $query;
    }

    //講座グループ
    function getChairGroup($model)
    {
        //教育課程対応
        $fieldCrc = $whereCrc2 = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
            $whereCrc2  = "W3.CLASSCD = W1.CLASSCD AND W3.SCHOOL_KIND = W1.SCHOOL_KIND AND W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        }
        $query  = "";
        //講座グループ設定
        $query .= ",CHAIR_GROUP AS ( ";
        $query .= " SELECT ";
        $query .= "     SUBSTR(W1.CHAIR_GROUP_CD, LENGTH(W1.CHAIR_GROUP_CD)-2) AS CHAIR_GROUP_CD, "; //下３桁
        $query .= "     {$fieldCrc} W1.SUBCLASSCD, ";
        $query .= "     W2.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_GROUP_MST W1, ";
        $query .= "     CHAIR_GROUP_DAT W2 ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND W1.SEMESTER = '".CTRL_SEMESTER."' "; //ログイン学期
        $query .= "     AND W2.YEAR = W1.YEAR ";
        $query .= "     AND W2.SEMESTER = W1.SEMESTER ";
        $query .= "     AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
        $query .= "     AND W2.TESTKINDCD || W2.TESTITEMCD = '0000' "; //固定:0000
        $query .= ")  ";
        //講座名簿とリンクし、生徒別科目別の講座グループを取得
        $query .= ",T_CHAIR_GROUP AS ( ";
        $query .= "SELECT ";
        $query .= "    {$fieldCrc} W1.SUBCLASSCD, W2.SCHREGNO, ";
        $query .= "    MIN(W3.CHAIR_GROUP_CD) AS CHAIR_GROUP_CD "; //MIN講座グループ
        $query .= "FROM ";
        $query .= "    CHAIR_DAT W1, ";
        $query .= "    CHAIR_STD_DAT W2, ";
        $query .= "    CHAIR_GROUP W3 ";
        $query .= "WHERE ";
        $query .= "    W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    W2.YEAR = W1.YEAR AND ";
        $query .= "    W2.SEMESTER = W1.SEMESTER AND ";
        $query .= "    W2.CHAIRCD = W1.CHAIRCD AND ";
        $query .= "    '".str_replace("/", "-", $model->chairdate)."' BETWEEN W2.APPDATE AND W2.APPENDDATE AND ";
        $query .= "    {$whereCrc2} W3.SUBCLASSCD = W1.SUBCLASSCD AND W3.CHAIRCD = W2.CHAIRCD ";
        $query .= "GROUP BY ";
        $query .= "    {$fieldCrc} W1.SUBCLASSCD, W2.SCHREGNO ";
        $query .= ")  ";
        return $query;
    }

}
?>