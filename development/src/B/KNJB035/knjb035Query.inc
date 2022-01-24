<?php

require_once('for_php7.php');

class knjb035Query extends Query {

	//---------------------------------共通---------------------------------

    //校時情報を取得
    function getVnamemst()
    {
        $query  = "SELECT ";
        $query .= "    namecd2,name1 ";
        $query .= "FROM ";
        $query .= "    v_name_mst ";
        $query .= "WHERE ";
        $query .= "    year='".CTRL_YEAR."' AND ";
        $query .= "    namecd1='B001' ";
        $query .= "order by namecd2 ";

        return $query;
    }

    //クラス情報を取得
    function getGrCl_hdat($semester)
    {
        $query  = "SELECT ";
        $query .= "    grade || hr_class gr_cl,hr_name ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat ";
        $query .= "WHERE ";
        $query .= "    year='".CTRL_YEAR."' AND ";
        $query .= "    semester='".$semester."' ";
        $query .= "order by grade || hr_class ";

        return $query;
    }

	//---------------------------------基本---------------------------------

    //学期コンボリスト用を取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    semester, ";
        $query .= "    semestername ";
        $query .= "FROM ";
        $query .= "    semester_mst ";
        $query .= "WHERE ";
        $query .= "    year='".CTRL_YEAR."' AND ";
        $query .= "    semester<>'9' ";
        $query .= "ORDER BY ";
        $query .= "    semester ";

