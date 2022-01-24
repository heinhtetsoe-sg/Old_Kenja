<?php

require_once('for_php7.php');

class knjf080Query extends Query
{

    //生徒健康診断データを取得する
    public function selectQuery($model, $nameCd2_Jcd = "")
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  CASE WHEN INTEGER(T1.NUTRITIONCD)    >= 2 THEN L1.NAME1 END NUTRITIONCD, ";
        $query .= "  CASE WHEN INTEGER(T1.EYEDISEASECD)   >= 2 THEN L2.NAME1 END EYEDISEASECD, ";
        $query .= "  CASE WHEN INTEGER(T1.SKINDISEASECD)  >= 2 THEN L3.NAME1 END SKINDISEASECD, ";
        $query .= "  CASE WHEN INTEGER(T1.SPINERIBCD)     >= 2 THEN L4.NAME1 END SPINERIBCD, ";
        $query .= "  CASE WHEN INTEGER(T1.NOSEDISEASECD)  >= 2 THEN L5.NAME1 END NOSEDISEASECD, ";
        $query .= "  CASE WHEN INTEGER(T1.OTHERDISEASECD) >= 2 THEN L6.NAME1 END OTHERDISEASECD, ";
        $query .= "  CASE WHEN INTEGER(T1.HEARTDISEASECD) >= 2 THEN L7.NAME1 END HEARTDISEASECD, ";
        $query .= "  T1.NUTRITION_RESULT, ";
        $query .= "  T1.EYEDISEASE_RESULT, ";
        $query .= "  T1.SKINDISEASE_RESULT, ";
        $query .= "  T1.SPINERIB_RESULT, ";
        $query .= "  T1.NOSEDISEASE_RESULT, ";
        $query .= "  T1.OTHERDISEASE_RESULT, ";
        $query .= "  T1.HEARTDISEASE_RESULT ";
        if ($model->isKumamoto) {
            $query .= "  ,CASE WHEN INTEGER(T3.REMAINBABYTOOTH) > 0 THEN '乳歯' || T3.REMAINBABYTOOTH || '本' END REMAINBABYTOOTH ";
            $query .= "  ,CASE WHEN INTEGER(T3.REMAINADULTTOOTH) > 0 THEN '永久歯' || T3.REMAINADULTTOOTH || '本' END REMAINADULTTOOTH ";
            $query .= "  ,CASE WHEN T3.JAWS_JOINTCD > '01' THEN L8.NAME1 END JAWS_JOINTCD ";
            $query .= "  ,CASE WHEN ";
            $query .= "       T3.PLAQUECD > '01' ";
            $query .= "   THEN ";
            $query .= "       CASE WHEN ";
            $query .= "           FISCALYEAR(B1.ENT_DATE) >= '2016' ";
            $query .= "       THEN ";
            $query .= "           F516.NAME1 ";
            $query .= "       ELSE ";
            $query .= "           F520.NAME1 ";
            $query .= "       END ";
            $query .= "   END PLAQUECD ";
            $query .= "  ,CASE WHEN ";
            $query .= "       T3.GUMCD > '01' ";
            $query .= "   THEN ";
            $query .= "       CASE WHEN ";
            $query .= "           FISCALYEAR(B1.ENT_DATE) >= '2016' ";
            $query .= "       THEN ";
            $query .= "           F517.NAME1 ";
            $query .= "       ELSE ";
            $query .= "           F513.NAME1 ";
            $query .= "       END ";
            $query .= "   END GUMCD ";
            $query .= "  ,CASE WHEN ";
            $query .= "       T3.OTHERDISEASECD > '01' ";
            $query .= "   THEN ";
            $query .= "       CASE WHEN ";
            $query .= "           T3.OTHERDISEASECD = '99' and T3.OTHERDISEASE is not null ";
            $query .= "       THEN ";
            $query .= "           F530.NAME1 || '(' || T3.OTHERDISEASE || ')' ";
            $query .= "       ELSE ";
            $query .= "           F530.NAME1 ";
            $query .= "       END ";
            $query .= "   END TOOTH_OTHERDISEASECD ";
            $query .= "  ,CASE WHEN ";
            $query .= "       T1.R_VISION_MARK is null AND T1.R_BAREVISION_MARK > 'A' ";
            $query .= "   THEN ";
            $query .= "       '右:裸眼(' || T1.R_BAREVISION_MARK || ')' ";
            $query .= "   END R_BAREVISION_MARK ";
            $query .= "  ,CASE WHEN ";
            $query .= "       T1.L_VISION_MARK is null AND T1.R_BAREVISION_MARK > 'A' ";
            $query .= "   THEN ";
            $query .= "       '左:裸眼(' || T1.L_BAREVISION_MARK || ')' ";
            $query .= "   END L_BAREVISION_MARK ";
            $query .= "  ,CASE WHEN ";
            $query .= "       T1.R_VISION_MARK is not null AND T1.R_VISION_MARK > 'A' ";
            $query .= "   THEN ";
            $query .= "       '右:矯正(' || T1.R_VISION_MARK || ')' ";
            $query .= "   END R_VISION_MARK ";
            $query .= "  ,CASE WHEN ";
            $query .= "       T1.L_VISION_MARK is not null AND T1.L_VISION_MARK > 'A' ";
            $query .= "   THEN ";
            $query .= "       '左:矯正(' || T1.L_VISION_MARK || ')' ";
            $query .= "   END L_VISION_MARK ";
            $query .= "  ,T2.DET_REMARK1 AS D021_REMARK1 ";
            $query .= "  ,T2.DET_REMARK2 AS D021_REMARK2 ";
        }
        $query .= "FROM ";
        $query .= "  MEDEXAM_DET_DAT T1 ";
        $query .= "  LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'F030' AND L1.NAMECD2 = T1.NUTRITIONCD ";
        $query .= "  LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'F050' AND L2.NAMECD2 = T1.EYEDISEASECD ";
        $query .= "  LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'F070' AND L3.NAMECD2 = T1.SKINDISEASECD ";
        $query .= "  LEFT JOIN NAME_MST L4 ON L4.NAMECD1 = 'F040' AND L4.NAMECD2 = T1.SPINERIBCD ";
        $query .= "  LEFT JOIN NAME_MST L5 ON L5.NAMECD1 = 'F060' AND L5.NAMECD2 = T1.NOSEDISEASECD ";
        $query .= "  LEFT JOIN NAME_MST L6 ON L6.NAMECD1 = 'F140' AND L6.NAMECD2 = T1.OTHERDISEASECD ";
        $query .= "  LEFT JOIN NAME_MST L7 ON L7.NAMECD1 = 'F090' AND L7.NAMECD2 = T1.HEARTDISEASECD ";
        if ($model->isKumamoto) {
            $query .= "  LEFT JOIN MEDEXAM_DET_DETAIL_DAT T2 ";
            $query .= "       ON T2.YEAR     = T1.YEAR ";
            $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "      AND T2.DET_SEQ  = '021' ";
            $query .= "  LEFT JOIN MEDEXAM_TOOTH_DAT T3 ";
            $query .= "       ON T3.YEAR     = T1.YEAR ";
            $query .= "      AND T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "  LEFT JOIN SCHREG_BASE_MST B1 ";
            $query .= "       ON B1.SCHREGNO = T1.SCHREGNO ";
            if ($nameCd2_Jcd) {
                $query .= "  LEFT JOIN NAME_MST L8 ON L8.NAMECD1 = '".$nameCd2_Jcd."' AND L8.NAMECD2 = T3.JAWS_JOINTCD ";
            }
            $query .= "  LEFT JOIN NAME_MST F516 ON F516.NAMECD1 = 'F516' AND F516.NAMECD2 = T3.PLAQUECD ";
            $query .= "  LEFT JOIN NAME_MST F520 ON F520.NAMECD1 = 'F520' AND F520.NAMECD2 = T3.PLAQUECD ";
            $query .= "  LEFT JOIN NAME_MST F517 ON F517.NAMECD1 = 'F517' AND F517.NAMECD2 = T3.GUMCD ";
            $query .= "  LEFT JOIN NAME_MST F513 ON F513.NAMECD1 = 'F513' AND F513.NAMECD2 = T3.GUMCD ";
            $query .= "  LEFT JOIN NAME_MST F530 ON F530.NAMECD1 = 'F530' AND F530.NAMECD2 = T3.OTHERDISEASECD ";
        }
        $query .= "WHERE ";
        $query .= "  T1.YEAR     = '". CTRL_YEAR . "' AND ";
        $query .= "  T1.SCHREGNO = '". $model->schregno . "' ";

