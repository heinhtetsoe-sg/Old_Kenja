<?php

require_once('for_php7.php');

class knjm260Query extends Query {

    //登録済み生徒データ
    function getSch($model)
    {
        $query  = " SELECT t1.SCHREGNO,t2.NAME_SHOW,RECEIPT_TIME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     t1.SUBCLASSCD, ";
        }
        $query .= "     t1.SUBCLASSCD AS SUBCLASSCD_ONLY, ";
        $query .= "     t1.CHAIRCD,t1.RECEIPT_DATE, ";
        $query .= " t4.SUBCLASSNAME,t1.STANDARD_SEQ,t1.REPRESENT_SEQ,t1.GRAD_VALUE,t1.GRAD_TIME, ";
        $query .= " t3.NAMECD2,t3.NAME1 ";
        $query .= " FROM REP_PRESENT_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " left join V_NAME_MST t3 on t1.GRAD_VALUE = t3.NAMECD2 and t3.YEAR = t1.YEAR and t3.NAMECD1 = 'M003' ";
        $query .= " left join SUBCLASS_MST t4 on t1.SUBCLASSCD = t4.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t1.CLASSCD = t4.CLASSCD ";
            $query .= "     AND t1.SCHOOL_KIND = t4.SCHOOL_KIND ";
            $query .= "     AND t1.CURRICULUM_CD = t4.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '" .$model->Year."' and t1.RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= " ORDER BY t1.RECEIPT_DATE DESC, t1.RECEIPT_TIME DESC,t1.SCHREGNO ";

