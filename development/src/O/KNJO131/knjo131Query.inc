<?php
class knjo131Query extends Query {
    //DELETE
    function delete($schregno, $table)
    {
        $query  = " DELETE FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //賢者コード取得
    function getKenjaCode($no)
    {
        $query  = " SELECT ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        $query .= " FROM ";
        $query .= "     CE_APP_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_KIND = '03' AND ";
        $query .= "     DICTIONARY_NO = '".$no."' ";
        $query .= " ORDER BY ";
        $query .= "     DICTIONARY_CODE, ";
        $query .= "     KENJA_CODE ";
        
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
    
    //学年エラーチェック
    function getGradeCheck($schregno)
    {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     CE_APP_H_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //MEDEXAM_DET_DAT用データ取得
    function getMedexamData($schregno, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_H_MEDICAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "    YEAR ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //MEDEXAM_TOOTH_DAT用データ取得
    function getToothData($schregno, $mode="")
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
        $query .= "         CE_APP_H_DENTAL_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ORDER BY ";
        $query .= "         YEAR ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }

    
    
    //在校生検索ボタン
    function getSchregregd_data($model, $ych, $gch, $sch) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.NAME AS STUDENTNAME, ";
        $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".$ych."' AND ";
        $query .= "     T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$gch."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$sch."' AND ";
        $query .= "     T1.GRADE        = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS     = T3.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }


    //１レコード取得（学籍在籍データ）
    public function getSchregregdData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjo131Query::getJudgeTable($model->SCHREGNO, $db);
        $query  = " SELECT ";
        $query .= " T1.YEAR, ";
        $query .= " T1.GRADE, ";
        $query .= " T1.SEMESTER, ";
        $query .= " T1.HR_CLASS, ";
        $query .= "    T3.STAFFNAME_SHOW AS HR_TEARCH,";
        $query .= " T4.COURSENAME, ";
        $query .= " T5.MAJORNAME, ";
        $query .= " T6.HR_NAME ";
        $query .= " FROM ";
        $query .= "   (";
        $query .= "   SELECT ";
        $query .= "     SUBSTR(TT1.WORK,1,4) AS YEAR, ";
        $query .= "     SUBSTR(TT1.WORK,5,1) AS SEMESTER, ";
        $query .= "     TT2.GRADE, ";
        $query .= "     TT2.HR_CLASS, ";
        $query .= "     TT2.COURSECD, ";
        $query .= "     TT2.MAJORCD ";
        $query .= "   FROM ";
        $query .= "     (SELECT SCHREGNO,MAX(YEAR || SEMESTER) AS WORK FROM ". $header ."_REGD_DAT WHERE SCHREGNO = '" .$model->SCHREGNO ."' GROUP BY SCHREGNO) TT1, ";
        $query .= "     ". $header ."_REGD_DAT TT2 ";
        $query .= "   WHERE ";
        $query .= "     TT1.SCHREGNO = '" .$model->SCHREGNO ."'";
        $query .= "     AND   TT1.SCHREGNO = TT2.SCHREGNO ";
        $query .= "     AND   SUBSTR(TT1.WORK,1,4) = TT2.YEAR ";
        $query .= "     AND   SUBSTR(TT1.WORK,5,1) = TT2.SEMESTER ";
        $query .= "   ) T1";
        $query .= "   LEFT JOIN ";
        $query .= "     ". $header ."_REGD_HDAT T6 ";
        $query .= "   ON T1.YEAR     = T6.YEAR ";
        $query .= "     AND   T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND   T1.GRADE = T6.GRADE ";
        $query .= "     AND   T1.HR_CLASS = T6.HR_CLASS ";
        $query .= "   LEFT JOIN STAFF_MST T3 ON  T6.TR_CD1 = T3.STAFFCD ";
        $query .= "   LEFT JOIN  COURSE_MST T4 ON  T1.COURSECD = T4.COURSECD ";
        $query .= "   LEFT JOIN MAJOR_MST T5 ON  T1.COURSECD = T5.COURSECD AND T1.MAJORCD = T5.MAJORCD ";
        $query .= " ORDER BY T1.YEAR DESC, T1.GRADE DESC ,T1.SEMESTER DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

     //参照するテーブルを判断：卒業用(GRD_XXX)、在籍用(SCH_XXX)
    public function getJudgeTable($schregno, $db)
    {
        $query  = "SELECT COUNT(*) FROM GRD_BASE_MST WHERE SCHREGNO = '".$schregno."' ";

        $ret_val = $db->getOne($query);
        return ($ret_val) ? "GRD" : "SCHREG" ;
    }
}
?>
