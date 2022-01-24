<?php

require_once('for_php7.php');
class knjl012vQuery extends Query
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

    //入試区分リスト
    public function getApplicantDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     APPLICANT_DIV AS VALUE, ";
        $query .= "     APPLICANT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->examSchoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     APPLICANT_DIV ";

        return $query;
    }

    //校種リスト
    public function getExamSchoolKind($model)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //志願者リスト
    public function getMenbars($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTBASE_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT T2 ";
        $query .= "     ON T1.EXAMNO = T2.EXAMNO ";
        $query .= "     AND FREQUENCY = (SELECT MAX(FREQUENCY) FROM ENTEXAM_STD_RECEPT_DAT WHERE EXAMNO = T1.EXAMNO) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T2.APPLICANT_DIV = '{$model->applicantDiv}' ";
        $query .= "     AND T2.EXAM_SCHOOL_KIND = '{$model->examSchoolKind}' ";

        return $query;
    }

    //志願者データ
    public function selectQuery($model, $grade1, $grade2, $grade3, $grade4 = '', $grade5 = '', $grade6 = '')
    {
        $query .= " SELECT ";
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
        $query .= "     T1.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
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
        $query .= "     BASE.YEAR = '{$model->exp_year}' ";
        $query .= "     AND BASE.EXAMNO = '{$model->field['EXAMNO']}' ";

        return $query;
    }

    //志願者データ
    public function selectQueryRemark($model)
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
        $query .= "     AND T4.EXAMNO = '{$model->field['EXAMNO']}' ";

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
        $query .= "     ENTEXAMYEAR = '{$model->exp_year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->examSchoolKind}' ";
        $query .= "     AND SETTING_CD = 'L008' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //性別
    public function getSex($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z002' ";

        return $query;
    }

    //続柄
    public function getRelationship($model)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 IN ('H201') AND ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";

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

    //レコードの有無
    public function getMemberCount($model)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND EXAMNO = '{$model->field['EXAMNO']}' ";

        return $query;
    }
    //更新処理
    public function updateData($db, $model)
    {
        $loopMax = ($model->examSchoolKind == 'J') ? 6 : 3;
        for ($i = 0; $i < $loopMax; $i++) {
            $idx = $i + 1;

            $data["JAPANESE"][TEXT]        = $model->field['JAPANESE' . $idx];
            $data["MATH"][TEXT]            = $model->field['MATH' . $idx];
            $data["SOCIETY"][TEXT]         = $model->field['SOCIETY' . $idx];
            $data["SCIENCE"][TEXT]         = $model->field['SCIENCE' . $idx];
            $data["ENGLISH"][TEXT]         = $model->field['ENGLISH' . $idx];
            $data["HEALTH_PHYSICAL"][TEXT] = $model->field['HEALTH_PHYSICAL' . $idx];
            $data["TECH_HOME"][TEXT]       = $model->field['TECH_HOME' . $idx];
            $data["MUSIC"][TEXT]           = $model->field['MUSIC' . $idx];
            $data["ART"][TEXT]             = $model->field['ART' . $idx];
            $data["TOTAL3"][TEXT]          = $model->field['TOTAL3' . $idx];
            $data["TOTAL5"][TEXT]          = $model->field['TOTAL5' . $idx];
            $data["TOTAL9"][TEXT]          = $model->field['TOTAL9' . $idx];
            $data["ATTENDANCE"][TEXT]      = $model->field['ATTENDANCE' . $idx];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->exp_year}' ";
            $where .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";
            $where .= "     AND GRADE = '0{$idx}' ";

            if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_DAT {$where}") > 0) {
                $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_DAT", $where));
            } else {
                $data["YEAR"][TEXT]           = $model->exp_year;
                $data["EXAMNO"][TEXT]         = $model->field["EXAMNO"];
                $data["GRADE"][TEXT]          = '0' . $idx;

                $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_DAT"));
            }
        }

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
        $data["ACT_TOTAL"][TEXT]         = $model->field['ACT_TOTAL'];
        $data["SPECIAL_REMARK"][TEXT]    = $model->field['SPECIAL_REMARK'];
        $data["REMARK1"][TEXT]           = $model->field['REMARK1'];
        $data["REMARK2"][TEXT]           = $model->field['REMARK2'];
        $data["REMARK3"][TEXT]           = $model->field['REMARK3'];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->exp_year}' ";
        $where .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

        if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT {$where}") > 0) {
            $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT", $where));
        } else {
            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["EXAMNO"][TEXT]        = $model->field["EXAMNO"];

            $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT"));
        }
    }

    //削除処理
    public function deleteData($db, $model)
    {
        $query  = " DELETE FROM ENTEXAM_STD_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

        $db->query($query);

        $query  = " DELETE FROM ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

        $db->query($query);
    }
}
