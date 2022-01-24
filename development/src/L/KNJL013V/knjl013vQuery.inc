<?php

require_once('for_php7.php');
class knjl013vQuery extends Query
{
    //年度リスト
    public function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANT_MST ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";
        $query .= "  ";

        return $query;
    }

    //校種リスト
    public function getExamSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    //年度リスト
    public function getSikenId($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY AS VALUE, ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || VALUE(EXAM_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND EXAM_SCHOOL_KIND='{$model->examSchoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校情報
    public function getFinschool()
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOLCD AS VALUE, ";
        $query .= "     FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";

        return $query;
    }

    //学年情報
    public function getGradeCd($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '2006' ";
        $query .= "     AND NAMECD1='A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->examSchoolKind == $row['NAME1']) {
                break;
            }
            $schoolKind = $row['NAME1'];
        }

        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOL_KIND='{$schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //科目名取得
    public function getKamokuName($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->examSchoolKind}' ";
        $query .= "     AND SETTING_CD = 'L008' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //備考名
    public function getRemarkName($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->exp_year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->examSchoolKind}' ";
        $query .= "     AND SETTING_CD = 'L081' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
    }
    //志願者データ
    public function selectQuery($model, $grade1, $grade2, $grade3, $grade4 = '', $grade5 = '', $grade6 = '')
    {
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     T1.JAPANESE AS JAPANESE1, ";
        $query .= "     T1.MATH AS MATH1, ";
        $query .= "     T1.SOCIETY AS SOCIETY1, ";
        $query .= "     T1.SCIENCE AS SCIENCE1, ";
        $query .= "     T1.ENGLISH AS ENGLISH1, ";
        $query .= "     T1.HEALTH_PHYSICAL AS HEALTH_PHYSICAL1, ";
        $query .= "     T1.TECH_HOME AS TECH_HOME1, ";
        $query .= "     T1.MUSIC AS MUSIC1, ";
        $query .= "     T1.ART AS ART1, ";
        $query .= "     T1.TOTAL3 AS TOTAL31, ";
        $query .= "     T1.TOTAL5 AS TOTAL51, ";
        $query .= "     T1.TOTAL9 AS TOTAL91, ";
        $query .= "     T1.ATTENDANCE AS ATTENDANCE1, ";

        $query .= "     T2.JAPANESE AS JAPANESE2, ";
        $query .= "     T2.MATH AS MATH2, ";
        $query .= "     T2.SOCIETY AS SOCIETY2, ";
        $query .= "     T2.SCIENCE AS SCIENCE2, ";
        $query .= "     T2.ENGLISH AS ENGLISH2, ";
        $query .= "     T2.HEALTH_PHYSICAL AS HEALTH_PHYSICAL2, ";
        $query .= "     T2.TECH_HOME AS TECH_HOME2, ";
        $query .= "     T2.MUSIC AS MUSIC2, ";
        $query .= "     T2.ART AS ART2, ";
        $query .= "     T2.TOTAL3 AS TOTAL32, ";
        $query .= "     T2.TOTAL5 AS TOTAL52, ";
        $query .= "     T2.TOTAL9 AS TOTAL92, ";
        $query .= "     T2.ATTENDANCE AS ATTENDANCE2, ";

        $query .= "     T3.JAPANESE AS JAPANESE3, ";
        $query .= "     T3.MATH AS MATH3, ";
        $query .= "     T3.SOCIETY AS SOCIETY3, ";
        $query .= "     T3.SCIENCE AS SCIENCE3, ";
        $query .= "     T3.ENGLISH AS ENGLISH3, ";
        $query .= "     T3.HEALTH_PHYSICAL AS HEALTH_PHYSICAL3, ";
        $query .= "     T3.TECH_HOME AS TECH_HOME3, ";
        $query .= "     T3.MUSIC AS MUSIC3, ";
        $query .= "     T3.ART AS ART3, ";
        $query .= "     T3.TOTAL3 AS TOTAL33, ";
        $query .= "     T3.TOTAL5 AS TOTAL53, ";
        $query .= "     T3.TOTAL9 AS TOTAL93, ";
        $query .= "     T3.ATTENDANCE AS ATTENDANCE3, ";

        if ($model->examSchoolKind == 'J') {
            $query .= "     T5.JAPANESE AS JAPANESE4, ";
            $query .= "     T5.MATH AS MATH4, ";
            $query .= "     T5.SOCIETY AS SOCIETY4, ";
            $query .= "     T5.SCIENCE AS SCIENCE4, ";
            $query .= "     T5.ENGLISH AS ENGLISH4, ";
            $query .= "     T5.HEALTH_PHYSICAL AS HEALTH_PHYSICAL4, ";
            $query .= "     T5.TECH_HOME AS TECH_HOME4, ";
            $query .= "     T5.MUSIC AS MUSIC4, ";
            $query .= "     T5.ART AS ART4, ";
            $query .= "     T5.TOTAL3 AS TOTAL34, ";
            $query .= "     T5.TOTAL5 AS TOTAL54, ";
            $query .= "     T5.TOTAL9 AS TOTAL94, ";
            $query .= "     T5.ATTENDANCE AS ATTENDANCE4, ";

            $query .= "     T6.JAPANESE AS JAPANESE5, ";
            $query .= "     T6.MATH AS MATH5, ";
            $query .= "     T6.SOCIETY AS SOCIETY5, ";
            $query .= "     T6.SCIENCE AS SCIENCE5, ";
            $query .= "     T6.ENGLISH AS ENGLISH5, ";
            $query .= "     T6.HEALTH_PHYSICAL AS HEALTH_PHYSICAL5, ";
            $query .= "     T6.TECH_HOME AS TECH_HOME5, ";
            $query .= "     T6.MUSIC AS MUSIC5, ";
            $query .= "     T6.ART AS ART5, ";
            $query .= "     T6.TOTAL3 AS TOTAL35, ";
            $query .= "     T6.TOTAL5 AS TOTAL55, ";
            $query .= "     T6.TOTAL9 AS TOTAL95, ";
            $query .= "     T6.ATTENDANCE AS ATTENDANCE5, ";

            $query .= "     T7.JAPANESE AS JAPANESE6, ";
            $query .= "     T7.MATH AS MATH6, ";
            $query .= "     T7.SOCIETY AS SOCIETY6, ";
            $query .= "     T7.SCIENCE AS SCIENCE6, ";
            $query .= "     T7.ENGLISH AS ENGLISH6, ";
            $query .= "     T7.HEALTH_PHYSICAL AS HEALTH_PHYSICAL6, ";
            $query .= "     T7.TECH_HOME AS TECH_HOME6, ";
            $query .= "     T7.MUSIC AS MUSIC6, ";
            $query .= "     T7.ART AS ART6, ";
            $query .= "     T7.TOTAL3 AS TOTAL36, ";
            $query .= "     T7.TOTAL5 AS TOTAL56, ";
            $query .= "     T7.TOTAL9 AS TOTAL96, ";
            $query .= "     T7.ATTENDANCE AS ATTENDANCE6, ";
        }
        $query .= "     RECEPT.RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     ON RECEPT.YEAR = BASE.YEAR ";
        $query .= "     AND RECEPT.EXAMNO =  BASE.EXAMNO ";
        $query .= " LEFT JOIN ENTEXAM_STD_APPLICANTCONFRPT_DAT T1 ";
        $query .= "     ON T1.YEAR = BASE.YEAR ";
        $query .= "     AND T1.EXAMNO = BASE.EXAMNO ";
        $query .= "     AND T1.GRADE = '{$grade1}' ";
        $query .= " LEFT JOIN ENTEXAM_STD_APPLICANTCONFRPT_DAT T2 ";
        $query .= "     ON T2.YEAR = BASE.YEAR ";
        $query .= "     AND T2.EXAMNO = BASE.EXAMNO ";
        $query .= "     AND T2.GRADE = '{$grade2}' ";
        $query .= " LEFT JOIN ENTEXAM_STD_APPLICANTCONFRPT_DAT T3 ";
        $query .= "     ON T3.YEAR = BASE.YEAR ";
        $query .= "     AND T3.EXAMNO = BASE.EXAMNO ";
        $query .= "     AND T3.GRADE = '{$grade3}' ";
        if ($model->examSchoolKind == 'J') {
            $query .= " LEFT JOIN ENTEXAM_STD_APPLICANTCONFRPT_DAT T5 ";
            $query .= "     ON T5.YEAR = BASE.YEAR ";
            $query .= "     AND T5.EXAMNO = BASE.EXAMNO ";
            $query .= "     AND T5.GRADE = '{$grade4}' ";
            $query .= " LEFT JOIN ENTEXAM_STD_APPLICANTCONFRPT_DAT T6 ";
            $query .= "     ON T6.YEAR = BASE.YEAR ";
            $query .= "     AND T6.EXAMNO = BASE.EXAMNO ";
            $query .= "     AND T6.GRADE = '{$grade5}' ";
            $query .= " LEFT JOIN ENTEXAM_STD_APPLICANTCONFRPT_DAT T7 ";
            $query .= "     ON T7.YEAR = BASE.YEAR ";
            $query .= "     AND T7.EXAMNO = BASE.EXAMNO ";
            $query .= "     AND T7.GRADE = '{$grade6}' ";
        }
        $query .= " WHERE ";
            $query .= " RECEPT.YEAR = '{$model->year}' ";
        if ($model->examnoStart != '') {
            $query .= "     AND RECEPT.EXAMNO >= '{$model->examnoStart}' ";
        }
        if ($model->examnoEnd != '') {
            $query .= "     AND RECEPT.EXAMNO <= '{$model->examnoEnd}' ";
        }
        if ($model->examSchoolKind != '') {
            $query .= "     AND EXAM_SCHOOL_KIND = '{$model->examSchoolKind}' ";
        }
        if ($model->applicantDiv != '') {
            $query .= "     AND APPLICANT_DIV = '{$model->applicantDiv}' ";
        }
        if ($model->courseDiv != '') {
            $query .= "     AND COURSE_DIV = '{$model->courseDiv}' ";
        }
        if ($model->frequency != '') {
            $query .= "     AND FREQUENCY = '{$model->frequency}' ";
        }
        if ($model->receptnoStart != '') {
            $query .= "     AND RECEPT.RECEPTNO >= '{$model->receptnoStart}' ";
        }
        if ($model->receptnoEnd != '') {
            $query .= "     AND RECEPT.RECEPTNO <= '{$model->receptnoEnd}' ";
        }
        if ($model->finschoolcd != '') {
            $query .= "     AND BASE.FINSCHOOLCD = '{$model->finschoolcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME_KANA ";

        return $query;
    }

    //志願者データ
    public function selectQueryRemark($model, $examno)
    {
        $query .= " SELECT ";
        $query .= "     T4.SPECIAL_ACT1, ";
        $query .= "     T4.SPECIAL_ACT2, ";
        $query .= "     T4.SPECIAL_ACT3, ";
        $query .= "     T4.SPECIAL_ACT4, ";
        $query .= "     T4.SPECIAL_ACT5, ";
        $query .= "     T4.SPECIAL_ACT6, ";
        $query .= "     T4.SPECIAL_ACT7, ";
        $query .= "     T4.SPECIAL_ACT8, ";
        $query .= "     T4.SPECIAL_ACT9, ";
        $query .= "     T4.SPECIAL_ACT10, ";
        $query .= "     T4.ACT_TOTAL, ";
        $query .= "     T4.SPECIAL_REMARK, ";
        $query .= "     T4.REMARK1, ";
        $query .= "     T4.REMARK2, ";
        $query .= "     T4.REMARK3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T4.EXAMNO = '{$examno}' ";

        return $query;
    }

    //更新処理
    public function updateData($db, $model)
    {
        $cnt = 0;
        $grades = array();
        $result = $db->query(knjl013vQuery::getGradeCd($db, $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grades[] = $row['GRADE_CD'];
            $cnt++;
            if ($model->examSchoolKind == 'J') {
                if ($cnt == 6) {
                    break;
                }
            } else {
                if ($cnt == 3) {
                    break;
                }
            }
        }

        for ($counter = 0; $counter < get_count($model->data); $counter++) {
            $model->field = $model->data[$counter];

            if ($model->selectPattern == '1') {
                $i = 0;
            }
            if ($model->selectPattern == '2') {
                $i = 1;
            }
            if ($model->selectPattern == '3') {
                $i = 2;
            }
            if ($model->selectPattern == '8') {
                $i = 3;
            }
            if ($model->selectPattern == '9') {
                $i = 4;
            }
            if ($model->selectPattern == '10') {
                $i = 5;
            }
            if ($model->selectPattern == '1' || $model->selectPattern == '2' || $model->selectPattern == '3' || $model->selectPattern == '8' || $model->selectPattern == '9' || $model->selectPattern == '10') {
                $grades[$i];
                $idx = $i + 1;

                $data = array();
                $data["JAPANESE"][TEXT]        = $model->field['JAPANESE' . $idx];
                $data["MATH"][TEXT]            = $model->field['MATH' . $idx];
                $data["SOCIETY"][TEXT]         = $model->field['SOCIETY' . $idx];
                $data["SCIENCE"][TEXT]         = $model->field['SCIENCE' . $idx];
                $data["ENGLISH"][TEXT]         = $model->field['ENGLISH' . $idx];
                $data["HEALTH_PHYSICAL"][TEXT] = $model->field['HEALTH_PHYSICAL' . $idx];
                $data["TECH_HOME"][TEXT]       = $model->field['TECH_HOME' . $idx];
                $data["MUSIC"][TEXT]           = $model->field['MUSIC' . $idx];
                $data["ART"][TEXT]             = $model->field['ART' . $idx];

                $sum = 0;
                for ($j = 0; $j < get_count($model->total3); $j++) {
                    $sum += intval($model->field[$model->total3[$j] . $idx]);
                }
                $data["TOTAL3"][TEXT]          = $sum;

                $sum = 0;
                for ($j = 0; $j < get_count($model->total5); $j++) {
                    $sum += intval($model->field[$model->total5[$j] . $idx]);
                }
                $data["TOTAL5"][TEXT]          = $sum;

                $sum = 0;
                for ($j = 0; $j < get_count($model->total9); $j++) {
                    $sum += intval($model->field[$model->total9[$j] . $idx]);
                }
                $data["TOTAL9"][TEXT]          = $sum;

                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][FUNC]         = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->exp_year}' ";
                $where .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";
                $where .= "     AND GRADE = '{$grades[$i]}' ";

                if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_DAT {$where}") > 0) {
                    $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_DAT", $where));
                } else {
                    $data["YEAR"][TEXT]           = $model->exp_year;
                    $data["EXAMNO"][TEXT]         = $model->field["EXAMNO"];
                    $data["GRADE"][TEXT]          = $grades[$i];

                    $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_DAT"));
                }
            }
            if ($model->selectPattern == '4') {
                $loopMax = ($model->examSchoolKind == 'J') ? 6 : 3;
                for ($i = 0; $i < $loopMax; $i++) {
                    $grades[$i];
                    $idx = $i + 1;

                    $data = array();
                    $data["ATTENDANCE"][TEXT]      = $model->field['ATTENDANCE' . $idx];
                    $data["REGISTERCD"][TEXT]      = STAFFCD;
                    $data["UPDATED"][FUNC]         = "sysdate()";

                    $where  = " WHERE ";
                    $where .= "     YEAR = '{$model->exp_year}' ";
                    $where .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";
                    $where .= "     AND GRADE = '{$grades[$i]}' ";

                    if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_DAT {$where}") > 0) {
                        $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_DAT", $where));
                    } else {
                        $data["YEAR"][TEXT]           = $model->exp_year;
                        $data["EXAMNO"][TEXT]         = $model->field["EXAMNO"];
                        $data["GRADE"][TEXT]          = $grades[$i];

                        $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_DAT"));
                    }
                }
            }

            $where2  = " WHERE ";
            $where2 .= "     YEAR = '{$model->exp_year}' ";
            $where2 .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

            if ($model->selectPattern == '5') {
                $data = array();
                $data["SPECIAL_ACT1"][TEXT]      = $model->field['SPECIAL_ACT1'];
                $data["SPECIAL_ACT2"][TEXT]      = $model->field['SPECIAL_ACT2'];
                $data["SPECIAL_ACT3"][TEXT]      = $model->field['SPECIAL_ACT3'];
                $data["SPECIAL_ACT4"][TEXT]      = $model->field['SPECIAL_ACT4'];
                $data["SPECIAL_ACT5"][TEXT]      = $model->field['SPECIAL_ACT5'];
                $data["SPECIAL_ACT6"][TEXT]      = $model->field['SPECIAL_ACT6'];
                $data["SPECIAL_ACT7"][TEXT]      = $model->field['SPECIAL_ACT7'];
                $data["SPECIAL_ACT8"][TEXT]      = $model->field['SPECIAL_ACT8'];
                $data["SPECIAL_ACT9"][TEXT]      = $model->field['SPECIAL_ACT9'];
                $data["SPECIAL_ACT10"][TEXT]     = $model->field['SPECIAL_ACT10'];

                $sum = 0;
                for ($j = 1; $j <= 10; $j++) {
                    if ($model->field['SPECIAL_ACT'.$j] == '1') {
                        $sum++;
                    }
                }
                $data["ACT_TOTAL"][TEXT]         = $sum;
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "sysdate()";

                if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT {$where2}") > 0) {
                    $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT", $where2));
                } else {
                    $data["YEAR"][TEXT]          = $model->exp_year;
                    $data["EXAMNO"][TEXT]        = $model->field["EXAMNO"];

                    $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT"));
                }
            }
            if ($model->selectPattern == '6') {
                $data = array();
                $data["SPECIAL_REMARK"][TEXT]    = $model->field['SPECIAL_REMARK'];
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "sysdate()";

                if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT {$where2}") > 0) {
                    $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT", $where2));
                } else {
                    $data["YEAR"][TEXT]          = $model->exp_year;
                    $data["EXAMNO"][TEXT]        = $model->field["EXAMNO"];

                    $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT"));
                }
            }
            if ($model->selectPattern == '7') {
                $data = array();
                $data["REMARK1"][TEXT]           = $model->field['REMARK1'];
                $data["REMARK2"][TEXT]           = $model->field['REMARK2'];
                $data["REMARK3"][TEXT]           = $model->field['REMARK3'];
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "sysdate()";

                if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT {$where2}") > 0) {
                    $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT", $where2));
                } else {
                    $data["YEAR"][TEXT]          = $model->exp_year;
                    $data["EXAMNO"][TEXT]        = $model->field["EXAMNO"];

                    $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT"));
                }
            }
        }
    }
}
