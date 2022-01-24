<?php

require_once('for_php7.php');

class knjd236aQuery extends Query {

    //学期取得
    function getSemesterMst() 
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($semester) 
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if($semester == "9"){
            $query .= "     SEMESTER IN(SELECT  ";
            $query .= "                     MAX(SEMESTER) ";
            $query .= "                 FROM ";
            $query .= "                      SCHREG_REGD_HDAT ";
            $query .= "                 WHERE ";
            $query .= "                      YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                      GRADE < '11') AND ";
        } else {
            $query .= "     SEMESTER = '".$semester."' AND ";
        }
        $query .= "     GRADE < '11' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //類型グループ一覧取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TYPE_GROUP_CD, ";
        $query .= "     T2.TYPE_GROUP_NAME, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_DOCUMENT_DAT T1 ";
        $query .= " LEFT JOIN TYPE_GROUP_COURSE_MST T2 ON ";
        $query .= "         T2.YEAR = T1.YEAR AND ";
        $query .= "         T2.GRADE = T1.GRADE AND ";
        $query .= "         T2.TYPE_GROUP_CD = T1.TYPE_GROUP_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE = '".$model->grade."' AND ";
        $query .= "     T1.REMARK_DIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TYPE_GROUP_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_DOCUMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     GRADE = '".$model->grade."' AND ";
        $query .= "     TYPE_GROUP_CD = '".$model->type_group_cd."' AND ";
        $query .= "     REMARK_DIV = '1' ";

        return $query;
    }

    //重複チェック
    function chkCode($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_DOCUMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     GRADE = '".$model->grade."' AND ";
        $query .= "     TYPE_GROUP_CD = '".$model->field["TYPE_GROUP_CD"]."' AND ";
        $query .= "     REMARK_DIV = '1' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //類型グループリスト取得
    function getTypeGroupList($grade)
    {
        $query  = " SELECT ";
        $query .= "     TYPE_GROUP_CD AS VALUE, ";
        $query .= "     TYPE_GROUP_CD || '  ' || TYPE_GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TYPE_GROUP_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
        
    }

    //前年度からのデータを取得
    function &getOldYear($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_DOCUMENT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".(CTRL_YEAR - 1)."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE = '".$model->grade."' AND ";
        $query .= "     T1.REMARK_DIV = '1' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     HEXAM_RECORD_DOCUMENT_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                         T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "                         T1.GRADE = T2.GRADE AND ";
        $query .= "                         T1.TYPE_GROUP_CD = T2.TYPE_GROUP_CD AND ";
        $query .= "                         T1.REMARK_DIV = T2.REMARK_DIV ";
        $query .= "                    ) ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     TYPE_GROUP_COURSE_MST T3 ";
        $query .= "                 WHERE ";
        $query .= "                     T3.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.TYPE_GROUP_CD = T3.TYPE_GROUP_CD AND ";
        $query .= "                     T1.GRADE = T3.GRADE ";
        $query .= "                ) ";

        return $query;
    }

    //前年度からのデータを今年度にINSERT
    function &copyInsert($Row)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $Row["SEMESTER"];
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["TYPE_GROUP_CD"][TEXT]    = $Row["TYPE_GROUP_CD"];
        $data["REMARK_DIV"][TEXT]       = $Row["REMARK_DIV"];
        $data["REMARK1"][TEXT]          = $Row["REMARK1"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "HEXAM_RECORD_DOCUMENT_DAT");
        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        if($model->cmd == "update"){
            knjd236aQuery::getDeleteQuery($model);
        }

        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["TYPE_GROUP_CD"][TEXT]    = $model->field["TYPE_GROUP_CD"];
        $data["REMARK_DIV"][TEXT]       = "1";
        $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "HEXAM_RECORD_DOCUMENT_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM HEXAM_RECORD_DOCUMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "     TYPE_GROUP_CD = '".$model->field["TYPE_GROUP_CD"]."' AND ";
        $query .= "     REMARK_DIV = '1' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
