<?php

require_once('for_php7.php');

class knjb0070Query extends Query {

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
    function getGrCl_hdat($semester, $model)
    {
        $query  = " SELECT ";
        $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS gr_cl, ";
        $query .= "     REGD_H.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_H.GRADE IN (SELECT ";
            $query .= "                              REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                              SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS ";

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
/*********************---SQL処理速度改善 2004-08-17 naka
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
        $query .= "                                                WHERE year='".CTRL_YEAR."' ";
        $query .= "                                                  AND semester='".$model->semester."' ";
        $query .= "                                                  AND groupcd='0000' ";
        $query .= "                                               ) w6 ON w6.chaircd=w4.chaircd  ";
        $query .= "                                    LEFT JOIN (SELECT * FROM chair_cls_dat ";
        $query .= "                                                WHERE year='".CTRL_YEAR."' ";
        $query .= "                                                  AND semester='".$model->semester."' ";
        $query .= "                                                  AND groupcd>'0000' ";
        $query .= "                                               ) w7 ON w7.groupcd=w4.groupcd  ";
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
***************************/
        //2005.08.18Modify
        $query  = "SELECT DISTINCT ";
        $query .= "    w4.GR_CL, w1.daycd || w1.periodcd DAYCD, w4.chaircd, w4.groupcd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ";
        }
        $query .= "    w4.subclasscd, w7.subclassabbv, ";
        $query .= "    case when w9.staffcd is not null then w9.staffcd else w5.staffcd end as staffcd, ";
        $query .= "    case when w9.staffcd is not null then w9.staffname_show else w6.staffname_show end as staffname_show ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        T1.daycd, T1.periodcd, T1.chaircd, T2.STAFFCD ";
        $query .= "    FROM ";
        $query .= "        sch_ptrn_dat T1 ";
        $query .= "        LEFT JOIN SCH_PTRN_STF_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND ";
        $query .= "                                         T2.BSCSEQ = T1.BSCSEQ AND T2.DAYCD = T1.DAYCD AND ";
        $query .= "                                         T2.PERIODCD = T1.PERIODCD AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "    WHERE ";
        $query .= "        T1.year ='".CTRL_YEAR."' AND T1.semester ='".$model->semester."' AND T1.bscseq =".$model->seq." ";
        $query .= "    ) w1 LEFT JOIN staff_mst w9 ON w9.staffcd = w1.staffcd, ";
/***************************---2005.08.18Modify
        $query  = "SELECT ";
        $query .= "    w4.GR_CL, w1.daycd || w1.periodcd DAYCD, w4.chaircd, w4.groupcd, ";
        $query .= "    w4.subclasscd, w5.staffcd, w6.staffname_show, w7.subclassabbv ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        daycd, periodcd, chaircd ";
        $query .= "    FROM ";
        $query .= "        sch_ptrn_dat ";
        $query .= "    WHERE ";
        $query .= "        year ='".CTRL_YEAR."' AND semester ='".$model->semester."' AND bscseq =".$model->seq." ";
        $query .= "    ) w1, ";
***************************/
        $query .= "    (SELECT ";
        $query .= "        chaircd, staffcd ";
        $query .= "    FROM ";
        $query .= "        chair_stf_dat ";
        $query .= "    WHERE ";
        $query .= "        year = '".CTRL_YEAR."' AND semester = '".$model->semester."' ";
        $query .= "    ) w5 LEFT JOIN v_staff_mst w6 ON w6.year='".CTRL_YEAR."' and w6.staffcd = w5.staffcd, ";
        $query .= "    (SELECT ";
        $query .= "        k2.chaircd, k2.groupcd, k2.subclasscd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    K2.CLASSCD, K2.SCHOOL_KIND, K2.CURRICULUM_CD, ";
        }
        $query .= "        k1.trgtgrade || k1.trgtclass as GR_CL ";
        $query .= "    FROM ";
        $query .= "        chair_cls_dat k1, chair_dat k2 ";
        $query .= "    WHERE ";
        $query .= "        k1.year = k2.year AND k1.semester = k2.semester AND ";
        $query .= "        k1.chaircd = '0000000' AND k1.year = '".CTRL_YEAR."' AND ";
        $query .= "        k1.semester = '".$model->semester."' AND k1.groupcd = k2.groupcd ";
        $query .= "    UNION  ";
        $query .= "    SELECT ";
        $query .= "        t2.chaircd, t2.groupcd, t2.subclasscd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ";
        }
        $query .= "        t1.trgtgrade || t1.trgtclass as GR_CL ";
        $query .= "    FROM ";
        $query .= "        chair_cls_dat t1, chair_dat t2 ";
        $query .= "    WHERE ";
        $query .= "        t1.year = t2.year AND t1.semester = t2.semester AND ";
        $query .= "        t1.groupcd = t2.groupcd AND t1.year = '".CTRL_YEAR."' AND ";
        $query .= "        t1.semester = '".$model->semester."' AND t1.chaircd = t2.chaircd ";
        $query .= "    ) w4 LEFT JOIN v_subclass_mst w7 ON w7.year='".CTRL_YEAR."' and w7.subclasscd = w4.subclasscd ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    AND W7.CLASSCD = W4.CLASSCD AND W7.SCHOOL_KIND = W4.SCHOOL_KIND AND W7.CURRICULUM_CD = W4.CURRICULUM_CD ";
        }
        $query .= "    ,v_name_mst w3 ";
        $query .= "WHERE ";
        $query .= "    w3.year = '".CTRL_YEAR."' AND w3.namecd1 = 'B001' AND ";
        $query .= "    w3.namecd2 = w1.periodcd AND w4.chaircd = w1.chaircd AND w5.chaircd = w4.chaircd ";
        $query .= "ORDER BY ";
        $query .= "    w4.GR_CL, w1.daycd || w1.periodcd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ";
        }
        $query .= "    w4.subclasscd ";

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
    function readQuery_Tujou($OutDate1,$OutDate2,$y2t_seme, $model)
    {
/*********************---SQL処理速度改善 2004-08-17 naka
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
***************************/
        //2005.08.18Modify
        $query .= "SELECT DISTINCT ";
        $query .= "    w4.GR_CL, w1.executedate, w1.periodcd, w4.chaircd, w4.groupcd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ";
        }
        $query .= "    w4.subclasscd, w7.subclassabbv, ";
        $query .= "    case when w9.staffcd is not null then w9.staffcd else w5.staffcd end as staffcd, ";
        $query .= "    case when w9.staffcd is not null then w9.staffname_show else w5.staffname_show end as staffname_show ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        T1.executedate, T1.periodcd, T1.chaircd, T2.STAFFCD ";
        $query .= "    FROM ";
        $query .= "        sch_chr_dat T1 ";
        $query .= "        LEFT JOIN SCH_STF_DAT T2 ON T2.EXECUTEDATE = T1.EXECUTEDATE AND ";
        $query .= "                                    T2.PERIODCD = T1.PERIODCD AND ";
        $query .= "                                    T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "    WHERE ";
        $query .= "        T1.executedate between date('".$OutDate1."') AND date('".$OutDate2."') AND ";
        $query .= "        T1.periodcd in (SELECT namecd2 FROM v_name_mst ";
        $query .= "                        WHERE year='".CTRL_YEAR."' AND namecd1='B001') ";
        $query .= "    )  w1 LEFT JOIN staff_mst w9 ON w9.staffcd = w1.staffcd, ";
