<?php

require_once('for_php7.php');

class knjm261wQuery extends Query {

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
        $query  = " SELECT ";
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
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "     LEFT JOIN V_NAME_MST t3 ON t1.GRAD_VALUE = t3.NAMECD2 ";
        $query .= "          AND t3.YEAR = t1.YEAR ";
        $query .= "          AND t3.NAMECD1 = 'M003' ";
        $query .= "     LEFT JOIN SUBCLASS_MST t4 ON t1.SUBCLASSCD = t4.SUBCLASSCD ";
        $query .= "          AND t1.CLASSCD = t4.CLASSCD ";
        $query .= "          AND t1.SCHOOL_KIND = t4.SCHOOL_KIND ";
        $query .= "          AND t1.CURRICULUM_CD = t4.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->Year."' ";
        $query .= "     AND t1.RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= "     AND t1.REPRESENT_SEQ <> 0 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE(CHAR(t1.RECEIPT_INPUT_DATE),'0000-00-00') DESC, ";
        $query .= "     t1.RECEIPT_TIME DESC, ";
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

    //講座登録確認
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
        $query .= "     SCHREGNO = '".$setSchregNo."'  ";
        //$query .= "     RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' ";

        return $query;
    }

    //講座登録確認MAXGRADVAL
    function getMaxGradeValUmu($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];
        $query  = " WITH MAX_DATA AS ( ";
        $query .= " SELECT MAX(REPRESENT_SEQ) AS REPRESENT_SEQ ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     SCHREGNO = '".$setSchregNo."'  ";
        //$query .= "     RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= " ) ";
        $query .= " SELECT T1.REPRESENT_SEQ ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT T1, ";
        $query .= "     MAX_DATA T2 ";
        $query .= " WHERE T1.YEAR = '" .$model->Year."' AND ";
        $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     T1.STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     T1.SCHREGNO = '".$setSchregNo."' AND ";
        //$query .= "     T1.RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= "     T1.REPRESENT_SEQ = T2.REPRESENT_SEQ AND ";
        $query .= "     T1.GRAD_VALUE IS NOT NULL ";

        return $query;
    }

    //合格しているか
    function getPassGradeValUmu($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];
        $query  = " WITH MAX_DATA AS ( ";
        $query .= " SELECT MAX(REPRESENT_SEQ) AS REPRESENT_SEQ ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     SCHREGNO = '".$setSchregNo."'  ";
        $query .= " ) ";
        $query .= " SELECT COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT T1, ";
        $query .= "     MAX_DATA T2 ";
        $query .= " WHERE T1.YEAR = '" .$model->Year."' AND ";
        $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     T1.STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     T1.SCHREGNO = '".$setSchregNo."' AND ";
        $query .= "     T1.REPRESENT_SEQ = T2.REPRESENT_SEQ AND ";
        $query .= "     T1.GRAD_VALUE IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'M003' AND NAMESPARE1 = '1') ";

        return $query;
    }

    //レポート提出基準日確認
    function getCheckout2($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];
        $query  = " SELECT CHAIRCD ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= " FROM REP_STANDARDDATE_COURSE_DAT ";
        } else {
            $query .= " FROM REP_STANDARDDATE_DAT ";
        }
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        if ($model->Properties["useRepStandarddateCourseDat"] == '1') {
            $query .= "     COURSECD || MAJORCD || COURSECODE = ( ";
            $query .= "         SELECT COURSECD || MAJORCD || COURSECODE ";
            $query .= "         FROM SCHREG_REGD_DAT ";
            $query .= "         WHERE YEAR = '".$model->Year."' ";
            $query .= "         AND SEMESTER = '".$model->semester."' ";
            $query .= "         AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
            $query .= "     ) ";
            $query .= "     AND ";
        }
        $query .= "     STANDARD_SEQ = ".$setRepSep." ";

        return $query;
    }

    //レポート提出基準日確認
    function getCheckout3($model)
    {
        $setSchregNo = $model->field["SCHREGNO"];
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $query  = " SELECT CHAIRCD ";
        $query .= " FROM CHAIR_STD_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     SCHREGNO = '".$setSchregNo."' AND ";
        $query .= "     CHAIRCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             CHAIRCD ";
        $query .= "         FROM ";
        $query .= "             CHAIR_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '" .$model->Year."' ";
        $query .= "             AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '{$setSubclass}' ";
        $query .= "     ) ";

        return $query;
    }

    function deletesiteicheck($data, $model)
    {

        $query  = "SELECT GRAD_VALUE ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$data["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$data["SCHNO"]."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$data["SBCLS"]."' AND ";
        $query .= "     STANDARD_SEQ = ".$data["KAISU"]." AND ";
        $query .= "     REPRESENT_SEQ  = ".$data["SAISU"]." AND ";
        $query .= "     RECEIPT_DATE = '" .str_replace("/","-",$data["SYDAY"])."' AND ";
        $query .= "     REPRESENT_SEQ = ".$data["SAISU"]." AND ";
        $query .= "     GRAD_VALUE IS NOT NULL AND ";
        $query .= "     GRAD_VALUE <> '' ";

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

    //講座登録確認MAXGRADVAL
    function getMaxGradeVal($model)
    {
        $curriculumCd = substr($model->field["REPNO"], 1, 1);
        $subclassCd = substr($model->field["REPNO"], 2, 6);
        $setSubclass = $model->field["INPUT_RADIO"] == "1" ? substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd : $model->field["SUBCLASSCD"];
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];
        $setReceptDate = str_replace("/","-",$model->Date);
        $query  = " SELECT MAX(REPRESENT_SEQ) AS REPRESENT_SEQ ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$setSubclass."' AND ";
        $query .= "     STANDARD_SEQ = ".$setRepSep." AND ";
        $query .= "     SCHREGNO = '".$setSchregNo."'  ";
        //$query .= "     RECEIPT_DATE = '".$setReceptDate."' ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query = knjm261wQuery::getMaxGradeVal($model);
        $maxSeq = $db->getOne($query);

        $curriculumCd = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 1, 1) : substr($model->field["SUBCLASSCD"], 3, 1);
        $subclassCd = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 2, 6) : substr($model->field["SUBCLASSCD"], 4);
        $setSchregNo = $model->field["SCHREGNO"];
        $setRepSep = $model->field["INPUT_RADIO"] == "1" ? substr($model->field["REPNO"], 8, 2) : $model->field["STANDARD_SEQ"];
        $query  = " INSERT INTO REP_PRESENT_DAT";
        $query .= " ( YEAR, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD,STANDARD_SEQ,REPRESENT_SEQ,SCHREGNO,RECEIPT_DATE, ";
        $query .= "   RECEIPT_INPUT_DATE, ";
        $query .= "   RECEIPT_TIME,CHAIRCD,REGISTERCD,UPDATED ) ";
        $query .= " VALUES( ";
        $query .= "     '".$model->Year."', ";
        $query .= "     '".substr($subclassCd, 0, 2)."', ";
        $query .= "     'H', ";
        $query .= "     '".$curriculumCd."', ";
        $query .= "     '".$subclassCd."', ";
        $query .= "     ".$setRepSep." , ";
        $query .= "     ".($maxSeq + 1)." , ";
        $query .= "     '".$setSchregNo."', ";
        $query .= "     '".str_replace("/","-",$model->Date)."', ";
        $query .= "     DATE(CURRENT TIMESTAMP), ";
        $query .= "     current time, ";
        $query .= "     '".$model->field["CHAIRCD"]."', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     current timestamp ";
        $query .= " ) ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuerysitei($data, $model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE ";
        $query .= " FROM REP_PRESENT_DAT ";
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