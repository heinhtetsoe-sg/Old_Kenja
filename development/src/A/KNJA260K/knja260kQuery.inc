<?php

require_once('for_php7.php');

class knja260kQuery extends Query {

    //中高判別---2005.09.30
    function GetJorH($year){
        $db = Query::dbCheckOut();
        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   SCHOOL_MST ";
        $query .= " WHERE  YEAR = '".$year."' AND SCHOOLNAME1 LIKE '%中学%' ";
        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

//NO001 NO002 false:中学,true:高校
        return ($ret_val) ? false : true;
//        return ($ret_val <= 0) ? false : true;
    }

    //学期取得
    function getSelectSeme($year)
    {
        $query = "SELECT semester,semestername FROM semester_mst where year='" .$year ."' order by semester";

        return $query;
    }

    //最終学期取得
    function getSelectSemester($year)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT MAX(semester) AS GAKKI_MAX FROM schreg_regd_hdat where year='" .$year ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //年度取得
    function getSelectYear()
    {
        $query = "SELECT distinct year FROM schreg_regd_hdat ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$model->field["YEAR"] ."'";
            if ($model->field["GAKKI"] != "9"){
                $query .= "AND SEMESTER='".$model->field["GAKKI"] ."'";
            }else {
                $query .= "AND SEMESTER='".$model->control["学期"] ."'";
            }
            if ($model->field["GAKKI"]=="3")
                $query .= "AND GRADE<>'03'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$model->field["YEAR"] ."' ";
            if ($model->field["GAKKI"] != "9"){
                $query .= "AND SEMESTER='".$model->field["GAKKI"] ."'";
            }else {
                $query .= "AND SEMESTER='".$model->control["学期"] ."'";
            }
            if ($model->field["GAKKI"]=="3")
                $query .= "AND GRADE<>'03'";
            //2005.09.30Modify
            //$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            //$query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            //$query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //科目取得
    function getSelectSubClass1($model,$semester)
    {
        $query  = "SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.classcd, ";
            $query .= "    t1.school_kind, ";
            $query .= "    t1.curriculum_cd, ";
        }
        $query .= "    t1.subclasscd, ";
        $query .= "    t2.subclassname ";
        $query .= "FROM ";
        $query .= "    kin_record_dat t1, ";
        $query .= "    subclass_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='" .$model->field["YEAR"] ."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.classcd=t2.classcd AND ";
            $query .= "    t1.school_kind=t2.school_kind AND ";
            $query .= "    t1.curriculum_cd=t2.curriculum_cd AND ";
        }
        $query .= "    t1.subclasscd=t2.subclasscd AND ";
        $query .= "    t1.schregno IN (SELECT ";
        $query .= "                        schregno ";
        $query .= "                    FROM ";
        $query .= "                        schreg_regd_dat ";
        $query .= "                    WHERE ";
        $query .= "                        year='" .$model->field["YEAR"] ."' AND ";
        if ($semester != "9"){
            $query .= "                        semester='" .$semester ."' AND ";
        }else {
            $query .= "                        semester='" .$model->control["学期"]."' AND ";
        }
        $query .= "                        grade || hr_class IN ('".implode($model->select_data["selectdata"],"','")."') ";
        $query .= "                    ) ";
        $query .= "ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.classcd, ";
            $query .= "    t1.school_kind, ";
            $query .= "    t1.curriculum_cd, ";
        }
        $query .= "    t1.subclasscd ";

        return $query;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //CVS作成用のQUERY（テスト毎）
    function selectCsvQuery1($model,$testdi)
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    m1.schregno,m2.grade,m2.hr_class,m2.attendno,m3.name_show,'".$testdi."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $chk = "m1.classcd || '_' || m1.school_kind || '_' || m1.curriculum_cd || '_' || m1.subclasscd";
        } else {
            $chk = "m1.subclasscd";
        }
        if ($model->field["GAKKI"]=="1"){
            if ($model->field["TESTKINDCD"] == "01"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM1_INTER_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM1_INTER_REC is not null THEN int(m1.SEM1_INTER_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
//                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM1_INTER_REC is not null THEN int(m1.SEM1_INTER_REC_DI) ELSE NULL END ) as DI" .$i ." ";
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM1_INTER_REC_DI WHEN 'KS' THEN int(2) WHEN 'KK' THEN int(1) ELSE int(0) END ELSE int(0) END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "02"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM1_TERM_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM1_TERM_REC is not null THEN int(m1.SEM1_TERM_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
//                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM1_TERM_REC is not null THEN int(m1.SEM1_TERM_REC_DI) ELSE NULL END ) as DI" .$i ." ";
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM1_TERM_REC_DI WHEN 'KS' THEN int(2) WHEN 'KK' THEN int(1) ELSE int(0) END ELSE int(0) END) as DI" .$i ." ";
                }
            }else {
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM1_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM1_REC is not null THEN int(m1.SEM1_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
                    $query .= "    ,'' ";
                }
            }
        }else if ($model->field["GAKKI"]=="2"){
            if ($model->field["TESTKINDCD"] == "01"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM2_INTER_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM2_INTER_REC is not null THEN int(m1.SEM2_INTER_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
//                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM2_INTER_REC is not null THEN int(m1.SEM2_INTER_REC_DI) ELSE NULL END ) as DI" .$i ." ";
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM2_INTER_REC_DI WHEN 'KS' THEN int(2) WHEN 'KK' THEN int(1) ELSE int(0) END ELSE int(0) END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "02"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM2_TERM_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM2_TERM_REC is not null THEN int(m1.SEM2_TERM_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
//                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM2_TERM_REC is not null THEN int(m1.SEM2_TERM_REC_DI) ELSE NULL END ) as DI" .$i ." ";
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM2_TERM_REC_DI WHEN 'KS' THEN int(2) WHEN 'KK' THEN int(1) ELSE int(0) END ELSE int(0) END) as DI" .$i ." ";
                }
            }else {
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM2_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM2_REC is not null THEN int(m1.SEM2_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
                    $query .= "    ,'' ";
                }
            }
        }else if ($model->field["GAKKI"]=="3"){
            if ($model->field["TESTKINDCD"] == "02"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM3_TERM_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM3_TERM_REC is not null THEN int(m1.SEM3_TERM_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
//                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM3_TERM_REC is not null THEN int(m1.SEM3_TERM_REC_DI) ELSE NULL END ) as DI" .$i ." ";
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM3_TERM_REC_DI WHEN 'KS' THEN int(2) WHEN 'KK' THEN int(1) ELSE int(0) END ELSE int(0) END) as DI" .$i ." ";
                }
            }else {
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM3_REC ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,SUM( CASE WHEN ".$chk."='".$model->opt_subclass[$i]."' and m1.SEM3_REC is not null THEN int(m1.SEM3_REC_FLG) ELSE NULL END ) as flg" .$i ." ";
                    $query .= "    ,'' ";
                }
            }
        }else {
            //NO001 NO002
            if ($model->jhflg != "on"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    if ($model->field["TESTKINDCD"] == "99"){
                        $query .= "    ,MIN( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN ";
                        $query .= "              CASE GRADE_RECORD IS NOT NULL THEN ";
                        $query .= "                  CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ";
                        $query .= "                                     WHEN 'B' THEN B_PATTERN_ASSESS ";
                        $query .= "                                     WHEN 'C' THEN C_PATTERN_ASSESS ";
                        $query .= "                  ELSE NULL END ";
                        $query .= "              ELSE NULL END ";
                        $query .= "          ELSE NULL END ) as tokuten" .$i ." ";
                    } else {
                        $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.GRADE_RECORD ELSE NULL END ) as tokuten" .$i ." ";
                        $query .= "    ,'' ";
                        $query .= "    ,'' ";
                        $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN SMALLINT(m1.GRADE_ASSESS) ELSE NULL END ) as GRADE_ASSESS" .$i ." ";
                        $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN SMALLINT(m1.GRADE3_RELAASSESS_5STEP) ELSE NULL END ) as GRADE3_RELAASSESS_5STEP" .$i ." ";
                        $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN SMALLINT(m1.GRADE3_RELAASSESS_10STEP) ELSE NULL END ) as GRADE3_RELAASSESS_10STEP" .$i ." ";
                    }
                }
            }else {
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "    ,SUM( CASE ".$chk." WHEN '".$model->opt_subclass[$i]."' THEN m1.GRADE_RECORD ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "    ,'' ";
                    $query .= "    ,'' ";
                }
            }
        }
        $query .= "FROM ";
        $query .= "    kin_record_dat m1, ";
        $query .= "    schreg_regd_dat m2, ";
        $query .= "    schreg_base_mst m3 ";
        $query .= "WHERE ";
        $query .= "    m2.year='".$model->field["YEAR"]."' AND ";
        if ($model->field["GAKKI"] != "9"){
            $query .= "    m2.semester='".$model->field["GAKKI"]."' AND ";
        }else {
            $query .= "    m2.semester='".$model->control["学期"]."' AND ";
        }
        $query .= "    m2.grade || m2.hr_class IN ('".implode($model->select_data["selectdata"],"','")."') AND ";
        $query .= "    m2.year=m1.year AND ";
        $query .= "    m2.schregno=m1.schregno AND ";
        $query .= "    m3.schregno=m2.schregno ";
        $query .= "GROUP BY ";
        $query .= "    m1.schregno,m2.grade,m2.hr_class,m2.attendno,m3.name_show ";
        $query .= "ORDER BY ";
        $query .= "    m2.grade,m2.hr_class,m2.attendno ";

        return $query;
    }

    //科目取得(学年評定/学期評定)---2005.09.30
    function getSelectSubClass2($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO, GRADE, HR_CLASS, ATTENDNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR = '" .$model->field["YEAR"] ."' AND ";
        if ($model->field["GAKKI"] == "9") {
            $query .= "            SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "            SEMESTER = '" .$model->field["GAKKI"] ."' AND ";
        }
        $query .= "            GRADE||HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') ) ";
        $query .= " ,KIN_REC AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ";//JUDGE_PATTERNをカット
        } else {
            $query .= "     SELECT SUBCLASSCD, SCHREGNO, ";//JUDGE_PATTERNをカット
        }
        $query .= "            SUM(SEM1_REC) AS SCORE1, SUM(SEM2_REC) AS SCORE2, SUM(SEM3_TERM_REC) AS SCORE3, ";
        $query .= "            COUNT(SEM1_REC) AS CNT1, COUNT(SEM2_REC) AS CNT2, COUNT(SEM3_TERM_REC) AS CNT3 ";
        $query .= "     FROM   KIN_RECORD_DAT ";
        $query .= "     WHERE  YEAR = '" .$model->field["YEAR"] ."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     GROUP BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO ";//JUDGE_PATTERNをカット
        } else {
            $query .= "     GROUP BY SUBCLASSCD, SCHREGNO ";//JUDGE_PATTERNをカット
        }
        //学年評定
        if ($model->field["TESTKINDCD"] == "99") {
            if ($model->field["GAKKI"] == "1") 
                $query .= " HAVING (0 < COUNT(SEM1_REC)) ) ";
            if ($model->field["GAKKI"] == "2") 
                $query .= " HAVING (0 < COUNT(SEM1_REC) OR 0 < COUNT(SEM2_REC)) ) ";
            if ($model->field["GAKKI"] == "3") 
                $query .= " HAVING (0 < COUNT(SEM1_REC) OR 0 < COUNT(SEM2_REC) OR 0 < COUNT(SEM3_TERM_REC)) ) ";
            if ($model->field["GAKKI"] == "9") {
                $query .= " HAVING (0 < COUNT( ";
                $query .= "              CASE WHEN GRADE_RECORD IS NOT NULL THEN ";
                $query .= "                  CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ";
                $query .= "                                     WHEN 'B' THEN B_PATTERN_ASSESS ";
                $query .= "                                     WHEN 'C' THEN C_PATTERN_ASSESS ";
                $query .= "                  ELSE NULL END ";
                $query .= "              ELSE NULL END)) ) ";
            }
        //学期評定
        } else {
            if ($model->field["GAKKI"] == "1") 
                $query .= " HAVING (0 < COUNT(SEM1_REC)) ) ";
            if ($model->field["GAKKI"] == "2") 
                $query .= " HAVING (0 < COUNT(SEM2_REC)) ) ";
            if ($model->field["GAKKI"] == "3") 
                $query .= " HAVING (0 < COUNT(SEM3_TERM_REC)) ) ";
        }

        //メイン
        $query .= " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ";
        }
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     INNER JOIN KIN_REC T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T3.CLASSCD = T2.CLASSCD ";
            $query .= "                           AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "                           AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        if ($model->field["TESTKINDCD"] == '99') {
            $query .= " UNION ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "     T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
            $query .= "     T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "     L1.SUBCLASSNAME ";
            $query .= " FROM ";
            $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
            $query .= "                           AND L1.CLASSCD = T1.COMBINED_CLASSCD ";
            $query .= "                           AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
            $query .= "                           AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
            $query .= " WHERE ";
            $query .= "     T1.REPLACECD = '1' ";
            $query .= "     AND T1.YEAR = '" .$model->field["YEAR"] ."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    function selectReplaceSubclass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ATTEND_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.ATTEND_CLASSCD AS CLASSCD, ";
        $query .= "     T1.COMBINED_CLASSCD, ";
        $query .= "     T1.ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "     T1.COMBINED_SCHOOL_KIND, ";
        $query .= "     T1.ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ";
        $query .= "     T1.COMBINED_CURRICULUM_CD, ";
        $query .= "     L1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "                           AND L1.CLASSCD = T1.ATTEND_CLASSCD ";
        $query .= "                           AND L1.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
        $query .= "                           AND L1.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.REPLACECD = '1' ";
        $query .= "     AND T1.YEAR = '" .$model->field["YEAR"] ."' ";

        return $query;
    }

    //CSV出力(学年評定)---2005.09.30
    function selectCsvQuery2($model)
    {
        //在籍の表(クラス)
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO, GRADE, HR_CLASS, ATTENDNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR = '" .$model->field["YEAR"] ."' AND ";
        if ($model->field["GAKKI"] == "9") {
            $query .= "            SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "            SEMESTER = '" .$model->field["GAKKI"] ."' AND ";
        }
        $query .= "            GRADE||HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') ) ";
        if ($model->field["GAKKI"] == "9") {
            //成績の表A
            $query .= " ,KIN_REC AS ( ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ";//JUDGE_PATTERNをカット
            } else {
                $query .= "     SELECT SUBCLASSCD, SCHREGNO, ";//JUDGE_PATTERNをカット
            }
            $query .= "              CASE WHEN GRADE_RECORD IS NOT NULL THEN ";
            $query .= "                  CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ";
            $query .= "                                     WHEN 'B' THEN B_PATTERN_ASSESS ";
            $query .= "                                     WHEN 'C' THEN C_PATTERN_ASSESS ";
            $query .= "                  ELSE NULL END ";
            $query .= "              END AS TYPE_ASSES_LEVEL ";
            $query .= "     FROM   KIN_RECORD_DAT ";
            $query .= "     WHERE  YEAR = '" .$model->field["YEAR"] ."' ";
            $query .= " ) ";

            //メイン
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T4.NAME_SHOW, ";
            $query .= "     '学年評定' AS KOTEI ";
            for ($i = 0; $i < get_count($model->opt_subclass); $i++) {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "    ,MAX(CASE WHEN T2.CLASSCD || '_' || T2.SCHOOL_KIND || '_' || T2.CURRICULUM_CD || '_' || T2.SUBCLASSCD = '".$model->opt_subclass[$i]."' THEN T2.TYPE_ASSES_LEVEL ELSE NULL END) AS ASSES". $model->opt_subclass[$i] ." ";
                } else {
                    $query .= "    ,MAX(CASE WHEN T2.SUBCLASSCD = '".$model->opt_subclass[$i]."' THEN T2.TYPE_ASSES_LEVEL ELSE NULL END) AS ASSES". $model->opt_subclass[$i] ." ";
                }
                $query .= "    ,'' AS FLG". $i ." ";
                $query .= "    ,'' AS DI". $i ." ";
            }
            $query .= " FROM   SCHNO T1 ";
            $query .= "        LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "        LEFT JOIN KIN_REC T2 ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= " GROUP BY T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T4.NAME_SHOW ";
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        } else {
        //成績の表A
        $query .= " ,KIN_REC AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ";//JUDGE_PATTERNをカット
        } else {
            $query .= "     SELECT SUBCLASSCD, SCHREGNO, ";//JUDGE_PATTERNをカット
        }
        $query .= "            SUM(SEM1_REC) AS SCORE1, SUM(SEM2_REC) AS SCORE2, SUM(SEM3_TERM_REC) AS SCORE3, ";
        $query .= "            COUNT(SEM1_REC) AS CNT1, COUNT(SEM2_REC) AS CNT2, COUNT(SEM3_TERM_REC) AS CNT3 ";
        $query .= "     FROM   KIN_RECORD_DAT ";
        $query .= "     WHERE  YEAR = '" .$model->field["YEAR"] ."' ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, SCHREGNO ";//JUDGE_PATTERNをカット
        if ($model->field["GAKKI"] == "1") 
            $query .= " HAVING (0 < COUNT(SEM1_REC)) ) ";
        if ($model->field["GAKKI"] == "2") 
            $query .= " HAVING (0 < COUNT(SEM1_REC) OR 0 < COUNT(SEM2_REC)) ) ";
        if ($model->field["GAKKI"] == "3") 
            $query .= " HAVING (0 < COUNT(SEM1_REC) OR 0 < COUNT(SEM2_REC) OR 0 < COUNT(SEM3_TERM_REC)) ) ";
        //成績の表B(指定学期までの平均)
        $query .= " ,KIN_REC2 AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ";
        } else {
            $query .= "     SELECT SUBCLASSCD, SCHREGNO, ";
        }
        if ($model->field["GAKKI"] == "1") 
            $query .= "        ROUND(FLOAT((VALUE(SCORE1,0)))/(CNT1),0) AS SCORE ";
        if ($model->field["GAKKI"] == "2") 
            $query .= "        ROUND(FLOAT((VALUE(SCORE1,0)+VALUE(SCORE2,0)))/(CNT1+CNT2),0) AS SCORE ";
        if ($model->field["GAKKI"] == "3") 
            $query .= "        ROUND(FLOAT((VALUE(SCORE1,0)+VALUE(SCORE2,0)+VALUE(SCORE3,0)))/(CNT1+CNT2+CNT3),0) AS SCORE ";
        $query .= "     FROM   KIN_REC ) ";

        /***↓*** 2005.10.20 ***↓***/
        //類型グループの表A(クラス)
        $query .= ",TYPE_GROUP_A AS ( ";
        $query .= "    SELECT W1.GRADE, W1.HR_CLASS, W2.TYPE_GROUP_CD AS GROUP_CD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD, W2.TYPE_ASSES_CD AS ASSES_CD, ";
        } else {
            $query .= "           W2.SUBCLASSCD, W2.TYPE_ASSES_CD AS ASSES_CD, ";
        }
        $query .= "           W2.SEM1_REC_SUM AS SUM1,W2.SEM2_REC_SUM AS SUM2,W2.SEM3_TERM_REC_SUM AS SUM3, ";
        $query .= "           W2.SEM1_REC_CNT AS CNT1,W2.SEM2_REC_CNT AS CNT2,W2.SEM3_TERM_REC_CNT AS CNT3 ";
        $query .= "    FROM   TYPE_GROUP_HR_DAT W1, TYPE_GROUP_MST W2 ";
        $query .= "    WHERE  W1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "           W1.GRADE||W1.HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') AND ";
        $query .= "           W1.YEAR = W2.YEAR AND W1.TYPE_GROUP_CD = W2.TYPE_GROUP_CD AND W1.GRADE = W2.GRADE ) ";
        //類型グループの表B(学期毎の平均)
        $query .= ",TYPE_GROUP_B AS ( ";
        $query .= "    SELECT GRADE, HR_CLASS, GROUP_CD, SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           FLOAT(SUM1)/CNT1 AS SCORE, '1' AS SEM ";
        $query .= "    FROM   TYPE_GROUP_A ";
        $query .= "    WHERE  0 < CNT1 ";
        $query .= "    UNION ALL ";
        $query .= "    SELECT GRADE, HR_CLASS, GROUP_CD, SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           FLOAT(SUM2)/CNT2 AS SCORE, '2' AS SEM ";
        $query .= "    FROM   TYPE_GROUP_A ";
        $query .= "    WHERE  0 < CNT2 ";
        $query .= "    UNION ALL ";
        $query .= "    SELECT GRADE, HR_CLASS, GROUP_CD, SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "           FLOAT(SUM3)/CNT3 AS SCORE, '3' AS SEM ";
        $query .= "    FROM   TYPE_GROUP_A ";
        $query .= "    WHERE  0 < CNT3 ) ";
        //類型グループの表C(指定学期までの平均)
        $query .= ",TYPE_GROUP_C AS ( ";
        $query .= "    SELECT GRADE, HR_CLASS, GROUP_CD, SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    , CLASSCD, SCHOOL_KIND, CURRICULUM_CD ";
        }
        $query .= "           ,ROUND(AVG(SCORE),0) AS SCORE_AVG ";
        $query .= "    FROM   TYPE_GROUP_B ";
        $query .= "    WHERE  SEM <= '".$model->field["GAKKI"]."' ";
        $query .= "    GROUP BY GRADE, HR_CLASS, GROUP_CD, SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    , CLASSCD, SCHOOL_KIND, CURRICULUM_CD ";
        }
        $query .= " ) ";
        //類型評定の表A(類型評定コードの取得:評価類型評定コードを優先)
        $query .= ",TYPE_ASSES_A AS ( ";
        $query .= "    SELECT T1.GRADE, T1.HR_CLASS, T1.GROUP_CD, T1.SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "           CASE WHEN T3.ASSES_CD IS NOT NULL THEN T3.ASSES_CD ";
        $query .= "                ELSE T2.TYPE_ASSES_CD END AS ASSES_CD ";
        $query .= "    FROM   TYPE_GROUP_C T1 ";
        $query .= "           LEFT JOIN TYPE_ASSES_HDAT T2 ON T2.YEAR = '" .$model->field["YEAR"] ."' AND  ";
        $query .= "                                           T1.SCORE_AVG BETWEEN T2.TYPE_GROUP_AVE_LOW AND ";
        $query .= "                                           T2.TYPE_GROUP_AVE_HIGH ";
        $query .= "           LEFT JOIN TYPE_GROUP_A T3 ON T3.GRADE = T1.GRADE AND  ";
        $query .= "                                        T3.HR_CLASS = T1.HR_CLASS AND  ";
        $query .= "                                        T3.GROUP_CD = T1.GROUP_CD AND  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD = T1.CLASSCD AND ";
            $query .= "    T3.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "    T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                                        T3.SUBCLASSCD = T1.SUBCLASSCD ) ";
        //類型評定の表B(類型評定段階・類型評定上限値・類型評定下限値の取得)
        $query .= ",TYPE_ASSES_B AS ( ";
        $query .= "    SELECT T1.GRADE, T1.HR_CLASS, T1.GROUP_CD, T1.SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "           T2.TYPE_ASSES_LEVEL, T2.TYPE_ASSES_LOW, T2.TYPE_ASSES_HIGH ";
        $query .= "    FROM   TYPE_ASSES_A T1 ";
        $query .= "           LEFT JOIN TYPE_ASSES_MST T2 ON T2.YEAR = '" .$model->field["YEAR"] ."' AND  ";
        $query .= "                                          T2.TYPE_ASSES_CD = T1.ASSES_CD ) ";
        /***↑*** 2005.10.20 ***↑***/

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T4.NAME_SHOW, ";
        $query .= "     '学年評定' AS KOTEI ";
        for ($i = 0; $i < get_count($model->opt_subclass); $i++) {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    ,MAX(CASE WHEN T2.CLASSCD || '_' || T2.SCHOOL_KIND || '_' || T2.CURRICULUM_CD || '_' || T2.SUBCLASSCD = '".$model->opt_subclass[$i]."' THEN T3.TYPE_ASSES_LEVEL ELSE NULL END) AS ASSES". $model->opt_subclass[$i] ." ";
            } else {
                $query .= "    ,MAX(CASE WHEN T2.SUBCLASSCD = '".$model->opt_subclass[$i]."' THEN T3.TYPE_ASSES_LEVEL ELSE NULL END) AS ASSES". $model->opt_subclass[$i] ." ";
            }
            $query .= "    ,'' AS FLG". $i ." ";
            $query .= "    ,'' AS DI". $i ." ";
        }
        $query .= " FROM   SCHNO T1 ";
        $query .= "        LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN KIN_REC2 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        /***↓*** 2005.10.20 ***↓***/
        $query .= "        LEFT JOIN TYPE_ASSES_B T3 ON T3.GRADE = T1.GRADE AND ";
        $query .= "                                     T3.HR_CLASS = T1.HR_CLASS AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                     T3.CLASSCD = T2.CLASSCD AND ";
            $query .= "                                     T3.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "                                     T3.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "                                     T3.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "                                     T2.SCORE BETWEEN T3.TYPE_ASSES_LOW AND ";
        $query .= "                                     T3.TYPE_ASSES_HIGH ";
        /***
        $query .= "        LEFT JOIN TYPE_ASSES_MST T3 ON T3.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "                                       T3.TYPE_ASSES_CD = T2.JUDGE_PATTERN AND ";
        $query .= "                                       T2.SCORE BETWEEN T3.TYPE_ASSES_LOW AND ";
        $query .= "                                       T3.TYPE_ASSES_HIGH ";
        ***/
        /***↑*** 2005.10.20 ***↑***/
        $query .= " GROUP BY T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T4.NAME_SHOW ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        }
        return $query;
    }

    //CSV出力(学期評定)
    function selectCsvQuery3($model)
    {
        $score_name = "";
        if ($model->field["GAKKI"] == "1") $score_name = "SEM1_REC";
        if ($model->field["GAKKI"] == "2") $score_name = "SEM2_REC";
        if ($model->field["GAKKI"] == "3") $score_name = "SEM3_TERM_REC";
        //在籍の表(クラス)
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO, GRADE, HR_CLASS, ATTENDNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "            SEMESTER = '" .$model->field["GAKKI"] ."' AND ";
        $query .= "            GRADE||HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') ) ";
        //成績の表A
        $query .= " ,KIN_REC AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO ";
        } else {
            $query .= "     SELECT SUBCLASSCD, SCHREGNO ";
        }
        $query .= "           ,SUM(".$score_name.") AS SCORE ";
        $query .= "     FROM   KIN_RECORD_DAT ";
        $query .= "     WHERE  YEAR = '" .$model->field["YEAR"] ."' ";
        $query .= "     GROUP BY SUBCLASSCD, SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     , CLASSCD ,SCHOOL_KIND, CURRICULUM_CD ";
        }
        $query .= "     HAVING 0 < COUNT(".$score_name.") ) ";
        //類型グループの表A(クラス)
        $query .= ",TYPE_GROUP_A AS ( ";
        $query .= "    SELECT W1.GRADE, W1.HR_CLASS, W2.TYPE_GROUP_CD AS GROUP_CD, W2.SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ";
        }
        $query .= "           CASE WHEN W2.TYPE_ASSES_CD IS NOT NULL THEN W2.TYPE_ASSES_CD ";
        $query .= "                ELSE W2.".$score_name."_TYPE_ASSES_CD END AS ASSES_CD ";
        $query .= "    FROM   TYPE_GROUP_HR_DAT W1, TYPE_GROUP_MST W2 ";
        $query .= "    WHERE  W1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "           W1.GRADE||W1.HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') AND ";
        $query .= "           W1.YEAR = W2.YEAR AND W1.TYPE_GROUP_CD = W2.TYPE_GROUP_CD AND W1.GRADE = W2.GRADE ) ";
        //類型評定の表B(類型評定段階・類型評定上限値・類型評定下限値の取得)
        $query .= ",TYPE_ASSES_A AS ( ";
        $query .= "    SELECT T1.GRADE, T1.HR_CLASS, T1.GROUP_CD, T1.SUBCLASSCD, T1.ASSES_CD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "           T2.TYPE_ASSES_LEVEL, T2.TYPE_ASSES_LOW, T2.TYPE_ASSES_HIGH ";
        $query .= "    FROM   TYPE_GROUP_A T1 ";
        $query .= "           LEFT JOIN TYPE_ASSES_MST T2 ON T2.YEAR = '" .$model->field["YEAR"] ."' AND  ";
        $query .= "                                          T2.TYPE_ASSES_CD = T1.ASSES_CD ) ";

        //メイン
        $query .= " SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T4.NAME_SHOW, '学期評定' ";
        for ($i = 0; $i < get_count($model->opt_subclass); $i++) {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    ,MAX(CASE WHEN T2.CLASSCD || '_' || T2.SCHOOL_KIND || '_' || T2.CURRICULUM_CD || '_' || T2.SUBCLASSCD = '".$model->opt_subclass[$i]."' THEN T3.TYPE_ASSES_LEVEL ELSE NULL END) AS ASSES". $i ." ";
            } else {
                $query .= "    ,MAX(CASE WHEN T2.SUBCLASSCD = '".$model->opt_subclass[$i]."' THEN T3.TYPE_ASSES_LEVEL ELSE NULL END) AS ASSES". $i ." ";
            }
            $query .= "    ,'' AS FLG". $i ." ";
            $query .= "    ,'' AS DI". $i ." ";
        }
        $query .= " FROM   SCHNO T1 ";
        $query .= "        LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN KIN_REC T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN TYPE_ASSES_A T3 ON T3.GRADE = T1.GRADE AND ";
        $query .= "                                     T3.HR_CLASS = T1.HR_CLASS AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                     T3.CLASSCD = T2.CLASSCD AND ";
            $query .= "                                     T3.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "                                     T3.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "                                     T3.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "                                     T2.SCORE BETWEEN T3.TYPE_ASSES_LOW AND ";
        $query .= "                                     T3.TYPE_ASSES_HIGH ";
        $query .= " GROUP BY T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T4.NAME_SHOW ";
        $query .= " ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

}
?>
