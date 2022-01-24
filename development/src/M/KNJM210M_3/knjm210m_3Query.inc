<?php

require_once('for_php7.php');

class knjm210m_3Query extends Query {

    //--------------------------その他情報--------------------------//
    //追加　その他情報を取得
    function getRow_others($schregno) {
        if($schregno != "") {

            $db = Query::dbCheckOut();

            $query  = " SELECT * ";
            $query .= "   FROM schreg_send_address_dat ";
            $query .= "  WHERE schregno = '".$schregno."' ";
            $query .= "    AND div = '1' ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            Query::dbCheckIn($db);

        }
        return $row;
    }

    //コンボボックスの地区コードを表示
    function getNameMstA020() {
        return "SELECT namecd2, name1 FROM v_name_mst WHERE namecd1 ='A020' AND year = '".CTRL_YEAR."'";
    }

    //--------------------------異動情報--------------------------//

    function getStudentName($schregno)
    {
        return "SELECT schregno,name_show AS name FROM schreg_base_mst WHERE schregno ='".$schregno."'";
    }
    function getList($schregno)
    {

        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.schregno,";
        $query .= "      T1.transfer_sdate,";
        $query .= "      T1.transfer_edate,";
        $query .= "      T1.transfercd,";
        $query .= "      MEISYOU_GET(T1.transfercd,'A004',1) AS transfername,";
        $query .= "      T1.transferreason,";
        $query .= "      T1.transferplace,";
        $query .= "      T1.transferaddr,";
        $query .= "      T1.abroad_classdays,";
        $query .= "      T1.abroad_credits";
        $query .= "  FROM";
        $query .= "      schreg_transfer_dat T1";
        $query .= "  WHERE";
        $query .= "      T1.schregno = '".$schregno."'";
        $query .= "  ORDER BY";
        $query .= "      T1.transfer_sdate,";
        $query .= "      T1.transfercd";

        return $query;
    }

    //--------------------------住所情報--------------------------//

    function getSchregno_name($schregno){
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$schregno."'";
        return $query;
    }
    function getAddress_all($schregno)
    {
        return "SELECT * FROM schreg_address_dat WHERE schregno = '".$schregno."'";
    }
    function List_AreaCd($cd)
    {
        return "SELECT name1 FROM v_name_mst WHERE namecd1 = 'A020' AND namecd2 = '".$cd."' AND year = '".CTRL_YEAR."'";
    }
    function getGuardianAddr($schregno)
    {
        $query  = "SELECT relationship, ";
        $query .= "       guard_name, ";
        $query .= "       guard_kana, ";
        $query .= "       guard_sex, ";
        $query .= "       guard_birthday, ";
        $query .= "       guard_zipcd, ";
        $query .= "       guard_addr1, ";
        $query .= "       guard_addr2, ";
        $query .= "       guard_telno, ";
        $query .= "       guard_faxno, ";
        $query .= "       guard_e_mail, ";
        $query .= "       guard_jobcd, ";
        $query .= "       guard_work_name, ";
        $query .= "       guard_work_telno ";
        $query .= "  FROM guardian_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        return $query;
    }
    function get_name_mst(){

        $query  = "SELECT namecd1, ";
        $query .= "       namecd2, ";
        $query .= "       name1 ";
        $query .= "  FROM v_name_mst";
        $query .= " WHERE namecd1 IN ('Z002','H201','H202') ";
        $query .= "   AND year = '".CTRL_YEAR."'";
        $query .= "ORDER BY namecd1,namecd2";
        return $query;
    }

    //--------------------------基本情報--------------------------//

