<?php
class knjl413hQuery extends Query
{
    //入試年度一覧取得
    public function selectYearQuery($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examyear}', '{$model->examyear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $default = "", $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        if ($default) {
            $query .= "    ,NAMESPARE2 AS DEFAULT ";
        }
        if ($namecd2) {
            $query .= "    ,NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     SETTING_CD   = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    public function getApplicantdivSet($year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '{$year}' AND ";
        $query .= "     NAMECD1    = 'L003' AND ";
        $query .= "     NAMESPARE2 = '1' ";

        return $query;
    }

    //受験コース取得
    public function getExamCourseMst($model, $examcoursecd = "")
    {
        $query  = "   SELECT ";
        $query .= "       *, ";
        $query .= "       EXAMCOURSECD AS VALUE, ";
        $query .= "       EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_COURSE_MST ";
        $query .= "   WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '0' "; // '0'固定
        if ($examcoursecd) {
            $query .= "     AND EXAMCOURSECD = '{$examcoursecd}' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     C1.EXAMCOURSE_NAME, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     CASE WHEN T1.SHDIV = '0' THEN '' ELSE T1.SHDIV || ':' || S1.NAME1 END AS SHDIV_LABEL, ";
        $query .= "     T1.BORDER1, ";
        $query .= "     T1.BORDER2, ";
        $query .= "     T1.BORDER3, ";
        $query .= "     T1.BORDER4, ";
        $query .= "     T1.BORDER5, ";
        $query .= "     T1.BORDER6, ";
        $query .= "     T1.BORDER7, ";
        $query .= "     T1.BORDER8, ";
        $query .= "     T1.BORDER9, ";
        $query .= "     T1.BORDER10, ";
        $query .= "     T1.BORDER11, ";
        $query .= "     T1.BORDER12 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND C1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST S1 ON S1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L006' ";
        $query .= "         AND S1.SEQ          = T1.SHDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.EXAMCOURSECD = '".$model->examcoursecd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     T1.SHDIV ";

        return $query;
    }

    //１レコード取得
    public function getRow($year, $applicantdiv, $examcoursecd, $shdiv)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     C1.EXAMCOURSE_NAME, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.BORDER1, ";
        $query .= "     T1.BORDER2, ";
        $query .= "     T1.BORDER3, ";
        $query .= "     T1.BORDER4, ";
        $query .= "     T1.BORDER5, ";
        $query .= "     T1.BORDER6, ";
        $query .= "     T1.BORDER7, ";
        $query .= "     T1.BORDER8, ";
        $query .= "     T1.BORDER9, ";
        $query .= "     T1.BORDER10, ";
        $query .= "     T1.BORDER11, ";
        $query .= "     T1.BORDER12 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND C1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '".$year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND T1.EXAMCOURSECD = '".$examcoursecd."' ";
        $query .= "     AND T1.SHDIV        = '".$shdiv."' ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 1; $i <=12; $i++) {
            ${"border".$i} = "";
        }

        //中学を選択した場合
        if ($model->applicantdiv == "1") {
            $border10      = $model->field["BORDER10"];
            $border12      = $model->field["BORDER12"];
        }
        //高校を選択した場合
        if ($model->applicantdiv == "2") {
            $border1      = $model->field["BORDER1"];
            $border2      = $model->field["BORDER2"];
            $border3      = $model->field["BORDER3"];
            $border4      = $model->field["BORDER4"];
            $border9      = $model->field["BORDER9"];
            $border11      = $model->field["BORDER11"];
        }
        //高校特進Aコースを選択した場合 EXAMCOURSECD:02
        if ($model->applicantdiv == "2" && $model->examcoursecd == $model->tokushinACd) {
            $border5      = $model->field["BORDER5"];
            $border6      = $model->field["BORDER6"];
            $border7      = $model->field["BORDER7"];
            $border8      = $model->field["BORDER8"];
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["COURSECD"][TEXT]      = $model->field["COURSECD"];
        $data["MAJORCD"][TEXT]       = $model->field["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]  = $model->examcoursecd;
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        for ($i = 1; $i <=12; $i++) {
            $data["BORDER{$i}"][TEXT]    = ${"border".$i};
        }
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 1; $i <=12; $i++) {
            ${"border".$i} = "";
        }

        //中学を選択した場合
        if ($model->applicantdiv == "1") {
            $border10      = $model->field["BORDER10"];
            $border12      = $model->field["BORDER12"];
        }
        //高校を選択した場合
        if ($model->applicantdiv == "2") {
            $border1      = $model->field["BORDER1"];
            $border2      = $model->field["BORDER2"];
            $border3      = $model->field["BORDER3"];
            $border4      = $model->field["BORDER4"];
            $border9      = $model->field["BORDER9"];
            $border11      = $model->field["BORDER11"];
        }
        //高校特進Aコースを選択した場合 EXAMCOURSECD:02
        if ($model->applicantdiv == "2" && $model->examcoursecd == $model->tokushinACd) {
            $border5      = $model->field["BORDER5"];
            $border6      = $model->field["BORDER6"];
            $border7      = $model->field["BORDER7"];
            $border8      = $model->field["BORDER8"];
        }

        $data = array();
        for ($i = 1; $i <=12; $i++) {
            $data["BORDER{$i}"][TEXT]    = ${"border".$i};
        }
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR      = '".$model->year."' ";
        $where .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= "     AND EXAMCOURSECD = '".$model->examcoursecd."' ";
        $where .= "     AND SHDIV        = '".$model->field["SHDIV"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST ";
        $query .= "       WHERE ";
        $query .= "           ENTEXAMYEAR      = '".$model->year."' ";
        $query .= "           AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "           AND EXAMCOURSECD = '".$model->examcoursecd."' ";
        $query .= "           AND SHDIV        = '".$model->field["SHDIV"]."' ";

        $result = $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //ある年度にデータが存在するのかチェック
    public function getCheckExamTypeMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    public function getCopyQuery($thisYear, $nextYear)
    {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     SHDIV, ";
        $query .= "     BORDER1, ";
        $query .= "     BORDER2, ";
        $query .= "     BORDER3, ";
        $query .= "     BORDER4, ";
        $query .= "     BORDER5, ";
        $query .= "     BORDER6, ";
        $query .= "     BORDER7, ";
        $query .= "     BORDER8, ";
        $query .= "     BORDER9, ";
        $query .= "     BORDER10, ";
        $query .= "     BORDER11, ";
        $query .= "     BORDER12, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        return $query;
    }
}
