<?php

require_once('for_php7.php');


class knjd153rQuery extends Query {

    //学期一覧取得
    function getSemesterList()
    {
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

    //年組一覧取得
    function getGradeHrclass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model)
    {
        $query  = "";
        $query .= " WITH REMARK1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 ";
        $query .= "     FROM ";
        $query .= "         HEXAM_RECORD_REMARK_SDIV_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '9' AND ";
        $query .= "         TESTKINDCD || TESTITEMCD || SCORE_DIV = '000000' AND ";
        $query .= "         REMARK_DIV = '5' ";
        $query .= " ), REMARK2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK2 ";
        $query .= "     FROM ";
        $query .= "         HEXAM_RECORD_REMARK_SDIV_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         TESTKINDCD || TESTITEMCD || SCORE_DIV = '000000' AND ";
        $query .= "         REMARK_DIV = '6' ";
        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     R1.REMARK1, ";
        $query .= "     R2.REMARK2 ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN REMARK1 R1 ON R1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN REMARK2 R2 ON R2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        //---担任所見

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     HEXAM_RECORD_REMARK_SDIV_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '9' AND ";
            $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '000000' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            $query .= "     REMARK_DIV = '5' ";

            $db->query($query);

            if (!strlen($model->fields["REMARK1"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = "9";
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["TESTKINDCD"][TEXT]       = "00";
            $data["TESTITEMCD"][TEXT]       = "00";
            $data["SCORE_DIV"][TEXT]        = "00";
            $data["REMARK_DIV"][TEXT]       = "5";
            $data["REMARK1"][TEXT]          = $model->fields["REMARK1"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT");

            $db->query($query);
        }

        //---その他

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     HEXAM_RECORD_REMARK_SDIV_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '000000' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            $query .= "     REMARK_DIV = '6' ";

            $db->query($query);

            if (!strlen($model->fields["REMARK2"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["TESTKINDCD"][TEXT]       = "00";
            $data["TESTITEMCD"][TEXT]       = "00";
            $data["SCORE_DIV"][TEXT]        = "00";
            $data["REMARK_DIV"][TEXT]       = "6";
            $data["REMARK2"][TEXT]          = $model->fields["REMARK2"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT");

            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

}
?>
