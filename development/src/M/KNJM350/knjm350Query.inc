<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjm350Query.inc 56590 2017-10-22 13:01:54Z maeshiro $
class knjm350Query extends Query {

    //科目コンボ
    function GetSubclass($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     t1.SUBCLASSCD, ";
        }
        $query .= "     t2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT t1 ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "     AND t2.YEAR = '".$model->ObjYear."' ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->ObjYear."' ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t2.SUBCLASSNAME ";

        return $query;
    }

    //表示データ取得
    function GetReportData($model)
    {
        $query  = " WITH basetable AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME_SHOW, ";
        $query .= "     t1.STANDARD_SEQ, ";
        $query .= "     MAX(REPRESENT_SEQ) AS REPRESENT_SEQ, ";
        $query .= "     MAX(RECEIPT_DATE) AS RECEIPT_DATE ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT t1 LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.GRAD_VALUE = '1' AND ";
        $query .= "     t1.YEAR = '".$model->ObjYear."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sub."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->sub."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME_SHOW, ";
        $query .= "     t1.STANDARD_SEQ ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t3.REPRESENT_PRINT, ";
        $query .= "     t2.REPORTDIV, ";
        $query .= "     t2.REMARK ";
        $query .= " FROM ";
        $query .= "     basetable t1 ";
        $query .= "     LEFT JOIN REP_PRESENT_DAT t3 ON t1.SCHREGNO = t3.SCHREGNO ";
        $query .= "     AND t1.REPRESENT_SEQ = t3.REPRESENT_SEQ ";
        $query .= "     AND t1.STANDARD_SEQ = t3.STANDARD_SEQ ";
        $query .= "     AND t1.RECEIPT_DATE = t3.RECEIPT_DATE ";
        $query .= "     AND t3.YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' || t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD = '".$model->sub."' ";
        } else {
            $query .= "     AND t3.SUBCLASSCD = '".$model->sub."' ";
        }
        $query .= "     LEFT JOIN REP_STANDARDDATE_DAT t2 ON t1.STANDARD_SEQ = t2.STANDARD_SEQ ";
        $query .= "     AND t2.YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t2.CLASSCD || '-' || t2.SCHOOL_KIND || '-' || t2.CURRICULUM_CD || '-' || t2.SUBCLASSCD = '".$model->sub."' ";
        } else {
            $query .= "     AND t2.SUBCLASSCD = '".$model->sub."' ";
        }
        $query .= " ORDER BY t3.REPRESENT_PRINT,t1.SCHREGNO,t1.STANDARD_SEQ ";

        return $query;
    }

    //UPDATE(更新処理)
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  REPRESENT_PRINT = '1', ";
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$model->ObjYear."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sub."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->sub."' AND ";
        }
        $query .= "     STANDARD_SEQ = $model->stsq AND ";
        $query .= "     REPRESENT_SEQ = ($model->rsq - 1) AND ";
        $query .= "     SCHREGNO = '".$model->sch."' AND ";
        $query .= "     RECEIPT_DATE = '" .str_replace("/","-",$model->rdat)."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }

}
?>
