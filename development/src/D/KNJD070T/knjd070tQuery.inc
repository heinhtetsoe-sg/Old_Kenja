<?php

require_once('for_php7.php');


class knjd070tQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //対象科目一覧リスト取得
    function getSubclass($model) {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.CLASSCD || '-' || R1.SCHOOL_KIND || '-' || R1.CURRICULUM_CD || '-' || R1.SUBCLASSCD AS VALUE, ";
            $query .= "     M1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     R1.SUBCLASSCD AS VALUE, ";
            $query .= "     M1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ( ";
        //成績期末データから年度、学期にあうデータを取得
        $query .= "     (SELECT  ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD,  ";
            $query .= "         SCHOOL_KIND,  ";
            $query .= "         CURRICULUM_CD,  ";
        }
        $query .= "         SUBCLASSCD  ";
        $query .= "     FROM  ";
        $query .= "         RECORD_DAT  ";
        $query .= "     WHERE  ";
        $query .= "         YEAR= '".$model->control["年度"]."' ) R1  ";
        //学籍在籍データから、指定されたクラスに合う学籍番号等を取得
        $query .= " INNER JOIN  ";
        $query .= "     (SELECT  ";
        $query .= "         SCHREGNO  ";
        $query .= "     FROM  ";
        $query .= "         SCHREG_REGD_DAT  ";
        $query .= "     WHERE  ";
        $query .= "         YEAR = '".$model->control["年度"]."'  ";
        $query .= "     AND SEMESTER = '".$model->control["学期"]."'  ";
        $query .= "     AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."') T1  ";
        $query .= "     ON R1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " INNER JOIN SUBCLASS_MST M1  ";
        $query .= "         ON R1.SUBCLASSCD = M1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND R1.CLASSCD       = M1.CLASSCD ";
            $query .= "                          AND R1.SCHOOL_KIND   = M1.SCHOOL_KIND ";
            $query .= "                          AND R1.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
            
        return $query;
    }

    //１レコード取得 ※未使用
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

}
?>