        return $query;
    }

    //教育課程取得
    function getCurriculumCd($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(CURRICULUM_CD) CURRICULUM_CD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,2)."' ||  '-' || 'H' ||  '-' || '".substr($model->field["REPNO"],1,6)."' ";

        return $query;
    }

    //教育課程取得
    function getCurriculumCd2($model, $where)
    {
        $query  = " SELECT ";
        $query .= "     MAX(CURRICULUM_CD) CURRICULUM_CD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND ".$where;

        return $query;
    }

    //講座登録確認
    function getCheckout($model)
    {
        $query  = " SELECT GRAD_VALUE,RECEIPT_DATE ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,2)."' ||  '-' || 'H' || '-' || '{$model->curriculumCd}' || '-' || '".substr($model->field["REPNO"],1,6)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],7,2)." AND ";
            $query .= "     REPRESENT_SEQ = ".substr($model->field["REPNO"],9,1)." AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,13)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],14,2)." AND ";
            $query .= "     REPRESENT_SEQ = ".substr($model->field["REPNO"],16,1)." AND ";
        } else {
            $query .= "     SUBCLASSCD = '".substr($model->field["REPNO"],1,6)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],7,2)." AND ";
            $query .= "     REPRESENT_SEQ = ".substr($model->field["REPNO"],9,1)." AND ";
        }
        $query .= "     SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $query .= " ORDER BY RECEIPT_DATE DESC ";

        return $query;
    }

    //レポート提出基準日確認
    function getCheckout2($model)
    {
        $query  = " SELECT CHAIRCD ";
        $query .= " FROM REP_STANDARDDATE_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,2)."' ||  '-' || 'H' ||  '-' || '{$model->curriculumCd}' ||  '-' || '".substr($model->field["REPNO"],1,6)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],7,2)." ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,13)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],14,2)." ";
        } else {
            $query .= "     SUBCLASSCD = '".substr($model->field["REPNO"],1,6)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],7,2)." ";
        }

        return $query;
    }

    //レポート提出基準日確認
    function getCheckout3($model)
    {
        $query  = " SELECT CHAIRCD ";
        $query .= " FROM CHAIR_STD_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     CHAIRCD = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "     SCHREGNO = '".$model->field["SCHREGNO"]."' ";

        return $query;
    }

    function deletesiteicheck($data, $model)
    {

        $query  = "SELECT GRAD_VALUE ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$data["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$data["SCHNO"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($data["SBCLS"],0,2)."' ||  '-' || 'H' ||  '-' || '{$model->curriculumCd}' ||  '-' || '".$data["SBCLS"]."' AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data["SBCLS"]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$data["SBCLS"]."' AND ";
        }
        $query .= "     STANDARD_SEQ = ".$data["KAISU"]." AND ";
        $query .= "     REPRESENT_SEQ  = ".$data["SAISU"]." AND ";
        $query .= "     RECEIPT_DATE = '" .str_replace("/","-",$data["SYDAY"])."' AND ";
        $query .= "     GRAD_VALUE IS NOT NULL AND ";
        $query .= "     GRAD_VALUE <> '' ";

        return $query;
    }

    function deleteallcheck($model)
    {

        $query  = "SELECT COUNT(GRAD_VALUE) AS GRAD_VALUE";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "       RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' AND ";
        $query .= "       GRAD_VALUE IS NOT NULL AND ";
        $query .= "       GRAD_VALUE <> '' ";

        return $query;
    }

    //講座登録確認 NO001
    function getDeleteIns($model)
    {

        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,2)."' ||  '-' || 'H' ||  '-' || '{$model->curriculumCd}' ||  '-' || '".substr($model->field["REPNO"],1,6)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],7,2)." AND ";
            $query .= "     REPRESENT_SEQ = ".substr($model->field["REPNO"],9,1)." AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,13)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],14,2)." AND ";
            $query .= "     REPRESENT_SEQ = ".substr($model->field["REPNO"],16,1)." AND ";
        } else {
            $query .= "     SUBCLASSCD = '".substr($model->field["REPNO"],1,6)."' AND ";
            $query .= "     STANDARD_SEQ = ".substr($model->field["REPNO"],7,2)." AND ";
            $query .= "     REPRESENT_SEQ = ".substr($model->field["REPNO"],9,1)." AND ";
        }
        $query .= "     SCHREGNO = '".$model->field["SCHREGNO"]."' AND ";
        $query .= "     RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;

    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = " INSERT INTO REP_PRESENT_DAT";
        $query .= " ( YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "   CLASSCD, ";
            $query .= "   SCHOOL_KIND, ";
            $query .= "   CURRICULUM_CD, ";
        }
        $query .= "   SUBCLASSCD,STANDARD_SEQ,REPRESENT_SEQ,SCHREGNO,RECEIPT_DATE, ";
        $query .= "   RECEIPT_TIME,CHAIRCD,REGISTERCD,UPDATED ) ";
        $query .= " VALUES( ";
        $query .= "     '".$model->Year."', ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     '".substr($model->field["REPNO"],1,2)."', ";
            $query .= "     'H' , ";
            $query .= "     '{$model->curriculumCd}' , ";
            $query .= "     '".substr($model->field["REPNO"],1,6)."', ";
            $query .= "     ".substr($model->field["REPNO"],7,2)." , ";
            $query .= "     ".substr($model->field["REPNO"],9,1)." , ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     '".substr($model->field["REPNO"],1,2)."', ";
            $query .= "     '".substr($model->field["REPNO"],4,1)."', ";
            $query .= "     '".substr($model->field["REPNO"],6,1)."', ";
            $query .= "     '".substr($model->field["REPNO"],8,6)."', ";
            $query .= "     ".substr($model->field["REPNO"],14,2)." , ";
            $query .= "     ".substr($model->field["REPNO"],16,1)." , ";
        } else {
            $query .= "     '".substr($model->field["REPNO"],1,6)."', ";
            $query .= "     ".substr($model->field["REPNO"],7,2)." , ";
            $query .= "     ".substr($model->field["REPNO"],9,1)." , ";
        }
        $query .= "     '".$model->field["SCHREGNO"]."', ";
        $query .= "     '".str_replace("/","-",$model->Date)."', ";
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

    /**
     * DELETE
     */
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "       RECEIPT_DATE = '".str_replace("/","-",$model->Date)."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuerysitei($data, $model)
    {
        $db = Query::dbCheckOut();
		$where = '';
        if ($model->Properties["useCurriculumcd"] == "1") {
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || SUBCLASSCD = '".substr($data["SBCLS"],0,2)."' || '-' || 'H' || '-' || '".$data["SBCLS"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data["SBCLS"]."' ";
        } else {
            $where .= "     SUBCLASSCD = '".$data["SBCLS"]."' ";
        }

        $query = knjm260Query::getCurriculumCd2($model, $where);
        $model->curriculumCd = $db->getOne($query);

        $db->autoCommit(false);

        $query  = "DELETE ";
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$data["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$data["SCHNO"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($data["SBCLS"],0,2)."' || '-' || 'H' ||  '-' || '{$model->curriculumCd}' ||  '-' || '".$data["SBCLS"]."' AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data["SBCLS"]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$data["SBCLS"]."' AND ";
        }
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