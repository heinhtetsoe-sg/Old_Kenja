<?php

require_once('for_php7.php');


class knjd328Query extends Query {

    //一覧リスト---出身学校
    function GetFinschool($model){

        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD AS OUTCD, ";
        $query .= "     FINSCHOOL_NAME AS OUTNAME ";
        $query .= " FROM   V_FINSCHOOL_MST ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        FINSCHOOLCD IN (SELECT DISTINCT FINSCHOOLCD FROM SCHREG_BASE_MST t1,SCHREG_REGD_DAT t2 ";
        $query .= "                         WHERE FINSCHOOLCD IS NOT NULL AND t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "                               AND t2.SEMESTER = '".CTRL_SEMESTER."' AND t2.SCHREGNO = t1.SCHREGNO) ";
        $query .= " ORDER BY FINSCHOOLCD ";

        return $query;
    }

    //一覧リスト---塾
    function GetPrischool($model){
        $query  = " SELECT ";
        $query .= "     PRISCHOOLCD AS OUTCD, ";
        $query .= "     PRISCHOOL_NAME AS OUTNAME ";
        $query .= " FROM ";
        $query .= "     V_PRISCHOOL_MST ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        PRISCHOOLCD IN (SELECT DISTINCT PRISCHOOLCD FROM SCHREG_BASE_MST t1,SCHREG_REGD_DAT t2 ";
        $query .= "                         WHERE PRISCHOOLCD IS NOT NULL AND t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "                               AND t2.SEMESTER = '".CTRL_SEMESTER."' AND t2.SCHREGNO = t1.SCHREGNO) ";
        $query .= " ORDER BY PRISCHOOLCD";

        return $query;
    }

    //コースを取得
    function get_Coursecode($model){

        $query  = "SELECT ";
        if ($model->jhflg == 1) {
            $query .= "    t1.COURSE_SEQ,t1.GRADE,t2.COURSECODENAME ";
        } else {
            $query .= "    t1.COURSE_SEQ,t1.GRADE,t1.COURSECODE,t1.GROUP_NAME as COURSECODENAME ";
        }
        $query .= "FROM ";
        $query .= "    COURSE_GROUP_DAT t1 ";
        $query .= "    LEFT JOIN V_COURSECODE_MST t2 ON t1.YEAR = t2.YEAR ";
        $query .= "    AND t1.COURSECODE = t2.COURSECODE ";
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        if ($model->jhflg == 2) {
            $query .= "GROUP BY ";
            $query .= "    t1.COURSE_SEQ,t1.GRADE,t1.COURSECODE,t1.GROUP_NAME ";
        }
        $query .= "ORDER BY ";
        $query .= "    t1.GRADE,t1.COURSE_SEQ,t1.COURSECODE ";

        return $query;
    }

    //中高判別
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

    //学校又は、塾名取得
    function Getfspr($model,$fsprcd){
        if ($model->output2 == 2) {
            $query  = " SELECT ";
            $query .= "     FINSCHOOL_NAME AS FSPRNM";
            $query .= " FROM ";
            $query .= "     V_FINSCHOOL_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     FINSCHOOLCD = '".$fsprcd."' ";
        } else {
            $query  = " SELECT ";
            $query .= "     PRISCHOOL_NAME AS FSPRNM";
            $query .= " FROM ";
            $query .= "     V_PRISCHOOL_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     PRISCHOOLCD = '".$fsprcd."' ";
        }

        return $query;
    }

    //ＣＳＶ出力
    function getOutputQueryCsv($model,$fipicd){

        //データ出力
        $query  = "SELECT ";
        $query .= "    t1.GRADE,L3.COURSE_SEQ,L3.COURSECODE,L2.COURSECODENAME, ";
        $query .= "    t2.NAME_SHOW,cast(right(t1.SCHREGNO,4) as char(4)) as SCHREGNO, ";
        $query .= "    L1.HR_NAMEABBV || '-' || t1.ATTENDNO as HRNO, ";
        //定期テスト
        $query .= "    L4.SEM1_INTER_REC_TOTAL,L4.SEM1_INTER_REC_RANK, ";
        $query .= "    L4.SEM1_TERM_REC_TOTAL,L4.SEM1_TERM_REC_RANK, ";
        $query .= "    L4.SEM1_REC_TOTAL,L4.SEM1_REC_RANK, ";
        $query .= "    L4.SEM2_INTER_REC_TOTAL,L4.SEM2_INTER_REC_RANK, ";
        $query .= "    L4.SEM2_TERM_REC_TOTAL,L4.SEM2_TERM_REC_RANK, ";
        $query .= "    L4.SEM2_REC_TOTAL,L4.SEM2_REC_RANK, ";
        $query .= "    L4.SEM3_TERM_REC_TOTAL,L4.SEM3_TERM_REC_RANK, ";
        $query .= "    L4.SEM3_REC_TOTAL,L4.SEM3_REC_RANK, ";
        $query .= "    L4.GRADE_RECORD_TOTAL,L4.GRADE_RECORD_RANK, ";

        //実力１～９
        $tableNameCnt = 1;
        $fieldNameCnt = 1;
        for ($cnt = 0; $cnt < 9; $cnt++) {
            $lastSep = $cnt == 8 ? "" : ",";
            $query .= knjd328Query::getShamexamSelectQuery($tableNameCnt, $fieldNameCnt, $lastSep);
            $tableNameCnt = $tableNameCnt + 4;
            $fieldNameCnt++;
        }

        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT t1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L1 ON t1.GRADE || t1.HR_CLASS = L1.GRADE || L1.HR_CLASS ";
        $query .= "    AND t1.YEAR = L1.YEAR AND t1.SEMESTER = L1.SEMESTER ";
        $query .= "    LEFT JOIN V_COURSECODE_MST L2 ON L2.YEAR = t1.YEAR ";
        $query .= "    AND L2.COURSECODE = t1.COURSECODE ";
        $query .= "    LEFT JOIN COURSE_GROUP_DAT L3 ON L3.YEAR = t1.YEAR ";
        $query .= "    AND L3.GRADE = t1.GRADE ";
        if ($model->jhflg == 1) {
            $query .= "    AND L3.HR_CLASS = '000' ";
        } else {
            $query .= "    AND L3.HR_CLASS = t1.HR_CLASS ";
        }
        $query .= "    AND L3.COURSECODE = t1.COURSECODE ";
        //席次データ
        $query .= "    LEFT JOIN RECORD_RANK_DAT L4 ON L4.YEAR = t1.YEAR ";
        $query .= "    AND L4.SCHREGNO = t1.SCHREGNO ";
        $query .= "    AND L4.RANK_DIV = '2' ";

        //実力１～９
        $tableNameCnt = 1;
        $shamexamCdArray = array("01", "02", "03", "04", "05", "06", "07", "08", "09");
        foreach ($shamexamCdArray as $val) {
            $query .= knjd328Query::getShamexamJoinQuery($tableNameCnt, $val, $model);
        }

        $query .= "    ,SCHREG_BASE_MST t2 ";
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->output2 == 2) {
            $query .= "    AND t2.FINSCHOOLCD = '".$fipicd."' ";
        } else {
            $query .= "    AND t2.PRISCHOOLCD = '".$fipicd."' ";
        }
        $query .= "    AND t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "ORDER BY ";
        $query .= "    t1.GRADE,L3.COURSE_SEQ,L3.COURSECODE,t1.HR_CLASS,t1.ATTENDNO ";

        return $query;
    }

    function getShamexamSelectQuery($tableNameCnt, $fieldNameCnt, $lastSep) {
        $table1 = "SX".$tableNameCnt;
        $table2 = "SX".($tableNameCnt + 1);
        $table3 = "SX".($tableNameCnt + 2);
        $table4 = "SX".($tableNameCnt + 3);
        $query  = "    case when ".$table1.".SUBCLASSCD_CNT = '5' THEN cast(cast(".$table1.".SCORE as smallint) as char(3)) ELSE '*' || cast(cast(".$table1.".SCORE as smallint) as char(3)) END as TOTAL".$fieldNameCnt."_3, ";
        $query .= "    case when ".$table1.".SUBCLASSCD_CNT = '5' THEN ".$table1.".SCHOOL_PRECEDENCE ELSE cast(null as int) END as SCHOOL_PRECEDENCE".$fieldNameCnt."_3, ";
        $query .= "    ".$table3.".SCORE as AVG".$fieldNameCnt."_3, ";
        $query .= "    case when ".$table2.".SUBCLASSCD_CNT = '5' THEN cast(cast(".$table2.".SCORE as smallint) as char(3)) ELSE '*' || cast(cast(".$table2.".SCORE as smallint) as char(3)) END as TOTAL".$fieldNameCnt."_5, ";
        $query .= "    case when ".$table2.".SUBCLASSCD_CNT = '5' THEN ".$table2.".SCHOOL_PRECEDENCE ELSE cast(null as int) END as SCHOOL_PRECEDENCE".$fieldNameCnt."_5, ";
        $query .= "    ".$table4.".SCORE as AVG".$fieldNameCnt."_5";
        $query .= $lastSep." ";

        return $query;
    }

    function getShamexamJoinQuery(&$tableNameCnt, $val, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = knjd328Query::getShamexamJoin($tableNameCnt++, $val, "80-J-2-800101", $model);
            $query .= knjd328Query::getShamexamJoin($tableNameCnt++, $val, "80-J-2-800102", $model);
            $query .= knjd328Query::getShamexamJoin($tableNameCnt++, $val, "80-J-2-800201", $model);
            $query .= knjd328Query::getShamexamJoin($tableNameCnt++, $val, "80-J-2-800202", $model);
        } else {
            $query  = knjd328Query::getShamexamJoin($tableNameCnt++, $val, "800101", $model);
            $query .= knjd328Query::getShamexamJoin($tableNameCnt++, $val, "800102", $model);
            $query .= knjd328Query::getShamexamJoin($tableNameCnt++, $val, "800201", $model);
            $query .= knjd328Query::getShamexamJoin($tableNameCnt++, $val, "800202", $model);
        }

        return $query;
    }

    function getShamexamJoin($tableNameCnt, $shamexamCd, $subclassCd, $model) {
        $table = "SX".$tableNameCnt;
        $query  = "    LEFT JOIN SHAMEXAMINATION_DAT ".$table." ON ".$table.".YEAR = t1.YEAR ";
        $query .= "    AND ".$table.".SHAMEXAMCD = '".$shamexamCd."' ";
        $query .= "    AND t1.SCHREGNO = ".$table.".SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND ".$table.".CLASSCD       = '".substr($subclassCd, 0, 2)."' ";
            $query .= "    AND ".$table.".SCHOOL_KIND   = '".substr($subclassCd, 3, 1)."' ";
            $query .= "    AND ".$table.".CURRICULUM_CD = '".substr($subclassCd, 5, 1)."' ";
            $query .= "    AND ".$table.".SUBCLASSCD    = '".substr($subclassCd, 7, 6)."' ";
        } else {
            $query .= "    AND ".$table.".SUBCLASSCD = '".$subclassCd."' ";
        }
        
        return $query;
    }
}
?>
