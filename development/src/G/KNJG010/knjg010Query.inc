<?php

require_once('for_php7.php');


// kanji=漢字

class knjg010Query extends Query
{
    //学校名取得
    public function getSchoolName()
    {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //テーブルのフィールドチェック
    public function getFieldCheck($tabname, $colname)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '{$tabname}' ";
        $query .= "     AND COLNAME = '{$colname}' ";

        return $query;
    }

    //発行手数料取得
    public function getCertifKindMstPrice($db)
    {
        $query .= " SELECT ";
        $query .= "     CERTIF_KINDCD ";
        $query .= "   , CURRENT_PRICE, GRADUATED_PRICE ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_MST ";

        return $query;
    }

    //参照するテーブルを判断：卒業用(GRD_XXX)、在籍用(SCH_XXX)
    public function getJudgeTable($schregno, $db)
    {
        $query  = "SELECT COUNT(*) FROM GRD_BASE_MST WHERE SCHREGNO = '".$schregno."' ";

        $ret_val = $db->getOne($query);
        return ($ret_val) ? "GRD" : "SCHREG" ;
    }

    //証明書発行情報の印字幅を取得
    public function getMainDataHaba($db, $model)
    {
        $strworkArray = array();
        $strworkArray["JKUBUN"] = 0;
        if ($model->cmd == "list2") {
            $strworkArray["CERTIF_NO"] = 0;
        } else {
            $strworkArray["HKUBUN"] = 0;
        }
        $strworkArray["HR_NAME"] = 0;
        $strworkArray["SCHREGNO"] = 0;
        $strworkArray["APPLYDATE"] = 0;
        $strworkArray["KINDNAME"] = 0;
        $strworkArray["NAME_SHOW"] = 0;
        $strworkArray["GYEAR"] = 0;
        $strworkArray["REMARK1"] = 0;
        $fieldHaba = array();
        $query = knjg010Query::getMainData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($strworkArray as $field => $haba) {
                $datahaba = mb_strwidth($row[$field]);
                if (0 < $datahaba && ($fieldHaba[$field] == '' || $fieldHaba[$field] < $datahaba)) {
                    $fieldHaba[$field] = $datahaba;
                }
            }
        }
        $result->free();

