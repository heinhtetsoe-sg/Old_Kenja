<?php

require_once('for_php7.php');

class knjd183dQuery extends Query
{
    //学期取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER <> '9'";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタデータ取得(名称で取得。コードは取得しない)
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE,";
        $query .= "     NAME1 || ':' || NAME2 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //観点マスタ
    public function getBehaviorSemesMst($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE AS VALUE, ";
        $query .= "     CODENAME AS DETAIL, ";
        $query .= "     VIEWNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= " AND GRADE = '{$model->exp_grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒の記録
    public function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    // HREPORTREMARK_DAT取得
    public function getHreportremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    // HREPORTREMARK_DETAIL_DAT取得
    public function getHreportremarkDetailDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND";
        $query .= "     DIV         = '01'AND";
        $query .= "     CODE        = '01'";

        return $query;
    }

    //学籍資格データよりデータを取得
    public function getAward($model)
    {
        if ($model->Properties["useQualifiedMst"] == '1') {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
            $query .= "     L1.QUALIFIED_ABBV, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            if ($model->Properties["useSchregQualified"] == 'SUBCLASS_QUALIFIED_TEST_DAT') {
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     0 AS SEQ, ";
                $query .= "     T1.TEST_DATE AS REGDDATE, ";
                $query .= "     '00-00-00-000000' AS SUBCLASSCD, ";
                $query .= "     CASE L1.CONDITION_DIV ";
                $query .= "          WHEN '1' ";
                $query .= "          THEN '国家資格' ";
                $query .= "          WHEN '2' ";
                $query .= "          THEN '公的資格' ";
                $query .= "          WHEN '3' ";
                $query .= "          THEN '民間資格' ";
                $query .= "          ELSE 'その他' ";
                $query .= "     END AS CONDITION_DIV, ";
                $query .= "     T1.QUALIFIED_CD, ";
                $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
                $query .= "     L1.QUALIFIED_ABBV, ";
                $query .= "     L1.PROMOTER, ";
                $query .= "     '' AS CONTENTS, ";
                $query .= "     L2.TEST_NAME AS RANK, ";
                $query .= "     N1.NAME1 AS REMARK, ";
                $query .= "     0 AS CREDITS ";
                $query .= " FROM ";
                $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
                $query .= "     LEFT JOIN QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "     LEFT JOIN QUALIFIED_TEST_MST L2 ";
                $query .= "          ON L2.YEAR         = T1.YEAR ";
                $query .= "         AND L2.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "         AND L2.TEST_CD      = T1.TEST_CD ";
                $query .= "     LEFT JOIN NAME_MST N1 ";
                $query .= "          ON N1.NAMECD1      = 'Z050' ";
                $query .= "         AND SUBSTR(T1.RESULT_CD, 4) = N1.NAMECD2 ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
                $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
                $query .= "     T1.RESULT_CD NOT IN ('8888', '9999') "; //欠席・不合格を除く
            }
            $query .= " ORDER BY ";
            $query .= "     REGDDATE, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SEQ ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN T1.CONDITION_DIV || ':資格' ";
            $query .= "          ELSE T1.CONDITION_DIV || ':その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "       LEFT OUTER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.YEAR ";
            $query .= "                              AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                              AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                              AND T5.GRADE      = T4.GRADE ";
            $query .= "                              AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T5.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                          AND T5.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }

    //更新処理
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        // HREPORTREMARK_DAT
        //存在チェック
        $chk = $db->getCol(knjd183dQuery::getHreportremarkDat($model));
        $seikin = '';
        if ($model->field["SEIKIN_CHECK"] == '1') {
            $seikin = '精勤';
        }

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["REMARK1"][TEXT]          = $model->field["SHUTOKUSIKAKU"];
            $data["ATTENDREC_REMARK"][TEXT] = $seikin;
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data = array();
            $data["REMARK1"][TEXT]          = $model->field["SHUTOKUSIKAKU"];
            $data["ATTENDREC_REMARK"][TEXT] = $seikin;
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }
        $chk = $db->getCol(knjd183dQuery::getHreportremarkDetailDat($model));

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]       = $model->exp_year;
            $data["SEMESTER"][TEXT]   = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]   = $model->schregno;
            $data["DIV"][TEXT]        = "01";
            $data["CODE"][TEXT]       = '01';
            $data["REMARK1"][TEXT]    = $model->field["ZIRITUKATUDO"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";
            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data = array();
            $data["REMARK1"][TEXT]    = $model->field["ZIRITUKATUDO"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //INSERT
    public function &getInsertQuery2($model)
    {
        $retval = false;
        $db = Query::dbCheckOut();

        //行動の記録・特別活動の記録（削除）
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->exp_semester."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);
        if ($db->getmessage != "") {
            $retval = true;
        }
        for ($i = 0; $i < get_count($model->idlist); $i++) {
            $ival = sprintf("%02d", $i+1);

            $data["YEAR"][TEXT]       = $model->exp_year;
            $data["SEMESTER"][TEXT]   = $model->exp_semester;
            $data["SCHREGNO"][TEXT]   = $model->schregno;
            $data["CODE"][TEXT]       = $ival;
            $data["RECORD"][TEXT]     = $model->record["RECORD"][$ival];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $query = Query::insertSQL($data, "BEHAVIOR_SEMES_DAT");
            $db->query($query);
            if ($db->getmessage != "") {
                $retval = false;
            }
        }

        Query::dbCheckIn($db);
        return ;
    }

    //学校マスタの校種有無チェック
    public function checkSchoolMst()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    //出欠の記録参照
    public function getAttendSemesDat($model, $semester, $knjSchoolMst)
    {
        $query  = " WITH ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS,0))  AS SICK, ";
        } else {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) - VALUE(OFFDAYS,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0))  AS SICK, ";
        }
        $query .= "         SUM(VALUE(SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             + VALUE(VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             + VALUE(KOUDOME,0) ";
        }
        $query .= "             + VALUE(MOURNING,0))  AS SUSPEND ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->exp_year."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "         SEMESTER    = '".$semester."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        $query .= " , ATTEND_REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->exp_year."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "         SEMESTER    = '".$semester."' ";
        $query .= " ) ";
        $query .= " , MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.LESSON, ";
        $query .= "         T1.SUSPEND, ";
        $query .= "         VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) AS ATTEND1, ";
        $query .= "         T1.SICK, ";
        $query .= "         VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) - VALUE(T1.SICK,0) AS ATTEND2, ";
        $query .= "         T2.ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM T1 ";
        $query .= "         LEFT JOIN ATTEND_REMARK T2 ON T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "             T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    public function getHandCap($SCHREGNO)
    {
        $query  = "";
        $query  = "     SELECT ";
        $query .= "         NAME, ";
        $query .= "         HANDICAP ";
        $query .= "     FROM";
        $query .= "         SCHREG_BASE_MST";
        $query .= "     WHERE";
        $query .= "         SCHREGNO='".$SCHREGNO."'";

        return $query;
    }

    public function getNyuryokuPattern()
    {
        $query  = "";
        $query  = "     SELECT REMARK1 ";
        $query .= "       FROM HREPORT_CONDITION_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "        AND SEQ  = '012'";

        return $query;
    }

    public function getKetsuji($schregno, $semester)
    {
        $query  = "";
        $query .= "  SELECT SUM(NVL(ASD.SICK, 0) ";
        $query .= "             + NVL(ASD.NOTICE, 0) ";
        $query .= "             + NVL(ASD.NONOTICE, 0) ";
        $query .= "             + NVL(ASD.NURSEOFF, 0) ";
        $query .= "             + CASE WHEN NVL(SCHOOL.SUB_OFFDAYS,  '0') = '1' THEN NVL(ASD.OFFDAYS,  0) ELSE 0 END ";
        $query .= "             + CASE WHEN NVL(SCHOOL.SUB_ABSENT,   '0') = '1' THEN NVL(ASD.ABSENT,   0) ELSE 0 END ";
        $query .= "             + CASE WHEN NVL(SCHOOL.SUB_SUSPEND,  '0') = '1' THEN NVL(ASD.SUSPEND,  0) ELSE 0 END ";
        $query .= "             + CASE WHEN NVL(SCHOOL.SUB_MOURNING, '0') = '1' THEN NVL(ASD.MOURNING, 0) ELSE 0 END ";
        $query .= "             + CASE WHEN NVL(SCHOOL.SUB_VIRUS,    '0') = '1' THEN NVL(ASD.VIRUS,    0) ELSE 0 END ";
        $query .= "           ) AS KETSUJI ";
        $query .= "      FROM ATTEND_SUBCLASS_DAT ASD ";
        $query .= " LEFT JOIN SCHOOL_MST AS SCHOOL ";
        $query .= "        ON (SCHOOL.YEAR        = ASD.YEAR ";
        $query .= "       AND  SCHOOL.SCHOOLCD    = '". SCHOOLCD ."' ";
        $query .= "       AND  SCHOOL.SCHOOL_KIND = '". SCHOOLKIND ."' )";
        $query .= "     WHERE ASD.YEAR     = '". CTRL_YEAR ."' ";
        $query .= "       AND ASD.SCHREGNO = '". $schregno ."' ";
        $query .= "       AND ASD.SEMESTER = '". $semester ."' ";

        return $query;
    }

    public function getSyussekiTikokuSoutai($schregno, $semester)
    {
        $query  = "";
        $query .= "  SELECT SUM(NVL(ASD.SICK, 0) ";
        $query .= "              + NVL(ASD.NOTICE, 0) ";
        $query .= "              + NVL(ASD.NONOTICE, 0) ";
        $query .= "              + NVL(ASD.LATE, 0) ";
        $query .= "              + NVL(ASD.EARLY, 0) ";
        $query .= "            ) AS SYUSSEKI_TIKOKU_SOUTAI ";
        $query .= "       FROM ATTEND_SEMES_DAT ASD ";
        $query .= "      WHERE ASD.YEAR     = '". CTRL_YEAR ."' ";
        $query .= "        AND ASD.SCHREGNO = '". $schregno ."' ";
        $query .= "        AND ASD.SEMESTER = '". $semester ."' ";

        return $query;
    }
}
