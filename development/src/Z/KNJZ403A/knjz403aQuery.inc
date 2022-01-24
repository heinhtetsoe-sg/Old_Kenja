<?php

require_once('for_php7.php');

class knjz403aQuery extends Query {

    //全体の一覧の取得（左画面）
    function selectQuery($model) {
        $query .= " SELECT ";
        $query .= "     NM.NAMECD2, ";
        $query .= "     LM.SCHOOL_KIND, ";
        $query .= "     LM.GRADE, ";
        $query .= "     CASE WHEN LM.GRADE = '00' ";
        $query .= "          THEN '基本設定(' || NM.ABBV1 || ')' ";
        $query .= "          ELSE GDAT.GRADE_NAME1 ";
        $query .= "     END GRADE_NAME1, ";
        $query .= "     LM.L_CD, ";
        $query .= "     LM.L_NAME, ";
        $query .= "     MM.M_CD, ";
        $query .= "     MM.M_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST LM ";
        $query .= "     LEFT JOIN HREPORT_BEHAVIOR_M_MST MM ON LM.YEAR = MM.YEAR ";
        $query .= "          AND LM.SCHOOL_KIND = MM.SCHOOL_KIND ";
        $query .= "          AND LM.GRADE = MM.GRADE ";
        $query .= "          AND LM.L_CD = MM.L_CD ";
        $query .= "     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'A023' ";
        $query .= "          AND LM.SCHOOL_KIND = NM.NAME1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON LM.YEAR  = GDAT.YEAR ";
        $query .= "          AND LM.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "     LM.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND LM.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND LM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NM.NAMECD2, ";
        $query .= "     LM.GRADE, ";
        $query .= "     LM.L_CD, ";
        $query .= "     MM.M_CD ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query .= " WITH ALL_T AS ( ";
        $query .= " SELECT ";
        $query .= "     '0' AS SORT, ";
        $query .= "     A023.NAMECD2, ";
        $query .= "     A023.NAME1 || '-' || '00' AS VALUE, ";
        $query .= "     '基本設定(' || A023.ABBV1 || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST A023 ";
        $query .= " WHERE ";
        $query .= "     A023.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND A023.NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND A023.NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND A023.NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ), GDAT AS ( ";
        $query .= " SELECT ";
        $query .= "     '1' AS SORT, ";
        $query .= "     A023.NAMECD2, ";
        $query .= "     GDAT.SCHOOL_KIND || '-' || GDAT.GRADE AS VALUE, ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= "     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ";
        $query .= "          AND GDAT.SCHOOL_KIND = A023.NAME1 ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."'  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SORT, ";
        $query .= "     NAMECD2, ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     ALL_T ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SORT, ";
        $query .= "     NAMECD2, ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     GDAT ";
        $query .= " ORDER BY ";
        $query .= "     SORT, ";
        $query .= "     NAMECD2, ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得 (右画面)
    function getSelectOne($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND  =  '".$model->schoolKind."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND L_CD = '".$model->lCd."' ";

        return $query;
    }

    //M_MST
    function getSelectMmst($model) {
        $query .= " SELECT ";
        $query .= "     MM.M_CD, ";
        $query .= "     MM.M_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST LM ";
        $query .= "     INNER JOIN HREPORT_BEHAVIOR_M_MST MM ON LM.YEAR = MM.YEAR ";
        $query .= "           AND LM.SCHOOL_KIND = MM.SCHOOL_KIND ";
        $query .= "           AND LM.GRADE = MM.GRADE ";
        $query .= "           AND LM.L_CD = MM.L_CD ";
        $query .= " WHERE ";
        $query .= "     LM.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND LM.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND LM.GRADE = '{$model->grade}' ";
        $query .= "     AND LM.L_CD = '".$model->lCd."' ";
        $query .= " ORDER BY ";
        $query .= "     MM.M_CD ";

        return $query;
    }

    //ログイン年度データカウント
    function cntThisOrder() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);

        return $data_check;
    }

