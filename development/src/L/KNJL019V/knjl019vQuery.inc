<?php

require_once('for_php7.php');
class knjl019vQuery extends Query
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
    public function getExamId($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS VALUE,";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || VALUE(EXAM_NAME,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '{$model->ObjYear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
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
        $query .= "     YEAR = '{$model->ObjYear}' ";
        if ($placeId) {
            $query .= "     AND PLACE_ID = '{$placeId}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getExamSubclass($model)
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
        $query .= "         EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->examId}' ";
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
        $query .= "     PERFECT.YEAR                 = '{$model->ObjYear}' ";
        $query .= "     AND PERFECT.EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //満点マスタ取得
    public function getExamSubclassPerfect($model, $examSubclass, $perfectdiv, $value = "")
    {
        $query  = " SELECT ";
        $query .= "     STEPS, ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR              = '{$model->ObjYear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->examId}' ";
        $query .= "     AND EXAM_SUBCLASS = '{$examSubclass}' ";
        if ($value) {
            $query .= "     AND VALUE = '{$value}' ";
        }
        if ($perfectdiv != "1") {
            $query .= " ORDER BY ";
            $query .= "     INT(STEPS) ";
        }

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $arySubclass)
    {
        $orStr = "     ";
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        foreach ($arySubclass as $val) {
            $query .= "     SCORE_{$val["SUBCLASS_CD"]}.SCORE AS SCORE_{$val["SUBCLASS_CD"]}, ";
            $query .= "     SCORE_{$val["SUBCLASS_CD"]}.ABSENCE_FLG ABSENCE_FLG_{$val["SUBCLASS_CD"]}, ";
        }
        $query .= "     CASE WHEN ";
        foreach ($arySubclass as $val) {
            $query .= $orStr;
            $query .= " SCORE_{$val["SUBCLASS_CD"]}.ABSENCE_FLG = '1' ";
            $orStr = "     OR ";
        }
        $query .= "     THEN '1' ELSE '0' END AS ABSENCE_FLG, ";
        $query .= "     RECEPT.JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "         AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_HALL_DAT HALL ";
        $query .= "          ON HALL.YEAR             = RECEPT.YEAR ";
        $query .= "         AND HALL.EXAM_SCHOOL_KIND = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "         AND HALL.APPLICANT_DIV    = RECEPT.APPLICANT_DIV ";
        $query .= "         AND HALL.COURSE_DIV       = RECEPT.COURSE_DIV ";
        $query .= "         AND HALL.FREQUENCY        = RECEPT.FREQUENCY ";
        $query .= "         AND HALL.EXAMNO           = RECEPT.EXAMNO ";
        foreach ($arySubclass as $val) {
            $query .= "     LEFT JOIN ENTEXAM_STD_SCORE_DAT SCORE_{$val["SUBCLASS_CD"]} ";
            $query .= "          ON SCORE_{$val["SUBCLASS_CD"]}.YEAR          = RECEPT.YEAR ";
            $query .= "         AND SCORE_{$val["SUBCLASS_CD"]}.RECEPTNO      = RECEPT.RECEPTNO ";
            $query .= "         AND SCORE_{$val["SUBCLASS_CD"]}.EXAM_SUBCLASS = '{$val["SUBCLASS_CD"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     RECEPT.YEAR  = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.EXAM_SCHOOL_KIND || RECEPT.APPLICANT_DIV || RECEPT.COURSE_DIV || RECEPT.FREQUENCY = '{$model->examId}' ";
        if ($model->placeId != "ALL") {
            $query .= "         AND HALL.PLACE_ID         = '{$model->placeId}' ";
        }
        //欠席者非表示
        if ($model->checkHide == "1") {
            foreach ($arySubclass as $val) {
                $query .= "     AND VALUE(SCORE_{$val["SUBCLASS_CD"]}.ABSENCE_FLG,'') <> '1' ";
            }
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:受験番号順 2:かな順
        if ($model->sort == "1") {
            $query .= "     RECEPT.RECEPTNO ";
        } else {
            $query .= "     BASE.NAME_KANA, ";
            $query .= "     RECEPT.RECEPTNO ";
        }

        return $query;
    }

    //更新(エンター押下時にひとりのデータを更新）
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $receptno => $value) {
            if ($value["ABSENCE_FLG"] == "1") {
                continue;
            }

            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_STD_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR              = '{$model->ObjYear}' ";
            $query .= "     AND RECEPTNO      = '{$receptno}' ";
            $query .= "     AND EXAM_SUBCLASS = '{$model->examSubclass}' ";
            $dataCnt = $db->getOne($query);

            //データセット
            $data = array();
            $data["SCORE"][NUMBER]          = $value["SCORE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR              = '{$model->ObjYear}' ";
                $where .= "     AND RECEPTNO      = '{$receptno}' ";
                $where .= "     AND EXAM_SUBCLASS = '{$model->examSubclass}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_STD_SCORE_DAT", $where);
            } else {
                $data["YEAR"][TEXT]             = $model->ObjYear;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["EXAM_SUBCLASS"][TEXT]    = $model->examSubclass;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_STD_SCORE_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
