<?php

require_once('for_php7.php');

class knjg060aQuery extends Query {
    //リストtoリスト
    function getCategoryName($model, $div) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        if ($model->field["GROUP"] == "2") {
            $query .= "     T1.SCHREGNO || '　' || L2.HR_NAME || '　' || L1.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
        } else {
            $query .= "     T1.SCHREGNO || '　' || L1.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_HDAT L2 ON L2.YEAR = L1.YEAR ";
        $query .= "         AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L2.GRADE = L1.GRADE ";
        $query .= "         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "     L1.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        if ($div == "right") {
            if ($model->field["GROUP"] == "2") {
                $query .= "     L1.GRADE = '{$model->field["GRADE_HR_CLASS"]}' AND ";
            } else {
                $query .= "     L1.GRADE || L1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' AND ";
            }
            $query .= "     T1.SCHREGNO NOT IN ('". implode("','", $model->select_data) ."') ";
        } else {
            $query .= "     T1.SCHREGNO IN ('". implode("','", $model->select_data) ."') ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (L2.TR_CD1 = '".STAFFCD."' ";
            $query .= "   OR L2.TR_CD2 = '".STAFFCD."' ";
            $query .= "   OR L2.TR_CD3 = '".STAFFCD."' ";
            $query .= "   OR L2.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "   OR L2.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "   OR L2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY TRANSLATE_KANA(T1.NAME_KANA), T1.NAME_KANA, L1.GRADE, L1.HR_CLASS, L1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト
    function getSchregRegdDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, VALUE(BASE.NAME_KANA, '') || T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS COMPARE_VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        if ($model->field["GROUP"] == "2") {
            $query .= "     (T1.GRADE = '{$model->field["GRADE_HR_CLASS"]}' ";
            $query .= "   OR T1.SCHREGNO IN ('". implode("','", $model->select_data) ."')) ";
        } else {
            $query .= "     (T1.GRADE || T1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
            $query .= "   OR T1.SCHREGNO IN ('". implode("','", $model->select_data) ."')) ";
        }

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            if ($model->field["GROUP"] == "2") {
                $query  = "SELECT DISTINCT REG_H.GRADE AS VALUE, GDAT.GRADE_NAME1 AS LABEL ";
            } else {
                $query  = "SELECT REG_H.GRADE || REG_H.HR_CLASS AS VALUE, REG_H.HR_NAME AS LABEL ";
            }
            $query .= "FROM SCHREG_REGD_HDAT REG_H ";
            $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REG_H.YEAR AND GDAT.GRADE = REG_H.GRADE ";
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
            $query .= "  AND REG_H.SEMESTER = '".CTRL_SEMESTER ."'";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
                $query .= " AND (REG_H.TR_CD1 = '".STAFFCD."' ";
                $query .= "   OR REG_H.TR_CD2 = '".STAFFCD."' ";
                $query .= "   OR REG_H.TR_CD3 = '".STAFFCD."' ";
                $query .= "   OR REG_H.SUBTR_CD1 = '".STAFFCD."' ";
                $query .= "   OR REG_H.SUBTR_CD2 = '".STAFFCD."' ";
                $query .= "   OR REG_H.SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY VALUE ";
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
        $query .="     OR  REG_H.TR_CD2    = '".STAFFCD."' ";
        $query .="     OR  REG_H.TR_CD3    = '".STAFFCD."' ";
        $query .="     OR  REG_H.SUBTR_CD1 = '".STAFFCD."' ";
        $query .="     OR  REG_H.SUBTR_CD2 = '".STAFFCD."' ";
        $query .="     OR  REG_H.SUBTR_CD3 = '".STAFFCD."') ";

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
        list($and, $where) = knjg060aQuery::getStudentSearch($model);

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.GRADE, ";
        $query .= "     EGHIST.ENT_DATE, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
        $query .= "     INNER JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T2.SCHREGNO AND EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         T2.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND VALUE(EGHIST.ENT_DIV, '0') != '7' ";
        $query .= "     AND (VALUE(EGHIST.GRD_DIV, '0') IN ('0', '4') ";
        $query .= "          OR (VALUE(EGHIST.GRD_DIV, '0') IN ('1', '2', '3') ";
        $query .= "              AND EGHIST.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."') ";
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
    function getStudentInfoData2($model, $schregno) {

        $query .= " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     EGHIST.ENT_DATE, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     T2.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T2.YEAR AND HDAT.SEMESTER = T2.SEMESTER AND HDAT.GRADE = T2.GRADE AND HDAT.HR_CLASS = T2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T2.SCHREGNO AND EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T2.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T2.YEAR     = '" .CTRL_YEAR ."' ";
        $query .= "     AND T2.SEMESTER = '". CTRL_SEMESTER ."' ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfoCnt($model) {
        list($and, $where) = knjg060aQuery::getStudentSearch($model);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
        $query .= "     INNER JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T2.SCHREGNO AND EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         T2.YEAR     = '" .CTRL_YEAR ."' ";
        $query .= "     AND T2.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND VALUE(EGHIST.ENT_DIV, '0') != '7' ";
        $query .= "     AND (VALUE(EGHIST.GRD_DIV, '0') IN ('0', '4') ";
        $query .= "          OR (VALUE(EGHIST.GRD_DIV, '0') IN ('1', '2', '3') ";
        $query .= "              AND EGHIST.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."') ";
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
                    $where .= $and." BASE.".$key." LIKE'".$val."%' ";
                    $and = "AND";
                } else if ($key == "SCHREGNO1") {
                    $where .= $and." BASE.SCHREGNO >= '".$val."' ";
                    $and = "AND";
                } else if ($key == "SCHREGNO2") {
                    $where .= $and." BASE.SCHREGNO <= '".$val."' ";
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

//        //権限設定
//        if ((AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) && !$model->search["BELONGING_DIV"]) {
//            $grade = join("','", $model->grade);
//            $where .= $and." (T2.GRADE = '".$model->belonging["BELONGING_DIV"]."' ";
//            $where .= "    OR T2.GRADE IN ('".$grade."')) ";
//            $and = "AND";
//        }

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
            $schInfo = $db->getRow(knjg060aQuery::getCertifKindcd($schregno), DB_FETCHMODE_ASSOC);
            //生徒証
            if ($this->print_div == "1") { //今の所「print_div」という値は2が固定でされている(理由は不明)
                $CERTIF_INDEX = $db->getOne(knjg060aQuery::getCertifIssueEachtypeMAX(CTRL_YEAR));
                $CERTIF_NO   = $db->getOne(knjg060aQuery::getCertifIssueEachtypePrintMAX("9", CTRL_YEAR));

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
                $CERTIF_INDEX = $db->getOne(knjg060aQuery::getCertifIssueEachtypeMAX(CTRL_YEAR));

                $CERTIF_NO = $db->getOne(knjg060aQuery::getCertifIssueEachtypePrintMAX("2", CTRL_YEAR));

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
                //$data["REMARK1"][TEXT]      = $model->field["STATION_FROM"][$index];
                //$data["REMARK2"][TEXT]      = $model->field["STATION_TO"][$index];
                //$data["REMARK3"][TEXT]      = $model->field["STATION_VIA"][$index];
                $sta_from = explode(",", $model->field["STATION_FROM"]);
                $sta_to = explode(",", $model->field["STATION_TO"]);
                $sta_via = explode(",", $model->field["STATION_VIA"]);
                $data["REMARK1"][TEXT]      = $sta_from[$index];
                $data["REMARK2"][TEXT]      = $sta_to[$index];
                $data["REMARK3"][TEXT]      = $sta_via[$index];
                $data["REMARK4"][TEXT]      = str_replace("/", "-", $model->str_date);
                $data["REMARK5"][TEXT]      = str_replace("/", "-", $model->end_date);
                $data["REMARK6"][TEXT]      = $model->month_div;

                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");

                $db->query($query);

                $tugakuArray[] = array("TUGAKU" => $CERTIF_INDEX+1);
            }
            //学校学生生徒旅客運賃割引証
            if ($this->print_div == "3") {
                $CERTIF_INDEX = $db->getOne(knjg060aQuery::getCertifIssueEachtypeMAX(CTRL_YEAR));

                $CERTIF_NO = $db->getOne(knjg060aQuery::getCertifIssueEachtypePrintMAX("2", CTRL_YEAR));

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

    //学年数上限値取得
    function getMaxGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_HVAL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "       AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

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

    //生徒環境調査データ取得
    function getSchreg_envir_dat($model, $schregno = "") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        if ($schregno == "") {
            $query .= "     SCHREGNO = '{$model->subSchregno}' ";
        } else {
            $query .= "     SCHREGNO = '{$schregno}' ";
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
}
?>
