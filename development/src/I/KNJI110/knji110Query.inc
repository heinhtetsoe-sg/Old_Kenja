<?php

require_once('for_php7.php');

class knji110Query extends Query {

    //職員リストコンボ用取得
    function getStaff($year, $model)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        
        $query  = "SELECT DISTINCT staffcd, staffname_show as staffname, staffname_kana as kana";
        $query .= "  FROM staff_mst ";
        $query .= " WHERE ";
        $query .= " chargeclasscd = '1'";
        $query .= " ORDER BY kana";

        list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
        $result = $db->query($query);
        $opt[] = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $row["FUSE_STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
            } else {
                $row["FUSE_STAFFCD"] = $row["STAFFCD"];
            }
            $opt[] = array("label" => $row["FUSE_STAFFCD"]."  ".$row["STAFFNAME"],
                           "value" => $row["STAFFCD"]);
        }
        Query::dbCheckIn($db);

        return $opt;
    }
    //ＨＲ施設リストコンボ用取得
    function getFacility()
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = "SELECT faccd, facilityname FROM v_facility_mst";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        $query .= " ORDER BY faccd";
        $result = $db->query($query);

        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["FACCD"]."  ".$row["FACILITYNAME"],
                           "value" => $row["FACCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }

    //１レコード取得
    function getRow($model, $term, $grade, $hr_class)
    {
        $db  = Query::dbCheckOut();

        $query  = "SELECT T1.* FROM GRD_REGD_HDAT T1 ";
        $query .= " WHERE T1.year   = '".SUBSTR($term,0,4)."'";
        $query .= " AND semester = '".SUBSTR($term,5)."'";
        $query .= " AND T1.grade    = '".$grade."'";
        $query .= " AND hr_class = '".$hr_class."'"; 

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //参照学期
    function SelectYear($model)
    {

        $query  = "SELECT DISTINCT T1.YEAR,SEMESTER FROM GRD_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY T1.YEAR DESC,SEMESTER DESC ";

        return $query;
    }

    //職員一覧
    function SelectList($model, $term)
    {
        $query  = " SELECT ";
        $query .= "     x.grade, ";
        $query .= "     x.hr_class, ";
        $query .= "     x.hr_name, ";
        $query .= "     x.hr_nameabbv, ";
        $query .= "     x.hr_class_name1, ";
        $query .= "     x.hr_class_name2, ";
        $query .= "     x.grade_name, ";
        $query .= "     x.tr_cd1, ";
        $query .= "     a.staffname_show name1, ";
        $query .= "     x.tr_cd2, ";
        $query .= "     b.staffname_show name2, ";
        $query .= "     x.tr_cd3, ";
        $query .= "     c.staffname_show name3, ";
        $query .= "     x.subtr_cd1, ";
        $query .= "     d.staffname_show name4, ";
        $query .= "     x.subtr_cd2, ";
        $query .= "     e.staffname_show name5, ";
        $query .= "     x.subtr_cd3, ";
        $query .= "     f.staffname_show name6, ";
        $query .= "     x.hr_faccd, ";
        $query .= "     n.facilityname, ";
        $query .= "     x.classweeks, ";
        $query .= "     x.classdays ";
        $query .= " FROM ";
        $query .= " ((((((GRD_REGD_HDAT x LEFT OUTER JOIN v_staff_mst a ON x.tr_cd1 = a.staffcd AND x.year = a.year)";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = x.YEAR AND T2.GRADE = x.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = x.YEAR AND T2.GRADE = x.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT OUTER JOIN v_staff_mst    b ON x.tr_cd2    = b.staffcd AND x.year = b.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    c ON x.tr_cd3    = c.staffcd AND x.year = c.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    d ON x.subtr_cd1 = d.staffcd AND x.year = d.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    e ON x.subtr_cd2 = e.staffcd AND x.year = e.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    f ON x.subtr_cd3 = f.staffcd AND x.year = f.year)";
        $query .= " LEFT OUTER JOIN v_facility_mst n ON x.hr_faccd  = n.faccd   AND x.year = n.year";
        $query .= " WHERE x.year     = '".SUBSTR($term,0,4)."'";
        $query .= "   AND x.semester = '".SUBSTR($term,5)."'";
        $query .= " ORDER BY x.grade, x.hr_class ";
        return $query;
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]       = substr($model->term,5);
        $data["GRADE"][TEXT]          = $model->fields["GRADE"];
        $data["HR_CLASS"][TEXT]       = $model->fields["HR_CLASS"];
        $data["HR_NAME"][TEXT]        = $model->fields["HR_NAME"];
        $data["HR_NAMEABBV"][TEXT]    = $model->fields["HR_NAMEABBV"];
        $data["HR_CLASS_NAME1"][TEXT] = $model->fields["HR_CLASS_NAME1"];
        $data["HR_CLASS_NAME2"][TEXT] = $model->fields["HR_CLASS_NAME2"];
        $data["GRADE_NAME"][TEXT]     = $model->fields["GRADE_NAME"];
        $data["TR_CD1"][TEXT]         = $model->fields["TR_CD1"];
        $data["TR_CD2"][TEXT]         = $model->fields["TR_CD2"];
        $data["TR_CD3"][TEXT]         = $model->fields["TR_CD3"];
        $data["SUBTR_CD1"][TEXT]      = $model->fields["SUBTR_CD1"];
        $data["SUBTR_CD2"][TEXT]      = $model->fields["SUBTR_CD2"];
        $data["SUBTR_CD3"][TEXT]      = $model->fields["SUBTR_CD3"];
        $data["HR_FACCD"][TEXT]       = $model->fields["HR_FACCD"];
        $data["CLASSWEEKS"][NUMBER]   = $model->fields["CLASSWEEKS"];
        $data["CLASSDAYS"][NUMBER]    = $model->fields["CLASSDAYS"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "SYSDATE()";

        $query = Query::insertSQL($data, "GRD_REGD_HDAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]       = substr($model->term,5);
        $data["GRADE"][TEXT]          = $model->fields["GRADE"];
        $data["HR_CLASS"][TEXT]       = $model->fields["HR_CLASS"];
        $data["HR_NAME"][TEXT]        = $model->fields["HR_NAME"];
        $data["HR_NAMEABBV"][TEXT]    = $model->fields["HR_NAMEABBV"];
        $data["HR_CLASS_NAME1"][TEXT] = $model->fields["HR_CLASS_NAME1"];
        $data["HR_CLASS_NAME2"][TEXT] = $model->fields["HR_CLASS_NAME2"];
        $data["GRADE_NAME"][TEXT]     = $model->fields["GRADE_NAME"];
        $data["TR_CD1"][TEXT]         = $model->fields["TR_CD1"];
        $data["TR_CD2"][TEXT]         = $model->fields["TR_CD2"];
        $data["TR_CD3"][TEXT]         = $model->fields["TR_CD3"];
        $data["SUBTR_CD1"][TEXT]      = $model->fields["SUBTR_CD1"];
        $data["SUBTR_CD2"][TEXT]      = $model->fields["SUBTR_CD2"];
        $data["SUBTR_CD3"][TEXT]      = $model->fields["SUBTR_CD3"];
        $data["HR_FACCD"][TEXT]       = $model->fields["HR_FACCD"];
        $data["CLASSWEEKS"][NUMBER]   = $model->fields["CLASSWEEKS"];
        $data["CLASSDAYS"][NUMBER]    = $model->fields["CLASSDAYS"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        $where  = " WHERE year   = '" .SUBSTR($model->term,0,4)."'";
        $where .= " AND semester = '".SUBSTR($model->term,5)."'";
        $where .= " AND grade    = '".$model->grade."'";
        $where .= " AND hr_class = '".$model->hr_class."'";

        $query = Query::updateSQL($data, "GRD_REGD_HDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        //生徒が割り振られているかチェック
        $query  = "SELECT COUNT(*) FROM class_formation_dat t1 ";
        $query .= " WHERE t1.year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND t1.grade    = '".$model->grade."'";
        $query .= "   AND hr_class = '".$model->hr_class."'";
        if ($db->getOne($query) > 0) {
            return "class_formation";
        }
        //生徒が在籍しているかチェック
        $query  = "SELECT COUNT(*) FROM schreg_regd_dat t1 ";
        $query .= " WHERE t1.YEAR     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND t1.grade    = '".$model->grade."'";
        $query .= "   AND hr_class = '".$model->hr_class."'";
        if ($db->getOne($query) > 0) {
            return "schreg_regd";
        }
        $query  = "DELETE FROM GRD_REGD_HDAT T1 ";
        $query .= " WHERE t1.year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND t1.grade    = '".$model->grade."'";
        $query .= "   AND hr_class = '".$model->hr_class."'";
        $db->query($query);
        Query::dbCheckIn($db);
        return "succeeded";
    }

    //「コピーボタン」押し時に,クラス編成データに生徒が割り振られているかチェック
    function Count($model, $term)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) FROM class_formation_dat t1 ";
        $query .=" WHERE t1.year ='".SUBSTR($term,0,4)."' AND semester='".SUBSTR($term,5,1)."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= " )";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " )";
        }

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }

    //学籍ヘッダーデータの設定学期のデータを削除
    function Delete_Hdat($model, $term)
    {
        $query  ="DELETE FROM GRD_REGD_HDAT t1 ";
        $query .=" WHERE t1.year     ='".SUBSTR($term,0,4)."'";
        $query .="   AND semester ='".SUBSTR($term,5,1)."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= " )";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " )";
        }

        return $query;
    }

    //学籍在籍ヘッダデータの参照学期のデータを設定学期にインサート
    function Insert_Hdat($model, $term2,$term)
    {
        $query  =" INSERT INTO GRD_REGD_HDAT ";
        $query .=" SELECT ";
        $query .="      '".SUBSTR($term,0,4)."', ";
        $query .="      '".SUBSTR($term,5,1)."', ";
        $query .="      t1.grade, ";
        $query .="      hr_class, ";
        $query .="      hr_name, ";
        $query .="      hr_nameabbv, ";
        $query .="      hr_class_name1, ";
        $query .="      hr_class_name2, ";
        $query .="      grade_name, ";
        $query .="      hr_faccd,";
        $query .="      tr_cd1, ";
        $query .="      tr_cd2, ";
        $query .="      tr_cd3, ";
        $query .="      subtr_cd1, ";
        $query .="      subtr_cd2, ";
        $query .="      subtr_cd3, ";
        $query .="      classweeks, ";
        $query .="      classdays, ";
        $query .="      registercd, SYSDATE() ";
        $query .="   FROM ";
        $query .="      GRD_REGD_HDAT T1 ";
        $query .="  WHERE ";
        $query .="        t1.year = '".SUBSTR($term2,0,4)."' ";
        $query .="    AND semester = '".SUBSTR($term2,5,1)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= " )";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " )";
        }

        return $query;
    }

    //「コピーボタン」押し時の処理
    function getInsertCopyQuery($term2,$term)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query(knji110Query::Delete_Hdat($this, $term));
        $db->query(knji110Query::Insert_Hdat($this, $term2,$term));
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
