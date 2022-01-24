<?php

require_once('for_php7.php');

class knje020oQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE020O' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //PROGRAMPATH取得
    function getProgrampath() {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJE020O' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJE020O' ";

        return $query;
    }

    //調査所見データ表示
    function getReportRemark_dat($model)
    {
        if ($model->mode == "grd"){     //卒業
            $t = "GRD_HEXAM_EMPREMARK_DAT";
        }else{
            $t = "HEXAM_EMPREMARK_DAT";
        }

        $query  = "select jobhunt_rec,jobhunt_absence,jobhunt_healthremark,";
        $query .= " jobhunt_recommend,updated from ".$t ." where schregno ='".$model->schregno."'";

        return $query;
    }

    function &getUpdateQuery($model)
    {
        if ($model->mode == "grd"){     //卒業
            $t = "GRD_HEXAM_EMPREMARK_DAT";
        }else{
            $t = "HEXAM_EMPREMARK_DAT";
        }

        $db = Query::dbCheckOut();
        $num = $db->getOne("select COUNT(*) from ".$t ." where schregno = '".$model->schregno."'");

        if($num > "0" ){//データあり

            $model->field["JOBHUNT_HEALTHREMARK"] = (isset($model->check) ? "異常なし" : $model->field["JOBHUNT_HEALTHREMARK"]);

            $data["JOBHUNT_REC"][TEXT]            =    $model->field["JOBHUNT_REC"];
            $data["JOBHUNT_ABSENCE"][TEXT]        =    $model->field["JOBHUNT_ABSENCE"];
            $data["JOBHUNT_HEALTHREMARK"][TEXT]   =    $model->field["JOBHUNT_HEALTHREMARK"];
            $data["JOBHUNT_RECOMMEND"][TEXT]      =    $model->field["JOBHUNT_RECOMMEND"];
            $data["UPDATED"][FUNC]                =    "sysdate()";

            $where = "WHERE SCHREGNO ='".$model->schregno."'";
            $query = Query::updateSQL($data, $t, $where);

        }else{//データ無し

                $model->field["JOBHUNT_HEALTHREMARK"] = (isset($model->check) ? "異常なし" : $model->field["JOBHUNT_HEALTHREMARK"]);

                $data["SCHREGNO"][TEXT]               =    $model->schregno;
                $data["JOBHUNT_REC"][TEXT]            =    $model->field["JOBHUNT_REC"];
                $data["JOBHUNT_ABSENCE"][TEXT]        =    $model->field["JOBHUNT_ABSENCE"];
                $data["JOBHUNT_HEALTHREMARK"][TEXT]   =    $model->field["JOBHUNT_HEALTHREMARK"];
                $data["JOBHUNT_RECOMMEND"][TEXT]      =    $model->field["JOBHUNT_RECOMMEND"];
                $data["UPDATED"][FUNC]                =    "sysdate()";

                $query = Query::insertSQL($data, $t);

        }
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //健康診断詳細データ取得
    function getMedexam_det_dat($model)
    {
        if ($model->mode == "grd"){     //卒業
            $year = $model->grd_year;
        }else{
            $year = CTRL_YEAR;
        }

        $db = Query::dbCheckOut();

        $query  = " select ";
        $query .= "     t1.height, ";
        $query .= "     t1.weight, ";
        $query .= "     t1.r_barevision_mark, ";
        $query .= "     t1.l_barevision_mark, ";
        $query .= "     t1.r_vision_mark, ";
        $query .= "     t1.l_vision_mark,";
        $query .= "     t1.r_ear, ";
        $query .= "     t1.l_ear, ";
        $query .= "     t2.date, ";
        $query .= "     MEISYOU_GET(t1.r_ear,'F010',1) r_ear_name, ";
        $query .= "     MEISYOU_GET(t1.l_ear,'F010',1) l_ear_name ";
        if ($model->mode == "grd"){     //卒業
            $query .= " from grd_medexam_hdat t2,grd_medexam_det_dat t1 ";
        }else{
            $query .= " from medexam_hdat t2,medexam_det_dat t1 ";
        }
        $query .= " where t1.schregno = t2.schregno ";
        $query .= " and t1.year = t2.year ";
        $query .= " and t1.schregno = '" .$model->schregno. "'";
        $query .= " and t1.year = '" .$year. "'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }


    //欠席日数表示
    function getAttendrec($schregno)
    {
        if ($model->mode == "grd"){     //卒業
            $year = $model->grd_year;
        }else{
            $year = CTRL_YEAR;
        }

        $query  = "select coalesce(sick,0)+coalesce(accidentnotice,0)+coalesce(noaccidentnotice,0) as days,year,schoolcd ";
        $query .= " from schreg_attendrec_dat ";
        $query .= " where schregno = '" .$schregno. "'";

        return $query;
    }


    //学年（年次）コンボボックス用(Subform1)
    function selectQueryAnnual($model)
    {
        if ($model->mode == "grd"){     //卒業
            $year = $model->grd_year;
            $semester = $model->grd_semester;
            if ($model->control["学校区分"] == "0"){     //学年制
                $t = "V_GRD_REGDYEAR_GRADE_DAT";
            }else if ($model->control["学校区分"] == "1"){     //単位制
                $t = "V_GRD_REGDYEAR_UNIT_DAT";
            }
        }else{
            $year = CTRL_YEAR;
            $semester = CTRL_SEMESTER;
            if ($model->control["学校区分"] == "0"){     //学年制
                $t = "V_REGDYEAR_GRADE_DAT";
            }else if ($model->control["学校区分"] == "1"){     //単位制
                $t = "V_REGDYEAR_UNIT_DAT";
            }
        }
        $query = " SELECT DISTINCT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "   ".$t." T1 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."'";


        return $query;
    }

    //成績参照画面(Subform1)
    function selectQuerySubForm1($model)
    {

        $query = " SELECT ";
        $query .= "   T1.ANNUAL, ";
#        $query .= "   COALESCE(T1.CLASSNAME,T3.CLASSNAME)       CLASSNAME, ";
#        $query .= "   COALESCE(T1.SUBCLASSNAME,T4.SUBCLASSNAME) SUBCLASSNAME, ";
        //2004-06-03 y.arakaki 略称表示へ変更
        //$query .= "   CASE WHEN T1.CLASSNAME IS NULL OR T1.CLASSNAME = '' THEN T3.CLASSNAME ELSE T1.CLASSNAME END AS CLASSNAME, ";
        $query .= "   CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' THEN T3.CLASSABBV ELSE T1.CLASSABBV END AS CLASSNAME, ";
        $query .= "   CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' THEN T4.SUBCLASSNAME ELSE T1.SUBCLASSNAME END AS SUBCLASSNAME, ";
        $query .= "   T1.VALUATION, ";
        $query .= "   T1.GET_CREDIT, ";
        $query .= "   T1.ADD_CREDIT, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T2.REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd"){     //卒業
            $query .= "   GRD_STUDYREC_DAT T1 ";
        }else{
            $query .= "   SCHREG_STUDYREC_DAT T1 ";
        }
        $query .= "   LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "   LEFT OUTER JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        $query .= "   LEFT OUTER JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0"){
            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
            $query .= "   T1.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   ORDER BY T1.CLASSCD,T1.SUBCLASSCD,T1.YEAR,T1.ANNUAL ";


        return $query;

#        $query = " SELECT ";
#        $query .= "   T1.ANNUAL, ";
#        $query .= "   T1.CLASSNAME, ";
#        $query .= "   T1.SUBCLASSNAME, ";
#        $query .= "   T1.VALUATION, ";
#        $query .= "   T1.GET_CREDIT, ";
#        $query .= "   T1.ADD_CREDIT ";
#        $query .= " FROM ";
#        $query .= "   SCHREG_STUDYREC_DAT T1 LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
#        $query .= "   T1.YEAR = T2.YEAR AND  ";
#        $query .= "   T1.SCHREGNO = T2.SCHREGNO AND  ";
#        $query .= "   T1.CLASSCD = T2.CLASSCD AND  ";
#        $query .= "   T1.SUBCLASSCD = T2.SUBCLASSCD ";
#        $query .= " WHERE ";
#        $query .= "   T1.SCHOOLCD = '0' AND ";
#        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0"){
#            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
#            $query .= "   T1.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
#        }
#        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
# 
#        return $query;
    }

}
?>
