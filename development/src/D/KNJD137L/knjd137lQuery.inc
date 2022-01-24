<?php

require_once('for_php7.php');

class knjd137lQuery extends Query {
    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //観点を取得（固定9学期）
    function getViewpoint($model) {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV         = '03' AND ";
        $query .= "     CODE        = '02' ";

        return $query;
    }

    //観点マスタより読込
    function getJviewGradeMst($model) {
        $query  = " SELECT ";
        $query .= "     L1.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST L1 ";
        $query .= "         ON  L1.GRADE        = T1.GRADE AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             L1.CLASSCD          = T1.CLASSCD AND ";
            $query .= "             L1.SCHOOL_KIND      = T1.SCHOOL_KIND AND ";
            $query .= "             L1.CURRICULUM_CD    = T1.CURRICULUM_CD AND ";
        }
        $query .= "             L1.SUBCLASSCD   = T1.SUBCLASSCD AND ";
        $query .= "             L1.VIEWCD       = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.GRADE    = '".$model->exp_grade."' AND ";
        $query .= "     substr(T1.SUBCLASSCD, 1, 2) = '90' ";
        $query .= " ORDER BY ";
        $query .= "     L1.SHOWORDER, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137lQuery::getTrainRow($model));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        //観点削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV         = '03' AND ";
        $query .= "     CODE        = '02' ";
        $db->query($query);

        //観点追加
        $data = array();
        $data["YEAR"][TEXT]         = $model->exp_year;
        $data["SEMESTER"][TEXT]     = "9";
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["DIV"][TEXT]          = "03";
        $data["CODE"][TEXT]         = "02";
        $data["REMARK1"][TEXT]      = $model->field["RECORD_VAL02"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));

        Query::dbCheckIn($db);
        return ;
    }

    //印刷学期
    function getPrintSemester() {
        $query  = " SELECT ";
        $query .= "   NM.NAME1 AS LABEL, ";
        $query .= "   NM.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   NAME_MST NM ";
        $query .= " WHERE ";
        $query .= "   NM.NAMECD1 = 'DP78' ";

        return $query;
    }

}
?>
