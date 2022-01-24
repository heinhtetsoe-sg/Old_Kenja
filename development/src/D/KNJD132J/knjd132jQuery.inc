<?php

require_once('for_php7.php');

class knjd132jQuery extends Query {
    /************/
    /**  画面  **/
    /************/
    //IBコース一覧取得
    function getIBPrgCourse($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A034' AND ";
        $query .= "     NAMECD2 = '0' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }    

    //学期を取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年一覧取得
    function getIBGrade($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR    = T1.YEAR ";
        $query .= "                            AND L1.NAMECD1 = 'A034' ";
        $query .= "                            AND L1.NAME1   = '".$model->field["IBPRG_COURSE"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE BETWEEN L1.NAME2 AND L1.NAME3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得(権限チェック)
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T2.YEAR     = T1.YEAR ";
        $query .= "     AND T2.GRADE    = T1.GRADE ";
        $query .= "     AND T2.GRADE    = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "          OR T1.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "          OR T1.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科情報取得
    function getD045($nameCd2) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS IBCLASSCD, ";
        $query .= "     NAME2 AS IBPRG_COURSE, ";
        $query .= "     NAMESPARE1 AS FROM_VAL, ";
        $query .= "     NAMESPARE2 AS TO_VAL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'D045' ";
        $query .= "     AND NAMECD2 = '{$nameCd2}' ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     PDAT.SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN IBSUBCLASS_GRADE_PHASE_DAT PDAT ON PDAT.IBYEAR       = REGD.YEAR ";
        $query .= "                                              AND PDAT.IBGRADE      = REGD.GRADE ";
        $query .= "                                              AND PDAT.SCHREGNO     = REGD.SCHREGNO ";
        $query .= "                                              AND PDAT.IBCLASSCD    = '".$model->infoD045["IBCLASSCD"]."' ";
        $query .= "                                              AND PDAT.IBPRG_COURSE = '".$model->infoD045["IBPRG_COURSE"]."' ";
        $query .= "                                              AND PDAT.SEMESTER     = REGD.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR                   = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER               = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    /****************/
    /**  更新処理  **/
    /****************/
    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBSUBCLASS_GRADE_PHASE_DAT ";
        $query .= " WHERE ";
        $query .= "         IBYEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND IBGRADE      = '".$model->field["GRADE"]."' ";
        $query .= "     AND IBCLASSCD    = '".$model->infoD045["IBCLASSCD"]."' ";
        $query .= "     AND IBPRG_COURSE = '".$model->infoD045["IBPRG_COURSE"]."' ";
        $query .= "     AND SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO    IN (SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                                 YEAR              = '".CTRL_YEAR."' ";
        $query .= "                             AND SEMESTER          = '".$model->field["SEMESTER"]."' ";
        $query .= "                             AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."') ";

        $db->query($query);

        foreach ($model->arrSchregNo as $key => $schregNo) {
            $data = array();
            $data["IBYEAR"][TEXT]           = CTRL_YEAR;
            $data["IBGRADE"][TEXT]          = $model->field["GRADE"];
            $data["SCHREGNO"][TEXT]         = $schregNo;
            $data["IBCLASSCD"][TEXT]        = $model->infoD045["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]     = $model->infoD045["IBPRG_COURSE"];
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCORE"][NUMBER]          = $model->field["SCORE_".$schregNo];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "IBSUBCLASS_GRADE_PHASE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
