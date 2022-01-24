<?php

require_once('for_php7.php');


class knjc039cquery extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //集計単位一覧取得
    function getCollectionList($model)
    {
        $query  = " SELECT ";
        $query .= "     COLLECTION_CD AS VALUE, ";
        $query .= "     COLLECTION_CD || ':' || COLLECTION_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT HDAT ";
        $query .= "                 WHERE ";
        $query .= "                     GDAT.YEAR           = HDAT.YEAR ";
        $query .= "                     AND HDAT.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "                     AND GDAT.GRADE      = HDAT.GRADE ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "                     AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model)
    {
        $query  = " WITH ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHOOL_KIND, ";
        $query .= "         T2.COLLECTION_CD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTEND_REMARK ";
        $query .= "     FROM ";
        $query .= "         ATTEND_REASON_COLLECTION_DAT T1 ";
        $query .= "         INNER JOIN ATTEND_REASON_COLLECTION_MST T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "             AND T1.COLLECTION_CD = T2.COLLECTION_CD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "         AND T1.COLLECTION_CD = '".$model->field["COLLECTION_CD"]."' ";
        $query .= " ) ";
        $query .= " , SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "             ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "             ON T1.YEAR = T3.YEAR ";
        $query .= "             AND T1.GRADE = T3.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "         AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.ATTEND_REMARK, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.COLLECTION_CD ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN ATTEND T2 ";
        $query .= "         ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
            $data["COLLECTION_CD"][TEXT]    = $model->field["COLLECTION_CD"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["ATTEND_REMARK"][TEXT]    = $model->data["ATTEND_REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //存在チェック
            $query  = " SELECT COUNT(*) FROM ATTEND_REASON_COLLECTION_DAT";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."'";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "     AND COLLECTION_CD = '".$model->field["COLLECTION_CD"]."' ";

            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SCHREGNO = '".$schregno."' ";
                $where .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
                $where .= "     AND COLLECTION_CD = '".$model->field["COLLECTION_CD"]."' ";
                $query = Query::updateSQL($data, "ATTEND_REASON_COLLECTION_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "ATTEND_REASON_COLLECTION_DAT");
            }

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
