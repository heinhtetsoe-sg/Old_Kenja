<?php
class knjh830Query extends Query {
    //選択生徒情報取得
    function getStudentName($gakuseki, $year)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t6.GRADE_NAME as GRADE, ";
        $query .= "     t6.HR_CLASS_NAME as HR_CLASS, ";
        $query .= "     t1.ATTENDNO, ";
        $query .= "     t1.SEMESTER, ";
        $query .= "     t2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "     left join SCHREG_REGD_GDAT t3 on t1.YEAR = t3.YEAR AND t1.GRADE = t3.GRADE ";
        $query .= "     left join (SELECT ";
        $query .= "                     YEAR, ";
        $query .= "                     GRADE, ";
        $query .= "                     SEMESTER, ";
        $query .= "                     HR_CLASS, ";
        $query .= "                     to_single_byte(GRADE_NAME) as GRADE_NAME, ";
        $query .= "                     to_single_byte(HR_CLASS_NAME1) as HR_CLASS_NAME ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' ";
        $query .= "                 ) t6 on t1.YEAR = t6.YEAR and t1.GRADE = t6.GRADE and t1.HR_CLASS = t6.HR_CLASS and t1.SEMESTER = t6.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO =  (SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         PV_SCHREG_MST ";
        $query .= "                     WHERE ";
        $query .= "                         KNJID = '".$gakuseki."' ";
        $query .= "                     ) AND ";
        $query .= "     t1.YEAR = '".$year."' AND ";
        $query .= "     t1.SEMESTER = (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREGNO = (SELECT ";
        $query .= "                                         SCHREGNO ";
        $query .= "                                     FROM ";
        $query .= "                                         PV_SCHREG_MST ";
        $query .= "                                     WHERE ";
        $query .= "                                         KNJID = '".$gakuseki."' ";
        $query .= "                                     ) AND ";
        $query .= "                         YEAR = '".$year."' ";
        $query .= "                     )  ";
        
