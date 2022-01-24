<?php

require_once('for_php7.php');

class knjg060bQuery extends Query {
    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= "    ,NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }
    //リストtoリスト(右側)
    function getCategoryName($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || L1.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "     L1.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        $query .= "     L1.GRADE || L1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' AND ";
        $query .= "     T1.SCHREGNO NOT IN ('". implode("','", $model->select_data) ."') ";
        $query .= " ORDER BY ATTENDNO ";

        return $query;
    }
    //リストtoリスト(左側)
    function getCategorySelected($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || L1.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "     L1.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO IN ('". implode("','", $model->select_data) ."') ";
        $query .= " ORDER BY ATTENDNO ";

        return $query;
    }
    //新入生 リストtoリスト(右側)
    function getFreshmanName($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || L1.GRADE_NAME1 || '　' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT T1";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT L1 ";
        $query .= "         ON T1.ENTERYEAR = L1.YEAR ";
        $query .= "            AND T1.GRADE = L1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTERYEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "     T1.SCHREGNO NOT IN ('". implode("','", $model->select_data) ."') ";
        $query .= " ORDER BY T1.GRADE, T1.ATTENDNO ";

        return $query;
    }
    //新入生 リストtoリスト(左側)
    function getFreshmanSelected($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || L1.GRADE_NAME1 || '　' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT T1";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT L1 ";
        $query .= "         ON T1.ENTERYEAR = L1.YEAR ";
        $query .= "            AND T1.GRADE = L1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTERYEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "     T1.SCHREGNO IN ('". implode("','", $model->select_data) ."') ";
        $query .= " ORDER BY T1.GRADE, T1.ATTENDNO ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT REG_H.GRADE || REG_H.HR_CLASS AS VALUE, REG_H.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT REG_H ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                    $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                    $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE REG_H.YEAR = '" .CTRL_YEAR ."'";
            $query .= "AND REG_H.SEMESTER = '".CTRL_SEMESTER ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT REG_H.GRADE || REG_H.HR_CLASS AS VALUE, REG_H.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT REG_H ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                    $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                    $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE REG_H.YEAR   = '".CTRL_YEAR."' ";
            $query .= "AND REG_H.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= " AND (REG_H.TR_CD1 = '".STAFFCD."' ";
            $query .= "   OR REG_H.TR_CD2 = '".STAFFCD."' ";
            $query .= "   OR REG_H.TR_CD3 = '".STAFFCD."') ";
        }
        return $query;
    }

    //担当クラス
    function getHrclass($model) {
        $query  =" SELECT ";
        $query .="      REG_H.GRADE || '-' || REG_H.HR_CLASS AS VALUE ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_HDAT REG_H ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
            $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .=" WHERE ";
        $query .="         REG_H.YEAR     = '" .CTRL_YEAR ."' ";
        $query .="    AND (REG_H.TR_CD1   = '".STAFFCD."' ";
        $query .="    OR  REG_H.TR_CD2    = '".STAFFCD."' ";
        $query .="    OR  REG_H.TR_CD3    = '".STAFFCD."' ";
        $query .="    OR  REG_H.SUBTR_CD1 = '".STAFFCD."' ";
        $query .="    OR  REG_H.SUBTR_CD2 = '".STAFFCD."' ";
        $query .="    OR  REG_H.SUBTR_CD3 = '".STAFFCD."') ";

        return $query;
    }

