<?php

require_once('for_php7.php');

class knjf331Query extends Query {

    //教育委員会判定
    function z010Abbv1() {
        $query  = " SELECT ";
        $query .= "      ABBV1 ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'Z010' ";
        $query .= "      AND NAMECD2 = '00' ";

        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst() {
        $db = Query::dbCheckOut();

        //教育委員会判定
        $query = knjf331Query::z010Abbv1();
        $z010Abbv1 = $db->getOne($query);

        $retSchool = "";
        if ($z010Abbv1 == "1" || $z010Abbv1 == "2") {
            //V_SCHOOL_MSTから学校コードを取得
            $query  = " SELECT ";
            $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
            $query .= " FROM ";
            $query .= "      V_SCHOOL_MST ";
            $query .= " WHERE ";
            $query .= "      YEAR    = '".CTRL_YEAR."' ";

            $retSchool = $db->getOne($query);
        } else {
            $retSchool = "000000000000";
        }

        Query::dbCheckIn($db);

        return $retSchool;
    }

    //Z010
    function getZ010()
    {
        $query  = "SELECT NAME1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学習記録データの存在確認
    function getRecSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_studyrec_dat ";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        return $query;
    }
    //学籍在籍データの存在確認
    function getSchSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_regd_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }
    //評定マスタの存在確認
    function getAssesscd()
    {
        $query  = "SELECT COUNT(assesscd) FROM assess_mst ";
        $query .= " WHERE assesscd = '4' ";
        return $query;
    }