        return $query;
    }
    
    //サイト名取得
    function getSite($mnid = "")
    {
        $query .= " SELECT ";
        if($mnid == ""){
            $query .= "     NAME1, ";
        }
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H800' ";
        if($mnid != ""){
            $query .= " AND  ";
            $query .= "     NAMECD2 = '".$mnid."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }
    
    
    //教科取得
    function getKyouka($year, $site, $kyouka="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLASSCD as VALUE, ";
        $query .= "     CLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        if($kyouka != ""){
            $query .= "     CLASSCD = '".$kyouka."' ";
            $query .= " AND ";
        }
        $query .= "     SITE_ID = '".$site."' ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     CLASSCD ";
        
        return $query;
    }
    //科目取得
    function getKamoku($year, $site, $gakuseki, $kyouka, $kamoku="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SUBCLASSCD as VALUE, ";
        $query .= "     SUBCLASSNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     CLASSCD = '".$kyouka."' ";
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$kamoku."' ";
        }else{
            $query .= " AND ";
            $query .= "     SUBCLASSCD IS NOT NULL ";
        }
        $query .= " AND  ";
        $query .= "     SITE_ID = '".$site."' ";
        $query .= " AND ";
        $query .= "     STUDENT_ID = (SELECT ";
        $query .= "                         LOWER(LOGINID) ";
        $query .= "                   FROM ";
        $query .= "                       PV_SCHREG_MST ";
        $query .= "                   WHERE ";
        $query .= "                       KNJID = '".$gakuseki."' ";
        $query .= "                  ) ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }
    
    //前年度と次年度の学籍データがあるかチェック
    function getBackNextData($knjid, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = (SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     PV_SCHREG_MST ";
        $query .= "                 WHERE ";
        $query .= "                     KNJID = '".$knjid."' ";
        $query .= "                 ) AND ";
        $query .= "     YEAR = '".$year."' ";
        
        return $query;
    }
    
    
    //選択した条件のテストIDを取得
    function getTestId($year, $site, $gakuseki, $kyouka="", $kamoku="", $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            if($mode == "1"){
                $query .= "     COUNT(*) ";
            }else{
                $query .= "     MAX(CNT) ";
            }
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= " SELECT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     STUDENT_ID = (SELECT ";
        $query .= "                         LOWER(LOGINID) ";
        $query .= "                   FROM ";
        $query .= "                       PV_SCHREG_MST ";
        $query .= "                   WHERE ";
        $query .= "                       KNJID = '".$gakuseki."' ";
        $query .= "                  ) ";
        if($kyouka != ""){
            $query .= " AND ";
            $query .= "     CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= " AND ";
            $query .= "     SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= " AND ";
        $query .= "     SITE_ID = '".$site."' ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     TEACHING_CD ";
        if($mode != ""){
            $query .= "     ) ";
        }

        return $query;
    }
    
    //選択した教材情報
    function getTestName($year, $gakuseki, $teachingcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TEACHING_CD, ";
        $query .= "     TEACHING_NAME, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     ALL_CNT ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     STUDENT_ID = (SELECT ";
        $query .= "                         LOWER(LOGINID) ";
        $query .= "                   FROM ";
        $query .= "                       PV_SCHREG_MST ";
        $query .= "                   WHERE ";
        $query .= "                       KNJID = '".$gakuseki."' ";
        $query .= "                  ) AND ";
        $query .= "     TEACHING_CD = '".$teachingcd."' ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     TEACHING_CD ";
        
        return $query;
    }
    //選択したテストの得点取得
    function getTestData($year, $gakuseki, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PV_MBT_SCORE_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     STUDENT_ID = (SELECT ";
        $query .= "                         LOWER(LOGINID) ";
        $query .= "                   FROM ";
        $query .= "                       PV_SCHREG_MST ";
        $query .= "                   WHERE ";
        $query .= "                       KNJID = '".$gakuseki."' ";
        $query .= "                  ) AND ";
        $query .= "     TEACHING_CD = '".$teachingcd."' ";
        $query .= " AND ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     TAKE_CNT ";
        
        return $query;
    }
    
    //選択したテストの大問別得点率取得
    function getPercent($year, $site, $gakuseki, $kyouka="", $kamoku="")
    {
        $query  = " SELECT ";
        $query .= "     a1.*, ";
        $query .= "     a2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t2.PARTCD, ";
        $query .= "         case when t1.POINT IS NOT NULL THEN TO_NUMBER(t1.POINT) / TO_NUMBER(t2.ALLPOINT) * 100 ";
        $query .= "         else 0 end as PERCENT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             PARTCD, ";
        $query .= "             COUNT(*) as ALLPOINT ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 SUBSTR(FIELDNO,1,4) as PARTCD, ";
        $query .= "                 JUDGE ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "             WHERE ";
        $query .= "                 (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             MAX(TAKE_CNT), ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID = (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                           FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                           WHERE ";
        $query .= "                                                                             KNJID = '".$gakuseki."' ";
        $query .= "                                                                           )  ";
        if($kyouka != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$site."' ";
        $query .= "                                                         AND ";
        $query .= "                                                             YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) AND ";
        $query .= "                 YEAR = '".$year."' ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             PARTCD ";
        $query .= "         ) t2   ";
        $query .= "         left join (SELECT ";
        $query .= "                         PARTCD, ";
        $query .= "                         COUNT(*) as POINT ";
        $query .= "                     FROM ";
        $query .= "                         (SELECT ";
        $query .= "                             SUBSTR(FIELDNO,1,4) as PARTCD, ";
        $query .= "                             JUDGE ";
        $query .= "                         FROM ";
        $query .= "                             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         MAX(TAKE_CNT), ";
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     FROM ";
        $query .= "                                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                     WHERE ";
        $query .= "                                                                         STUDENT_ID = (SELECT ";
        $query .= "                                                                                         LOWER(LOGINID) ";
        $query .= "                                                                                     FROM ";
        $query .= "                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         KNJID = '".$gakuseki."' ";
        $query .= "                                                                                     )  ";
        if($kyouka != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                         CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= "                                                                     AND ";
            $query .= "                                                                         SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= "                                                                     AND ";
        $query .= "                                                                         SITE_ID = '".$site."' ";
        $query .= "                                                                     AND ";
        $query .= "                                                                         YEAR = '".$year."' ";
        $query .= "                                                                     GROUP BY ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     ) AND ";
        $query .= "                             YEAR = '".$year."' ";
        $query .= "                         ) ";
        $query .= "                     WHERE ";
        $query .= "                         JUDGE = '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         PARTCD ";
        $query .= "                     ) t1 on t1.PARTCD = t2.PARTCD ";
        $query .= "     ) a1   ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID as PARTCD, ";
        $query .= "                     THIRD_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) a2 on a1.PARTCD = a2.PARTCD ";
        $query .= " ORDER BY ";
        $query .= "     PARTCD ";

        
        return $query;
    }
    //大問ごと成績取得
    function getPartScore($year, $gakuseki, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     t1.PARTCD, ";
        $query .= "     t1.ALLCNT, ";
        $query .= "     case when t2.CNT IS NOT NULL then t2.CNT ";
        $query .= "     else 0 end as CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         PARTCD, ";
        $query .= "         COUNT(*) as ALLCNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             SUBSTR(FIELDNO,1,4) as PARTCD, ";
        $query .= "             JUDGE ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         MAX(TAKE_CNT), ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID = (SELECT ";
        $query .= "                                                                         LOWER(LOGINID) ";
        $query .= "                                                                       FROM ";
        $query .= "                                                                         PV_SCHREG_MST ";
        $query .= "                                                                       WHERE ";
        $query .= "                                                                         KNJID = '".$gakuseki."' ";
        $query .= "                                                                       )  ";
        $query .= "                                                     AND ";
        $query .= "                                                         TEACHING_CD = '".$teachingcd."' ";
        $query .= "                                                     AND ";
        $query .= "                                                         YEAR = '".$year."' ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         AND ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PARTCD ";
        $query .= "     ) t1   ";
        $query .= "     left join (SELECT ";
        $query .= "                     PARTCD, ";
        $query .= "                     COUNT(*) as CNT ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         SUBSTR(FIELDNO,1,4) as PARTCD, ";
        $query .= "                         JUDGE ";
        $query .= "                     FROM ";
        $query .= "                         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                                     STUDENT_ID, ";
        $query .= "                                                                     MAX(TAKE_CNT), ";
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     STUDENT_ID = (SELECT ";
        $query .= "                                                                                     LOWER(LOGINID) ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     PV_SCHREG_MST ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     KNJID = '".$gakuseki."' ";
        $query .= "                                                                                 )  ";
        $query .= "                                                                 AND ";
        $query .= "                                                                     TEACHING_CD = '".$teachingcd."' ";
        $query .= "                                                                 AND ";
        $query .= "                                                                     YEAR = '".$year."' ";
        $query .= "                                                                 GROUP BY ";
        $query .= "                                                                     STUDENT_ID, ";
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 ) AND ";
        $query .= "                         JUDGE = '1' ";
        $query .= "                     AND ";
        $query .= "                         YEAR = '".$year."' ";
        $query .= "                     ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     PARTCD ";
        $query .= "                 ) t2 on t1.PARTCD = t2.PARTCD ";

        return $query;
    }
    
    //小分野ごとの出力用
    //大分野名称とテストに含まれる小分野個数を取得
    function getQuestCnt($year, $site, $gakuseki, $kyouka="", $kamoku="")
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.THIRD_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         PARTCD, ";
        $query .= "         COUNT(QUESTCD) as CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             SUBSTR(FIELDNO, 1, 4) as PARTCD, ";
        $query .= "             SUBSTR(FIELDNO, 1, 6) as QUESTCD ";
        $query .= "         FROM ";
        $query .= "             (SELECT DISTINCT ";
        $query .= "                 FIELDNO ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "             WHERE ";
        $query .= "                 (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             MAX(TAKE_CNT), ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID = (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             KNJID = '".$gakuseki."' ";
        $query .= "                                                                         )  ";
        if($kyouka != ""){
            $query .= "                                                     AND ";
            $query .= "                                                             CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$site."' ";
        $query .= "                                                         AND ";
        $query .= "                                                             YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) AND ";
        $query .= "                 YEAR = '".$year."' ";
        $query .= "             ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PARTCD ";
        $query .= " ) t1 left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID as PARTCD, ";
        $query .= "                     THIRD_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) t2 on t1.PARTCD = t2.PARTCD ";
        $query .= " ORDER BY ";
        $query .= "    t1.PARTCD ";
        
        return $query;
    }
    //小分野名称取得
    function getQuestName($year, $site, $gakuseki, $kyouka="", $kamoku="")
    {
        $query .= " SELECT ";
        $query .= "     a1.*, ";
        $query .= "     a2.FOURTH_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.QUEST_FIELDNO, ";
        $query .= "         case when t2.CNT IS NOT NULL then TO_NUMBER(t2.CNT) / TO_NUMBER(t1.ALLCNT) * 100 else 0 end as PERCENT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             QUEST_FIELDNO, ";
        $query .= "             COUNT(*) as ALLCNT ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 SUBSTR(FIELDNO, 1, 6) as QUEST_FIELDNO, ";
        $query .= "                 JUDGE ";
        $query .= "             FROM ";
        $query .= "                 PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "             WHERE ";
        $query .= "                 (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             MAX(TAKE_CNT), ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         FROM ";
        $query .= "                                                             PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             STUDENT_ID = (SELECT ";
        $query .= "                                                                             LOWER(LOGINID) ";
        $query .= "                                                                         FROM ";
        $query .= "                                                                             PV_SCHREG_MST ";
        $query .= "                                                                         WHERE ";
        $query .= "                                                                             KNJID = '".$gakuseki."' ";
        $query .= "                                                                         )  ";
        if($kyouka != ""){
            $query .= "                                                     AND ";
            $query .= "                                                             CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= "                                                         AND ";
            $query .= "                                                             SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= "                                                         AND ";
        $query .= "                                                             SITE_ID = '".$site."' ";
        $query .= "                                                         AND ";
        $query .= "                                                             YEAR = '".$year."' ";
        $query .= "                                                         GROUP BY ";
        $query .= "                                                             STUDENT_ID, ";
        $query .= "                                                             TEACHING_CD ";
        $query .= "                                                         ) ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             QUEST_FIELDNO ";
        $query .= "         ) t1   ";
        $query .= "         left join  (SELECT ";
        $query .= "                         QUEST_FIELDNO, ";
        $query .= "                         COUNT(*) as CNT ";
        $query .= "                     FROM ";
        $query .= "                         (SELECT ";
        $query .= "                             SUBSTR(FIELDNO, 1, 6) as QUEST_FIELDNO, ";
        $query .= "                             JUDGE ";
        $query .= "                         FROM ";
        $query .= "                             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         MAX(TAKE_CNT), ";
        $query .= "                                                                        TEACHING_CD ";
        $query .= "                                                                    FROM ";
        $query .= "                                                                        PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                    WHERE ";
        $query .= "                                                                        STUDENT_ID = (SELECT ";
        $query .= "                                                                                         LOWER(LOGINID) ";
        $query .= "                                                                                      FROM ";
        $query .= "                                                                                         PV_SCHREG_MST ";
        $query .= "                                                                                     WHERE ";
        $query .= "                                                                                         KNJID = '".$gakuseki."' ";
        $query .= "                                                                                     )  ";
        if($kyouka != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     CLASSCD = '".$kyouka."' ";
        }
        if($kamoku != ""){
            $query .= "                                                                 AND ";
            $query .= "                                                                     SUBCLASSCD = '".$kamoku."' ";
        }
        $query .= "                                                                     AND ";
        $query .= "                                                                         SITE_ID = '".$site."' ";
        $query .= "                                                                     AND ";
        $query .= "                                                                         YEAR = '".$year."' ";
        $query .= "                                                                     GROUP BY ";
        $query .= "                                                                         STUDENT_ID, ";
        $query .= "                                                                         TEACHING_CD ";
        $query .= "                                                                     ) ";
        $query .= "                         AND ";
        $query .= "                             YEAR = '".$year."' ";
        $query .= "                         ) ";
        $query .= "                     WHERE ";
        $query .= "                         JUDGE = '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         QUEST_FIELDNO ";
        $query .= "                     ) t2 on t1.QUEST_FIELDNO = t2.QUEST_FIELDNO ";
        $query .= "     ) a1  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     FIRST_ID || SECOND_ID || THIRD_ID || FOURTH_ID as QUESTNO, ";
        $query .= "                     FOURTH_NAME ";
        $query .= "                 FROM ";
        $query .= "                     PV_CBT_FIELD_MST ";
        $query .= "                 ) a2 on a1.QUEST_FIELDNO = a2.QUESTNO ";
        $query .= " ORDER BY ";
        $query .= "     a1.QUEST_FIELDNO ";
        
        return $query;
    }
    //小分野得点取得
    function getQuestPoint($year, $gakuseki, $teachingcd)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     case when t2.CNT IS NOT NULL then t2.CNT else 0 end as CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         PARTCD, ";
        $query .= "         QUESTCD, ";
        $query .= "         COUNT(*) as ALLCNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             SUBSTR(FIELDNO, 1, 4) as PARTCD, ";
        $query .= "             SUBSTR(FIELDNO, 1, 6) as QUESTCD, ";
        $query .= "             JUDGE ";
        $query .= "         FROM ";
        $query .= "             PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         MAX(TAKE_CNT), ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     FROM ";
        $query .= "                                                         PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         STUDENT_ID = (SELECT ";
        $query .= "                                                                         LOWER(LOGINID) ";
        $query .= "                                                                       FROM ";
        $query .= "                                                                         PV_SCHREG_MST ";
        $query .= "                                                                       WHERE ";
        $query .= "                                                                         KNJID = '".$gakuseki."' ";
        $query .= "                                                                       )  ";
        $query .= "                                                     AND ";
        $query .= "                                                         TEACHING_CD = '".$teachingcd."' ";
        $query .= "                                                     AND ";
        $query .= "                                                         YEAR = '".$year."' ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         STUDENT_ID, ";
        $query .= "                                                         TEACHING_CD ";
        $query .= "                                                     ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         PARTCD, ";
        $query .= "         QUESTCD ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     PARTCD, ";
        $query .= "                     QUESTCD, ";
        $query .= "                     COUNT(*) as CNT ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         SUBSTR(FIELDNO, 1, 4) as PARTCD, ";
        $query .= "                         SUBSTR(FIELDNO, 1, 6) as QUESTCD, ";
        $query .= "                         JUDGE ";
        $query .= "                     FROM ";
        $query .= "                         PV_MBT_SCORE_DETAIL_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         (STUDENT_ID, TAKE_CNT, TEACHING_CD) IN (SELECT ";
        $query .= "                                                                     STUDENT_ID, ";
        $query .= "                                                                     MAX(TAKE_CNT), ";
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 FROM ";
        $query .= "                                                                     PV_MBT_SCORE_COUNT_DAT ";
        $query .= "                                                                 WHERE ";
        $query .= "                                                                     STUDENT_ID = (SELECT ";
        $query .= "                                                                                     LOWER(LOGINID) ";
        $query .= "                                                                                 FROM ";
        $query .= "                                                                                     PV_SCHREG_MST ";
        $query .= "                                                                                 WHERE ";
        $query .= "                                                                                     KNJID = '".$gakuseki."' ";
        $query .= "                                                                                 )  ";
        $query .= "                                                                 AND ";
        $query .= "                                                                     TEACHING_CD = '".$teachingcd."' ";
        $query .= "                                                                 AND ";
        $query .= "                                                                     YEAR = '".$year."' ";
        $query .= "                                                                 GROUP BY ";
        $query .= "                                                                     STUDENT_ID, ";
        $query .= "                                                                     TEACHING_CD ";
        $query .= "                                                                 ) ";
        $query .= "                     AND ";
        $query .= "                         YEAR = '".$year."' ";
        $query .= "                     ) ";
        $query .= "                 WHERE ";
        $query .= "                     JUDGE = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     PARTCD, ";
        $query .= "                     QUESTCD ";
        $query .= "                 ) t2 on t1.QUESTCD = t2.QUESTCD ";
        $query .= " ORDER BY ";
        $query .= "     t1.QUESTCD ";
        
        return $query;
    }
}
?>
