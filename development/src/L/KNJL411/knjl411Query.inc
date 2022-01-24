<?php

require_once('for_php7.php');

class knjl411Query extends Query {

    //イベント参加者データ取得
    function getRecruitDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO  = '".$model->recruit_no."' ";

        return $query;
    }

    //イベント参加登録データ取得
    function getRecruitEventDat($model, $div="") {

        $date = ($model->touroku_date == "") ? "" : str_replace("/", "-", $model->touroku_date);

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     S2.EVENT_CLASS_NAME, ";
        $query .= "     S1.EVENT_NAME, ";
        $query .= "     N1.NAME1 AS MEDIA_NAME, ";
        $query .= "     N2.NAME1 AS STATE_NAME ";
        for ($i = 1; $i <= 5; $i++) {
            $query .= "     ,T1.HOPE_COURSECD{$i} || '-' || T1.HOPE_MAJORCD{$i} AS HOPE_COURSE_MAJOR{$i} ";
            $query .= "     ,MAJOR{$i}.COURSENAME || MAJOR{$i}.MAJORNAME AS MAJORNAME{$i} ";
            $query .= "     ,COURSE{$i}.COURSECODENAME AS COURSECODENAME{$i} ";
        }
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_DAT T1 ";
        $query .= "     LEFT JOIN RECRUIT_EVENT_YMST S1 ON S1.YEAR      = T1.YEAR ";
        $query .= "                                    AND S1.EVENT_CLASS_CD  = T1.EVENT_CLASS_CD ";
        $query .= "                                    AND S1.EVENT_CD  = T1.EVENT_CD ";
        $query .= "     LEFT JOIN RECRUIT_CLASS_MST S2 ON S2.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR      = T1.YEAR ";
        $query .= "                            AND N1.NAMECD1   = 'L401' ";
        $query .= "                            AND N1.NAMECD2   = T1.MEDIA_CD ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR      = T1.YEAR ";
        $query .= "                            AND N2.NAMECD1   = 'L402' ";
        $query .= "                            AND N2.NAMECD2   = T1.STATE_CD ";
        for ($i = 1; $i <= 5; $i++) {
            $query .= "     LEFT JOIN V_COURSE_MAJOR_MST MAJOR{$i} ON MAJOR{$i}.YEAR      = T1.YEAR ";
            $query .= "                                    AND MAJOR{$i}.COURSECD  = T1.HOPE_COURSECD{$i} ";
            $query .= "                                    AND MAJOR{$i}.MAJORCD   = T1.HOPE_MAJORCD{$i} ";
            $query .= "     LEFT JOIN V_COURSECODE_MST COURSE{$i} ON COURSE{$i}.YEAR        = T1.YEAR ";
            $query .= "                                  AND COURSE{$i}.COURSECODE  = T1.HOPE_COURSECODE{$i} ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".(CTRL_YEAR + 1)."' ";
        $query .= " AND T1.RECRUIT_NO   = '".$model->recruit_no."' ";
        if ($div) {
            $query .= " AND T1.TOUROKU_DATE = '".$date."' ";
            $query .= " AND T1.EVENT_CLASS_CD     = '".$model->event_class_cd."' ";
            $query .= " AND T1.EVENT_CD     = '".$model->event_cd."' ";
            $query .= " AND T1.MEDIA_CD     = '".$model->media_cd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TOUROKU_DATE DESC, ";
        $query .= "     T1.EVENT_CLASS_CD, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.MEDIA_CD ";

        return $query;
    }

    //分類取得
    function getRecruitClass() {
        $query  = " SELECT ";
        $query .= "     EVENT_CLASS_CD || ':' || EVENT_CLASS_NAME LABEL, ";
        $query .= "     EVENT_CLASS_CD VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //イベントマスタ取得
    function getRecruitEventYmst($model, $event_class_cd, $school_kind) {
        $query  = " SELECT ";
        $query .= "     EVENT_CD AS VALUE, ";
        $query .= "     EVENT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND EVENT_CLASS_CD = '{$event_class_cd}' ";
        if ($school_kind == "J") {
            $query .= "     AND INT(EVENT_CD) < 500 ";
        } else {
            $query .= "     AND INT(EVENT_CD) >= 500 ";
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
        $query .= "     YEAR    = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科取得
    function getCourseMajorMst() {
        $query  = " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得
    function getCoursecodeMst($model, $coursecd_majorcd) {
        $query  = " SELECT ";
        $query .= "     T2.EXAMCOURSECD AS VALUE, ";
        $query .= "     T2.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT T1, ";
        $query .= "     ENTEXAM_COURSE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.YEAR         = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     T1.RECRUIT_NO   = '".$model->recruit_no."' AND ";
        $query .= "     T2.TESTDIV      = '1' AND ";
        $query .= "     (CASE WHEN T1.SCHOOL_KIND = 'J' THEN '1' ELSE '2' END) = T2.APPLICANTDIV AND ";
        $query .= "     T2.COURSECD || '-' || T2.MAJORCD = '".$coursecd_majorcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function getRow_check($model) {

        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["TOUROKU_DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' AND ";
        $query .= "     TOUROKU_DATE    = '".$date."' AND ";
        $query .= "     EVENT_CLASS_CD  = '".$model->field["EVENT_CLASS_CD"]."' AND ";
        $query .= "     EVENT_CD        = '".$model->field["EVENT_CD"]."' AND ";
        $query .= "     MEDIA_CD        = '".$model->field["MEDIA_CD"]."' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields) {

        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $fields["TOUROKU_DATE"]);

        $data = array();
        $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
        $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
        $data["TOUROKU_DATE"][TEXT]     = $date;
        $data["EVENT_CLASS_CD"][TEXT]   = $fields["EVENT_CLASS_CD"];
        $data["EVENT_CD"][TEXT]         = $fields["EVENT_CD"];
        $data["MEDIA_CD"][TEXT]         = $fields["MEDIA_CD"];

        $data["STATE_CD"][TEXT]         = $fields["STATE_CD"];

        for ($i = 1; $i <= 5; $i++) {
            if ($fields["HOPE_COURSE_MAJOR{$i}"]) {
                list($hope_coursecd, $hope_majorcd) = explode('-', $fields["HOPE_COURSE_MAJOR{$i}"]);
            } else {
                $hope_coursecd = $hope_majorcd = "";
            }
            $data["HOPE_COURSECD{$i}"][TEXT]    = $hope_coursecd;
            $data["HOPE_MAJORCD{$i}"][TEXT]     = $hope_majorcd;
            $data["HOPE_COURSECODE{$i}"][TEXT]  = $fields["HOPE_COURSECODE{$i}"];
        }
        $data["REMARK"][TEXT]           = $fields["REMARK"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        if ($model->cmd == "insert") {

            $db->query(Query::insertSQL($data, "RECRUIT_EVENT_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
            $where .= "     RECRUIT_NO      = '".$model->recruit_no."' AND ";
            $where .= "     TOUROKU_DATE    = '".$date."' AND ";
            $where .= "     EVENT_CLASS_CD  = '".$model->event_class_cd."' AND ";
            $where .= "     EVENT_CD        = '".$model->event_cd."' AND ";
            $where .= "     MEDIA_CD        = '".$model->media_cd."' ";

            $db->query(Query::updateSQL($data, "RECRUIT_EVENT_DAT", $where));
        }
        Query::dbCheckIn($db);

        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["TOUROKU_DATE"]);

        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' AND ";
        $query .= "     TOUROKU_DATE    = '".$date."' AND ";
        $query .= "     EVENT_CLASS_CD  = '".$model->event_class_cd."' AND ";
        $query .= "     EVENT_CD        = '".$model->event_cd."' AND ";
        $query .= "     MEDIA_CD        = '".$model->media_cd."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return ;
    }
}
?>
