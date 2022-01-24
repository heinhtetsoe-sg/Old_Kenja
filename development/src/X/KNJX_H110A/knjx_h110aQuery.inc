<?php

require_once('for_php7.php');
class knjx_h110aQuery extends Query
{
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT GRADE,HR_CLASS,HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY GRADE,HR_CLASS";

        return $query;
    }

    //生徒一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = "SELECT DISTINCT T1.SCHREGNO, T1.ATTENDNO, T2.NAME_SHOW ";
        $query .= "FROM SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "WHERE T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO";

        return $query;
    }

    //存在チェック(教科コード)
    public function checkClassMst($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD         = '{$data["CLASSCD"]}' AND ";
        $query .= "     SCHOOL_KIND     = '{$data["SCHOOL_KIND"]}' ";

        return $query;
    }

    //存在チェック(科目コード)
    public function checkSubclassMst($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     SUBCLASSCD = '{$data["SUBCLASSCD"]}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '{$data["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND     = '{$data["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD   = '{$data["CURRICULUM_CD"]}' ";
        }

        return $query;
    }

    public function getMaxSeq($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     MAX(VALUE(SEQ,0)) + 1 AS SEQ ";
        $query .= " FROM ";
        $query .= "     schreg_qualified_dat ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //MAX値取得(SEQ)
            $query = knjx_h110aQuery::getMaxSeq($data_arr[$i]["YEAR"], $data_arr[$i]["SCHREGNO"]);
            $maxSeq = $db->getOne($query);
            //データセット
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["SEQ"][NUMBER]            = strlen($maxSeq) ? $maxSeq : 1;
            $data["REGDDATE"][TEXT]         = str_replace("/", "-", $data_arr[$i]["REGDDATE"]);
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]      = $data_arr[$i]["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]  = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]= $data_arr[$i]["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
            $data["CONDITION_DIV"][TEXT]    = $data_arr[$i]["CONDITION_DIV"];
            $data["CONTENTS"][TEXT]         = $data_arr[$i]["CONTENTS"];
            $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $data["CREDITS"][NUMBER]        = $data_arr[$i]["CREDITS"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            //追加
            $query = Query::insertSQL($data, "SCHREG_QUALIFIED_DAT");
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T3.REGDDATE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     T3.CONDITION_DIV, ";
        $query .= "     '増加単位認定' AS CONDITION_DIV_NAME, ";
        $query .= "     T3.CONTENTS, ";
        $query .= "     N1.NAME1 AS CONTENTS_NAME, ";
        $query .= "     T3.REMARK, ";
        $query .= "     T3.CREDITS, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_QUALIFIED_DAT T3 ";
        $query .= "          ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T3.CONDITION_DIV = '1' ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD       = T3.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND   = T3.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'H305' AND N1.NAMECD2 = T3.CONTENTS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["STUDENT"] != "") {
            $query .= "     AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T3.REGDDATE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SEQ ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //教育課程コード取得
    public function getCurriculumCd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";

        return $query;
    }
}