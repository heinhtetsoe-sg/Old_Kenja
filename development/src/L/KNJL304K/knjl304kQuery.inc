<?php

require_once('for_php7.php');

class knjl304kQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear,$flg=0)
    {
        $query  = "SELECT namecd2, name1, name2 FROM v_name_mst";
        $query .= " WHERE year     = '".$ObjYear."'";
        $query .= "    AND namecd1 = '".$namecd1."'";
        return $query;
    }

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

    //高校リストデータ
    function GetHsList($model)
    {
        $query  = " WITH DESIRETBL AS ( ";
        $query .= " SELECT DESIREDIV, EXAMCOURSE_MARK,WISHNO ";
        $query .= " FROM   ENTEXAM_WISHDIV_MST t1 ";
        $query .= "        LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ";
        $query .= "        AND t2.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "        AND t2.MAJORCD = t1.MAJORCD ";
        $query .= "        AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ";
        $query .= " WHERE  t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "        AND t1.TESTDIV = '".$model->testdiv."' ";
        $query .= " ),EXAM_BASE AS ( ";
        $query .= " SELECT EXAMNO,VALUE(NAME,'') AS NAME,VALUE(NAME_KANA,'') AS NAME_KANA,t1.DESIREDIV,t1.TESTDIV,VALUE(t1.FS_CD,'') AS FS_CD, ";   //NO004
        $query .= "        n1.ABBV1 || '･' || ";
        $query .= "        CASE WHEN D1.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D1.EXAMCOURSE_MARK,'') END || "; //NO004
        $query .= "        CASE WHEN D2.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D2.EXAMCOURSE_MARK,'') END || "; //NO004
        $query .= "        CASE WHEN D3.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D3.EXAMCOURSE_MARK,'') END || "; //NO004
        $query .= "        CASE WHEN D4.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D4.EXAMCOURSE_MARK,'') END AS JUDG, ";   //NO004
        $query .= "        t3.FINSCHOOL_NAME AS SCLNAME,'志願' AS DESIRENAME,t4.NAME1 AS LOCATIONNAME, ";
        $query .= "        CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX ";
        $query .= " FROM   ENTEXAM_APPLICANTBASE_DAT t1 ";
        $query .= "        LEFT JOIN DESIRETBL D1 ON t1.DESIREDIV = D1.DESIREDIV AND D1.WISHNO = '1' ";
        $query .= "        LEFT JOIN DESIRETBL D2 ON t1.DESIREDIV = D2.DESIREDIV AND D2.WISHNO = '2' ";
        $query .= "        LEFT JOIN DESIRETBL D3 ON t1.DESIREDIV = D3.DESIREDIV AND D3.WISHNO = '3' ";
        $query .= "        LEFT JOIN DESIRETBL D4 ON t1.DESIREDIV = D4.DESIREDIV AND D4.WISHNO = '4' ";
        $query .= "        LEFT JOIN FINSCHOOL_MST t3 ON t3.FINSCHOOLCD = t1.FS_CD ";
        $query .= "        LEFT JOIN NAME_MST t4 ON t4.NAMECD2 = t1.LOCATIONCD ";
        $query .= "        AND t4.NAMECD1 = 'L007' ";
        $query .= "        LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = t1.SHDIV ";
        $query .= "        AND n1.NAMECD1 = 'L006' ";
        $query .= " WHERE  ENTEXAMYEAR='".$model->ObjYear."' ";
        $query .= "        AND t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "        AND TESTDIV = '".$model->testdiv."' ";
        $query .= "        AND VALUE(APPLICANTDIV,'0') NOT IN ('2') ";  //NO005
        $query .= " ),FSDATA AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO,DATADIV,SHDIV,WISHNO, ";
        $query .= "     n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT t1 ";
        $query .= "     LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     AND n1.NAMECD1 = 'L002' ";
        $query .= "     LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ";
        $query .= "     AND n2.NAMECD1 = 'L006' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ";
        $query .= "     AND t2.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t2.MAJORCD = t1.MAJORCD ";
        $query .= "     AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.DATADIV = '1' ";
        $query .= " ),PSDATA AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO,DATADIV,SHDIV,WISHNO, ";
        $query .= "     n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT t1 ";
        $query .= "     LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     AND n1.NAMECD1 = 'L002' ";
        $query .= "     LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ";
        $query .= "     AND n2.NAMECD1 = 'L006' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ";
        $query .= "     AND t2.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t2.MAJORCD = t1.MAJORCD ";
        $query .= "     AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.DATADIV = '2' ";
        $query .= " ),EXAM_CON AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.ACCEPTNO,t1.FS_CD,t1.TESTDIV, ";
        $query .= "     t2.FINSCHOOL_NAME AS SCLNAME, ";
        $query .= "     t1.CREATE_DATE,'事前' AS DESIRENAME, ";
        $query .= "     t1.NAME,t1.NAME_KANA,CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ";
        $query .= "     CASE WHEN f1.ACCEPTNO IS NOT NULL THEN ";
        $query .= "     CASE WHEN f1.MAJOR IS NULL THEN '' ELSE VALUE(f1.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN f2.MAJOR IS NULL THEN '' ELSE VALUE(f2.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN f3.MAJOR IS NULL THEN '' ELSE VALUE(f3.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN f4.MAJOR IS NULL THEN '' ELSE VALUE(f4.MAJOR,'') END ";       //NO004
        $query .= "     ELSE ";
        $query .= "     CASE WHEN p1.MAJOR IS NULL THEN '' ELSE VALUE(p1.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN p2.MAJOR IS NULL THEN '' ELSE VALUE(p2.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN p3.MAJOR IS NULL THEN '' ELSE VALUE(p3.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN p4.MAJOR IS NULL THEN '' ELSE VALUE(p4.MAJOR,'') END ";       //NO004
        $query .= "     END MAJOR, ";
        $query .= "     CASE WHEN f1.ACCEPTNO IS NOT NULL THEN ";
        $query .= "     CASE WHEN f1.MAJOR IS NULL THEN ' ' ELSE  VALUE(f1.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f1.SHNAME IS NULL THEN ' ' ELSE VALUE(f1.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f1.JUDG IS NULL THEN ' ･' ELSE  VALUE(f1.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN f2.MAJOR IS NULL THEN ' ' ELSE  VALUE(f2.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f2.SHNAME IS NULL THEN ' ' ELSE VALUE(f2.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f2.JUDG IS NULL THEN ' ･' ELSE  VALUE(f2.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN f3.MAJOR IS NULL THEN ' ' ELSE  VALUE(f3.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f3.SHNAME IS NULL THEN ' ' ELSE VALUE(f3.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f3.JUDG IS NULL THEN ' ･' ELSE  VALUE(f3.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN f4.MAJOR IS NULL THEN ' ' ELSE  VALUE(f4.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f4.SHNAME IS NULL THEN ' ' ELSE VALUE(f4.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f4.JUDG IS NULL THEN ' ' ELSE   VALUE(f4.JUDG,'') END ";              //NO004
        $query .= "     ELSE ";
        $query .= "     CASE WHEN p1.MAJOR IS NULL THEN ' ' ELSE  VALUE(p1.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p1.SHNAME IS NULL THEN ' ' ELSE VALUE(p1.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p1.JUDG IS NULL THEN ' ･' ELSE  VALUE(p1.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN p2.MAJOR IS NULL THEN ' ' ELSE  VALUE(p2.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p2.SHNAME IS NULL THEN ' ' ELSE VALUE(p2.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p2.JUDG IS NULL THEN ' ･' ELSE  VALUE(p2.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN p3.MAJOR IS NULL THEN ' ' ELSE  VALUE(p3.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p3.SHNAME IS NULL THEN ' ' ELSE VALUE(p3.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p3.JUDG IS NULL THEN ' ･' ELSE  VALUE(p3.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN p4.MAJOR IS NULL THEN ' ' ELSE  VALUE(p4.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p4.SHNAME IS NULL THEN ' ' ELSE VALUE(p4.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p4.JUDG IS NULL THEN ' ' ELSE   VALUE(p4.JUDG,'') END ";              //NO004
        $query .= "     END JUDG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT t1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST t2 ON t2.FINSCHOOLCD = t1.FS_CD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST t5 ON t5.PRISCHOOLCD = t1.PS_CD ";
        $query .= "     LEFT JOIN FSDATA f1 ON f1.ACCEPTNO = t1.ACCEPTNO AND f1.WISHNO = '1' "; //NO007
        $query .= "     LEFT JOIN FSDATA f2 ON f2.ACCEPTNO = t1.ACCEPTNO AND f2.WISHNO = '2' "; //NO007
        $query .= "     LEFT JOIN FSDATA f3 ON f3.ACCEPTNO = t1.ACCEPTNO AND f3.WISHNO = '3' "; //NO007
        $query .= "     LEFT JOIN FSDATA f4 ON f4.ACCEPTNO = t1.ACCEPTNO AND f4.WISHNO = '4' "; //NO007
        $query .= "     LEFT JOIN PSDATA p1 ON p1.ACCEPTNO = t1.ACCEPTNO AND p1.WISHNO = '1' "; //NO007
        $query .= "     LEFT JOIN PSDATA p2 ON p2.ACCEPTNO = t1.ACCEPTNO AND p2.WISHNO = '2' "; //NO007
        $query .= "     LEFT JOIN PSDATA p3 ON p3.ACCEPTNO = t1.ACCEPTNO AND p3.WISHNO = '3' "; //NO007
        $query .= "     LEFT JOIN PSDATA p4 ON p4.ACCEPTNO = t1.ACCEPTNO AND p4.WISHNO = '4' "; //NO007
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     ) ";
        $query .= " SELECT T1.EXAMNO,VALUE(T1.NAME,'') AS NAME,VALUE(T1.NAME_KANA,'') AS NAME_KANA,T1.SEX,T2.DESIRENAME, "; //NO004
        $query .= "        T2.ACCEPTNO,'CC' AS TEXAM,VALUE(T2.NAME,'') AS NAME2,VALUE(T2.NAME_KANA,'') AS KANA2, "; //NO004
        $query .= "        T2.SEX,T2.SCLNAME,T2.JUDG,VALUE(T2.FS_CD,'') AS FS_CD,'' AS LOCATIONNAME, "; //NO004
        $query .= "        CASE WHEN VALUE(T1.NAME,'') = VALUE(T2.NAME,'') THEN 'K' ELSE 'N' END AS NORKDIV ";  //NO004
        $query .= " FROM   EXAM_BASE T1,EXAM_CON T2 ";