    //年組,先生名
    function getGrd_ClasQuery($model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      T1.grade || ',' || T1.hr_class AS gc,";
        $query .= "      T1.hr_name,";
        $query .= "      T2.staffname_show AS name_show,";
        $query .= "      T2.staffcd";
        $query .= "  FROM";
        $query .= "      schreg_regd_hdat T1 ";
        $query .= "      LEFT OUTER JOIN staff_mst T2 ON T1.tr_cd1 = T2.staffcd";
        $query .= "  WHERE";
        $query .= "      T1.year     = '".CTRL_YEAR."' AND";
        $query .= "      T1.semester = '".CTRL_SEMESTER."'";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "AND (T1.tr_cd1    = '".STAFFCD."' OR T1.tr_cd2    = '".STAFFCD."' OR T1.tr_cd3    = '".STAFFCD."' OR ";
            $query .= "     T1.subtr_cd1 = '".STAFFCD."' OR T1.subtr_cd2 = '".STAFFCD."' OR T1.subtr_cd3 = '".STAFFCD."')";
        }
        $query .= "  ORDER BY";
        $query .= "      gc";
        return $query;
    }
    //各生徒情報
    function getStudent_data($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.schregno, T2.annual, T1.inoutcd,";
        $query .= "      T1.name, T1.name_show, T1.name_kana, T1.name_eng,";
        $query .= "      T1.birthday, T1.sex, T1.bloodtype, T1.blood_rh,";
        $query .= "      T1.finschoolcd, T1.finish_date,";
        $query .= "      T1.prischoolcd,";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "      T1.ent_addr, T1.grd_addr, ";
        }
        $query .= "      T1.ent_date, T1.ent_div, T1.ent_school, ";
        $query .= "      T1.grd_date, T1.grd_div, T1.grd_school, T1.grd_no,";
        $query .= "      T1.remark1, T1.remark2, T1.remark3,";
        $query .= "      T2.coursecode,";
        $query .= "      T1.updated AS updated1,";
        $query .= "      T2.attendno,";
        $query .= "      T2.coursecd ||','|| T2.majorcd AS coursemajorcd,";
        $query .= "      T2.updated AS updated2,";
        $query .= "      T2.grade,";
        $query .= "      T2.grade || ',' || T2.hr_class AS grcl";
        $query .= "  FROM";
        $query .= "      schreg_base_mst T1 ";
        $query .= "      LEFT OUTER JOIN schreg_regd_dat T2 ON T1.schregno = T2.schregno";
        $query .= "  WHERE";
        $query .= "      T1.schregno = '". $schregno. "' AND";
        $query .= "      T2.year     = '". CTRL_YEAR. "' AND";
        $query .= "      T2.semester = '". CTRL_SEMESTER. "'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }
    //入学事由取得
    function getEnt_reason($schregno)
    {
        return "SELECT ent_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }
    //卒業事由取得
    function getGrd_reason($schregno)
    {
        return "SELECT grd_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }
    //課程学科コンボ
    function getCourse_Subject()
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      coursecd || ',' || majorcd AS coursemajorcd,";
        $query .= "      coursename || majorabbv AS course_subject";
        $query .= "  FROM";
        $query .= "      v_course_major_mst";
        $query .= "  WHERE";
        $query .= "      year = '".CTRL_YEAR."'";
        $query .= "  ORDER BY";
        $query .= "      coursemajorcd";
        return $query;
    }
    //出身中学校コンボ
    function getFinschoolName()
    {
        return "SELECT finschoolcd, finschool_name FROM v_finschool_mst WHERE year = '".CTRL_YEAR. "' ORDER BY finschoolcd ";
    }
    //出身塾コンボ
    function getPrischoolName()
    {
        $query  = " SELECT prischoolcd, prischool_name ";
        $query .= " FROM v_prischool_mst ";
        $query .= " WHERE year = '".CTRL_YEAR. "'";
        $query .= " ORDER BY prischoolcd";
        return $query;
    }
    //コースコード取得
    function getCourseCode()
    {
        $query  = " SELECT coursecode, coursecodename ";
        $query .= "   FROM v_coursecode_mst ";
        $query .= " WHERE year     = '". CTRL_YEAR ."'";
        $query .= " ORDER BY coursecode";
        return $query;
    }    
    //V_NAME_MSTから情報を取得
    function getNameMst_data($cd)
    {
        $query  = " SELECT * FROM v_name_mst ";
        $query .= " WHERE namecd1 ='" .$cd. "' AND year='".CTRL_YEAR."'";
        $query .= " ORDER BY namecd1, namecd2";
        return $query;
    }
}
?>
