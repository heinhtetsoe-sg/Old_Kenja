<?php
class knjl363kQuery extends Query {

    //試験区分
    function GetTestdiv($model){
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L003' ";

        return $query;
    }

    //中高判別
    function GetJorH($model){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

    //CSVデータ取得
    function selectCvsQuery($model){

        $query  = " WITH NAME_L002 AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD1,NAMECD2,NAME1 AS L002NAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L002' ";
        $query .= " ), NAME_L006 AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD1,NAMECD2,ABBV1 AS L006ABBV ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L006' ";
        $query .= " ), COURSE_MST AS ( ";
        $query .= " SELECT ";
        $query .= "     COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ";
        $query .= "     EXAMCOURSE_NAME, ";
        $query .= "     EXAMCOURSE_MARK AS COURSEMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= " ), SCORE_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     EXAMNO,TESTSUBCLASSCD,A_SCORE,B_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
/**NO002-->
        $query .= " ),CONSD AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO, ";
        $query .= "     DATADIV, ";
        $query .= "     WISHNO, ";
        $query .= "     CASE WHEN VALUE(SHDIV,'0') = '0' THEN '-' ELSE n2.L006ABBV END AS SH_NM, ";
        $query .= "     COURSE, ";
        $query .= "     CASE WHEN VALUE(COURSEMARK,'0') = '0' THEN '-' ELSE COURSEMARK END AS COURSEMARK, ";
        $query .= "     CASE WHEN VALUE(JUDGEMENT,'0') = '0' THEN '-' ELSE n1.L002NAME END AS JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT t1 ";
        $query .= "     LEFT JOIN NAME_L002  n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_L006  n2 ON n2.NAMECD2 = SHDIV ";
        $query .= "     LEFT JOIN COURSE_MST t2 ON t2.COURSE = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.TESTDIV = '".$model->testdiv."' ";
        $query .= " ),CONS_HDAT AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.EXAMNO, ";
        $query .= "     t1.ACCEPTNO, ";
        $query .= "     t1.PS_ACCEPTNO, ";
        $query .= "     PS_ITEM1,PS_ITEM2,PS_ITEM4,PS_ITEM5, ";
        $query .= "     p1.WISHNO AS PS_WIS1,p1.COURSE AS PS_COURSE1,p1.COURSEMARK AS PS_COURSEMARK1, ";
        $query .= "     p1.JUDGEMENT AS PS_JUDG1,p1.SH_NM AS PS_SHDIV1, ";
        $query .= "     p2.WISHNO AS PS_WIS2,p2.COURSE AS PS_COURSE2,p2.COURSEMARK AS PS_COURSEMARK2, ";
        $query .= "     p2.JUDGEMENT AS PS_JUDG2,p2.SH_NM AS PS_SHDIV2, ";
        $query .= "     p3.WISHNO AS PS_WIS3,p3.COURSE AS PS_COURSE3,p3.COURSEMARK AS PS_COURSEMARK3, ";
        $query .= "     p3.JUDGEMENT AS PS_JUDG3,p3.SH_NM AS PS_SHDIV3, ";
        $query .= "     p4.WISHNO AS PS_WIS4,p4.COURSE AS PS_COURSE4,p4.COURSEMARK AS PS_COURSEMARK4, ";
        $query .= "     p4.JUDGEMENT AS PS_JUDG4,p4.SH_NM AS PS_SHDIV4, ";
        $query .= "     FS_ACCEPTNO, ";
        $query .= "     FS_ITEM1,FS_ITEM2,FS_ITEM4,FS_ITEM5, ";
        $query .= "     f1.WISHNO AS FS_WIS1,f1.COURSE AS FS_COURSE1,f1.COURSEMARK AS FS_COURSEMARK1, ";
        $query .= "     f1.JUDGEMENT AS FS_JUDG1,f1.SH_NM AS FS_SHDIV1, ";
        $query .= "     f2.WISHNO AS FS_WIS2,f2.COURSE AS FS_COURSE2,f2.COURSEMARK AS FS_COURSEMARK2, ";
        $query .= "     f2.JUDGEMENT AS FS_JUDG2,f2.SH_NM AS FS_SHDIV2, ";
        $query .= "     f3.WISHNO AS FS_WIS3,f3.COURSE AS FS_COURSE3,f3.COURSEMARK AS FS_COURSEMARK3, ";
        $query .= "     f3.JUDGEMENT AS FS_JUDG3,f3.SH_NM AS FS_SHDIV3, ";
        $query .= "     f4.WISHNO AS FS_WIS4,f4.COURSE AS FS_COURSE4,f4.COURSEMARK AS FS_COURSEMARK4, ";
        $query .= "     f4.JUDGEMENT AS FS_JUDG4,f4.SH_NM AS FS_SHDIV4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT t1 ";
        $query .= "     LEFT JOIN CONSD p1 ON p1.ACCEPTNO = t1.PS_ACCEPTNO ";
        $query .= "     AND p1.DATADIV = '2' ";
        $query .= "     AND p1.WISHNO = '1' ";
        $query .= "     LEFT JOIN CONSD p2 ON p2.ACCEPTNO = t1.PS_ACCEPTNO ";
        $query .= "     AND p2.DATADIV = '2' ";
        $query .= "     AND p2.WISHNO = '2' ";
        $query .= "     LEFT JOIN CONSD p3 ON p3.ACCEPTNO = t1.PS_ACCEPTNO ";
        $query .= "     AND p3.DATADIV = '2' ";
        $query .= "     AND p3.WISHNO = '3' ";
        $query .= "     LEFT JOIN CONSD p4 ON p4.ACCEPTNO = t1.PS_ACCEPTNO ";
        $query .= "     AND p4.DATADIV = '2' ";
        $query .= "     AND p4.WISHNO = '4' ";

        $query .= "     LEFT JOIN CONSD f1 ON f1.ACCEPTNO = t1.FS_ACCEPTNO ";
        $query .= "     AND f1.DATADIV = '1' ";
        $query .= "     AND f1.WISHNO = '1' ";
        $query .= "     LEFT JOIN CONSD f2 ON f2.ACCEPTNO = t1.FS_ACCEPTNO ";
        $query .= "     AND f2.DATADIV = '1' ";
        $query .= "     AND f2.WISHNO = '2' ";
        $query .= "     LEFT JOIN CONSD f3 ON f3.ACCEPTNO = t1.FS_ACCEPTNO ";
        $query .= "     AND f3.DATADIV = '1' ";
        $query .= "     AND f3.WISHNO = '3' ";
        $query .= "     LEFT JOIN CONSD f4 ON f4.ACCEPTNO = t1.FS_ACCEPTNO ";
        $query .= "     AND f4.DATADIV = '1' ";
        $query .= "     AND f4.WISHNO = '4' ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND t1.EXAMNO is not null ";
        $query .= "     AND t1.EXAMNO != '' ";
<--NO002***/
//NO002-->
        $query .= " ),CONSD AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO, ";
        $query .= "     DATADIV, ";
        $query .= "     WISHNO, ";
        $query .= "     CASE WHEN VALUE(SHDIV,'0') = '0' THEN '-' ELSE n2.L006ABBV END AS SH_NM, ";
        $query .= "     COURSE, ";
        $query .= "     CASE WHEN VALUE(COURSEMARK,'0') = '0' THEN '-' ELSE COURSEMARK END AS COURSEMARK, ";
        $query .= "     n1.L002NAME AS JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT t1 ";
        $query .= "     LEFT JOIN NAME_L002  n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_L006  n2 ON n2.NAMECD2 = SHDIV ";
        $query .= "     LEFT JOIN COURSE_MST t2 ON t2.COURSE = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.TESTDIV = '".$model->testdiv."' ";
        $query .= " ),CONSD1 AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO, ";
        $query .= "     DATADIV, ";
        $query .= "     SH_NM, ";
        $query .= "     COURSEMARK, ";
        $query .= "     VALUE(MIN(JUDGEMENT),'-') AS JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     CONSD ";
        $query .= " WHERE ";
        $query .= "     SH_NM IN ('S','H') AND COURSEMARK IN ('S','K','T','P') ";
        $query .= " GROUP BY ACCEPTNO,DATADIV,SH_NM,COURSEMARK ";
        $query .= " ),CONSD2 AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='S' AND COURSEMARK='S' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FS_JUDG1 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='S' AND COURSEMARK='K' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FS_JUDG2 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='S' AND COURSEMARK='T' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FS_JUDG3 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='S' AND COURSEMARK='P' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FS_JUDG4 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='H' AND COURSEMARK='S' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FH_JUDG1 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='H' AND COURSEMARK='K' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FH_JUDG2 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='H' AND COURSEMARK='T' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FH_JUDG3 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='1' AND SH_NM='H' AND COURSEMARK='P' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS FH_JUDG4 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='S' AND COURSEMARK='S' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PS_JUDG1 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='S' AND COURSEMARK='K' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PS_JUDG2 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='S' AND COURSEMARK='T' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PS_JUDG3 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='S' AND COURSEMARK='P' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PS_JUDG4 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='H' AND COURSEMARK='S' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PH_JUDG1 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='H' AND COURSEMARK='K' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PH_JUDG2 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='H' AND COURSEMARK='T' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PH_JUDG3 ";
        $query .= "    ,MIN(CASE WHEN DATADIV='2' AND SH_NM='H' AND COURSEMARK='P' THEN COURSEMARK||SH_NM||JUDGEMENT END) AS PH_JUDG4 ";
        $query .= " FROM ";
        $query .= "     CONSD1 ";
        $query .= " GROUP BY ACCEPTNO ";
        $query .= " ),CONSHD AS ( ";
        $query .= " SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     ACCEPTNO ";
        $query .= "    ,PS_ITEM1,PS_ITEM2,PS_ITEM4,PS_ITEM5 ";
        $query .= "    ,FS_ITEM1,FS_ITEM2,FS_ITEM4,FS_ITEM5 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND EXAMNO is not null ";
        $query .= "     AND EXAMNO != '' ";
        $query .= " ),CONS_HDAT AS ( ";
        $query .= " SELECT ";
        $query .= "     W1.EXAMNO ";
        $query .= "    ,FS_ITEM1,FS_ITEM2,FS_ITEM4,FS_ITEM5 ";
        $query .= "    ,FS_JUDG1,FS_JUDG2,FS_JUDG3,FS_JUDG4 ,FH_JUDG1,FH_JUDG2,FH_JUDG3,FH_JUDG4 ";
        $query .= "    ,PS_ITEM1,PS_ITEM2,PS_ITEM4,PS_ITEM5 ";
        $query .= "    ,PS_JUDG1,PS_JUDG2,PS_JUDG3,PS_JUDG4 ,PH_JUDG1,PH_JUDG2,PH_JUDG3,PH_JUDG4 ";
        $query .= " FROM ";
        $query .= "     CONSHD W1 ";
        $query .= "     LEFT JOIN CONSD2 W2 ON W2.ACCEPTNO=W1.ACCEPTNO ";
//NO002<--
        $query .= " ), APPLICONS_S AS ( ";  //NO001
        $query .= " SELECT ";
        $query .= "     t1.EXAMNO,t2.COURSEMARK AS APPCOURSEMARK, ";
        $query .= "     CASE WHEN VALUE(n1.L002NAME,'0') = '0' THEN '-' ELSE n1.L002NAME END AS APPJUDG, ";
        $query .= "     n2.L006ABBV AS APPSH_NM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONS_DAT t1 ";
        $query .= "     LEFT JOIN COURSE_MST t2 ON t2.COURSE = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN NAME_L002  n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_L006  n2 ON n2.NAMECD2 = SHDIV ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     t1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     t1.SHDIV = '1' AND ";   //NO001
        $query .= "     t1.JUDGEMENT IS NOT NULL AND "; //NO004
        $query .= "     exists(SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                ENTEXAM_APPLICANTBASE_DAT e1 ";
        $query .= "            WHERE ";
        $query .= "                e1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "                AND e1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                AND e1.TESTDIV = '".$model->testdiv."' ";
        $query .= "                AND e1.EXAMNO = t1.EXAMNO) ";    //NO001
//      $query .= "                AND e1.SHDIV = t1.SHDIV) ";
//NO001-->
        $query .= " ), APPLICONS_H AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.EXAMNO,t2.COURSEMARK AS APPCOURSEMARK, ";
        $query .= "     CASE WHEN VALUE(n1.L002NAME,'0') = '0' THEN '-' ELSE n1.L002NAME END AS APPJUDG, ";
        $query .= "     n2.L006ABBV AS APPSH_NM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONS_DAT t1 ";
        $query .= "     LEFT JOIN COURSE_MST t2 ON t2.COURSE = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN NAME_L002  n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_L006  n2 ON n2.NAMECD2 = SHDIV ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     t1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     t1.SHDIV = '2' AND ";
        $query .= "     t1.JUDGEMENT IS NOT NULL AND "; //NO004
        $query .= "     exists(SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                ENTEXAM_APPLICANTBASE_DAT e1 ";
        $query .= "            WHERE ";
        $query .= "                e1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "                AND e1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                AND e1.TESTDIV = '".$model->testdiv."' ";
        $query .= "                AND e1.EXAMNO = t1.EXAMNO) ";
//NO001<--
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t1.EXAMNO,t1.NAME,t1.NAME_KANA,n1.ABBV1,BIRTHDAY,ADDRESSCD,t6.ADDRESS AS ADDR, ";   //NO003
        $query .= "     t1.TELNO,t1.GNAME,t1.GKANA,t1.LOCATIONCD,t1.NATPUBPRIDIV, ";
        $query .= "     t1.FS_CD,t2.FINSCHOOL_KANA,t2.FINSCHOOL_NAME,t2.FINSCHOOL_ADDR1,t2.FINSCHOOL_ADDR2, ";
        $query .= "     t3.PRISCHOOL_NAME,t1.PS_CD, ";
        $query .= "     n2.NAME1,t1.DESIREDIV,n3.L006ABBV,t1.SPECIAL_REASON_DIV,t4.EXAMCOURSE_NAME,n4.ABBV1 AS JUDGENM,t1.SCALASHIPDIV, ";
        $query .= "     t1.A_TOTAL,s1.A_SCORE AS A_KOKU,s2.A_SCORE AS A_SYA,s3.A_SCORE AS A_SU,s4.A_SCORE AS A_RIKA,s5.A_SCORE AS A_ENG, ";
        $query .= "     t1.B_TOTAL,s1.B_SCORE AS B_KOKU,s2.B_SCORE AS B_SYA,s3.B_SCORE AS B_SU,s4.B_SCORE AS B_RIKA,s5.B_SCORE AS B_ENG, ";
        $query .= "     as1.APPCOURSEMARK AS APPMS1,as1.APPSH_NM AS APPS1,as1.APPJUDG AS APPJS1, ";
        $query .= "     as2.APPCOURSEMARK AS APPMS2,as2.APPSH_NM AS APPS2,as2.APPJUDG AS APPJS2, ";
        $query .= "     as3.APPCOURSEMARK AS APPMS3,as3.APPSH_NM AS APPS3,as3.APPJUDG AS APPJS3, ";
        $query .= "     as4.APPCOURSEMARK AS APPMS4,as4.APPSH_NM AS APPS4,as4.APPJUDG AS APPJS4, ";
//NO001-->
        $query .= "     ah1.APPCOURSEMARK AS APPMH1,ah1.APPSH_NM AS APPH1,ah1.APPJUDG AS APPJH1, ";
        $query .= "     ah2.APPCOURSEMARK AS APPMH2,ah2.APPSH_NM AS APPH2,ah2.APPJUDG AS APPJH2, ";
        $query .= "     ah3.APPCOURSEMARK AS APPMH3,ah3.APPSH_NM AS APPH3,ah3.APPJUDG AS APPJH3, ";
        $query .= "     ah4.APPCOURSEMARK AS APPMH4,ah4.APPSH_NM AS APPH4,ah4.APPJUDG AS APPJH4, ";
//NO001<--
/**NO002-->
        $query .= "     t5.FS_ITEM1,FS_ITEM2,FS_ITEM4,FS_ITEM5, ";
        $query .= "     t5.FS_COURSEMARK1,t5.FS_SHDIV1,t5.FS_JUDG1, ";
        $query .= "     t5.FS_COURSEMARK2,t5.FS_SHDIV2,t5.FS_JUDG2, ";
        $query .= "     t5.FS_COURSEMARK3,t5.FS_SHDIV3,t5.FS_JUDG3, ";
        $query .= "     t5.FS_COURSEMARK4,t5.FS_SHDIV4,t5.FS_JUDG4, ";
        $query .= "     t5.PS_ITEM1,PS_ITEM2,PS_ITEM4,PS_ITEM5, ";
        $query .= "     t5.PS_COURSEMARK1,t5.PS_SHDIV1,t5.PS_JUDG1, ";
        $query .= "     t5.PS_COURSEMARK2,t5.PS_SHDIV2,t5.PS_JUDG2, ";
        $query .= "     t5.PS_COURSEMARK3,t5.PS_SHDIV3,t5.PS_JUDG3, ";
        $query .= "     t5.PS_COURSEMARK4,t5.PS_SHDIV4,t5.PS_JUDG4 ";
<--NO002***/
//NO002-->
        $query .= "     FS_ITEM1,FS_ITEM2,FS_ITEM4,FS_ITEM5 ";
        $query .= "    ,FS_JUDG1,FS_JUDG2,FS_JUDG3,FS_JUDG4 ,FH_JUDG1,FH_JUDG2,FH_JUDG3,FH_JUDG4 ";
        $query .= "    ,PS_ITEM1,PS_ITEM2,PS_ITEM4,PS_ITEM5 ";
        $query .= "    ,PS_JUDG1,PS_JUDG2,PS_JUDG3,PS_JUDG4 ,PH_JUDG1,PH_JUDG2,PH_JUDG3,PH_JUDG4 ";
//NO002<--
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT t1 ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST t2 ON t2.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t2.FINSCHOOLCD = t1.FS_CD ";
        $query .= "     LEFT JOIN V_PRISCHOOL_MST t3 ON t3.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t3.PRISCHOOLCD = t1.PS_CD ";
        $query .= "     LEFT JOIN COURSE_MST t4 ON t4.COURSE = t1.SUC_COURSECD||t1.SUC_MAJORCD||t1.SUC_COURSECODE ";
        $query .= "     LEFT JOIN CONS_HDAT t5 ON t5.EXAMNO = t1.EXAMNO ";
//NO003-->
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT t6 ON t6.ENTEXAMYEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t6.TESTDIV = t1.TESTDIV ";
        $query .= "     AND t6.EXAMNO = t1.EXAMNO ";
//NO003<--
        $query .= "     LEFT JOIN V_NAME_MST n1 ON n1.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND n1.NAMECD1 = 'Z002' ";
        $query .= "     AND n1.NAMECD2 = t1.SEX ";
        $query .= "     LEFT JOIN V_NAME_MST n2 ON n2.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND n2.NAMECD1 = 'L005' ";
        $query .= "     AND n2.NAMECD2 = t1.APPLICANTDIV ";
        $query .= "     LEFT JOIN NAME_L006  n3 ON n3.NAMECD2 = SHDIV ";
        $query .= "     LEFT JOIN V_NAME_MST n4 ON n4.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND n4.NAMECD1 = 'L010' ";
        $query .= "     AND n4.NAMECD2 = t1.JUDGEMENT ";

        $query .= "     LEFT JOIN SCORE_DAT  s1 ON s1.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s1.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN SCORE_DAT  s2 ON s2.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s2.TESTSUBCLASSCD = '2' ";
        $query .= "     LEFT JOIN SCORE_DAT  s3 ON s3.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s3.TESTSUBCLASSCD = '3' ";
        $query .= "     LEFT JOIN SCORE_DAT  s4 ON s4.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s4.TESTSUBCLASSCD = '4' ";
        $query .= "     LEFT JOIN SCORE_DAT  s5 ON s5.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s5.TESTSUBCLASSCD = '5' ";

        $query .= "     LEFT JOIN APPLICONS_S as1 ON as1.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as1.APPCOURSEMARK = 'S' ";
        $query .= "     LEFT JOIN APPLICONS_S as2 ON as2.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as2.APPCOURSEMARK = 'K' ";
        $query .= "     LEFT JOIN APPLICONS_S as3 ON as3.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as3.APPCOURSEMARK = 'T' ";
        $query .= "     LEFT JOIN APPLICONS_S as4 ON as4.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as4.APPCOURSEMARK = 'P' ";
//NO001-->
        $query .= "     LEFT JOIN APPLICONS_H ah1 ON ah1.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah1.APPCOURSEMARK = 'S' ";
        $query .= "     LEFT JOIN APPLICONS_H ah2 ON ah2.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah2.APPCOURSEMARK = 'K' ";
        $query .= "     LEFT JOIN APPLICONS_H ah3 ON ah3.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah3.APPCOURSEMARK = 'T' ";
        $query .= "     LEFT JOIN APPLICONS_H ah4 ON ah4.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah4.APPCOURSEMARK = 'P' ";
//NO001<--
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "     AND t1.TESTDIV = '".$model->testdiv."' ";

        return $query;
    }

}
?>
