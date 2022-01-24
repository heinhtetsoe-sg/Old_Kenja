<?php

require_once('for_php7.php');

class knjp725Query extends Query {
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

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "")
    {
        $gradewk = array();
        $grade = "";
        $s_orderflg = "";
        if ($model->field["GRADE"] != "") {
            $gradewk = explode("-", $model->field["GRADE"]);
            if (get_count($gradewk) > 0) {
                $grade = $gradewk[0];
                if (get_count($gradewk) > 1) {
                    $s_orderflg = $gradewk[1];
                }
            }
        }
        //参照・更新可
        //GRADE項目に、内部で新入生/在籍生の切り分けフラグを保持(1:在校生、0:新入生)する。
        //学年/クラスがNULLの場合は、'0'で埋めて出力。
        //在籍生の学年/クラスの情報
        if ($div != "HR_CLASS" || $s_orderflg === "" || ($div == "HR_CLASS" && $s_orderflg === "1")) {
            $query  = " SELECT DISTINCT ";
            if ($div == "GRADE") {
                $query .= "     T1.GRADE || '-1' AS VALUE, ";
                $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
            } else {
                $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
                $query .= "     T1.HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1";
            $query .= " LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = T1.YEAR";
            $query .= "      AND GDAT.GRADE = T1.GRADE";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$semester."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            if ($div == "HR_CLASS" && $grade != "" && $s_orderflg == "1") {
                //$query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
                $query .= "     AND T1.GRADE = '".$grade."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
                $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
                $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
            }
        }

        //新入生の学年/クラス情報をUNIONする。
        if ($div != "HR_CLASS" || $s_orderflg === "") {
            $query .= " UNION ";
        }
        if ($div != "HR_CLASS" || $s_orderflg === "" || ($div == "HR_CLASS" && $s_orderflg === "0")) {
            $query .= " SELECT DISTINCT ";
            if ($div == "GRADE") {
                $query .= "   VALUE(T2.GRADE, '00') || '-0' AS VALUE, ";
                $query .= "   '新入生' || VALUE(T2.GRADE, '00') AS LABEL ";
            } else {
                $query .= "   VALUE(T2.GRADE, '00') || '-' || VALUE(T2.HR_CLASS, '000') AS VALUE, ";
                $query .= "   '新入生' || VALUE(T2.HR_CLASS, '000') AS LABEL ";
            }
            $query .= " FROM ";
            $query .= "   FRESHMAN_DAT T2 ";
            $query .= " WHERE ";
            $query .= "   T2.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
            if ($div == "HR_CLASS" && $grade != "" && $s_orderflg == "0") {
                //$query .= "     AND T2.GRADE = '".$model->field["GRADE"]."' ";
                $query .= "     AND T2.GRADE = '".$grade."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //対象生徒
    function getStudent($model, $seme) {
        $grclswk = explode("-", $model->field["GRADE"]);
        //GRADE項目に、内部で新入生/在籍生の切り分けフラグを保持しているので、切り分けて利用する。
        $s_orderflg = "";
        if (get_count($grclswk) > 1) {
            $grhrcls = $grclswk[0];
            $s_orderflg = $grclswk[1];
        }

        if ($s_orderflg === "0") {
            $query  = " SELECT ";
            $query .= "     T3.SCHREGNO, ";
            $query .= "     T3.ATTENDNO, ";
            $query .= "     T3.NAME AS NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T3 ";
            $query .= " WHERE ";
            $query .= "     T3.ENTERYEAR = '".(CTRL_YEAR+1)."' ";
            $query .= "     AND T3.GRADE || '-' || T3.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
            $query .= " ORDER BY ";
            $query .= "     T3.ATTENDNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     T2.SCHREGNO, ";
            $query .= "     T2.ATTENDNO, ";
            $query .= "     T1.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST T1, ";
            $query .= "     SCHREG_REGD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T2.SEMESTER = '".$seme."' ";
            $query .= "     AND T2.GRADE || '-' || T2.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
            $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
            $query .= " ORDER BY ";
            $query .= "     T2.ATTENDNO ";
        }

        return $query;
    }

    //対象外(対象学期に居ない)の生徒取得
    function getSchnoIdou($model,$semester) {
        $date = str_replace("/", "-", CTRL_DATE);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$semester."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["HR_CLASS"]."' AND ( ";
                           //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                           //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                     (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                           //留学(1)・休学(2)者
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                      S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                      CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                           THEN T2.EDATE ELSE '".$date."' END  ";
        $query .= "                      BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }
}
?>