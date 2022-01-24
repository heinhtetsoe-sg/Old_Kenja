<?php

require_once('for_php7.php');

class knjl018vQuery extends Query
{
    //校種リスト取得
    public function getExamSchoolKind($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->examYear."' AND ";
        $query .= "     NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験IDリスト取得
    public function getExamID($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS VALUE, ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || EXAM_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR             = '".$model->examYear."' AND ";
        $query .= "     EXAM_SCHOOL_KIND = '".$model->examSchoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場リスト取得
    public function getPlaceID($model)
    {
        $query  = "SELECT ";
        $query .= "    PLACE_ID AS VALUE, ";
        $query .= "    PLACE_ID || ':' || PLACE_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_STD_HALL_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->examYear."'";

        return $query;
    }

    //出身学校リスト取得
    public function getFinschoolCD($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     FIN.FINSCHOOL_NAME AS LABEL, ";
        $query .= "     FIN.FINSCHOOLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "            ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "           AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     INNER JOIN FINSCHOOL_MST FIN ";
        $query .= "             ON FIN.FINSCHOOLCD = BASE.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     RECEPT.YEAR = '".$model->examYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $receptno = "")
    {
        $query  = " WITH ABSENT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         YEAR, ";
        $query .= "         RECEPTNO, ";
        $query .= "         ABSENCE_FLG ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_STD_SCORE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ABSENCE_FLG = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CASE WHEN ABSENT.ABSENCE_FLG = '1' THEN '1' ELSE '0' END AS ABSENCE_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "            ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "           AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_HALL_DAT HALL ";
        $query .= "            ON HALL.YEAR             = RECEPT.YEAR ";
        $query .= "           AND HALL.EXAMNO           = RECEPT.EXAMNO ";
        $query .= "           AND HALL.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "           AND HALL.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "           AND HALL.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "           AND HALL.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= "     LEFT JOIN ABSENT ";
        $query .= "            ON ABSENT.YEAR     = RECEPT.YEAR ";
        $query .= "           AND ABSENT.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     RECEPT.YEAR = '".$model->examYear."' ";
        $query .= " AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '".$model->examID."' ";
        if ($model->placeID != "ALL") {
            $query .= " AND HALL.PLACE_ID = '".$model->placeID."' ";
        }
        if ($model->finschoolCD != "ALL") {
            $query .= " AND BASE.FINSCHOOLCD = '".$model->finschoolCD."' ";
        }
        if ($receptno != "") {
            $query .= " AND RECEPT.RECEPTNO = '".$receptno."' ";
        }
        $query .= " ORDER BY ";
        if ($model->order == "2") {
            $query .= "     VALUE(BASE.NAME_KANA, ''), ";
        }
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    //テスト科目取得
    public function selectExamSubclassQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAM_SUBCLASS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->examYear."' AND ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '".$model->examID."' ";

        return $query;
    }

    /***** 更新処理 *****/
    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $receptno => $value) {
            if ($value["UPD_FLG"] !== "1") {
                continue;
            }

            $query = knjl018vQuery::selectQuery($model, $receptno);
            $receptno = $db->getRow($query, DB_FETCHMODE_ASSOC)["RECEPTNO"];

            $where  = " WHERE YEAR     = '".$model->examYear."' ";
            $where .= "   AND RECEPTNO = '".$receptno."'";

            if ($value["ABSENCE_FLG"]) {
                $query = " DELETE FROM ENTEXAM_STD_SCORE_DAT ".$where;
                $db->query($query);

                $data = array();
                $query = knjl018vQuery::selectExamSubclassQuery($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data["YEAR"][TEXT] = $model->examYear;
                    $data["RECEPTNO"][TEXT] = $receptno;
                    $data["EXAM_SUBCLASS"][TEXT] = $row["EXAM_SUBCLASS"];
                    $data["ABSENCE_FLG"][TEXT] = $value["ABSENCE_FLG"];
                    $data["REGISTERCD"][TEXT] = STAFFCD;
                    $data["UPDATED"][FUNC] = "SYSDATE()";

                    $query = Query::insertSQL($data, "ENTEXAM_STD_SCORE_DAT");
                    $db->query($query);
                }
                $result->free();
            } else {
                $data["YEAR"][TEXT] = $model->examYear;
                $data["RECEPTNO"][TEXT] = $receptno;
                $data["ABSENCE_FLG"][TEXT] = $value["ABSENCE_FLG"];
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC] = "SYSDATE()";

                $query = Query::updateSQL($data, "ENTEXAM_STD_SCORE_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