    //前年度データカウント
    function cntLastOrder() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);

        return $data_check;
    }

    //前年度からコピー
    function &getCopyQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR)."' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_BEHAVIOR_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR)."' ";
        $db->query($query);

        $query  = " INSERT INTO HREPORT_BEHAVIOR_L_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             GRADE, ";
        $query .= "             L_CD, ";
        $query .= "             L_NAME, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         GRADE, ";
        $query .= "         L_CD, ";
        $query .= "         L_NAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         HREPORT_BEHAVIOR_L_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= "     ) ";

        $db->query($query);

        $query  = " INSERT INTO HREPORT_BEHAVIOR_M_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             GRADE, ";
        $query .= "             L_CD, ";
        $query .= "             M_CD, ";
        $query .= "             M_NAME, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         GRADE, ";
        $query .= "         L_CD, ";
        $query .= "         M_CD, ";
        $query .= "         M_NAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         HREPORT_BEHAVIOR_M_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= "     ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        
        return true;
    }

    //１レコード取得 (右画面)
    function getChkSelectOne($model)
    {
        list($schoolKind, $grade) = preg_split("/-/", $model->field["GRADE"]);
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND  =  '".$schoolKind."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        $query .= "     AND L_CD = '".$model->field["L_CD"]."' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        list($schoolKind, $grade) = preg_split("/-/", $fields["GRADE"]);
        $data["YEAR"][TEXT]        = CTRL_YEAR;
        $data["SCHOOL_KIND"][TEXT] = $schoolKind;
        $data["GRADE"][TEXT]       = $grade;
        $data["L_CD"][TEXT]        = $fields["L_CD"];
        $data["L_NAME"][TEXT]      = $fields["L_NAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_BEHAVIOR_L_MST");
        $db->query($query);

        foreach ($model->mMstArray as $soeji => $mArray) {
            if ($mArray["M_CD"] == "" && $mArray["M_NAME"] == "") {
                continue;
            }
            $data = array();
            $data["YEAR"][TEXT]        = CTRL_YEAR;
            $data["SCHOOL_KIND"][TEXT] = $schoolKind;
            $data["GRADE"][TEXT]       = $grade;
            $data["L_CD"][TEXT]        = $fields["L_CD"];
            $data["M_CD"][TEXT]        = $mArray["M_CD"];
            $data["M_NAME"][TEXT]      = $mArray["M_NAME"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_BEHAVIOR_M_MST");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["L_NAME"][TEXT]           = $fields["L_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        list($schoolKind, $grade) = preg_split("/-/", $fields["GRADE"]);
        $where  = " WHERE YEAR        = '".CTRL_YEAR."' ";
        $where .= "  AND SCHOOL_KIND  = '" .$schoolKind."'";
        $where .= "  AND GRADE        = '" .$grade."'";
        $where .= "  AND L_CD         = '" .$fields["L_CD"]."'";

        $query = Query::updateSQL($data, "HREPORT_BEHAVIOR_L_MST", $where);
        $db->query($query);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_KIND = '" .$schoolKind."' ";
        $query .= " AND GRADE = '" .$grade."' ";
        $query .= " AND L_CD  = '" .$fields["L_CD"]."' ";

        $db->query($query);

        foreach ($model->mMstArray as $soeji => $mArray) {
            if ($mArray["M_CD"] == "" && $mArray["M_NAME"] == "") {
                continue;
            }
            $data = array();
            $data["YEAR"][TEXT]        = CTRL_YEAR;
            $data["SCHOOL_KIND"][TEXT] = $schoolKind;
            $data["GRADE"][TEXT]       = $grade;
            $data["L_CD"][TEXT]        = $fields["L_CD"];
            $data["M_CD"][TEXT]        = $mArray["M_CD"];
            $data["M_NAME"][TEXT]      = $mArray["M_NAME"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_BEHAVIOR_M_MST");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        list($schoolKind, $grade) = preg_split("/-/", $fields["GRADE"]);
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_KIND = '" .$schoolKind."' ";
        $query .= " AND GRADE = '" .$grade."' ";
        $query .= " AND L_CD  = '" .$fields["L_CD"]."' ";

        $db->query($query);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_KIND = '" .$schoolKind."' ";
        $query .= " AND GRADE = '" .$grade."' ";
        $query .= " AND L_CD  = '" .$fields["L_CD"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);

        return $result;
    }
}
?>
