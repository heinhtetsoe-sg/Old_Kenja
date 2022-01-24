<?php

require_once('for_php7.php');

class knjz403Query extends Query {

    //全体の一覧の取得（左画面）
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T2.GRADE_NAME1, ";
        $query .= "     T1.CODE || ':' || T1.CODENAME AS VALUE, ";
        $query .= "     T1.CODE, ";
        $query .= "     T1.VIEWNAME, ";
        $query .= "     T1.STUDYREC_CODE || ':' || T3.STUDYREC_CODENAME AS STUDYREC_CODE, ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " LEFT JOIN BEHAVIOR_SUB_MST T3 ON T3.SCHOOL_KIND     = T2.SCHOOL_KIND";
        $query .= "                              AND T3.STUDYREC_CODE   = T1.STUDYREC_CODE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.SCHOOL_KIND DESC, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.CODE ";
        
        return $query;
    }
    
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."'  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_KIND DESC, ";
        $query .= "     VALUE ";

        return $query;
    }

    //指導要録用行動の記録コード取得
    function getStudyrecCode($grade) {
        $query  = " SELECT ";
        $query .= "     T1.STUDYREC_CODE AS VALUE, ";
        $query .= "     T1.STUDYREC_CODE || ':' || T1.STUDYREC_CODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SUB_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "          ON L1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND L1.GRADE    = '".$grade."' ";
        $query .= "         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得 (右画面)
    function getRow($grade, $code)
    {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$grade."' ";
        $query .= " AND CODE  =  '".$code."' ";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        
        return $row;
    }
        
    //ログイン年度データカウント
    function cntThisOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        
        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //前年度データカウント
    function cntLastOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".(CTRL_YEAR-1)."' ";
        
        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //前年度からコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO BEHAVIOR_SEMES_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             CODE, ";
        $query .= "             CODENAME, ";
        $query .= "             VIEWNAME, ";
        $query .= "             STUDYREC_CODE, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.CODE, ";
        $query .= "         T1.CODENAME, ";
        $query .= "         T1.VIEWNAME, ";
        $query .= "         T1.STUDYREC_CODE, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         BEHAVIOR_SEMES_MST T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "              ON G1.YEAR         = T1.YEAR ";
        $query .= "             AND G1.GRADE        = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "             AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        
        return true;
    }
    
    
    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["CODE"][TEXT]             = $fields["CODE"];
        $data["CODENAME"][TEXT]         = $fields["CODENAME"];
        $data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        $data["STUDYREC_CODE"][TEXT]    = $fields["STUDYREC_CODE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "BEHAVIOR_SEMES_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["CODE"][TEXT]             = $fields["CODE"];
        $data["CODENAME"][TEXT]         = $fields["CODENAME"];
        $data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        $data["STUDYREC_CODE"][TEXT]    = $fields["STUDYREC_CODE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
          $where  = " WHERE YEAR         = '".CTRL_YEAR."' ";
          $where .= "  AND GRADE        = '" .$fields["GRADE"]."'";
          $where .= "  AND CODE         = '" .$fields["CODE"]."'";

        $query = Query::updateSQL($data, "BEHAVIOR_SEMES_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '" .$fields["GRADE"]."' ";
        $query .= " AND CODE  = '" .$fields["CODE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
