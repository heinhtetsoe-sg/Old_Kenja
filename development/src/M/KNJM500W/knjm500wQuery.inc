<?php

require_once('for_php7.php');


class knjm500wQuery extends Query {

    //学期マスタ
    function getSemeMst()
    {
        $query .= "  SELECT DISTINCT";
        $query .= "      SEMESTERNAME LABEL, ";
        $query .= "      SEMESTER AS VALUE    ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '" .CTRL_YEAR ."'";
        $query .= "  AND SEMESTER <> '9' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getSchreg($model)
    {
        $query  = " SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
        $query .= "        SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ";
        $query .= " FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ";
        $query .= "       ((SCHREG_REGD_DAT.SEMESTER)='" .$model->control["学期"]. "') AND ";
        $query .= "       ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."')) ";
        $query .= " ORDER BY ATTENDNO ";

        return $query;
    }
    //生徒取得
    function getComment()
    {
        $query  = " SELECT ";
        $query .= "     REMARKID,REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_T_DAT ";
        $query .= " WHERE REMARKID NOT IN ('7', '8', '9') ";

        return $query;
    }
    
    //DELETE(更新処理)
    function getDeleteQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM HREPORTREMARK_T_DAT ";
        $query .= " WHERE REMARKID NOT IN ('7', '8', '9') ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE(更新処理)
    function getUpdateQuery(&$model,$cnt)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["REMARKID"][TEXT]   = $cnt;
        $data["REMARK"][TEXT]     = $model->field["COMMENT".$cnt];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "SYSDATE()";

        $query = Query::insertSQL($data, "HREPORTREMARK_T_DAT");

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>
