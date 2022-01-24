<?php

require_once('for_php7.php');

class knjd132tquery extends Query {
    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //学期一覧取得
    function getSemesterList($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model, $semester) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $semester) {
        $query  = " WITH HREPORTREMARKS AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         TOTALSTUDYTIME, ";       //学習活動
        $query .= "         REMARK1 AS VIEWPOINT, "; //観点
        $query .= "         SPECIALACTREMARK, ";     //活動の様子
        $query .= "         COMMUNICATION ";         //通信欄
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR                 = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER             = '".$semester."' ";
        $query .= "         AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "         AND T1.SCHREGNO             = T2.SCHREGNO ";
        $query .= " ), SPECIAL_ACT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 AS CLASS_ACT, ";   //学級活動
        $query .= "         REMARK2 AS SCH_COUNCIL, "; //生徒会活動
        $query .= "         REMARK3 AS SCHOOL_EVENT "; //学校行事
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND DIV      = '01' ";
        $query .= "         AND CODE     = '01' ";
        $query .= " ), CLUB_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 AS CLUB_NAME, "; //部活動名
        $query .= "         REMARK2 AS CLUB_MEMO ";  //部活動の記録
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND DIV      = '02' ";
        $query .= "         AND CODE     = '01' ";
        $query .= " ), OTHER_ACTION AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 AS OTHER_ACT "; //その他の活動
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND DIV      = '03' ";
        $query .= "         AND CODE     = '01' ";
        $query .= " ), OTHER_MRL AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 AS OTHER_MORAL "; //道徳欄
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND DIV      = '04' ";
        $query .= "         AND CODE     = '01' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SCHI.SCHREGNO, ";
        $query .= "     SCHI.ATTENDNO, ";
        $query .= "     SCHI.NAME_SHOW AS NAME, ";
        $query .= "     HREP.TOTALSTUDYTIME, ";  //学習活動
        $query .= "     HREP.VIEWPOINT, ";       //観点
        $query .= "     HREP.SPECIALACTREMARK, ";//活動の様子
        $query .= "     SPCL.CLASS_ACT, ";       //学級活動
        $query .= "     SPCL.SCH_COUNCIL, ";     //生徒会活動
        $query .= "     SPCL.SCHOOL_EVENT, ";    //学校行事
        $query .= "     CLUB.CLUB_NAME, ";       //部活動名
        $query .= "     CLUB.CLUB_MEMO, ";       //部活動の記録
        $query .= "     OTHM.OTHER_MORAL, ";     //道徳欄
        $query .= "     OTHR.OTHER_ACT, ";       //その他の活動
        $query .= "     HREP.COMMUNICATION ";    //通信欄
        $query .= " FROM ";
        $query .= "     SCHINFO SCHI ";
        $query .= "     LEFT JOIN HREPORTREMARKS HREP ON SCHI.SCHREGNO = HREP.SCHREGNO ";
        $query .= "     LEFT JOIN SPECIAL_ACT    SPCL ON SCHI.SCHREGNO = SPCL.SCHREGNO ";
        $query .= "     LEFT JOIN CLUB_INFO      CLUB ON SCHI.SCHREGNO = CLUB.SCHREGNO ";
        $query .= "     LEFT JOIN OTHER_ACTION   OTHR ON SCHI.SCHREGNO = OTHR.SCHREGNO ";
        $query .= "     LEFT JOIN OTHER_MRL      OTHM ON SCHI.SCHREGNO = OTHM.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     SCHI.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            if ($model->field["DATADIV"] == "1") {
                //HREPORTREMARK_DAT
                //項目
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["TOTALSTUDYTIME"][TEXT]   = $model->fields["TOTALSTUDYTIME"][$schregno];
                $data["REMARK1"][TEXT]          = $model->fields["VIEWPOINT"][$schregno];
                $data["SPECIALACTREMARK"][TEXT] = $model->fields["SPECIALACTREMARK"][$schregno];
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";

                //存在チェック
                $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DAT";
                $query .= " WHERE ";
                $query .= "         YEAR     = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $query .= "     AND SCHREGNO = '".$schregno."' ";

                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE ";
                    $where .= "         YEAR     = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                    $where .= "     AND SCHREGNO = '".$schregno."' ";
                    $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
                }
                $db->query($query);

                //HREPORTREMARK_DETAIL_DAT
                for ($num = 1; $num <= 2; $num++) {
                    $setDiv = sprintf("%02d", $num);
                    //項目
                    $data = array();
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                    $data["SCHREGNO"][TEXT]     = $schregno;
                    $data["DIV"][TEXT]          = $setDiv;
                    $data["CODE"][TEXT]         = "01";
                    $data["REMARK1"][TEXT]      = ($setDiv == "01") ? $model->fields["CLASS_ACT"][$schregno]  : $model->fields["CLUB_NAME"][$schregno];
                    $data["REMARK2"][TEXT]      = ($setDiv == "01") ? $model->fields["SCH_COUNCIL"][$schregno]: $model->fields["CLUB_MEMO"][$schregno];
                    if ($setDiv == "01") {
                        $data["REMARK3"][TEXT]      = $model->fields["SCHOOL_EVENT"][$schregno];
                    }
                    $data["REGISTERCD"][TEXT]   = STAFFCD ;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    //存在チェック
                    $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DETAIL_DAT";
                    $query .= " WHERE ";
                    $query .= "         YEAR     = '".CTRL_YEAR."' ";
                    $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                    $query .= "     AND SCHREGNO = '".$schregno."' ";
                    $query .= "     AND DIV      = '".$setDiv."' ";
                    $query .= "     AND CODE     = '01' ";

                    if (0 < $db->getOne($query)) {
                        //更新
                        $where  = " WHERE ";
                        $where .= "         YEAR     = '".CTRL_YEAR."' ";
                        $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                        $where .= "     AND SCHREGNO = '".$schregno."' ";
                        $where .= "     AND DIV      = '".$setDiv."' ";
                        $where .= "     AND CODE     = '01' ";
                        $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                    } else {
                        //追加
                        $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                    }
                    $db->query($query);
                }
            } else {
                //HREPORTREMARK_DAT
                //項目
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["COMMUNICATION"][TEXT]    = $model->fields["COMMUNICATION"][$schregno];
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";

                //存在チェック
                $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DAT";
                $query .= " WHERE ";
                $query .= "         YEAR     = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $query .= "     AND SCHREGNO = '".$schregno."' ";

                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE ";
                    $where .= "         YEAR     = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                    $where .= "     AND SCHREGNO = '".$schregno."' ";
                    $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
                }
                $db->query($query);

                //HREPORTREMARK_DETAIL_DAT
                for ($num = 3; $num <= 4; $num++) {
                    $setDiv = sprintf("%02d", $num);
                    $getfield = $num == 4 ? "OTHER_MORAL" : "OTHER_ACT";
                    //項目
                    $data = array();
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                    $data["SCHREGNO"][TEXT]     = $schregno;
                    $data["DIV"][TEXT]          = $setDiv;
                    $data["CODE"][TEXT]         = "01";
                    $data["REMARK1"][TEXT]      = $model->fields[$getfield][$schregno]; //その他の活動
                    $data["REGISTERCD"][TEXT]   = STAFFCD ;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    //存在チェック
                    $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DETAIL_DAT";
                    $query .= " WHERE ";
                    $query .= "         YEAR     = '".CTRL_YEAR."' ";
                    $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                    $query .= "     AND SCHREGNO = '".$schregno."' ";
                    $query .= "     AND DIV      = '{$setDiv}' ";
                    $query .= "     AND CODE     = '01' ";

                    if (0 < $db->getOne($query)) {
                        //更新
                        $where  = " WHERE ";
                        $where .= "         YEAR     = '".CTRL_YEAR."' ";
                        $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                        $where .= "     AND SCHREGNO = '".$schregno."' ";
                        $where .= "     AND DIV      = '{$setDiv}' ";
                        $where .= "     AND CODE     = '01' ";
                        $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                    } else {
                        //追加
                        $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                    }
                    $db->query($query);
                }
            }
        }
        Query::dbCheckIn($db);
    }
}
?>