        return $query;
    }
    //年組,先生名
    public function getGrd_ClasQuery($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "       T1.GRADE || ',' || T1.HR_CLASS AS GC,";
        $query .= "       T1.HR_NAME AS GC_J, ";
        $query .= "       T2.STAFFNAME_SHOW AS NAME_SHOW, ";
        $query .= "       T2.STAFFCD ";
        $query .= "  FROM SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ";
        $query .= "       ON T1.tr_cd1 = T2.staffcd";
        $query .= " WHERE T1.YEAR     = '". CTRL_YEAR. "' ";
        $query .= "   AND T1.SEMESTER = '". CTRL_SEMESTER. "' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "AND T1.TR_CD1 = '". STAFFCD ."'";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY GC ";

        return $query;
    }

    //生徒一覧
    public function ReadQuery($model)
    {
        $query = "";
        $query .= "SELECT DISTINCT ";
        $query .= "  T1.YEAR, ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.ATTENDNO, ";
        $query .= "  T2.SCHREGNO, ";
        $query .= "  T2.NAME_SHOW AS NAME_SHOW, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T2.NAME_KANA AS KANA_SHOW, ";
        $query .= "  T2.SEX, ";
        $query .= "  T1.UPDATED, ";
        $query .= "  T3.NUTRITIONCD ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_DAT T1, ";
        $query .= "  SCHREG_BASE_MST T2, ";
        $query .= "  MEDEXAM_DET_DAT T3 ";
        if ($model->isKumamoto) {
            $query .= "  ,MEDEXAM_TOOTH_DAT T4 ";
        }
        $query .= "WHERE ";
        $query .= "  T1.YEAR = '". CTRL_YEAR . "' AND ";
        $query .= "  T1.GRADE = '". substr($model->GradeClass, 0, 2). "' AND ";
        $query .= "  T1.HR_CLASS = '". substr($model->GradeClass, 3). "' AND ";
        $query .= "  T1.SEMESTER = '". CTRL_SEMESTER. "' AND ";
        $query .= "  T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "  T1.YEAR = T3.YEAR AND ";
        $query .= "  T2.SCHREGNO = T3.SCHREGNO AND ";
        if ($model->isKumamoto) {
            $query .= "  T1.YEAR = T4.YEAR AND ";
            $query .= "  T2.SCHREGNO = T4.SCHREGNO AND ";
        }
        $query .= "  ((T3.NUTRITIONCD <> '' AND INTEGER(T3.NUTRITIONCD) >= 2) OR  ";
        $query .= "   (T3.EYEDISEASECD <> '' AND INTEGER(T3.EYEDISEASECD) >= 2) OR ";
        $query .= "   (T3.NOSEDISEASECD <> '' AND INTEGER(T3.NOSEDISEASECD) >= 2) OR ";
        $query .= "   (T3.SKINDISEASECD <> '' AND INTEGER(T3.SKINDISEASECD) >= 2) OR ";
        $query .= "   (T3.SPINERIBCD <> '' AND INTEGER(T3.SPINERIBCD) >= 2) OR ";
        $query .= "   (T3.OTHERDISEASECD <> '' AND INTEGER(T3.OTHERDISEASECD) >= 2) OR ";
        $query .= "   (T3.HEARTDISEASECD <> '' AND INTEGER(T3.HEARTDISEASECD) >= 2) ";
        if ($model->isKumamoto) {
            $query .= "   OR ";
            //視力
            $query .= "   (T3.R_VISION_MARK is not null AND T3.R_VISION_MARK > 'A') OR ";
            $query .= "   (T3.L_VISION_MARK is not null AND T3.L_VISION_MARK > 'A') OR ";
            $query .= "   (T3.R_VISION_MARK is null AND T3.R_BAREVISION_MARK > 'A') OR ";
            $query .= "   (T3.L_VISION_MARK is null AND T3.L_BAREVISION_MARK > 'A') OR ";
            //歯科
            $query .= "   (T4.REMAINBABYTOOTH > 0) OR ";
            $query .= "   (T4.REMAINADULTTOOTH > 0) OR ";
            $query .= "   (T4.JAWS_JOINTCD > '01') OR ";
            $query .= "   (T4.PLAQUECD > '01') OR ";
            $query .= "   (T4.GUMCD > '01') OR ";
            $query .= "   (T4.OTHERDISEASECD > '01') ";
        }
        $query .= "  ) ";
        $query .= "ORDER BY ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.ATTENDNO ";

        return $query;
    }
    /**
     * UPDATE
     */
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query = "";
        $query .= "UPDATE ";
        $query .= "  MEDEXAM_DET_DAT ";
        $query .= "SET ";
        $query .= "  NUTRITION_RESULT    = '" .Query::addquote($model->field["NUTRITION_RESULT"]) ."', ";
        $query .= "  EYEDISEASE_RESULT   = '" .Query::addquote($model->field["EYEDISEASE_RESULT"]) ."', ";
        $query .= "  SKINDISEASE_RESULT  = '" .Query::addquote($model->field["SKINDISEASE_RESULT"]) ."', ";
        $query .= "  SPINERIB_RESULT     = '" .Query::addquote($model->field["SPINERIB_RESULT"]) ."', ";
        $query .= "  NOSEDISEASE_RESULT  = '" .Query::addquote($model->field["NOSEDISEASE_RESULT"]) ."', ";
        $query .= "  OTHERDISEASE_RESULT = '" .Query::addquote($model->field["OTHERDISEASE_RESULT"]) ."', ";
        $query .= "  HEARTDISEASE_RESULT = '" .Query::addquote($model->field["HEARTDISEASE_RESULT"]) ."', ";
        $query .= "  UPDATED = SYSDATE() ";
        $query .= "WHERE ";
        $query .= "  YEAR     = '". CTRL_YEAR . "' AND ";
        $query .= "  SCHREGNO = '". $model->schregno . "' ";
        $query .= " ";

        $db->query($query);

        if ($model->isKumamoto) {
            //MEDEXAM_DET_DETAIL_DAT(021)
            $s021Cnt = $db->getOne(knjf080Query::getDetailCnt(CTRL_YEAR, $model->schregno, '021'));
            $data2 = array();
            $data2["YEAR"][TEXT]         = CTRL_YEAR;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = '021';
            $data2["DET_REMARK1"][TEXT]  = $model->field["D021_REMARK1"];    //視力
            $data2["DET_REMARK2"][TEXT]  = $model->field["D021_REMARK2"];    //歯科
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            if ($s021Cnt == 0) {
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
            } else {
                $where  = " WHERE YEAR     = '".CTRL_YEAR."' ";
                $where .= "   AND SCHREGNO = '{$model->schregno}' ";
                $where .= "   AND DET_SEQ  = '021' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //カウント取得(MEDEXAM_DET_DETAIL_DAT)
    public function getDetailCnt($year, $schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DET_SEQ  = '{$seq}' ";

        return $query;
    }

    //Z010取得
    public function getNameMstZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //最小学年（校種毎）
    public function getMinGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 AS MIN_GRADE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1   = '{$model->getSchKind}' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$grade."' ";

        return $query;
    }
}
