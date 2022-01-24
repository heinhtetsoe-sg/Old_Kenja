<?php
class knjo101Query extends Query {
    //課程学科取得
    function getKatei()
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      coursecd || ',' || majorcd AS value, ";
        $query .= "      coursecd || majorcd || '　' ||coursename || majorabbv AS label ";
        $query .= "  FROM ";
        $query .= "      v_course_major_mst ";
        $query .= "  WHERE ";
        $query .= "      year = '".CTRL_YEAR."' ";
        $query .= "  ORDER BY ";
        $query .= "      label ";
        
        return $query;
    }
    //コース取得
    function getCourse()
    {
        $query  = " SELECT ";
        $query .= "     coursecode as value, ";
        $query .= "     coursecode || '　' || coursecodename as label ";
        $query .= " FROM ";
        $query .= "     v_coursecode_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     value ";
        
        return $query;
    }
    //年組取得
    function getGrade()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.grade || ',' || T1.hr_class AS value, ";
        $query .= "     T1.hr_name as label ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat T1 ";
        $query .= " WHERE ";
        $query .= "     T1.year     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester = '".CTRL_SEMESTER."'  ";
        //if (AUTHORITY == DEF_UPDATE_RESTRICT) {   //ここを入れるとSTAFFCDの担当クラスしか出なくなる
            //$query .= " AND ";
            //$query .= "     (T1.tr_cd1    = '".STAFFCD."' OR T1.tr_cd2    = '".STAFFCD."' OR T1.tr_cd3    = '".STAFFCD."' OR ";
            //$query .= "      T1.subtr_cd1 = '".STAFFCD."' OR T1.subtr_cd2 = '".STAFFCD."' OR T1.subtr_cd3 = '".STAFFCD."') ";
        //}
        $query .= " ORDER BY ";
        $query .= "     value ";
        
        return $query;
    }
    
    //DELETE
    function delete($schregno, $table)
    {
        $query  = " DELETE FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    function delete2($schregno, $table)
    {
        $query  = " DELETE FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND ";
        $query .= "     SCHOOLCD = '1' ";
        
        return $query;
    }
    
    
    //コード値エラーチェック
    function getSchGrade($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.KUBUN, ";
        $query .= "     t1.GRADE as CHANGE, ";
        $query .= "     t2.GRADE as ZAIGAKU ";
        $query .= " FROM ";
        $query .= "     H_APP_Y_SCHOOL_CHANGE_DAT t1 ";
        $query .= "     left join H_APP_Y_GRADE_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = t2.YEAR ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.DATA_ROW ";
        
        return $query;
    }
    function getShidou($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.GRADE as SHIDOU1, ";
        $query .= "     t2.GRADE as SHIDOU2 ";
        $query .= " FROM ";
        $query .= "     H_APP_Y_SHIDOU1_DAT t1 full outer join H_APP_Y_SHIDOU2_DAT t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' OR ";
        $query .= "     t2.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    function getShiGrade($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         a1.*, ";
        $query .= "         a2.GRADE as ZAIGAKU ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             case when SCHREGNO1 IS NOT NULL then SCHREGNO1 else SCHREGNO2 end as SCHREGNO, ";
        $query .= "             case when SHIDOU1 IS NOT NULL then SHIDOU1 else SHIDOU2 end as GRADE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 t1.SCHREGNO as SCHREGNO1, ";
        $query .= "                 t1.GRADE as SHIDOU1, ";
        $query .= "                 t2.SCHREGNO as SCHREGNO2, ";
        $query .= "                 t2.GRADE as SHIDOU2 ";
        $query .= "             FROM ";
        $query .= "                 H_APP_Y_SHIDOU1_DAT t1 FULL JOIN H_APP_Y_SHIDOU2_DAT t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "             ) ";
        $query .= "         ) a1 ";
        $query .= "         left join H_APP_Y_GRADE_DAT a2 on a1.SCHREGNO = a2.SCHREGNO and a1.GRADE = a2.GRADE ";
        $query .= "     WHERE ";
        $query .= "         a1.SCHREGNO = '".$schregno."' ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     ZAIGAKU IS NULL ";
        
        return $query;
    }
    
    
    
    //SCHREG_BASE_MST用データ取得
    function getBaseData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.STUDENT_NNAME, ";
        $query .= "     t2.NNAME as ALIAS, ";
        $query .= "     t1.STUDENT_KANA, ";
        $query .= "     t1.STUDENT_BIRTHDAY, ";
        $query .= "     t1.STUDENT_SEX, ";
        $query .= "     t1.STUDENT_ZIPCD, ";
        $query .= "     t1.STUDENT_NADDR, ";
        $query .= "     t1.STUDENT_NKATAGAKI, ";
        $query .= "     case when t1.YOUROKU_SYUBETU = 'K01' then '001' ";
        $query .= "          when t1.YOUROKU_SYUBETU = 'K02' then '002' ";
        $query .= "          when t1.YOUROKU_SYUBETU = 'K03' then '003' else NULL end as HANDICAP, ";
        $query .= "     t3.OUT_DATE, ";
        $query .= "     t4.SCHOOL_NNAME, ";
        $query .= "     t4.SCHOOL_NADDR, ";
        $query .= "     t4.SCHOOL_NKATAGAKI, ";
        $query .= "     t3.KATEI_NAME, ";
        $query .= "     t5.KENJA_CODE as KATEI, ";
        $query .= "     t3.GAKKA_NAME, ";
        $query .= "     t6.KENJA_CODE as GAKKA ";
        $query .= " FROM ";
        $query .= "     H_APP_YOUROKU_SYUBETU_MST t1  ";
        $query .= "     left join H_APP_Y_STUDENT_ALIAS_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t2.DATA_ROW = '1'  ";
        $query .= "     left join H_APP_YOUROKU_IDOU_MST t3 on t1.SCHREGNO = t3.SCHREGNO  ";
        $query .= "     left join H_APP_YOUROKU_FROM_MST t4 on t1.SCHREGNO = t4.SCHREGNO ";
        $query .= "     left join H_APP_NAME_MST t5 on t3.KATEI_NAME = t5.DICTIONARY_CODE and t5.DICTIONARY_KIND = '02' and t5.DICTIONARY_NO = '02' ";
        $query .= "     left join H_APP_NAME_MST t6 on t3.GAKKA_NAME = t6.DICTIONARY_CODE and t6.DICTIONARY_KIND = '02' and t6.DICTIONARY_NO = '03' ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //SCHREG_REGD_DAT用データ取得
    function getEntHistData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.OUT_DATE, ";
        $query .= "     t2.SCHOOL_NNAME, ";
        $query .= "     t2.SCHOOL_NADDR, ";
        $query .= "     t2.SCHOOL_NKATAGAKI, ";
        $query .= "     t1.KATEI_NAME, ";
        $query .= "     t1.GAKKA_NAME ";
        $query .= " FROM ";
        $query .= "     H_APP_YOUROKU_IDOU_MST t1  ";
        $query .= "     left join H_APP_YOUROKU_FROM_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //SCHREG_ATTENDREC_DAT更新用データ
    function getAttendRecData($schregno, $mode="")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     t1.*, ";
            $query .= "     t2.YEAR ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         RIGHT('00' || RTRIM(CHAR(GRADECD + STAY_CNT)), 2) as GRADECD, ";
        $query .= "         CLASS_CNT, ";
        $query .= "         STOP_CNT, ";
        $query .= "         ABROAD_CNT, ";
        $query .= "         MUST_CNT, ";
        $query .= "         ABSENCE_CNT, ";
        $query .= "         ATTEND_CNT, ";
        $query .= "         BIKOU, ";
        $query .= "         GRADECD as GAKUNEN ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             GRADE, ";
        $query .= "             case when STAY_CNT is null then 0 else STAY_CNT end as STAY_CNT, ";
        $query .= "             CLASS_CNT, ";
        $query .= "             STOP_CNT, ";
        $query .= "             ABROAD_CNT, ";
        $query .= "             MUST_CNT, ";
        $query .= "             ABSENCE_CNT, ";
        $query .= "             ATTEND_CNT, ";
        $query .= "             BIKOU, ";
        $query .= "             KENJA_CODE as GRADECD ";
        $query .= "         FROM ";
        $query .= "             H_APP_Y_SHIDOU1_DAT a1 left join H_APP_NAME_MST a2 on a1.GRADE = a2.DICTIONARY_CODE and a2.DICTIONARY_KIND = '01' AND a2.DICTIONARY_NO in ('03','05') ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO = '".$schregno."' ";
        $query .= "         ORDER BY ";
        $query .= "             GRADE ";
        $query .= "         ) ";
        $query .= "     ) t1 ";
        $query .= "     left join (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     H_APP_Y_GRADE_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     (SCHREGNO, GRADE, YEAR) in (SELECT ";
        $query .= "                                                     SCHREGNO, ";
        $query .= "                                                     GRADE, ";
        $query .= "                                                     MAX(YEAR) ";
        $query .= "                                                 FROM ";
        $query .= "                                                     H_APP_Y_GRADE_DAT ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     SCHREGNO, ";
        $query .= "                                                     GRADE ";
        $query .= "                                                 ) ";
        $query .= "                 ) t2 on t1.GRADE = t2.GRADE and t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";

        return $query;
    }
    
    function getAttendRecData2($schregno, $mode="")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     t1.*, ";
            $query .= "     t2.YEAR ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         RIGHT('00' || RTRIM(CHAR(GRADE + STAY_CNT)), 2) as GRADECD, ";
        $query .= "         CLASS_CNT, ";
        $query .= "         STOP_CNT, ";
        $query .= "         ABROAD_CLASS_CNT, ";
        $query .= "         MUST_CNT, ";
        $query .= "         ABSENCE_CNT, ";
        $query .= "         ATTEND_CNT, ";
        $query .= "         BIKOU, ";
        $query .= "         GRADECD as GAKUNEN ";
        $query .= "     FROM ";
        $query .= "             SCHREGNO, ";
        $query .= "             GRADE, ";
        $query .= "             case when STAY_CNT is null then 0 else STAY_CNT end as STAY_CNT, ";
        $query .= "             CLASS_CNT, ";
        $query .= "             STOP_CNT, ";
        $query .= "             ABROAD_CNT, ";
        $query .= "             MUST_CNT, ";
        $query .= "             ABSENCE_CNT, ";
        $query .= "             ATTEND_CNT, ";
        $query .= "             BIKOU, ";
        $query .= "             KENJA_CODE as GRADECD ";
        $query .= "         FROM ";
        $query .= "             H_APP_Y_SHIDOU2_DAT a1 left join H_APP_NAME_MST a2 on a1.GRADE = a2.DICTIONARY_CODE and a2.DICTIONARY_KIND = '01' AND a2.DICTIONARY_NO in ('03','05') ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO = '".$schregno."' ";
        $query .= "         ORDER BY ";
        $query .= "             GRADE ";
        $query .= "         ) ";
        $query .= "     ) t1 ";
        $query .= "     left join (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     H_APP_Y_GRADE_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     (SCHREGNO, GRADE, YEAR) in (SELECT ";
        $query .= "                                                     SCHREGNO, ";
        $query .= "                                                     GRADE, ";
        $query .= "                                                     MAX(YEAR) ";
        $query .= "                                                 FROM ";
        $query .= "                                                     H_APP_Y_GRADE_DAT ";
        $query .= "                                                 GROUP BY ";
        $query .= "                                                     SCHREGNO, ";
        $query .= "                                                     GRADE ";
        $query .= "                                                 ) ";
        $query .= "                 ) t2 on t1.GRADE = t2.GRADE and t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }

    //保護者情報
    function getParentData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     DATA_ROW, ";
        $query .= "     NNAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     NADDR, ";
        $query .= "     NKATAGAKI, ";
        $query .= "     ZIPCD ";
        $query .= " FROM ";
        $query .= "     H_APP_Y_PARENT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     DATA_ROW in ('1','2') ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //H_APP_Y_COMMON_SUBJ_DAT取得
    function getCommonSubj($schregno, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_COMMON_SUBJ_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         DATA_ROW ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //H_APP_Y_EXPERT_SUBJ_DAT取得
    function getExpertSubj($schregno, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_EXPERT_SUBJ_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         DATA_ROW ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    //H_APP_SUBCLASS_MSTから賢者コード取得
    function getSubclassCd($subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     substr(KENJA_CODE, 1, 2) as CLASSCD, ";
        $query .= "     KENJA_CODE as SUBCLASSCD, ";
        $query .= "     CURRICULUM_CD ";
        $query .= " FROM ";
        $query .= "     H_APP_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_CODE = '".$subclasscd."' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE ";
        
        return $query;
    }
    
    //SCHREG_TRANSFER_DATの更新用データ
    function getTransfer($schregno, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_STAY_ABROAD_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         DATA_ROW ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    //SCHREG_TRANSFER_DATに単位数更新
    function updateAbroadCredit($schregno)
    {
        $query  = " UPDATE  ";
        $query .= "     SCHREG_TRANSFER_DAT  ";
        $query .= " SET  ";
        $query .= "     (ABROAD_CREDITS,ABROAD_CLASSDAYS, REGISTERCD, UPDATED) = (SELECT ";
        $query .= "                                                                     t1.ABROAD_TANNI, ";
        $query .= "                                                                     t2.ABROADCNT, ";
        $query .= "                                                                     '".STAFFCD."', ";
        $query .= "                                                                     sysdate() ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     H_APP_YOUROKU_IDOU_MST t1  ";
        $query .= "                                                                     left join (SELECT ";
        $query .= "                                                                                     SCHREGNO, ";
        $query .= "                                                                                     SUM(ABROAD_CNT) as ABROADCNT ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     H_APP_Y_SHIDOU1_DAT ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     SCHREGNO = '".$schregno."' ";
        $query .= "                                                                                 GROUP BY ";
        $query .= "                                                                                     SCHREGNO ";
        $query .= "                                                                                 ) t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     t1.SCHREGNO = '".$schregno."' ";
        $query .= "                                                                 )  ";
        $query .= " WHERE  ";
        $query .= "     SCHREGNO = '".$schregno."' AND  ";
        $query .= "     TRANSFER_SDATE = (SELECT  ";
        $query .= "                             MAX(TRANSFER_SDATE)  ";
        $query .= "                         FROM  ";
        $query .= "                             SCHREG_TRANSFER_DAT  ";
        $query .= "                         WHERE  ";
        $query .= "                             SCHREGNO = '".$schregno."' AND  ";
        $query .= "                             TRANSFERCD = '1'  ";
        $query .= "                         )  ";
        
        return $query;
    }
}
?>
