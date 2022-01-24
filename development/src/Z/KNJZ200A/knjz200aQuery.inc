<?php

require_once('for_php7.php');

class knjz200aQuery extends Query {

    //学年取得
    function getIBGrade($model, $flg) {
        list($grade, $course) = explode('-', $model->ibgrade_course);
    
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "    L1.GRADE || '-' || T1.NAME1 AS VALUE, ";
            $query .= "    L1.GRADE_NAME1 || '(' || T1.ABBV1 || ')' AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "    V_NAME_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "AND T1.NAMECD1 = 'A034' ";
        $query .= "AND L1.GRADE BETWEEN T1.NAME2 AND T1.NAME3 ";
        if ($flg == "name") {
            $query .= " AND L1.GRADE  = '".$grade."' ";
            $query .= " AND T1.NAME1 = '".$course."'  ";
        } else if ($flg == "grade_list") {
            $query .= " AND T1.NAME1 = '".$course."'  ";
        }
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "    T1.NAMECD2, ";
            $query .= "    L1.GRADE ";
        }
        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        list($grade, $course) = explode('-', $model->ibgrade_course);
    
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.IBSUBCLASSNAME, ";
        $query .= "     T2.IBSUBCLASSNAME_ENG ";
        $query .= " FROM ";
        $query .= "     IBCREDIT_MST T1";
        $query .= " LEFT JOIN IBSUBCLASS_MST T2 ON T2.IBCLASSCD  = T1.IBCLASSCD";
        $query .= "                            AND T2.IBPRG_COURSE = T1.IBPRG_COURSE";
        $query .= "                            AND T2.IBCURRICULUM_CD = T1.IBCURRICULUM_CD";
        $query .= "                            AND T2.IBSUBCLASSCD = T1.IBSUBCLASSCD";
        $query .= " LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                        AND T3.NAME1 = T1.IBPRG_COURSE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE  = '".$grade."' ";
        $query .= " AND T3.NAME1 = '".$course."'  ";
        $query .= " ORDER BY ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD ";

