<?php
class knjo121Query extends Query {
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
    
    // APPLICで定められた学年のコードを取得する。
    function getApplicGrade()
    {
        $query = " SELECT ";
        $query .= "     DICTIONARY_CODE ";
        $query .= " FROM ";
        $query .= "     CE_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '01' AND";
        $query .= "     DICTIONARY_NO = '02'";

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
    
    
    
    //指導要録からデータ作成
    //コードのエラーチェック
    function getErrSyubetu($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SYUBETU ";
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_SYUBETU_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    function getErrGrade($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.GRADE as CHANGE, ";
        $query .= "     t2.GRADE as ZAIGAKU ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SCHOOL_CHANGE_DAT t1 left join CE_APP_Y_GRADE_DAT t2 on t1.YEAR = t2.YEAR and t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    function getShidouGrade($schrgeno)
    {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     CE_APP_Y_SHIDOU_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //SCHREG_BASE_MST用データ取得
    function getSBaseData($schregno)
    {
        $query .= " SELECT ";
        $query .= "     t1.SYUBETU, ";
        $query .= "     t1.NNAME as NAME, ";
        $query .= "     t2.NNAME as ALIAS, ";
        $query .= "     t1.NAME_KANA, ";
        $query .= "     t1.BIRTHDAY, ";
        $query .= "     t1.SEX, ";
        $query .= "     t1.ZIPCD, ";
        $query .= "     t1.NADDR, ";
        $query .= "     t1.NKATAGAKI, ";
        $query .= "     t5.KENJA_CODE as HANDICAP, ";
        $query .= "     t4.NNAME as ENT_SCHOOL, ";
        $query .= "     t4.NADDR || t4.NKATAGAKI as ENT_ADDR ";
        $query .= " FROM ";
        $query .= "     CE_APP_YOUROKU_SYUBETU_MST t1  ";
        $query .= "     left join CE_APP_Y_ALIAS_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t2.DATA_ROW = '1' ";
        $query .= "     left join (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     CE_APP_Y_SHIDOU_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHREGNO = '".$schregno."' AND ";
        $query .= "                     DATA_ROW = (SELECT ";
        $query .= "                                     MAX(DATA_ROW) ";
        $query .= "                                 FROM ";
        $query .= "                                     CE_APP_Y_SHIDOU_DAT ";
        $query .= "                                 WHERE ";
        $query .= "                                     SCHREGNO = '".$schregno."' ";
        $query .= "                                 ) ";
        $query .= "                 ) t3 on t1.SCHREGNO = t3.SCHREGNO  ";
        $query .= "     left join CE_APP_YOUROKU_FROM_MST t4 on t1.SCHREGNO = t4.SCHREGNO ";
        $query .= "     left join CE_APP_NAME_MST t5 on t3.SYUBETU = t5.DICTIONARY_CODE and t5.DICTIONARY_KIND = '02' and t5.DICTIONARY_NO = '09' ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //SCHREG_ATTENDREC_DAT用データ取得
    function getSAttendRecData($schregno, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT  ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.GRADE, ";
        $query .= "         t1.CLASS_CNT, ";
        $query .= "         t1.STOP_CNT, ";
        $query .= "         t1.MUST_CNT, ";
        $query .= "         t1.ABSENCE_CNT, ";
        $query .= "         t1.ATTEND_CNT, ";
        $query .= "         t2.YEAR, ";
        $query .= "         t3.KENJA_CODE as GRADECD ";
        $query .= "     FROM ";
        $query .= "         CE_APP_Y_SHIDOU_DAT t1  ";
        $query .= "         left join CE_APP_Y_GRADE_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.GRADE = t2.GRADE  ";
        $query .= "         left join CE_APP_NAME_MST t3 on t1.GRADE = t3.DICTIONARY_CODE and t3.DICTIONARY_KIND = '01' and t3.DICTIONARY_NO = '02' ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.DATA_ROW ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
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
        $query .= "     CE_APP_Y_PARENT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     DATA_ROW in ('1','2') ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //学齢簿情報取得
    function getGBaseData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.S_NNAME as NAME, ";
        $query .= "     t1.S_NAME_KANA as NAME_KANA, ";
        $query .= "     t1.S_NADDR as NADDR, ";
        $query .= "     t1.S_NKATAGAKI as NKATAGAKI, ";
        $query .= "     t1.S_ZIPCD as ZIPCD, ";
        $query .= "     t1.S_SEX as SEX, ";
        $query .= "     t1.S_BIRTH_YEAR || '-' || S_BIRTH_MONTH || '-' || S_BIRTH_DAY as BIRTHDAY, ";
        $query .= "     t1.S_GRADE, ";
        $query .= "     t1.CLASS_KUBUN, ";
        $query .= "     t1.P_NNAME, ";
        $query .= "     t1.P_NAME_KANA, ";
        $query .= "     t1.P_NADDR, ";
        $query .= "     t1.P_NKATAGAKI, ";
        $query .= "     t1.P_ZIPCD, ";
        $query .= "     t1.P_TELNO, ";
        $query .= "     t2.* ";
        $query .= " FROM ";
        $query .= "     CE_APP_GAKUREIBO_F_MST t1";
        $query .= "     left join CE_APP_GAKUREIBO_S_MST t2";
        $query .= "     on t1.schregno = t2.schregno";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
}
?>
