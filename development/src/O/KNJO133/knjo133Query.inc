<?php
class knjo133Query extends Query {
    //ƒf[ƒ^‚Ì—L–³
    function countData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CE_APP_HOKEN_S_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //‘—Mæî•ñ
    function getToSchool($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_HOKEN_TO_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //‘—MŒ³î•ñ
    function getFromSchool($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_HOKEN_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //¶“kŒ’Nf’f•[î•ñ
    function getHokenSMst($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_HOKEN_S_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //Ž™“¶¶“k„Šî–{î•ñ„’ÊÌ–¼
    function getAlias($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_H_ALIAS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //Ž™“¶¶“k„Šî–{î•ñ„Œ»ZŠ_‚»‚Ì‘¼
    function getStAdd($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_H_OTHERADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //Ž™“¶¶“k„ÝŠwŠw”N
    function getZaigaku($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CE_APP_H_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_ROW ";
        
        return $query;
    }
    
    //Ž™“¶¶“k„¬ŠwZ
    function getHokenPMst($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_HOKEN_P_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND ";
        $query .= "     NNAME IS NOT NULL ";
        
        return $query;
    }
    
    //Ž™“¶¶“k„’†ŠwZ
    function getHokenJMst($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_HOKEN_J_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " AND ";
        $query .= "     NNAME IS NOT NULL ";
        
        return $query;
    }
    
    //Œ’Nf’f•[î•ñ„Œ’Nf’fˆê”Ê
    function getMedical($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_H_MEDICAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //Œ’Nf’f•[î•ñ„Ž•‰ÈŒŸ¸
    function getDental($schregno, $mode = "")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "     COUNT(*) ";
        }else{
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     CE_APP_H_DENTAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "     DATA_ROW ";
        }
        
        return $query;
    }
    
    //‘—M“úŽžŽæ“¾
    function getSendDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SEND_DATE ";
        $query .= " FROM ";
        $query .= "     CE_APP_HOKEN_FROM_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //ŠwZ–¼Žæ“¾
    function getSchoolName($schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolKind."' ";

        return $query;
    }

        
    //ÝZ¶ŒŸõƒ{ƒ^ƒ“
    function getSchregregd_data($model, $ych, $gch, $sch) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.NAME AS STUDENTNAME, ";
        $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO || '”Ô' AS NENKUMI ";
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


    //‚PƒŒƒR[ƒhŽæ“¾iŠwÐÝÐƒf[ƒ^j
    public function getSchregregdData($model)
    {
        $db = Query::dbCheckOut();

        $header = knjo133Query::getJudgeTable($model->SCHREGNO, $db);

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

     //ŽQÆ‚·‚éƒe[ƒuƒ‹‚ð”»’fF‘²‹Æ—p(GRD_XXX)AÝÐ—p(SCH_XXX)
    public function getJudgeTable($schregno, $db)
    {
        $query  = "SELECT COUNT(*) FROM GRD_BASE_MST WHERE SCHREGNO = '".$schregno."' ";

        $ret_val = $db->getOne($query);
        return ($ret_val) ? "GRD" : "SCHREG" ;
    }
}

?>
