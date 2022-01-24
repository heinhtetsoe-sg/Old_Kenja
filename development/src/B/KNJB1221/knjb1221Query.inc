<?php

require_once('for_php7.php');

class knjb1221Query extends Query {
    //コース取得
    function getCourse() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD ||  T1.MAJORCD || T1.COURSECODE AS VALUE ";
        $query .= "     , T2.MAJORNAME || ':' || T3.COURSECODENAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     INNER JOIN MAJOR_MST T2  ";
        $query .= "         ON T2.COURSECD = T1.COURSECD  ";
        $query .= "         AND T2.MAJORCD = T1.MAJORCD  ";
        $query .= "     INNER JOIN COURSECODE_MST T3  ";
        $query .= "         ON T3.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2  ";
        $query .= "         ON T2.YEAR = T1.YEAR  ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER  ";
        $query .= "         AND T2.GRADE = T1.GRADE  ";
        $query .= "         AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "         ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.GRADE = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コピー元生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= "     , T2.NAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2  ";
        $query .= "         ON T2.SCHREGNO = T1.SCHREGNO  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //コピー元生徒の履修講座を取得
    function getChairStd($model){
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T2.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ";
        $query .= "         ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        $query .= "     AND '".str_replace('/', '-', $model->field["COPY_CHAIR_DATE"])."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";

        return $query;
    }

    //開講講座取得
    function getCourseChair($model) {
        $query  = " SELECT ";
        $query .= "     T2.CHAIRCD ";
        $query .= "     , T2.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ";
        $query .= "         ON T1.YEAR = T2.YEAR ";
        $query .= "         AND T1.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        $query .= "     AND T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.CHAIRCD ";

        return $query;
    }

    //更新対象の年組取得
    function getTrgtGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     REGDH.GRADE || REGDH.HR_CLASS AS VALUE, ";
        $query .= "     REGDH.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     REGDH.GRADE    = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model) {
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["TRGT_GRADE_HR_CLASS"]."' AND ( ";
                        //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                        //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                  (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                        //留学(1)・休学(2)者
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                   S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                   CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                        THEN T2.EDATE ELSE '".$date."' END ";
        $query .= "                        BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //更新対象の生徒
    function getTrgtStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "     , T1.ATTENDNO ";
        $query .= "     , T2.NAME_SHOW  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2  ";
        $query .= "         ON T2.SCHREGNO = T1.SCHREGNO  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["TRGT_GRADE_HR_CLASS"]."'  ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= "     , T1.ATTENDNO ";

        return $query;
    }

    //学期の日付範囲を取得
    function getSemesterRange(){
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER='".CTRL_SEMESTER."' ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $newDate = date('Y-m-d',strtotime($model->field["DATE"])-60*60*24);

        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER     = '".CTRL_SEMESTER."' ";
        $query .= " AND T1.APPDATE      = '".str_replace('/', '-', $model->field["DATE"])."' ";
        $query .= " AND T1.SCHREGNO    IN ('".str_replace("|", "','", $model->selectdataStd)."') ";
        $db->query($query);

        $data = array();
        $data["APPENDDATE"][TEXT] = $newDate;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC] = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR         = '".CTRL_YEAR."' ";
        $where .= " AND SEMESTER     = '".CTRL_SEMESTER."' ";
        $where .= " AND APPENDDATE   >= '".str_replace('/', '-', $model->field["DATE"])."' ";
        $where .= " AND SCHREGNO    IN ('".str_replace("|", "','", $model->selectdataStd)."') ";

        $db->query(Query::updateSQL($data, "CHAIR_STD_DAT", $where));

        $trgtSchregnoList = explode("|", $model->selectdataStd);
        $trgtChaircdList = explode("|", $model->selectdataChair);
        foreach ($trgtSchregnoList as $trgtSchregno) {
            foreach ($trgtChaircdList as $chaircd) {
                $data = array();
                $data["YEAR"][TEXT]           = CTRL_YEAR;
                $data["SEMESTER"][TEXT]       = CTRL_SEMESTER;
                $data["CHAIRCD"][TEXT]        = $chaircd;
                $data["SCHREGNO"][TEXT]       = $trgtSchregno;
                $data["APPDATE"][TEXT]        = str_replace('/', '-', $model->field["DATE"]);
                $data["APPENDDATE"][TEXT]     = $model->edate;
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";

                $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }
}
?>
