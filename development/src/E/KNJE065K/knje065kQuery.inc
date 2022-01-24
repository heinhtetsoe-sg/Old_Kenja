<?php

require_once('for_php7.php');

class knje065kQuery extends Query {

    //成績学年データの存在確認
    function getRecSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_studyrec_dat ";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        return $query;
    }
    //学籍在籍データの存在確認
    function getSchSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_regd_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }
    //評定マスタの存在確認
    function getAssesscd()
    {
        $query  = "SELECT COUNT(assesscd) FROM assess_mst ";
        $query .= " WHERE assesscd = '4' ";
        return $query;
    }

    //コンボボックスの中
    function getGradeQuery()
    {
        $query  = "SELECT DISTINCT(grade) ";
        $query .= "  FROM schreg_regd_hdat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= " ORDER BY GRADE";
        return $query;
    }

    //表示用
    function ReadQuery($model)
    {
        $query  = "SELECT T1.coursecd, T1.majorcd, ";
        $query .= "       (W2.coursename || W2.majorname) AS name, ";
        $query .= "       T1.coursecode, W3.coursecodename, ";
        $query .= "       W1.a_member, W1.b_member, W1.c_member, W1.d_member, W1.e_member, ";
        $query .= "       W1.course_member, W1.grade_member ";
        $query .= "  FROM (SELECT distinct coursecd, majorcd, coursecode ";
        $query .= "          FROM schreg_regd_dat ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'";
        $query .= "           AND semester   = '".CTRL_SEMESTER."'";
        $query .= "           AND grade      = '".$model->gc_select."'";
        $query .= "       ) T1 ";
        $query .= "       LEFT OUTER JOIN geneviewmbr_dat W1 ";
        $query .= "         ON T1.coursecd   = W1.coursecd ";
        $query .= "        AND T1.majorcd    = W1.majorcd ";
        $query .= "        AND T1.coursecode = W1.coursecode ";
        $query .= "        AND W1.year       = '".CTRL_YEAR."'";
        $query .= "        AND W1.grade      = '".$model->gc_select."'";
        $query .= "       LEFT OUTER JOIN v_course_major_mst W2 ";
        $query .= "         ON T1.coursecd   = W2.coursecd ";
        $query .= "        AND T1.majorcd    = W2.majorcd ";
        $query .= "        AND W2.year       = '".CTRL_YEAR."'";
        $query .= "       LEFT OUTER JOIN v_coursecode_mst W3 ";
        $query .= "         ON T1.coursecode = W3.coursecode ";
        $query .= "        AND W3.year       = '".CTRL_YEAR."'";
        $query .= " ORDER BY T1.coursecd, T1.majorcd, T1.coursecode ";
        return $query;
    }
    //表示用
    function ReadGroupQuery($model)
    {
        $query = " SELECT ";
        $query .= "     T1.coursecode, ";
        $query .= "     T1.group_name as coursecodename, ";
        $query .= "     W1.a_member, ";
        $query .= "     W1.b_member, ";
        $query .= "     W1.c_member, ";
        $query .= "     W1.d_member, ";
        $query .= "     W1.e_member, ";
        $query .= "     W1.course_member, ";
        $query .= "     W1.grade_member ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         year, ";
        $query .= "         course_seq AS COURSECODE, ";
        $query .= "         group_name, ";
        $query .= "         grade ";
        $query .= "      FROM course_group_dat ";
        $query .= "     ) T1 ";
        $query .= "     LEFT OUTER JOIN geneviewmbr_dat W1 ON  ";
        $query .= "         W1.coursecd   = '0' AND  ";
        $query .= "         W1.majorcd    = '000' AND  ";                    #2005/07/12
        $query .= "         T1.coursecode = W1.coursecode AND  ";
        $query .= "         W1.year       = '".CTRL_YEAR."' AND  ";
        $query .= "         W1.grade      = T1.grade ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".$model->gc_select."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.coursecode ";
        #echo $query;
        return $query;
    }

    //再計算(コースコード)
    //2005/10/20 作業依頼0509_001により修正
    function &getRecalculateQuery($model)
    {
        //ヘッダーデータ
        $query  = " WITH HEADER_DATA as( ";
        $query .= " SELECT DISTINCT T1.coursecd, T1.majorcd, W2.coursename, W2.majorname, T1.coursecode, W3.coursecodename ";
        $query .= "  FROM (SELECT distinct coursecd, majorcd, coursecode ";
        $query .= "          FROM schreg_regd_dat ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'";
        $query .= "           AND semester   = '".CTRL_SEMESTER."'";
        $query .= "           AND grade      = '".$model->gc_select."'";
        $query .= "       ) T1 ";
        $query .= "       LEFT OUTER JOIN v_course_major_mst W2 ";
        $query .= "         ON T1.coursecd   = W2.coursecd ";
        $query .= "        AND T1.majorcd    = W2.majorcd ";
        $query .= "        AND W2.year       = '".CTRL_YEAR."'";
        $query .= "       LEFT OUTER JOIN v_coursecode_mst W3 ";
        $query .= "         ON T1.coursecode = W3.coursecode ";
        $query .= "        AND W3.year       = '".CTRL_YEAR."'";
        $query .= " ORDER BY T1.coursecd, T1.majorcd, T1.coursecode ";
        $query .= ") ";
        $query .= ",REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "     AND GRADE = '".$model->gc_select."' ";
        $query .= " ), REGD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     MAX(T1.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = REGD_T.SCHREGNO ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE ";
        $query .= " ) ";
        //コースコード別の場合も、「総合的な学習の時間」は除く
        $query .= " ,W_schreg_studyrec_dat as( ";
        $query .= " SELECT ";
        $query .= "     t1.* ";
        $query .= " FROM ";
        $query .= "     schreg_studyrec_dat t1, ";
        $query .= "     REGD_YEAR t2 ";
        $query .= " WHERE ";
        $query .= "     t1.subclasscd not like '90%' AND ";
        $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D065') AND ";
        $query .= "     t1.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "     t1.YEAR = t2.YEAR ";
        $query .= " ) ";
        //対象外の生徒
        $query .= ",SCH_OFF as( ";
        $query .= "       SELECT schregno as sch_off FROM schreg_base_mst WHERE grd_div IN ('2','3') AND grd_date < '".str_replace("/", "-", $model->base_date)."'";    //基準日以前に2:退学、3:転学している生徒
        $query .= " UNION SELECT schregno as sch_off FROM schreg_base_mst WHERE ent_div IS NOT NULL  AND ent_date > '".str_replace("/", "-", $model->base_date)."'";    //基準日以降に在籍する生徒
        $query .= " UNION SELECT schregno as sch_off FROM schreg_transfer_dat ";
        $query .= "       WHERE '".str_replace("/", "-", $model->base_date)."' BETWEEN transfer_sdate AND transfer_edate AND TRANSFERCD <> '1' ";   //基準日に異動している生徒（留学は除く）
        $query .= ") ";
        //
        $query .= "SELECT  THD.coursecd , THD.majorcd, (THD.coursename || THD.majorname) AS name, THD.coursecode, THD.coursecodename ";
        $query .= "       ,SUM(CASE WHEN W2.assesslevel=5 THEN 1 ELSE 0 END) AS A_MEMBER ";
        $query .= "       ,SUM(CASE WHEN W2.assesslevel=4 THEN 1 ELSE 0 END) AS B_MEMBER ";
        $query .= "       ,SUM(CASE WHEN W2.assesslevel=3 THEN 1 ELSE 0 END) AS C_MEMBER ";
        $query .= "       ,SUM(CASE WHEN W2.assesslevel=2 THEN 1 ELSE 0 END) AS D_MEMBER ";
        $query .= "       ,SUM(CASE WHEN W2.assesslevel=1 THEN 1 ELSE 0 END) AS E_MEMBER ";
        $query .= "  FROM HEADER_DATA THD";
        $query .= "     LEFT OUTER JOIN  ";
        $query .= "       (SELECT distinct w1.year, w1.schregno, w1.grade, w1.coursecd, w1.majorcd, w1.coursecode ";
        $query .= "          FROM schreg_regd_dat w1,";
        $query .= "               W_schreg_studyrec_dat w2 ";
        $query .= "         WHERE w1.year     = '".CTRL_YEAR."'";
        $query .= "           AND w1.semester = '".CTRL_SEMESTER."'";
        $query .= "           AND w1.grade    = '".$model->gc_select."' ";
        $query .= "           AND w1.schregno = w2.schregno ";
        $query .= "           AND w1.schregno NOT IN(SELECT sch_off FROM SCH_OFF)";                                                //対象外の生徒
        $query .= "       ) T1 ON THD.coursecd=T1.coursecd AND THD.majorcd=T1.majorcd AND THD.coursecode=T1.coursecode ";
        $query .= "       LEFT OUTER JOIN (SELECT schregno, ROUND(AVG(DEC(VALUE(valuation,0))),1) AS avg FROM W_schreg_studyrec_dat GROUP BY schregno) W1 ";   //2005/11/08 平均値丸め修正
        $query .= "                    ON T1.schregno   = W1.schregno ";
        $query .= "       LEFT OUTER JOIN assess_mst W2 ";
        $query .= "                    ON W2.assesscd   = '4'";
        $query .= "                   AND W1.avg BETWEEN W2.assesslow AND W2.assesshigh ";
        $query .= " GROUP BY THD.coursecd, THD.majorcd, THD.coursename, THD.majorname, THD.coursecode, THD.coursecodename";
        $query .= " ORDER BY THD.coursecd, THD.majorcd, THD.coursecode ";
        return $query;
    }

    //再計算(コースグループ)
    //2005/10/20 作業依頼0509_001により修正
    function &getRecalculateGroupQuery($model)
    {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "     AND GRADE = '".$model->gc_select."' ";
        $query .= " ), REGD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     MAX(T1.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = REGD_T.SCHREGNO ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE ";
        $query .= " ) ";

        //コースグループ別の場合、「総合的な学習の時間」は除く
        $query .= " ,W_schreg_studyrec_dat as ( ";
        $query .= " SELECT ";
        $query .= "     t1.* ";
        $query .= " FROM ";
        $query .= "     schreg_studyrec_dat t1, ";
        $query .= "     REGD_YEAR t2 ";
        $query .= " WHERE ";
        $query .= "     t1.subclasscd not like '90%' AND ";
        $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D065') AND ";
        $query .= "     t1.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "     t1.YEAR = t2.YEAR ";
        $query .= " ) ";
        //対象外の生徒
        $query .= " ,SCH_OFF as( ";
        $query .= "       SELECT schregno as sch_off FROM schreg_base_mst       WHERE grd_div IN ('2','3') AND grd_date < '".str_replace("/", "-", $model->base_date)."'";            //基準日以前に2:退学、3:転学している生徒
        $query .= " UNION SELECT schregno as sch_off FROM schreg_base_mst       WHERE ent_div IS NOT NULL  AND ent_date > '".str_replace("/", "-", $model->base_date)."'";            //基準日以降に在籍する生徒
        $query .= " UNION SELECT schregno as sch_off FROM schreg_transfer_dat ";
        $query .= "       WHERE '".str_replace("/", "-", $model->base_date)."' BETWEEN transfer_sdate AND transfer_edate AND TRANSFERCD <> '1' ";     //基準日に異動している生徒（留学は除く）
        $query .= ") ";
        //
        $query .= " SELECT ";
        $query .= "     T1.coursecode, ";
        $query .= "     T1.group_name AS coursecodename, ";
        $query .= "     SUM(T2.a_member) as a_member, ";
        $query .= "     SUM(T2.b_member) as b_member, ";
        $query .= "     SUM(T2.c_member) as c_member, ";
        $query .= "     SUM(T2.d_member) as d_member, ";
        $query .= "     SUM(T2.e_member) as e_member ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "             year, ";
        $query .= "             course_seq AS coursecode, ";
        $query .= "             group_name, ";
        $query .= "             grade, ";
        $query .= "             hr_class ";
        $query .= "        FROM course_group_dat ";
        $query .= "     ) T1";
        $query .= "     LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "             st1.grade, ";
        $query .= "             st1.hr_class, ";
        $query .= "             SUM(CASE WHEN st2.assesslevel=5 THEN 1 ELSE 0 END) AS A_MEMBER , ";
        $query .= "             SUM(CASE WHEN st2.assesslevel=4 THEN 1 ELSE 0 END) AS B_MEMBER , ";
        $query .= "             SUM(CASE WHEN st2.assesslevel=3 THEN 1 ELSE 0 END) AS C_MEMBER , ";
        $query .= "             SUM(CASE WHEN st2.assesslevel=2 THEN 1 ELSE 0 END) AS D_MEMBER , ";
        $query .= "             SUM(CASE WHEN st2.assesslevel=1 OR st2.assesslevel IS NULL THEN 1 ELSE 0 END) AS E_MEMBER ";
        $query .= "        FROM (SELECT ";
        $query .= "                     st1.schregno, ";
        $query .= "                     st1.grade, ";
        $query .= "                     st1.hr_class, ";
        $query .= "                     ROUND(AVG(DEC(VALUE(st2.valuation,0))),1) AS avg1 ";     //2005/11/08 平均値丸め修正
        $query .= "                FROM ";
        $query .= "                     schreg_regd_dat     st1,";
        $query .= "                     W_schreg_studyrec_dat st2 ";
        $query .= "               WHERE st1.year     = '".CTRL_YEAR."' ";
        $query .= "                 AND st1.semester = '".CTRL_SEMESTER."' ";
        $query .= "                 AND st1.grade    = '".$model->gc_select."'";
        $query .= "                 AND st1.schregno = st2.schregno ";
        $query .= "                 AND st1.schregno NOT IN(SELECT sch_off FROM SCH_OFF)"; //対象外の生徒
        $query .= "               GROUP BY ";
        $query .= "                        st1.schregno, ";
        $query .= "                        st1.grade, ";
        $query .= "                        st1.hr_class ";
        $query .= "             ) st1 ";
        $query .= "             LEFT OUTER JOIN assess_mst st2 ";
        $query .= "                          ON st2.assesscd = '4' ";
        $query .= "                         AND st1.avg1 BETWEEN st2.assesslow AND st2.assesshigh ";
        $query .= "      GROUP BY ";
        $query .= "               st1.grade, ";
        $query .= "               st1.hr_class ";
        $query .= "     ) T2 ON T1.grade = T2.grade AND T1.hr_class = T2.hr_class ";
        $query .= " WHERE ";
        $query .= "     T1.year  = '".CTRL_YEAR."' AND";
        $query .= "     T1.grade = '".$model->gc_select."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.coursecode, ";
        $query .= "     T1.group_name ";
        $query .= " ORDER BY ";
        $query .= "     T1.coursecode ";
        return $query;
    }

    //学年人数合計
    //2005/10/20 作業依頼0509_001により修正
    function &getAllCntQuery($model)
    {

        //コースグループ別の場合、「総合的な学習の時間」は除く
        //コースコード別の場合も、「総合的な学習の時間」は除く
        $query = "WITH W_schreg_studyrec_dat as( ";
            if ($model->radio == 1){    //コースコード別
                $query .= " SELECT * FROM schreg_studyrec_dat WHERE subclasscd not like '90%' ";
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D065') ";
            }else{                      //コースグループ別
                $query .= " SELECT * FROM schreg_studyrec_dat WHERE subclasscd not like '90%' ";
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D065') ";
            }
        $query .= ") ";
        //対象外の生徒
        $query .= " ,SCH_OFF as( ";
        $query .= "       SELECT schregno as sch_off FROM schreg_base_mst WHERE grd_div IN ('2','3') AND grd_date < '".str_replace("/", "-", $model->base_date)."'";    //基準日以前に2:退学、3:転学してる生徒
        $query .= " UNION SELECT schregno as sch_off FROM schreg_base_mst WHERE ent_div IS NOT NULL  AND ent_date > '".str_replace("/", "-", $model->base_date)."'";    //基準日以降に在籍する生徒
        $query .= " UNION SELECT schregno as sch_off FROM schreg_transfer_dat ";
        $query .= "       WHERE '".str_replace("/", "-", $model->base_date)."' BETWEEN transfer_sdate AND transfer_edate AND TRANSFERCD <> '1' "; //基準日に異動している生徒
        $query .= ") ";
        //
        $query .= "SELECT COUNT(schregno) AS GRADE_MEMBER ";
        $query .= "  FROM schreg_regd_dat w1 ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= "   AND grade    = '".$model->gc_select."'";
        $query .= "   AND     EXISTS ( SELECT 'x' FROM W_schreg_studyrec_dat sw1 WHERE sw1.schregno = w1.schregno ) ";      ////学習記録データにデータがある生徒
        $query .= "   AND schregno NOT IN(SELECT sch_off FROM SCH_OFF)";    //対象外の生徒
        return $query;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM geneviewmbr_dat ";
        $query .= " WHERE year  = '".CTRL_YEAR."'";
        $query .= "   AND grade = '".$model->gc_select."'";
        if ($model->radio == 2){
            $query .= "   AND coursecd  = '0'";
            $query .= "   AND majorcd   = '000'";   #2005/07/12
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //UPDATE
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        for($i=0; $i<get_count($model->fields["CODE"]); $i++)
        {
            $code_arr = explode(",",$model->fields["CODE"][$i]);

            $course_member  = $model->fields["A_MEMBER"][$i];
            $course_member += $model->fields["B_MEMBER"][$i];
            $course_member += $model->fields["C_MEMBER"][$i];
            $course_member += $model->fields["D_MEMBER"][$i];
            $course_member += $model->fields["E_MEMBER"][$i];

            $data["YEAR"][TEXT]             = CTRL_YEAR;
            if ($model->radio == 1){
                $data["COURSECD"][TEXT]         = $code_arr[0];
                $data["MAJORCD"][TEXT]          = $code_arr[1];
            }else{
                $data["COURSECD"][TEXT]         = '0';
                $data["MAJORCD"][TEXT]          = '000';        //ALP
            }
            $data["GRADE"][TEXT]            = $model->gc_select;
            $data["COURSECODE"][TEXT]       = $code_arr[2];
            $data["A_MEMBER"][NUMBER]       = $model->fields["A_MEMBER"][$i];
            $data["B_MEMBER"][NUMBER]       = $model->fields["B_MEMBER"][$i];
            $data["C_MEMBER"][NUMBER]       = $model->fields["C_MEMBER"][$i];
            $data["D_MEMBER"][NUMBER]       = $model->fields["D_MEMBER"][$i];
            $data["E_MEMBER"][NUMBER]       = $model->fields["E_MEMBER"][$i];
            $data["COURSE_MEMBER"][NUMBER]  = $course_member;
            $data["GRADE_MEMBER"][NUMBER]   = $model->fields["GRADE_MEMBER"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        =" sysdate()";

            $db->query(Query::insertSQL($data, "geneviewmbr_dat"));
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
