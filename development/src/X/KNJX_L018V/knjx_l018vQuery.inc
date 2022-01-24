<?php

require_once('for_php7.php');
class knjx_l018vQuery extends Query
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
        $query .= "     YEAR = '".$model->field["EXAM_YEAR"]."' AND ";
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
        $query .= "     YEAR             = '".$model->field["EXAM_YEAR"]."' AND ";
        $query .= "     EXAM_SCHOOL_KIND = '".$model->field["EXAM_SCHOOL_KIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場リスト取得
    public function getPlaceID($model)
    {
        $query  = " SELECT ";
        $query .= "     PLACE_ID AS VALUE, ";
        $query .= "     PLACE_ID || ':' || PLACE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_HALL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["EXAM_YEAR"]."'";

        return $query;
    }

    //志願者検索
    public function selectReceptDat($year, $receptno, $examID)
    {
        $query  = " SELECT ";
        $query .= "     RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     RECEPTNO = '".$receptno."' AND ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '".$examID."'";

        return $query;
    }

    //テスト科目取得
    public function selectExamSubclassQuery($year, $examID)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAM_SUBCLASS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '".$examID."' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($idx = 0; $idx < get_count($data_arr); $idx++) {
            $data = array();
            $query = knjx_l018vQuery::selectReceptDat($data_arr[$idx]["YEAR"], $data_arr[$idx]["RECEPTNO"], $data_arr[$idx]["EXAM_ID"]);
            $receptno = $db->getOne($query);

            $where  = " WHERE YEAR     = '".$data_arr[$idx]["YEAR"]."' ";
            $where .= "   AND RECEPTNO = '".$receptno."'";

            if ($data_arr[$idx]["ABSENCE_FLG"]) {
                $query = " DELETE FROM ENTEXAM_STD_SCORE_DAT ".$where;
                $db->query($query);

                $data = array();
                $query = knjx_l018vQuery::selectExamSubclassQuery($data_arr[$idx]["YEAR"], $data_arr[$idx]["EXAM_ID"]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data["YEAR"][TEXT] = $data_arr[$idx]["YEAR"];
                    $data["RECEPTNO"][TEXT] = $receptno;
                    $data["EXAM_SUBCLASS"][TEXT] = $row["EXAM_SUBCLASS"];
                    $data["ABSENCE_FLG"][TEXT] = $data_arr[$idx]["ABSENCE_FLG"];
                    $data["REGISTERCD"][TEXT] = STAFFCD;
                    $data["UPDATED"][FUNC] = "SYSDATE()";

                    $query = Query::insertSQL($data, "ENTEXAM_STD_SCORE_DAT");
                    $db->query($query);
                }
                $result->free();
            } else {
                $data["YEAR"][TEXT] = $data_arr[$idx]["YEAR"];
                $data["RECEPTNO"][TEXT] = $receptno;
                $data["ABSENCE_FLG"][TEXT] = $data_arr[$idx]["ABSENCE_FLG"];
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC] = "SYSDATE()";

                $query = Query::updateSQL($data, "ENTEXAM_STD_SCORE_DAT", $where);
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query = " SELECT * FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " WITH ABSENT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         RECEPTNO, ";
        $query .= "         COUNT(CASE WHEN VALUE(ABSENCE_FLG, '0') = '0' THEN 1 ELSE 0 END) AS ABSENCE0 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_STD_SCORE_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         RECEPTNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     RECEPT.YEAR, ";
        $query .= "     RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY AS EXAM_ID, ";
        $query .= "     STD.EXAM_NAME, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CASE WHEN ABSENT.ABSENCE0 IS NOT NULL AND (ABSENT.ABSENCE0 = 0) THEN '1' ELSE '0' END AS ABSENCE_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN ENTEXAM_STD_MST STD ";
        $query .= "            ON STD.YEAR             = RECEPT.YEAR ";
        $query .= "           AND STD.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND";
        $query .= "           AND STD.APPLICANT_DIV    = RECEPT.APPLICANT_DIV";
        $query .= "           AND STD.COURSE_DIV       = RECEPT.COURSE_DIV";
        $query .= "           AND STD.FREQUENCY        = RECEPT.FREQUENCY ";
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
        $query .= "     RECEPT.YEAR = '".$model->field["EXAM_YEAR"]."' ";
        $query .= " AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '".$model->field["EXAM_ID"]."' ";
        if ($model->field["PLACE_ID"] != "ALL") {
            $query .= " AND HALL.PLACE_ID = '".$model->field["PLACE_ID"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }
}