    //名称マスタ
    function getNameMst($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'G004' ";
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //生徒情報取得(NOT USE)
    function getStudentInfoData($model) {
        list($and, $where) = knjg060bQuery::getStudentSearch($model);

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T2.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND VALUE(T1.ENT_DIV, '0') != '7' ";
        $query .= "     AND (VALUE(T1.GRD_DIV, '0') IN ('0', '4') ";
        $query .= "          OR (VALUE(T1.GRD_DIV, '0') IN ('1', '2', '3') ";
        $query .= "              AND T1.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."') ";
        $query .= "         ) ";
        $query .= "     ".$and." ".$where;

        $query .= " ), NAME_T AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'W006' ";

        $query .= " ), CERTIF_DETAIL_MAX AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(CERTIF_INDEX) AS CERTIF_INDEX ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND TYPE = '2' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        $query .= " ), CERTIF_DETAIL_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.REMARK1 AS STATION_FROM, ";
        $query .= "     T2.REMARK2 AS STATION_TO, ";
        $query .= "     T2.REMARK3 AS STATION_VIA ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_MAX T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT T2 ";
        $query .= "     ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.CERTIF_INDEX = T2.CERTIF_INDEX ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     C1.STATION_FROM, ";
        $query .= "     C1.STATION_TO, ";
        $query .= "     C1.STATION_VIA ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CERTIF_DETAIL_DATA C1 ";
        $query .= "     ON T1.SCHREGNO = C1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //生徒情報取得(NOT USE)
    function getStudentInfoData2($model, $schregnos) {
        $grade  = substr($model->grade_hr_class,0,2);
        $class  = substr($model->grade_hr_class,2,3);
        $schListStr = "";
        if (is_array($schregnos)) {
            if (get_count($schregnos) > 1) {
                $schListStr = " IN ('".implode("', '", $schregnos)."') ";
            } else if (get_count($schregnos) == 1) {
                $schListStr = " = '".$schregnos[0]."' ";
            }
        } else {
            $schListStr = " = '".$schregnos."' ";
        }

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T2.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     ON   T2.YEAR     = '" .CTRL_YEAR ."' ";
        $query .= "     AND  T2.SEMESTER = '".CTRL_SEMESTER ."' ";
        $query .= "     AND  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= "     ON T3.YEAR       = '" .CTRL_YEAR ."' ";
        $query .= "     AND  T3.SEMESTER = '".CTRL_SEMESTER ."' ";
        $query .= "     AND  T3.GRADE    = T2.GRADE ";
        $query .= "     AND  T3.HR_CLASS = T2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO ".$schListStr." ";
        $query .= " ORDER BY ";
        $query .= "    T2.GRADE, ";
        $query .= "    T2.HR_CLASS, ";
        $query .= "    T2.ATTENDNO ";

        return $query;
    }
    //生徒情報取得(新入生情報)
    function getFreshmanInfoData2($model, $schregnos) {
        $grade  = substr($model->grade_hr_class,0,2);
        $class  = substr($model->grade_hr_class,2,3);
        $schListStr = "";
        if (is_array($schregnos)) {
            if (get_count($schregnos) > 1) {
                $schListStr = " IN ('".implode("', '", $schregnos)."') ";
            } else if (get_count($schregnos) == 1) {
                $schListStr = " = '".$schregnos[0]."' ";
            }
        } else {
            $schListStr = " = '".$schregnos."' ";
        }

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     '' ENT_DATE, ";
        $query .= "     '' REMARK1, ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= "     ON T2.YEAR       = '" .CTRL_YEAR ."' ";
        $query .= "     AND  T2.SEMESTER = '".CTRL_SEMESTER ."' ";
        $query .= "     AND  T1.GRADE    = T2.GRADE ";
        $query .= "     AND  T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.ENTERYEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO ".$schListStr." ";
        $query .= " ORDER BY ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.SCHREGNO ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfoCnt($model) {
        list($and, $where) = knjg060bQuery::getStudentSearch($model);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T2.YEAR     = '" .CTRL_YEAR ."' ";
        $query .= "     AND T2.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND VALUE(T1.ENT_DIV, '0') != '7' ";
        $query .= "     AND (VALUE(T1.GRD_DIV, '0') IN ('0', '4') ";
        $query .= "          OR (VALUE(T1.GRD_DIV, '0') IN ('1', '2', '3') ";
        $query .= "              AND T1.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."') ";
        $query .= "         ) ";
        $query .= "     ".$and." ".$where;

        return $query;
    }

    //生徒情報取得
    function getStudentSearch($model) {
        $and = "";
        $where = "";
        foreach ($model->search as $key => $val) {
            if ($val) {
                if ($key == "NAME" || $key == "NAME_KANA") {
                    $where .= $and." T1.".$key." LIKE'".$val."%' ";
                    $and = "AND";
                } else if ($key == "SCHREGNO1") {
                    $where .= $and." T1.SCHREGNO >= '".$val."' ";
                    $and = "AND";
                } else if ($key == "SCHREGNO2") {
                    $where .= $and." T1.SCHREGNO <= '".$val."' ";
                    $and = "AND";
                } else if ($key == "EXE_YEAR") {
                    $where .= $and." T2.YEAR = '".$val."' ";
                    $and = "AND";
                } else {
                    $where .= $and." T1.".$key." = '".$val."' ";
                    $and = "AND";
                }
            }
        }

        //権限設定
        if ((AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) && !$model->search["BELONGING_DIV"]) {
            $grade = join("','", $model->grade);
            $where .= $and." (T2.GRADE = '".$model->belonging["BELONGING_DIV"]."' ";
            $where .= "    OR T2.GRADE IN ('".$grade."')) ";
            $and = "AND";
        }

        return array($and, $where);
    }

    //証明書発行データ連番取得
    function getCertifIssueEachtypeMAX($year) {
        $query  = " SELECT ";
        $query .= "     MAX(INT(CERTIF_INDEX)) ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //証明書発行データ発行番号取得
    function getCertifIssueEachtypePrintMAX($TYPE, $year) {
        $query  = " SELECT ";
        $query .= "     MAX(CERTIF_NO) ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $ret = array();
        $schregnoArray = array();
        $seitoArray = array();
        $tugakuArray = array();
        $untinArray = array();
        $up_data = ($model->field["PRINT_CHECK"]) ? $model->field["PRINT_CHECK"]: array();
        foreach ($up_data as $val) {
            list($index, $schregno) = preg_split("/:/", $val);
            if ($model->studentRadio == "2") {
                $schInfo = $db->getRow(knjg060bQuery::getCertifKindcd($schregno), DB_FETCHMODE_ASSOC);
            } else {
                $schInfo = $db->getRow(knjg060bQuery::getFreshmanCertifKindcd($schregno), DB_FETCHMODE_ASSOC);
            }

            //生徒証
            if ($this->print_div == "1") { //今の所「print_div」という値は2が固定でされている(理由は不明)
                $CERTIF_INDEX = $db->getOne(knjg060bQuery::getCertifIssueEachtypeMAX(CTRL_YEAR));
                $CERTIF_NO   = $db->getOne(knjg060bQuery::getCertifIssueEachtypePrintMAX("9", CTRL_YEAR));

                //証明書種別別発行データ
                $data = array();
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT]  = $CERTIF_INDEX+1;
                $data["SCHREGNO"][TEXT]      = $schregno;
                $data["CERTIF_KINDCD"][TEXT] = "308";
                $data["GRADUATE_FLG"][TEXT]  = "0";
                $data["APPLYDATE"][TEXT]     = CTRL_DATE;
                $data["ISSUERNAME"][TEXT]    = STAFFNAME_SHOW;
                $data["ISSUECD"][TEXT]       = "1";
                $data["CERTIF_NO"][NUMBER]   = intval($CERTIF_NO)+1;
                $data["ISSUEDATE"][TEXT]     = str_replace("/", "-", $model->print_date);

                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_ISSUE_DAT");
                $db->query($query);

                //証明書種別別詳細データ
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT] = $CERTIF_INDEX+1;
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["REMARK4"][TEXT]      = str_replace("/", "-", $model->field["ENT_DATE"][$index]);
                $data["REMARK5"][TEXT]      = str_replace("/", "-", $model->field["GRD_SCHEDULE_DATE"][$index]);

                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
                $db->query($query);

                $seitoArray[] = array("SEITO" => $CERTIF_INDEX+1 );

            }
            //通学証明書
            if ($this->print_div == "2") {
                $CERTIF_INDEX = $db->getOne(knjg060bQuery::getCertifIssueEachtypeMAX(CTRL_YEAR));

                $CERTIF_NO = $db->getOne(knjg060bQuery::getCertifIssueEachtypePrintMAX("2", CTRL_YEAR));

                if ($schInfo["SCHOOL_KIND"] == "P") {
                    $certifKind = "119";
                } else if ($schInfo["SCHOOL_KIND"] == "J") {
                    $certifKind = "118";
                } else if ($schInfo["SCHOOL_KIND"] == "H") {
                    $certifKind = "113";
                }
                //証明書種別別発行データ
                $data = array();
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT]  = $CERTIF_INDEX+1;
                $data["SCHREGNO"][TEXT]      = $schregno;
                $data["CERTIF_KINDCD"][TEXT] = $certifKind;
                $data["GRADUATE_FLG"][TEXT]  = "0";
                $data["APPLYDATE"][TEXT]     = CTRL_DATE;
                $data["ISSUERNAME"][TEXT]    = STAFFNAME_SHOW;
                $data["ISSUECD"][TEXT]       = "1";
                $data["CERTIF_NO"][NUMBER]   = intval($CERTIF_NO)+1;
                $data["ISSUEDATE"][TEXT]     = str_replace("/", "-", $model->print_date);

                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_ISSUE_DAT");
                $db->query($query);

                //証明書種別別詳細データ
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["TYPE"][TEXT]         = "2";
                $data["CERTIF_INDEX"][TEXT] = $CERTIF_INDEX+1;
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["REMARK1"][TEXT]      = $model->field["STATION_FROM"][$index];
                $data["REMARK2"][TEXT]      = $model->field["STATION_TO"][$index];
                $data["REMARK3"][TEXT]      = $model->field["STATION_VIA"][$index];
                $data["REMARK4"][TEXT]      = str_replace("/", "-", $model->str_date);
                $data["REMARK5"][TEXT]      = str_replace("/", "-", $model->end_date);
                $data["REMARK6"][TEXT]      = $model->month_div;
                // 立志舎の場合、電車用・都バス用を登録
                if ($model->isRisshi == "1") {
                    $data["REMARK7"][TEXT]      = $model->ticketRadio;
                }

                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
                $db->query($query);


                $tugakuArray[] = array("TUGAKU" => $CERTIF_INDEX+1);
            }
            //学校学生生徒旅客運賃割引証
            if ($this->print_div == "3") { //今の所「print_div」という値は2が固定でされている(理由は不明)
                $CERTIF_INDEX = $db->getOne(knjg060bQuery::getCertifIssueEachtypeMAX(CTRL_YEAR));

                $CERTIF_NO = $db->getOne(knjg060bQuery::getCertifIssueEachtypePrintMAX("2", CTRL_YEAR));

                //証明書種別別発行データ
                $data = array();
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT]  = $CERTIF_INDEX+1;
                $data["SCHREGNO"][TEXT]      = $schregno;
                $data["CERTIF_KINDCD"][TEXT] = "307";
                $data["GRADUATE_FLG"][TEXT]  = "0";
                $data["APPLYDATE"][TEXT]     = CTRL_DATE;
                $data["ISSUERNAME"][TEXT]    = STAFFNAME_SHOW;
                $data["ISSUECD"][TEXT]       = "1";
                $data["CERTIF_NO"][NUMBER]   = intval($CERTIF_NO)+1;
                $data["ISSUEDATE"][TEXT]     = str_replace("/", "-", $model->print_date);

                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_ISSUE_DAT");
                $db->query($query);

                //証明書種別別詳細データ
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT] = $CERTIF_INDEX+1;
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["REMARK1"][TEXT]      = $model->field["STATION_FROM"][$index];
                $data["REMARK2"][TEXT]      = $model->field["STATION_TO"][$index];
                $data["REMARK3"][TEXT]      = $model->field["STATION_VIA"][$index];
                $data["REMARK4"][TEXT]      = str_replace("/", "-", $model->str_date);
                $data["REMARK5"][TEXT]      = str_replace("/", "-", $model->end_date);

                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
                $db->query($query);

