<?php
class knjl002vQuery extends Query
{
    //年度コンボ
    public function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      YEAR AS LABEL,";
        $query .= "      YEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_MST ";
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

    //入試コードコンボ
    public function getApplicantMst($model)
    {
        $query  = " SELECT ";
        $query .= "     APPLICANT_DIV || ':' || APPLICANT_NAME AS LABEL,  ";
        $query .= "     APPLICANT_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '{$model->leftYear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望コースコンボ
    public function getCourseCodeMst($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.COURSECODE || ':' || T1.COURSECODENAME AS LABEL, ";  // 学籍コースコード
        $query .= "   T1.COURSECODE AS VALUE ";  // 学籍コース名称
        $query .= " FROM ";
        $query .= "   V_COURSECODE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->leftYear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    public function getSubclassList($model)
    {
        $query  = " WITH SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         L009.ENTEXAMYEAR, ";
        $query .= "         N1.NAMECD2 AS EXAM_SCHOOL_KIND, ";
        $query .= "         L009.SEQ, ";
        $query .= "         L009.NAME1 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST L009 ";
        $query .= "         LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR    = L009.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1 = 'L003' ";
        $query .= "             AND N1.NAMECD2 = L009.APPLICANTDIV ";
        $query .= "     WHERE ";
        $query .= "         L009.SETTING_CD = 'L009' ";
        $query .= " ), PERFECT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         EXAM_SCHOOL_KIND, ";
        $query .= "         EXAM_SUBCLASS ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_STD_PERFECT_MST ";
        $query .= "     WHERE ";
        $query .= "         EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->examId}' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         EXAM_SCHOOL_KIND, ";
        $query .= "         APPLICANT_DIV, ";
        $query .= "         COURSE_DIV, ";
        $query .= "         FREQUENCY, ";
        $query .= "         EXAM_SUBCLASS ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   PERFECT.EXAM_SUBCLASS || ':' || SUBCLASS.NAME1 AS LABEL, ";
        $query .= "   PERFECT.EXAM_SUBCLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "   PERFECT ";
        $query .= "   LEFT JOIN SUBCLASS ";
        $query .= "        ON SUBCLASS.ENTEXAMYEAR      = PERFECT.YEAR ";
        $query .= "       AND SUBCLASS.EXAM_SCHOOL_KIND = PERFECT.EXAM_SCHOOL_KIND ";
        $query .= "       AND SUBCLASS.SEQ              = PERFECT.EXAM_SUBCLASS ";
        $query .= " WHERE ";
        $query .= "     PERFECT.YEAR = '{$model->leftYear}' ";
        $query .= "     and PERFECT.EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト
    public function getList($model)
    {
        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR                 = '{$model->leftYear}' ";
        $query .= "      AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "  ORDER BY ";
        $query .= "      APPLICANT_DIV, ";
        $query .= "      COURSE_DIV, ";
        $query .= "      FREQUENCY ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $div = "")
    {
        $query  = "  SELECT ";
        $query .= "     APPLICANT_DIV, ";
        $query .= "     COURSE_DIV, ";
        $query .= "     FREQUENCY, ";
        $query .= "     EXAM_NAME, ";
        $query .= "     EXAM_DATE, ";
        $query .= "     AM_PM, ";
        $query .= "     EXAM_NO_BAND, ";
        $query .= "     UPDATED AS UPDATED1";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR               = '{$model->leftYear}' ";
        if ($div == "chk") {
            $query .= "      AND EXAM_SCHOOL_KIND    = '{$model->schoolKind}' ";
            $query .= "      AND APPLICANT_DIV       = '{$model->field["APPLICANT_DIV"]}' ";
            $query .= "      AND COURSE_DIV          = '{$model->field["COURSE_DIV"]}' ";
            $query .= "      AND FREQUENCY           = '{$model->field["FREQUENCY"]}' ";
        } else {
            $query .= "      AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY  = '{$model->examId}' ";
        }

        return $query;
    }

    public function getRowExamSubClass($model)
    {
        $query  = "  SELECT ";
        $query  .= "      distinct ";
        $query  .= "      L009.SEQ AS EXAM_SUBCLASS, ";
        $query  .= "      L009.NAME1 AS EXAM_SUBCLASS_NAME, ";
        $query  .= "      CASE WHEN PERFECT.EXAM_SUBCLASS IS NULL THEN FALSE ELSE TRUE END AS CHECKED, ";
        $query  .= "      INT(L009.SEQ) AS SORT ";
        $query  .= "  FROM ";
        $query  .= "      ENTEXAM_SETTING_MST L009 ";
        $query  .= "      INNER JOIN V_NAME_MST N1 ";
        $query  .= "              ON N1.YEAR           = L009.ENTEXAMYEAR ";
        $query  .= "             AND N1.NAMECD1        = 'L003' ";
        $query  .= "             AND N1.NAMECD2        = L009.APPLICANTDIV ";
        $query  .= "      LEFT JOIN ENTEXAM_STD_PERFECT_MST PERFECT ";
        $query  .= "             ON PERFECT.YEAR           = L009.ENTEXAMYEAR ";
        $query  .= "            AND PERFECT.EXAM_SCHOOL_KIND || PERFECT.APPLICANT_DIV || PERFECT.COURSE_DIV || PERFECT.FREQUENCY  = '{$model->examId}' ";
        $query  .= "            AND PERFECT.EXAM_SUBCLASS  = L009.SEQ ";
        $query  .= "  WHERE ";
        $query  .= "      L009.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query  .= "      AND L009.APPLICANTDIV = '{$model->schoolKind}' ";
        $query  .= "      AND L009.SETTING_CD   = 'L009' ";
        $query  .= "  ORDER BY ";
        $query  .= "      SORT ";

        return $query;
    }

    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckEntExamStdMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_STD_MST)
    public function &getCopyQuery($model, $prevYear)
    {
        $query  = " INSERT INTO ENTEXAM_STD_MST ";
        $query .= "         (  ";
        $query .= "          YEAR ";
        $query .= "          , EXAM_SCHOOL_KIND ";
        $query .= "          , APPLICANT_DIV ";
        $query .= "          , COURSE_DIV ";
        $query .= "          , FREQUENCY ";
        $query .= "          , EXAM_NAME ";
        $query .= "          , EXAM_DATE ";
        $query .= "          , AM_PM ";
        $query .= "          , EXAM_NO_BAND ";
        $query .= "          , REGISTERCD ";
        $query .= "          , UPDATED ";
        $query .= "         ) ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          '".$model->leftYear."' ";
        $query .= "          , EXAM_SCHOOL_KIND ";
        $query .= "          , APPLICANT_DIV ";
        $query .= "          , COURSE_DIV ";
        $query .= "          , FREQUENCY ";
        $query .= "          , EXAM_NAME ";
        $query .= "          , EXAM_DATE ";
        $query .= "          , AM_PM ";
        $query .= "          , EXAM_NO_BAND ";
        $query .= "          , '".STAFFCD."' ";
        $query .= "          , SYSDATE() ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_STD_MST ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '{$prevYear}' ";
        $query .= "     ) ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_STD_PERFECT_MST)
    public function &getCopyPerfectMstQuery($model, $prevYear)
    {
        $query  = " INSERT INTO ENTEXAM_STD_PERFECT_MST ";
        $query .= "         (  ";
        $query .= "          YEAR ";
        $query .= "          , EXAM_SCHOOL_KIND ";
        $query .= "          , APPLICANT_DIV ";
        $query .= "          , COURSE_DIV ";
        $query .= "          , FREQUENCY ";
        $query .= "          , EXAM_SUBCLASS ";
        $query .= "          , STEPS ";
        $query .= "          , LABEL ";
        $query .= "          , VALUE ";
        $query .= "          , REGISTERCD ";
        $query .= "          , UPDATED ";
        $query .= "         ) ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          '".$model->leftYear."' ";
        $query .= "          , EXAM_SCHOOL_KIND ";
        $query .= "          , APPLICANT_DIV ";
        $query .= "          , COURSE_DIV ";
        $query .= "          , FREQUENCY ";
        $query .= "          , EXAM_SUBCLASS ";
        $query .= "          , STEPS ";
        $query .= "          , LABEL ";
        $query .= "          , VALUE ";
        $query .= "          , '".STAFFCD."' ";
        $query .= "          , SYSDATE() ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_STD_PERFECT_MST ";
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
        $data["COURSE_DIV"][TEXT]       = $fields["COURSE_DIV"];
        $data["FREQUENCY"][TEXT]        = $fields["FREQUENCY"];
        $data["EXAM_NAME"][TEXT]        = $fields["EXAM_NAME"];
        $data["EXAM_DATE"][TEXT]        = str_replace("/", "-", $fields["EXAM_DATE"]);
        $data["AM_PM"][TEXT]            = $fields["AM_PM"];
        $data["EXAM_NO_BAND"][TEXT]     = $fields["EXAM_NO_BAND"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_STD_MST");
        $db->query($query);
        unset($data);

        $result = $db->query(knjl002vQuery::getRowExamSubClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!is_null($model->field["EXAM_SUBCLASS"]) && in_array($row["EXAM_SUBCLASS"], $model->field["EXAM_SUBCLASS"])) {
                $data["YEAR"][TEXT]              = $model->leftYear;
                $data["EXAM_SCHOOL_KIND"][TEXT]  = $model->schoolKind;
                $data["APPLICANT_DIV"][TEXT]     = $fields["APPLICANT_DIV"];
                $data["COURSE_DIV"][TEXT]        = $fields["COURSE_DIV"];
                $data["FREQUENCY"][TEXT]         = $fields["FREQUENCY"];
                $data["EXAM_SUBCLASS"][TEXT]     = $row["EXAM_SUBCLASS"];
                $data["STEPS"][TEXT]             = "0";
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "sysdate()";

                $query = Query::insertSQL($data, "ENTEXAM_STD_PERFECT_MST");
                $db->query($query);
            }
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["EXAM_NAME"][TEXT]        = $fields["EXAM_NAME"];
        $data["EXAM_DATE"][TEXT]        = str_replace("/", "-", $fields["EXAM_DATE"]);
        $data["AM_PM"][TEXT]            = $fields["AM_PM"];
        $data["EXAM_NO_BAND"][TEXT]     = $fields["EXAM_NO_BAND"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "   YEAR                 = '{$model->leftYear}' ";
        $where .= "   AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $where .= "   AND APPLICANT_DIV    = '{$fields["APPLICANT_DIV"]}' ";
        $where .= "   AND COURSE_DIV       = '{$fields["COURSE_DIV"]}' ";
        $where .= "   AND FREQUENCY        = '{$fields["FREQUENCY"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_STD_MST", $where);
        $db->query($query);

        $result = $db->query(knjl002vQuery::getRowExamSubClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!is_null($model->field["EXAM_SUBCLASS"]) && in_array($row["EXAM_SUBCLASS"], $model->field["EXAM_SUBCLASS"])) {
                if ($row["CHECKED"]) {
                    // チェックあり、データあり　何もしない
                } else {
                    // チェックあり、データなし　追加
                    unset($data);

                    $data["YEAR"][TEXT]              = $model->leftYear;
                    $data["EXAM_SCHOOL_KIND"][TEXT]  = $model->schoolKind;
                    $data["APPLICANT_DIV"][TEXT]     = $fields["APPLICANT_DIV"];
                    $data["COURSE_DIV"][TEXT]        = $fields["COURSE_DIV"];
                    $data["FREQUENCY"][TEXT]         = $fields["FREQUENCY"];
                    $data["EXAM_SUBCLASS"][TEXT]     = $row["EXAM_SUBCLASS"];
                    $data["STEPS"][TEXT]             = "0";
                    $data["REGISTERCD"][TEXT]        = STAFFCD;
                    $data["UPDATED"][FUNC]           = "sysdate()";
    
                    $query = Query::insertSQL($data, "ENTEXAM_STD_PERFECT_MST");
                    $db->query($query);
                }
            } else {
                if ($row["CHECKED"]) {
                    // チェックなし、データあり　削除
                    $query  = " DELETE FROM ";
                    $query .= "     ENTEXAM_STD_PERFECT_MST ";
                    $query .= " WHERE ";
                    $query .= "   YEAR                  = '".$model->leftYear."' ";
                    $query .= "   AND EXAM_SCHOOL_KIND  = '".$model->schoolKind."' ";
                    $query .= "   AND APPLICANT_DIV     = '".$fields["APPLICANT_DIV"]."' ";
                    $query .= "   AND COURSE_DIV        = '".$fields["COURSE_DIV"]."' ";
                    $query .= "   AND FREQUENCY         = '".$fields["FREQUENCY"]."' ";
                    $query .= "   AND EXAM_SUBCLASS     = '".$row["EXAM_SUBCLASS"]."' ";
                    $db->query($query);
                } else {
                    // チェックなし、データなし　何もしない
                }
            }
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR              = '{$model->leftYear}' ";
        $query .= "   AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY  = '{$model->examId}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_STD_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR              = '{$model->leftYear}' ";
        $query .= "   AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY  = '{$model->examId}' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //満点マスタ メイン
    public function selectPerfectMainQuery($model, $div = "")
    {
        $query  = " SELECT ";
        if ($div == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     STEPS, ";
            $query .= "     LABEL, ";
            $query .= "     VALUE ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR              = '{$model->leftYear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->examId}' ";
        $query .= "     AND EXAM_SUBCLASS = '{$model->examSubclass}' ";
        if ($div != "cnt") {
            $query .= " ORDER BY STEPS ";
        }

        return $query;
    }

    //Insert作成
    public function &getInsertPerfectQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  =  " DELETE FROM ";
        $query .= "      ENTEXAM_STD_PERFECT_MST ";
        $query .= "  WHERE ";
        $query .= "       YEAR              = '{$model->leftYear}' ";
        $query .= "       AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->examId}' ";
        $query .= "       AND EXAM_SUBCLASS = '{$model->examSubclass}' ";
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]                 = $model->leftYear;
        $data["EXAM_SCHOOL_KIND"][TEXT]     = $model->schoolKind;
        $data["APPLICANT_DIV"][TEXT]        = $model->applicantDiv;
        $data["COURSE_DIV"][TEXT]           = $model->courseDiv;
        $data["FREQUENCY"][TEXT]            = $model->frequency;
        $data["EXAM_SUBCLASS"][TEXT]        = $model->examSubclass;
        $data["REGISTERCD"][TEXT]           = STAFFCD ;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($model->mantendiv == "1") {
            $cnt = 1;
        } else {
            $cnt = $model->maxLevel;
        }

        for ($i = 1; $i <= $cnt; $i++) {
            $data["STEPS"][TEXT]      = $model->dataPerfectField[$i]["STEPS"];
            $data["LABEL"][TEXT]      = $model->dataPerfectField[$i]["LABEL"];
            $data["VALUE"][TEXT]      = $model->dataPerfectField[$i]["VALUE"];

            $query = Query::insertSQL($data, "ENTEXAM_STD_PERFECT_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
