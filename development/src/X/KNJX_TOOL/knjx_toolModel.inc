<?php

require_once('for_php7.php');

require_once('dBug.php');

class knjx_toolModel extends Model {



    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjx_toolModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->valErrMess = array();
        $this->valErrFlg = "";
        $this->new = array();

        $this->cmd   = VARS::request("cmd");
    }


    //学籍
    function REGDupdateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //TOOL_SCHREGD_DATクリア
        switch(SCHOOLKIND){
            case "P":
                $where1 = " WHERE BUMONCD = '1' ";
                $where2 = " WHERE t1.GRADE BETWEEN  '01' AND '06' ";
                break 1;
            case "J":
                $where1 = " WHERE BUMONCD = '2' ";
                $where2 = " WHERE t1.GRADE BETWEEN  '07' AND '09' ";
                break 1;
            case "H":
                $where1 = " WHERE BUMONCD = '3' OR BUMONCD = '4' ";
                $where2 = " WHERE t1.GRADE BETWEEN  '10' AND '12' AND t1.year > 2003";
                break 1;
            default :
                $where1 = "";
                $where2 = "";
                break 1;
        }
        
        $query = "DELETE FROM TOOL_SCHREG_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_SCHREG_DAT  ";
        $query .= "  ";
        $query .= " SELECT  ";
        $query .= "     t1.YEAR, ";
        $query .= "     t2.SCHREGNO, ";
        $query .= "     CASE ";
        $query .= "     WHEN t1.HR_CLASS = '011' ";
        $query .= "     THEN '4' ";
        $query .= "     WHEN t1.GRADE BETWEEN  '01' AND '06' ";
        $query .= "     THEN '1' ";
        $query .= "     WHEN t1.GRADE BETWEEN  '07' AND '09' ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN t1.GRADE BETWEEN  '10' AND '12' ";
        $query .= "     THEN '3' ";
        $query .= "     END as BUMONCD, ";
        $query .= "     CASE ";
        $query .= "     WHEN t1.HR_CLASS = '011' ";
        $query .= "     THEN '美デ' ";
        $query .= "     WHEN t1.GRADE BETWEEN  '01' AND '06' ";
        $query .= "     THEN '駿小' ";
        $query .= "     WHEN t1.GRADE BETWEEN  '07' AND '09' ";
        $query .= "     THEN '駿中' ";
        $query .= "     WHEN t1.GRADE BETWEEN  '10' AND '12' ";
        $query .= "     THEN '駿高' ";
        $query .= "     END as BUMONNAME, ";
        $query .= "     t3.PRESENT_EST, ";
        $query .= "     ltrim(t1.GRADE,'0') AS GRADE, ";
        $query .= "     CASE ";
        $query .= "     WHEN t9.HR_CLASS_NAME1 IS NOT NULL ";
        $query .= "     THEN t9.HR_CLASS_NAME1 ";
        $query .= "     ELSE ";
        $query .= "     t1.HR_CLASS ";
        $query .= "     END AS HR_CLASS, ";
        $query .= "     ltrim(t1.ATTENDNO,'0') AS ATTENDNO, ";
        $query .= "     t2.NAME, ";
        $query .= "     t2.NAME_KANA, ";
        $query .= "     t2.SEX, ";
        $query .= "     SUBSTR(T1.SCHREGNO,5,1) as ENT_BUMONCD, ";
        $query .= "     CASE ";
        $query .= "     WHEN SUBSTR(T1.SCHREGNO,5,1) = '1'  ";
        $query .= "     THEN '駿小' ";
        $query .= "     WHEN SUBSTR(T1.SCHREGNO,5,1) = '2'  ";
        $query .= "     THEN '駿中' ";
        $query .= "     WHEN SUBSTR(T1.SCHREGNO,5,1) = '3'  ";
        $query .= "     THEN '駿高' ";
        $query .= "     WHEN SUBSTR(T1.SCHREGNO,5,1) = '4'  ";
        $query .= "     THEN '美デ' ";
        $query .= "     END as ENT_BUMONNAME, ";
        $query .= "     t2.ENT_DATE, ";
        $query .= "     CASE WHEN ENT_DIV IS NULL ";
        $query .= "     THEN '0' ";
        $query .= "     ELSE ";
        $query .= "     t2.ENT_DIV ";
        $query .= "     END AS ENT_DIV, ";
        $query .= "     t4.NAME1 as ENT_NAME, ";
        $query .= "     CASE ";
        $query .= "     WHEN GRD_DATE between w1.SDATE and w1.EDATE or GRD_DATE < w1.SDATE ";
        $query .= "     THEN GRD_DATE ";
        $query .= "     ELSE '9999-01-01' ";
        $query .= "     END AS GRD_DATE, ";
        $query .= "     CASE ";
        $query .= "     WHEN GRD_DATE between w1.SDATE and w1.EDATE or GRD_DATE < w1.SDATE ";
        $query .= "     THEN t2.GRD_DIV ";
        $query .= "     ELSE '0' ";
        $query .= "     END AS GRD_DIV, ";
        $query .= "     CASE ";
        $query .= "     WHEN GRD_DATE between w1.SDATE and w1.EDATE or GRD_DATE < w1.SDATE ";
        $query .= "     THEN t7.NAME1 ";
        $query .= "     ELSE NULL ";
        $query .= "     END as GRD_NAME, ";
        $query .= "     CASE T1.COURSECODE ";
        $query .= "     WHEN '0000' ";
        $query .= "     THEN '000' ";
        $query .= "     WHEN '1000' ";
        $query .= "     THEN '201' ";
        $query .= "     WHEN '2000' ";
        $query .= "     THEN '202' ";
        $query .= "     WHEN '3000' ";
        $query .= "     THEN '100' ";
        $query .= "     WHEN '4000' ";
        $query .= "     THEN '100' ";
        $query .= "     WHEN '4100' ";
        $query .= "     THEN '201' ";
        $query .= "     WHEN '4300' ";
        $query .= "     THEN '201' ";
        $query .= "     WHEN '4200' ";
        $query .= "     THEN '202' ";
        $query .= "     WHEN '4400' ";
        $query .= "     THEN '202' ";
        $query .= "     WHEN '5000' ";
        $query .= "     THEN '000' ";
        $query .= "     WHEN '6000' ";
        $query .= "     THEN '000' ";
        $query .= "     WHEN '7000' ";
        $query .= "     THEN '000' ";
        $query .= "     WHEN '8000' ";
        $query .= "     THEN '000' ";
        $query .= "     WHEN '9000' ";
        $query .= "     THEN '000' ";
        $query .= "     ELSE NULL ";
        $query .= "     END AS COURSECODE, ";
        $query .= "     t5.COURSECODENAME, ";
        $query .= "     t2.FINSCHOOLCD, ";
        $query .= "     t6.FINSCHOOL_NAME, ";
        $query .= "     CASE ";
        $query .= "     WHEN t6.FINSCHOOL_DIV IS NULL ";
        $query .= "     THEN '9' ";
        $query .= "     ELSE t6.FINSCHOOL_DIV ";
        $query .= "     END AS FINSCHOOL_DIV, ";
        $query .= "     t6.DISTRICTCD, ";
        $query .= "     CASE ";
        $query .= "     WHEN t6.DISTRICTCD IS NULL ";
        $query .= "     THEN '県外' ";
        $query .= "     ELSE t8.NAME1 ";
        $query .= "     END as DISTRICTNAME, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T2.UPDATED ";
        $query .= "     THEN T1.UPDATED ";
        $query .= "     ELSE T2.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     t2.REGISTERCD, ";
        $query .= "     sysdate() as COMPDATETIME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST t2 ";
        $query .= "     left join (select a2.* from ";
        $query .= "                                 ( ";
        $query .= "                                 SELECT ";
        $query .= "                                     year, ";
        $query .= "                                     schregno, ";
        $query .= "                                     max(semester) as semester ";
        $query .= "                                 FROM ";
        $query .= "                                     schreg_regd_dat ";
        $query .= "                                 GROUP BY ";
        $query .= "                                     year, ";
        $query .= "                                     schregno ";
        $query .= "                                 ) as a1 ";
        $query .= "                                 left join schreg_regd_dat a2 on a1.year = a2.year and a1.schregno = a2.schregno and a1.semester = a2.semester  ";
        $query .= "                 ) t1 on t2.SCHREGNO = t1.SCHREGNO ";
        $query .= "     left join SCHREG_REGD_GDAT t10 on t1.YEAR = t10.YEAR and t1.GRADE = t10.GRADE ";
        $query .= "     left join SCHOOL_MST t3 on int(t1.YEAR) - int(t10.grade_cd) + 1 = t3.YEAR and t3.SCHOOL_KIND = t10.SCHOOL_KIND ";
        $query .= "     left join NAME_MST t4 on t2.ENT_DIV = t4.NAMECD2 and t4.NAMECD1 = 'A002' ";
        $query .= "     left join COURSECODE_MST t5 on t1.COURSECODE = t5.COURSECODE ";
        $query .= "     left join FINSCHOOL_MST t6 on t2.FINSCHOOLCD = t6.FINSCHOOLCD ";
        $query .= "     left join NAME_MST t7 on t2.GRD_DIV = t7.NAMECD2 and t7.NAMECD1 = 'A003' ";
        $query .= "     left join NAME_MST t8 on t6.DISTRICTCD = t8.NAMECD2 and t8.NAMECD1 = 'Z003' ";
        $query .= "     left join SCHREG_REGD_HDAT t9 on t1.YEAR = t9.YEAR and t1.SEMESTER = t9.SEMESTER and t1.GRADE = t9.GRADE and t1.HR_CLASS = t9.HR_CLASS ";
        $query .= "     left join (SELECT YEAR,MIN(SDATE) AS SDATE, MAX(EDATE) AS EDATE FROM SEMESTER_MST GROUP BY YEAR) w1 on t1.year = w1.year ";
        $query .= $where2;
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO ";
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //試験（定期考査）
    function SCORE1updateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //TOOL_SCORE_DATクリア
        switch(SCHOOLKIND){
            case "P":
                $where1 = " WHERE BUMONCD = '1' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '1'";
                $where2 = " AND T2.GRADE BETWEEN  '01' AND '06' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "J":
                $where1 = " WHERE BUMONCD = '2' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '1'";
                $where2 = " AND T2.GRADE BETWEEN  '07' AND '09' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "H":
                $where1 = " WHERE (BUMONCD = '3' OR BUMONCD = '4') AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '1'";
                $where2 = " AND T2.GRADE BETWEEN  '10' AND '12' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            default :
                $where1 = "";
                $where2 = "";
                break 1;
        }
        
        $query = "DELETE FROM TOOL_SCORE_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_SCORE_DAT  ";
        $query .= "  ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '4' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '1' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '3' ";
        $query .= "     END AS BUMONCD, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '美デ' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '駿小' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '駿中' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '駿高' ";
        $query .= "     END AS BUMONNAME, ";
        $query .= "     W3.PRESENT_EST, ";
        $query .= "     ltrim(T2.GRADE,'0') AS GRADE, ";
        $query .= "     '1' as TEST_CATEGORYCD, ";
        $query .= "     '定期考査' as TESTCATEGORYNAME, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.YEAR || T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV as TESTID, ";
        $query .= "     T3.TESTITEMNAME, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T4.CLASSNAME, ";
        $query .= "     T4.SHOWORDER, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T5.SUBCLASSNAME, ";
        $query .= "     T5.SHOWORDER, ";
        $query .= "     T72.CHAIR_GROUP_CD, ";
        $query .= "     T72.CHAIR_GROUP_NAME, ";
        $query .= "     T7.CHAIRCD, ";
        $query .= "     T7.CHAIRNAME, ";
        $query .= "     W1.LESSON AS SCH_CHR_COUNT, ";
        $query .= "     CASE ";
        $query .= "     WHEN T8.PASS_SCORE IS NULL ";
        $query .= "     THEN 30 ";
        $query .= "     ELSE T8.PASS_SCORE ";
        $query .= "     END AS PASS_SCORE, ";
        $query .= "     T9.AVG AS GROUP_AVG, ";
        $query .= "     T9.STDDEV AS GROUP_STDDEV, ";
        $query .= "     T11.COURSE_RANK AS GROUP_RANK, ";
        $query .= "     T10.AVG AS AVG, ";
        $query .= "     T10.STDDEV AS STDDEV, ";
        $query .= "     T11.CLASS_RANK AS RANK, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T1.VALUE_DI IS NOT NULL ";
        $query .= "     THEN '0' ";
        $query .= "     ELSE '1' ";
        $query .= "     END AS ATTEND, ";
        $query .= "     CASE  ";
        $query .= "     WHEN W2.ABSENCE IS NULL ";
        $query .= "     THEN 0 ";
        $query .= "     ELSE W2.ABSENCE ";
        $query .= "     END AS ABSENCE, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.SCORE IS NULL ";
        $query .= "     THEN 0 ";
        $query .= "     ELSE T1.SCORE ";
        $query .= "     END AS SCORE, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T12.SCORE IS NOT NULL ";
        $query .= "     THEN T12.SCORE ";
        $query .= "     ELSE 0 ";
        $query .= "     END AS TOTAL_SCORE, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T12.AVG IS NOT NULL ";
        $query .= "     THEN T12.AVG ";
        $query .= "     ELSE 0 ";
        $query .= "     END AS TOTAL_AVG, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T9.UPDATED AND T1.UPDATED > T10.UPDATED AND T1.UPDATED > T11.UPDATED ";
        $query .= "     THEN T1.UPDATED ";
        $query .= "     WHEN T9.UPDATED > T1.UPDATED AND T9.UPDATED > T10.UPDATED AND T9.UPDATED > T11.UPDATED ";
        $query .= "     THEN T9.UPDATED ";
        $query .= "     WHEN T10.UPDATED > T1.UPDATED AND T10.UPDATED > T9.UPDATED AND T10.UPDATED > T11.UPDATED ";
        $query .= "     THEN T10.UPDATED ";
        $query .= "     WHEN T11.UPDATED > T1.UPDATED AND T11.UPDATED > T9.UPDATED AND T11.UPDATED > T10.UPDATED ";
        $query .= "     THEN T11.UPDATED ";
        $query .= "     ELSE T1.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T9.UPDATED AND T1.UPDATED > T10.UPDATED AND T1.UPDATED > T11.UPDATED ";
        $query .= "     THEN T1.REGISTERCD ";
        $query .= "     WHEN T9.UPDATED > T1.UPDATED AND T9.UPDATED > T10.UPDATED AND T9.UPDATED > T11.UPDATED ";
        $query .= "     THEN T9.REGISTERCD ";
        $query .= "     WHEN T10.UPDATED > T1.UPDATED AND T10.UPDATED > T9.UPDATED AND T10.UPDATED > T11.UPDATED ";
        $query .= "     THEN T10.REGISTERCD ";
        $query .= "     WHEN T11.UPDATED > T1.UPDATED AND T11.UPDATED > T9.UPDATED AND T11.UPDATED > T10.UPDATED ";
        $query .= "     THEN T11.REGISTERCD ";
        $query .= "     ELSE T1.REGISTERCD ";
        $query .= "     END AS REGISTERCD, ";
        $query .= "     SYSDATE() AS COMPDATETIME ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T3 on T1.YEAR = T3.YEAR and T1.SEMESTER = T3.SEMESTER and T1.TESTKINDCD = T3.TESTKINDCD and T1.TESTITEMCD = T3.TESTITEMCD and T1.SCORE_DIV = T3.SCORE_DIV and T1.SCHOOL_KIND = T3.SCHOOL_KIND and T2.MAJORCD = T3.MAJORCD ";
        $query .= "     LEFT JOIN CLASS_MST T4 on T1.CLASSCD = T4.CLASSCD and T1.SCHOOL_KIND = T4.SCHOOL_KIND ";
        $query .= "     LEFT JOIN SUBCLASS_MST T5 on T1.SCHOOL_KIND = T5.SCHOOL_KIND and T1.SUBCLASSCD = T5.SUBCLASSCD and T1.CURRICULUM_CD = T5.CURRICULUM_CD ";
        $query .= "     LEFT JOIN CHAIR_STD_DAT T6 on T1.YEAR = T6.YEAR and T1.SEMESTER = T6.SEMESTER and T1.SCHREGNO = T6.SCHREGNO ";
        //$query .= "     LEFT JOIN CHAIR_DAT T7 on T1.YEAR = T7.YEAR and T1.SEMESTER = T7.SEMESTER and T1.SCHOOL_KIND = T7.SCHOOL_KIND and T1.CURRICULUM_CD = T7.CURRICULUM_CD and T1.SUBCLASSCD = T7.SUBCLASSCD ";
        $query .= "     LEFT JOIN (SELECT T1.*,T2.FACCD FROM CHAIR_DAT T1 LEFT JOIN CHAIR_FAC_DAT T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.CHAIRCD = T2.CHAIRCD and T2.FACCD = '900') T7 on T1.YEAR = T7.YEAR and T1.SEMESTER = T7.SEMESTER and T1.SCHOOL_KIND = T7.SCHOOL_KIND and T1.CURRICULUM_CD = T7.CURRICULUM_CD and T1.SUBCLASSCD = T7.SUBCLASSCD and (T7.FACCD IS NULL or T7.FACCD != '900') ";
        $query .= "     LEFT JOIN CHAIR_GROUP_DAT T71 on T7.YEAR = T71.YEAR and T7.SEMESTER = T71.SEMESTER and T7.CHAIRCD = T71.CHAIRCD ";
        $query .= "     LEFT JOIN CHAIR_GROUP_MST T72 on T71.YEAR = T72.YEAR and T71.SEMESTER = T72.SEMESTER and T71.CHAIR_GROUP_CD = T72.CHAIR_GROUP_CD and T1.SCHOOL_KIND = T72.SCHOOL_KIND and T1.CURRICULUM_CD = T72.CURRICULUM_CD ";
        $query .= "     LEFT JOIN ";
        $query .= "     (SELECT YEAR,SEMESTER,'02' as TESTKINDCD,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,sum(LESSON) as LESSON FROM ( ";
        $query .= "     SELECT YEAR,SEMESTER,CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,case LESSON when null then 0 else LESSON end as LESSON FROM ATTEND_SUBCLASS_DAT) group by year,semester,schregno,classcd,school_kind,curriculum_cd,subclasscd ";
        $query .= "     union ";
        $query .= "     SELECT YEAR,SEMESTER,CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,case LESSON when null then 0 else LESSON end as LESSON FROM ATTEND_SUBCLASS_DAT where month in('05','10') ";
        $query .= "     ) W1 ON T1.YEAR = W1.YEAR and T1.SEMESTER = W1.SEMESTER and T1.TESTKINDCD = W1.TESTKINDCD and T1.SCHREGNO = W1.SCHREGNO and T1.SCHOOL_KIND = W1.SCHOOL_KIND and T1.CURRICULUM_CD = W1.CURRICULUM_CD and T1.SUBCLASSCD = W1.SUBCLASSCD ";
        $query .= "     LEFT JOIN PERFECT_RECORD_DAT T8 on T1.YEAR = T8.YEAR and T1.SEMESTER = T8.SEMESTER and T1.TESTKINDCD = T8.TESTKINDCD and T1.TESTITEMCD = T8.TESTITEMCD and T1.SCHOOL_KIND = T8.SCHOOL_KIND and T1.CURRICULUM_CD = T8.CURRICULUM_CD and T1.SUBCLASSCD = T8.SUBCLASSCD ";
        $query .= "     LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT T9 on T1.YEAR = T9.YEAR and T1.SEMESTER = T9.SEMESTER and T1.TESTKINDCD = T9.TESTKINDCD and T1.TESTITEMCD = T9.TESTITEMCD and T1.SCHOOL_KIND = T9.SCHOOL_KIND and T1.CURRICULUM_CD = T9.CURRICULUM_CD and T1.SUBCLASSCD = T9.SUBCLASSCD and T2.COURSECODE = T9.COURSECODE and T9.AVG_DIV = '3' ";
        $query .= "     LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT T10 on T1.YEAR = T10.YEAR and T1.SEMESTER = T10.SEMESTER and T1.TESTKINDCD = T10.TESTKINDCD and T1.TESTITEMCD = T10.TESTITEMCD and T1.SCHOOL_KIND = T10.SCHOOL_KIND and T1.CURRICULUM_CD = T10.CURRICULUM_CD and T1.SUBCLASSCD = T10.SUBCLASSCD and T2.GRADE = T10.GRADE and T2.HR_CLASS = T10.HR_CLASS and T10.AVG_DIV = '2' ";
        
        $query .= "     LEFT JOIN  ";
        $query .= "     (SELECT YEAR,SEMESTER,TESTKINDCD,SCHREGNO,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CASE WHEN SEMESTER = '3' THEN ABSENCE WHEN TESTKINDCD = '02' THEN ABSENCE + ABSENCE2 ELSE ABSENCE END AS ABSENCE FROM  ";
        $query .= "     ( ";
        $query .= "     SELECT T1.YEAR,T1.SEMESTER,T1.TESTKINDCD,T1.SCHREGNO,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD,CASE WHEN T1.ABSENCE IS NULL THEN 0 ELSE T1.ABSENCE END as ABSENCE,CASE WHEN T2.ABSENCE IS NULL THEN 0 ELSE T2.ABSENCE END as ABSENCE2 FROM  ";
        $query .= "     ( ";
        $query .= "     SELECT YEAR,SEMESTER,CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SICK AS ABSENCE FROM ATTEND_SUBCLASS_DAT) as T1 ";
        $query .= "     LEFT JOIN  ";
        $query .= "     ( ";
        //$query .= "     SELECT YEAR,SEMESTER,    CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SICK AS ABSENCE FROM ATTEND_SUBCLASS_DAT) as T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO and T1.SCHOOL_KIND = T2.SCHOOL_KIND and T1.CURRICULUM_CD = T2.CURRICULUM_CD and T1.SUBCLASSCD = T2.SUBCLASSCD and T2.TESTKINDCD = '01' ";
        //中間試験がないデータだけは欠時数に中間分のデータを足すように変更（2017/10/17）
        $query .= "     SELECT T1.YEAR,T1.SEMESTER,CASE T1.MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,T1.SCHREGNO,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD,T1.SICK AS ABSENCE,T2.SCHREGNO as CHK FROM ATTEND_SUBCLASS_DAT T1 left join RECORD_SCORE_DAT T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO and T1.SCHOOL_KIND = T2.SCHOOL_KIND and T1.CURRICULUM_CD = T2.CURRICULUM_CD and T1.SUBCLASSCD = T2.SUBCLASSCD and T2.TESTKINDCD = '01') as T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO and T1.SCHOOL_KIND = T2.SCHOOL_KIND and T1.CURRICULUM_CD = T2.CURRICULUM_CD and T1.SUBCLASSCD = T2.SUBCLASSCD and T2.TESTKINDCD = '01' and T2.CHK IS NULL ";
        $query .= "     ) ";
        $query .= "     ) W2 ";
        $query .= "     on T1.YEAR = W2.YEAR and T1.SEMESTER = W2.SEMESTER and T1.TESTKINDCD = W2.TESTKINDCD and T1.SCHREGNO = W2.SCHREGNO and T1.SCHOOL_KIND = W2.SCHOOL_KIND and T1.CURRICULUM_CD = W2.CURRICULUM_CD and T1.SUBCLASSCD = W2.SUBCLASSCD ";
        
        //$query .= "     LEFT JOIN (SELECT YEAR,SEMESTER, CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SICK AS ABSENCE FROM ATTEND_SUBCLASS_DAT) W2 on T1.YEAR = W2.YEAR and T1.SEMESTER = W2.SEMESTER and T1.TESTKINDCD = W2.TESTKINDCD and T1.SCHREGNO = W2.SCHREGNO and T1.SCHOOL_KIND = W2.SCHOOL_KIND and T1.CURRICULUM_CD = W2.CURRICULUM_CD and T1.SUBCLASSCD = W2.SUBCLASSCD ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT T11 on T1.YEAR = T11.YEAR and T1.SEMESTER = T11.SEMESTER and T1.TESTKINDCD = T11.TESTKINDCD and T1.TESTITEMCD = T11.TESTITEMCD and T1.SCORE_DIV = T11.SCORE_DIV and T1.SCHOOL_KIND = T11.SCHOOL_KIND and T1.CURRICULUM_CD = T11.CURRICULUM_CD and T1.SUBCLASSCD = T11.SUBCLASSCD and T1.SCHREGNO = T11.SCHREGNO  ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT T12 on T1.YEAR = T12.YEAR and T1.SEMESTER = T12.SEMESTER and T1.TESTKINDCD = T12.TESTKINDCD and T1.TESTITEMCD = T12.TESTITEMCD and T1.SCORE_DIV = T12.SCORE_DIV and T1.SCHOOL_KIND = T12.SCHOOL_KIND and T12.CURRICULUM_CD = '99' and T1.SCHREGNO = T12.SCHREGNO and T12.SUBCLASSCD = '999999' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT W4 on T2.YEAR = W4.YEAR and T2.GRADE = W4.GRADE ";
        $query .= "     LEFT JOIN SCHOOL_MST W3 on int(T1.YEAR) - int(W4.grade_cd) + 1 = int(W3.YEAR) and T1.SCHOOL_KIND = W3.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.SEMESTER != '9' AND ";
        $query .= "     T6.CHAIRCD = T7.CHAIRCD AND ";
        $query .= "     T6.CHAIRCD = T9.CHAIRCD AND ";
        $query .= "     T6.CHAIRCD = T10.CHAIRCD ";
        $query .= $where2;
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //試験（実力試験）
    function SCORE2updateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //TOOL_SCORE_DATクリア
        switch(SCHOOLKIND){
            case "P":
                $where1 = " WHERE BUMONCD = '1' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '2'";
                $where2 = " AND T2.GRADE BETWEEN  '01' AND '06' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "J":
                $where1 = " WHERE BUMONCD = '2' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '2'";
                $where2 = " AND T2.GRADE BETWEEN  '07' AND '09' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "H":
                $where1 = " WHERE (BUMONCD = '3' OR BUMONCD = '4') AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '2'";
                $where2 = " AND T2.GRADE BETWEEN  '10' AND '12' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            default :
                $where1 = "";
                $where2 = "";
                break 1;
        }
        
        $query = "DELETE FROM TOOL_SCORE_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_SCORE_DAT  ";
        $query .= "  ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR as NENDO, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '4' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '1' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '3' ";
        $query .= "     END AS BUMONCD, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '美デ' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '駿小' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '駿中' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '駿高' ";
        $query .= "     END AS BUMONNAME, ";
        $query .= "     W3.PRESENT_EST, ";
        $query .= "     ltrim(T2.GRADE,'0') AS GRADE, ";
        $query .= "     '2' as TEST_CATEGORYCD, ";
        $query .= "     '実力テスト' as TESTCATEGORYNAME, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.YEAR || T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD as TESTID, ";
        $query .= "     T3.PROFICIENCYNAME1, ";
        $query .= "     T4.CLASSCD, ";
        $query .= "     T5.CLASSNAME, ";
        $query .= "     NULL as CLASSSORT, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T4.SUBCLASS_NAME, ";
        $query .= "     NULL as SUBCLASSSORT, ";
        $query .= "     NULL as GROUP_CHAIRCD, ";
        $query .= "     NULL as GROUP_CHAIRNAME, ";
        $query .= "     NULL as CHAIRCD, ";
        $query .= "     NULL as CHAIRNAME, ";
        $query .= "     0 as SCH_CHR_COUNT, ";
        $query .= "     30 as PASS_SCORE, ";
        $query .= "     T6.AVG as GROUP_AVG, ";
        $query .= "     T6.STDDEV as GROUP_STDDEV, ";
        $query .= "     T7.RANK as GROUP_RANK, ";
        $query .= "     T8.AVG, ";
        $query .= "     T8.STDDEV, ";
        $query .= "     T9.RANK, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '1' as ATTEND, ";
        $query .= "     0 as ABSENCE, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T10.SCORE as TOTAL_SCORE, ";
        $query .= "     T10.AVG as TOTAL_AVG, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T6.UPDATED AND T1.UPDATED > T7.UPDATED ";
        $query .= "     THEN T1.UPDATED ";
        $query .= "     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T6.UPDATED AND T3.UPDATED > T7.UPDATED ";
        $query .= "     THEN T3.UPDATED ";
        $query .= "     WHEN T6.UPDATED > T1.UPDATED AND T6.UPDATED > T3.UPDATED AND T6.UPDATED > T7.UPDATED ";
        $query .= "     THEN T6.UPDATED ";
        $query .= "     WHEN T7.UPDATED > T1.UPDATED AND T7.UPDATED > T3.UPDATED AND T7.UPDATED > T6.UPDATED ";
        $query .= "     THEN T7.UPDATED ";
        $query .= "     ELSE T1.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T6.UPDATED AND T1.UPDATED > T7.UPDATED ";
        $query .= "     THEN T1.REGISTERCD ";
        $query .= "     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T6.UPDATED AND T3.UPDATED > T7.UPDATED ";
        $query .= "     THEN T3.REGISTERCD ";
        $query .= "     WHEN T6.UPDATED > T1.UPDATED AND T6.UPDATED > T3.UPDATED AND T6.UPDATED > T7.UPDATED ";
        $query .= "     THEN T6.REGISTERCD ";
        $query .= "     WHEN T7.UPDATED > T1.UPDATED AND T7.UPDATED > T3.UPDATED AND T7.UPDATED > T6.UPDATED ";
        $query .= "     THEN T7.REGISTERCD ";
        $query .= "     ELSE T1.REGISTERCD ";
        $query .= "     END AS REGISTERCD, ";
        $query .= "     SYSDATE() AS COMPDATETIME ";
        $query .= "      ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT T1  ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T3 on T1.PROFICIENCYDIV = T3.PROFICIENCYDIV and T1.PROFICIENCYCD = T3.PROFICIENCYCD ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST T4 on T1.PROFICIENCY_SUBCLASS_CD = T4.PROFICIENCY_SUBCLASS_CD ";
        $query .= "     LEFT JOIN CLASS_MST T5 on T4.SCHOOL_KIND = T5.SCHOOL_KIND and T4.CLASSCD = T5.CLASSCD ";
        $query .= "     LEFT JOIN PROFICIENCY_AVERAGE_DAT T6 on T1.YEAR = T6.YEAR and T1.SEMESTER = T6.SEMESTER and T1.PROFICIENCYDIV = T6.PROFICIENCYDIV and T1.PROFICIENCYCD = T6.PROFICIENCYCD and T1.PROFICIENCY_SUBCLASS_CD = T6.PROFICIENCY_SUBCLASS_CD and T6.DATA_DIV = '1' and T6.AVG_DIV = '01' and T2.GRADE = T6.GRADE ";
        $query .= "     LEFT JOIN PROFICIENCY_RANK_DAT T7 on T1.YEAR = T7.YEAR and T1.SEMESTER = T7.SEMESTER and T1.PROFICIENCYDIV = T7.PROFICIENCYDIV and T1.PROFICIENCYCD = T7.PROFICIENCYCD and T1.SCHREGNO = T7.SCHREGNO and T1.PROFICIENCY_SUBCLASS_CD = T7.PROFICIENCY_SUBCLASS_CD and T7.RANK_DATA_DIV = '01' and T7.RANK_DIV = '01' ";
        $query .= "     LEFT JOIN PROFICIENCY_AVERAGE_DAT T8 on T1.YEAR = T8.YEAR and T1.SEMESTER = T8.SEMESTER and T1.PROFICIENCYDIV = T8.PROFICIENCYDIV and T1.PROFICIENCYCD = T8.PROFICIENCYCD and T1.PROFICIENCY_SUBCLASS_CD = T8.PROFICIENCY_SUBCLASS_CD and T8.DATA_DIV = '1' and T8.AVG_DIV = '03' and T2.GRADE = T8.GRADE and T2.COURSECD = T8.COURSECD and T2.MAJORCD = T8.MAJORCD and T2.COURSECODE = T8.COURSECODE ";
        $query .= "     LEFT JOIN PROFICIENCY_RANK_DAT T9 on T1.YEAR = T9.YEAR and T1.SEMESTER = T9.SEMESTER and T1.PROFICIENCYDIV = T9.PROFICIENCYDIV and T1.PROFICIENCYCD = T9.PROFICIENCYCD and T1.SCHREGNO = T9.SCHREGNO and T1.PROFICIENCY_SUBCLASS_CD = T9.PROFICIENCY_SUBCLASS_CD and T9.RANK_DATA_DIV = '01' and T9.RANK_DIV = '03' ";
        $query .= "     LEFT JOIN PROFICIENCY_RANK_DAT T10 on T1.YEAR = T10.YEAR and T1.SEMESTER = T10.SEMESTER and T1.PROFICIENCYDIV = T10.PROFICIENCYDIV and T1.PROFICIENCYCD = T10.PROFICIENCYCD and T1.SCHREGNO = T10.SCHREGNO and T10.PROFICIENCY_SUBCLASS_CD = '999999' and T10.RANK_DATA_DIV = '01' and T10.RANK_DIV = '01' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT W4 on T1.YEAR = W4.YEAR and T1.GRADE = W4.GRADE ";
        $query .= "     LEFT JOIN SCHOOL_MST W3 on int(T1.YEAR) - int(W4.grade_cd) + 1 = int(W3.YEAR) and W4.SCHOOL_KIND = W3.SCHOOL_KIND ";
        $query .= "  ";
        $query .= " WHERE ";
        $query .= "     T1.SCORE IS NOT NULL ";
        $query .= $where2;
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //試験（外部模試）
    function SCORE4updateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //TOOL_SCORE_DATクリア
        switch(SCHOOLKIND){
            case "P":
                $where1 = " WHERE BUMONCD = '1' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '4'";
                $where2 = " AND T2.GRADE BETWEEN  '01' AND '06' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "J":
                $where1 = " WHERE BUMONCD = '2' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '4'";
                $where2 = " AND T2.GRADE BETWEEN  '07' AND '09' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "H":
                $where1 = " WHERE (BUMONCD = '3' OR BUMONCD = '4') AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '4'";
                $where2 = " AND T2.GRADE BETWEEN  '10' AND '12' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            default :
                $where1 = "";
                $where2 = "";
                break 1;
        }
        
        $query = "DELETE FROM TOOL_SCORE_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_SCORE_DAT  ";
        $query .= "  ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR as NENDO, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '4' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '1' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '3' ";
        $query .= "     END AS BUMONCD, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '美デ' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '駿小' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '駿中' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '駿高' ";
        $query .= "     END AS BUMONNAME, ";
        $query .= "     W3.PRESENT_EST, ";
        $query .= "     ltrim(T2.GRADE,'0') AS GRADE, ";
        $query .= "     '4' as TEST_CATEGORYCD, ";
        $query .= "     '外部模擬テスト' as TESTCATEGORYNAME, ";
        $query .= "     NULL as SEMESTER, ";
        $query .= "     substr(T1.MOCKCD,2,4) || '000' || substr(T1.MOCKCD,6,4), ";
        $query .= "     T3.MOCKNAME1, ";
        $query .= "     NULL as CLASSCD, ";
        $query .= "     NULL as CLASSNAME, ";
        $query .= "     NULL as CLASSSORT, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T4.SUBCLASS_NAME, ";
        $query .= "     NULL as SUBCLASSSORT, ";
        $query .= "     NULL as GROUP_CHAIRCD, ";
        $query .= "     NULL as GROUP_CHAIRNAME, ";
        $query .= "     NULL as CHAIRCD, ";
        $query .= "     NULL as CHAIRNAME, ";
        $query .= "     0 as SCH_CHR_COUNT, ";
        $query .= "     NULL as PASS_SCORE, ";
        $query .= "     NULL as GROUP_AVG, ";
        $query .= "     NULL as GROUP_STDDEV, ";
        $query .= "     NULL as GROUP_RANK, ";
        $query .= "     NULL as AVG, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '1' as ATTEND, ";
        $query .= "     0 as ABSENCE, ";
        $query .= "     T1.SCORE, ";
        $query .= "     NULL as TOTAL_SCORE, ";
        $query .= "     NULL as TOTAL_AVG, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T4.UPDATED ";
        $query .= "     THEN T1.UPDATED ";
        $query .= "     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T4.UPDATED ";
        $query .= "     THEN T3.UPDATED ";
        $query .= "     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > T3.UPDATED ";
        $query .= "     THEN T4.UPDATED ";
        $query .= "     ELSE T1.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T4.UPDATED ";
        $query .= "     THEN T1.REGISTERCD ";
        $query .= "     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T4.UPDATED ";
        $query .= "     THEN T3.REGISTERCD ";
        $query .= "     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > T3.UPDATED ";
        $query .= "     THEN T4.REGISTERCD ";
        $query .= "     ELSE T1.REGISTERCD ";
        $query .= "     END AS REGISTERCD, ";
        $query .= "     SYSDATE() AS COMPDATETIME ";
        $query .= "      ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_RANGE_DAT T1  ";
        $query .= "     left join (select a2.* from ";
        $query .= "                                 ( ";
        $query .= "                                 SELECT ";
        $query .= "                                     year, ";
        $query .= "                                     schregno, ";
        $query .= "                                     max(semester) as semester ";
        $query .= "                                 FROM ";
        $query .= "                                     schreg_regd_dat ";
        $query .= "                                 GROUP BY ";
        $query .= "                                     year, ";
        $query .= "                                     schregno ";
        $query .= "                                 ) as a1 ";
        $query .= "                                 left join schreg_regd_dat a2 on a1.year = a2.year and a1.schregno = a2.schregno and a1.semester = a2.semester  ";
        $query .= "                 ) t2 on t1.SCHREGNO = t2.SCHREGNO and t1.year = t2.year ";
        $query .= "     LEFT JOIN MOCK_MST T3 on T1.MOCKCD = T3.MOCKCD ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST T4 on T1.MOCK_SUBCLASS_CD = T4.MOCK_SUBCLASS_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT W4 on T1.YEAR = W4.YEAR and T2.GRADE = W4.GRADE ";
        $query .= "     LEFT JOIN SCHOOL_MST W3 on int(T1.YEAR) - int(W4.grade_cd) + 1 = int(W3.YEAR) and W4.SCHOOL_KIND = W3.SCHOOL_KIND ";
        $query .= "  ";
        $query .= " WHERE ";
        $query .= "     T1.SCORE IS NOT NULL AND T1.RANK_RANGE = '1' ";
        $query .= $where2;
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //試験（センター）
    function SCORE5updateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //TOOL_SCORE_DATクリア
        switch(SCHOOLKIND){
            case "P":
                $where1 = " WHERE BUMONCD = '1' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '5'";
                $where2 = " AND T2.GRADE BETWEEN  '01' AND '06' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "J":
                $where1 = " WHERE BUMONCD = '2' AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '5'";
                $where2 = " AND T2.GRADE BETWEEN  '07' AND '09' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "H":
                $where1 = " WHERE (BUMONCD = '3' OR BUMONCD = '4') AND NENDO = ".CTRL_YEAR." AND TEST_CATEGORYCD = '5'";
                $where2 = " AND T2.GRADE BETWEEN  '10' AND '12' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            default :
                $where1 = "";
                $where2 = "";
                break 1;
        }
        
        $query = "DELETE FROM TOOL_SCORE_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_SCORE_DAT  ";
        $query .= "  ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR as NENDO, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '4' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '1' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '3' ";
        $query .= "     END AS BUMONCD, ";
        $query .= "     CASE  ";
        $query .= "     WHEN T2.HR_CLASS = '011' ";
        $query .= "     THEN '美デ' ";
        $query .= "     WHEN T2.GRADE BETWEEN '01' and '06' ";
        $query .= "     THEN '駿小' ";
        $query .= "     WHEN T2.GRADE BETWEEN '07' and '09' ";
        $query .= "     THEN '駿中' ";
        $query .= "     WHEN T2.GRADE BETWEEN '10' and '12' ";
        $query .= "     THEN '駿高' ";
        $query .= "     END AS BUMONNAME, ";
        $query .= "     W3.PRESENT_EST, ";
        $query .= "     ltrim(T2.GRADE,'0') AS GRADE, ";
        $query .= "     '5' as TEST_CATEGORYCD, ";
        $query .= "     'センター自己採点' as TESTCATEGORYNAME, ";
        $query .= "     NULL as SEMESTER, ";
        $query .= "     T1.CENTERCD || '00', ";
        $query .= "     'センター自己採点' as TESTITEMNAME, ";
        $query .= "     NULL as CLASSCD, ";
        $query .= "     NULL as CLASSNAME, ";
        $query .= "     NULL as CLASSSORT, ";
        $query .= "     T1.CENTER_SUBCLASS_CD, ";
        $query .= "     T4.SUBCLASS_NAME, ";
        $query .= "     NULL as SUBCLASSSORT, ";
        $query .= "     NULL as GROUP_CHAIRCD, ";
        $query .= "     NULL as GROUP_CHAIRNAME, ";
        $query .= "     NULL as CHAIRCD, ";
        $query .= "     NULL as CHAIRNAME, ";
        $query .= "     0 as SCH_CHR_COUNT, ";
        $query .= "     NULL as PASS_SCORE, ";
        $query .= "     NULL as GROUP_AVG, ";
        $query .= "     NULL as GROUP_STDDEV, ";
        $query .= "     NULL as GROUP_RANK, ";
        $query .= "     NULL as AVG, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '1' as ATTEND, ";
        $query .= "     0 as ABSENCE, ";
        $query .= "     T1.SCORE, ";
        $query .= "     NULL as TOTAL_SCORE, ";
        $query .= "     NULL as TOTAL_AVG, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T4.UPDATED ";
        $query .= "     THEN T1.UPDATED ";
        $query .= "     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T4.UPDATED ";
        $query .= "     THEN T3.UPDATED ";
        $query .= "     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > T3.UPDATED ";
        $query .= "     THEN T4.UPDATED ";
        $query .= "     ELSE T1.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T4.UPDATED ";
        $query .= "     THEN T1.REGISTERCD ";
        $query .= "     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T4.UPDATED ";
        $query .= "     THEN T3.REGISTERCD ";
        $query .= "     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > T3.UPDATED ";
        $query .= "     THEN T4.REGISTERCD ";
        $query .= "     ELSE T1.REGISTERCD ";
        $query .= "     END AS REGISTERCD, ";
        $query .= "     SYSDATE() AS COMPDATETIME ";
        $query .= "      ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_RANK_RANGE_DAT T1  ";
        $query .= "     left join (select a2.* from ";
        $query .= "                                 ( ";
        $query .= "                                 SELECT ";
        $query .= "                                     year, ";
        $query .= "                                     schregno, ";
        $query .= "                                     max(semester) as semester ";
        $query .= "                                 FROM ";
        $query .= "                                     schreg_regd_dat ";
        $query .= "                                 GROUP BY ";
        $query .= "                                     year, ";
        $query .= "                                     schregno ";
        $query .= "                                 ) as a1 ";
        $query .= "                                 left join schreg_regd_dat a2 on a1.year = a2.year and a1.schregno = a2.schregno and a1.semester = a2.semester  ";
        $query .= "                 ) t2 on t1.SCHREGNO = t2.SCHREGNO and t1.year = t2.year ";
        $query .= "     LEFT JOIN MOCK_CENTER_DAT T3 on T1.CENTERCD = T3.CENTERCD and T1.YEAR = T2.YEAR and T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN MOCK_CENTER_SUBCLASS_MST T4 on T1.CENTER_SUBCLASS_CD = T4.CENTER_SUBCLASS_CD and T1.YEAR = T4.YEAR ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT W4 on T1.YEAR = W4.YEAR and T2.GRADE = W4.GRADE ";
        $query .= "     LEFT JOIN SCHOOL_MST W3 on int(T1.YEAR) - int(W4.grade_cd) + 1 = int(W3.YEAR) and W4.SCHOOL_KIND = W3.SCHOOL_KIND ";
        $query .= "  ";
        $query .= " WHERE ";
        $query .= "     T1.SCORE IS NOT NULL ";
        $query .= $where2;
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //出欠
    function ATTENDupdateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //TOOL_ATTEND_DATクリア
        switch(SCHOOLKIND){
            case "P":
                $where1 = " WHERE BUMONCD = '1' AND NENDO = ".CTRL_YEAR;
                $where2 = " WHERE T2.GRADE BETWEEN  '01' AND '06'";
                $where3 = " WHERE YEAR = ".CTRL_YEAR;
                break 1;
            case "J":
                $where1 = " WHERE BUMONCD = '2' AND NENDO = ".CTRL_YEAR;
                $where2 = " WHERE T2.GRADE BETWEEN  '07' AND '09'";
                $where3 = " WHERE YEAR = ".CTRL_YEAR;
                break 1;
            case "H":
                $where1 = " WHERE BUMONCD = '3' OR BUMONCD = '4' AND NENDO = ".CTRL_YEAR;
                $where2 = " WHERE T2.GRADE BETWEEN  '10' AND '12'";
                $where3 = " WHERE YEAR = ".CTRL_YEAR;
                break 1;
            default :
                $where1 = "";
                $where2 = "";
                $where3 = "";
                break 1;
        }
        
        $query = "DELETE FROM TOOL_ATTEND_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_ATTEND_DAT  ";
        $query .= " SELECT  ";
        $query .= "     t1.YEAR, ";
        $query .= "     CASE ";
        $query .= "     WHEN HR_CLASS = '011' ";
        $query .= "     THEN '4' ";
        $query .= "     WHEN GRADE BETWEEN  '01' AND '06' ";
        $query .= "     THEN '1' ";
        $query .= "     WHEN GRADE BETWEEN  '07' AND '09' ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN GRADE BETWEEN  '10' AND '12' ";
        $query .= "     THEN '3' ";
        $query .= "     ELSE '3' ";
        $query .= "     END as BUMONCD, ";
        $query .= "     t1.SEMESTER, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     ltrim(t2.GRADE,'0') as GRADE, ";
        $query .= "     CASE ";
        $query .= "     WHEN W3.COUNT > 0 ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN (t3.GRD_DIV IS NULL OR t3.GRD_DIV = '4') or (t3.GRD_DIV IS NOT NULL and t4.EDATE < t3.GRD_DATE) ";
        $query .= "     THEN '1' ";
        $query .= "     ELSE '0' ";
        $query .= "     END as ENT_DIV, ";
        $query .= "     t1.LESSON, ";
        $query .= "     t1.MOURNING, ";
        $query .= "     t1.SUSPEND, ";
        $query .= "     t1.ABSENT, ";
        $query .= "     t1.SICK + t1.NOTICE + t1.NONOTICE as SICK, ";
        $query .= "     t1.LATE, ";
        $query .= "     t1.EARLY, ";
        $query .= "     CASE ";
        $query .= "     WHEN t1.UPDATED > t2.UPDATED and t1.UPDATED > t3.UPDATED and (t1.UPDATED > W3.UPDATED or W3.UPDATED IS NULL) ";
        $query .= "     THEN t1.UPDATED ";
        $query .= "     WHEN t2.UPDATED > t1.UPDATED and t2.UPDATED > t3.UPDATED and (t2.UPDATED > W3.UPDATED or W3.UPDATED IS NULL) ";
        $query .= "     THEN t2.UPDATED ";
        $query .= "     WHEN t3.UPDATED > t1.UPDATED and t3.UPDATED > t2.UPDATED and (t3.UPDATED > W3.UPDATED or W3.UPDATED IS NULL) ";
        $query .= "     THEN t3.UPDATED ";
        $query .= "     WHEN W3.UPDATED > t1.UPDATED and W3.UPDATED > t2.UPDATED and W3.UPDATED > t3.UPDATED and W3.UPDATED IS NOT NULL ";
        $query .= "     THEN W3.UPDATED ";
        $query .= "     ELSE t1.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     t1.REGISTERCD, ";
        $query .= "     sysdate() as COMPDATETIME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) as LESSON, ";
        $query .= "         SUM(MOURNING) as MOURNING, ";
        $query .= "         SUM(SUSPEND) as SUSPEND, ";
        $query .= "         SUM(ABSENT) as ABSENT, ";
        $query .= "         SUM(SICK) as SICK, ";
        $query .= "         SUM(NOTICE) as NOTICE, ";
        $query .= "         SUM(NONOTICE) as NONOTICE, ";
        $query .= "         SUM(LATE) AS LATE, ";
        $query .= "         SUM(EARLY) AS EARLY, ";
        $query .= "         MAX(REGISTERCD) as REGISTERCD, ";
        $query .= "         MAX(UPDATED) as UPDATED ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             YEAR, ";
        $query .= "             SEMESTER, ";
        $query .= "             SCHREGNO, ";
        $query .= "             CASE ";
        $query .= "             WHEN LESSON IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE LESSON ";
        $query .= "             END as LESSON, ";
        $query .= "             CASE ";
        $query .= "             WHEN MOURNING IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE MOURNING ";
        $query .= "             END as MOURNING, ";
        $query .= "             CASE ";
        $query .= "             WHEN SUSPEND IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE SUSPEND ";
        $query .= "             END as SUSPEND, ";
        $query .= "             CASE ";
        $query .= "             WHEN ABSENT IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE ABSENT ";
        $query .= "             END as ABSENT, ";
        $query .= "             CASE ";
        $query .= "             WHEN SICK IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE SICK ";
        $query .= "             END as SICK, ";
        $query .= "             CASE ";
        $query .= "             WHEN NOTICE IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE NOTICE ";
        $query .= "             END  as NOTICE, ";
        $query .= "             CASE ";
        $query .= "             WHEN NONOTICE IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE NONOTICE ";
        $query .= "             END as NONOTICE, ";
        $query .= "             CASE ";
        $query .= "             WHEN LATE IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE LATE ";
        $query .= "             END AS LATE, ";
        $query .= "             CASE ";
        $query .= "             WHEN EARLY IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE EARLY ";
        $query .= "             END AS EARLY, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ";
        $query .= "         FROM ";
        $query .= "             ATTEND_SEMES_DAT ";
        $query .= $where3;
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= "     ) t1  ";
        $query .= "     left join SCHREG_REGD_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER ";
        $query .= "     left join SCHREG_BASE_MST t3 on t1.SCHREGNO = t3.SCHREGNO ";
        $query .= "     LEFT JOIN SEMESTER_MST T4 on t1.year = t4.year and t1.semester = t4.semester ";
        $query .= "     LEFT JOIN (SELECT SCHREGNO,YEAR,SEMESTER,MAX(UPDATED) AS UPDATED,COUNT(*) AS COUNT FROM ";
        $query .= "                     ( ";
        $query .= "                     SELECT ";
        $query .= "                         SCHREGNO,YEAR,SEMESTER,T1.UPDATED ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_TRANSFER_DAT T1 ";
        $query .= "                         LEFT JOIN SEMESTER_MST T2 on (T1.TRANSFER_SDATE BETWEEN T2.SDATE AND T2.EDATE or T1.TRANSFER_EDATE BETWEEN T2.SDATE AND T2.EDATE) and T2.SEMESTER != '9' ";
        $query .= "                     ) ";
        $query .= "                GROUP BY SCHREGNO,YEAR,SEMESTER ";
        $query .= "               ) as W3 on T1.SCHREGNO = W3.SCHREGNO and T1.YEAR = W3.YEAR and T1.SEMESTER = W3.SEMESTER ";
        $query .= $where2;
        $query .= " ORDER BY YEAR ";
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //評定
    function RECORDupdateModel()
    {
        $flg = false;
        
        //DB接続
        switch(SCHOOLKIND){
            case "P":
                $where1 = " WHERE BUMONCD = '1' AND NENDO = ".CTRL_YEAR;
                $where2 = " AND W1.GRADE BETWEEN  '01' AND '06' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "J":
                $where1 = " WHERE BUMONCD = '2' AND NENDO = ".CTRL_YEAR;
                $where2 = " AND W1.GRADE BETWEEN  '07' AND '09' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            case "H":
                $where1 = " WHERE BUMONCD = '3' OR BUMONCD = '4' AND NENDO = ".CTRL_YEAR;
                $where2 = " AND W1.GRADE BETWEEN  '10' AND '12' AND T1.YEAR = ".CTRL_YEAR;
                break 1;
            default :
                $where1 = "";
                $where2 = "";
                break 1;
        }
        
        $db = Query::dbCheckOut();
        
        
        //TOOL_RECORD_DATクリア
        $query = "DELETE FROM TOOL_RECORD_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_RECORD_DAT ";
        $query .= "  ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     CASE ";
        $query .= "     WHEN W1.HR_CLASS = '011' ";
        $query .= "     THEN '4' ";
        $query .= "     WHEN W1.GRADE BETWEEN  '01' AND '06' ";
        $query .= "     THEN '1' ";
        $query .= "     WHEN W1.GRADE BETWEEN  '07' AND '09' ";
        $query .= "     THEN '2' ";
        $query .= "     WHEN W1.GRADE BETWEEN  '10' AND '12' ";
        $query .= "     THEN '3' ";
        $query .= "     END as BUMONCD, ";
        $query .= "     ltrim(W1.GRADE,'0') AS GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     T2.SHOWORDER, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T3.SHOWORDER, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.SCORE, ";
        $query .= "     CASE ";
        $query .= "     WHEN T4.PROV_FLG IS NULL ";
        $query .= "     THEN 0 ";
        $query .= "     ELSE T4.PROV_FLG ";
        $query .= "     END AS PROV_FLG, ";
        $query .= "     W2.SICK, ";
        $query .= "     CASE ";
        $query .= "     WHEN T1.UPDATED > T4.UPDATED AND T1.UPDATED > W2.UPDATED ";
        $query .= "     THEN T1.UPDATED ";
        $query .= "     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > W2.UPDATED ";
        $query .= "     THEN T4.UPDATED ";
        $query .= "     WHEN W2.UPDATED > T1.UPDATED AND W2.UPDATED > T4.UPDATED ";
        $query .= "     THEN W2.UPDATED ";
        $query .= "     ELSE T1.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     T1.REGISTERCD, ";
        $query .= "     SYSDATE() AS COMPDATETIME ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT DISTINCT YEAR,SCHREGNO,GRADE,HR_CLASS,ATTENDNO FROM SCHREG_REGD_DAT) W1 on T1.YEAR = W1.YEAR and T1.SCHREGNO = W1.SCHREGNO ";
        $query .= "     LEFT JOIN CLASS_MST T2 on T1.CLASSCD = T2.CLASSCD and T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 on T1.SUBCLASSCD = T3.SUBCLASSCD and T1.SCHOOL_KIND = T3.SCHOOL_KIND and T1.CLASSCD = T3.CLASSCD and T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "     LEFT JOIN RECORD_PROV_FLG_DAT T4 on T1.YEAR = T4.YEAR and T1.CLASSCD = T4.CLASSCD and T1.SCHOOL_KIND = T4.SCHOOL_KIND and T1.CURRICULUM_CD = T4.CURRICULUM_CD and T1.SUBCLASSCD = T4.SUBCLASSCD and T1.SCHREGNO = T4.SCHREGNO  ";
        $query .= "     LEFT JOIN (SELECT YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SUM(SICK) AS SICK,MAX(UPDATED) AS UPDATED FROM (SELECT YEAR,SEMESTER,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,(SICK + NOTICE + NONOTICE) AS SICK,UPDATED FROM (SELECT YEAR,SEMESTER,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CASE WHEN SICK IS NULL THEN 0 ELSE SICK END AS SICK,CASE WHEN NOTICE IS NULL THEN 0 ELSE NOTICE END AS NOTICE,CASE WHEN NONOTICE IS NULL THEN 0 ELSE NONOTICE END AS NONOTICE,UPDATED FROM ATTEND_SUBCLASS_DAT)) GROUP BY YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD) W2 on T1.YEAR = W2.YEAR and T1.SCHREGNO = W2.SCHREGNO and T1.CLASSCD = W2.CLASSCD and T1.SCHOOL_KIND = W2.SCHOOL_KIND and T1.CURRICULUM_CD = W2.CURRICULUM_CD and T1.SUBCLASSCD = W2.SUBCLASSCD ";
        $query .= "  ";
        $query .= "                  ";
        $query .= " WHERE ";
        $query .= "     T1.SEMESTER = '9' and T1.TESTKINDCD = '99' and T1.TESTITEMCD = '00' and SCORE_DIV = '09' ";
        $query .= $where2;
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //大学合格
    function GRADupdateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //TOOL_AFT_GRAD_DATクリア
        $where1 = " WHERE NENDO = ".CTRL_YEAR;
        $where2 = " AND t1.YEAR = ".CTRL_YEAR;

        $query = "DELETE FROM TOOL_AFT_GRAD_DAT".$where1;
        $db->query($query);
        
        $query  = " INSERT INTO TOOL_AFT_GRAD_DAT ";
        $query .= "  ";
        $query .= " SELECT  ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME as NAME_KANJI, ";
        $query .= "     t2.NAME_KANA, ";
        $query .= "     t1.STAT_CD, ";
        $query .= "     t3.SCHOOL_NAME, ";
        $query .= "     t1.FACULTYCD, ";
        $query .= "     t4.FACULTYNAME, ";
        $query .= "     t1.DEPARTMENTCD, ";
        $query .= "     t5.DEPARTMENTNAME, ";
        $query .= "     case when t6.REMARK1 = '1' then '1' ";
        $query .= "     else null end as ANONYMOUS_FLG, ";
        $query .= "     case when t2.GRD_DIV = '1' then '2' ";
        $query .= "     else '1' end as ZAISEKI_FLG, ";
        $query .= "     CASE ";
        $query .= "     WHEN t1.UPDATED > t2.UPDATED ";
        $query .= "     THEN t1.UPDATED ";
        $query .= "     WHEN t2.UPDATED > t1.UPDATED ";
        $query .= "     THEN t2.UPDATED ";
        $query .= "     ELSE t1.UPDATED ";
        $query .= "     END AS UPDATED, ";
        $query .= "     t1.REGISTERCD, ";
        $query .= "     sysdate() as COMPDATETIME ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT t1 ";
        $query .= "     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "     left join COLLEGE_MST t3 on t1.STAT_CD = t3.SCHOOL_CD ";
        $query .= "     left join COLLEGE_FACULTY_MST t4 on t1.STAT_CD = t4.SCHOOL_CD and t1.FACULTYCD = t4.FACULTYCD ";
        $query .= "     left join COLLEGE_DEPARTMENT_MST t5 on t1.STAT_CD = t5.SCHOOL_CD and t1.FACULTYCD = t5.FACULTYCD and t1.DEPARTMENTCD = t5.DEPARTMENTCD ";
        $query .= "     left join AFT_GRAD_COURSE_DETAIL_DAT t6 on t1.YEAR = t6.YEAR and t1.SEQ = t6.SEQ and t6.DETAIL_SEQ = '5' ";
        $query .= " WHERE ";
        $query .= "     t1.STAT_CD IS NOT NULL AND t1.DECISION IN ('1', '7') ";
        $query .= $where2;
        $query .= " ORDER BY SCHREGNO ";
        
        $db->query($query);
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


    //ユーザー
    function USERupdateModel()
    {
        $flg = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        
        $year = "";
        $userid = "";
        
        $count = 0;
        
        //TOOL_USER_MSTクリア
        $query = "DELETE FROM TOOL_USER_MST";
        $db->query($query);
        
        $update = array();
        $sep1 = "";
        $sep2 = "";
        $sep3 = "";
        
        $result = $db->query(knjx_toolQuery::getUserData($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if($count > 0 && ($row["YEAR"] != $year || $row["USERID"] != $userid)){
                common::_update("TOOL_USER_MST", "", "", $update, $db, "2");
                $update = array();
                $sep1 = "";
                $sep2 = "";
                $sep3 = "";
            }
            
            $year = $row["YEAR"];
            $userid = $row["USERID"];
            
            $update["NENDO"] = $year;
            $update["USERID"] = $userid;
            
            if($row["SCHOOL_KIND"] != ""){
                if($row["SCHOOL_KIND"] == "P"){
                    $bumoncd = "1";
                }
                if($row["SCHOOL_KIND"] == "J"){
                    $bumoncd = "2";
                }
                if($row["SCHOOL_KIND"] == "H"){
                    $bumoncd = "3";
                }
                if(strpos($update["BUMONCD"],$bumoncd) === false){
                    $update["BUMONCD"] .= $sep1.$bumoncd;
                    $sep1 = ",";
                }
            }
            
            if($row["TRGTGRADE"] != ""){
                if(strpos($update["GRADECD"],$row["TRGTGRADE"]) === false){
                    $update["GRADECD"] .= $sep2.$row["TRGTGRADE"];
                    $sep2 = ",";
                }
            }
            
            if($row["CLASSCD"] != ""){
                if(strpos($update["CLASSCD"],$row["CLASSCD"]) === false){
                    $update["CLASSCD"] .= $sep3.$row["CLASSCD"];
                    $sep3 = ",";
                }
            }
            
            //教務と教科主任は属してる部門のすべての学年が見れる
            if($row["GROUPCD"] == "0004" || $row["GROUPCD"] == "0021"){
                
                $sepG = "";
                $wrkG = "";
                if(strpos($update["BUMONCD"],"1") !== false){
                    $wrkG = "1,2,3,4,5,6";
                    $sepG = ",";
                }
                if(strpos($update["BUMONCD"],"2") !== false){
                    $wrkG .= $sepG;
                    $wrkG .= "7,8,9";
                    $sepG  = ",";
                }
                if(strpos($update["BUMONCD"],"3") !== false || strpos($update["BUMONCD"],"4") !== false){
                    $wrkG .= $sepG;
                    $wrkG .= "10,11,12";
                }
                if($wrkG != ""){
                    $update["GRADECD"] = $wrkG;
                }
            }
            
            //管理職（校長・副校長・教頭）と総務事務はすべての部門、学年をみることができる
            if($row["GROUPCD"] == "0003" || $row["GROUPCD"] == "0012"){
                $update["BUMONCD"] = "1,2,3,4";
                $update["GRADECD"] = "1,2,3,4,5,6,7,8,9,10,11,12";
            }
            
            $count ++;
        }
        
        //$count>0でデータ更新
        if($count > 0){
            common::_update("TOOL_USER_MST", "", "", $update, $db, "2");
        }
        
        //更新情報テーブル更新
        UPDATEupdate($db);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage('更新しました');
        
    }


}

function UPDATEupdate($db) {
    
    //TOOL_UPDATE_DATクリア
    $query = "DELETE FROM TOOL_UPDATE_DAT";
    $db->query($query);
    
    $query  = " INSERT INTO TOOL_UPDATE_DAT ";
    $query .= "  ";
    $query .= " SELECT ";
    $query .= "     MAX(UPDATED) AS UPDATED, ";
    $query .= "     NENDO, ";
    $query .= "     '1' as DATA_DIV, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE, ";
    $query .= "     NULL as CLASSCD, ";
    $query .= "     NULL as TESTID, ";
    $query .= "     COUNT(*) as RECORDS ";
    $query .= " FROM ";
    $query .= "     TOOL_SCHREG_DAT ";
    $query .= " GROUP BY ";
    $query .= "     NENDO, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE     ";
    $query .= " UNION ";
    $query .= " SELECT ";
    $query .= "     MAX(UPDATED) AS UPDATED, ";
    $query .= "     NENDO, ";
    $query .= "     '2' as DATA_DIV, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE, ";
    $query .= "     NULL as CLASSCD, ";
    $query .= "     TESTID, ";
    $query .= "     COUNT(*) as RECORDS ";
    $query .= " FROM ";
    $query .= "     TOOL_SCORE_DAT ";
    $query .= " GROUP BY ";
    $query .= "     NENDO, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE, ";
    $query .= "     TESTID ";
    $query .= " UNION ";
    $query .= " SELECT ";
    $query .= "     MAX(UPDATED) AS UPDATED, ";
    $query .= "     NENDO, ";
    $query .= "     '3' as DATA_DIV, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE, ";
    $query .= "     NULL as CLASSCD, ";
    $query .= "     NULL as TESTID, ";
    $query .= "     COUNT(*) as RECORDS ";
    $query .= " FROM ";
    $query .= "     TOOL_ATTEND_DAT ";
    $query .= " GROUP BY ";
    $query .= "     NENDO, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE ";
    $query .= " UNION ";
    $query .= " SELECT ";
    $query .= "     MAX(UPDATED) AS UPDATED, ";
    $query .= "     NENDO, ";
    $query .= "     '4' as DATA_DIV, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE, ";
    $query .= "     NULL as CLASSCD, ";
    $query .= "     NULL as TESTID, ";
    $query .= "     COUNT(*) as RECORDS ";
    $query .= " FROM ";
    $query .= "     TOOL_RECORD_DAT ";
    $query .= " GROUP BY ";
    $query .= "     NENDO, ";
    $query .= "     BUMONCD, ";
    $query .= "     GRADE ";
    $query .= " UNION ";
    $query .= " SELECT ";
    $query .= "     MAX(UPDATED) AS UPDATED, ";
    $query .= "     NENDO, ";
    $query .= "     '5' as DATA_DIV, ";
    $query .= "     '3' as BUMONCD, ";
    $query .= "     NULL as GRADE, ";
    $query .= "     NULL as CLASSCD, ";
    $query .= "     NULL as TESTID, ";
    $query .= "     COUNT(*) as RECORDS ";
    $query .= " FROM ";
    $query .= "     TOOL_AFT_GRAD_DAT ";
    $query .= " GROUP BY ";
    $query .= "     NENDO ";
    
    $db->query($query);
    
}

?>