                $untinArray[] = array("UNTIN" => $CERTIF_INDEX+1);
            }
            $schregnoArray[] = array("SCHREGNO" => $schregno);
        }

        $ret = array($schregnoArray,
                     $seitoArray,
                     $tugakuArray,
                     $untinArray
                     );

        $db->commit();
        Query::dbCheckIn($db);

        return $ret;
    }

    //生徒情報
    function getCertifKindcd($schregNo) {
        $query  = " SELECT ";
        $query .= "    T1.GRADE, ";
        $query .= "    L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT T1  ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR  ";
        $query .= "         AND L1.GRADE = T1.GRADE  ";
        $query .= " WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregNo."' ";
        $query .= "    AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }
    //生徒情報(新入生用)
    function getFreshmanCertifKindcd($schregNo) {
        $query  = " SELECT ";
        $query .= "    T1.GRADE, ";
        $query .= "    L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    FRESHMAN_DAT T1  ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT L1 ON T1.ENTERYEAR = L1.YEAR  ";
        $query .= "         AND T1.GRADE = L1.GRADE  ";
        $query .= " WHERE ";
        $query .= "        T1.SCHREGNO = '".$schregNo."' ";
        $query .= "    AND L1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学年数上限値取得
    function getMaxGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE_HVAL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";

        return $query;
    }

    //生徒情報取得
    function getName($schregno)
    {
        $query  = "SELECT ";
        $query .= "    NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //生徒
    function getSchStation($model, $schregn) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND TYPE = '2' ";
        if ($schregn == "") {
            $query .= "     AND SCHREGNO = '{$model->subSchregno}' ";
        } else {
            $query .= "     AND SCHREGNO = '{$schregn}' ";
        }
        if ($model->isRisshi == "1") {
            $query .= "     AND REMARK7 = '{$model->ticketRadio}' ";
        }
        $query .= " ORDER BY CERTIF_INDEX DESC ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";
        return $query;
    }

    //生徒環境調査データ取得
    function getSchreg_envir_dat($model, $schregn = "") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        if ($schregn == "") {
            $query .= "     SCHREGNO = '{$model->subSchregno}' ";
        } else {
            $query .= "     SCHREGNO = '{$schregn}' ";
        }
        return $query;
    }

    //路線名、駅名を取得
    function getStationName($station_cd) {
        $query  = " SELECT ";
        $query .= "     STATION_NAME, ";
        $query .= "     LINE_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     STATION_CD = '{$station_cd}' ";

        return $query;
    }

    //駅名を取得
    function getStationName2($station_cd, $linecd) {
        $query  = " SELECT ";
        $query .= "     STATION_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     STATION_CD = '".$station_cd."' ";
        $query .= "     AND LINE_CD = '".$linecd."' ";

        return $query;
    }

    function getStationList_Josya($model, $schregno, $sitei="") {
        $query = "";
        if ($sitei == "") {
            $query .= " WITH AL_TYPE_UNION_BASE AS ( ";
        }
        if ($sitei == "" || $sitei == "1") {
            $query .= " SELECT ";
            $query .= "      '11' AS NO, ";
            $query .= "      T1.JOSYA_1 AS VALUE, ";
            $query .= "      T1.JOSYA_1 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、下車も含める
            $query .= " UNION ";
            $query .= " select ";
            $query .= "      '12' AS NO, ";
            $query .= "      T1.GESYA_1 AS VALUE, ";
            $query .= "      T1.GESYA_1 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "2") {
            $query .= " SELECT ";
            $query .= "      '21' AS NO, ";
            $query .= "      T1.JOSYA_2 AS VALUE, ";
            $query .= "      T1.JOSYA_2 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、下車も含める
            $query .= " UNION ";
            $query .= " select ";
            $query .= "      '22' AS NO, ";
            $query .= "      T1.GESYA_2 AS VALUE, ";
            $query .= "      T1.GESYA_2 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "3") {
            $query .= " SELECT ";
            $query .= "      '31' AS NO, ";
            $query .= "      T1.JOSYA_3 AS VALUE, ";
            $query .= "      T1.JOSYA_3 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、下車も含める
            $query .= " UNION ";
            $query .= " select ";
            $query .= "      '32' AS NO, ";
            $query .= "      T1.GESYA_3 AS VALUE, ";
            $query .= "      T1.GESYA_3 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "4") {
            $query .= " SELECT ";
            $query .= "      '41' AS NO, ";
            $query .= "      T1.JOSYA_4 AS VALUE, ";
            $query .= "      T1.JOSYA_4 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、下車も含める
            $query .= " UNION ";
            $query .= " select ";
            $query .= "      '42' AS NO, ";
            $query .= "      T1.GESYA_4 AS VALUE, ";
            $query .= "      T1.GESYA_4 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "5") {
            $query .= " SELECT ";
            $query .= "      '51' AS NO, ";
            $query .= "      T1.JOSYA_5 AS VALUE, ";
            $query .= "      T1.JOSYA_5 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、下車も含める
            $query .= " UNION ";
            $query .= " select ";
            $query .= "      '52' AS NO, ";
            $query .= "      T1.GESYA_5 AS VALUE, ";
            $query .= "      T1.GESYA_5 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "6") {
            $query .= " SELECT ";
            $query .= "      '61' AS NO, ";
            $query .= "      T1.JOSYA_6 AS VALUE, ";
            $query .= "      T1.JOSYA_6 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、下車も含める
            $query .= " UNION ";
            $query .= " select ";
            $query .= "      '62' AS NO, ";
            $query .= "      T1.GESYA_6 AS VALUE, ";
            $query .= "      T1.GESYA_6 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "7") {
            $query .= " SELECT ";
            $query .= "      '71' AS NO, ";
            $query .= "      T1.JOSYA_7 AS VALUE, ";
            $query .= "      T1.JOSYA_7 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時、7の下車は含めない
            $query .= " ) ";
            $query .= " SELECT LABEL, VALUE, MIN(NO) AS NO FROM AL_TYPE_UNION_BASE WHERE VALUE IS NOT NULL GROUP BY LABEL, VALUE ORDER BY NO "; //空行や同じ選択肢が複数出ないよう、一意にする。
        }

        return $query;
    }
    function getStationList_Gesya($model, $schregno, $sitei="") {
        $query = "";
        if ($sitei == "") {
            $query .= " WITH AL_TYPE_UNION_BASE AS ( ";
            //全体取得時、1の乗車は含めない
        }
        if ($sitei == "" || $sitei == "1") {
            $query .= " select ";
            $query .= "      '12' AS NO, ";
            $query .= "      T1.GESYA_1 AS VALUE, ";
            $query .= "      T1.GESYA_1 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、乗車も含める
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "      '21' AS NO, ";
            $query .= "      T1.JOSYA_2 AS VALUE, ";
            $query .= "      T1.JOSYA_2 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "2") {
            $query .= " select ";
            $query .= "      '22' AS NO, ";
            $query .= "      T1.GESYA_2 AS VALUE, ";
            $query .= "      T1.GESYA_2 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、乗車も含める
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "      '31' AS NO, ";
            $query .= "      T1.JOSYA_3 AS VALUE, ";
            $query .= "      T1.JOSYA_3 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "3") {
            $query .= " select ";
            $query .= "      '32' AS NO, ";
            $query .= "      T1.GESYA_3 AS VALUE, ";
            $query .= "      T1.GESYA_3 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、乗車も含める
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "      '41' AS NO, ";
            $query .= "      T1.JOSYA_4 AS VALUE, ";
            $query .= "      T1.JOSYA_4 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "4") {
            $query .= " select ";
            $query .= "      '42' AS NO, ";
            $query .= "      T1.GESYA_4 AS VALUE, ";
            $query .= "      T1.GESYA_4 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、乗車も含める
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "      '51' AS NO, ";
            $query .= "      T1.JOSYA_5 AS VALUE, ";
            $query .= "      T1.JOSYA_5 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "5") {
            $query .= " select ";
            $query .= "      '52' AS NO, ";
            $query .= "      T1.GESYA_5 AS VALUE, ";
            $query .= "      T1.GESYA_5 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、乗車も含める
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "      '61' AS NO, ";
            $query .= "      T1.JOSYA_6 AS VALUE, ";
            $query .= "      T1.JOSYA_6 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "6") {
            $query .= " select ";
            $query .= "      '62' AS NO, ";
            $query .= "      T1.GESYA_6 AS VALUE, ";
            $query .= "      T1.GESYA_6 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            //全体取得時は、乗車も含める
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "      '71' AS NO, ";
            $query .= "      T1.JOSYA_7 AS VALUE, ";
            $query .= "      T1.JOSYA_7 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
            $query .= " UNION ";
        }
        if ($sitei == "" || $sitei == "7") {
            $query .= " select ";
            $query .= "      '72' AS NO, ";
            $query .= "      T1.GESYA_7 AS VALUE, ";
            $query .= "      T1.GESYA_7 AS LABEL ";
            $query .= " FROM ";
            $query .= "      SCHREG_ENVIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "      T1.SCHREGNO = '".$schregno."' ";
        }
        if ($sitei == "") {
            $query .= " ) ";
            $query .= " SELECT LABEL, VALUE, MAX(NO) AS NO FROM AL_TYPE_UNION_BASE WHERE VALUE IS NOT NULL GROUP BY LABEL, VALUE ORDER BY NO "; //空行や同じ選択肢が複数出ないよう、一意にする。
        }

        return $query;
    }
}
?>
