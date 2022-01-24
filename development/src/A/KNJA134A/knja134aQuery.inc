<?php

require_once('for_php7.php');


class knja134aQuery extends Query
{

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = 'A' ";

        return $query;
    }

    //クラス取得
    public function getHrClass($model)
    {
        if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.SCHOOL_KIND = 'A' ";
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
        } elseif ($model->field["HR_CLASS_TYPE"] == "2") {
            $query  = " SELECT DISTINCT ";
            if ($model->Properties["useFi_Hrclass"] != '1' && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     T1.GHR_CD AS VALUE, ";
                $query .= "     T1.GHR_NAME AS LABEL ";
            } else {
                $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
                $query .= "     T1.HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT T1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'A' ";
            } elseif ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER    = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'A' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER    = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //名称マスタ
    public function getNameMst($namecd1, $namecd2)
    {
        $query  = "SELECT * ";
        $query .= "FROM V_NAME_MST ";
        $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
        $query .= "AND NAMECD1='".$namecd1 ."'";
        $query .= "AND NAMECD2='".$namecd2 ."'";
        return $query;
    }

    //未履修科目出力　履修のみ科目出力
    public function getRisyuMirsyu($model)
    {
        //参照・更新可
        $query  = "";
        $query .= " SELECT NAMESPARE1, NAMESPARE2, NAMESPARE3 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'A027' ";
        $query .= " AND NAMECD2 = '107' ";
        return $query;
    }

    //データ取得
    public function getHtrainremarkDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //学習記録データ取得
    public function getStudyRec($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //出欠記録データ取得
    public function getAttendRec($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
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

    //INSERT
//    function &getInsertSql($db, $model) {
//
//        $array = $model->select_data["selectdata"];
//
//        $db->autoCommit(false);
//
//        if ($array[0] != "") {
//            for ($i = 0; $i < get_count($array); $i++){
//
//                $query  = " DELETE FROM ATTEST_OPINIONS_UNMATCH WHERE SCHREGNO = '".$array[$i]."' ";
//                $db->query($query);
//
//                $year = $db->getCol(knja134aQuery::getOpinionsWk($array[$i]));
//
//                foreach($year as $key => $val){
//
//                    $remark = $db->getRow(knja134aQuery::getHtrainremarkDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
//
//                    //学習記録データ
//                    $result = $db->query(knja134aQuery::getStudyRec($val, $array[$i]));
//                    $study = "";
//                    //教育課程対応
//                    if ($model->Properties["useCurriculumcd"] == '1') {
//                        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
//                            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
//                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
//                        }
//                    } else {
//                        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
//                            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
//                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
//                        }
//                    }
//
//                    //出欠記録データ
//                    $attend = $db->getRow(knja134aQuery::getAttendRec($val, $array[$i]), DB_FETCHMODE_ASSOC);
//
//                    $hash_remark = $model->makeHash($remark, $study, $attend);
//
//                    $opinion = $db->getRow(knja134aQuery::getOpinionsDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
//                    $hash_opinion = $opinion["OPINION"];
//
//                    if($hash_remark != $hash_opinion){
//                        $data["YEAR"][TEXT]                 = $val;
//                        $data["SCHREGNO"][TEXT]             = $array[$i];
//                        $data["FLG"][TEXT]                  = "1";
//                        $data["REGISTERCD"][TEXT]           = STAFFCD;
//                        $data["UPDATED"][FUNC]              = "sysdate()";
//
//                        $db->query(Query::insertSQL($data, "ATTEST_OPINIONS_UNMATCH"));
//                    }
//                }
//            }
//        }
//
//        $db->commit(); //トランザクションをコミットする。
//        return;
//    }

    public function getHrClassAuth($model, $year, $semester)
    {
        $auth_user = AUTHORITY;
        $staffcd = STAFFCD;
        $Chiteki = $model->field["CHITEKI"];
        //参照・更新可
        if ($auth_user == DEF_REFERABLE || $auth_user == DEF_UPDATABLE) {
            if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
                $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_HDAT T1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'A' ";
            } elseif ($model->field["HR_CLASS_TYPE"] == '2') {
                $query  = "SELECT T1.GHR_CD AS VALUE,T1.GHR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_GHR_HDAT T1 ";
                $query .= " LEFT JOIN ";
                $query .= "     SCHREG_REGD_GHR_DAT T3 ";
                $query .= "     ON T3.GHR_CD = T1.GHR_CD AND T3.SEMESTER=T1.SEMESTER AND T3.YEAR=T1.YEAR ";
            } else {
                $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_HDAT T1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'A' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND T1.SEMESTER='".$semester ."' AND ";
            if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
                $query .= "     EXISTS( ";
                $query .= "         SELECT ";
                $query .= "             * ";
                $query .= "         FROM ";
                $query .= "             SCHREG_REGD_DAT T3 ";
                $query .= "         INNER JOIN ";
                $query .= "             SCHREG_BASE_MST T4 ";
                $query .= "             ON T3.SCHREGNO = T4.SCHREGNO ";
                $query .= "         WHERE ";
                $query .= "             T3.YEAR = T1.YEAR AND ";
                $query .= "             T3.SEMESTER = T1.SEMESTER AND ";
                $query .= "             T3.GRADE = T1.GRADE AND ";
                $query .= "             T3.HR_CLASS = T1.HR_CLASS AND ";
                $query .= "             EXISTS( ";
                $query .= "                 SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T4.HANDICAP ";
                $query .= "            ) ";
                $query .= "    ) ";
            } elseif ($model->field["HR_CLASS_TYPE"] == '2') {
                $query .= "    EXISTS( ";
                $query .= "        SELECT ";
                $query .= "            * ";
                $query .= "        FROM ";
                $query .= "            SCHREG_BASE_MST T4 ";
                $query .= "        WHERE ";
                $query .= "            T4.SCHREGNO = T3.SCHREGNO AND ";
                $query .= "            EXISTS( ";
                $query .= "                SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T4.HANDICAP ";
                $query .= "            ) ";
                $query .= "    ) ";
            } else {
                $query .= "     EXISTS( ";
                $query .= "         SELECT ";
                $query .= "             * ";
                $query .= "         FROM ";
                $query .= "             SCHREG_REGD_DAT T3 ";
                $query .= "         INNER JOIN ";
                $query .= "             SCHREG_BASE_MST T4 ";
                $query .= "             ON T3.SCHREGNO = T4.SCHREGNO ";
                $query .= "         WHERE ";
                $query .= "             T3.YEAR = T1.YEAR AND ";
                $query .= "             T3.SEMESTER = T1.SEMESTER AND ";
                $query .= "             T3.GRADE = T1.GRADE AND ";
                $query .= "             T3.HR_CLASS = T1.HR_CLASS AND ";
                $query .= "             EXISTS( ";
                $query .= "                 SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T4.HANDICAP ";
                $query .= "             ) ";
                $query .= "     ) ";
            }
        }
        //参照・更新可（制限付き）
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT) {
            if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
                $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_HDAT T1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'A' ";
            } elseif ($model->field["HR_CLASS_TYPE"] == '2') {
                $query  = "SELECT T1.GHR_CD AS VALUE,T1.GHR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_GHR_HDAT T1 ";
                $query .= " LEFT JOIN ";
                $query .= "     SCHREG_REGD_GHR_DAT T3 ";
                $query .= "     ON T3.GHR_CD = T1.GHR_CD AND T3.SEMESTER=T1.SEMESTER AND T3.YEAR=T1.YEAR ";
            } else {
                $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
                $query .= "FROM SCHREG_REGD_HDAT T1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'A' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."' ";
            $query .= "AND T1.SEMESTER='".$semester ."' AND ";

            if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
                $query .= "     EXISTS( ";
                $query .= "         SELECT ";
                $query .= "             * ";
                $query .= "         FROM ";
                $query .= "             SCHREG_REGD_DAT T3 ";
                $query .= "         INNER JOIN ";
                $query .= "             SCHREG_BASE_MST T4 ";
                $query .= "             ON T3.SCHREGNO = T4.SCHREGNO ";
                $query .= "         WHERE ";
                $query .= "             T3.YEAR = T1.YEAR AND ";
                $query .= "             T3.SEMESTER = T1.SEMESTER AND ";
                $query .= "             T3.GRADE = T1.GRADE AND ";
                $query .= "             T3.HR_CLASS = T1.HR_CLASS AND ";
                $query .= "             EXISTS( ";
                $query .= "                 SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T4.HANDICAP ";
                $query .= "            ) ";
                $query .= "    ) ";
            } elseif ($model->field["HR_CLASS_TYPE"] == '2') {
                $query .= "    EXISTS( ";
                $query .= "        SELECT ";
                $query .= "            * ";
                $query .= "        FROM ";
                $query .= "            SCHREG_BASE_MST T4 ";
                $query .= "        WHERE ";
                $query .= "            T4.SCHREGNO = T3.SCHREGNO AND ";
                $query .= "            EXISTS( ";
                $query .= "                SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T4.HANDICAP ";
                $query .= "            ) ";
                $query .= "    ) ";
            } else {
                $query .= "     EXISTS( ";
                $query .= "         SELECT ";
                $query .= "             * ";
                $query .= "         FROM ";
                $query .= "             SCHREG_REGD_DAT T3 ";
                $query .= "         INNER JOIN ";
                $query .= "             SCHREG_BASE_MST T4 ";
                $query .= "             ON T3.SCHREGNO = T4.SCHREGNO ";
                $query .= "         WHERE ";
                $query .= "             T3.YEAR = T1.YEAR AND ";
                $query .= "             T3.SEMESTER = T1.SEMESTER AND ";
                $query .= "             T3.GRADE = T1.GRADE AND ";
                $query .= "             T3.HR_CLASS = T1.HR_CLASS AND ";
                $query .= "             EXISTS( ";
                $query .= "                 SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T4.HANDICAP ";
                $query .= "             ) ";
                $query .= "     ) ";
            }
            $query .= " AND (T1.TR_CD1 = '" .$staffcd ."' ";
            $query .= " OR T1.TR_CD2 = '" .$staffcd ."' ";
            $query .= " OR T1.TR_CD3 = '" .$staffcd ."' ";
            $query .= " OR T1.SUBTR_CD1 = '" .$staffcd ."' ";
            $query .= " OR T1.SUBTR_CD2 = '" .$staffcd ."' ";
            $query .= " OR T1.SUBTR_CD3 = '" .$staffcd ."') ";
        }
        if ($model->field["HR_CLASS_TYPE"] == '2') {
            $query .= " ORDER BY T1.GHR_CD ";
        } else {
            $query .= " ORDER BY T1.GRADE, T1.HR_CLASS ";
        }
        return $query;
    }
}
