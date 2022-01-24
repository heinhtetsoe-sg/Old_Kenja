<?php

require_once('for_php7.php');
class knja116Query extends Query {

    //年度一覧
    function getNendo() {
        $query .= " select distinct ";
        $query .= "     YEAR as LABEL, ";
        $query .= "     YEAR as VALUE";
        $query .= " from ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
    
    //年組
    function getNenKumi($namecd1, $year) {
        $query .= " select ";
        $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME as LABEL ";
        $query .= " from ";
        $query .= "     NAME_MST ";
        $query .= " left join ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= "     on NAME_MST.NAME2 = REGD_H.GRADE ";
        $query .= " where ";
        $query .= "     NAME_MST.NAMECD1 = '" . $namecd1 . "' ";
        $query .= "     and REGD_H.YEAR = '" . $year . "' ";
        $query .= "     and REGD_H.SEMESTER = '1' ";

        return $query;
    }

    //生徒情報
    function getStudent($year, $semester, $nenkumi, $preischoolcd, $preischoolClassCd) {
        $query .= " select ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS || '-' || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD_H.HR_NAME || ' ' || REGD.ATTENDNO || '番 ' || BASE.NAME AS LABEL  ";
        $query .= " from ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     left join SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     left join SCHREG_REGD_HDAT REGD_H ON  REGD.YEAR = REGD_H.YEAR  ";
        $query .= "          and REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "          and REGD.GRADE = REGD_H.GRADE ";
        $query .= "          and REGD.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= " where ";
        $query .= "     REGD.YEAR = '" . $year . "' ";
        $query .= "     and REGD.SEMESTER = '" . $semester . "' ";
        $query .= "     and REGD.GRADE || REGD.HR_CLASS = '" . $nenkumi . "' ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "         select ";
        $query .= "             'X' ";
        $query .= "         from ";
        $query .= "             ANOTHER_PRISCHOOL_HIST_DAT ANOTHER_PRI ";
        $query .= "         where ";
        $query .= "             REGD.YEAR = ANOTHER_PRI.YEAR ";
        $query .= "             and REGD.SCHREGNO = ANOTHER_PRI.SCHREGNO ";
        $query .= "             and ANOTHER_PRI.PRISCHOOLCD = '" . $preischoolcd . "' ";
        $query .= "             and ANOTHER_PRI.PRISCHOOL_CLASS_CD = '" . $preischoolClassCd . "' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //塾に入っている生徒
    function getStudent2($year, $semester, $nenkumi, $preischoolcd, $preischoolClassCd) {
        $query .= " SELECT ";
        $query .= "     ANOTHER_PRI.SCHREGNO, ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS || '-' || REGD.ATTENDNO || '-' || ANOTHER_PRI.SCHREGNO AS VALUE, ";
        $query .= "     REGD_H.HR_NAME || ' ' || REGD.ATTENDNO || '番 ' || BASE.NAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "     ANOTHER_PRISCHOOL_HIST_DAT ANOTHER_PRI ";
        $query .= "     left join SCHREG_BASE_MST BASE ON ANOTHER_PRI.SCHREGNO = BASE.SCHREGNO ";
        $query .= " left join ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     on REGD.SCHREGNO = ANOTHER_PRI.SCHREGNO ";
        $query .= "     and REGD.YEAR = ANOTHER_PRI.YEAR ";
        $query .= "     and REGD.SEMESTER = '{$semester}' ";
        $query .= " left join ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= "     on REGD.YEAR = REGD_H.YEAR ";
        $query .= "     and REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "     and REGD.GRADE  = REGD_H.GRADE ";
        $query .= "     and REGD.HR_CLASS  = REGD_H.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     ANOTHER_PRI.PRISCHOOLCD = '" .  $preischoolcd . "' ";
        $query .= "     AND ANOTHER_PRI.PRISCHOOL_CLASS_CD = '" . $preischoolClassCd . "' ";
        $query .= "     AND ANOTHER_PRI.YEAR = '" . $year . "' ";

        return $query;
    }

    //左側のリストの生徒情報
    function getSelectStudent($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS || '-' || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD_H.HR_NAME || ' ' || REGD.ATTENDNO || '番 ' || BASE.NAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = REGD.YEAR ";
        $query .= "                                  AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "                                  AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "                                  AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '".$model->year."' AND ";
        $query .= "     REGD.SEMESTER = '1' AND ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS || '-' || REGD.ATTENDNO || '-' || REGD.SCHREGNO IN ('".implode("','", $schregno)."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //塾名
    function getPriSchoolName($priSchoolCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        return $query;
    }
    //教室名
    function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         PRISCHOOLCD        = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
        
        return $query;
    }

    //登録チェック
    function chkDataQuery($model, $param) {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "     ANOTHER_PRISCHOOL_HIST_DAT ";
        $query .= " WHERE SCHREGNO = '" . $param['SCHREGNO'] . "'";
        $query .= "   AND SCHOOL_KIND = '" . $param['SCHOOL_KIND'] . "' ";
        $query .= "   AND PRISCHOOLCD = '" . $param['PRISCHOOLCD'] . "' ";
        $query .= "   AND PRISCHOOL_CLASS_CD = '" . $param['PRISCHOOL_CLASS_CD'] . "' ";
        $query .= "   AND REGISTERCD = '" . $param['REGISTERCD'] . "' ";

        $retval =$db->getOne($query);
        Query::dbCheckIn($db);
        return $retval;
    }

    //新規挿入
    function insertQuery($model, $schregNo) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data = array();
        
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["YEAR"][TEXT]                 = $model->year;
        $data["PRISCHOOLCD"][TEXT]          = $model->preischoolcd;
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $model->preischoolClassCd;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ANOTHER_PRISCHOOL_HIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //削除
    function deleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ANOTHER_PRISCHOOL_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '{$model->year}' ";
        $query .= "     and PRISCHOOLCD  = '{$model->preischoolcd}' ";
        $query .= "     and PRISCHOOL_CLASS_CD  = '$model->preischoolClassCd' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>