//NO001↓
        if ($model->center_title == "0"){   //NO002
            $query .= " WHERE  VALUE(T1.NAME,'') = VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') <> VALUE(T2.NAME_KANA,'') AND VALUE(T1.FS_CD,'') = VALUE(T2.FS_CD,'') ";    //NO004
        }else if ($model->center_title == "1"){ //NO002
            $query .= " WHERE  VALUE(T1.NAME,'') <> VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') = VALUE(T2.NAME_KANA,'') AND VALUE(T1.FS_CD,'') = VALUE(T2.FS_CD,'') ";    //NO004
        }else if ($model->center_title == "2"){ //NO002
            $query .= " WHERE  VALUE(T1.NAME,'') = VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') = VALUE(T2.NAME_KANA,'') AND VALUE(T1.FS_CD,'') <> VALUE(T2.FS_CD,'') ";    //NO004
        }else if ($model->center_title == "3"){ //NO002
            $query .= " WHERE  VALUE(T1.NAME,'') = VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') <> VALUE(T2.NAME_KANA,'') AND VALUE(T1.FS_CD,'') <> VALUE(T2.FS_CD,'') ";   //NO004
        }else if ($model->center_title == "4"){ //NO002
            $query .= " WHERE  VALUE(T1.NAME,'') <> VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') = VALUE(T2.NAME_KANA,'') AND VALUE(T1.FS_CD,'') <> VALUE(T2.FS_CD,'') ";   //NO004
        }else {
            $query .= " WHERE  VALUE(T1.NAME,'') <> VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') <> VALUE(T2.NAME_KANA,'') AND VALUE(T1.FS_CD,'') = VALUE(T2.FS_CD,'') ";   //NO004
        }