    //データ表示
    function ReadQuery($model) {
        $query  = "";
        //学年・性別
        $query .= " WITH T_AGE (AGE_DATA, AGE_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS AGE_DATA, ";
        $query .= "         RTRIM(CAST(CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS CHAR(4))) AS AGE_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
                $query .= "           AND REGD.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
            $query .= "           AND REGD.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "               AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     GROUP BY ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES(99, '合計') ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('9', '合計') ";
        $query .= "     ) ";
        $query .= " , T_AGE_SEX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_AGE T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.AGE_DATA = 99 OR T2.SEX != '9' ";
        $query .= "     ) ";
        if ($model->cmd == "recalc") {
            //再計算
            $query .= " , T_RECALC AS ( ";
            if ($model->dataDiv != "2") {
                $query .= "     SELECT ";
                $query .= "         CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - T2.BIRTHDAY) END AS AGE_DATA, ";
                $query .= "         VALUE(T2.SEX, '9') AS SEX, ";
                $query .= "         SUM(1) AS DATA000_01, ";
                $query .= "         SUM(CASE WHEN L1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS DATA000_02, ";
                $query .= "         SUM(CASE WHEN F030.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA001_01, ";
                $query .= "         SUM(CASE WHEN F030.ABBV3 = '20' THEN 1 ELSE 0 END) AS DATA001_02, ";
                $query .= "         SUM(CASE WHEN F040.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA002_01, ";
                $query .= "         SUM(CASE WHEN F040.ABBV3 = '20' THEN 1 ELSE 0 END) AS DATA002_02, ";
                $query .= "         SUM(CASE WHEN L1.R_BAREVISION_MARK = 'A' AND L1.L_BAREVISION_MARK = 'A' THEN 1 ELSE 0 END) AS DATA003_01, ";
                $query .= "         SUM(CASE WHEN (L1.R_BAREVISION_MARK = 'B' AND L1.L_BAREVISION_MARK >= 'B') OR (L1.R_BAREVISION_MARK >= 'B' AND L1.L_BAREVISION_MARK = 'B') THEN 1 ELSE 0 END) AS DATA003_02, ";
                $query .= "         SUM(CASE WHEN (L1.R_BAREVISION_MARK = 'C' AND L1.L_BAREVISION_MARK >= 'C') OR (L1.R_BAREVISION_MARK >= 'C' AND L1.L_BAREVISION_MARK = 'C') THEN 1 ELSE 0 END) AS DATA003_03, ";
                $query .= "         SUM(CASE WHEN (L1.R_BAREVISION_MARK = 'D' AND L1.L_BAREVISION_MARK >= 'D') OR (L1.R_BAREVISION_MARK >= 'D' AND L1.L_BAREVISION_MARK = 'D') THEN 1 ELSE 0 END) AS DATA003_04, ";
                $query .= "         SUM(CASE WHEN (L1.R_BAREVISION_MARK IN ('B', 'C', 'D') AND L1.L_BAREVISION_MARK >= 'D') OR (L1.R_BAREVISION_MARK >= 'D' AND L1.L_BAREVISION_MARK IN ('B', 'C', 'D')) THEN 1 ELSE 0 END) AS DATA003_05, ";
                $query .= "         SUM(CASE WHEN F050.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA004_01, ";
                $query .= "         SUM(CASE WHEN F051.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA004_02, ";
                $query .= "         SUM(CASE WHEN F050.ABBV3 = '20' THEN 1 ELSE 0 END) AS DATA004_03, ";
                $query .= "         SUM(CASE WHEN ((VALUE(L1.R_EAR, '01') = '02' AND VALUE(L1.R_EAR_DB, 0) >= 30) AND ";
                $query .= "                        ((VALUE(L1.L_EAR, '01') = '02' AND VALUE(L1.L_EAR_DB, 0) >= 30) OR ";
                $query .= "                         (VALUE(L1.L_EAR, '01') = '03' AND VALUE(L1.L_EAR_DB, 0) >= 25)) ";
                $query .= "                       ) OR ";
                $query .= "                       ((VALUE(L1.R_EAR, '01') = '03' AND VALUE(L1.R_EAR_DB, 0) >= 25) AND ";
                $query .= "                        ((VALUE(L1.L_EAR, '01') = '02' AND VALUE(L1.L_EAR_DB, 0) >= 30) OR ";
                $query .= "                         (VALUE(L1.L_EAR, '01') = '03' AND VALUE(L1.L_EAR_DB, 0) >= 25)) ";
                $query .= "                       ) ";
                $query .= "                  THEN 1 ELSE 0 END) AS DATA005_01, ";
                $query .= "         SUM(CASE WHEN (NOT((VALUE(L1.R_EAR, '01') = '02' AND VALUE(L1.R_EAR_DB, 0) >= 30) AND ";
                $query .= "                        ((VALUE(L1.L_EAR, '01') = '02' AND VALUE(L1.L_EAR_DB, 0) >= 30) OR ";
                $query .= "                         (VALUE(L1.L_EAR, '01') = '03' AND VALUE(L1.L_EAR_DB, 0) >= 25)) ";
                $query .= "                       )) AND ";
                $query .= "                       (NOT((VALUE(L1.R_EAR, '01') = '03' AND VALUE(L1.R_EAR_DB, 0) >= 25) AND ";
                $query .= "                        ((VALUE(L1.L_EAR, '01') = '02' AND VALUE(L1.L_EAR_DB, 0) >= 30) OR ";
                $query .= "                         (VALUE(L1.L_EAR, '01') = '03' AND VALUE(L1.L_EAR_DB, 0) >= 25)) ";
                $query .= "                       )) AND ";
                $query .= "                       F061.ABBV3 = '10' ";
                $query .= "                  THEN 1 ELSE 0 END) AS DATA006_01, ";
                $query .= "         SUM(CASE WHEN F062.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA006_02, ";
                $query .= "         SUM(CASE WHEN F063.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA006_03, ";
                $query .= "         SUM(CASE WHEN F070.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA007_01, ";
                $query .= "         SUM(CASE WHEN F120.ABBV3 = '10' OR F130.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA010_01, ";
                $query .= "         SUM(CASE WHEN F100.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA011_01, ";
                $query .= "         SUM(CASE WHEN F100.ABBV3 = '20' THEN 1 ELSE 0 END) AS DATA011_02, ";
                $query .= "         SUM(CASE WHEN F090.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA012_01, ";
                $query .= "         SUM(CASE WHEN F080.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA013_01, ";
                $query .= "         SUM(CASE WHEN F020.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA014_01, ";
                $query .= "         SUM(CASE WHEN F019.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA014_02, ";
                $query .= "         SUM(CASE WHEN F023.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA015_01, ";
                $query .= "         SUM(CASE WHEN F140.ABBV3 = '10' OR F145.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA016_01 ";
                $query .= "     FROM ";
                $query .= "         SCHREG_REGD_DAT T1 ";
                $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
                $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
                $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "             ON  T3.YEAR     = T1.YEAR ";
                $query .= "             AND T3.GRADE    = T1.GRADE ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "             AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "             AND T3.SCHOOL_KIND    = '".SCHOOLKIND."' ";
                }
                $query .= "         LEFT JOIN MEDEXAM_DET_DAT L1 ";
                $query .= "             ON  L1.YEAR     = T1.YEAR ";
                $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
                $query .= "         LEFT JOIN NAME_MST F030 ON F030.NAMECD1 = 'F030' ";
                $query .= "              AND F030.NAMECD2 = L1.NUTRITIONCD ";
                $query .= "         LEFT JOIN NAME_MST F040 ON F040.NAMECD1 = 'F040' ";
                $query .= "              AND F040.NAMECD2 = L1.SPINERIBCD ";
                $query .= "         LEFT JOIN NAME_MST F050 ON F050.NAMECD1 = 'F050' ";
                $query .= "              AND F050.NAMECD2 = L1.EYEDISEASECD ";
                $query .= "         LEFT JOIN MEDEXAM_DET_DETAIL_DAT DETAIL1 ";
                $query .= "             ON  DETAIL1.YEAR     = T1.YEAR ";
                $query .= "             AND DETAIL1.SCHREGNO = T1.SCHREGNO ";
                $query .= "             AND DETAIL1.DET_SEQ = '002' ";
                $query .= "         LEFT JOIN NAME_MST F051 ON F051.NAMECD1 = 'F051' ";
                $query .= "              AND F051.NAMECD2 = DETAIL1.DET_REMARK6 ";
                $query .= "         LEFT JOIN MEDEXAM_DET_DETAIL_DAT DETAIL2 ";
                $query .= "             ON  DETAIL2.YEAR     = T1.YEAR ";
                $query .= "             AND DETAIL2.SCHREGNO = T1.SCHREGNO ";
                $query .= "             AND DETAIL2.DET_SEQ = '003' ";
                $query .= "         LEFT JOIN NAME_MST F061 ON F061.NAMECD1 = 'F061' ";
                $query .= "              AND F061.NAMECD2 = DETAIL2.DET_REMARK6 ";
                $query .= "         LEFT JOIN NAME_MST F062 ON F062.NAMECD1 = 'F062' ";
                $query .= "              AND F062.NAMECD2 = DETAIL2.DET_REMARK7 ";
                $query .= "         LEFT JOIN NAME_MST F063 ON F063.NAMECD1 = 'F063' ";
                $query .= "              AND F063.NAMECD2 = DETAIL2.DET_REMARK8 ";
                $query .= "         LEFT JOIN NAME_MST F070 ON F070.NAMECD1 = 'F070' ";
                $query .= "              AND F070.NAMECD2 = L1.SKINDISEASECD ";
                $query .= "         LEFT JOIN NAME_MST F120 ON F120.NAMECD1 = 'F120' ";
                $query .= "              AND F120.NAMECD2 = L1.TB_NAMECD ";
                $query .= "         LEFT JOIN NAME_MST F130 ON F130.NAMECD1 = 'F130' ";
                $query .= "              AND F130.NAMECD2 = L1.TB_ADVISECD ";
                $query .= "         LEFT JOIN NAME_MST F100 ON F100.NAMECD1 = 'F100' ";
                $query .= "              AND F100.NAMECD2 = L1.TB_REMARKCD ";
                $query .= "         LEFT JOIN NAME_MST F090 ON F090.NAMECD1 = 'F090' ";
                $query .= "              AND F090.NAMECD2 = L1.HEARTDISEASECD ";
                $query .= "         LEFT JOIN NAME_MST F080 ON F080.NAMECD1 = 'F080' ";
                $query .= "              AND F080.NAMECD2 = L1.HEART_MEDEXAM ";
                $query .= "         LEFT JOIN NAME_MST F020 ON F020.NAMECD1 = 'F020' ";
                $query .= "              AND F020.NAMECD2 = L1.ALBUMINURIA1CD ";
                $query .= "         LEFT JOIN NAME_MST F019 ON F019.NAMECD1 = 'F019' ";
                $query .= "              AND F019.NAMECD2 = L1.URICSUGAR1CD ";
                $query .= "         LEFT JOIN NAME_MST F023 ON F023.NAMECD1 = 'F023' ";
                $query .= "              AND F023.NAMECD2 = L1.PARASITE ";
                $query .= "         LEFT JOIN NAME_MST F140 ON F140.NAMECD1 = 'F140' ";
                $query .= "              AND F140.NAMECD2 = L1.OTHERDISEASECD ";
                $query .= "         LEFT JOIN NAME_MST F145 ON F145.NAMECD1 = 'F145' ";
                $query .= "              AND F145.NAMECD2 = L1.OTHER_ADVISECD ";
                $query .= "     WHERE ";
                $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
                $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
                $query .= "     GROUP BY ";
                $query .= "         GROUPING SETS ((CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - T2.BIRTHDAY) END, T2.SEX), (T2.SEX), ()) ";
            } else {
                $query .= "     SELECT ";
                $query .= "         CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - T2.BIRTHDAY) END AS AGE_DATA, ";
                $query .= "         VALUE(T2.SEX, '9') AS SEX, ";
                $query .= "         SUM(1) AS DATA000_03, ";
                $query .= "         SUM(CASE WHEN L1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS DATA000_04, ";
                $query .= "         SUM(CASE WHEN (VALUE(L1.TREATEDBABYTOOTH, 0) + VALUE(L1.REMAINBABYTOOTH, 0) + VALUE(L1.TREATEDADULTTOOTH, 0) + VALUE(L1.REMAINADULTTOOTH, 0)) = 0 THEN 1 ELSE 0 END) AS DATA008_01, ";
                $query .= "         SUM(CASE WHEN (VALUE(L1.TREATEDBABYTOOTH, 0) + VALUE(L1.REMAINBABYTOOTH, 0) + VALUE(L1.TREATEDADULTTOOTH, 0) + VALUE(L1.REMAINADULTTOOTH, 0)) > 0 THEN 1 ELSE 0 END) AS DATA008_02, ";
                $query .= "         COUNT(*) AS DATA008_03, ";
                $query .= "         SUM(CASE WHEN F530.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA008_04, ";
                $query .= "         SUM(CASE WHEN F531.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA008_05, ";
                if ($model->Properties["printKenkouSindanIppan"] == '2' || $model->Properties["printKenkouSindanIppan"] == '3') {
                    $query .= "         SUM(CASE WHEN L1.BRACK_ADULTTOOTH >= 1 THEN 1 ELSE 0 END) AS DATA009_01, ";
                    $query .= "         SUM(CASE WHEN L1.CHECKADULTTOOTH >= 1 THEN 1 ELSE 0 END) AS DATA009_02, ";
                } else {
                    $query .= "         SUM(CASE WHEN L1.DENTISTREMARK_CO >= 1 THEN 1 ELSE 0 END) AS DATA009_01, ";
                    $query .= "         SUM(CASE WHEN L1.DENTISTREMARK_GO >= '1' THEN 1 ELSE 0 END) AS DATA009_02, ";
                }
                $query .= "         SUM(CASE WHEN F511.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA009_03, ";
                $query .= "         SUM(CASE WHEN F510.ABBV3 = '10' OR F510_2.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA009_04, ";
                $query .= "         SUM(CASE WHEN F513.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA009_05, ";
                $query .= "         COUNT(*) AS DATA009_06, ";
                $query .= "         SUM(VALUE(L1.REMAINADULTTOOTH, 0)) AS DATA009_07, ";
                $query .= "         SUM(VALUE(L1.LOSTADULTTOOTH, 0)) AS DATA009_08, ";
                $query .= "         SUM(VALUE(L1.TREATEDADULTTOOTH, 0)) AS DATA009_09, ";
                $query .= "         ROUND((SUM(VALUE(L1.REMAINADULTTOOTH, 0)) + SUM(VALUE(L1.LOSTADULTTOOTH, 0)) + SUM(VALUE(L1.TREATEDADULTTOOTH, 0))) / COUNT(*) * 100000, 0) / 100000 AS DATA009_10 ";
                $query .= "     FROM ";
                $query .= "         SCHREG_REGD_DAT T1 ";
                $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
                $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
                $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "             ON  T3.YEAR     = T1.YEAR ";
                $query .= "             AND T3.GRADE    = T1.GRADE ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "             AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "             AND T3.SCHOOL_KIND    = '".SCHOOLKIND."' ";
                }
                $query .= "         LEFT JOIN MEDEXAM_TOOTH_DAT L1 ";
                $query .= "             ON  L1.YEAR     = T1.YEAR ";
                $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
                $query .= "         LEFT JOIN NAME_MST F530 ON F530.NAMECD1 = 'F530' ";
                $query .= "              AND F530.NAMECD2 = L1.OTHERDISEASECD ";
                $query .= "         LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DETAIL1 ";
                $query .= "             ON  DETAIL1.YEAR     = T1.YEAR ";
                $query .= "             AND DETAIL1.SCHREGNO = T1.SCHREGNO ";
                $query .= "             AND DETAIL1.TOOTH_SEQ = '002' ";
                $query .= "         LEFT JOIN NAME_MST F531 ON F531.NAMECD1 = 'F531' ";
                $query .= "              AND F531.NAMECD2 = DETAIL1.TOOTH_REMARK1 ";
                $query .= "         LEFT JOIN NAME_MST F511 ON F511.NAMECD1 = 'F511' ";
                $query .= "              AND F511.NAMECD2 = L1.JAWS_JOINTCD2 ";
                $query .= "         LEFT JOIN NAME_MST F510 ON F510.NAMECD1 = 'F510' ";
                $query .= "              AND F510.NAMECD2 = L1.JAWS_JOINTCD ";
                $query .= "         LEFT JOIN NAME_MST F510_2 ON F510_2.NAMECD1 = 'F510_2' ";
                $query .= "              AND F510_2.NAMECD2 = L1.JAWS_JOINTCD3 ";
                $query .= "         LEFT JOIN NAME_MST F513 ON F513.NAMECD1 = 'F513' ";
                $query .= "              AND F513.NAMECD2 = L1.GUMCD ";
                $query .= "     WHERE ";
                $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
                $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
                $query .= "     GROUP BY ";
                $query .= "         GROUPING SETS ((CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - T2.BIRTHDAY) END, T2.SEX), (T2.SEX), ()) ";
            }
            $query .= "     ) ";
        } else {
            //登録テーブル
            $query .= " , T_ADDITION1 AS ( ";
            $query .= "     SELECT ";
            $query .= "         * ";
            $query .= "     FROM ";
            if ($model->fixedData) {
                $query .= "         MEDEXAM_DISEASE_KENSIN_FIXED_DAT ";
            } else {
                $query .= "         MEDEXAM_DISEASE_KENSIN_DAT ";
            }
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND YEAR = '".CTRL_YEAR."' ";
            if ($model->fixedData) {
                $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
            }
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.AGE_DATA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.AGE_NAME, ";
        $query .= "     T1.SEX_NAME ";
        if ($model->cmd == "recalc") {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     ,L1.DATA{$divSeq} ";
            }
        } else {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     ,L{$divSeq}.INT_VAL AS DATA{$divSeq} ";
            }
        }
        $query .= " FROM ";
        $query .= "     T_AGE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON VALUE(L1.AGE_DATA, 99) = T1.AGE_DATA AND L1.SEX = T1.SEX ";
        } else {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     LEFT JOIN T_ADDITION1 L{$divSeq} ON L{$divSeq}.AGE = T1.AGE_DATA ";
                $query .= "          AND L{$divSeq}.SEX = T1.SEX ";
                $query .= "          AND L{$divSeq}.DATA_DIV || '_' || L{$divSeq}.SEQ = '{$divSeq}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.AGE_DATA, ";
        $query .= "     T1.SEX ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        foreach ($model->dataField as $key => $divSeq) {
            list($dataDiv, $seq) = preg_split("/_/", $divSeq);
            $query  = "DELETE FROM MEDEXAM_DISEASE_KENSIN_DAT ";
            $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
            $query .= "   AND YEAR     = '".CTRL_YEAR."'";
            $query .= "   AND DATA_DIV = '{$dataDiv}'";
            $query .= "   AND SEQ      = '{$seq}'";
            $db->query($query);
        }

        //INSERT
        foreach ($model->updField as $key => $val) {
            list($dataDiv, $seq, $age, $sex) = preg_split("/_/", $key);
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["AGE"][NUMBER]                = $age;
            $data["SEX"][TEXT]                  = $sex;
            $data["DATA_DIV"][TEXT]             = $dataDiv;
            $data["SEQ"][TEXT]                  = $seq;

            //理由は解らないが、サーバーにより
            //$valが配列になる場合とならない場合がある。
            //tokioでは、配列となっているが73サーバーではならない
            $data["INT_VAL"][NUMBER]            = is_array($val) ? $val[0] : $val;

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_KENSIN_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_KENSIN_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knjf331Query::getKensinDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            //人数
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["INT_VAL"][TEXT]              = $row["CHAR_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_KENSIN_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE AS LABEL, ";
        $query .= "     T1.EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_KENSIN_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC ";

        return $query;
    }

    //確定コンボ
    function getFixed($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIXED_DATE AS LABEL, ";
        $query .= "     T1.FIXED_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_KENSIN_FIXED_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.FIXED_DATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.FIXED_DATE DESC ";

        return $query;
    }

    //文書番号
    function getTuutatu($model) {
        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ";
        $query .= "          ON T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.DOC_NUMBER   = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        $query .= "     T1.EDBOARD_SCHOOLCD     = '".$model->schoolcd."' AND ";
        $query .= "     L1.REQUEST_ANSWER_PRG   = '".PROGRAMID."' AND ";
        $query .= "     L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getKensinDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_KENSIN_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getKensinFixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_KENSIN_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_DISEASE_KENSIN_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_DISEASE_KENSIN_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・疾病等結果一覧テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_KENSIN_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（疾病等結果一覧）
        $query = knjf331Query::getKensinFixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            //人数
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["CHAR_VAL"][TEXT]             = $row["CHAR_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_KENSIN_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        //報告処理
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".CTRL_YEAR."' AND ";
        $where .= "     DOC_NUMBER = {$model->docNumber} AND ";
        $where .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSV
    function selectCsvQuery($model) {
        $query  = "";
        //学年・性別
        $query .= " WITH T_AGE (AGE_DATA, AGE_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS AGE_DATA, ";
        $query .= "         RTRIM(CAST(CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS CHAR(4))) AS AGE_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
                $query .= "           AND REGD.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
            $query .= "           AND REGD.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "               AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     GROUP BY ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES(99, '合計') ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('9', '合計') ";
        $query .= "     ) ";
        $query .= " , T_AGE_SEX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_AGE T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.AGE_DATA = 99 OR T2.SEX != '9' ";
        $query .= "     ) ";
        //登録テーブル
        $query .= " , T_ADDITION1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        if ($model->fixedData) {
            $query .= "         MEDEXAM_DISEASE_KENSIN_FIXED_DAT ";
        } else {
            $query .= "         MEDEXAM_DISEASE_KENSIN_DAT ";
        }
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND YEAR = '".CTRL_YEAR."' ";
        if ($model->fixedData) {
            $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.AGE_NAME, ";
        $query .= "     T1.SEX_NAME ";
        foreach ($model->dataField as $key => $divSeq) {
            $query .= "     ,L{$divSeq}.INT_VAL AS DATA{$divSeq} ";
        }
        $query .= " FROM ";
        $query .= "     T_AGE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.AGE_DATA = T1.AGE_DATA AND L1.SEX = T1.SEX ";
        } else {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     LEFT JOIN T_ADDITION1 L{$divSeq} ON L{$divSeq}.AGE = T1.AGE_DATA ";
                $query .= "          AND L{$divSeq}.SEX = T1.SEX ";
                $query .= "          AND L{$divSeq}.DATA_DIV || '_' || L{$divSeq}.SEQ = '{$divSeq}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.AGE_DATA, ";
        $query .= "     T1.SEX ";

        return $query;
    }
}
?>