        return $fieldHaba;
    }

    //証明書発行情報を取得
    public function getMainData($model)
    {
        $query  = "SELECT  ";
        $query .= "    T1.YEAR AS ISSUEYEAR, ";
        $query .= "    T1.CERTIF_INDEX, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.CERTIF_KINDCD, ";
        $query .= "    T1.GRADUATE_FLG, ";
        $query .= "    T1.APPLYDATE, ";
        $query .= "    T1.ISSUERNAME, ";
        $query .= "    T1.ISSUECD, ";
        $query .= "    T1.CERTIF_NO, ";
        $query .= "    L1.CERTIF_INDEX AS D_CERTIF_INDEX, ";
        $query .= "    L1.REMARK1, ";
        $query .= "    L1.REMARK2 AS TR_CD1, ";
        $query .= "    L1.REMARK3 AS KJ_OUT, ";
        $query .= "    L1.REMARK4 AS MIRISYU, ";
        $query .= "    L1.REMARK5 AS RISYU, ";
        $query .= "    L1.REMARK6 AS FORM6, ";
        $query .= "    REPLACE(L1.REMARK7,'-','/') AS PR_DATE, ";
        $query .= "    L1.REMARK8 AS HYOUTEI, ";
        $query .= "    L1.REMARK9 AS SONOTAJUUSYO, ";
        $query .= "    L1.REMARK10 AS GAIHYOU, ";
        $query .= "    L1.REMARK11, ";
        $query .= "    L1.REMARK12 AS ENT_GRD_DATE_FORMAT, ";
        $query .= "    L1.REMARK15 AS CANCEL_FLG, ";
        $query .= "    L1.REMARK16 AS PRINT_STAMP, ";
        $query .= "    L1.REMARK17 AS HANKI_NINTEI_FORM, ";
        $query .= "    L1.REMARK18 AS RYUGAKU_CREDIT, ";
        $query .= "    L1.REMARK19 AS SOGAKU_CREDIT, ";
        $query .= "    L1.REMARK20 AS KNJE070_CHECK_PRINT_STAMP_PRINCIPAL, ";
        $query .= "    L1.REMARK21 AS KNJE070_CHECK_PRINT_STAMP_HR_STAFF, ";
        $query .= "    L1.REMARK22 AS KNJE070D_PRINTHEADERNAME, ";
        $query .= "    L1.REMARK23 AS GVAL_CALC_CHECK, ";
        $query .= "    L1.REMARK24 AS PRINT_AVG_RANK, ";
        $query .= "    L1.REMARK25 AS \"tyousasyo2020shojikouExtendsSelect\", ";
        $query .= "    INT(T1.CERTIF_NO) AS NO, ";
        $query .= "    T1.ISSUEDATE, ";
        $query .= "    T1.CHARGE, ";
        $query .= "    T1.PRINTCD, ";
        $query .= "    T1.UPDATED,  ";
        $query .= "    (CASE WHEN L1.REMARK13 IS NOT NULL THEN '★' ELSE '' END) || VALUE(T3.KINDNAME, '') AS KINDNAME, ";
        $query .= "    T3.ISSUECD AS J_ISSUECD, ";
        $query .= "    T4.ABBV1 AS JKUBUN, ";
        $query .= "    T5.ABBV1 AS HKUBUN, ";
//---------↓--------2005.05.10
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G2.NAME_SHOW ";
        $query .= "         ELSE T2.NAME_SHOW END AS NAME_SHOW, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G2.BIRTHDAY ";
        $query .= "         ELSE T2.BIRTHDAY END AS BIRTHDAY, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G2.GRD_DATE ";
        $query .= "         ELSE HIST.GRD_DATE END AS GRD_DATE, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G7.YEAR ";
        $query .= "         ELSE T7.YEAR END AS YEAR, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G7.GRADE ";
        $query .= "         ELSE T7.GRADE END AS GRADE, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G7.SEMESTER ";
        $query .= "         ELSE T7.SEMESTER END AS SEMESTER, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G7.HR_CLASS ";
        $query .= "         ELSE T7.HR_CLASS END AS HR_CLASS, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G8.COURSENAME ";
        $query .= "         ELSE T8.COURSENAME END AS COURSENAME, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G9.MAJORNAME ";
        $query .= "         ELSE T9.MAJORNAME END AS MAJORNAME, ";
        //$query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G11.TR_CD1 ";
        //$query .= "         ELSE T11.TR_CD1 END AS TR_CD1, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G11.HR_NAME ";
        $query .= "         ELSE T11.HR_NAME END AS HR_NAME, ";
        $query .= "    CASE WHEN G2.SCHREGNO IS NOT NULL THEN G12.STAFFNAME_SHOW ";
        $query .= "         ELSE T12.STAFFNAME_SHOW END AS STAFFNAME, ";
        $query .= "    CASE WHEN T1.GRADUATE_FLG = '0' THEN '' ";
        $query .= "         WHEN T1.GRADUATE_FLG = '1' AND G2.SCHREGNO IS NOT NULL ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         THEN CHAR(INTEGER(G7.YEAR) - ((SELECT INTEGER(FOUNDEDYEAR) FROM SCHOOL_MST WHERE YEAR = '" .$model->year ."' AND SCHOOL_KIND = T7.SCHOOL_KIND) + 1)) ";
            $query .= "         ELSE CHAR(INTEGER(T7.YEAR) - ((SELECT INTEGER(FOUNDEDYEAR) FROM SCHOOL_MST WHERE YEAR = '" .$model->year ."' AND SCHOOL_KIND = T7.SCHOOL_KIND) + 1)) ";
        } else {
            $query .= "         THEN CHAR(INTEGER(G7.YEAR) - ((SELECT INTEGER(FOUNDEDYEAR) FROM SCHOOL_MST WHERE YEAR = '" .$model->year ."') + 1)) ";
            $query .= "         ELSE CHAR(INTEGER(T7.YEAR) - ((SELECT INTEGER(FOUNDEDYEAR) FROM SCHOOL_MST WHERE YEAR = '" .$model->year ."') + 1)) ";
        }
        $query .= "         END AS GYEAR ";
        $query .= "FROM  ";
        $query .= "(SELECT * FROM CERTIF_ISSUE_DAT WHERE YEAR = '" .$model->year ."' AND CERTIF_KINDCD < '100' ";
        if ($model->disp == "1") {
            $query .= " AND ISSUECD = '0' ";
        } elseif ($model->disp == "2") {
            $query .= " ";
        } elseif ($model->disp == "3") {
            $query .= " AND GRADUATE_FLG = '1' AND ISSUECD = '1' ";
        } else { //  if ($model->disp == "4") {
            $query .= " AND GRADUATE_FLG = '0' AND ISSUECD = '1' ";
        }
        $query .= ") T1 ";
        $query .= "LEFT JOIN CERTIF_KIND_MST T3 ON T1.CERTIF_KINDCD = T3.CERTIF_KINDCD ";
        $query .= "LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT L1 ON T1.YEAR = L1.YEAR AND T1.CERTIF_INDEX = L1.CERTIF_INDEX ";
        $query .= "LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'G001' AND T3.ISSUECD = T4.NAMECD2 ";
        $query .= "LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'G003' AND T1.ISSUECD = T5.NAMECD2 ";
        $query .= "LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO  ";
        $query .= "LEFT JOIN (";
        $query .= "   SELECT ";
        $query .= "     TT1.SCHREGNO, ";
        $query .= "     SUBSTR(TT1.WORK,1,4) AS YEAR, ";
        $query .= "     SUBSTR(TT1.WORK,5,1) AS SEMESTER, ";
        $query .= "     TT2.GRADE, ";
        $query .= "     TT2.HR_CLASS, ";
        $query .= "     TT2.COURSECD, ";
        $query .= "     TT2.MAJORCD, ";
        $query .= "     L1.SCHOOL_KIND ";
        $query .= "   FROM ";
        $query .= "     (SELECT SCHREGNO,MAX(YEAR || SEMESTER) AS WORK FROM SCHREG_REGD_DAT WHERE YEAR <= '" .$model->year ."' GROUP BY SCHREGNO) TT1, ";
        $query .= "     SCHREG_REGD_DAT TT2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = TT2.YEAR AND L1.GRADE = TT2.GRADE ";
        $query .= "   WHERE ";
        $query .= "     TT1.SCHREGNO = TT2.SCHREGNO";
        $query .= "     AND   TT1.SCHREGNO = TT2.SCHREGNO ";
        $query .= "     AND   SUBSTR(TT1.WORK,1,4) = TT2.YEAR ";
        $query .= "     AND   SUBSTR(TT1.WORK,5,1) = TT2.SEMESTER ";
        $query .= "   ) T7 ON T1.SCHREGNO = T7.SCHREGNO ";
        $query .= "LEFT JOIN SCHREG_ENT_GRD_HIST_DAT HIST ON T7.SCHREGNO = HIST.SCHREGNO AND T7.SCHOOL_KIND = HIST.SCHOOL_KIND  ";
        $query .= "LEFT JOIN COURSE_MST T8 ON T7.COURSECD = T8.COURSECD ";
        $query .= "LEFT JOIN MAJOR_MST T9 ON T7.COURSECD = T9.COURSECD AND T7.MAJORCD = T9.MAJORCD ";
        $query .= "LEFT JOIN SCHREG_REGD_HDAT T11 ON T7.YEAR = T11.YEAR AND T7.GRADE = T11.GRADE AND T7.HR_CLASS = T11.HR_CLASS AND T7.SEMESTER = T11.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON T11.YEAR = REGD_G.YEAR ";
            $query .= "           AND T11.GRADE = REGD_G.GRADE ";
            if ($model->selectSchoolKind) {
                $query .= "           AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON T11.YEAR = REGD_G.YEAR ";
            $query .= "           AND T11.GRADE = REGD_G.GRADE ";
            $query .= "           AND REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "LEFT JOIN STAFF_MST T12 ON T11.TR_CD1 = T12.STAFFCD ";
        $query .= "LEFT JOIN GRD_BASE_MST G2 ON T1.SCHREGNO = G2.SCHREGNO ";
        $query .= "LEFT JOIN (";
        $query .= "   SELECT ";
        $query .= "     GG1.SCHREGNO, ";
        $query .= "     SUBSTR(GG1.WORK,1,4) AS YEAR, ";
        $query .= "     SUBSTR(GG1.WORK,5,1) AS SEMESTER, ";
        $query .= "     GG2.GRADE, ";
        $query .= "     GG2.HR_CLASS, ";
        $query .= "     GG2.COURSECD, ";
        $query .= "     GG2.MAJORCD ";
        $query .= "   FROM ";
        $query .= "     (SELECT SCHREGNO,MAX(YEAR || SEMESTER) AS WORK FROM GRD_REGD_DAT WHERE (YEAR = '" .$model->year ."' AND SEMESTER ='" .$model->control["学期"] ."') OR YEAR < '" .$model->year ."' GROUP BY SCHREGNO) GG1, ";
        $query .= "     GRD_REGD_DAT GG2 ";
        $query .= "   WHERE ";
        $query .= "     GG1.SCHREGNO = GG2.SCHREGNO";
        $query .= "     AND   GG1.SCHREGNO = GG2.SCHREGNO ";
        $query .= "     AND   SUBSTR(GG1.WORK,1,4) = GG2.YEAR ";
        $query .= "     AND   SUBSTR(GG1.WORK,5,1) = GG2.SEMESTER ";
        $query .= "   ) G7 ON (T1.SCHREGNO = G7.SCHREGNO) ";
        $query .= "LEFT JOIN COURSE_MST G8 ON G7.COURSECD = G8.COURSECD ";
        $query .= "LEFT JOIN MAJOR_MST G9 ON G7.COURSECD = G9.COURSECD AND G7.MAJORCD = G9.MAJORCD ";
        $query .= "LEFT JOIN GRD_REGD_HDAT G11 ON G7.YEAR = G11.YEAR AND G7.GRADE = G11.GRADE AND G7.HR_CLASS = G11.HR_CLASS AND G7.SEMESTER = G11.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GREGD_G ON G11.YEAR = GREGD_G.YEAR ";
            $query .= "           AND G11.GRADE = GREGD_G.GRADE ";
            if ($model->selectSchoolKind) {
                $query .= "           AND GREGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GREGD_G ON G11.YEAR = GREGD_G.YEAR ";
            $query .= "           AND G11.GRADE = GREGD_G.GRADE ";
            $query .= "           AND GREGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "LEFT JOIN STAFF_MST G12 ON G11.TR_CD1 = G12.STAFFCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE  ";
            $query .= "     REGD_G.YEAR IS NOT NULL OR GREGD_G.YEAR IS NOT NULL ";
        }
        if ($model->disp == "1") {
            $query .= " ORDER BY ";
            $query .= "     T1.APPLYDATE DESC, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.CERTIF_KINDCD, ";
            $query .= "     INT(T1.CERTIF_INDEX) ";
        } elseif ($model->disp == "2") {
            $query .= "ORDER BY INT(T1.CERTIF_INDEX) DESC ";
        } else {
            $query .= "ORDER BY T1.CERTIF_NO DESC ";
        }

        return $query;
    }

    //１レコード取得（学籍基礎マスタ）
    public function getSchregBaseMstData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjg010Query::getJudgeTable($model->field["SCHREGNO"], $db);

        $query  = " SELECT * ";
        $query .= " FROM ". $header ."_BASE_MST ";
        $query .= " WHERE SCHREGNO = '" .$model->field["SCHREGNO"] ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //１レコード取得（学籍在籍データ）
    public function getSchregregdData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjg010Query::getJudgeTable($model->field["SCHREGNO"], $db);

        $query  = " SELECT ";
        $query .= " T1.YEAR, ";
        $query .= " T1.GRADE, ";
        $query .= " T1.SEMESTER, ";
        $query .= " T1.HR_CLASS, ";
        $query .= "    T3.STAFFNAME_SHOW AS HR_TEARCH,";
        $query .= " T4.COURSENAME, ";
        $query .= " T5.MAJORNAME, ";
        $query .= " T6.HR_NAME ";
        $query .= " FROM ";
        $query .= "   (";
        $query .= "   SELECT ";
        $query .= "     SUBSTR(TT1.WORK,1,4) AS YEAR, ";
        $query .= "     SUBSTR(TT1.WORK,5,1) AS SEMESTER, ";
        $query .= "     TT2.GRADE, ";
        $query .= "     TT2.HR_CLASS, ";
        $query .= "     TT2.COURSECD, ";
        $query .= "     TT2.MAJORCD ";
        $query .= "   FROM ";
        $query .= "     (SELECT SCHREGNO,MAX(YEAR || SEMESTER) AS WORK FROM ". $header ."_REGD_DAT WHERE SCHREGNO = '" .$model->field["SCHREGNO"] ."' GROUP BY SCHREGNO) TT1, ";
        $query .= "     ". $header ."_REGD_DAT TT2 ";
        $query .= "   WHERE ";
        $query .= "     TT1.SCHREGNO = '" .$model->field["SCHREGNO"] ."'";
        $query .= "     AND   TT1.SCHREGNO = TT2.SCHREGNO ";
        $query .= "     AND   SUBSTR(TT1.WORK,1,4) = TT2.YEAR ";
        $query .= "     AND   SUBSTR(TT1.WORK,5,1) = TT2.SEMESTER ";
        $query .= "   ) T1";
        $query .= "   LEFT JOIN ";
        $query .= "     ". $header ."_REGD_HDAT T6 ";
        $query .= "   ON T1.YEAR     = T6.YEAR ";
        $query .= "     AND   T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND   T1.GRADE = T6.GRADE ";
        $query .= "     AND   T1.HR_CLASS = T6.HR_CLASS ";
        $query .= "   LEFT JOIN STAFF_MST T3 ON  T6.TR_CD1 = T3.STAFFCD ";
        $query .= "   LEFT JOIN  COURSE_MST T4 ON  T1.COURSECD = T4.COURSECD ";
        $query .= "   LEFT JOIN MAJOR_MST T5 ON  T1.COURSECD = T5.COURSECD AND T1.MAJORCD = T5.MAJORCD ";
        $query .= " ORDER BY T1.YEAR DESC, T1.GRADE DESC ,T1.SEMESTER DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //既卒データ取得（学籍異動データ）
    public function getSchregTransferData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjg010Query::getJudgeTable($model->field["SCHREGNO"], $db);

        $query  = " SELECT  ";
        $query .= "  SCHREGNO, ";
        $query .= "  GRD_DIV AS TRANSFERCD, ";
        $query .= "  GRD_DATE AS TRANSFER_SDATE ";
        $query .= " FROM  ";
        if ($header == "SCHREG") {
            $query .= "  SCHREG_ENT_GRD_HIST_DAT ";
        } else {
            $query .= "  ". $header ."_BASE_MST ";
        }
        $query .= " WHERE ";
        $query .= "  SCHREGNO = '" .$model->field["SCHREGNO"] ."' AND ";
        $query .= "  GRD_DATE < DATE('" .$model->control["学籍処理日"] ."') AND ";
        $query .= "  GRD_DIV IN('1','2','3') ";  //退学、卒業データ取得
        if ($header == "SCHREG") {
            $query .= "  AND SCHOOL_KIND IN ( ";
            $query .= "     SELECT ";
            $query .= "         L1.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         (SELECT ";
            $query .= "             SCHREGNO, ";
            $query .= "             MAX(YEAR || SEMESTER) AS MAX_YEAR_SEMESTER ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT ";
            $query .= "         WHERE ";
            $query .= "             SCHREGNO = '" .$model->field["SCHREGNO"] ."' ";
            $query .= "         GROUP BY ";
            $query .= "             SCHREGNO ";
            $query .= "         ) TT1, ";
            $query .= "         SCHREG_REGD_DAT TT2 ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = TT2.YEAR AND L1.GRADE = TT2.GRADE ";
            $query .= "     WHERE ";
            $query .= "             TT1.SCHREGNO = '" .$model->field["SCHREGNO"] ."' ";
            $query .= "         AND TT1.SCHREGNO = TT2.SCHREGNO ";
            $query .= "         AND SUBSTR(TT1.MAX_YEAR_SEMESTER,1,4) = TT2.YEAR ";
            $query .= "         AND SUBSTR(TT1.MAX_YEAR_SEMESTER,5,1) = TT2.SEMESTER ";
            $query .= "  ) ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //職員マスタから情報取得
    public function getStaffcd($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || '　' || T1.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST AS T1, ";
        $query .= "     STAFF_YDAT AS T2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = T2.STAFFCD  ";
        $query .= " AND T2.YEAR = '". $model->control["年度"]. "' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }

    //証明書マスタから情報を取得
    public function getCertifKind($year)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "  T1.CERTIF_KINDCD, ";
        $query .= "  T1.KINDNAME, ";
        $query .= "  T1.ISSUECD, ";
        $query .= "  T1.STUDENTCD, ";
        $query .= "  T1.GRADUATECD, ";
        $query .= "  T1.DROPOUTCD ";
        $query .= " FROM ";
        $query .= "  CERTIF_KIND_MST T1,";
        $query .= "  CERTIF_KIND_YDAT T2";
        $query .= " WHERE ";
        $query .= "  T2.YEAR = '" .$year."' AND ";
        $query .= "  T2.CERTIF_KINDCD = T1.CERTIF_KINDCD";
        $query .= " ORDER BY ";
        $query .= "  T1.CERTIF_KINDCD ";

        return $query;
    }

    //証明書番号情報を取得
    public function getCertifNumber($cd1, $cd2)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "  CERTIF_INDEX ";
        $query .= " FROM ";
        $query .= "  CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "  ISSUEYEAR = '" .$cd1 ."' AND ";
        $query .= "  CERTIF_INDEX = '" .$cd2 ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //証明書番号最終INDEX情報を取得
    public function getCertifIndexMax($cd1)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "  COALESCE(MAX(INTEGER(CERTIF_INDEX)+1),1) as INDEX ";
        $query .= " FROM ";
        $query .= "  CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '" .$cd1 ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //証明書最終番号情報を取得
    public function getBumonCeritfDiv($model, $year, $certifIndex)
    {
        $query .= " SELECT ";
        $query .= "     CISSUE.YEAR, ";
        $query .= "     CISSUE.CERTIF_INDEX, ";
        $query .= "     CISSUE.SCHREGNO, ";
        $query .= "     CISSUE.CERTIF_KINDCD, ";
        $query .= "     CISSUE.GRADUATE_FLG, ";
        $query .= "     CASE WHEN VALUE(CISSUE.GRADUATE_FLG, '0') = '1' AND GREGD.SCHREGNO IS NOT NULL ";
        $query .= "          THEN G005_G.NAME2 ";
        $query .= "          ELSE G005_R.NAME2 ";
        $query .= "     END AS BUMON, ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     G008.NAME2 ";
        } else {
            $query .= "     '' ";
        }
        $query .= "      AS CERTIF_SCHOOL_KIND_NUM, ";
        $query .= "     CKIND.CERTIF_DIV ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT AS CISSUE ";
        $query .= "     LEFT JOIN CERTIF_KIND_MST CKIND ON CISSUE.CERTIF_KINDCD = CKIND.CERTIF_KINDCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '{$year}' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND CISSUE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST G005_R ON G005_R.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_R.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND G005_R.NAMESPARE2 = REGD.COURSECD ";
        $query .= "          AND G005_R.NAMESPARE3 = REGD.MAJORCD ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     LEFT JOIN NAME_MST G008 ON G008.NAMECD1 = 'G008' ";
            $query .= "          AND G008.NAME1 = CKIND.CERTIF_SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    GREGD.* ";
        $query .= "                FROM ";
        $query .= "                   (SELECT ";
        $query .= "                        GREGD_S.YEAR, ";
        $query .= "                        GREGD_S.SCHREGNO, ";
        $query .= "                        MAX(GREGD_S.SEMESTER) AS SEMESTER ";
        $query .= "                    FROM ";
        $query .= "                        (SELECT SCHREGNO, MAX(YEAR) AS YEAR FROM GRD_REGD_DAT GROUP BY SCHREGNO) YMAX, ";
        $query .= "                        GRD_REGD_DAT GREGD_S ";
        $query .= "                    WHERE ";
        $query .= "                        YMAX.YEAR = GREGD_S.YEAR ";
        $query .= "                        AND YMAX.SCHREGNO = GREGD_S.SCHREGNO ";
        $query .= "                    GROUP BY ";
        $query .= "                        GREGD_S.YEAR, ";
        $query .= "                        GREGD_S.SCHREGNO ";
        $query .= "                    ) GREGDYS, ";
        $query .= "                    GRD_REGD_DAT GREGD ";
        $query .= "                WHERE ";
        $query .= "                    GREGDYS.YEAR = GREGD.YEAR ";
        $query .= "                    AND GREGDYS.SEMESTER = GREGD.SEMESTER ";
        $query .= "                    AND GREGDYS.SCHREGNO = GREGD.SCHREGNO ";
        $query .= "                 ) AS GREGD ON CISSUE.SCHREGNO = GREGD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST G005_G ON G005_G.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_G.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND G005_G.NAMESPARE2 = GREGD.COURSECD ";
        $query .= "          AND G005_G.NAMESPARE3 = GREGD.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     CISSUE.YEAR = '{$year}' ";
        $query .= "     AND CISSUE.CERTIF_INDEX = '{$certifIndex}' ";
        return $query;
    }

    //証明書最終番号情報を取得
    //certif_no_8keta == 1の場合、使用する発行番号を生成して返す
    public function getCertifMaxnumber($cd1, $model, $certifIndex)
    {
        $db = Query::dbCheckOut();
        if ($model->Properties["certif_no_8keta"] == "1") {
            $query = knjg010Query::getBumonCeritfDiv($model, $cd1, $certifIndex);
            $issueRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $yearSimo2 = substr($cd1, 2, 2);
            $bumon = $issueRow["BUMON"];
            $bumon = $bumon ? $bumon : "0";
            $certifDiv = $issueRow["CERTIF_DIV"];
            $certifDiv = $certifDiv ? $certifDiv: "0";
            $certifKindcd = $issueRow["CERTIF_KINDCD"];
            $certifSchoolKindNum = $issueRow["CERTIF_SCHOOL_KIND_NUM"];
            $certifDiv = $certifDiv ? $certifDiv: "0";

            $renbanMae = ""; // 連番より前の部
            $renbanAto = ""; // 連場より後の部
            $passSEQ = false;
            $seqKeta = 4; // 連番の桁数
            $totalKeta = 8; // 発行番号の桁
            if ($model->Properties["certif_no_format"]) {
                $formatsplit = preg_split("/\s*[+]\s*/", $model->Properties["certif_no_format"]); // " + "でsplit
                $totalKeta = 0;
                foreach ($formatsplit as $s) {
                    $s = trim($s);
                    $add = "";
                    if (preg_match("/^YEAR(:[0-9]+)?$/", $s)) {
                        // 年度。
                        $simoKeta = 0;
                        // ':'後の数値が年度の下桁数
                        $split2 = preg_split("/:/", $s);
                        if ($split2 != '') {
                            $simoKeta = $split2[1];
                        }
                        // 指定なしは4桁。5以上は指定なしと同様に4桁。
                        if ($simoKeta == '' || $simoKeta > 4) {
                            $simoKeta = strlen($cd1);
                        }
                        $yearSimo = substr($cd1, strlen($cd1) - (int)$simoKeta, $simoKeta);
                        $add .= $yearSimo;
                        $totalKeta += $simoKeta;
                    } elseif ($s == "BUMON") {
                        // 部門
                        $add.= $bumon;
                        $totalKeta += 1;
                    } elseif ($s == "CERTIF_DIV") {
                        // 証明書区分
                        $add = $certifDiv;
                        $totalKeta += 1;
                    } elseif (preg_match("/^SEQ(:[0-9]+)?$/", $s)) {
                        // 連番。':'後の数値が桁数。指定なしは4桁
                        $split2 = preg_split("/:/", $s);
                        if ($split2 != '' && $split2[1]) {
                            $seqKeta = $split2[1];
                        }
                        $totalKeta += $seqKeta;
                        $passSEQ = true;
                    } elseif ($s == "SCHOOL_KIND") {
                        $add = $certifSchoolKindNum;
                        $totalKeta += 1;
                    }
                    if ($add) {
                        if ($passSEQ) {
                            $renbanAto .= $add;
                        } else {
                            $renbanMae .= $add;
                        }
                    }
                }
            } else {
                // 賢者のフォーマット : 年度下2桁、部門1桁、証明書区分1桁、連番4桁
                // "YEAR:2 + BUMON + CERTIF_DIV + SEQ:4";
                $renbanMae = $yearSimo2.$bumon.$certifDiv;
            }

            $query  = " SELECT ";
            $query .= "  MAX(REMARK1) as NUMBER ";
            $query .= " FROM ";
            $query .= "  CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "  YEAR = '" .$cd1 ."'";
            $query .= "  AND REMARK1 like '{$renbanMae}%{$renbanAto}' ";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $maxSEQ = 0;
            if ($row["NUMBER"]) {
                $maxSEQ = substr($row["NUMBER"], strlen($renbanMae), $seqKeta); // 連番部を抽出
                if ($seqKeta > 0 && $maxSEQ == pow(10, $seqKeta) - 1) {
                    $maxSEQ = 0;
                }
            }
            $row["NUMBER"] = sprintf($renbanMae."%0".$seqKeta."d".$renbanAto, $maxSEQ + 1);
            $row["NUMBER"] = sprintf("%0".$totalKeta."d", $row["NUMBER"]);
        } else {
            $query  = " SELECT ";
            $query .= "  COALESCE(MAX(INTEGER(CERTIF_NO)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "  CERTIF_ISSUE_DAT ";
            $query .= " WHERE ";
            $query .= "  YEAR = '" .$cd1 ."'";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        Query::dbCheckIn($db);
        return $row["NUMBER"];
    }

    //証明書最終番号情報をチェック
    public function getCertifMaxnumchk($year, $cd1)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "  COALESCE(CERTIF_NO,0) as NUMBER ";
        $query .= " FROM ";
        $query .= "  CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '" .$year ."' AND ";
        $query .= "  CERTIF_INDEX = '" .$cd1 ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["NUMBER"];
    }

    //--- INSERT
    public function &setInsertQuery($model, $fields, $year, $cmd)
    {
        $db = Query::dbCheckOut();
        $data = array();
        $data["SCHREGNO"][TEXT]             = $fields["SCHREGNO"];
        $data["CERTIF_KINDCD"][TEXT]        = substr($fields["CERTIF_KD"], 0, 3);
        $data["GRADUATE_FLG"][TEXT]         = $fields["GRADUATE_FLG"];
        $data["APPLYDATE"][TEXT]            = str_replace("/", "-", $fields["TK_DATE"]);
        $data["ISSUERNAME"][TEXT]           = $name;
        $data["ISSUECD"][TEXT]              = "0";
        $data["ISSUEDATE"][TEXT]            = $fields["ISSUEDATE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        if ($cmd === 'update') {
            $where  = " WHERE ";
            $where .= "     YEAR = '" .$year ."' ";
            $where .= "     AND CERTIF_INDEX = '" .$fields["CERTIF_INDEX"] ."' ";
            $query = Query::updateSQL($data, "CERTIF_ISSUE_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                 = $year;
            $data["CERTIF_INDEX"][TEXT]         = $fields["CERTIF_INDEX"];
            $query = Query::insertSQL($data, "CERTIF_ISSUE_DAT");
        }
        $db->query($query);

        $detailCnt = $db->getOne(knjg010Query::getDetailEach($year, $fields["CERTIF_INDEX"]));
        $data = array();
        $data["SCHREGNO"][TEXT] = $fields["SCHREGNO"];
        $data["REMARK1"][TEXT]  = $fields["REMARK1"];
        $data["REMARK2"][TEXT]  = $fields["KISAI_SEKI"];
        $data["REMARK3"][TEXT]  = $fields["KJ_OUT"];
        $data["REMARK4"][TEXT]  = $fields["MIRISYU"];
        $data["REMARK5"][TEXT]  = $fields["RISYU"];
        $data["REMARK6"][TEXT]  = $fields["FORM6"];
        $data["REMARK7"][TEXT]  = str_replace("/", "-", $fields["PR_DATE"]);
        $data["REMARK8"][TEXT]  = $fields["HYOUTEI"];
        $data["REMARK9"][TEXT]  = $fields["SONOTAJUUSYO"];
        $data["REMARK10"][TEXT] = $fields["GAIHYOU"];
        $data["REMARK11"][TEXT] = $fields["tyousasyoNotPrintAnotherAttendrec"];
        $data["REMARK12"][TEXT] = $fields["ENT_GRD_DATE_FORMAT"];
        $data["REMARK16"][TEXT] = $fields["PRINT_STAMP"];
        $data["REMARK17"][TEXT] = $fields["HANKI_NINTEI_FORM"];
        $data["REMARK18"][TEXT] = $fields["RYUGAKU_CREDIT"];
        $data["REMARK19"][TEXT] = $fields["SOGAKU_CREDIT"];
        $data["REMARK20"][TEXT] = $fields["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"];
        $data["REMARK21"][TEXT] = $fields["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"];
        $data["REMARK22"][TEXT] = $fields["KNJE070D_PRINTHEADERNAME"];
        $data["REMARK23"][TEXT] = $fields["GVAL_CALC_CHECK"];
        $data["REMARK24"][TEXT] = $fields["PRINT_AVG_RANK"];
        $data["REMARK25"][TEXT] = $fields["tyousasyo2020shojikouExtendsSelect"];
        $data["TYPE"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";
        if ($detailCnt == 0) {
            $data["YEAR"][TEXT]         = $year;
            $data["CERTIF_INDEX"][TEXT] = $fields["CERTIF_INDEX"];
            $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '" .$year ."' ";
            $where .= "     AND CERTIF_INDEX = '" .$fields["CERTIF_INDEX"] ."' ";
            $query = Query::updateSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT", $where);
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    public function getDetailEach($year, $certifIndex)
    {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$year ."'";
        $query .= "     AND CERTIF_INDEX = '" .$certifIndex ."'";

        return $query;
    }

    //証明書発行者情報を取得
    public function getissuername($cd1)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "  STAFFNAME_SHOW as NAME ";
        $query .= " FROM ";
        $query .= "  STAFF_MST ";
        $query .= " WHERE ";
        $query .= "  STAFFCD = '" .$cd1 ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["NAME"];
    }

    //--- 証明書発行
    public function &getUpdateQuery($year, $index, $name, $no, $date, $certif_no_div, $model)
    {
        $db = Query::dbCheckOut();
        if ($model->Properties["certif_no_8keta"] == "1") {
            $data["ISSUERNAME"][TEXT]           = $name;
            $data["ISSUECD"][TEXT] = "1";
            $data["ISSUEDATE"][TEXT] = str_replace("/", "-", $date);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where = " WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
            $query = Query::updateSQL($data, "CERTIF_ISSUE_DAT", $where);
            $db->query($query);

            $data = array();
            $data["REMARK1"][TEXT]              = $no;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where = " WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
            $query = Query::updateSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT", $where);
            $db->query($query);
        } else {
            $data["ISSUERNAME"][TEXT]           = $name;
            $data["ISSUECD"][TEXT] = "1";
            if ($certif_no_div == 0) {
                $data["CERTIF_NO"][NUMBER] = $no;
            }
            $data["ISSUEDATE"][TEXT] = str_replace("/", "-", $date);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where = " WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
            $query = Query::updateSQL($data, "CERTIF_ISSUE_DAT", $where);
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return;
    }


    //--- 証明書発行前情報削除
    public function &getDeleteQuery($year, $index)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM CERTIF_ISSUE_DAT "
                 ." WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
        $db->query($query);

        $query  = " DELETE FROM CERTIF_DETAIL_EACHTYPE_DAT "
                 ." WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //発行番号印刷不可をチェック
    public function getCertifSchool($year, $kindcd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     CERTIF_KINDCD, ";
        $query .= "     CERTIF_NO "; //0:印刷あり 1:印刷なし
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='".$year."' AND ";
        $query .= "     CERTIF_KINDCD='".$kindcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["CERTIF_NO"];
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    public function getSchoolDiv()
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN, ";
        $query .= "     T1.GVAL_CALC ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //発行番号重複チェック
    public function getCertifNoSyudouCheck($model, $flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        if ($flg === 'index') {
            $query .= "     CERTIF_INDEX ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     TYPE = '1' AND ";
        $query .= "     REMARK1 = '".$model->field["REMARK1"]."' ";
        if ($model->Properties["certifNoSyudouCheck"] == "2") {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 CERTIF_ISSUE_DAT S1 ";
            $query .= "             WHERE ";
            $query .= "                 T1.YEAR = S1.YEAR AND ";
            $query .= "                 T1.CERTIF_INDEX = S1.CERTIF_INDEX AND ";
            $query .= "                 S1.CERTIF_KINDCD = '".substr($model->field["CERTIF_KD"], 0, 3)."' ";
            $query .= "             ) ";
        }

        $check_cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $check_cnt;
    }

    //留学休学を含むか
    public function hasOffdaysOrAbroad($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        $check_cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $check_cnt;
    }

    public function &setCancelQuery($model)
    {
        $db = Query::dbCheckOut();
        $data = array();
        $data["REMARK15"][TEXT]             = "1";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where = " WHERE YEAR = '{$model->year}' AND CERTIF_INDEX = '{$model->field["CERTIF_INDEX"]}'";
        $query = Query::updateSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //印字履歴
    public function getMaxPrintNo($year, $certifIndex)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(PRINT_NO), 0) + 1 AS PRINT_NO ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_PRINT_ORDER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$year ."' ";

        return $query;
    }

    public function insertPrintHist($year, $certifIndex, $printNo)
    {
        $data = array();
        $data["YEAR"][TEXT]                 = $year;
        $data["PRINT_NO"][NUMBER]           = $printNo;
        $data["CERTIF_INDEX"][TEXT]         = $certifIndex;
        $data["PRINT_STAFFCD"][TEXT]        = STAFFCD;
        $data["PRINT_SYSDATE"][NUMBER]      = "sysdate()";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "CERTIF_ISSUE_PRINT_ORDER_DAT");

        return $query;
    }
}