        return $query;
    }

    //IBCREDIT_MSTから１レコード取得
    function getRow($ibgrade_course, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) {
        list($grade, $course) = explode('-', $ibgrade_course);
    
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBCREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE         = '".$grade."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' ";

        return $query;
    }

    //科目一覧取得
    function getIBSubclasscd($model, $flg) {
        list($grade, $course) = explode('-', $model->ibgrade_course);

        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD AS VALUE, ";
            $query .= "     IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD || ' ' || IBSUBCLASSNAME_ENG AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_IBSUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".CTRL_YEAR."' AND ";
        $query .= "     IBPRG_COURSE    = '".$course."' ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     IBCURRICULUM_CD, ";
            $query .= "     IBCLASSCD, ";
            $query .= "     IBSUBCLASSCD ";
        }

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        list($grade, $course) = explode('-', $model->ibgrade_course);

        //追加
        $data = array();
        $data["YEAR"][TEXT]           = CTRL_YEAR;
        $data["GRADE"][TEXT]          = $grade;
        $data["IBCLASSCD"][TEXT]        = $model->field["IBCLASSCD"];
        $data["IBPRG_COURSE"][TEXT]     = $model->field["IBPRG_COURSE"];
        $data["IBCURRICULUM_CD"][TEXT]  = $model->field["IBCURRICULUM_CD"];
        $data["IBSUBCLASSCD"][TEXT]     = $model->field["IBSUBCLASSCD"];
        $data["NEED_TIME"][NUMBER]      = $model->field["NEED_TIME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "IBCREDIT_MST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        list($grade, $course) = explode('-', $model->ibgrade_course);

        //追加
        $data = array();
        $data["NEED_TIME"][NUMBER]      = $model->field["NEED_TIME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        
        $where  = " WHERE ";
        $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $where .= "     GRADE           = '".$grade."' AND ";
        $where .= "     IBCLASSCD       = '".$model->field["IBCLASSCD"]."' AND ";
        $where .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $where .= "     IBCURRICULUM_CD = '".$model->field["IBCURRICULUM_CD"]."' AND ";
        $where .= "     IBSUBCLASSCD    = '".$model->field["IBSUBCLASSCD"]."' ";

        $query = Query::updateSQL($data, "IBCREDIT_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        list($grade, $course) = explode('-', $model->ibgrade_course);

        $query  = " DELETE FROM ";
        $query .= "     IBCREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE           = '".$grade."' AND ";
        $query .= "     IBCLASSCD       = '".$model->field["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$model->field["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$model->field["IBSUBCLASSCD"]."' ";
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //IBCREDIT_MST存在チェック
    function checkIBCreditMst($set_year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBCREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$set_year."' ";

        return $query;
    }

    /********/
    /* COPY */
    /********/
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBCREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        //前年度データ
        $query = knjz200aQuery::getPreYearData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //追加
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["IBCLASSCD"][TEXT]        = $row["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]     = $row["IBPRG_COURSE"];
            $data["IBCURRICULUM_CD"][TEXT]  = $row["IBCURRICULUM_CD"];
            $data["IBSUBCLASSCD"][TEXT]     = $row["IBSUBCLASSCD"];
            $data["NEED_TIME"][NUMBER]      = $row["NEED_TIME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "IBCREDIT_MST");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }

    //前年度データ取得
    function getPreYearData($model) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     IBCREDIT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".(CTRL_YEAR-1)."' ";

        return $query;
    }


//**********一括処理**********//

    //IB単位マスタ取得
    function getSubQuery1($model)
    {
        list($grade, $course) = explode('-', $model->ibgrade_course);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBCREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE           = '".$grade."' AND ";
        $query .= "     IBCLASSCD       = '".$model->ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$model->ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$model->ibsubclasscd."' ";

        return $query;
    }

    //IB単位マスタチェック
    function checkCreditMst($model, $grade_course, $subclass) {
    
        list($classcd, $course, $curriculum_cd, $suclasscd) = explode('-',$subclass);
        list($grade, $course) = explode('-', $grade_course);
    
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBCREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE           = '".$grade."' AND ";
        $query .= "     IBCLASSCD       = '".$classcd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$curriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$suclasscd."' ";

        return $query;
    }

    // UPDATE（一括更新）
    function &getRepUpdateQuery($db, $model, $fields, $grade_course, $subclass) {

        list($classcd, $course, $curriculum_cd, $suclasscd) = explode('-',$subclass);
        list($grade, $course) = explode('-', $grade_course);
        
        //IB単位マスタ存在チェック
        $checkRow = array();
        $checkRow = $db->getRow(knjz200aQuery::checkCreditMst($model, $grade_course, $subclass), DB_FETCHMODE_ASSOC);

        //更新対象項目
        $check_item = array(array("NEED_TIME", "N"));
        
        foreach($model->replace["data_chk"] as $key => $val){
            if ($val == "1") {
                //チェック項目
                if ($check_item[$key][1] == "N") {
                    $data[$check_item[$key][0]][NUMBER] = $fields[$check_item[$key][0]];
                } else {
                    $data[$check_item[$key][0]][TEXT]   = $fields[$check_item[$key][0]];
                }
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if (!is_array($checkRow) && $fields["NEED_TIME"] != "") {
            //追加
            $data["YEAR"][TEXT]               = CTRL_YEAR;
            $data["GRADE"][TEXT]              = $grade;
            $data["IBCLASSCD"][TEXT]          = $classcd;
            $data["IBPRG_COURSE"][TEXT]       = $course;
            $data["IBCURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["IBSUBCLASSCD"][TEXT]       = $suclasscd;
    
            $query = Query::insertSQL($data, "IBCREDIT_MST");
        } else {
            //更新
            $where  = " WHERE ";
            $where .= "     YEAR              = '". CTRL_YEAR ."' AND ";
            $where .= "     GRADE             = '".$grade."' AND ";
            $where .= "     IBCLASSCD         = '".$classcd."' AND ";
            $where .= "     IBPRG_COURSE      = '".$course."' AND ";
            $where .= "     IBCURRICULUM_CD   = '".$curriculum_cd."' AND ";
            $where .= "     IBSUBCLASSCD      = '".$suclasscd."' ";

            $query = Query::updateSQL($data, "IBCREDIT_MST", $where);
        }
        return $query;
    }
    
}
?>
