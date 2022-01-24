<?php

require_once('for_php7.php');

class knjp723Query extends Query {

    function getYear($model) {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."'), ";
        $query .= "     ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学期
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTER || ':' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧
    function getSchregList($model) {
        $query  = "";
        $query .= " WITH APP_D AS ( ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_APPLICANT_CHECK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND MONTH       = '".$model->month."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     APP_D.PASSNO, ";
        $query .= "     APP_D.INTENTION_YES_FLG, ";
        $query .= "     APP_D.INTENTION_NO_FLG, ";
        $query .= "     APP_D.FORMS_YES_FLG, ";
        $query .= "     APP_D.FORMS_NO_FLG, ";
        $query .= "     APP_D.FATHER_TAX_CERTIFICATE_FLG, ";
        $query .= "     APP_D.FATHER_SPECIAL_TAX_DEC_FLG, ";
        $query .= "     APP_D.FATHER_TAX_NOTICE_FLG, ";
        $query .= "     APP_D.MOTHER_TAX_CERTIFICATE_FLG, ";
        $query .= "     APP_D.MOTHER_SPECIAL_TAX_DEC_FLG, ";
        $query .= "     APP_D.MOTHER_TAX_NOTICE_FLG, ";
        $query .= "     APP_D.MAINTAINER_TAX_CERTIFICATE_FLG, ";
        $query .= "     APP_D.MAINTAINER_SPECIAL_TAX_DEC_FLG, ";
        $query .= "     APP_D.MAINTAINER_TAX_NOTICE_FLG, ";
        $query .= "     APP_D.SCHOLARSHIP_PAYMENT_YES_NO_FLG, ";
        $query .= "     APP_D.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND HDAT.GRADE = REGD.GRADE ";
        $query .= "          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN APP_D ON APP_D.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND REGD.GRADE = '{$model->grade}' ";
        if (strlen($model->hr_class)) {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->hr_class}' ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "1") {
            $query .= "     BASE.NAME_KANA, ";
        }
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }

    //更新
    function getUpdateQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->setList as $counter => $Row) {
            $schregNo = $Row["SCHREGNO"];
            $setKey = $Row["SCHREGNO"];

            //データカウント
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
            $where .= "     AND MONTH       = '".$model->month."' ";
            $where .= "     AND SCHREGNO    = '".$schregNo."' ";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM REDUCTION_COUNTRY_APPLICANT_CHECK_DAT " .$where);

            //更新
            $data = array();
            if ($dataCnt == 0) {
                $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]                  = $model->schoolKind;
                $data["YEAR"][TEXT]                         = $model->year;
                $data["MONTH"][TEXT]                        = $model->month;
                $data["SCHREGNO"][TEXT]                     = $schregNo;
            }
            foreach ($model->fieldList as $fieldName) {
                $data[$fieldName][TEXT]                     = $model->field[$setKey][$fieldName];
            }
            $data["REGISTERCD"][TEXT]                       = STAFFCD;
            $data["UPDATED"][FUNC]                          = "sysdate()";

            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "REDUCTION_COUNTRY_APPLICANT_CHECK_DAT");
            } else {
                $query = Query::updateSQL($data, "REDUCTION_COUNTRY_APPLICANT_CHECK_DAT", $where);
            }
            $db->query($query);
        } //foreach

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }
}
?>
