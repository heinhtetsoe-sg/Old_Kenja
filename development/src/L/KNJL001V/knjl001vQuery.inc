<?php
class knjl001vQuery extends Query
{
    //校種コンボ
    public function getSchoolKind($year)
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度コンボ
    public function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      YEAR AS LABEL,";
        $query .= "      YEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_APPLICANT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR <> '{$model->examyear}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examyear}', '{$model->examyear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //リスト
    public function getList($model)
    {
        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_APPLICANT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR                   = '{$model->leftYear}' ";
        $query .= "      AND EXAM_SCHOOL_KIND   = '{$model->schoolKind}' ";
        $query .= "  ORDER BY ";
        $query .= "      APPLICANT_DIV ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $div = "")
    {
        $applicantDiv = ($div == "chk") ? $model->field["APPLICANT_DIV"] : $model->applicantDiv;

        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_APPLICANT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR                 = '{$model->leftYear}' ";
        $query .= "      AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "      AND APPLICANT_DIV    = '{$applicantDiv}' ";

        return $query;
    }

    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckApplicantDivMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_STD_APPLICANT_MST)
    public function &getCopyQuery($db, $model, $prevYear)
    {
        $query  = " INSERT INTO ENTEXAM_STD_APPLICANT_MST ";
        $query .= "         (  ";
        $query .= "          YEAR ";
        $query .= "          , EXAM_SCHOOL_KIND ";
        $query .= "          , APPLICANT_DIV ";
        $query .= "          , APPLICANT_NAME ";
        $query .= "          , REGISTERCD ";
        $query .= "          , UPDATED ";
        $query .= "         ) ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          '".$model->leftYear."' ";
        $query .= "          , EXAM_SCHOOL_KIND ";
        $query .= "          , APPLICANT_DIV ";
        $query .= "          , APPLICANT_NAME ";
        $query .= "          , '".STAFFCD."' ";
        $query .= "          , SYSDATE() ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_STD_APPLICANT_MST ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '{$prevYear}' ";
        $query .= "     ) ";

        return $query;
    }

    /******** 追加更新処理  ********/
    //--- INSERT
    public function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]             = $model->leftYear;
        $data["EXAM_SCHOOL_KIND"][TEXT] = $model->schoolKind;
        $data["APPLICANT_DIV"][TEXT]    = $fields["APPLICANT_DIV"];
        $data["APPLICANT_NAME"][TEXT]   = $fields["APPLICANT_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_STD_APPLICANT_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["APPLICANT_NAME"][TEXT]   = $fields["APPLICANT_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "   YEAR                 = '{$model->leftYear}' ";
        $where .= "   AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $where .= "   AND APPLICANT_DIV    = '{$fields["APPLICANT_DIV"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_STD_APPLICANT_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_STD_APPLICANT_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR                 = '{$model->leftYear}' ";
        $query .= "   AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "   AND APPLICANT_DIV    = '{$fields["APPLICANT_DIV"]}' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
