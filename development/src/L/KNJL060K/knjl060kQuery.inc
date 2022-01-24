<?php

require_once('for_php7.php');
class knjl060kQuery extends Query {

    //名称
    function GetName($year, $namecd1)
    {
        $query = "SELECT namecd2, name1 FROM v_name_mst
                   WHERE year    = '".$year."'
                     AND namecd1 = '".$namecd1."'";
        return $query;
    }

    //中学・高校判定---2005.08.13---alp
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

    //確定処理実行
    function exec(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $jhflg = $db->getOne(knjl060kQuery::GetJorH());

        //データを削除
        $query  = " DELETE FROM entexam_applicantcons_dat t1 ";
        $query .= "  WHERE t1.entexamyear   = '".$model->year."' AND ";
        $query .= "        t1.testdiv       = '".$model->testdiv ."' AND ";
        $query .= "        EXISTS (SELECT 'x' FROM entexam_applicantbase_dat w1 ";
        $query .= "                 WHERE w1.entexamyear    = t1.entexamyear AND ";
        $query .= "                       w1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "                       w1.testdiv        = t1.testdiv AND ";
        $query .= "                       w1.examno         = t1.examno ";
        $query .= "               ) ";
        $db->query($query);


        $query  = " INSERT INTO entexam_applicantcons_dat ";
        $query .= "    ( ";
        $query .= "        entexamyear,  ";
        $query .= "        testdiv, ";
        $query .= "        examno, ";
        $query .= "        shdiv, ";
        $query .= "        coursecd, ";
        $query .= "        majorcd, ";
        $query .= "        examcoursecd, ";
        $query .= "        judgement, ";
        $query .= "        registercd ";
        $query .= "    ) ";

        $query .= "    WITH T01 AS( ";
        $query .= "            SELECT ";
        $query .= "                t1.entexamyear    AS entexamyear, ";
        $query .= "                t1.testdiv        AS testdiv, ";
        $query .= "                t3.examno         AS examno, ";
        $query .= "                t2.shdiv          AS shdiv, ";
        $query .= "                t2.coursecd       AS coursecd, ";
        $query .= "                t2.majorcd        AS majorcd, ";
        $query .= "                t2.examcoursecd   AS examcoursecd, ";
        $query .= "                MIN(t2.judgement) AS judgement ";
        $query .= "            FROM ";
        $query .= "                entexam_consultation_hdat t1, ";
        $query .= "                entexam_consultation_dat  t2, ";
        $query .= "                entexam_applicantbase_dat t3 ";
        $query .= "            WHERE ";
        $query .= "                t1.entexamyear              = '".$model->year."' AND ";
        $query .= "                t1.testdiv                  = '".$model->testdiv ."' AND ";
        $query .= "                t2.entexamyear              = t1.entexamyear AND ";
        $query .= "                t2.testdiv                  = t1.testdiv AND ";
        $query .= "                t2.acceptno                 = t1.acceptno AND ";
        $query .= "                t2.shdiv                    IS NOT NULL AND ";
        $query .= "                t2.coursecd                 IS NOT NULL AND ";
        $query .= "                t2.majorcd                  IS NOT NULL AND ";
        $query .= "                t2.examcoursecd             IS NOT NULL AND ";
        $query .= "                t3.entexamyear              = t1.entexamyear AND "; 
        $query .= "                t3.testdiv                  = t1.testdiv AND ";
        $query .= "                t3.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        //2005.11.09 alp m-yama
        if ($jhflg == 0) 
            $query .= "                t3.fs_cd                    = t1.fs_cd AND ";
        $query .= "                LTRIM(RTRIM(t3.name))       = LTRIM(RTRIM(t1.name)) AND ";
        $query .= "                LTRIM(RTRIM(t3.name_kana))  = LTRIM(RTRIM(t1.name_kana)) ";
        $query .= "            GROUP BY ";
        $query .= "                t1.entexamyear, ";
        $query .= "                t1.testdiv, ";
        $query .= "                t3.examno, ";
        $query .= "                t2.shdiv, ";
        $query .= "                t2.coursecd, ";
        $query .= "                t2.examcoursecd, ";
        $query .= "                t2.majorcd ";
        $query .= "    ) ";
        $query .= "    , T02 AS( ";            //補完処理　専願
        $query .= "            SELECT DISTINCT ";
        $query .= "                t1.entexamyear      AS entexamyear, ";
        $query .= "                t1.testdiv          AS testdiv, ";
        $query .= "                t1.examno           AS examno, ";
        $query .= "                t1.shdiv            AS shdiv, ";
        $query .= "                t1.cmp_coursecd     AS coursecd, ";
        $query .= "                t1.cmp_majorcd      AS majorcd, ";
        $query .= "                t1.cmp_examcoursecd AS examcoursecd, ";
        $query .= "                t1.s_judgement      AS s_judgement, ";       # 2005/11/15 arakaki
        $query .= "                t1.h_judgement      AS h_judgement ";        # 2005/11/15 arakaki
        $query .= "            FROM ";
        $query .= "                (SELECT ";
        $query .= "                    st1.entexamyear, ";
        $query .= "                    st2.testdiv, ";
        $query .= "                    st2.examno, ";
        $query .= "                    st2.shdiv, ";
        $query .= "                    st1.cmp_coursecd, ";
        $query .= "                    st1.cmp_majorcd, ";
        $query .= "                    st1.cmp_examcoursecd, ";
        $query .= "                    st1.s_judgement, ";                      # 2005/11/15 arakaki
        $query .= "                    st1.h_judgement ";                       # 2005/11/15 arakaki
        $query .= "                FROM ";
        $query .= "                    entexam_judgecomp_mst    st1, ";
        $query .= "                    T01                     st2 ";
        $query .= "                WHERE ";
#        $query .= "                    st2.entexamyear     = '".$model->year."' AND ";
#        $query .= "                    st2.testdiv         = '".$model->testdiv ."' AND ";
        $query .= "                    st2.shdiv           = '1' AND ";
        $query .= "                    st1.entexamyear     = st2.entexamyear AND ";
        $query .= "                    st1.shdiv           = st2.shdiv AND ";
        $query .= "                    st1.coursecd        = st2.coursecd AND ";
        $query .= "                    st1.majorcd         = st2.majorcd AND ";
        $query .= "                    st1.examcoursecd    = st2.examcoursecd AND ";
        $query .= "                    st1.judgement       = st2.judgement ";
        $query .= "                ) t1 ";
        $query .= "    ) ";
        $query .= "    , T03 AS( ";        //補完処理　併願
        $query .= "            SELECT DISTINCT ";
        $query .= "                t1.entexamyear      AS entexamyear, ";
        $query .= "                t1.testdiv          AS testdiv, ";
        $query .= "                t1.examno           AS examno, ";
        $query .= "                t1.shdiv            AS shdiv, ";
        $query .= "                t1.cmp_coursecd     AS coursecd, ";
        $query .= "                t1.cmp_majorcd      AS majorcd, ";
        $query .= "                t1.cmp_examcoursecd AS examcoursecd, ";
        $query .= "                t1.s_judgement      AS s_judgement, ";       # 2005/11/15 arakaki
        $query .= "                t1.h_judgement      AS h_judgement ";        # 2005/11/15 arakaki
        $query .= "            FROM ";
        $query .= "                (SELECT ";
        $query .= "                    st1.entexamyear, ";
        $query .= "                    st2.testdiv, ";
        $query .= "                    st2.examno, ";
        $query .= "                    st2.shdiv, ";
        $query .= "                    st1.cmp_coursecd, ";
        $query .= "                    st1.cmp_majorcd, ";
        $query .= "                    st1.cmp_examcoursecd, ";
        $query .= "                    st1.s_judgement, ";                      # 2005/11/15 arakaki
        $query .= "                    st1.h_judgement ";                       # 2005/11/15 arakaki
        $query .= "                FROM ";
        $query .= "                    entexam_judgecomp_mst     st1, ";
        $query .= "                    T01                     st2 ";
        $query .= "                WHERE ";
#        $query .= "                    st2.entexamyear     = '".$model->year."' AND ";
#        $query .= "                    st2.testdiv         = '".$model->testdiv ."' AND ";
        $query .= "                    st2.shdiv            = '2' AND ";
        $query .= "                    st1.entexamyear     = st2.entexamyear AND ";
        $query .= "                    st1.shdiv           = st2.shdiv AND ";
        $query .= "                    st1.coursecd        = st2.coursecd AND ";
        $query .= "                    st1.majorcd         = st2.majorcd AND ";
        $query .= "                    st1.examcoursecd    = st2.examcoursecd AND ";
        $query .= "                    st1.judgement       = st2.judgement ";
        $query .= "                ) t1 ";
        $query .= "    ) "; 

        $query .= "    SELECT ";    //事前相談データ、補完専願データ、補完併願データよりグループ化し同コースの場合は良い判定をセット。
        $query .= "        entexamyear, ";
        $query .= "        testdiv, ";
        $query .= "        examno, ";
        $query .= "        shdiv, ";
        $query .= "        coursecd, ";
        $query .= "        majorcd, ";
        $query .= "        examcoursecd, ";
        $query .= "        MIN(judgement) as judgement, ";
        $query .= "        '". STAFFCD ."' ";
        $query .= "    FROM ";
        $query .= "    ( ";
        $query .= "        SELECT * FROM T01 ";
        $query .= "        UNION ";
        $query .= "        SELECT entexamyear,testdiv,examno,'1' AS SHDIV,coursecd,majorcd,examcoursecd,MIN(s_judgement) AS JUDGEMENT FROM T02 GROUP BY entexamyear,testdiv,examno,coursecd,majorcd,examcoursecd";   # 2005/11/15 arakaki
        $query .= "        UNION ";
        $query .= "        SELECT entexamyear,testdiv,examno,'2' AS SHDIV,coursecd,majorcd,examcoursecd,MIN(h_judgement) AS JUDGEMENT FROM T02 GROUP BY entexamyear,testdiv,examno,coursecd,majorcd,examcoursecd";   # 2005/11/15 arakaki
        $query .= "        UNION ";
        $query .= "        SELECT entexamyear,testdiv,examno,'1' AS SHDIV,coursecd,majorcd,examcoursecd,MIN(s_judgement) AS JUDGEMENT FROM T03 GROUP BY entexamyear,testdiv,examno,coursecd,majorcd,examcoursecd";   # 2005/11/15 arakaki
        $query .= "        UNION ";
        $query .= "        SELECT entexamyear,testdiv,examno,'2' AS SHDIV,coursecd,majorcd,examcoursecd,MIN(h_judgement) AS JUDGEMENT FROM T03 GROUP BY entexamyear,testdiv,examno,coursecd,majorcd,examcoursecd";   # 2005/11/15 arakaki
        $query .= "    ) AS WK01 ";
        $query .= "    GROUP BY ";
        $query .= "        entexamyear, ";
        $query .= "        testdiv, ";
        $query .= "        examno, ";
        $query .= "        shdiv, ";
        $query .= "        coursecd, ";
        $query .= "        majorcd, ";
        $query .= "        examcoursecd ";
        $db->query($query);
//↓2005/11/08 変更後処理（事前相談データ作成から補完処理まで）M.kuninaka
/*
        $query  = " INSERT INTO entexam_applicantcons_dat ";
        $query .= "    ( ";
        $query .= "        entexamyear,  ";
        $query .= "        testdiv, ";
        $query .= "        examno, ";
        $query .= "        shdiv, ";
        $query .= "        coursecd, ";
        $query .= "        majorcd, ";
        $query .= "        examcoursecd, ";
        $query .= "        judgement, ";
        $query .= "        registercd ";
        $query .= "    ) ";
        $query .= "    WITH T01 AS( ";        //2005/11/15 arakaki
        $query .= "            SELECT ";
        $query .= "                t1.entexamyear    AS entexamyear, ";
        $query .= "                t1.testdiv        AS testdiv, ";
        $query .= "                t3.examno         AS examno, ";
        $query .= "                t2.shdiv          AS shdiv, ";
        $query .= "                t2.coursecd       AS coursecd, ";
        $query .= "                t2.majorcd        AS majorcd, ";
        $query .= "                t2.examcoursecd   AS examcoursecd, ";
        $query .= "                MIN(t2.judgement) AS judgement ";
        $query .= "            FROM ";
        $query .= "                entexam_consultation_hdat t1, ";
        $query .= "                entexam_consultation_dat  t2, ";
        $query .= "                entexam_applicantbase_dat t3 ";
        $query .= "            WHERE ";
        $query .= "                t1.entexamyear              = '".$model->year."' AND ";
        $query .= "                t1.testdiv                  = '".$model->testdiv ."' AND ";
        $query .= "                t2.entexamyear              = t1.entexamyear AND ";
        $query .= "                t2.testdiv                  = t1.testdiv AND ";
        $query .= "                t2.acceptno                 = t1.acceptno AND ";
        $query .= "                t2.shdiv                    IS NOT NULL AND ";
        $query .= "                t2.coursecd                 IS NOT NULL AND ";
        $query .= "                t2.majorcd                  IS NOT NULL AND ";
        $query .= "                t2.examcoursecd             IS NOT NULL AND ";
        $query .= "                t3.entexamyear              = t1.entexamyear AND "; 
        $query .= "                t3.testdiv                  = t1.testdiv AND ";
        //2005.11.09 alp m-yama
        if ($jhflg == 0) 
            $query .= "                t3.fs_cd                    = t1.fs_cd AND ";
        $query .= "                LTRIM(RTRIM(t3.name))       = LTRIM(RTRIM(t1.name)) AND ";
        $query .= "                LTRIM(RTRIM(t3.name_kana))  = LTRIM(RTRIM(t1.name_kana)) ";
        $query .= "            GROUP BY ";
        $query .= "                t1.entexamyear, ";
        $query .= "                t1.testdiv, ";
        $query .= "                t3.examno, ";
        $query .= "                t2.shdiv, ";
        $query .= "                t2.coursecd, ";
        $query .= "                t2.examcoursecd, ";
        $query .= "                t2.majorcd ";
        $query .= "    ) ";
        $query .= "    , T02 AS( ";            //補完処理　専願
        $query .= "            SELECT DISTINCT ";
        $query .= "                t1.entexamyear      AS entexamyear, ";
        $query .= "                t1.testdiv          AS testdiv, ";
        $query .= "                t1.examno           AS examno, ";
        $query .= "                t1.shdiv            AS shdiv, ";
        $query .= "                t1.cmp_coursecd     AS coursecd, ";
        $query .= "                t1.cmp_majorcd      AS majorcd, ";
        $query .= "                t1.cmp_examcoursecd AS examcoursecd, ";
        $query .= "                t1.s_judgement      AS s_judgement, ";       # 2005/11/15 arakaki
        $query .= "                t1.h_judgement      AS h_judgement ";        # 2005/11/15 arakaki
#        $query .= "                t1.judgement        AS judgement ";         # 2005/11/15 arakaki
        $query .= "            FROM ";
        $query .= "                (SELECT ";
        $query .= "                    st1.entexamyear, ";
        $query .= "                    st2.testdiv, ";
        $query .= "                    st2.examno, ";
        $query .= "                    st2.shdiv, ";
        $query .= "                    st1.cmp_coursecd, ";
        $query .= "                    st1.cmp_majorcd, ";
        $query .= "                    st1.cmp_examcoursecd, ";
#        $query .= "                    st1.s_judgement AS judgement ";         # 2005/11/15 arakaki
        $query .= "                    st1.s_judgement, ";                      # 2005/11/15 arakaki
        $query .= "                    st1.h_judgement ";                       # 2005/11/15 arakaki
        $query .= "                FROM ";
        $query .= "                    entexam_judgecomp_mst    st1, ";
        $query .= "                    T01                     st2 ";
        $query .= "                WHERE ";
        $query .= "                    st2.entexamyear     = '".$model->year."' AND ";
        $query .= "                    st2.testdiv         = '".$model->testdiv ."' AND ";
        $query .= "                    st2.shdiv            = '1' AND ";
        $query .= "                    st1.entexamyear     = st2.entexamyear AND ";
        $query .= "                    st1.shdiv           = st2.shdiv AND ";
        $query .= "                    st1.coursecd        = st2.coursecd AND ";
        $query .= "                    st1.majorcd         = st2.majorcd AND ";
        $query .= "                    st1.examcoursecd    = st2.examcoursecd AND ";
        $query .= "                    st1.judgement       = st2.judgement AND ";
#        $query .= "                    (st1.s_judgement IS NOT NULL OR st1.s_judgement != '') AND ";   # 2005/11/16 arakaki
        $query .= "                    st2.examno IN (SELECT st1.examno ";
        $query .= "                                    FROM ";
        $query .= "                                        entexam_applicantbase_dat st1, ";
        $query .= "                                        entexam_consultation_hdat st2 ";
        $query .= "                                    WHERE ";
        $query .= "                                        st1.entexamyear             = st1.entexamyear AND ";
        $query .= "                                        st1.testdiv                 = st1.testdiv AND ";
        $query .= "                                        st1.entexamyear             = st2.entexamyear AND ";
        $query .= "                                        st1.testdiv                 = st2.testdiv AND ";
        $query .= "                                        LTRIM(RTRIM(st1.name))      = LTRIM(RTRIM(st2.name)) AND ";
        $query .= "                                        LTRIM(RTRIM(st1.name_kana)) = LTRIM(RTRIM(st2.name_kana)) AND ";
        $query .= "                                        st1.fs_cd                   = st2.fs_cd ";
        $query .= "                                    ) ";
        $query .= "                ) t1 ";
        $query .= "    ) ";
        $query .= "    , T03 AS( ";        //補完処理　併願
        $query .= "            SELECT DISTINCT ";
        $query .= "                t1.entexamyear      AS entexamyear, ";
        $query .= "                t1.testdiv          AS testdiv, ";
        $query .= "                t1.examno           AS examno, ";
        $query .= "                t1.shdiv            AS shdiv, ";
        $query .= "                t1.cmp_coursecd     AS coursecd, ";
        $query .= "                t1.cmp_majorcd      AS majorcd, ";
        $query .= "                t1.cmp_examcoursecd AS examcoursecd, ";
        $query .= "                t1.s_judgement      AS s_judgement, ";       # 2005/11/15 arakaki
        $query .= "                t1.h_judgement      AS h_judgement ";        # 2005/11/15 arakaki
#        $query .= "                t1.judgement        AS judgement ";         # 2005/11/15 arakaki
        $query .= "            FROM ";
        $query .= "                (SELECT ";
        $query .= "                    st1.entexamyear, ";
        $query .= "                    st2.testdiv, ";
        $query .= "                    st2.examno, ";
        $query .= "                    st2.shdiv, ";
        $query .= "                    st1.cmp_coursecd, ";
        $query .= "                    st1.cmp_majorcd, ";
        $query .= "                    st1.cmp_examcoursecd, ";
        $query .= "                    st1.s_judgement, ";                      # 2005/11/15 arakaki
        $query .= "                    st1.h_judgement ";                       # 2005/11/15 arakaki
#        $query .= "                    st1.h_judgement AS judgement ";         # 2005/11/15 arakaki
        $query .= "                FROM ";
        $query .= "                    entexam_judgecomp_mst     st1, ";
        $query .= "                    T01                     st2 ";
        $query .= "                WHERE ";
        $query .= "                    st2.entexamyear     = '".$model->year."' AND ";
        $query .= "                    st2.testdiv         = '".$model->testdiv ."' AND ";
        $query .= "                    st2.shdiv            = '2' AND ";
        $query .= "                    st1.entexamyear     = st2.entexamyear AND ";
        $query .= "                    st1.shdiv           = st2.shdiv AND ";
        $query .= "                    st1.coursecd        = st2.coursecd AND ";
        $query .= "                    st1.majorcd         = st2.majorcd AND ";
        $query .= "                    st1.examcoursecd    = st2.examcoursecd AND ";
        $query .= "                    st1.judgement       = st2.judgement AND ";
#        $query .= "                    (st1.h_judgement IS NOT NULL OR st1.h_judgement != '') AND ";   #2005/11/16 arakaki
        $query .= "                    st2.examno IN (SELECT st1.examno ";
        $query .= "                                    FROM ";
        $query .= "                                        entexam_applicantbase_dat st1, ";
        $query .= "                                        entexam_consultation_hdat st2 ";
        $query .= "                                    WHERE ";
        $query .= "                                        st1.entexamyear             = st1.entexamyear AND ";
        $query .= "                                        st1.testdiv                 = st1.testdiv AND ";
        $query .= "                                        st1.entexamyear             = st2.entexamyear AND ";
        $query .= "                                        st1.testdiv                 = st2.testdiv AND ";
        $query .= "                                        LTRIM(RTRIM(st1.name))      = LTRIM(RTRIM(st2.name)) AND ";
        $query .= "                                        LTRIM(RTRIM(st1.name_kana)) = LTRIM(RTRIM(st2.name_kana)) AND ";
        $query .= "                                        st1.fs_cd                   = st2.fs_cd ";
        $query .= "                                    ) ";
        $query .= "                ) t1 ";
        $query .= "    ) "; 

        $query .= "    SELECT ";    //事前相談データ、補完専願データ、補完併願データよりグループ化し同コースの場合は良い判定をセット。
        $query .= "        entexamyear, ";
        $query .= "        testdiv, ";
        $query .= "        examno, ";
        $query .= "        shdiv, ";
        $query .= "        coursecd, ";
        $query .= "        majorcd, ";
        $query .= "        examcoursecd, ";
        $query .= "        MIN(judgement) as judgement, ";
        $query .= "        '". STAFFCD ."' ";
        $query .= "    FROM ";
        $query .= "    ( ";
        $query .= "        SELECT * FROM T01 ";
        $query .= "        UNION ";
#        $query .= "        SELECT * FROM T02 ";    # 2005/11/15 arakaki
        $query .= "        SELECT DISTINCT entexamyear,testdiv,examno,'1' AS SHDIV,coursecd,majorcd,examcoursecd,s_judgement AS JUDGEMENT FROM T02 ";   # 2005/11/15 arakaki
        $query .= "        UNION ";
        $query .= "        SELECT DISTINCT entexamyear,testdiv,examno,'2' AS SHDIV,coursecd,majorcd,examcoursecd,h_judgement AS JUDGEMENT FROM T02 ";   # 2005/11/15 arakaki
        $query .= "        UNION ";
#        $query .= "        SELECT * FROM T03 ";    # 2005/11/15 arakaki
        $query .= "        SELECT DISTINCT entexamyear,testdiv,examno,'1' AS SHDIV,coursecd,majorcd,examcoursecd,s_judgement AS JUDGEMENT FROM T03 ";   # 2005/11/15 arakaki
        $query .= "        UNION ";
        $query .= "        SELECT DISTINCT entexamyear,testdiv,examno,'2' AS SHDIV,coursecd,majorcd,examcoursecd,h_judgement AS JUDGEMENT FROM T03 ";   # 2005/11/15 arakaki
        $query .= "    ) AS WK01 ";
        $query .= "    GROUP BY ";
        $query .= "        entexamyear, ";
        $query .= "        testdiv, ";
        $query .= "        examno, ";
        $query .= "        shdiv, ";
        $query .= "        coursecd, ";
        $query .= "        majorcd, ";
        $query .= "        examcoursecd ";
        $db->query($query);
*/
        //塾コードのアップデート(事前相談から志願者基礎へ)
        if($jhflg != 0){        #中学のみ更新を行う。   #2006/01/30
            $query = " UPDATE entexam_applicantbase_dat t1";
            $query .= " SET (ps_cd, registercd, updated)=  ";
            $query .= "     (SELECT max(ps_cd), '".STAFFCD ."', SYSDATE() ";
            $query .= "        FROM entexam_consultation_hdat t2";
            $query .= "       WHERE t2.entexamyear              = t1.entexamyear AND ";
            $query .= "             t2.testdiv                  = t1.testdiv AND ";
            //高校のみの条件（出身学校コード）
            if($jhflg == 0){
                $query .= "             t2.fs_cd                    = t1.fs_cd AND ";
            }
            $query .= "             LTRIM(RTRIM(t2.name))       = LTRIM(RTRIM(t1.name)) AND ";
            $query .= "             LTRIM(RTRIM(t2.name_kana))  = LTRIM(RTRIM(t1.name_kana)) ";
            $query .= "     ) ";
            $query .= "WHERE ";
            $query .= "      t1.entexamyear  = '".$model->year."' AND ";
            $query .= "      t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
            $query .= "      t1.testdiv      = '".$model->testdiv ."' ";
            #echo $query;
            $result = $db->query($query);
        }

        //2005/10/20 minei
        //受験番号のアップデート（志願者基礎から事前相談へ）
        $query = " UPDATE entexam_consultation_hdat t1";
        $query .= " SET examno =  ";
#        $query .= "     (SELECT examno ";  #2006/01/30
        $query .= "     (SELECT MIN(examno) ";
        $query .= "        FROM entexam_applicantbase_dat t2";
        $query .= "       WHERE t2.entexamyear              = t1.entexamyear AND ";
        $query .= "             t2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "             t2.testdiv                  = t1.testdiv AND ";
        //高校のみの条件（出身学校コード）
        if($jhflg == 0){
            $query .= "             t2.fs_cd                    = t1.fs_cd AND ";
        }
        $query .= "             LTRIM(RTRIM(t2.name))       = LTRIM(RTRIM(t1.name)) AND ";
        $query .= "             LTRIM(RTRIM(t2.name_kana))  = LTRIM(RTRIM(t1.name_kana)) ";
        $query .= "     ) ";
        $query .= "WHERE ";
        $query .= "      t1.entexamyear  = '".$model->year."' AND ";
        $query .= "      t1.testdiv      = '".$model->testdiv ."' ";
        $result = $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //氏名データ重複チェック
    function selectQueryRepeated_Chck(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $jhflg = $db->getOne(knjl060kQuery::GetJorH());

        //高校のみの条件（出身学校コード）
        //2005.11.15 alp m-yama
        if($jhflg == 0){
            $query  = " SELECT T2.NAME,T2.NAME_KANA,T2.FS_CD,COUNT(*) ";
        }else {
            $query  = " SELECT T2.NAME,T2.NAME_KANA,COUNT(*) ";
        }
//2005.11.15 alp m-yama
//        $query  = " SELECT T2.NAME,COUNT(*) ";
        $query .= "   FROM ENTEXAM_CONSULTATION_HDAT AS T1, ";
        $query .= "        ENTEXAM_APPLICANTBASE_DAT AS T2 ";
        $query .= "  WHERE T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "    AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= "    AND T2.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "    AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "    AND T2.TESTDIV = T1.TESTDIV  ";
        //高校のみの条件（出身学校コード）
        if($jhflg == 0){
            $query .= "    AND T2.FS_CD = T1.FS_CD  ";
        }
        $query .= "    AND LTRIM(RTRIM(T2.NAME)) = LTRIM(RTRIM(T1.NAME))  ";
        $query .= "    AND LTRIM(RTRIM(T2.NAME_KANA)) = LTRIM(RTRIM(T1.NAME_KANA)) ";
//2005.11.15 alp m-yama
//        $query .= "    GROUP BY T2.NAME ";
        //2005.11.15 alp m-yama
        if($jhflg == 0){
            $query .= "    GROUP BY T2.NAME,T2.NAME_KANA,T2.FS_CD ";
        }else {
            $query .= "    GROUP BY T2.NAME,T2.NAME_KANA ";
        }
        $query .= "    HAVING COUNT(*)>1 ";
        #echo $query;
        return $query;
    }


}
?>