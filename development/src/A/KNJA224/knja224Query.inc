<?php

require_once('for_php7.php');


class knja224Query extends Query
{

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
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
    public function getDefaultSize($div)
    {
        $query  = " SELECT ";
        if ($div == "HEIGHT") {
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
    public function selectCsvQuery($grd_cls, $model)
    {
        $query  = " WITH TRF AS ( ";
        $query .= "     SELECT ";
        $query .= "         TRF1.schregno, ";
        $query .= "         TRF1.transfercd, ";
        $query .= "         TRF1.transfer_sdate ";
        $query .= "     FROM ";
        $query .= "         schreg_transfer_dat TRF1 ";
        $query .= "         inner join ( ";
        $query .= "             SELECT ";
        $query .= "                 schregno, ";
        $query .= "                 transfercd, ";
        $query .= "                 MAX(transfer_sdate) AS transfer_sdate ";
        $query .= "             FROM ";
        $query .= "                 schreg_transfer_dat ";
        $query .= "             WHERE ";
        $query .= "                 (TO_DATE('".CTRL_DATE."', 'YYYY/MM/DD') BETWEEN transfer_sdate and transfer_edate) ";
        $query .= "             OR  (transfer_sdate <= TO_DATE('".CTRL_DATE."', 'YYYY/MM/DD') and transfer_edate IS NULL) ";
        $query .= "             GROUP BY ";
        $query .= "                 schregno, ";
        $query .= "                 transfercd ";
        $query .= "         ) TRF2 on TRF1.schregno = TRF2.schregno and ";
        $query .= "             TRF1.transfercd = TRF2.transfercd and ";
        $query .= "             TRF1.transfer_sdate = TRF2.transfer_sdate ";
        $query .= " ) ";
        $query .= "SELECT ";
        $query .= "    w3.grade || w3.hr_class as grade_hr_class,";
        $query .= "    smallint(w3.hr_class) as hr_class,";
        $query .= "    smallint(w3.attendno) as attendno,";
        //文京の場合、性別の＊を表記しない。
        if ($model->schoolName == 'bunkyo') {
            $query .= "    '' AS SEX, ";
        } else {
            $query .= "    CASE WHEN w4.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
            // 青山学院専用フォームを使用している場合、かつ、
            // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）、
            // 性別の＊を表記しない。
            if (($model->Properties["useFormNameA224"] == "KNJA224_10.xml") && strlen($model->field["NAME_NASI"])) {
                $query .= "     CASE WHEN w4.GRD_DIV IN ('1','2','3') THEN '' WHEN W4.SEX = '2' THEN '*' ELSE '' END AS sex, ";
            } else {
                $query .= "     CASE WHEN W4.SEX = '2' THEN '*' ELSE '' END AS sex, ";  // 男:空白、女:'*'
            }
        }
        // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）
        if (strlen($model->field["NAME_NASI"])) {
            $query .= "    CASE WHEN w4.GRD_DIV IN ('1','2','3') THEN '' ELSE w4.name END AS NAME, ";
            $query .= "    CASE WHEN w4.GRD_DIV IN ('1','2','3') THEN '' ELSE w4.name_kana END AS NAME_KANA, ";
            $query .= "    CASE WHEN w4.GRD_DIV IN ('1','2','3') THEN '' ELSE w4.name_eng END AS NAME_ENG, ";
        } else {
            $query .= "    w4.name,";
            $query .= "    w4.name_kana,";
            $query .= "    w4.name_eng, ";
        }
        $query .= "    s1.finschool_name,";
        $query .= "    w1.hr_nameabbv,";
        $query .= "    w2.staffname, ";
        $query .= "    w4.schregno, ";
        $query .= "    W4.grd_div, ";
        $query .= "    TRF.transfercd, ";
        // 青山学院専用フォームを使用している場合、かつ、
        // 退学者・転学者・卒業生の除籍区分名や異動区分名は空白（チェック有りの場合）
        if (($model->Properties["useFormNameA224"] == "KNJA224_10.xml") && strlen($model->field["NAME_NASI"])) {
            $query .= "     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN NULL ELSE A003.name1 END AS grd_name,";
            $query .= "     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN NULL ELSE A004.name1 END AS transfer_name,";
        } else {
            $query .= "     A003.name1 AS grd_name, ";
            $query .= "     A004.name1 AS transfer_name, ";
        }
        $query .= "    TRF.transfer_sdate ";
        $query .= "FROM ";
        $query .= "    schreg_base_mst w4";
        $query .= "    left join finschool_mst s1 on w4.finschoolcd = s1.finschoolcd, ";
        $query .= "    schreg_regd_dat w3,";
        $query .= "    schreg_regd_hdat w1 ";
        $query .= "    left join staff_mst w2 on w1.tr_cd1 = w2.staffcd ";
        $query .= "    left join v_name_mst A003 on A003.year = W1.year and ";
        $query .= "        A003.namecd1 = 'A003' and ";
        $query .= "        A003.namecd2 = W4.grd_div ";
        $query .= "    left join TRF on TRF.schregno = W3.schregno ";
        $query .= "    left join v_name_mst A004 on A004.year = W1.year and ";
        $query .= "        A004.namecd1 = 'A004' and ";
        $query .= "        A004.namecd2 = TRF.transfercd ";
        $query .= "WHERE ";
        $query .= "    w1.year = '" .CTRL_YEAR ."' AND ";
        $query .= "    w1.semester = '".CTRL_SEMESTER ."' AND ";
        $query .= "    w1.grade || w1.hr_class = '".$grd_cls."' AND ";
        $query .= "    w1.year = w3.year AND ";
        $query .= "    w1.semester = w3.semester AND ";
        $query .= "    w1.grade = w3.grade AND ";
        $query .= "    w1.hr_class = w3.hr_class AND ";
        $query .= "    w3.schregno = w4.schregno ";
        // 青山学院専用フォームを使用している場合、かつ、
        // 空行を詰めて印字する場合、かつ、
        // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）、
        // 印字対象外とする。
        if (($model->Properties["useFormNameA224"] == "KNJA224_10.xml")&& strlen($model->field["KARA"]) && strlen($model->field["NAME_NASI"])) {
            $query .= "     and VALUE(w4.GRD_DIV, '0') NOT IN ('1', '2', '3') ";
        }
        $query .= "order by w3.grade, w3.hr_class, w3.attendno";

        return $query;
    }

    //学校名取得の取得(
    public function getSchoolName()
    {
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

    //職員情報取得
    public function getStaffInfo()
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' AND ";
        $query .= "     STAFF_SEQ = '001' ";
        return $query;
    }

    //通信制かチェックするフラグを取得する。
    public function getTuusinflg()
    {
        $query  = " SELECT ";
        $query  .= "  Z001.NAMESPARE3 ";
        $query  .= " FROM ";
        $query  .= "  SCHOOL_MST SM ";
        $query  .= "  LEFT JOIN NAME_MST Z001 ";
        $query  .= "    ON Z001.NAMECD1 = 'Z001' ";
        $query  .= "    AND Z001.NAMECD2 = SM.SCHOOLDIV ";
        $query  .= " WHERE ";
        $query  .= "  SM.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query  .= "  AND SM.SCHOOLCD = '".SCHOOLCD."' ";
            $query  .= "  AND SM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
}
