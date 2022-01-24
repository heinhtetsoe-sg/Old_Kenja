<?php

require_once('for_php7.php');

class knja134pQuery extends Query
{

    //学年取得
    public function getGrade($model)
    {
        $Chiteki = empty($model->field['CHITEKI'])?'1':$model->field['CHITEKI'];
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOL_KIND = 'P' ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
        $query .= "         WHERE ";
        $query .= "             REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND REGD.GRADE = T1.GRADE ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    public function getNameMst($namecd1, $namecd2)
    {
        $query  = "SELECT NAME1 ";
        $query .= "FROM V_NAME_MST ";
        $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
        $query .= "AND NAMECD1='".$namecd1 ."'";
        $query .= "AND NAMECD2='".$namecd2 ."'";
        return $query;
    }

    //年組取得
    public function getHrClass($model)
    {
        $Chiteki = empty($model->field['CHITEKI'])?'1':$model->field['CHITEKI'];
        if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.SCHOOL_KIND = 'P' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR     = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD  = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } elseif ($model->field["HR_CLASS_TYPE"] == '2') {
            $query .= " SELECT ";
            $query .= "     T1.GHR_CD AS VALUE, ";
            $query .= "     T1.GHR_NAME LABEL ";
            $query .= " FROM SCHREG_REGD_GHR_HDAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (T1.YEAR, T1.SEMESTER, T1.GHR_CD) IN ( ";
            $query .= "         SELECT ";
            $query .= "             REGD.YEAR, REGD.SEMESTER, GHR.GHR_CD ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT REGD ";
            $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ";
            $query .= "                AND GDAT.GRADE = REGD.GRADE ";
            $query .= "                AND GDAT.SCHOOL_KIND = 'P' ";
            $query .= "         INNER JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ";
            $query .= "                AND GHR.YEAR = REGD.YEAR ";
            $query .= "                AND GHR.SEMESTER = REGD.SEMESTER ";
            $query .= "         INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
            $query .= "     ) ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (T1.TR_CD1 = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD2 = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD3 = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GHR_CD ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS) IN ( ";
            $query .= "                 SELECT ";
            $query .= "                     REGD.YEAR, REGD.SEMESTER, REGD.GRADE, REGD.HR_CLASS ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_DAT REGD ";
            $query .= "                     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ";
            $query .= "                         AND GDAT.GRADE = REGD.GRADE ";
            $query .= "                         AND GDAT.SCHOOL_KIND = 'P' ";
            $query .= "                     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
            $query .= "                     INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
            $query .= "              ) ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (T1.TR_CD1 = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD2 = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD3 = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //年組取得
    public function getHrClassAuth($model, $flg, $selectdata = "")
    {
        $Chiteki = empty($model->field['CHITEKI'])?'1':$model->field['CHITEKI'];
        if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.SCHOOL_KIND = 'P' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            if ($flg == 'list' && $selectdata) {
                $query .= "     AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS NOT IN('".implode("','", $selectdata)."') ";
            }
            if ($flg == 'select' && $selectdata) {
                $query .= "     AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS IN('".implode("','", $selectdata)."') ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR     = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD  = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= "     AND (T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS) IN ( ";
            $query .= "         SELECT ";
            $query .= "             T3.YEAR, T3.SEMESTER, T3.GRADE, T3.HR_CLASS ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT T3 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON T3.SCHREGNO = BASE.SCHREGNO ";
            $query .= "         INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } elseif ($model->field["HR_CLASS_TYPE"] == '2') {
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.GHR_CD AS VALUE, ";
            $query .= "     T1.GHR_NAME LABEL ";
            $query .= " FROM SCHREG_REGD_GHR_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GHR_DAT T3 ";
            $query .= "     ON T3.GHR_CD = T1.GHR_CD AND T3.SEMESTER = T1.SEMESTER AND T3.YEAR = T1.YEAR ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (T3.YEAR, T3.SEMESTER, T3.SCHREGNO) IN ( ";
            $query .= "        SELECT ";
            $query .= "            REGD.YEAR, REGD.SEMESTER, BASE.SCHREGNO ";
            $query .= "        FROM ";
            $query .= "            SCHREG_BASE_MST BASE ";
            $query .= "            INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ";
            $query .= "            INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ";
            $query .= "                AND GDAT.GRADE = REGD.GRADE ";
            $query .= "                AND GDAT.SCHOOL_KIND = 'P' ";
            $query .= "            INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
            $query .= "    ) ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (T1.TR_CD1    = '".STAFFCD."' ";
                $query .= "  OR  T1.TR_CD2    = '".STAFFCD."' ";
                $query .= "  OR  T1.TR_CD3    = '".STAFFCD."' ";
                $query .= "  OR  T1.SUBTR_CD1 = '".STAFFCD."' ";
                $query .= "  OR  T1.SUBTR_CD2 = '".STAFFCD."' ";
                $query .= "  OR  T1.SUBTR_CD3 = '".STAFFCD."') ";
            }
            if ($flg == 'list' && $selectdata) {
                $query .= "     AND T1.GHR_CD NOT IN('".implode("','", $selectdata)."') ";
            }
            if ($flg == 'select' && $selectdata) {
                $query .= "     AND T1.GHR_CD IN('".implode("','", $selectdata)."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GHR_CD ";
        } else {
            $query  = " SELECT DISTINCT";
            $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
            $query .= "     T2.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "         AND T2.GRADE = T1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SCHOOL_KIND = 'P' ";
            $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (T2.YEAR, T2.SEMESTER, T2.GRADE, T2.HR_CLASS) IN ( ";
            $query .= "         SELECT ";
            $query .= "             REGD.YEAR, REGD.SEMESTER, REGD.GRADE, REGD.HR_CLASS ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT REGD ";
            $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
            $query .= "         INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
            $query .= "     ) ";

            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (T2.TR_CD1    = '".STAFFCD."' ";
                $query .= "  OR  T2.TR_CD2    = '".STAFFCD."' ";
                $query .= "  OR  T2.TR_CD3    = '".STAFFCD."' ";
                $query .= "  OR  T2.SUBTR_CD1 = '".STAFFCD."' ";
                $query .= "  OR  T2.SUBTR_CD2 = '".STAFFCD."' ";
                $query .= "  OR  T2.SUBTR_CD3 = '".STAFFCD."') ";
            }
            if ($flg == 'list' && $selectdata) {
                $query .= "     AND T2.GRADE || T2.HR_CLASS NOT IN('".implode("','", $selectdata)."') ";
            }
            if ($flg == 'select' && $selectdata) {
                $query .= "     AND T2.GRADE || T2.HR_CLASS IN('".implode("','", $selectdata)."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //生徒一覧取得
    public function getStudentList($model, $flg, $selectdata = "")
    {
        $Chiteki = empty($model->field['CHITEKI'])?'1':$model->field['CHITEKI'];
        if ($model->field["HR_CLASS_TYPE"] == '2') {
            $query .= "SELECT GHR.GHR_CD || GHR.GHR_ATTENDNO || '-' || GHR.SCHREGNO AS VALUE, ";
            $query .= "       GHR.SCHREGNO || '　' || GHR.GHR_ATTENDNO || '番' || '　' || BASE.NAME_SHOW AS LABEL ";
            $query .= "FROM SCHREG_BASE_MST BASE ";
            $query .= "INNER JOIN SCHREG_REGD_GHR_DAT GHR ON BASE.SCHREGNO = GHR.SCHREGNO ";
            $query .= "INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
            $query .= "WHERE  GHR.YEAR = '" .CTRL_YEAR ."' ";
            $query .= "   AND GHR.SEMESTER = '" .CTRL_SEMESTER ."' ";
            $query .= "   AND GHR.GHR_CD = '" .$model->field["GRADE_HR_CLASS"] ."' ";
            if ($flg == 'list' && $selectdata) {
                $query .= "     AND GHR.GHR_CD || GHR.GHR_ATTENDNO || '-' || BASE.SCHREGNO NOT IN('".implode("','", $selectdata)."') ";
            }
            if ($flg == 'select' && $selectdata) {
                $query .= "     AND GHR.GHR_CD || GHR.GHR_ATTENDNO || '-' || BASE.SCHREGNO IN('".implode("','", $selectdata)."') ";
            }
            $query .= "ORDER BY GHR.GHR_CD, GHR.GHR_ATTENDNO";
        } else {
            $query .= "SELECT REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
            $query .= "       REGD.SCHREGNO || '　' || REGD.ATTENDNO || '番' || '　' || BASE.NAME_SHOW AS LABEL ";
            $query .= "FROM SCHREG_BASE_MST BASE ";
            $query .= "INNER JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ";
            $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
            $query .= "INNER JOIN NAME_MST ON NAMECD1 = 'A025' AND NAMESPARE3 = '" . $Chiteki . "' AND NAMECD2 = BASE.HANDICAP ";
            $query .= "WHERE  REGD.YEAR = '" .CTRL_YEAR ."' ";
            $query .= "   AND REGD.SEMESTER = '" .CTRL_SEMESTER ."' ";
            if ($model->field["GAKUNEN_KONGOU"] == "1") {
                $query .= "       AND GDAT.SCHOOL_KIND || '-' || REGD.HR_CLASS = '" .$model->field["GRADE_HR_CLASS"] ."' ";
            } else {
                $query .= "       AND REGD.GRADE || REGD.HR_CLASS = '" .$model->field["GRADE_HR_CLASS"] ."' ";
            }
            if ($flg == 'list' && $selectdata) {
                $query .= "     AND REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || BASE.SCHREGNO NOT IN('".implode("','", $selectdata)."') ";
            }
            if ($flg == 'select' && $selectdata) {
                $query .= "     AND REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || BASE.SCHREGNO IN('".implode("','", $selectdata)."') ";
            }
            $query .= "ORDER BY REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO";
        }
        return $query;
    }

/*
    //生徒一覧取得
    public function getHrclassStudentList($hrclass = "")
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode("','",$hrclass)."') ";
        return $query;
    }

    //データ取得
    public function getHtrainremarkPDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_P_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //データ取得
    public function getOpinionsDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.OPINION ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1, ";
        $query .= "     ATTEST_OPINIONS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.CHAGE_OPI_SEQ = T2.SEQ ";

        return $query;
    }

    //年度取得
    public function getOpinionsWk($schregno)
    {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //行動記録の取得
    public function getBehavior($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     ANNUAL, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DIV IN ('3', '4') ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //学習記録データ取得
    public function getStudyRec($year, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //出欠記録データ取得
    public function getAttendRec($year, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //INSERT
    public function &getInsertSql($db, $model) {

        $select_data = "";
        if ($model->field["OUTPUT"] == 2 && $model->select_data != "") {
            $hrclass = ($model->select_data) ? explode(',', $model->select_data) : array();
            $query = knja134pQuery::getHrclassStudentList($hrclass);
            $result = $db->query($query);
            $sep = "";
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $select_data = $select_data.$sep.$row["VALUE"];
                $sep = ",";
            }
            $result->free();
        } else {
            $select_data = $model->select_data;
        }
        $array = explode (",", $select_data);

        $db->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++){

                $query  = " DELETE FROM ATTEST_OPINIONS_UNMATCH WHERE SCHREGNO = '".$array[$i]."' ";
                $db->query($query);

                $year = $db->getCol(knja134pQuery::getOpinionsWk($array[$i]));

                foreach($year as $key => $val){

                    $remark = $db->getRow(knja134pQuery::getHtrainremarkPDat($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    //BEHAVIOR_DAT
                    $behavior = "";
                    $resultb = $db->query(knja134pQuery::getBehavior($val, $array[$i]));
                    while($rowb = $resultb->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $behavior .= $rowb["DIV"].$rowb["CODE"].$rowb["ANNUAL"];
                    }

                    //SCHREG_STUDYREC_DAT
                    $query = knja134pQuery::getStudyRec($val, $array[$i]);
                    $result = $db->query($query);
                    $study = "";
                    while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        //教育課程対応
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        } else {
                            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        }
                    }

                    //SCHREG_ATTENDREC_DAT
                    $attend = $db->getRow(knja134pQuery::getAttendRec($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    //現在データのHASHを取得
                    $makeHash = $model->makeHash($remark, $behavior, $study, $attend);

                    $opinion = $db->getRow(knja134pQuery::getOpinionsDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
                    $hash_opinion = $opinion["OPINION"];

                    if($makeHash != $hash_opinion){
                        $data["YEAR"][TEXT]                 = $val;
                        $data["SCHREGNO"][TEXT]             = $array[$i];
                        $data["FLG"][TEXT]                  = "1";
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][FUNC]              = "sysdate()";

                        $db->query(Query::insertSQL($data, "ATTEST_OPINIONS_UNMATCH"));
                    }
                }
            }
        }

        $db->commit(); //トランザクションをコミットする。
        return;
    }
 */
}
