<?php

require_once('for_php7.php');
class knjl011vQuery extends Query
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
    public function getApplicantDiv($model, $examSchoolKind)
    {
        $query  = " SELECT ";
        $query .= "     APPLICANT_DIV AS VALUE, ";
        $query .= "     APPLICANT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$examSchoolKind}' ";
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
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTBASE_DAT T1 ";
        if ($model->applicantDiv != '' || $model->examSchoolKind != '') {
            $query .= " INNER JOIN ";
            $query .= "     ENTEXAM_STD_RECEPT_DAT T2 ";
            $query .= "     ON T1.EXAMNO = T2.EXAMNO ";
            $query .= "     AND FREQUENCY = (SELECT MAX(FREQUENCY) FROM ENTEXAM_STD_RECEPT_DAT WHERE EXAMNO = T1.EXAMNO) ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        if ($model->applicantDiv != '') {
            $query .= "     AND T2.APPLICANT_DIV = '{$model->applicantDiv}' ";
        }
        if ($model->examSchoolKind != '') {
            $query .= "     AND T2.EXAM_SCHOOL_KIND = '{$model->examSchoolKind}' ";
        }

        return $query;
    }

    //志願者データ
    public function selectQuery($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINISH_DATE, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.TELNO, ";
        $query .= "     T1.EMAIL, ";
        $query .= "     T2.GUARD_NAME, ";
        $query .= "     T2.GUARD_NAME_KANA, ";
        $query .= "     T2.RELATION, ";
        $query .= "     T2.GUARD_ZIP, ";
        $query .= "     T2.GUARD_ADDR1, ";
        $query .= "     T2.GUARD_ADDR2, ";
        $query .= "     T2.GUARD_TELNO, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     T1.REMARK4, ";
        $query .= "     T1.REMARK5 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_STD_APPLICANTGUARDIAN_DAT T2 ON T1.YEAR = T2.YEAR AND T1.EXAMNO = T2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.EXAMNO = '{$model->field['EXAMNO']}' ";

        return $query;
    }

    //学校情報
    public function getFinschoolName($finschoolcd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$finschoolcd}' ";

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
        $query .= "     AND SETTING_CD = 'L080' ";
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
    //追加処理
    public function insertMember($db, $model)
    {
        $data = array();
        $data["YEAR"][TEXT]        = $model->exp_year;
        $data["EXAMNO"][TEXT]      = $model->field["EXAMNO"];
        $data["NAME"][TEXT]        = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]   = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]         = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]    = str_replace('/', '-', $model->field["BIRTHDAY"]);
        $data["FINSCHOOLCD"][TEXT] = $model->field["FINSCHOOLCD"];
        $data["FINISH_DATE"][TEXT] = str_replace('/', '-', $model->field["FINISH_DATE"]);
        $data["ZIPCD"][TEXT]       = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]       = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]       = $model->field["ADDR2"];
        $data["TELNO"][TEXT]       = $model->field["TELNO"];
        $data["EMAIL"][TEXT]       = $model->field["EMAIL"];
        $data["REMARK1"][TEXT]     = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]     = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]     = $model->field["REMARK3"];
        $data["REMARK4"][TEXT]     = $model->field["REMARK4"];
        $data["REMARK5"][TEXT]     = $model->field["REMARK5"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTBASE_DAT"));

        $data = array();
        $data["YEAR"][TEXT]            = $model->exp_year;
        $data["EXAMNO"][TEXT]          = $model->field["EXAMNO"];
        $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
        $data["GUARD_NAME_KANA"][TEXT] = $model->field["GUARD_NAME_KANA"];
        $data["RELATION"][TEXT]        = $model->field["RELATION"];
        $data["GUARD_ZIP"][TEXT]       = $model->field["GUARD_ZIP"];
        $data["GUARD_ADDR1"][TEXT]     = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]     = $model->field["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]     = $model->field["GUARD_TELNO"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTGUARDIAN_DAT"));

        knjl011vQuery::insertUpdateRecept($db, $model);
    }
    //更新処理
    public function updateMember($db, $model)
    {
        $data = array();
        $data["NAME"][TEXT]        = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]   = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]         = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]    = str_replace('/', '-', $model->field["BIRTHDAY"]);
        $data["FINSCHOOLCD"][TEXT] = $model->field["FINSCHOOLCD"];
        $data["ZIPCD"][TEXT]       = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]       = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]       = $model->field["ADDR2"];
        $data["TELNO"][TEXT]       = $model->field["TELNO"];
        $data["EMAIL"][TEXT]       = $model->field["EMAIL"];
        $data["FINISH_DATE"][TEXT] = str_replace('/', '-', $model->field["FINISH_DATE"]);
        $data["REMARK1"][TEXT]     = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]     = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]     = $model->field["REMARK3"];
        $data["REMARK4"][TEXT]     = $model->field["REMARK4"];
        $data["REMARK5"][TEXT]     = $model->field["REMARK5"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->exp_year}' ";
        $where .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

        $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTBASE_DAT", $where));

        $data = array();
        $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
        $data["GUARD_NAME_KANA"][TEXT] = $model->field["GUARD_NAME_KANA"];
        $data["RELATION"][TEXT]        = $model->field["RELATION"];
        $data["GUARD_ZIP"][TEXT]       = $model->field["GUARD_ZIP"];
        $data["GUARD_ADDR1"][TEXT]     = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]     = $model->field["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]     = $model->field["GUARD_TELNO"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->exp_year}' ";
        $where .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

        if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTGUARDIAN_DAT {$where}") > 0) {
            $db->query(Query::updateSQL($data, "ENTEXAM_STD_APPLICANTGUARDIAN_DAT", $where));
        } else {
            $data["YEAR"][TEXT]            = $model->exp_year;
            $data["EXAMNO"][TEXT]          = $model->field["EXAMNO"];
            $db->query(Query::insertSQL($data, "ENTEXAM_STD_APPLICANTGUARDIAN_DAT"));
        }


        knjl011vQuery::insertUpdateRecept($db, $model);
    }

    //削除処理
    public function deleteMember($db, $model)
    {
        $query  = " DELETE FROM ENTEXAM_STD_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

        $db->query($query);

        $query  = " DELETE FROM ENTEXAM_STD_APPLICANTGUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";

        $db->query($query);
    }
    //コース取得
    public function selectQuery2($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.EXAM_NAME, ";
        $query .= "     T1.COURSE_DIV, ";
        $query .= "     T1.FREQUENCY, ";
        $query .= "     T2.RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST T1 ";
        $query .= " LEFT JOIN ENTEXAM_STD_RECEPT_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "     AND T2.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= "     AND T2.COURSE_DIV = T1.COURSE_DIV ";
        $query .= "     AND T2.FREQUENCY = T1.FREQUENCY ";
        $query .= "     AND T2.EXAMNO = '{$model->field['EXAMNO']}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.EXAM_SCHOOL_KIND = '{$model->examSchoolKind2}' ";
        $query .= "     AND T1.APPLICANT_DIV = '{$model->applicantDiv2}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSE_DIV, ";
        $query .= "     T1.FREQUENCY ";

        return $query;
    }
    //コース取得
    public function selectQuery3($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.NAME1 AS EXAM_SCHOOL_KIND_DISP, ";
        $query .= "     T4.APPLICANT_NAME AS APPLICANT_DIV_DISP, ";
        $query .= "     T1.EXAM_SCHOOL_KIND, ";
        $query .= "     T1.APPLICANT_DIV, ";
        $query .= "     T1.COURSE_DIV, ";
        $query .= "     T1.FREQUENCY, ";
        $query .= "     T2.RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST T1 ";
        $query .= " INNER JOIN ENTEXAM_STD_RECEPT_DAT T5 ";
        $query .= "     ON T5.YEAR = T1.YEAR ";
        $query .= "     AND T5.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "     AND T5.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= "     AND T5.EXAMNO = '{$model->field['EXAMNO']}' ";
        $query .= " LEFT JOIN ENTEXAM_STD_RECEPT_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "     AND T2.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= "     AND T2.COURSE_DIV = T1.COURSE_DIV ";
        $query .= "     AND T2.FREQUENCY = T1.FREQUENCY ";
        $query .= "     AND T2.EXAMNO = '{$model->field['EXAMNO']}' ";
        $query .= " LEFT JOIN V_NAME_MST T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.NAMECD1 = 'L003' ";
        $query .= "     AND T3.NAMECD2 = T1.EXAM_SCHOOL_KIND ";
        $query .= " LEFT JOIN ENTEXAM_STD_APPLICANT_MST T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "     AND T4.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ";
        $query .= "     AND T4.APPLICANT_DIV = T1.APPLICANT_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAM_SCHOOL_KIND, ";
        $query .= "     T1.APPLICANT_DIV, ";
        $query .= "     T1.COURSE_DIV, ";
        $query .= "     T1.FREQUENCY ";

        return $query;
    }

    //追加更新処理
    public function insertUpdateRecept($db, $model)
    {
        $query  = " DELETE FROM ENTEXAM_STD_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND EXAMNO = '{$model->field["EXAMNO"]}' ";
        $db->query($query);

        for ($i = 0; $i < get_count($model->receptData); $i++) {
            if ($model->receptData[$i]['COURSE_DIV'] == '' || $model->receptData[$i]['FREQUENCY'] == '' || $model->receptData[$i]['RECEPTNO'] == '') {
                continue;
            }
            $data = array();
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["EXAMNO"][TEXT]           = $model->field["EXAMNO"];
            $data["EXAM_SCHOOL_KIND"][TEXT] = $model->examSchoolKind2;
            $data["APPLICANT_DIV"][TEXT]    = $model->applicantDiv2;
            $data["COURSE_DIV"][TEXT]       = $model->receptData[$i]['COURSE_DIV'];
            $data["FREQUENCY"][TEXT]        = $model->receptData[$i]['FREQUENCY'];
            $data["RECEPTNO"][TEXT]         = $model->receptData[$i]['RECEPTNO'];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "ENTEXAM_STD_RECEPT_DAT"));
        }
    }
}
