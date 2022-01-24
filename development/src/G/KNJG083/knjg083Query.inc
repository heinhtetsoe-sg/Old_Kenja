<?php

require_once('for_php7.php');

class knjg083query extends Query {

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

    //グループコンボ
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D062' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query  = " WITH SELECT_DIV_DATA AS ( ";
        $query .= "     SELECT  ";
        $query .= "         T1.GROUP_DIV, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         L1.REMARK2 AS DIV1_REMARK2, ";
        $query .= "         L1.REMARK3 AS DIV1_REMARK3, ";
        $query .= "         L1.REMARK4 AS DIV1_REMARK4, ";
        $query .= "         L2.REMARK1 AS DIV2_REMARK1, ";
        $query .= "         L2.REMARK2 AS DIV2_REMARK2, ";
        $query .= "         L2.REMARK3 AS DIV2_REMARK3, ";
        $query .= "         L2.REMARK4 AS DIV2_REMARK4 ";
        $query .= "     FROM ";
        $query .= "         LICENSE_GROUP_STD_DAT T1";
        $query .= "         LEFT JOIN LICENSE_GROUP_STD_DAT L1 ON L1.GROUP_DIV = T1.GROUP_DIV ";
        $query .= "                                           AND L1.SELECT_DIV  = '1' ";
        $query .= "                                           AND L1.SCHREGNO  = T1.SCHREGNO ";
        $query .= "         LEFT JOIN LICENSE_GROUP_STD_DAT L2 ON L2.GROUP_DIV = T1.GROUP_DIV ";
        $query .= "                                           AND L2.SELECT_DIV  = '2' ";
        $query .= "                                           AND L2.SCHREGNO  = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.GROUP_DIV   = '{$model->field["GROUP_DIV"]}' ";

        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     L1.DIV1_REMARK2, ";
        $query .= "     L1.DIV1_REMARK3, ";
        $query .= "     L1.DIV1_REMARK4, ";
        $query .= "     L1.DIV2_REMARK1, ";
        $query .= "     L1.DIV2_REMARK2, ";
        $query .= "     L1.DIV2_REMARK3, ";
        $query .= "     L1.DIV2_REMARK4 ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN SELECT_DIV_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
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

            $query  = " DELETE FROM ";
            $query .= "     LICENSE_GROUP_STD_DAT ";
            $query .= " WHERE ";
            $query .= "     GROUP_DIV  = '{$model->field["GROUP_DIV"]}' ";
            $query .= " AND SCHREGNO   = '".$schregno."' ";
            $db->query($query);

            if (!strlen($model->fields["DIV1_REMARK2"][$i]) && !strlen($model->fields["DIV1_REMARK3"][$i]) && !strlen($model->fields["DIV1_REMARK4"][$i])
             && !strlen($model->fields["DIV2_REMARK1"][$i]) && !strlen($model->fields["DIV2_REMARK2"][$i]) && !strlen($model->fields["DIV2_REMARK3"][$i]) && !strlen($model->fields["DIV2_REMARK4"][$i])) {
                continue;
            }
            //生徒項目
            $data = array();
            $data["GROUP_DIV"][TEXT]     = $model->field["GROUP_DIV"];
            $data["SELECT_DIV"][TEXT]    = 1;
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["REMARK2"][TEXT]       = $model->fields["DIV1_REMARK2"][$i];
            $data["REMARK3"][TEXT]       = $model->fields["DIV1_REMARK3"][$i];
            $data["REMARK4"][TEXT]       = $model->fields["DIV1_REMARK4"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "LICENSE_GROUP_STD_DAT");
            $db->query($query);
            
            //保護者項目
            $data = array();
            $data["GROUP_DIV"][TEXT]     = $model->field["GROUP_DIV"];
            $data["SELECT_DIV"][TEXT]    = 2;
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["REMARK1"][TEXT]       = $model->fields["DIV2_REMARK1"][$i];
            $data["REMARK2"][TEXT]       = $model->fields["DIV2_REMARK2"][$i];
            $data["REMARK3"][TEXT]       = $model->fields["DIV2_REMARK3"][$i];
            $data["REMARK4"][TEXT]       = $model->fields["DIV2_REMARK4"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "LICENSE_GROUP_STD_DAT");
            $db->query($query);
            
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