        return $query;
    }
    //ＳＥＱ：タイトルを取得
    function getSeqTitle($semester)
    {
        $query  = "SELECT ";
        $query .= "    bscseq, ";
        $query .= "    title ";
        $query .= "FROM ";
        $query .= "    sch_ptrn_hdat ";
        $query .= "WHERE ";
        $query .= "    year='".CTRL_YEAR."' AND ";
        $query .= "    semester='".$semester."' ";
        $query .= "ORDER BY ";
        $query .= "    bscseq ";

        return $query;
    }
    //表示用データを取得
    function readQuery_Kihon($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.grade || T1.hr_class GR_CL, ";
        $query .= "    T2.daycd || T2.periodcd DAYCD, ";
        $query .= "    T2.chaircd,  ";
        $query .= "    T2.groupcd,  ";
        $query .= "    T2.subclasscd,  ";
        $query .= "    T2.staffcd,   ";
        $query .= "    T3.staffname_show,   ";
        $query .= "    T4.subclassabbv   ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat T1  INNER JOIN (SELECT ";
        $query .= "                                    w1.daycd, ";
        $query .= "                                    w1.periodcd, ";
        $query .= "                                    w4.chaircd, ";
        $query .= "                                    w4.groupcd, ";
        $query .= "                                    w4.subclasscd, ";
        $query .= "                                    w5.staffcd, ";
        $query .= "                                    case when value(w4.groupcd,'0000') > '0000'  ";
        $query .= "                                         then w7.trgtgrade || w7.trgtclass  ";
        $query .= "                                         else w6.trgtgrade || w6.trgtclass  ";
        $query .= "                                         end as GR_CL ";
        $query .= "                                FROM ";
        $query .= "                                    sch_ptrn_dat  w1, ";
        $query .= "                                    v_name_mst    w3, ";
        $query .= "                                    chair_stf_dat w5, ";
        $query .= "                                    chair_dat     w4  ";
        $query .= "                                    LEFT JOIN (SELECT * FROM chair_cls_dat ";
        $query .= "                                    			   WHERE year='".CTRL_YEAR."' ";
        $query .= "                                    			     AND semester='".$model->semester."' ";
        $query .= "                                    			     AND groupcd='0000' ";
        $query .= "                                    			  ) w6 ON w6.chaircd=w4.chaircd  ";
        $query .= "                                    LEFT JOIN (SELECT * FROM chair_cls_dat ";
        $query .= "                                    			   WHERE year='".CTRL_YEAR."' ";
        $query .= "                                    			     AND semester='".$model->semester."' ";
        $query .= "                                    			     AND groupcd>'0000' ";
        $query .= "                                    			  ) w7 ON w7.groupcd=w4.groupcd  ";
        $query .= "                                WHERE ";
        $query .= "                                    w1.year     = '".CTRL_YEAR."' AND ";
        $query .= "                                    w1.semester = '".$model->semester."' AND ";
        $query .= "                                    w1.bscseq   = ".$model->seq." AND ";
        $query .= "                                    w3.year     = w1.year       AND ";
        $query .= "                                    w3.namecd1  = 'B001'        AND ";
        $query .= "                                    w3.namecd2  = w1.periodcd   AND ";
        $query .= "                                    w4.year     = w1.year       AND ";
        $query .= "                                    w4.semester = w1.semester   AND ";
        $query .= "                                    w4.chaircd  = w1.chaircd    AND ";
        $query .= "                                    w5.year     = w4.year       AND ";
        $query .= "                                    w5.semester = w4.semester   AND ";
        $query .= "                                    w5.chaircd  = w4.chaircd ";
        $query .= "                                ) T2 ON T2.GR_CL = T1.grade || T1.hr_class ";
        $query .= "                    LEFT JOIN (SELECT ";
        $query .= "                                    staffcd, ";
        $query .= "                                    staffname_show ";
        $query .= "                                FROM ";
        $query .= "                                    v_staff_mst ";
        $query .= "                                WHERE ";
        $query .= "                                    year='".CTRL_YEAR."' ";
        $query .= "                                ) T3 ON T3.staffcd = T2.staffcd ";
        $query .= "                    LEFT JOIN (SELECT ";
        $query .= "                                    subclasscd, ";
        $query .= "                                    subclassabbv  ";
        $query .= "                                FROM ";
        $query .= "                                    v_subclass_mst ";
        $query .= "                                WHERE ";
        $query .= "                                    year='".CTRL_YEAR."' ";
        $query .= "                                ) T4 ON T4.subclasscd = T2.subclasscd ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.semester='".$model->semester."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.grade || T1.hr_class,T2.daycd || T2.periodcd,T2.subclasscd ";

        return $query;
    }

	//---------------------------------通常---------------------------------

    //指定日より学期を取得
    function getY2t($date){
        return " VALUES Y2t(date('".$date."'),'N') ";
    }
    //指定日より学期名を取得
    function getY2t_name($date){
        return " VALUES Y2t(date('".$date."'),'C') ";
    }
    //表示用データを取得
    function readQuery_Tujou($OutDate1,$OutDate2,$y2t_seme)
    {
        $query  = "SELECT ";
        $query .= "    T2.GR_CL, ";
        $query .= "    T2.executedate, ";
        $query .= "    T2.periodcd, ";
        $query .= "    T2.chaircd,  ";
        $query .= "    T2.groupcd,  ";
        $query .= "    T2.subclasscd,  ";
        $query .= "    T2.staffcd,   ";
        $query .= "    T2.staffname_show,   ";
        $query .= "    T2.subclassabbv   ";
        $query .= "FROM ";
        $query .= "   (SELECT ";
        $query .= "        w1.executedate, ";
        $query .= "        w1.periodcd, ";
        $query .= "        w4.chaircd, ";
        $query .= "        w4.groupcd, ";
        $query .= "        w4.subclasscd, ";
        $query .= "        w4.subclassabbv, ";
        $query .= "        w5.staffcd, ";
        $query .= "        w5.staffname_show, ";
        $query .= "        case when value(w4.groupcd,'0000') > '0000'  ";
        $query .= "             then w7.GR_CL else w6.GR_CL end as GR_CL ";
        $query .= "    FROM ";
        $query .= "        (SELECT ";
        $query .= "            executedate, ";
        $query .= "            periodcd, ";
        $query .= "            chaircd ";
        $query .= "        FROM ";
        $query .= "            sch_chr_dat ";
        $query .= "        WHERE ";
        $query .= "            executedate between date('".$OutDate1."') AND ";
        $query .= "            date('".$OutDate2."') AND ";
        $query .= "            chaircd > '0000000' AND ";
        $query .= "            periodcd in (SELECT ";
        $query .= "                            namecd2 ";
        $query .= "                        FROM ";
        $query .= "                            v_name_mst ";
        $query .= "                        WHERE ";
        $query .= "                            year='".CTRL_YEAR."' AND ";
        $query .= "                            namecd1='B001' ";
        $query .= "                        ) ";
        $query .= "        )  w1, ";
        $query .= "        (SELECT ";
        $query .= "            w1.chaircd, ";
        $query .= "            w1.staffcd, ";
        $query .= "            w2.staffname_show ";
        $query .= "        FROM ";
        $query .= "            chair_stf_dat w1, ";
        $query .= "            v_staff_mst w2 ";
        $query .= "        WHERE ";
        $query .= "            w1.year='".CTRL_YEAR."' AND ";
        $query .= "            w1.semester='".$y2t_seme."' AND ";
        $query .= "            w1.year=w2.year AND ";
        $query .= "            w1.staffcd=w2.staffcd ";
        $query .= "        ) w5, ";
        $query .= "        (SELECT ";
        $query .= "            w1.chaircd, ";
        $query .= "            w1.groupcd, ";
        $query .= "            w1.subclasscd, ";
        $query .= "            w2.subclassabbv ";
        $query .= "        FROM ";
        $query .= "            chair_dat w1, ";
        $query .= "            v_subclass_mst w2 ";
        $query .= "        WHERE ";
        $query .= "            w1.year='".CTRL_YEAR."' AND ";
        $query .= "            w1.semester='".$y2t_seme."' AND ";
        $query .= "            w1.year=w2.year AND ";
        $query .= "            w1.subclasscd=w2.subclasscd ";
        $query .= "        ) w4  ";
        $query .= "        LEFT JOIN (SELECT ";
        $query .= "            chaircd, ";
        $query .= "            trgtgrade || trgtclass GR_CL ";
        $query .= "        FROM ";
        $query .= "            chair_cls_dat ";
        $query .= "        WHERE ";
        $query .= "            year='".CTRL_YEAR."' AND ";
        $query .= "            semester='".$y2t_seme."' AND ";
        $query .= "            groupcd='0000' AND ";
        $query .= "            trgtgrade || trgtclass in (SELECT ";
        $query .= "                                            grade || hr_class ";
        $query .= "                                        FROM ";
        $query .= "                                            schreg_regd_hdat ";
        $query .= "                                        WHERE ";
        $query .= "                                            year='".CTRL_YEAR."' AND ";
        $query .= "                                            semester='".$y2t_seme."' ";
        $query .= "                                        ) ";
        $query .= "        ) w6 ON w6.chaircd=w4.chaircd  ";
        $query .= "        LEFT JOIN (SELECT ";
        $query .= "            groupcd, ";
        $query .= "            trgtgrade || trgtclass GR_CL ";
        $query .= "        FROM ";
        $query .= "            chair_cls_dat ";
        $query .= "        WHERE ";
        $query .= "            year='".CTRL_YEAR."' AND ";
        $query .= "            semester='".$y2t_seme."' AND ";
        $query .= "            groupcd>'0000' AND ";
        $query .= "            trgtgrade || trgtclass in (SELECT ";
        $query .= "                                            grade || hr_class ";
        $query .= "                                        FROM ";
        $query .= "                                            schreg_regd_hdat ";
        $query .= "                                        WHERE ";
        $query .= "                                            year='".CTRL_YEAR."' AND ";
        $query .= "                                            semester='".$y2t_seme."' ";
        $query .= "                                        ) ";
        $query .= "        ) w7 ON w7.groupcd=w4.groupcd  ";
        $query .= "    WHERE ";
        $query .= "        w4.chaircd = w1.chaircd AND ";
        $query .= "        w5.chaircd = w4.chaircd ";
        $query .= "    ) T2  ";
        $query .= "ORDER BY ";
        $query .= "    T2.GR_CL,T2.executedate,T2.periodcd,T2.subclasscd ";

        return $query;
    }

}
?>
