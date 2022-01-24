<?php

require_once('for_php7.php');

class knjm270Query extends Query {

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

    //登録済み生徒データ
    function getSch($model)
    {
        $query  = " SELECT t1.SCHREGNO,t2.NAME_SHOW,RECEIPT_TIME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= " t1.SUBCLASSCD, ";
        }
        $query .= " t1.SUBCLASSCD AS SUBCLASSCD_ONLY, ";
        $query .= " t1.CHAIRCD,t1.RECEIPT_DATE, ";
        $query .= " t4.SUBCLASSNAME,t1.STANDARD_SEQ,t1.REPRESENT_SEQ,t1.GRAD_VALUE,t1.GRAD_TIME, ";
        $query .= " t3.NAMECD2,t3.NAME1 ";
        $query .= " FROM REP_PRESENT_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " left join V_NAME_MST t3 on t1.GRAD_VALUE = t3.NAMECD2 and t1.YEAR = t3.YEAR and t3.NAMECD1 = 'M003' ";
        $query .= " left join SUBCLASS_MST t4 on t1.SUBCLASSCD = t4.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t1.CLASSCD = t4.CLASSCD ";
            $query .= "     AND t1.SCHOOL_KIND = t4.SCHOOL_KIND ";
            $query .= "     AND t1.CURRICULUM_CD = t4.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '" .$model->Year."' and ";
        $query .= "     t1.GRAD_DATE = '".str_replace("/","-",$model->Date)."' and ";
        $query .= "     t1.GRAD_VALUE is not null ";
        $query .= " ORDER BY t1.GRAD_DATE DESC, t1.GRAD_TIME DESC,t1.SCHREGNO ";

        return $query;
    }

    //教育課程取得
    function getCurriculumCd($model)
    {
        $query .= " SELECT ";
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
        $query  = " SELECT RECEIPT_DATE ";  //NO002
        $query .= " FROM REP_PRESENT_DAT ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,2)."' || '-' || 'H' || '-' || '{$model->curriculumCd}' || '-' || '".substr($model->field["REPNO"],1,6)."' AND ";
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
        $query .= " ORDER BY RECEIPT_DATE DESC ";   //NO002

        return $query;
    }

    //添削者登録確認
    function getCheckstf($model)
    {
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
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,2)."' || '-' || 'H' || '-' || '{$model->curriculumCd}' ||  '-' ||'".substr($model->field["REPNO"],1,6)."') ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,13)."') ";
        } else {
            $query .= "         SUBCLASSCD = '".substr($model->field["REPNO"],1,6)."') ";
        }
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
        $query  = " SELECT NAMECD2,NAME1 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'M003' ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  STAFFCD = '".$model->field["STAFF"]."', ";
        $query .= "  GRAD_VALUE = '".$model->field["HYOUKA"]."', ";
        $query .= "  GRAD_DATE = '".str_replace("/","-",$model->Date)."', ";
        $query .= "  GRAD_TIME = current time, ";
        if ($model->field["HYOUKA"] == 1){
            $query .= "  REPRESENT_PRINT = '0', ";
        }else {
            $query .= "  REPRESENT_PRINT = '', ";
        }
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->field["REPNO"],1,2)."' || '-' || 'H' || '-' || '{$model->curriculumCd}' || '-' || '".substr($model->field["REPNO"],1,6)."' AND ";
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
        $query .= "     RECEIPT_DATE = '".$model->maxreceipt_date."' "; //NO002

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
		$where = '';
        if ($model->Properties["useCurriculumcd"] == "1") {
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || SUBCLASSCD = '".substr($model->sucl,7,2)."' || '-' || 'H' || '-' || '".substr($model->sucl,7,13)."' ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sucl."' ";
        } else {
            $where .= "     SUBCLASSCD = '".$model->sucl."' ";
        }

        $query = knjm270Query::getCurriculumCd2($model, $where);

        $model->curriculumCd = $db->getOne($query);
        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  GRAD_VALUE = '".VARS::post("GVALSUB")."', ";
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sucl,7,2)."' || '-' || 'H' || '-' || '{$model->curriculumCd}' || '-' || '".substr($model->sucl,7,13)."' AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sucl."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->sucl."' AND ";
        }
        $query .= "     STANDARD_SEQ = $model->stsq AND ";
        $query .= "     REPRESENT_SEQ = $model->rsq AND ";
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
        $query .= "  GRAD_TIME = null, ";
        $query .= "  REPRESENT_PRINT = '', ";
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$model->Year."' AND ";
        $query .= "       GRAD_DATE = '".str_replace("/","-",$model->Date)."' ";

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
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || SUBCLASSCD = '".substr($data["SBCLS"],0,2)."' || '-' || 'H' || '-' || '".substr($data["SBCLS"],0,6)."' ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data["SBCLS"]."' ";
        } else {
            $where .= "     SUBCLASSCD = '".$data["SBCLS"]."' ";
        }
        $query = knjm270Query::getCurriculumCd2($model, $where);
        $model->curriculumCd = $db->getOne($query);
        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  REP_PRESENT_DAT SET  ";
        $query .= "  STAFFCD = '', ";
        $query .= "  GRAD_VALUE = '', ";
        $query .= "  GRAD_DATE = null, ";
        $query .= "  GRAD_TIME = null, ";
        $query .= "  REPRESENT_PRINT = '', ";
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .$data["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$data["SCHNO"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($data["SBCLS"],0,2)."' || '-' || 'H' || '-' || '{$model->curriculumCd}' || '-' || '".substr($data["SBCLS"],0,6)."' AND ";
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