<?php

require_once('for_php7.php');

class knjg085query extends Query {

    //学期名の取得
    function getSemesterName()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR     = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.GRADE    = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     L1.REMARK1 AS YDAT_REMARK1, ";
        $query .= "     L1.REMARK10 AS YDAT_REMARK10 ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN LICENSE_GROUP_STD_YDAT L1 ON L1.YEAR       = T1.YEAR ";
        $query .= "                                        AND L1.SCHREGNO   = T1.SCHREGNO ";
        $query .= "                                        AND L1.GROUP_DIV  = '00' ";
        $query .= "                                        AND L1.SELECT_DIV = '0' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
    
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            // LICENSE_GROUP_STD_YDAT
            $query  = " DELETE FROM ";
            $query .= "     LICENSE_GROUP_STD_YDAT ";
            $query .= " WHERE ";
            $query .= "     GROUP_DIV  = '00' ";
            $query .= " AND SELECT_DIV = '0' ";
            $query .= " AND SCHREGNO   = '".$schregno."' ";
            $db->query($query);

            if (!strlen($model->fields["YDAT_REMARK1"][$i])) {
                continue;
            }

            //健康診断
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["GROUP_DIV"][TEXT]     = '00';
            $data["SELECT_DIV"][TEXT]    = '0';
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["REMARK1"][TEXT]       = $model->fields["YDAT_REMARK1"][$i];
            $data["REMARK10"][TEXT]      = $model->fields["YDAT_REMARK10"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "LICENSE_GROUP_STD_YDAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
