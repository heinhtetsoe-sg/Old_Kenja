<?php

require_once('for_php7.php');


class knji090Query extends Query {

    //卒業年度・卒業期を取得
    function GetYear()
    {
//        $query  = "SELECT DISTINCT ";
//        $query .= "    Fiscalyear(t1.grd_date) as graduate_year, ";
//        $query .= "    t2.present_est ";
//        $query .= "FROM ";
//        $query .= "    schreg_base_mst t1, ";
//        $query .= "    school_mst t2 ";
//        $query .= "WHERE ";
//        $query .= "    t1.grd_div = '1' AND ";
//        $query .= "    t2.year = Fiscalyear(t1.grd_date) ";
//        $query .= "ORDER BY ";
//        $query .= "    graduate_year desc ";
// 2004/02/16 nakamoto
        $query  = "SELECT DISTINCT ";
        $query .= "    Fiscalyear(grd_date) as graduate_year, ";
        $query .= "    grd_term as present_est ";
        $query .= "FROM ";
        $query .= "    grd_base_mst ";
        $query .= "WHERE ";
        $query .= "    grd_div = '1' ";
        $query .= "ORDER BY ";
        $query .= "    graduate_year desc ";

        return $query;
    }


    //卒業年組を取得
    function GetGradeHrClass($year,$schooldiv)
    {
//        $query  = "SELECT DISTINCT ";
//        $query .= "    grade || hr_class VALUE, ";
//        $query .= "    hr_name ";
//        $query .= "FROM ";
//        $query .= "    schreg_regd_hdat  ";
//        $query .= "WHERE ";
//        $query .= "    year='".$year."' AND ";
//        $query .= "    grade || hr_class IN (SELECT DISTINCT ";
//        $query .= "                                    max(t1.grade || t1.hr_class) ";
//        $query .= "                                FROM ";
//        $query .= "                                    v_regdyear_unit_dat t1, ";
//        $query .= "                                    schreg_base_mst t2 ";
//        $query .= "                                WHERE ";
//        $query .= "                                    t1.year='".$year."' AND ";
//        $query .= "                                    t2.grd_div='1' AND ";
//        $query .= "                                    Fiscalyear(t2.grd_date)=t1.year AND ";
//        $query .= "                                    t1.schregno=t2.schregno ";
//        $query .= "                                GROUP BY ";
//        $query .= "                                    t1.schregno ";
//        $query .= "                                ) ";
//        $query .= "ORDER BY ";
//        $query .= "    1 ";
// 2004/02/16 nakamoto
        $query  = "SELECT DISTINCT ";
        $query .= "    grade || hr_class VALUE, ";
        $query .= "    hr_name ";
        $query .= "FROM ";
        $query .= "    grd_regd_hdat  ";
        $query .= "WHERE ";
        $query .= "    year='".$year."' AND ";
        $query .= "    grade || hr_class IN (SELECT DISTINCT ";
        $query .= "                                    max(t1.grade || t1.hr_class) ";
        $query .= "                                FROM ";
if($schooldiv=="0")	//学年制
        $query .= "                                    v_grd_regdyear_grade_dat t1, ";
if($schooldiv=="1")	//単位制
        $query .= "                                    v_grd_regdyear_unit_dat t1, ";
        $query .= "                                    grd_base_mst t2 ";
        $query .= "                                WHERE ";
        $query .= "                                    t1.year='".$year."' AND ";
        $query .= "                                    t2.grd_div='1' AND ";
        $query .= "                                    Fiscalyear(t2.grd_date)=t1.year AND ";
        $query .= "                                    t1.schregno=t2.schregno ";
        $query .= "                                GROUP BY ";
        $query .= "                                    t1.schregno ";
        $query .= "                                ) ";
        $query .= "ORDER BY ";
        $query .= "    1 ";

        return $query;
    }


    //卒業年度・卒業期を取得
    function GetSchregno($year,$grade_hr_class,$schooldiv)
    {
        $query .= "SELECT ";
        $query .= "    t1.schregno, ";
        $query .= "    t1.attendno, ";
        $query .= "    t2.name_show  ";
        $query .= "FROM ";
//        $query .= "    v_regdyear_unit_dat t1, ";
//        $query .= "    schreg_base_mst t2 ";
// 2004/02/16 nakamoto
if($schooldiv=="0")	//学年制
        $query .= "    v_grd_regdyear_grade_dat t1, ";
if($schooldiv=="1")	//単位制
        $query .= "    v_grd_regdyear_unit_dat t1, ";
        $query .= "    grd_base_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".$year."' AND ";
        $query .= "    t1.grade || t1.hr_class = '".$grade_hr_class."' AND ";
        $query .= "    t2.grd_div='1' AND ";
        $query .= "    Fiscalyear(t2.grd_date)=t1.year AND ";
        $query .= "    t1.schregno=t2.schregno  ";
        $query .= "ORDER BY ";
        $query .= "    t1.attendno ";

        return $query;
    }



}
?>
