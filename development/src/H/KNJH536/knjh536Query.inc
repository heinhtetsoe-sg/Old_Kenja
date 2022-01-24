<?php

require_once('for_php7.php');
class knjh536query extends Query
{

    //学期一覧
    public function getSemester()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年一覧
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getGradeHrclass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     'すべて' AS LABEL, ";
        $query .= "     GRADE || '000' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " UNION ALL ";
        $query .= " SELECT DISTINCT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実力種別
    public function getProfDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //実力テスト
    public function getProfMst($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.PROFICIENCYCD AS VALUE, ";
        $query .= "    T1.PROFICIENCYCD || ':' || L1.PROFICIENCYNAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    PROFICIENCY_YMST T1 ";
        $query .= "    LEFT JOIN PROFICIENCY_MST L1 ON T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "         AND T1.PROFICIENCYCD = L1.PROFICIENCYCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "    AND T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //実力科目一覧
    public function getProfSubclassMst($model)
    {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L1.GROUP_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if (substr($model->field["GRADE_HR_CLASS"], 2) == '000') {
            $query .= "     AND T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        } else {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ), GROUP_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN REGD_T L1 ON L1.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND T1.PROFICIENCYCD = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     AND T1.GRADE = CASE WHEN DIV = '01' ";
        $query .= "                         THEN '00' ";
        $query .= "                         ELSE L1.GRADE ";
        $query .= "                    END ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "                                                           THEN '00000000' ";
        $query .= "                                                           ELSE CASE WHEN T1.DIV = '04' ";
        $query .= "                                                                     THEN '0' || L1.GROUP_CD || '0000' ";
        $query .= "                                                                     ELSE L1.COURSECD || L1.MAJORCD || L1.COURSECODE ";
        $query .= "                                                                END ";
        $query .= "                                                      END ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ':' || T1.SUBCLASS_ABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD IN (SELECT I1.PROFICIENCY_SUBCLASS_CD FROM GROUP_T I1) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実力テスト
    public function getProfMstSonzai($data, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "ZEROUME_SELECT") {
            $query .= "     PROFICIENCYDIV, PROFICIENCYCD ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        if ($flg == "ZEROUME" || $flg == "ZEROUME_SELECT") {
            $query .= "     RIGHT('00' || PROFICIENCYDIV, 2) = RIGHT('00' || '".$data["PROFICIENCYDIV"]."', 2) ";
            $query .= "     AND RIGHT('0000' || PROFICIENCYCD, 4) = RIGHT('0000' || '".$data["PROFICIENCYCD"]."', 4) ";
        } else {
            $query .= "     PROFICIENCYDIV = '".$data["PROFICIENCYDIV"]."' ";
            $query .= "     AND PROFICIENCYCD = '".$data["PROFICIENCYCD"]."' ";
        }

        return $query;
    }

    //実力科目
    public function getProfSubclassMstSonzai($data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCY_SUBCLASS_CD = '".$data["PROFICIENCY_SUBCLASS_CD"]."' ";

        return $query;
    }

    //更新
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($idx = 0; $idx < get_count($data_arr); $idx++) {
            $data = array();

            //データセット
            $data["SCORE"][NUMBER]  = $data_arr[$idx]["SCORE"];
            $data["SCORE_DI"][TEXT] = $data_arr[$idx]["SCORE_DI"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $chk_query  = " SELECT COUNT(*) FROM PROFICIENCY_DAT ";
            $chk_query .= " WHERE YEAR = '".$data_arr[$idx]["YEAR"]."' ";
            $chk_query .= "       AND SEMESTER = '".$data_arr[$idx]["SEMESTER"]."' ";
            $chk_query .= "       AND PROFICIENCYDIV = '".$data_arr[$idx]["PROFICIENCYDIV"]."' ";
            $chk_query .= "       AND PROFICIENCYCD = '".$data_arr[$idx]["PROFICIENCYCD"]."' ";
            $chk_query .= "       AND SCHREGNO = '".$data_arr[$idx]["SCHREGNO"]."' ";
            $chk_query .= "       AND PROFICIENCY_SUBCLASS_CD = '".$data_arr[$idx]["PROFICIENCY_SUBCLASS_CD"]."' ";

            if ("0" == $db->getOne($chk_query)) {
                $data["YEAR"][TEXT]                     = $data_arr[$idx]["YEAR"];
                $data["SEMESTER"][TEXT]                 = $data_arr[$idx]["SEMESTER"];
                $data["PROFICIENCYDIV"][TEXT]           = $data_arr[$idx]["PROFICIENCYDIV"];
                $data["PROFICIENCYCD"][TEXT]            = $data_arr[$idx]["PROFICIENCYCD"];
                $data["SCHREGNO"][TEXT]                 = $data_arr[$idx]["SCHREGNO"];
                $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $data_arr[$idx]["PROFICIENCY_SUBCLASS_CD"];
                $query = Query::insertSQL($data, "PROFICIENCY_DAT");
            } else {
                $where  = " WHERE YEAR = '".$data_arr[$idx]["YEAR"]."' ";
                $where .= "       AND SEMESTER = '".$data_arr[$idx]["SEMESTER"]."' ";
                $where .= "       AND PROFICIENCYDIV = '".$data_arr[$idx]["PROFICIENCYDIV"]."' ";
                $where .= "       AND PROFICIENCYCD = '".$data_arr[$idx]["PROFICIENCYCD"]."' ";
                $where .= "       AND SCHREGNO = '".$data_arr[$idx]["SCHREGNO"]."' ";
                $where .= "       AND PROFICIENCY_SUBCLASS_CD = '".$data_arr[$idx]["PROFICIENCY_SUBCLASS_CD"]."' ";
                $query = Query::updateSQL($data, "PROFICIENCY_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($idx = 0; $idx < get_count($data_arr); $idx++) {
            $query  = " DELETE FROM PROFICIENCY_DAT ";
            $query .= " WHERE YEAR = '".$data_arr[$idx]["YEAR"]."' ";
            $query .= "       AND SEMESTER = '".$data_arr[$idx]["SEMESTER"]."' ";
            $query .= "       AND PROFICIENCYDIV = '".$data_arr[$idx]["PROFICIENCYDIV"]."' ";
            $query .= "       AND PROFICIENCYCD = '".$data_arr[$idx]["PROFICIENCYCD"]."' ";
            $query .= "       AND SCHREGNO = '".$data_arr[$idx]["SCHREGNO"]."' ";
            $query .= "       AND PROFICIENCY_SUBCLASS_CD = '".$data_arr[$idx]["PROFICIENCY_SUBCLASS_CD"]."' ";

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
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.PROFICIENCYDIV, ";
        $query .= "     L2.PROFICIENCYCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     L2.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     L3.SUBCLASS_NAME, ";
        $query .= "     L2.SCORE, ";
        $query .= "     L2.SCORE_DI, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN PROFICIENCY_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "          AND L2.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "          AND L2.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "          AND T1.SCHREGNO = L2.SCHREGNO ";
        if ($model->field["PROFICIENCY_SUBCLASS_CD"]) {
            $query .= "          AND L2.PROFICIENCY_SUBCLASS_CD = '".$model->field["PROFICIENCY_SUBCLASS_CD"]."' ";
        }
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST L3 ON L2.PROFICIENCY_SUBCLASS_CD = L3.PROFICIENCY_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if (substr($model->field["GRADE_HR_CLASS"], 2) == '000') {
            $query .= "     AND T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        } else {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["QUALIFIED"] == "2") {
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             SEQ ";
            $query .= "         FROM ";
            $query .= "             SCHREG_QUALIFIED_HOBBY_DAT L5 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = L5.YEAR ";
            $query .= "             AND T1.SCHREGNO = L5.SCHREGNO ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.PROFICIENCYDIV, ";
        $query .= "     L2.PROFICIENCYCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L2.PROFICIENCY_SUBCLASS_CD ";

        return $query;
    }

    public function getKwansei()
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN NAME1 = 'kwansei' THEN '1' ELSE '0' END KWANSEI_FLG ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        
        return $query;
    }
}
