<?php

require_once('for_php7.php');
class knjx_l019vquery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験ID取得
    public function getExamId($model, $examId = "")
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS VALUE,";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || VALUE(EXAM_NAME,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '{$model->year}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        if ($examId) {
            $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$examId}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    public function getPlaceId($model, $placeId = "")
    {
        $query  = " SELECT ";
        $query .= "     PLACE_ID AS VALUE,";
        $query .= "     PLACE_ID || ':' || VALUE(PLACE_NAME,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_HALL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        if ($placeId) {
            $query .= "     AND PLACE_ID = '{$placeId}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getExamSubclass($model, $examSubclass = "")
    {
        $query  = " WITH SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         L009.ENTEXAMYEAR, ";
        $query .= "         L009.APPLICANTDIV AS EXAM_SCHOOL_KIND, ";
        $query .= "         L009.SEQ, ";
        $query .= "         L009.NAME1 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST L009 ";
        $query .= "     WHERE ";
        $query .= "         L009.SETTING_CD = 'L009' ";
        $query .= " ), PERFECT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         EXAM_SCHOOL_KIND, ";
        $query .= "         EXAM_SUBCLASS, ";
        $query .= "         CASE WHEN STEPS = 'A' THEN '1' ELSE '0' END AS PERFECTDIV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_STD_PERFECT_MST ";
        $query .= "     WHERE ";
        $query .= "         EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->field["EXAM_ID"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         EXAM_SCHOOL_KIND, ";
        $query .= "         APPLICANT_DIV, ";
        $query .= "         COURSE_DIV, ";
        $query .= "         FREQUENCY, ";
        $query .= "         EXAM_SUBCLASS, ";
        $query .= "         CASE WHEN STEPS = 'A' THEN '1' ELSE '0' END ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     PERFECT.EXAM_SUBCLASS || ':' || SUBCLASS.NAME1 AS LABEL, ";
        $query .= "     PERFECT.EXAM_SUBCLASS AS VALUE, ";
        $query .= "     SUBCLASS.NAME1 AS SUBCLASS_NAME, ";
        $query .= "     PERFECT.PERFECTDIV ";
        $query .= " FROM ";
        $query .= "     PERFECT ";
        $query .= "     LEFT JOIN SUBCLASS ";
        $query .= "          ON SUBCLASS.ENTEXAMYEAR      = PERFECT.YEAR ";
        $query .= "         AND SUBCLASS.EXAM_SCHOOL_KIND = PERFECT.EXAM_SCHOOL_KIND ";
        $query .= "         AND SUBCLASS.SEQ              = PERFECT.EXAM_SUBCLASS ";
        $query .= " WHERE ";
        $query .= "     PERFECT.YEAR                 = '{$model->year}' ";
        $query .= "     AND PERFECT.EXAM_SCHOOL_KIND = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        if ($examSubclass) {
            $query .= "     AND PERFECT.EXAM_SUBCLASS = '{$examSubclass}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //試験IDチェック
    public function checkExamId($model, &$data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '".$data["EXAM_ID"]."' ";

        return $query;
    }

    //科目チェック
    public function checkExamSubclass($model, &$data)
    {
        $query  = " SELECT ";
        $query .= "     STEPS, ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '".$data["EXAM_ID"]."' ";
        $query .= "     AND EXAM_SUBCLASS = '".$data["EXAM_SUBCLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     CASE WHEN STEPS = 'A' THEN VALUE ELSE INT(STEPS) END ";

        return $query;
    }

    //受験番号チェック
    public function checkReceptno($model, &$data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "         AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_SCORE_DAT SCORE_D ";
        $query .= "          ON SCORE_D.YEAR          = RECEPT.YEAR ";
        $query .= "         AND SCORE_D.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "         AND SCORE_D.EXAM_SUBCLASS = '".$data["EXAM_SUBCLASS"]."' ";
        $query .= " WHERE ";
        $query .= "     RECEPT.YEAR = '".$model->year."' ";
        $query .= "     AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '".$data["EXAM_ID"]."' ";
        $query .= "     AND RECEPT.RECEPTNO = '".$data["RECEPTNO"]."' ";
        $query .= "     AND VALUE(SCORE_D.ABSENCE_FLG,'') <> '1' "; //欠席者を除く

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_STD_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR              = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND RECEPTNO      = '".$data_arr[$i]["RECEPTNO"]."' ";
            $query .= "     AND EXAM_SUBCLASS = '".$data_arr[$i]["EXAM_SUBCLASS"]."' ";
            $dataCnt = $db->getOne($query);

            //データセット
            $data = array();
            $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR              = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND RECEPTNO      = '".$data_arr[$i]["RECEPTNO"]."' ";
                $where .= "     AND EXAM_SUBCLASS = '".$data_arr[$i]["EXAM_SUBCLASS"]."' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_STD_SCORE_DAT", $where);
            } else {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
                $data["EXAM_SUBCLASS"][TEXT]    = $data_arr[$i]["EXAM_SUBCLASS"];
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_STD_SCORE_DAT");
            }
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
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $db->query($query);
    }

    //評価取得
    public function getExamSubclassHyoka($model, $examId, $examSubclass, $steps)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SUBCLASS, ";
        $query .= "     STEPS, ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '".$examId."' ";
        $query .= "     AND EXAM_SUBCLASS = '".$examSubclass."' ";
        $query .= "     AND STEPS = '".$steps."' ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " WITH PERFECTDIV AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         EXAM_SCHOOL_KIND, ";
        $query .= "         APPLICANT_DIV, ";
        $query .= "         COURSE_DIV, ";
        $query .= "         FREQUENCY, ";
        $query .= "         EXAM_SUBCLASS, ";
        $query .= "         CASE WHEN STEPS = 'A' THEN '1' ELSE '0' END AS PERFECTDIV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_STD_PERFECT_MST ";
        //試験ID 全て以外
        if ($model->field["EXAM_ID"] != "ALL") {
            $query .= "     WHERE ";
            $query .= "         EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->field["EXAM_ID"]}' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         EXAM_SCHOOL_KIND, ";
        $query .= "         APPLICANT_DIV, ";
        $query .= "         COURSE_DIV, ";
        $query .= "         FREQUENCY, ";
        $query .= "         EXAM_SUBCLASS, ";
        $query .= "         CASE WHEN STEPS = 'A' THEN '1' ELSE '0' END ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     RECEPT.YEAR, ";
        $query .= "     RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY AS EXAM_ID, ";
        $query .= "     EXAM_M.EXAM_NAME, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     L009.SEQ AS EXAM_SUBCLASS, ";
        $query .= "     L009.NAME1 AS EXAM_SUBCLASS_NAME, ";
        $query .= "     SCORE_D.SCORE, ";
        $query .= "     '' AS HYOKA_LABEL, ";
        $query .= "     '' AS HYOKA_VALUE, ";
        $query .= "     P1.PERFECTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "         AND BASE.EXAMNO = RECEPT.EXAMNO ";
        //会場 全て以外
        if ($model->field["PLACE_ID"] != "ALL") {
            $query .= "     INNER JOIN ENTEXAM_STD_HALL_DAT HALL ";
            $query .= "          ON HALL.YEAR             = RECEPT.YEAR ";
            $query .= "         AND HALL.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
            $query .= "         AND HALL.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
            $query .= "         AND HALL.COURSE_DIV       = RECEPT.COURSE_DIV ";
            $query .= "         AND HALL.FREQUENCY        = RECEPT.FREQUENCY ";
            $query .= "         AND HALL.PLACE_ID         = '{$model->field["PLACE_ID"]}' ";
            $query .= "         AND HALL.EXAMNO           = RECEPT.EXAMNO ";
        }
        $query .= "     INNER JOIN PERFECTDIV P1 ";
        $query .= "          ON P1.YEAR             = RECEPT.YEAR ";
        $query .= "         AND P1.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "         AND P1.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "         AND P1.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "         AND P1.FREQUENCY        = RECEPT.FREQUENCY ";
        //科目 全て以外
        if ($model->field["EXAM_SUBCLASS"] != "ALL") {
            $query .= "         AND P1.EXAM_SUBCLASS    = '{$model->field["EXAM_SUBCLASS"]}' ";
        }
        $query .= "     LEFT JOIN ENTEXAM_STD_SCORE_DAT SCORE_D ";
        $query .= "          ON SCORE_D.YEAR          = RECEPT.YEAR ";
        $query .= "         AND SCORE_D.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "         AND SCORE_D.EXAM_SUBCLASS = P1.EXAM_SUBCLASS ";
        $query .= "     LEFT JOIN ENTEXAM_STD_MST EXAM_M ";
        $query .= "          ON EXAM_M.YEAR             = RECEPT.YEAR ";
        $query .= "         AND EXAM_M.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "         AND EXAM_M.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "         AND EXAM_M.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "         AND EXAM_M.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L009 ";
        $query .= "          ON L009.ENTEXAMYEAR   = RECEPT.YEAR ";
        $query .= "         AND L009.APPLICANTDIV  = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "         AND L009.SETTING_CD    = 'L009' ";
        $query .= "         AND L009.SEQ           = P1.EXAM_SUBCLASS ";
        $query .= " WHERE ";
        $query .= "     RECEPT.YEAR  = '{$model->year}' ";
        $query .= "     AND RECEPT.EXAM_SCHOOL_KIND  = '{$model->field["EXAM_SCHOOL_KIND"]}' ";
        //試験ID 全て以外
        if ($model->field["EXAM_ID"] != "ALL") {
            $query .= "     AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '{$model->field["EXAM_ID"]}' ";
        }
        $query .= "     AND VALUE(SCORE_D.ABSENCE_FLG,'') <> '1' "; //欠席者を除く
        $query .= " ORDER BY ";
        $query .= "     EXAM_ID, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     EXAM_SUBCLASS ";

        return $query;
    }
}
