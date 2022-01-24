<?php
class knjl017vQuery extends Query
{
    //校種コンボ
    public function getSchoolKind($year, $schoolKind = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = 'L003' ";
        if ($schoolKind) {
            $query .= "     AND NAMECD2 = '{$schoolKind}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験IDコンボ
    public function getExamId($model, $examId = "")
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS VALUE,";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || VALUE(EXAM_NAME,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '{$model->ObjYear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        if ($examId) {
            $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$examId}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場コンボ
    public function getPlaceId($model, $placeId = "")
    {
        $query  = " SELECT ";
        $query .= "     PLACE_ID AS VALUE,";
        $query .= "     PLACE_ID || ':' || VALUE(PLACE_NAME,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_HALL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        if ($placeId) {
            $query .= "     AND PLACE_ID = '{$placeId}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    public function getSelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.EXAMNO AS VALUE, ";
        $query .= "     RECEPT.EXAMNO || '：' ||  BASE.NAME AS LABEL ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "            AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "       RECEPT.YEAR  = '".$model->ObjYear."' ";
        $query .= "   AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '".$model->field["EXAM_ID"]."' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.EXAMNO ";

        return $query;
    }

    //対象外の志願者取得
    public function getSelectQueryLeft($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "            AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_HALL_DAT HALL "; //会場割振りされた志願者は対象外
        $query .= "             ON HALL.YEAR             = RECEPT.YEAR ";
        $query .= "            AND HALL.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "            AND HALL.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "            AND HALL.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "            AND HALL.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= "            AND HALL.PLACE_ID         = '".$model->field["PLACE_ID"]."' ";
        $query .= "            AND HALL.EXAMNO           = RECEPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "       RECEPT.YEAR  = '".$model->ObjYear."' ";
        $query .= "   AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '".$model->field["EXAM_ID"]."' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.EXAMNO ";

        return $query;
    }

    //対象外の志願者取得(※別会場含む)
    public function getSelectQueryLeft2($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "            AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_HALL_DAT HALL "; //会場割振りされた志願者は対象外
        $query .= "             ON HALL.YEAR             = RECEPT.YEAR ";
        $query .= "            AND HALL.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "            AND HALL.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "            AND HALL.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "            AND HALL.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= "            AND HALL.EXAMNO           = RECEPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "       RECEPT.YEAR  = '".$model->ObjYear."' ";
        $query .= "   AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '".$model->field["EXAM_ID"]."' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.EXAMNO ";

        return $query;
    }

    //削除
    public function &getDeleteQuery($model, &$db)
    {
        $query  = "  DELETE FROM ENTEXAM_STD_HALL_DAT ";
        $query .= "  WHERE ";
        $query .= "        YEAR       = '".$model->ObjYear."' ";
        $query .= "    AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '".$model->field["EXAM_ID"]."' ";
        $query .= "    AND PLACE_ID   = '".$model->field["PLACE_ID"]."' ";

        $db->query($query);

        return;
    }

    //追加
    public function &getUpdateQuery($model, &$db)
    {
        $selectSchArr = explode(",", $model->selectLeft);
        foreach ($selectSchArr as $examno) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->ObjYear;
            $data["EXAM_SCHOOL_KIND"][TEXT] = $model->field["EXAM_SCHOOL_KIND"];
            $data["APPLICANT_DIV"][TEXT]    = substr($model->field["EXAM_ID"], 1, 2);
            $data["COURSE_DIV"][TEXT]       = substr($model->field["EXAM_ID"], 3, 4);
            $data["FREQUENCY"][TEXT]        = substr($model->field["EXAM_ID"], 7, 2);
            $data["PLACE_ID"][TEXT]         = $model->field["PLACE_ID"];
            $data["EXAMNO"][TEXT]           = $examno;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_STD_HALL_DAT");
            $db->query($query);
        }
        return;
    }

    //一覧表示 CSV出力
    public function selectCsvQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     RECEPT.YEAR, ";
        $query .= "     HALL.PLACE_ID, ";
        $query .= "     HALLM.PLACE_NAME, ";
        $query .= "     '".$model->field["EXAM_ID"]."' AS EXAM_ID, ";
        $query .= "     EXAMM.EXAM_NAME, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.YEAR           = RECEPT.YEAR ";
        $query .= "         AND BASE.EXAMNO         = RECEPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_HALL_DAT HALL ";
        $query .= "             ON HALL.YEAR             = RECEPT.YEAR ";
        $query .= "            AND HALL.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "            AND HALL.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "            AND HALL.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "            AND HALL.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= "            AND HALL.PLACE_ID         = '".$model->field["PLACE_ID"]."' ";
        $query .= "            AND HALL.EXAMNO           = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_HALL_MST HALLM ";
        $query .= "             ON HALLM.YEAR             = HALL.YEAR ";
        $query .= "            AND HALLM.PLACE_ID         = HALL.PLACE_ID ";
        $query .= "     LEFT JOIN ENTEXAM_STD_MST EXAMM ";
        $query .= "             ON EXAMM.YEAR             = RECEPT.YEAR ";
        $query .= "            AND EXAMM.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "            AND EXAMM.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "            AND EXAMM.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "            AND EXAMM.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= " WHERE ";
        $query .= "         RECEPT.YEAR        = '".$model->ObjYear."' ";
        $query .= "     AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '".$model->field["EXAM_ID"]."' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.EXAMNO ";

        return $query;
    }
}
