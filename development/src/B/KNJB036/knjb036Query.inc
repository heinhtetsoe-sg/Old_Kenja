<?php

require_once('for_php7.php');

class knjb036Query extends Query {

	//---------------------------------共通---------------------------------

    //施設を取得
    function getFacility()
    {
        $query  = "SELECT ";
        $query .= "    faccd, ";
        $query .= "    facilityname ";
        $query .= "FROM ";
        $query .= "    v_facility_mst ";
        $query .= "WHERE ";
        $query .= "    year='".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    faccd ";

        return $query;
    }
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
        $query .= "    T1.namecd2, ";
        $query .= "    T1.name1, ";
        $query .= "    T2.periodcd, ";
        $query .= "    T2.daycd, ";
        $query .= "    T2.chaircd, ";
        $query .= "    T2.groupcd,  ";
        $query .= "    case when value(T2.groupcd,'0000') > '0000'  ";
        $query .= "         then T4.hr_nameabbv_grp  ";
        $query .= "         else T3.hr_nameabbv_chr  ";
        $query .= "         end as hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    v_name_mst T1  INNER JOIN (SELECT ";
        $query .= "                                    w1.daycd, ";
        $query .= "                                    w1.periodcd, ";
        $query .= "                                    w1.chaircd, ";
        $query .= "                                    w4.groupcd ";
        $query .= "                                FROM ";
        $query .= "                                    sch_ptrn_dat  w1, ";
        $query .= "                                    chair_fac_dat w2, ";
        $query .= "                                    v_name_mst    w3, ";
        $query .= "                                    chair_dat     w4  ";
        $query .= "                                WHERE ";
        $query .= "                                    w1.year     = '".CTRL_YEAR."' AND ";
        $query .= "                                    w1.semester = '".$model->semester."' AND ";
        $query .= "                                    w1.bscseq   = ".$model->seq." AND ";
        $query .= "                                    w2.year     = w1.year       AND ";
        $query .= "                                    w2.semester = w1.semester   AND ";
        $query .= "                                    w2.faccd    = '".$model->faccd."' AND ";
        $query .= "                                    w2.chaircd  = w1.chaircd    AND ";
        $query .= "                                    w3.year     = w1.year       AND ";
        $query .= "                                    w3.namecd1  = 'B001'        AND ";
        $query .= "                                    w3.namecd2  = w1.periodcd   AND ";
        $query .= "                                    w4.year     = w1.year       AND ";
        $query .= "                                    w4.semester = w1.semester   AND ";
        $query .= "                                    w4.chaircd  = w1.chaircd ";
        $query .= "                                ) T2 ON T2.periodcd = T1.namecd2 ";
        $query .= "                    LEFT JOIN (SELECT ";
        $query .= "                                    w2.hr_nameabbv as hr_nameabbv_chr, ";
        $query .= "                                    w1.chaircd ";
        $query .= "                                FROM ";
        $query .= "                                    (SELECT ";
        $query .= "                                        MIN(trgtgrade || trgtclass) GR_CL, ";
        $query .= "                                        chaircd ";
        $query .= "                                    FROM ";
        $query .= "                                        chair_cls_dat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$model->semester."' AND ";
        $query .= "                                        groupcd='0000' ";
        $query .= "                                    GROUP BY ";
        $query .= "                                        chaircd ";
        $query .= "                                    ) w1, ";
        $query .= "                                    (SELECT ";
        $query .= "                                        grade || hr_class GR_CL, ";
        $query .= "                                        hr_nameabbv ";
        $query .= "                                    FROM ";
        $query .= "                                        schreg_regd_hdat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$model->semester."' ";
        $query .= "                                    ) w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.GR_CL=w1.GR_CL ";
        $query .= "                                ) T3 ON T3.chaircd = T2.chaircd ";
        $query .= "                    LEFT JOIN (SELECT ";
        $query .= "                                    w2.hr_nameabbv || '*' as hr_nameabbv_grp, ";
        $query .= "                                    w1.groupcd ";
        $query .= "                                FROM ";
        $query .= "                                    (SELECT ";
        $query .= "                                        MIN(trgtgrade || trgtclass) GR_CL, ";
        $query .= "                                        groupcd ";
        $query .= "                                    FROM ";
        $query .= "                                        chair_cls_dat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$model->semester."' AND ";
        $query .= "                                        groupcd>'0000' ";
        $query .= "                                    GROUP BY ";
        $query .= "                                        groupcd ";
        $query .= "                                    ) w1, ";
        $query .= "                                    (SELECT ";
        $query .= "                                        grade || hr_class GR_CL, ";
        $query .= "                                        hr_nameabbv ";
        $query .= "                                    FROM ";
        $query .= "                                        schreg_regd_hdat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$model->semester."' ";
        $query .= "                                    ) w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.GR_CL=w1.GR_CL ";
        $query .= "                                ) T4 ON T4.groupcd = T2.groupcd ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.namecd1='B001' ";
        $query .= "ORDER BY ";
        $query .= "    T1.namecd2,T2.daycd ";

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
    function readQuery_Tujou($model,$OutDate1,$OutDate2,$y2t_seme)
    {
        $query  = "SELECT ";
        $query .= "    T1.namecd2, ";
        $query .= "    T1.name1, ";
        $query .= "    T2.periodcd, ";
        $query .= "    T2.executedate, ";
        $query .= "    T2.chaircd, ";
        $query .= "    T2.groupcd,  ";
        $query .= "    case when value(T2.groupcd,'0000') > '0000'  ";
        $query .= "         then T4.hr_nameabbv_grp  ";
        $query .= "         else T3.hr_nameabbv_chr  ";
        $query .= "         end as hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    v_name_mst T1  INNER JOIN (SELECT ";
        $query .= "                                    w1.executedate, ";
        $query .= "                                    w1.periodcd, ";
        $query .= "                                    w1.chaircd, ";
        $query .= "                                    w4.groupcd ";
        $query .= "                                FROM ";
        $query .= "                                    sch_chr_dat w1, ";
        $query .= "                                    sch_fac_dat w2, ";
        $query .= "                                    v_name_mst  w3, ";
        $query .= "                                    chair_dat   w4  ";
        $query .= "                                WHERE ";
        $query .= "                                    w1.executedate between date('".$OutDate1."') and date('".$OutDate2."') AND ";
        $query .= "                                    w1.chaircd > '0000000' AND ";
        $query .= "                                    w2.executedate = w1.executedate AND ";
        $query .= "                                    w2.faccd    = '".$model->faccd."' AND ";
        $query .= "                                    w2.periodcd = w1.periodcd   AND ";
        $query .= "                                    w2.chaircd  = w1.chaircd    AND ";
        $query .= "                                    w3.year     = '".CTRL_YEAR."' AND ";
        $query .= "                                    w3.namecd1  = 'B001'        AND ";
        $query .= "                                    w3.namecd2  = w1.periodcd   AND ";
        $query .= "                                    w4.year     = '".CTRL_YEAR."' AND ";
        $query .= "                                    w4.semester = '".$y2t_seme."' AND ";
        $query .= "                                    w4.chaircd  = w1.chaircd ";
        $query .= "                                ) T2 ON T2.periodcd = T1.namecd2 ";
        $query .= "                    LEFT JOIN (SELECT ";
        $query .= "                                    w2.hr_nameabbv as hr_nameabbv_chr, ";
        $query .= "                                    w1.chaircd ";
        $query .= "                                FROM ";
        $query .= "                                    (SELECT ";
        $query .= "                                        MIN(trgtgrade || trgtclass) GR_CL, ";
        $query .= "                                        chaircd ";
        $query .= "                                    FROM ";
        $query .= "                                        chair_cls_dat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$y2t_seme."' AND ";
        $query .= "                                        groupcd='0000' ";
        $query .= "                                    GROUP BY ";
        $query .= "                                        chaircd ";
        $query .= "                                    ) w1, ";
        $query .= "                                    (SELECT ";
        $query .= "                                        grade || hr_class GR_CL, ";
        $query .= "                                        hr_nameabbv ";
        $query .= "                                    FROM ";
        $query .= "                                        schreg_regd_hdat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$y2t_seme."' ";
        $query .= "                                    ) w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.GR_CL=w1.GR_CL ";
        $query .= "                                ) T3 ON T3.chaircd = T2.chaircd ";
        $query .= "                    LEFT JOIN (SELECT ";
        $query .= "                                    w2.hr_nameabbv || '*' as hr_nameabbv_grp, ";
        $query .= "                                    w1.groupcd ";
        $query .= "                                FROM ";
        $query .= "                                    (SELECT ";
        $query .= "                                        MIN(trgtgrade || trgtclass) GR_CL, ";
        $query .= "                                        groupcd ";
        $query .= "                                    FROM ";
        $query .= "                                        chair_cls_dat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$y2t_seme."' AND ";
        $query .= "                                        groupcd>'0000' ";
        $query .= "                                    GROUP BY ";
        $query .= "                                        groupcd ";
        $query .= "                                    ) w1, ";
        $query .= "                                    (SELECT ";
        $query .= "                                        grade || hr_class GR_CL, ";
        $query .= "                                        hr_nameabbv ";
        $query .= "                                    FROM ";
        $query .= "                                        schreg_regd_hdat ";
        $query .= "                                    WHERE ";
        $query .= "                                        year='".CTRL_YEAR."' AND ";
        $query .= "                                        semester='".$y2t_seme."' ";
        $query .= "                                    ) w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.GR_CL=w1.GR_CL ";
        $query .= "                                ) T4 ON T4.groupcd = T2.groupcd ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.namecd1='B001' ";
        $query .= "ORDER BY ";
        $query .= "    T1.namecd2,T2.executedate ";

        return $query;
    }

}
?>
