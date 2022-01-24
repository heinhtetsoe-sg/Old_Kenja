<?php

require_once('for_php7.php');

class knji100c_12Query extends Query {

    //CSVデータ取得
    function selectCvsQuery($model) {
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CAST(YEAR(BASE.ENT_DATE) AS CHAR(4)) AS ENTEXAMYEAR, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     CON_H.ACCEPTNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN V_SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN ENTEXAM_CONSULTATION_HDAT CON_H ON BASE.EXAMNO = CON_H.EXAMNO ";
        $query .= "          AND CAST(YEAR(BASE.ENT_DATE) AS CHAR(4)) = CON_H.ENTEXAMYEAR ";
        $query .= "          AND CON_H.TESTDIV = '".$model->testDiv."' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ), NAME_L002 AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD1,NAMECD2,NAME1 AS L002NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L002' ";
        $query .= " ), NAME_L006 AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD1,NAMECD2,ABBV1 AS L006ABBV ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L006' ";
        $query .= " ), COURSE_MST AS ( ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ";
        $query .= "     EXAMCOURSE_NAME, ";
        $query .= "     EXAMCOURSE_MARK AS COURSEMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " ), SCORE_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     EXAMNO,TESTSUBCLASSCD,A_SCORE,B_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '".$model->testDiv."' ";
        $query .= "     AND EXISTS (SELECT 'x' FROM SCH_T WHERE T1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND T1.EXAMNO = SCH_T.EXAMNO) ";
        $query .= " ),CONSD AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.ENTEXAMYEAR, ";
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
        $query .= "     LEFT JOIN COURSE_MST t2 ON t1.ENTEXAMYEAR = t2.ENTEXAMYEAR AND t2.COURSE = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     t1.TESTDIV = '".$model->testDiv."' ";
        $query .= "     AND EXISTS (SELECT 'x' FROM SCH_T WHERE t1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND t1.ACCEPTNO = SCH_T.ACCEPTNO) ";
        $query .= " ),CONSD1 AS ( ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     ACCEPTNO, ";
        $query .= "     DATADIV, ";
        $query .= "     SH_NM, ";
        $query .= "     COURSEMARK, ";
        $query .= "     VALUE(MIN(JUDGEMENT),'-') AS JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     CONSD ";
        $query .= " WHERE ";
        $query .= "     SH_NM IN ('S','H') AND COURSEMARK IN ('S','K','T','P') ";
        $query .= " GROUP BY ENTEXAMYEAR,ACCEPTNO,DATADIV,SH_NM,COURSEMARK ";
        $query .= " ),CONSD2 AS ( ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
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
        $query .= " GROUP BY ENTEXAMYEAR,ACCEPTNO ";
        $query .= " ),CONSHD AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.ACCEPTNO ";
        $query .= "    ,T1.PS_ITEM1,T1.PS_ITEM2,T1.PS_ITEM4,T1.PS_ITEM5 ";
        $query .= "    ,T1.FS_ITEM1,T1.FS_ITEM2,T1.FS_ITEM4,T1.FS_ITEM5 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TESTDIV = '".$model->testDiv."' ";
        $query .= "     AND T1.EXAMNO is not null ";
        $query .= "     AND T1.EXAMNO != '' ";
        $query .= "     AND EXISTS (SELECT 'x' FROM SCH_T WHERE T1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND T1.EXAMNO = SCH_T.EXAMNO) ";
        $query .= " ),CONS_HDAT AS ( ";
        $query .= " SELECT ";
        $query .= "     W1.ENTEXAMYEAR, ";
        $query .= "     W1.EXAMNO ";
        $query .= "    ,FS_ITEM1,FS_ITEM2,FS_ITEM4,FS_ITEM5 ";
        $query .= "    ,FS_JUDG1,FS_JUDG2,FS_JUDG3,FS_JUDG4 ,FH_JUDG1,FH_JUDG2,FH_JUDG3,FH_JUDG4 ";
        $query .= "    ,PS_ITEM1,PS_ITEM2,PS_ITEM4,PS_ITEM5 ";
        $query .= "    ,PS_JUDG1,PS_JUDG2,PS_JUDG3,PS_JUDG4 ,PH_JUDG1,PH_JUDG2,PH_JUDG3,PH_JUDG4 ";
        $query .= " FROM ";
        $query .= "     CONSHD W1 ";
        $query .= "     LEFT JOIN CONSD2 W2 ON W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND W2.ACCEPTNO=W1.ACCEPTNO ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT 'x' FROM SCH_T WHERE W1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND W1.EXAMNO = SCH_T.EXAMNO) ";
        $query .= " ), APPLICONS_S AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.ENTEXAMYEAR, ";
        $query .= "     t1.EXAMNO,t2.COURSEMARK AS APPCOURSEMARK, ";
        $query .= "     CASE WHEN VALUE(n1.L002NAME,'0') = '0' THEN '-' ELSE n1.L002NAME END AS APPJUDG, ";
        $query .= "     n2.L006ABBV AS APPSH_NM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONS_DAT t1 ";
        $query .= "     LEFT JOIN COURSE_MST t2 ON t1.ENTEXAMYEAR = t2.ENTEXAMYEAR AND t2.COURSE = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN NAME_L002  n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_L006  n2 ON n2.NAMECD2 = SHDIV ";
        $query .= " WHERE ";
        $query .= "     t1.TESTDIV = '".$model->testDiv."' AND ";
        $query .= "     EXISTS (SELECT 'x' FROM SCH_T WHERE t1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND t1.EXAMNO = SCH_T.EXAMNO) AND ";
        $query .= "     t1.SHDIV = '1' AND ";
        $query .= "     t1.JUDGEMENT IS NOT NULL AND ";
        $query .= "     exists(SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                ENTEXAM_APPLICANTBASE_DAT e1 ";
        $query .= "            WHERE ";
        $query .= "                e1.ENTEXAMYEAR = t1.ENTEXAMYEAR ";
        $query .= "                AND EXISTS (SELECT 'x' FROM SCH_T WHERE e1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND e1.EXAMNO = SCH_T.EXAMNO) ";
        $query .= "                AND e1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                AND e1.TESTDIV = '".$model->testDiv."' ";
        $query .= "                AND e1.EXAMNO = t1.EXAMNO) ";
        $query .= " ), APPLICONS_H AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.ENTEXAMYEAR, ";
        $query .= "     t1.EXAMNO,t2.COURSEMARK AS APPCOURSEMARK, ";
        $query .= "     CASE WHEN VALUE(n1.L002NAME,'0') = '0' THEN '-' ELSE n1.L002NAME END AS APPJUDG, ";
        $query .= "     n2.L006ABBV AS APPSH_NM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONS_DAT t1 ";
        $query .= "     LEFT JOIN COURSE_MST t2 ON t1.ENTEXAMYEAR = t2.ENTEXAMYEAR AND t2.COURSE = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN NAME_L002  n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_L006  n2 ON n2.NAMECD2 = SHDIV ";
        $query .= " WHERE ";
        $query .= "     t1.TESTDIV = '".$model->testDiv."' AND ";
        $query .= "     EXISTS (SELECT 'x' FROM SCH_T WHERE t1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND t1.EXAMNO = SCH_T.EXAMNO) AND ";
        $query .= "     t1.SHDIV = '2' AND ";
        $query .= "     t1.JUDGEMENT IS NOT NULL AND ";
        $query .= "     exists(SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                ENTEXAM_APPLICANTBASE_DAT e1 ";
        $query .= "            WHERE ";
        $query .= "                e1.ENTEXAMYEAR = t1.ENTEXAMYEAR ";
        $query .= "                AND e1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                AND e1.TESTDIV = '".$model->testDiv."' ";
        $query .= "                AND e1.EXAMNO = t1.EXAMNO) ";
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.ENTEXAMYEAR, ";
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
        $query .= "     ah1.APPCOURSEMARK AS APPMH1,ah1.APPSH_NM AS APPH1,ah1.APPJUDG AS APPJH1, ";
        $query .= "     ah2.APPCOURSEMARK AS APPMH2,ah2.APPSH_NM AS APPH2,ah2.APPJUDG AS APPJH2, ";
        $query .= "     ah3.APPCOURSEMARK AS APPMH3,ah3.APPSH_NM AS APPH3,ah3.APPJUDG AS APPJH3, ";
        $query .= "     ah4.APPCOURSEMARK AS APPMH4,ah4.APPSH_NM AS APPH4,ah4.APPJUDG AS APPJH4, ";
        $query .= "     FS_ITEM1,FS_ITEM2,FS_ITEM4,FS_ITEM5 ";
        $query .= "    ,FS_JUDG1,FS_JUDG2,FS_JUDG3,FS_JUDG4 ,FH_JUDG1,FH_JUDG2,FH_JUDG3,FH_JUDG4 ";
        $query .= "    ,PS_ITEM1,PS_ITEM2,PS_ITEM4,PS_ITEM5 ";
        $query .= "    ,PS_JUDG1,PS_JUDG2,PS_JUDG3,PS_JUDG4 ,PH_JUDG1,PH_JUDG2,PH_JUDG3,PH_JUDG4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT t1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST t2 ON t2.FINSCHOOLCD = t1.FS_CD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST t3 ON t3.PRISCHOOLCD = t1.PS_CD ";
        $query .= "     LEFT JOIN COURSE_MST t4 ON t1.ENTEXAMYEAR = t4.ENTEXAMYEAR AND t4.COURSE = t1.SUC_COURSECD||t1.SUC_MAJORCD||t1.SUC_COURSECODE ";
        $query .= "     LEFT JOIN CONS_HDAT t5 ON t1.ENTEXAMYEAR = t5.ENTEXAMYEAR AND t5.EXAMNO = t1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT t6 ON t6.ENTEXAMYEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t6.TESTDIV = t1.TESTDIV ";
        $query .= "     AND t6.EXAMNO = t1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST n1 ON n1.NAMECD1 = 'Z002' ";
        $query .= "     AND n1.NAMECD2 = t1.SEX ";
        $query .= "     LEFT JOIN NAME_MST n2 ON n2.NAMECD1 = 'L005' ";
        $query .= "     AND n2.NAMECD2 = t1.APPLICANTDIV ";
        $query .= "     LEFT JOIN NAME_L006  n3 ON n3.NAMECD2 = SHDIV ";
        $query .= "     LEFT JOIN NAME_MST n4 ON n4.NAMECD1 = 'L010' ";
        $query .= "     AND n4.NAMECD2 = t1.JUDGEMENT ";
        $query .= "     LEFT JOIN SCORE_DAT  s1 ON t1.ENTEXAMYEAR = s1.ENTEXAMYEAR AND s1.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s1.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN SCORE_DAT  s2 ON t1.ENTEXAMYEAR = s2.ENTEXAMYEAR AND s2.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s2.TESTSUBCLASSCD = '2' ";
        $query .= "     LEFT JOIN SCORE_DAT  s3 ON t1.ENTEXAMYEAR = s3.ENTEXAMYEAR AND s3.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s3.TESTSUBCLASSCD = '3' ";
        $query .= "     LEFT JOIN SCORE_DAT  s4 ON t1.ENTEXAMYEAR = s4.ENTEXAMYEAR AND s4.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s4.TESTSUBCLASSCD = '4' ";
        $query .= "     LEFT JOIN SCORE_DAT  s5 ON t1.ENTEXAMYEAR = s5.ENTEXAMYEAR AND s5.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s5.TESTSUBCLASSCD = '5' ";
        $query .= "     LEFT JOIN APPLICONS_S as1 ON t1.ENTEXAMYEAR = as1.ENTEXAMYEAR AND as1.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as1.APPCOURSEMARK = 'S' ";
        $query .= "     LEFT JOIN APPLICONS_S as2 ON t1.ENTEXAMYEAR = as2.ENTEXAMYEAR AND as2.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as2.APPCOURSEMARK = 'K' ";
        $query .= "     LEFT JOIN APPLICONS_S as3 ON t1.ENTEXAMYEAR = as3.ENTEXAMYEAR AND as3.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as3.APPCOURSEMARK = 'T' ";
        $query .= "     LEFT JOIN APPLICONS_S as4 ON t1.ENTEXAMYEAR = as4.ENTEXAMYEAR AND as4.EXAMNO = t1.EXAMNO ";
        $query .= "     AND as4.APPCOURSEMARK = 'P' ";
        $query .= "     LEFT JOIN APPLICONS_H ah1 ON t1.ENTEXAMYEAR = ah1.ENTEXAMYEAR AND ah1.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah1.APPCOURSEMARK = 'S' ";
        $query .= "     LEFT JOIN APPLICONS_H ah2 ON t1.ENTEXAMYEAR = ah2.ENTEXAMYEAR AND ah2.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah2.APPCOURSEMARK = 'K' ";
        $query .= "     LEFT JOIN APPLICONS_H ah3 ON t1.ENTEXAMYEAR = ah3.ENTEXAMYEAR AND ah3.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah3.APPCOURSEMARK = 'T' ";
        $query .= "     LEFT JOIN APPLICONS_H ah4 ON t1.ENTEXAMYEAR = ah4.ENTEXAMYEAR AND ah4.EXAMNO = t1.EXAMNO ";
        $query .= "     AND ah4.APPCOURSEMARK = 'P' ";
        $query .= " WHERE ";
        $query .= "     t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "     AND t1.TESTDIV = '".$model->testDiv."' ";
        $query .= "     AND EXISTS (SELECT 'x' FROM SCH_T WHERE t1.ENTEXAMYEAR = SCH_T.ENTEXAMYEAR AND t1.EXAMNO = SCH_T.EXAMNO) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     BASE.NAME AS SCH_NAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        if ($model->output == OUT_CODE_NAME) {       //コード＋名称
            $query .= "  BASE.SEX AS SEXCD, ";
            $query .= "  Z002.NAME1 AS SEX_NAME,";
        } else if ($model->output == OUT_CODE_ONLY) { //コードのみ
            $query .= "  BASE.SEX AS SEXCD, ";
        } else if ($model->output == OUT_NAME_ONLY) { //名称のみ
            $query .= "  Z002.NAME1 AS SEXCD,";
        }
        $query .= "    MAIN_T.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN V_SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN MAIN_T ON YEAR(BASE.ENT_DATE) = YEAR(MAIN_T.ENTEXAMYEAR || '-04-01') ";
        $query .= "          AND BASE.EXAMNO = MAIN_T.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "ORDER BY ";
        $query .= "  REGD.GRADE, ";
        $query .= "  REGD.HR_CLASS, ";
        $query .= "  REGD.ATTENDNO ";

        return $query;
    }

    //試験区分
    function GetTestdiv(){
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L003' ";

        return $query;
    }

    //名称マスタ
    function getMeisyou($namecd1, $namecd2) {
        $query  = "SELECT ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = '".$namecd1."' ";
        $query .= "    AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "    AND GRADE = '". $model->grade ."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND GRADE IN (SELECT ";
                    $query .= "                      REGD_G.GRADE ";
                    $query .= "                   FROM ";
                    $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                   WHERE ";
                    $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "    AND GRADE = '". $model->grade ."' ";
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND GRADE IN (SELECT ";
                    $query .= "                      REGD_G.GRADE ";
                    $query .= "                   FROM ";
                    $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                   WHERE ";
                    $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model)
    {
        $date = str_replace("/","-",$model->date);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
                           //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト右
    function getStudent_right($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }


    //リストtoリスト左
    function getStudent_left($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE = '". $model->grade ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //サブシステム名取得
    function getSubSystem() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

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
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }
}
?>