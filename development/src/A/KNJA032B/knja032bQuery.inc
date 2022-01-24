<?php

require_once('for_php7.php');

class knja032bQuery extends Query {

    // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
    function getNameMst() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1, ";
        $query .= "    NAMESPARE1, ";
        $query .= "    NAMESPARE2 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";
        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $rtnRow;
    }

    //複式・年組コンボ
    function getClassCmb($model) {
        $query  = "";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= " SELECT ";
            $query .= "     '1:' || T1.GHR_CD || '-' || 'XXX' AS VALUE, ";
            $query .= "     T1.GHR_NAME AS LABEL, ";
            $query .= "     T2.STAFFNAME_SHOW ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
            $query .= "     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= " UNION ALL ";
        }
        $query .= " SELECT ";
        $query .= "     '2:' || T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //SCHREG_REGD_HDATの内、名称マスタ「A032」の年組は対象外
        //TODO:複式クラスにいる生徒が、各学年でまとまってい年組　例：01-099, 02-099, 03-099
        $query .= "     AND NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             NAME_MST N1 ";
        $query .= "         WHERE ";
        $query .= "                 N1.NAMECD1 = 'A032' ";
        $query .= "             AND N1.NAMECD2 = T1.GRADE ";
        $query .= "             AND N1.NAME1 = T1.HR_CLASS ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //学校マスタ
    function selectSchool_Mst()
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .CTRL_YEAR ."'";

        return $query;
    }
    //次年度の学期マスタが存在するか
    function selectSemester_Mst()
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .((int)CTRL_YEAR + 1) ."' ";

        return $query;
    }
    //コンボボックスより、生徒を検索
    function ReadQuery($model)
    {
        $query = " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   INTEGER(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "   T2.HR_NAMEABBV, ";
        $query .= "   T3.NAME_SHOW, ";
        $query .= "   T3.NAME_KANA, ";
        $query .= "   T3.GRD_DATE, ";
        $query .= "   T3.GRD_DIV, ";
        $query .= "   T8.REMAINGRADE_FLG, ";
        $query .= "   T3.GRD_NO, ";
        $query .= "   MEISYOU_GET(T6.TRANSFERCD,'A004',1) AS TRANS_STATUS, ";
        $query .= "   COALESCE(T7.ABROAD_CREDITS, 0) AS ABROAD_CREDITS ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= "   LEFT JOIN SCHREG_REGD_GHR_DAT G1 ON G1.SCHREGNO = T1.SCHREGNO AND G1.YEAR = T1.YEAR AND G1.SEMESTER = T1.SEMESTER ";
            $query .= "   LEFT JOIN SCHREG_REGD_GHR_HDAT G2 ON G2.YEAR = G1.YEAR AND G2.SEMESTER = G1.SEMESTER AND G2.GHR_CD = G1.GHR_CD ";
        }
        $query .= "   LEFT OUTER JOIN SCHREG_TRANSFER_DAT T6  ";
        $query .= "     ON T1.SCHREGNO = T6.SCHREGNO AND  ";
        $query .= "     DATE('".str_replace("/","-",CTRL_DATE)."') BETWEEN T6.TRANSFER_SDATE AND T6.TRANSFER_EDATE ";
        $query .= "   LEFT OUTER JOIN ( ";
        $query .= "     SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         SUM(COALESCE(ST1.ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRANSFER_DAT ST1 ";
        if ($model->school["SCHOOLDIV"] == 0 ){     //学年制
            $query .= " WHERE ";
            $query .= "     FISCALYEAR(ST1.TRANSFER_SDATE) = '" .CTRL_YEAR ."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         ST1.SCHREGNO ";
        $query .= "     ) T7 ON T1.SCHREGNO = T7.SCHREGNO ";
        $query .= "   LEFT OUTER JOIN CLASS_FORMATION_DAT T8  ";
        $query .= "     ON T1.SCHREGNO = T8.SCHREGNO AND ";
        $query .= "     INTEGER(T1.YEAR) + 1 = INTEGER(T8.YEAR) AND ";
        $query .= "     T8.SEMESTER = '1', ";
        $query .= "   SCHREG_REGD_HDAT T2, ";
        $query .= "   SCHREG_BASE_MST T3 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "   T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "   T1.YEAR = T2.YEAR AND ";
        $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "   T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "   T1.GRADE = T2.GRADE AND ";
        $query .= "   T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "   ((T3.GRD_DIV IS NOT NULL AND T3.GRD_DIV NOT IN ('2','3')) OR ";
        $query .= "    (T3.GRD_DIV IS NULL OR T3.GRD_DIV = '')) ";

        //複式・年組コンボ
        list($div, $cd) = explode(":", $model->class_cmb);
        if ($div == "1") {
            //1:複式
            $query .= "     AND G2.GHR_CD || '-' || 'XXX' = '{$cd}' ";
        } else {
            //2:年組
            $query .= "     AND T2.GRADE || '-' || T2.HR_CLASS = '{$cd}' ";
        }

        /***
        処理区分が
        「1.進級」
            V_名称マスタ「A023」NAMESPARE2～NAMESPARE3のGRADE以外の人が対象
        「4.進級・修了」、
            V_名称マスタ「A023」NAMESPARE2～NAMESPARE3のGRADEの人が対象
        「2.卒業」、
            V_名称マスタ「A023」NAMESPARE2～NAMESPARE3のGRADEの人が対象
        「3.留年」、
            全員（つまり、条件なし）
        「9.取消し」
            全員（つまり、条件なし）
        ***/
        if ($model->trans == 1) {
            $query .= " AND NOT EXISTS ( ";
            $query .= "     SELECT ";
            $query .= "         'X' ";
            $query .= "     FROM ";
            $query .= "         NAME_MST N1 ";
            $query .= "     WHERE ";
            $query .= "             N1.NAMECD1 = 'A023' ";
            $query .= "         AND T2.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ";
            $query .= " ) ";
        }
        if ($model->trans == 4) {
            $query .= " AND EXISTS ( ";
            $query .= "     SELECT ";
            $query .= "         'X' ";
            $query .= "     FROM ";
            $query .= "         NAME_MST N1 ";
            $query .= "     WHERE ";
            $query .= "             N1.NAMECD1 = 'A023' ";
            $query .= "         AND T2.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ";
            $query .= " ) ";
        }
        if ($model->trans == 2) {
            $query .= " AND EXISTS ( ";
            $query .= "     SELECT ";
            $query .= "         'X' ";
            $query .= "     FROM ";
            $query .= "         NAME_MST N1 ";
            $query .= "     WHERE ";
            $query .= "             N1.NAMECD1 = 'A023' ";
            $query .= "         AND T2.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ";
            $query .= " ) ";
        }

        //ソート
        if (is_array($model->sort)){
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "ATTENDNO":    //番号順
                        $query .= "T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        $query .= ($order > 0)? "ASC " : "DESC ";
                        break 2;
                    case "TARGET":       //席次順
                        if ($order > 0){
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV DESC,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }else{
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }
                        break 2;
                }
            }
        }else{
            $query .= "ORDER BY ";
            $query .= "    T2.HR_NAMEABBV, ";
            $query .= "    INTEGER(T1.ATTENDNO) ASC ";
        }

        return $query;
    }

    //修得単位(学年制)
    function getGetCredits0($schregno, $model)
    {
        $query  = "WITH RECORD_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(GET_CREDIT, 0)) AS RECORD_CREDITS ";
        $query .= "FROM ";
        if ($model->recordTableDiv == "1") {
            $query .= "    RECORD_SCORE_DAT ";
        } else {
            $query .= "    RECORD_DAT ";
        }
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "             NOT IN (SELECT DISTINCT ";
            $query .= "                         ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
        } else {
            $query .= "    AND SUBCLASSCD NOT IN (SELECT DISTINCT ";
            $query .= "                               ATTEND_SUBCLASSCD ";
        }
        $query .= "                           FROM ";
        $query .= "                               SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                               REPLACECD = '1' ";
        $query .= "                               AND YEAR = '".CTRL_YEAR ."' ";
        $query .= "                          ) ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        if ($model->recordTableDiv == "1") {
            $query .= "   AND  SEMESTER = '9' ";
            $query .= "   AND  TESTKINDCD = '99' ";
            $query .= "   AND  TESTITEMCD = '00' ";
            $query .= "   AND  SCORE_DIV = '00' ";
        }
        $query .= "), TRANSFER_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(ABROAD_CREDITS, 0)) AS TRANSFER_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_TRANSFER_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";
        $query .= "    AND TRANSFER_SDATE BETWEEN '".str_replace("/", "-", $model->control_data["学期開始日付"][9])."' ";
        $query .= "                           AND '".str_replace("/", "-", $model->control_data["学期終了日付"][9])."' ";
        $query .= "), QUALIFIED_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(CREDITS, 0)) AS QUALIFIED_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_QUALIFIED_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    VALUE(RECORD_CREDITS, 0) + ";
        $query .= "    VALUE(TRANSFER_CREDITS, 0) + ";
        $query .= "    VALUE(QUALIFIED_CREDITS, 0) AS GET_CREDITS ";
        $query .= "FROM ";
        $query .= "    RECORD_CREDIT, ";
        $query .= "    TRANSFER_CREDIT, ";
        $query .= "    QUALIFIED_CREDIT ";

        return $query;
    }

    //習得単位(単位制)
    function getGetCredits1($schregno)
    {
        $query  = "SELECT ";
        $query .= "    SUM(VALUE(GET_CREDIT, 0)) + SUM(VALUE(ADD_CREDIT, 0)) AS GET_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_STUDYREC_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //修得単位(学年制)
    function getGetCredits2($schregno, $model)
    {
        $query  = " SELECT ";
        $query .= "       SUM(COALESCE(ST4.CREDITS, 0)) AS GET_CREDITS ";
        $query .= " FROM ";
        $query .= "      (SELECT ";
        $query .= "           ST1.SCHREGNO, ";
        $query .= "           CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS ";
        $query .= "           END AS ASSESS, ";
        $query .= "           ST3.CREDITS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD, ";
            $query .= "           ST2.SCHOOL_KIND, ";
            $query .= "           ST2.CURRICULUM_CD, ";
        }
        $query .= "           ST2.SUBCLASSCD, ";
        $query .= "           ST2.GRADE_RECORD ";
        $query .= "       FROM ";
        $query .= "           SCHREG_REGD_DAT AS ST1, ";
        $query .= "           KIN_RECORD_DAT AS ST2, ";
        $query .= "           CREDIT_MST AS ST3 ";
        $query .= "       WHERE ";
        $query .= "           ST1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "           ST1.SEMESTER = '".CTRL_SEMESTER."'AND ";
        $query .= "           ST1.SCHREGNO = '".$schregno."'AND ";
        $query .= "           ST2.YEAR = ST1.YEAR AND ";
        $query .= "           ST2.SCHREGNO = ST1.SCHREGNO AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD = ST3.CLASSCD AND ";
            $query .= "           ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "           ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "           ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        $query .= "           ST3.YEAR = ST1.YEAR AND ";
        $query .= "           ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "           ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "           ST3.GRADE = ST1.GRADE AND ";
        $query .= "           ST3.COURSECODE = ST1.COURSECODE AND ";
        $query .= "           ST3.CLASSCD = SUBSTR(ST3.SUBCLASSCD,1,2) AND ";

        $query .= "           NOT EXISTS (SELECT 'X' ";
        $query .= "                         FROM SUBCLASS_REPLACE_DAT R3 ";
        $query .= "                         WHERE R3.REPLACECD = '1' ";
        $query .= "                           AND R3.YEAR = '".CTRL_YEAR."' ";
        $query .= "                           AND R3.ANNUAL = ST3.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND R3.ATTEND_CLASSCD || '-' || R3.ATTEND_SCHOOL_KIND || '-' || R3.ATTEND_CURRICULUM_CD || '-' || R3.ATTEND_SUBCLASSCD ";
            $query .= "                             = ST3.CLASSCD || '-' || ST3.SCHOOL_KIND || '-' || ST3.CURRICULUM_CD || '-' || ST3.SUBCLASSCD ";
        } else {
            $query .= "                           AND R3.ATTEND_SUBCLASSCD ";
            $query .= "                             = ST3.SUBCLASSCD ";
        }
        $query .= "                      ) ) ST4 ";

        $query .= "         WHERE ";
        $query .= "           (ST4.ASSESS <> '1' AND ST4.ASSESS IS NOT NULL) OR ";
        $query .= "           (SUBSTR(ST4.SUBCLASSCD,1,2) = '90' AND VALUE(ST4.GRADE_RECORD,0) IN (11,22,33)) ";
        $query .= "         GROUP BY ";
        $query .= "           ST4.SCHREGNO ";

        return $query;
    }

    //保留単位(学年制)
    function getRemCredits0($schregno, $model)
    {
        $query  = "SELECT ";
        $query .= "    SUM(CASE WHEN VALUE(COMP_CREDIT, 0) > VALUE(GET_CREDIT, 0) ";
        $query .= "         THEN VALUE(COMP_CREDIT, 0) - VALUE(GET_CREDIT, 0) ";
        $query .= "         ELSE 0 END ";
        $query .= "       ) AS RECORD_CREDITS ";
        $query .= "FROM ";
        if ($model->recordTableDiv == "1") {
            $query .= "    RECORD_SCORE_DAT ";
        } else {
            $query .= "    RECORD_DAT ";
        }
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "             NOT IN (SELECT DISTINCT ";
            $query .= "                               ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
        } else {
            $query .= "    AND SUBCLASSCD NOT IN (SELECT DISTINCT ";
            $query .= "                               ATTEND_SUBCLASSCD ";
        }
        $query .= "                           FROM ";
        $query .= "                               SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                               REPLACECD = '1' ";
        $query .= "                               AND YEAR = '".CTRL_YEAR ."' ";
        $query .= "                          ) ";
        $query .= "    AND SCHREGNO = '".$schregno ."' ";
        if ($model->recordTableDiv == "1") {
            $query .= "   AND  SEMESTER = '9' ";
            $query .= "   AND  TESTKINDCD = '99' ";
            $query .= "   AND  TESTITEMCD = '00' ";
            $query .= "   AND  SCORE_DIV = '00' ";
        }

        return $query;
    }

    //保留単位(学年制)
    function getRemCredits2($schregno, $model)
    {
        $query  = " SELECT ";
        $query .= "       ST4.SCHREGNO, ";
        $query .= "       SUM(COALESCE(ST4.CREDITS, 0)) AS REM_CREDITS ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "           ST1.SCHREGNO, ";
        $query .= "           CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS ";
        $query .= "           END AS ASSESS, ";
        $query .= "           ST3.CREDITS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD, ";
            $query .= "           ST2.SCHOOL_KIND, ";
            $query .= "           ST2.CURRICULUM_CD, ";
        }
        $query .= "           ST2.SUBCLASSCD, ";	//NO003
        $query .= "           ST2.GRADE_RECORD ";	//NO003
        $query .= "       FROM ";
        $query .= "           SCHREG_REGD_DAT AS ST1, ";
        $query .= "           KIN_RECORD_DAT AS ST2, ";
        $query .= "           CREDIT_MST AS ST3 ";
        $query .= "       WHERE ";
        $query .= "           ST1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "           ST1.SEMESTER = '".CTRL_SEMESTER."'AND ";
        $query .= "           ST1.SCHREGNO = '".$schregno."'AND ";
        $query .= "           ST2.YEAR = ST1.YEAR AND ";
        $query .= "           ST2.SCHREGNO = ST1.SCHREGNO AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD = ST3.CLASSCD AND ";
            $query .= "           ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "           ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "           ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        $query .= "           ST3.YEAR = ST1.YEAR AND ";
        $query .= "           ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "           ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "           ST3.GRADE = ST1.GRADE AND ";
        $query .= "           ST3.COURSECODE = ST1.COURSECODE AND ";
        $query .= "           ST3.CLASSCD = SUBSTR(ST3.SUBCLASSCD,1,2) AND ";

        $query .= "           NOT EXISTS (SELECT 'X' ";
        $query .= "                         FROM SUBCLASS_REPLACE_DAT R3 ";
        $query .= "                         WHERE R3.REPLACECD = '1' ";
        $query .= "                           AND R3.YEAR = '".CTRL_YEAR."' ";
        $query .= "                           AND R3.ANNUAL = ST3.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND R3.ATTEND_CLASSCD || '-' || R3.ATTEND_SCHOOL_KIND || '-' || R3.ATTEND_CURRICULUM_CD || '-' || R3.ATTEND_SUBCLASSCD ";
            $query .= "                             = ST3.CLASSCD || '-' || ST3.SCHOOL_KIND || '-' || ST3.CURRICULUM_CD || '-' || ST3.SUBCLASSCD ";
        } else {
            $query .= "                           AND R3.ATTEND_SUBCLASSCD ";
            $query .= "                             = ST3.SUBCLASSCD ";
        }
        $query .= "                      ) ) ST4 ";

        $query .= " WHERE ";
        $query .= "     SUBSTR(ST4.SUBCLASSCD,1,2) <> '90' AND (ST4.ASSESS = '1' OR ST4.ASSESS IS NULL) ";
        $query .= " GROUP BY ";
        $query .= "     ST4.SCHREGNO ";

        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->checked as $val) {
            $grade = $db->getOne("SELECT GRADE FROM SCHREG_REGD_DAT WHERE SCHREGNO = '{$val}' AND YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."'");
            $schoolKind = $db->getOne("SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '{$grade}'");

            //1.進級 4.進級・修了 3.留年 9.取消
            if ($model->trans == 1 || $model->trans == 4 || $model->trans == 3 || $model->trans == 9) {
                $query  = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO = '{$val}' AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int)CTRL_YEAR+1) ."' ";
                $query .= "                       GROUP BY ";
                $query .= "                         YEAR ";
                $query .= "                       ) AND ";
                $query .= "   NOT EXISTS(SELECT ";
                $query .= "               * ";
                $query .= "             FROM ";
                $query .= "               SCHREG_BASE_MST W ";
                $query .= "             WHERE ";
                $query .= "               W.SCHREGNO = T1.SCHREGNO AND ";
                $query .= "               W.GRD_DIV  = '1' ";
                $query .= "             ) ";
                $result = $db->query($query);

                //取消でチェックされている生徒が「卒業」の場合
                if ($model->trans == 3 || $model->trans == 9) {
                    $query  = " UPDATE ";
                    $query .= "   SCHREG_BASE_MST ";
                    $query .= " SET ";
                    $query .= "   (GRD_DIV, GRD_NO, GRD_DATE) = (NULL,NULL,NULL) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO = '{$val}' ";
                    $query .= "   AND GRD_DIV = '1' ";
                    $result = $db->query($query);

                    $query  = " UPDATE ";
                    $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                    $query .= " SET ";
                    $query .= "   (GRD_DIV, GRD_NO, GRD_DATE) = (NULL,NULL,NULL) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO = '{$val}' ";
                    $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";
                    $result = $db->query($query);
                }

                if ($model->trans == 1 || $model->trans == 4 || $model->trans == 3) {
                    $query  = " INSERT INTO CLASS_FORMATION_DAT( ";
                    $query .= " SCHREGNO, ";
                    $query .= " YEAR, ";
                    $query .= " SEMESTER, ";
                    $query .= " GRADE, ";
                    $query .= " OLD_GRADE, ";
                    $query .= " OLD_HR_CLASS, ";
                    $query .= " OLD_ATTENDNO, ";
                    $query .= " COURSECD, ";
                    $query .= " MAJORCD, ";
                    $query .= " COURSECODE, ";
                    $query .= " REMAINGRADE_FLG, ";
                    $query .= " REGISTERCD ";
                    $query .= " ) ";
                    $query .= "   (SELECT ";
                    $query .= "     ST1.SCHREGNO, ";
                    $query .= "     CHAR(INTEGER(ST1.YEAR) + 1) AS YEAR, ";
                    $query .= "     '1', ";
                    if ($model->trans == 1 || $model->trans == 4) {        //進級
                        //１６学年（小６）は、CLASS_FORMATION_DATのGRADEは固定値'01'をセット
                        if ($model->secondarySchoolDiv != "" && $grade == "16") {
                            $query .= "     '01'  AS GRADE, ";
                        } else {
                            $query .= "     SUBSTR(DIGITS(INTEGER(ST1.GRADE)+1),9,2)  AS GRADE, ";
                        }
                    }else{
                        $query .= "     ST1.GRADE, ";
                    }
                    $query .= "     ST1.GRADE, ";
                    $query .= "     ST1.HR_CLASS, ";
                    $query .= "     ST1.ATTENDNO, ";
                    $query .= "     ST1.COURSECD, ";
                    $query .= "     ST1.MAJORCD, ";
                    $query .= "     ST1.COURSECODE, ";
                    if ($model->trans == 3) {        //留年
                        $query .= "     '1',";
                    }else{
                        $query .= "     '0',";
                    }
                    $query .= "     '".STAFFCD ."' ";
                    $query .= "   FROM ";
                    $query .= "     SCHREG_REGD_DAT ST1 ";
                    $query .= "   WHERE ";
                    $query .= "     ST1.YEAR = '" .CTRL_YEAR ."' AND ";
                    $query .= "     ST1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
                    $query .= "     ST1.SCHREGNO = '{$val}' AND ";
                    $query .= "     NOT EXISTS(SELECT ";
                    $query .= "               * ";
                    $query .= "             FROM ";
                    $query .= "               SCHREG_BASE_MST W ";
                    $query .= "             WHERE ";
                    $query .= "               W.SCHREGNO = ST1.SCHREGNO AND ";
                    $query .= "               W.GRD_DIV  = '1' ";
                    $query .= "             ) ";
                    $query .= "   ) ";
                    $result = $db->query($query);
                }
            }

            //2.卒業
            if ($model->trans == 2) {
                $query  = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO = '{$val}' AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int)CTRL_YEAR+1) ."' ";
                $query .= "                       GROUP BY ";
                $query .= "                         YEAR ";
                $query .= "                       ) ";
                $result = $db->query($query);

                $query  = " UPDATE ";
                $query .= "   SCHREG_BASE_MST ";
                $query .= " SET ";
                $query .= "   (GRD_DATE,GRD_DIV) = (SELECT ";
                $query .= "                           ST1.GRADUATE_DATE, ";
                $query .= "                           '1' ";
                $query .= "                         FROM ";
                $query .= "                           SCHOOL_MST ST1 ";
                $query .= "                         WHERE ";
                $query .= "                           ST1.YEAR = '" .CTRL_YEAR ."' ";
                $query .= "                         ) ";
                $query .= " WHERE ";
                $query .= "   SCHREGNO = '{$val}' ";
                $result = $db->query($query);

                $egCnt = $db->getOne("SELECT COUNT(*) FROM SCHREG_ENT_GRD_HIST_DAT WHERE SCHREGNO = '{$val}' AND SCHOOL_KIND = '{$schoolKind}'");
                if ($egCnt > 0) {
                    $query  = " UPDATE ";
                    $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                    $query .= " SET ";
                    $query .= "   (GRD_DATE, GRD_DIV) = (SELECT ";
                    $query .= "                              ST1.GRADUATE_DATE, ";
                    $query .= "                              '1' ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHOOL_MST ST1 ";
                    $query .= "                          WHERE ";
                    $query .= "                              ST1.YEAR = '" .CTRL_YEAR ."' ";
                    $query .= "                         ) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO = '{$val}' ";
                    $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";
                } else {
                    $query  = " INSERT INTO ";
                    $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                    $query .= "   (SCHREGNO, SCHOOL_KIND, GRD_DATE, GRD_DIV) ( ";
                    $query .= "                          SELECT ";
                    $query .= "                              '{$val}' AS SCHREGNO, ";
                    $query .= "                              '{$schoolKind}' AS SCHOOL_KIND, ";
                    $query .= "                              ST1.GRADUATE_DATE, ";
                    $query .= "                              '1' ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHOOL_MST ST1 ";
                    $query .= "                          WHERE ";
                    $query .= "                              ST1.YEAR = '" .CTRL_YEAR ."' ";
                    $query .= "                         ) ";
                }
                $result = $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
