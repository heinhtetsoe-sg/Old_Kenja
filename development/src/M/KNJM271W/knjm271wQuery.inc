<?php

require_once('for_php7.php');

class knjm271wQuery extends Query {

    //担当者取得
    function selectStaff(&$model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE){
            $model->User = 0;
            $query  = "SELECT DISTINCT w1.STAFFCD,w2.STAFFNAME ";
            $query .= "FROM REP_STF_DAT w1 left join V_STAFF_MST w2 on w1.STAFFCD = w2.STAFFCD and w1.YEAR = w2.YEAR ";
            $query .= "WHERE w1.YEAR ='" .$model->Year ."'";
            $query .= " ORDER BY w1.STAFFCD ";
        }else {
            $model->User = 1;
            $query  = " with atable (CHAIRCD) as ( ";
            $query .= " SELECT ";
            $query .= "     CHAIRCD ";
            $query .= " FROM ";
            $query .= "     REP_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .$model->Year."'      AND ";
            $query .= "     STAFFCD ='" .STAFFCD ."' ";
            $query .= " GROUP BY ";
            $query .= "     CHAIRCD), ";
            $query .= " btable (STAFFCD,YEAR) as ( ";
            $query .= " SELECT ";
            $query .= "     w1.STAFFCD,w1.YEAR ";
            $query .= " FROM ";
            $query .= "     REP_STF_DAT w1,atable w2 ";
            $query .= " WHERE ";
            $query .= "     w1.YEAR = '" .$model->Year."'      AND ";
            $query .= "     w1.CHAIRCD = w2.CHAIRCD ";
            $query .= " GROUP BY ";
            $query .= "     w1.STAFFCD,w1.YEAR) ";
            $query .= " SELECT ";
            $query .= "     w1.STAFFCD, ";
            $query .= "     w2.STAFFNAME ";
            $query .= " FROM ";
            $query .= "     btable w1 left join V_STAFF_MST w2 on w1.STAFFCD = w2.STAFFCD and ";
            $query .= "     w1.YEAR = w2.YEAR ";
            $query .= " ORDER BY ";
            $query .= "     w1.STAFFCD ";
        }

