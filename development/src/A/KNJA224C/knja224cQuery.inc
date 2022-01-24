<?php

require_once('for_php7.php');


class knja224cQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."' ";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //縦・横サイズのデフォルト値取得
    function getDefaultSize($div)
    {
        $query  = " SELECT ";
        if($div == "HEIGHT"){
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAME2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A026' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //CSV出力
    function selectCsvQuery($grd_cls, $model)
    {

        $query  = "SELECT ";
        $query .= "    w3.grade || w3.hr_class as grade_hr_class,";
        $query .= "    smallint(w3.hr_class) as hr_class,";
        $query .= "    smallint(w3.attendno) as attendno,";
        //文京の場合、性別の＊を表記しない。
        if ($model->schoolName == 'bunkyo') {
            $query .= "    '' AS SEX, ";
        } else {
            $query .= "    CASE WHEN w4.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
        }
        // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）
        if (strlen($model->field["NAME_NASI"])) {
            $query .= "    CASE WHEN w4.GRD_DIV IN ('1','2','3') THEN '' ELSE w4.name END AS NAME, ";
            $query .= "    CASE WHEN w4.GRD_DIV IN ('1','2','3') THEN '' ELSE w4.name_kana END AS NAME_KANA, ";
        } else {
            $query .= "    w4.name,";
            $query .= "    w4.name_kana,";
        }
        $query .= "    s1.finschool_name,";
        $query .= "    w1.hr_nameabbv,";
        $query .= "    w2.staffname, ";
        $query .= "    w4.schregno ";
        $query .= "FROM ";
        $query .= "    schreg_base_mst w4";
        $query .= "    left join finschool_mst s1 on w4.finschoolcd = s1.finschoolcd, ";
        $query .= "    schreg_regd_dat w3,";
        $query .= "    schreg_regd_hdat w1 ";
        $query .= "    left join staff_mst w2 on w1.tr_cd1 = w2.staffcd ";
        $query .= "WHERE ";
        $query .= "    w1.year = '" .CTRL_YEAR ."' AND ";
        $query .= "    w1.semester = '".CTRL_SEMESTER ."' AND ";
        $query .= "    w1.grade || w1.hr_class = '".$grd_cls."' AND ";
        $query .= "    w1.year = w3.year AND ";
        $query .= "    w1.semester = w3.semester AND ";
        $query .= "    w1.grade = w3.grade AND ";
        $query .= "    w1.hr_class = w3.hr_class AND ";
        $query .= "    w3.schregno = w4.schregno ";
        $query .= "order by w3.grade, w3.hr_class, w3.attendno";

        return $query;
    }

    //学校名取得の取得(
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }
}
?>