//NO001↑
        $query .= " ORDER BY NAME_KANA,EXAMNO,ACCEPTNO DESC ";

        return $query;
    }


    //中学リストデータ NO003
    function GetJsList($model)
    {
        $query  = " WITH NOSERCH AS (SELECT ";
        $query .= "     t1.EXAMNO AS NOEXAM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT t1, ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT t2 ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."'     AND ";
        $query .= "     t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     t1.TESTDIV = '".$model->testdiv."'     AND ";
        $query .= "     VALUE(t1.APPLICANTDIV,'0') NOT IN ('2') AND ";  //NO005
        $query .= "     t2.ENTEXAMYEAR = '".$model->ObjYear."'     AND ";
        $query .= "     t2.TESTDIV = '".$model->testdiv."'     AND ";
        $query .= "     VALUE(t1.NAME,'') || VALUE(t1.NAME_KANA,'') = VALUE(t2.NAME,'') || VALUE(t2.NAME_KANA,'') ";    //NO004
        $query .= " ), DESIRETBL AS ( ";
        $query .= " SELECT DESIREDIV, EXAMCOURSE_MARK,WISHNO ";
        $query .= " FROM   ENTEXAM_WISHDIV_MST t1 ";
        $query .= "        LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ";
        $query .= "        AND t2.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "        AND t2.MAJORCD = t1.MAJORCD ";
        $query .= "        AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ";
        $query .= " WHERE  t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "        AND t1.TESTDIV = '".$model->testdiv."' ";
        $query .= " ),EXAM_BASE AS ( ";
        $query .= " SELECT EXAMNO,VALUE(NAME,'') AS NAME,VALUE(NAME_KANA,'') AS NAME_KANA,t1.DESIREDIV,t1.TESTDIV,VALUE(t1.FS_CD,'') AS FS_CD, ";   //NO004
        $query .= "        n1.ABBV1 || '･' || ";
        $query .= "        CASE WHEN D1.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D1.EXAMCOURSE_MARK,'') END || ";         //NO004
        $query .= "        CASE WHEN D2.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D2.EXAMCOURSE_MARK,'') END || ";         //NO004
        $query .= "        CASE WHEN D3.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D3.EXAMCOURSE_MARK,'') END || ";         //NO004
        $query .= "        CASE WHEN D4.EXAMCOURSE_MARK IS NULL THEN '' ELSE VALUE(D4.EXAMCOURSE_MARK,'') END AS JUDG, ";   //NO004
        $query .= "        t3.FINSCHOOL_NAME AS SCLNAME,'志願' AS DESIRENAME,t4.NAME1 AS LOCATIONNAME, ";
        $query .= "        CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX ";
        $query .= " FROM   ENTEXAM_APPLICANTBASE_DAT t1 ";
        $query .= "        LEFT JOIN DESIRETBL D1 ON t1.DESIREDIV = D1.DESIREDIV AND D1.WISHNO = '1' ";
        $query .= "        LEFT JOIN DESIRETBL D2 ON t1.DESIREDIV = D2.DESIREDIV AND D2.WISHNO = '2' ";
        $query .= "        LEFT JOIN DESIRETBL D3 ON t1.DESIREDIV = D3.DESIREDIV AND D3.WISHNO = '3' ";
        $query .= "        LEFT JOIN DESIRETBL D4 ON t1.DESIREDIV = D4.DESIREDIV AND D4.WISHNO = '4' ";
        $query .= "        LEFT JOIN FINSCHOOL_MST t3 ON t3.FINSCHOOLCD = t1.FS_CD ";
        $query .= "        LEFT JOIN NAME_MST t4 ON t4.NAMECD2 = t1.LOCATIONCD ";
        $query .= "        AND t4.NAMECD1 = 'L007' ";
        $query .= "        LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = t1.SHDIV ";
        $query .= "        AND n1.NAMECD1 = 'L006' ";
        $query .= " WHERE  ENTEXAMYEAR='".$model->ObjYear."' ";
        $query .= "        AND t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "        AND TESTDIV = '".$model->testdiv."' ";
        $query .= "        AND VALUE(APPLICANTDIV,'0') NOT IN ('2') ";  //NO005
        if ($model->center_title == "2"){
            $query .= "        AND EXAMNO NOT IN (SELECT ";
            $query .= "                               NOEXAM ";
            $query .= "                           FROM ";
            $query .= "                               NOSERCH) ";
        }
        $query .= " ),FSDATA AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO,DATADIV,SHDIV,WISHNO, ";
        $query .= "     n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT t1 ";
        $query .= "     LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     AND n1.NAMECD1 = 'L002' ";
        $query .= "     LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ";
        $query .= "     AND n2.NAMECD1 = 'L006' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ";
        $query .= "     AND t2.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t2.MAJORCD = t1.MAJORCD ";
        $query .= "     AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.DATADIV = '1' ";
        $query .= " ),PSDATA AS ( ";
        $query .= " SELECT ";
        $query .= "     ACCEPTNO,DATADIV,SHDIV,WISHNO, ";
        $query .= "     n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT t1 ";
        $query .= "     LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ";
        $query .= "     AND n1.NAMECD1 = 'L002' ";
        $query .= "     LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ";
        $query .= "     AND n2.NAMECD1 = 'L006' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ";
        $query .= "     AND t2.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t2.MAJORCD = t1.MAJORCD ";
        $query .= "     AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND t1.DATADIV = '2' ";
        $query .= " ),EXAM_CON AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.ACCEPTNO,t1.FS_CD,t1.TESTDIV, ";
        $query .= "     t2.FINSCHOOL_NAME AS SCLNAME, ";
        $query .= "     t1.CREATE_DATE,'事前' AS DESIRENAME, ";
        $query .= "     t1.NAME,t1.NAME_KANA,CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ";
        $query .= "     CASE WHEN f1.ACCEPTNO IS NOT NULL THEN ";
        $query .= "     CASE WHEN f1.MAJOR IS NULL THEN '' ELSE VALUE(f1.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN f2.MAJOR IS NULL THEN '' ELSE VALUE(f2.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN f3.MAJOR IS NULL THEN '' ELSE VALUE(f3.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN f4.MAJOR IS NULL THEN '' ELSE VALUE(f4.MAJOR,'') END ";       //NO004
        $query .= "     ELSE ";
        $query .= "     CASE WHEN p1.MAJOR IS NULL THEN '' ELSE VALUE(p1.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN p2.MAJOR IS NULL THEN '' ELSE VALUE(p2.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN p3.MAJOR IS NULL THEN '' ELSE VALUE(p3.MAJOR,'') END || ";    //NO004
        $query .= "     CASE WHEN p4.MAJOR IS NULL THEN '' ELSE VALUE(p4.MAJOR,'') END ";       //NO004
        $query .= "     END MAJOR, ";
        $query .= "     CASE WHEN f1.ACCEPTNO IS NOT NULL THEN ";
        $query .= "     CASE WHEN f1.MAJOR IS NULL THEN ' ' ELSE  VALUE(f1.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f1.SHNAME IS NULL THEN ' ' ELSE VALUE(f1.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f1.JUDG IS NULL THEN ' ･' ELSE  VALUE(f1.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN f2.MAJOR IS NULL THEN ' ' ELSE  VALUE(f2.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f2.SHNAME IS NULL THEN ' ' ELSE VALUE(f2.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f2.JUDG IS NULL THEN ' ･' ELSE  VALUE(f2.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN f3.MAJOR IS NULL THEN ' ' ELSE  VALUE(f3.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f3.SHNAME IS NULL THEN ' ' ELSE VALUE(f3.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f3.JUDG IS NULL THEN ' ･' ELSE  VALUE(f3.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN f4.MAJOR IS NULL THEN ' ' ELSE  VALUE(f4.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN f4.SHNAME IS NULL THEN ' ' ELSE VALUE(f4.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN f4.JUDG IS NULL THEN ' ' ELSE   VALUE(f4.JUDG,'') END ";              //NO004
        $query .= "     ELSE ";
        $query .= "     CASE WHEN p1.MAJOR IS NULL THEN ' ' ELSE  VALUE(p1.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p1.SHNAME IS NULL THEN ' ' ELSE VALUE(p1.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p1.JUDG IS NULL THEN ' ･' ELSE  VALUE(p1.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN p2.MAJOR IS NULL THEN ' ' ELSE  VALUE(p2.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p2.SHNAME IS NULL THEN ' ' ELSE VALUE(p2.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p2.JUDG IS NULL THEN ' ･' ELSE  VALUE(p2.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN p3.MAJOR IS NULL THEN ' ' ELSE  VALUE(p3.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p3.SHNAME IS NULL THEN ' ' ELSE VALUE(p3.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p3.JUDG IS NULL THEN ' ･' ELSE  VALUE(p3.JUDG,'') || '･' END || ";    //NO004
        $query .= "     CASE WHEN p4.MAJOR IS NULL THEN ' ' ELSE  VALUE(p4.MAJOR,'') END || ";          //NO004
        $query .= "     CASE WHEN p4.SHNAME IS NULL THEN ' ' ELSE VALUE(p4.SHNAME,'') END || ";         //NO004
        $query .= "     CASE WHEN p4.JUDG IS NULL THEN ' ' ELSE   VALUE(p4.JUDG,'') END ";              //NO004
        $query .= "     END JUDG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT t1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST t2 ON t2.FINSCHOOLCD = t1.FS_CD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST t5 ON t5.PRISCHOOLCD = t1.PS_CD ";
        $query .= "     LEFT JOIN FSDATA f1 ON f1.ACCEPTNO = t1.ACCEPTNO AND f1.WISHNO = '1' "; //NO007
        $query .= "     LEFT JOIN FSDATA f2 ON f2.ACCEPTNO = t1.ACCEPTNO AND f2.WISHNO = '2' "; //NO007
        $query .= "     LEFT JOIN FSDATA f3 ON f3.ACCEPTNO = t1.ACCEPTNO AND f3.WISHNO = '3' "; //NO007
        $query .= "     LEFT JOIN FSDATA f4 ON f4.ACCEPTNO = t1.ACCEPTNO AND f4.WISHNO = '4' "; //NO007
        $query .= "     LEFT JOIN PSDATA p1 ON p1.ACCEPTNO = t1.ACCEPTNO AND p1.WISHNO = '1' "; //NO007
        $query .= "     LEFT JOIN PSDATA p2 ON p2.ACCEPTNO = t1.ACCEPTNO AND p2.WISHNO = '2' "; //NO007
        $query .= "     LEFT JOIN PSDATA p3 ON p3.ACCEPTNO = t1.ACCEPTNO AND p3.WISHNO = '3' "; //NO007
        $query .= "     LEFT JOIN PSDATA p4 ON p4.ACCEPTNO = t1.ACCEPTNO AND p4.WISHNO = '4' "; //NO007
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     ) ";
        $query .= " SELECT T1.EXAMNO,VALUE(T1.NAME,'') AS NAME,VALUE(T1.NAME_KANA,'') AS NAME_KANA,T1.SEX,T2.DESIRENAME, "; //NO004
        $query .= "        T1.TESTDIV AS TESTDIV1,T2.TESTDIV AS TESTDIV2, ";    //NO003
        $query .= "        T2.ACCEPTNO,'CC' AS TEXAM,VALUE(T2.NAME,'') AS NAME2,VALUE(T2.NAME_KANA,'') AS KANA2, ";         //NO004
        $query .= "        T2.SEX,T2.SCLNAME,T2.JUDG,VALUE(T2.FS_CD,'') AS FS_CD,'' AS LOCATIONNAME, ";                     //NO004
        $query .= "        CASE WHEN VALUE(T1.NAME,'') = VALUE(T2.NAME,'') THEN 'K' ELSE 'N' END AS NORKDIV ";              //NO004
        $query .= " FROM   EXAM_BASE T1,EXAM_CON T2 ";

        if ($model->center_title == "0"){
            $query .= " WHERE  VALUE(T1.NAME,'') = VALUE(T2.NAME,'') ";
        }else if ($model->center_title == "1"){
            $query .= " WHERE (VALUE(T1.NAME,'') = VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') <> VALUE(T2.NAME_KANA,'')) OR ";    //NO004
            $query .= "       (VALUE(T1.NAME,'') <> VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') = VALUE(T2.NAME_KANA,''))  ";      //NO004
        }else if ($model->center_title == "2"){
            $query .= " WHERE  VALUE(T1.NAME,'') = VALUE(T2.NAME,'') AND VALUE(T1.NAME_KANA,'') = VALUE(T2.NAME_KANA,'') AND T1.TESTDIV <> T2.TESTDIV ";    //NO004
        }else {
            //処理なし
        }

        $query .= " ORDER BY NAME_KANA,EXAMNO,ACCEPTNO DESC ";

        return $query;
    }

    //左リスト
    function GetLeftList($model)
    {
        //手続者
        if ($model->appli_type == "1") {
            $query  = "SELECT T2.examno";
            $query .= "      ,T2.name ";
            $query .= "  FROM entexam_applicantbase_dat T2 ";
            $query .= " WHERE T2.entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND T2.testdiv      = '".$model->testdiv."'";
            $query .= "   AND T2.shdiv        = '".$model->shdiv."'";
            $query .= "   AND T2.judgement   IN ('1','2','3','4','5','6','9') ";    //合格、繰上合格、特別合格
            $query .= "   AND T2.procedurediv = '2'";                           //手続き済み
            $query .= "   AND T2.entdiv       = '2' ";                        //入学あり
            $query .= " ORDER BY T2.examno";

        //辞退者
        } elseif ($model->appli_type == "2") {
            $query  = "SELECT T2.examno";
            $query .= "      ,T2.name ";
            $query .= "  FROM entexam_applicantbase_dat T2 ";
            $query .= " WHERE T2.entexamyear    = '".$model->ObjYear."'";
            $query .= "   AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND T2.testdiv        = '".$model->testdiv."'";
            $query .= "   AND T2.shdiv          = '".$model->shdiv."'";
            $query .= "   AND T2.judgement      IN ('1','2','3','4','5','6','9') "; //合格、繰上合格、特別合格
            $query .= "   AND T2.procedurediv   = '2' ";                        //手続き済み
            $query .= "   AND T2.entdiv         = '1' ";                        //入学無しまたは未設定
            $query .= " ORDER BY T2.examno";
        }
        return $query;
    }

    //右リスト
    function GetRightList($model)
    {
        //合格者
        if ($model->appli_type == "1") {
            $query  = "SELECT T2.examno";
            $query .= "      ,T2.name ";
            $query .= "  FROM entexam_applicantbase_dat T2 ";
            $query .= " WHERE T2.entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND T2.testdiv      = '".$model->testdiv."'";
            $query .= "   AND T2.shdiv        = '".$model->shdiv."'";
            $query .= "   AND T2.judgement   IN ('1','2','3','4','5','6','9') ";    //合格、繰上合格、特別合格
            $query .= "   AND COALESCE(T2.procedurediv,'0') <> '2' ";           //手続き未または未設定
            $query .= " ORDER BY T2.examno";

        //手続者
        } elseif ($model->appli_type == "2") {
        
            $query  = "SELECT T2.examno";
            $query .= "      ,T2.name ";
            $query .= "  FROM entexam_applicantbase_dat T2 ";
            $query .= " WHERE T2.entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND T2.testdiv      = '".$model->testdiv."'";
            $query .= "   AND T2.shdiv        = '".$model->shdiv."'";
            $query .= "   AND T2.judgement   IN ('1','2','3','4','5','6','9') ";    //合格、繰上合格、特別合格
            $query .= "   AND T2.procedurediv = '2' ";                          //手続き済み
            $query .= "   AND COALESCE(T2.entdiv,'0') <> '1' ";                 //入学無しまたは未設定
            $query .= " ORDER BY T2.examno";
        }
        return $query;
    }
    //UPDATE $upchange:1=base,2:=accept
    //NO003
    function &getUpdateQueryHs($model,$exam,$accept,$upchange)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($upchange == 1){
            $query  = "UPDATE ";
            $query .= "    ENTEXAM_CONSULTATION_HDAT ";
            $query .= "SET ";
            $query .= "    NAME = (SELECT ";
            $query .= "                NAME ";
            $query .= "            FROM ";
            $query .= "                ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "            WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "                AND TESTDIV = '".$model->testdiv."' ";
            $query .= "                AND EXAMNO = '".$exam."'), ";
            $query .= "    NAME_KANA = (SELECT ";
            $query .= "                    NAME_KANA ";
            $query .= "                 FROM ";
            $query .= "                    ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "                 WHERE ";
            $query .= "                    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                    AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "                    AND TESTDIV = '".$model->testdiv."' ";
            $query .= "                    AND EXAMNO = '".$exam."'), ";
            $query .= "    FS_CD = (SELECT ";
            $query .= "                FS_CD ";
            $query .= "             FROM ";
            $query .= "                ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "             WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "                AND TESTDIV = '".$model->testdiv."' ";
            $query .= "                AND EXAMNO = '".$exam."') ";
            $query .= "WHERE ";
            $query .= "    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "    AND TESTDIV = '".$model->testdiv."' ";
            $query .= "    AND ACCEPTNO = '".$accept."' ";

            $db->query($query);
        }else {
            $query  = "UPDATE ";
            $query .= "    ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "SET ";
            $query .= "    NAME = (SELECT ";
            $query .= "                NAME ";
            $query .= "            FROM ";
            $query .= "                ENTEXAM_CONSULTATION_HDAT ";
            $query .= "            WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND TESTDIV = '".$model->testdiv."' ";
            $query .= "                AND ACCEPTNO = '".$accept."'), ";
            $query .= "    NAME_KANA = (SELECT ";
            $query .= "                    NAME_KANA ";
            $query .= "                 FROM ";
            $query .= "                    ENTEXAM_CONSULTATION_HDAT ";
            $query .= "                 WHERE ";
            $query .= "                    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                    AND TESTDIV = '".$model->testdiv."' ";
            $query .= "                    AND ACCEPTNO = '".$accept."'), ";
            $query .= "    FS_CD = (SELECT ";
            $query .= "                FS_CD ";
            $query .= "             FROM ";
            $query .= "                ENTEXAM_CONSULTATION_HDAT ";
            $query .= "             WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND TESTDIV = '".$model->testdiv."' ";
            $query .= "                AND ACCEPTNO = '".$accept."') ";
            $query .= "WHERE ";
            $query .= "    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "    AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "    AND TESTDIV = '".$model->testdiv."' ";
            $query .= "    AND EXAMNO = '".$exam."' ";

            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //UPDATE $upchange:1=base,2:=accept
    //NO003
    function &getUpdateQueryJs($model,$exam,$accept,$testdiv1,$testdiv2,$upchange)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($upchange == 1){
            $query  = "UPDATE ";
            $query .= "    ENTEXAM_CONSULTATION_HDAT ";
            $query .= "SET ";
            $query .= "    NAME = (SELECT ";
            $query .= "                NAME ";
            $query .= "            FROM ";
            $query .= "                ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "            WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "                AND TESTDIV = '".$testdiv1."' ";
            $query .= "                AND EXAMNO = '".$exam."'), ";
            $query .= "    NAME_KANA = (SELECT ";
            $query .= "                    NAME_KANA ";
            $query .= "                 FROM ";
            $query .= "                    ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "                 WHERE ";
            $query .= "                    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                    AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "                    AND TESTDIV = '".$testdiv1."' ";
            $query .= "                    AND EXAMNO = '".$exam."'), ";
            $query .= "    TESTDIV = (SELECT ";
            $query .= "                TESTDIV ";
            $query .= "             FROM ";
            $query .= "                ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "             WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "                AND TESTDIV = '".$testdiv1."' ";
            $query .= "                AND EXAMNO = '".$exam."') ";
            $query .= "WHERE ";
            $query .= "    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "    AND TESTDIV = '".$testdiv2."' ";
            $query .= "    AND ACCEPTNO = '".$accept."' ";

            $db->query($query);
        }else {
            $query  = "UPDATE ";
            $query .= "    ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "SET ";
            $query .= "    NAME = (SELECT ";
            $query .= "                NAME ";
            $query .= "            FROM ";
            $query .= "                ENTEXAM_CONSULTATION_HDAT ";
            $query .= "            WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND TESTDIV = '".$testdiv2."' ";
            $query .= "                AND ACCEPTNO = '".$accept."'), ";
            $query .= "    NAME_KANA = (SELECT ";
            $query .= "                    NAME_KANA ";
            $query .= "                 FROM ";
            $query .= "                    ENTEXAM_CONSULTATION_HDAT ";
            $query .= "                 WHERE ";
            $query .= "                    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                    AND TESTDIV = '".$testdiv2."' ";
            $query .= "                    AND ACCEPTNO = '".$accept."'), ";
            $query .= "    TESTDIV = (SELECT ";
            $query .= "                TESTDIV ";
            $query .= "             FROM ";
            $query .= "                ENTEXAM_CONSULTATION_HDAT ";
            $query .= "             WHERE ";
            $query .= "                ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "                AND TESTDIV = '".$testdiv2."' ";
            $query .= "                AND ACCEPTNO = '".$accept."') ";
            $query .= "WHERE ";
            $query .= "    ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "    AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "    AND TESTDIV = '".$testdiv1."' ";
            $query .= "    AND EXAMNO = '".$exam."' ";

            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>
