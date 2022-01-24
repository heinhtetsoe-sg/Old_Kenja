<?php

require_once('for_php7.php');

class knjd137jQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model)
    {
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
    function getViewpoint($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '03' ";
        $query .= "     AND CODE = '02' ";

        return $query;
    }

    //身体の記録を取得（固定9学期）
    function getPhysicalrecords($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '03' ";
        $query .= "     AND CODE = '01' ";

        return $query;
    }


    //観点マスタより読込
    function getJviewGradeMst($model)
    {
        $query  = " SELECT ";
        $query .= "     L1.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST L1 ON L1.GRADE = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND L1.CLASSCD        = T1.CLASSCD ";
            $query .= "           AND L1.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "           AND L1.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= "           AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "           AND L1.VIEWCD = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.GRADE = '".$model->exp_grade."' ";
        $query .= "     AND substr(T1.SUBCLASSCD, 1, 2) = '90' ";
        $query .= " ORDER BY ";
        $query .= "     L1.SHOWORDER, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137jQuery::getTrainRow($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        //観点と身体の記録削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '03' ";
        $query .= "     AND CODE in ('01', '02')";
        $db->query($query);

        //身体の記録追加
        $data = array();
        $data["YEAR"][TEXT]         = $model->exp_year;
        $data["SEMESTER"][TEXT]     = "9";
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["DIV"][TEXT]          = "03";
        $data["CODE"][TEXT]         = "01";
        $data["REMARK1"][TEXT]      = $model->field["RECORD_VAL01"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
    
        $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));

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
}
?>