/***************************---2005.08.18Modify
        $query  = "SELECT  ";
        $query .= "    w4.GR_CL, w1.executedate, w1.periodcd, w4.chaircd, w4.groupcd, ";
        $query .= "    w4.subclasscd, w7.subclassabbv, w5.staffcd, w5.staffname_show ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        executedate, periodcd, chaircd ";
        $query .= "    FROM ";
        $query .= "        sch_chr_dat ";
        $query .= "    WHERE ";
        $query .= "        executedate between date('".$OutDate1."') AND date('".$OutDate2."') AND ";
        $query .= "        periodcd in (SELECT namecd2 FROM v_name_mst ";
        $query .= "                    WHERE year='".CTRL_YEAR."' AND namecd1='B001') ";
        $query .= "    )  w1, ";
***************************/
        $query .= "    (SELECT ";
        $query .= "        w1.chaircd, w1.staffcd, w2.staffname_show ";
        $query .= "    FROM ";
        $query .= "        chair_stf_dat w1, v_staff_mst w2 ";
        $query .= "    WHERE ";
        $query .= "        w1.year='".CTRL_YEAR."' AND w1.semester='".$y2t_seme."' AND ";
        $query .= "        w1.year=w2.year AND w1.staffcd=w2.staffcd ";
        $query .= "    ) w5, ";
        $query .= "    (SELECT ";
        $query .= "        k2.chaircd, k2.groupcd, k2.subclasscd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    K2.CLASSCD, K2.SCHOOL_KIND, K2.CURRICULUM_CD, ";
        }
        $query .= "        k1.trgtgrade || k1.trgtclass as GR_CL ";
        $query .= "    FROM ";
        $query .= "        chair_cls_dat k1, chair_dat k2 ";
        $query .= "    WHERE ";
        $query .= "        k1.year = k2.year AND k1.semester = k2.semester AND ";
        $query .= "        k1.chaircd = '0000000' AND k1.year = '".CTRL_YEAR."' AND ";
        $query .= "        k1.semester = '".$y2t_seme."' AND k1.groupcd = k2.groupcd ";
        $query .= "    UNION  ";
        $query .= "    SELECT ";
        $query .= "        t2.chaircd, t2.groupcd, t2.subclasscd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ";
        }
        $query .= "        t1.trgtgrade || t1.trgtclass as GR_CL ";
        $query .= "    FROM ";
        $query .= "        chair_cls_dat t1, chair_dat t2 ";
        $query .= "    WHERE ";
        $query .= "        t1.year = t2.year AND t1.semester = t2.semester AND ";
        $query .= "        t1.groupcd = t2.groupcd AND t1.year = '".CTRL_YEAR."' AND ";
        $query .= "        t1.semester = '".$y2t_seme."' AND t1.chaircd = t2.chaircd ";
        $query .= "    ) w4 LEFT JOIN v_subclass_mst w7 ON w7.year='".CTRL_YEAR."' and w7.subclasscd = w4.subclasscd ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    AND W7.CLASSCD = W4.CLASSCD AND W7.SCHOOL_KIND = W4.SCHOOL_KIND AND W7.CURRICULUM_CD = W4.CURRICULUM_CD ";
        }
        $query .= "WHERE ";
        $query .= "    w4.chaircd = w1.chaircd AND w5.chaircd = w4.chaircd ";
        $query .= "ORDER BY ";
        $query .= "    w4.GR_CL, w1.executedate, w1.periodcd, ";
        if ('1' == $model->Properties["useCurriculumcd"]) {
            $query .= "    W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ";
        }
        $query .= "    w4.subclasscd ";

        return $query;
    }

}
?>