        return $query;
    }

    //科目
    function getSubClass($model)
    {
        $query  = " SELECT ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS VALUE, ";
        $query .= "     SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD < '91' ";
        if ($model->useCombinedDat == "1") {
            $query .= "     AND NOT EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             REP_SUBCLASS_COMBINED_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$model->Year}' ";
            $query .= "             AND ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD = CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //回数
    function getStandardSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     VALUE(REP_START_SEQ, 1) AS REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";

        return $query;
    }

    //登録済み生徒データ
    function getSch($model)
    {
        $query  = " SELECT  ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME, ";
        $query .= "     t1.RECEIPT_TIME, ";
        $query .= "     t1.CLASSCD, ";
        $query .= "     t1.SCHOOL_KIND, ";
        $query .= "     t1.CURRICULUM_CD, ";
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t1.CHAIRCD, ";
        $query .= "     t1.RECEIPT_DATE, ";
        $query .= "     t4.SUBCLASSNAME, ";
        $query .= "     t1.STANDARD_SEQ, ";
        $query .= "     t1.REPRESENT_SEQ, ";
        $query .= "     t1.GRAD_VALUE, ";
        $query .= "     t1.GRAD_TIME, ";
        $query .= "     t3.NAMECD2, ";
        $query .= "     t3.NAME1 ";
        $query .= " FROM REP_PRESENT_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " left join V_NAME_MST t3 on t1.GRAD_VALUE = t3.NAMECD2 and t1.YEAR = t3.YEAR and t3.NAMECD1 = 'M003' ";
        $query .= " left join SUBCLASS_MST t4 on t1.SUBCLASSCD = t4.SUBCLASSCD ";
        $query .= "     AND t1.CLASSCD = t4.CLASSCD ";
        $query .= "     AND t1.SCHOOL_KIND = t4.SCHOOL_KIND ";
        $query .= "     AND t1.CURRICULUM_CD = t4.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '" .$model->Year."' and ";
        $query .= "     t1.GRAD_INPUT_DATE = '".str_replace("/","-",$model->Date)."' and ";
        $query .= "     t1.GRAD_VALUE is not null and ";
        $query .= "     t1.REPRESENT_SEQ = 0 ";
        if (AUTHORITY != DEF_UPDATABLE){
            $query .= " AND t1.CLASSCD || t1.SCHOOL_KIND  || t1.CURRICULUM_CD || t1.SUBCLASSCD ";
            $query .= " IN (SELECT ";
            $query .= "         I1.CLASSCD || I1.SCHOOL_KIND  || I1.CURRICULUM_CD || I1.SUBCLASSCD ";
            $query .= "     FROM ";
            $query .= "         REP_STF_DAT I1 ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '" .$model->Year."'      AND ";
            $query .= "         STAFFCD ='".STAFFCD."' ";
            $query .= " ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE(CHAR(t1.GRAD_INPUT_DATE),'0000-00-00') DESC, ";
        $query .= "     t1.GRAD_TIME DESC, ";
        $query .= "     t1.SCHREGNO ";
        return $query;
    }

    //読替テーブル有無
    function useCombinedDat()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSIBM.TABLES ";
        $query .= " WHERE ";
        $query .= "     TABLE_NAME = 'REP_SUBCLASS_COMBINED_DAT' ";

        return $query;
    }

    //読替先科目
    function getCombSubclassSaki($model, $info)
    {
        $setSubclass = $info["CLASSCD"].$info["SCHOOL_KIND"].$info["CURRICULUM_CD"].$info["SUBCLASSCD"];

        $query  = " SELECT ";
        $query .= "     REP_COMBINED.*, ";
        $query .= "     SUB_M.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     REP_SUBCLASS_COMBINED_DAT REP_COMBINED ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON REP_COMBINED.COMBINED_SUBCLASSCD = SUB_M.SUBCLASSCD ";
        $query .= "          AND REP_COMBINED.COMBINED_CLASSCD = SUB_M.CLASSCD ";
        $query .= "          AND REP_COMBINED.COMBINED_SCHOOL_KIND = SUB_M.SCHOOL_KIND ";
        $query .= "          AND REP_COMBINED.COMBINED_CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     REP_COMBINED.YEAR = '{$model->Year}' ";
        $query .= "     AND REP_COMBINED.ATTEND_CLASSCD || REP_COMBINED.ATTEND_SCHOOL_KIND || REP_COMBINED.ATTEND_CURRICULUM_CD || REP_COMBINED.ATTEND_SUBCLASSCD = '{$setSubclass}' ";

        return $query;
    }

    //読替元科目
    function getCombSubclass($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];

        $query  = " SELECT ";
        $query .= "     REP_STANDARD.* ";
        $query .= " FROM ";
        $query .= "     REP_SUBCLASS_COMBINED_DAT REP_COMB ";
        $query .= "     INNER JOIN CHAIR_DAT C_DAT ON C_DAT.YEAR = REP_COMB.YEAR ";
        $query .= "           AND C_DAT.SEMESTER = '".$model->semester."' ";
        $query .= "           AND C_DAT.CLASSCD || C_DAT.SCHOOL_KIND || C_DAT.CURRICULUM_CD || C_DAT.SUBCLASSCD = REP_COMB.ATTEND_CLASSCD || REP_COMB.ATTEND_SCHOOL_KIND || REP_COMB.ATTEND_CURRICULUM_CD || REP_COMB.ATTEND_SUBCLASSCD, ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     REP_STANDARDDATE_COURSE_DAT REP_STANDARD, ";
        } else {
            $query .= "     REP_STANDARDDATE_DAT REP_STANDARD, ";
        }
        $query .= "     CHAIR_STD_DAT C_STD ";
        $query .= " WHERE ";
        $query .= "     REP_COMB.YEAR = '{$model->Year}' ";
        $query .= "     AND COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD = '{$setSubclass}' ";
        $query .= "     AND ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD = REP_STANDARD.CLASSCD || REP_STANDARD.SCHOOL_KIND || REP_STANDARD.CURRICULUM_CD || REP_STANDARD.SUBCLASSCD ";
        $query .= "     AND REP_COMB.YEAR = REP_STANDARD.YEAR ";
        $query .= "     AND REP_STANDARD.STANDARD_SEQ = {$setRepSep} ";
        $query .= "     AND C_STD.YEAR = REP_COMB.YEAR ";
        $query .= "     AND C_STD.SEMESTER = '".$model->semester."' ";
        $query .= "     AND C_STD.SCHREGNO = '".$setSchregNo."' ";
        $query .= "     AND C_STD.CHAIRCD = C_DAT.CHAIRCD ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     AND REP_STANDARD.COURSECD || REP_STANDARD.MAJORCD || REP_STANDARD.COURSECODE = (";
            $query .= "             SELECT COURSECD || MAJORCD || COURSECODE ";
            $query .= "             FROM SCHREG_REGD_DAT ";
            $query .= "             WHERE YEAR = '{$model->Year}' ";
            $query .= "             AND SEMESTER = '{$model->semester}' ";
            $query .= "             AND SCHREGNO = '{$setSchregNo}' ";
            $query .= "         ) ";
        }

        return $query;
    }

    //受付登録確認
    function getCheckout($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     REPRESENT_SEQ = 0 AND ";
        $query .= "     SCHREGNO = '".$setSchregNo."' ";

        return $query;
    }

    //MAX日付取得
    function getMaxReceiptDate($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];
        $query  = " SELECT MAX(RECEIPT_DATE) AS RECEIPT_DATE ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     REPRESENT_SEQ = 0 AND ";
        $query .= "     SCHREGNO = '".$setSchregNo."' ";

        return $query;
    }

    //添削者登録確認
    function getCheckstf($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $query  = " SELECT STAFFCD ";
        $query .= " FROM REP_STF_DAT ";
        $query .= " WHERE ";
        $query .= " YEAR = '" .$model->Year."' ";
        $query .= " AND CHAIRCD IN ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '" .$model->Year."' AND ";
        $query .= "         SEMESTER = '" .$model->semester."' AND ";
        $query .= "         CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."') ";
        $query .= " AND STAFFCD = '".$model->field["STAFF"]."' ";

        return $query;
    }

    //評価確認
    function getCheckhyouka($dat)
    {
        $query  = " SELECT NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'M003' AND NAMECD2 = '".$dat."' ";

        return $query;
    }

    //評価返送登録確認
    function getCheckGrad($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     STANDARD_SEQ    = ".$setRepSep." AND ";
        $query .= "     REPRESENT_SEQ   = 0 AND ";
        $query .= "     SCHREGNO        = '".$setSchregNo."' AND ";
        $query .= "     VALUE(GRAD_VALUE,'') != '' ";

        return $query;
    }

    //MAX再提出回数取得
    function getMaxRepresentSeq($data)
    {
        $query  = " SELECT ";
        $query .= "     MAX(REPRESENT_SEQ) ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$data["YEAR"]."' AND ";
        $query .= "     SCHREGNO        = '".$data["SCHNO"]."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$data["SBCLS"]."' AND ";
        $query .= "     STANDARD_SEQ    = ".$data["KAISU"]." AND ";
        $query .= "     RECEIPT_DATE    = '" .str_replace("/","-",$data["SYDAY"])."' ";

        return $query;
    }

    //サブフォームデータ
    function getsubSch($model)
    {
        $query  = " SELECT SCHREGNO,NAME_SHOW ";
        $query .= " FROM SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->sch."' ";

        return $query;
    }

    //サブフォームデータ
    function getsubName()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'M003' ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  STAFFCD = '".$model->field["STAFF"]."', ";
        $query .= "  GRAD_VALUE = '".$model->field["HYOUKA"]."', ";
        $query .= "  GRAD_INPUT_DATE = '".str_replace("/","-",$model->Date)."', ";
        $query .= "  GRAD_TIME = current time, ";
        if ($model->field["HYOUKA"] == 1){
            $query .= "  REPRESENT_PRINT = '0', ";
        }else {
            $query .= "  REPRESENT_PRINT = '', ";
        }
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     REPRESENT_SEQ = 0 AND ";
        $query .= "     SCHREGNO = '".$setSchregNo."' AND ";
        $query .= "     RECEIPT_DATE = '".$model->maxreceipt_date."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  GRAD_VALUE = '".VARS::post("GVALSUB")."', ";
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$model->sucl."' AND ";
        $query .= "     STANDARD_SEQ = $model->stsq AND ";
        $query .= "     REPRESENT_SEQ = 0 AND ";
        $query .= "     SCHREGNO = '".$model->sch."' AND ";
        $query .= "     RECEIPT_DATE = '" .str_replace("/","-",$model->rdat)."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $query;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  STAFFCD = '', ";
        $query .= "  GRAD_VALUE = '', ";
        $query .= "  GRAD_DATE = null, ";
        $query .= "  GRAD_INPUT_DATE = null, ";
        $query .= "  GRAD_TIME = null, ";
        $query .= "  REPRESENT_PRINT = '', ";
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "       GRAD_INPUT_DATE = '".str_replace("/","-",$model->Date)."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuerysitei($data, $model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  STAFFCD = '', ";
        $query .= "  GRAD_VALUE = '', ";
        $query .= "  GRAD_DATE = null, ";
        $query .= "  GRAD_INPUT_DATE = null, ";
        $query .= "  GRAD_TIME = null, ";
        $query .= "  REPRESENT_PRINT = '', ";
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$data["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$data["SCHNO"]."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$data["SBCLS"]."' AND ";
        $query .= "     STANDARD_SEQ = ".$data["KAISU"]." AND ";
        $query .= "     REPRESENT_SEQ  = ".$data["SAISU"]." AND ";
        $query .= "     RECEIPT_DATE = '" .str_replace("/","-",$data["SYDAY"])."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>