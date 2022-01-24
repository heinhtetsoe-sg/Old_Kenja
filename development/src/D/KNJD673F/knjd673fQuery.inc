<?php

require_once('for_php7.php');

class knjd673fquery extends Query {
    //名称マスタ取得
    function getNameMst($namecd1, $model) {
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == $setNameCd) {
            $query .= " AND NAMECD2 <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //複式年組一覧取得
    function getGradeHrclass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_F.GRADE || REGD_F.HR_CLASS AS VALUE, ";
        $query .= "     REGD_F.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT REGD_F ";
        $query .= " WHERE ";
        $query .= "     REGD_F.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGD_F.SEMESTER    = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_F.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                             REGD_G.SCHOOL_KIND = '".SCHOOLKIND."') ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (REGD_F.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          REGD_F.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          REGD_F.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          REGD_F.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          REGD_F.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          REGD_F.SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //面談データ取得
    function selectQuery($model) {
        $query  = " WITH INTERVIEW AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         INTERVIEW_REMARK AS REMARK ";
        $query .= "     FROM ";
        $query .= "         COURSE_INTERVIEW_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR            = '". CTRL_YEAR ."' AND ";
        $query .= "         INTERVIEW_DIV   = '".$model->field["INTERVIEW_DIV"]."' ";
        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_FI_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T2.REMARK";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN INTERVIEW T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //データ更新処理
    function updateQuery($model) {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            //削除
            $query  = " DELETE FROM ";
            $query .= "     COURSE_INTERVIEW_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $query .= "     SCHREGNO        = '".$schregno."' AND ";
            $query .= "     INTERVIEW_DIV   = '".$model->field["INTERVIEW_DIV"]."' ";
            $db->query($query);

            if (!strlen($model->fields["REMARK"][$i])) {
                continue;
            }

            //追加
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["INTERVIEW_DIV"][TEXT]    = $model->field["INTERVIEW_DIV"];
            $data["INTERVIEW_REMARK"][TEXT] = $model->fields["REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "COURSE_INTERVIEW_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
