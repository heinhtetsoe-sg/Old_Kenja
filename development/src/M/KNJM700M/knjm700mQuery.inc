<?php

require_once('for_php7.php');

class knjm700mQuery extends Query {

    //教科取得
    function getClassMst($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
        }
        $query .= "     CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD >= '92' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubClassMst($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
        }
        $query .= "     SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        } else {
            $query .= "     AND substr(SUBCLASSCD, 1, 2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座取得
    function getChairDat($model) {
        $query  = " SELECT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //指定日付の学期取得
    function getSemesterMst($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND '".str_replace("/", "-", $model->field["ATTENDDATE"])."' BETWEEN SDATE AND EDATE ";

        return $query;
    }

    //登録済み生徒データ
    function getSch($model, $setSem) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     T1.ATTENDDATE, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L3.HR_NAME || L2.ATTENDNO || '番' AS HR_ATTEND, ";
        $query .= "     L1.NAME, ";
        $query .= "     T1.PERIODF, ";
        $query .= "     NM1.NAME1 AS PERIODF_SHOW, ";
        $query .= "     T1.CREDIT_TIME, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SPECIALACT_ATTEND_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "          AND T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L3 ON L2.YEAR = L3.YEAR ";
        $query .= "          AND L2.SEMESTER = L3.SEMESTER ";
        $query .= "          AND L2.GRADE = L3.GRADE ";
        $query .= "          AND L2.HR_CLASS = L3.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'B001' ";
        $query .= "          AND T1.PERIODF = NM1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$setSem."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     AND T1.ATTENDDATE = '".str_replace("/", "-", $model->field["ATTENDDATE"])."' ";
        $query .= "     AND T1.PERIODF = '".$model->field["PERIODF"]."' ";
        $query .= "     AND T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     L2.GRADE, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO ";

        return $query;
    }

    //講座名簿チェック
    function getChairStdCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'M010' ";
        $query .= "     AND NAMECD2 = '".$model->field["CREDIT_TIME"]."' ";

        $creditTime = $db->getOne($query);

        $query = knjm700mQuery::getSemesterMst($model);
        $setSem = $db->getOne($query);
        $setSem = $setSem ? $setSem : CTRL_SEMESTER;

        $db->autoCommit(false);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $setSem;
        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        if ($model->Properties["useCurriculumcd"] == "1") {
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->field["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["ATTENDDATE"][DATE]       = $model->field["ATTENDDATE"];
        $data["PERIODF"][TEXT]          = $model->field["PERIODF"];
        $data["PERIODT"][TEXT]          = $model->field["PERIODF"];
        $data["CHAIRCD"][TEXT]          = $model->field["CHAIRCD"];
        $data["CREDIT_TIME"][NUMBER]    = $creditTime;
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "SPECIALACT_ATTEND_DAT");

        $db->query($query);
        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SPECIALACT_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     AND ATTENDDATE = '".str_replace("/", "-", $model->field["ATTENDDATE"])."' ";
        $query .= "     AND PERIODF = '".$model->field["PERIODF"]."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";

        $db->query($query);
        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuerysitei($data, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SPECIALACT_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$data["YEAR"]."' ";
        $query .= "     AND SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$data["SCHREGNO"]."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        }
        $query .= "     AND ATTENDDATE = '".str_replace("/", "-", $data["ATTENDDATE"])."' ";
        $query .= "     AND PERIODF = '".$data["PERIODF"]."' ";

        $db->query($query);
        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>