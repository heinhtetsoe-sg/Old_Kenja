<?php

require_once('for_php7.php');

class knjf301Query extends Query {

    //教育委員会用の学校コード取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".CTRL_YEAR."' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //Z010
    function getZ010()
    {
        $query  = "SELECT NAME1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学習記録データの存在確認
    function getRecSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_studyrec_dat ";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        return $query;
    }
    //学籍在籍データの存在確認
    function getSchSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_regd_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }
    //評定マスタの存在確認
    function getAssesscd()
    {
        $query  = "SELECT COUNT(assesscd) FROM assess_mst ";
        $query .= " WHERE assesscd = '4' ";
        return $query;
    }

    //コンボボックスの中
    function getGradeQuery()
    {
        $query  = "SELECT DISTINCT(grade) ";
        $query .= "  FROM schreg_regd_hdat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= " ORDER BY GRADE";

        return $query;
    }

    //データ表示
    function ReadQuery($model) {
        $query  = "";
        //学年・性別
        $query .= " WITH T_GRADE (GRADE, GRADE_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T3.GRADE_NAME1 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "             ON  T3.YEAR     = T1.YEAR ";
        $query .= "             AND T3.GRADE    = T1.GRADE ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T3.GRADE_NAME1 ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('99', '合計') ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('9', '合計') ";
        $query .= "     ) ";
        $query .= " , T_GRADE_SEX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_GRADE T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.GRADE = '99' OR T2.SEX != '9' ";
        $query .= "     ) ";
        if ($model->cmd == "recalc") {
            //再計算
            $query .= " , T_RECALC AS ( ";
            $query .= "     SELECT ";
            $query .= "         VALUE(T1.GRADE, '99') AS GRADE, ";
            $query .= "         VALUE(T2.SEX, '9') AS SEX, ";
            $query .= "         SUM(CASE WHEN L1.NUTRITIONCD = '01' OR L1.NUTRITIONCD IS NULL THEN 1 ELSE 0 END) AS NUTRITIONCD01, ";
            $query .= "         SUM(CASE WHEN L1.NUTRITIONCD = '02' THEN 1 ELSE 0 END) AS NUTRITIONCD02, ";
            $query .= "         SUM(CASE WHEN L1.NUTRITIONCD = '03' THEN 1 ELSE 0 END) AS NUTRITIONCD03, ";
            $query .= "         SUM(CASE WHEN L1.SPINERIBCD = '01' OR L1.SPINERIBCD IS NULL THEN 1 ELSE 0 END) AS SPINERIBCD01, ";
            $query .= "         SUM(CASE WHEN L1.SPINERIBCD = '02' THEN 1 ELSE 0 END) AS SPINERIBCD02, ";
            $query .= "         SUM(CASE WHEN L1.SPINERIBCD = '03' THEN 1 ELSE 0 END) AS SPINERIBCD03, ";
            $query .= "         SUM(CASE WHEN L1.SPINERIBCD = '99' THEN 1 ELSE 0 END) AS SPINERIBCD99, ";
            $query .= "         SUM(CASE WHEN L1.SKINDISEASECD = '01' OR L1.SKINDISEASECD IS NULL THEN 1 ELSE 0 END) AS SKINDISEASECD01, ";
            $query .= "         SUM(CASE WHEN L1.SKINDISEASECD = '02' THEN 1 ELSE 0 END) AS SKINDISEASECD02, ";
            $query .= "         SUM(CASE WHEN L1.SKINDISEASECD = '03' THEN 1 ELSE 0 END) AS SKINDISEASECD03, ";
            $query .= "         SUM(CASE WHEN L1.SKINDISEASECD = '99' THEN 1 ELSE 0 END) AS SKINDISEASECD99, ";
            $query .= "         SUM(CASE WHEN L1.OTHERDISEASECD = '01' OR L1.OTHERDISEASECD IS NULL THEN 1 ELSE 0 END) AS OTHERDISEASECD01, ";
            $query .= "         SUM(CASE WHEN L1.OTHERDISEASECD = '02' THEN 1 ELSE 0 END) AS OTHERDISEASECD02, ";
            $query .= "         SUM(CASE WHEN L1.OTHERDISEASECD = '03' THEN 1 ELSE 0 END) AS OTHERDISEASECD03, ";
            $query .= "         SUM(CASE WHEN L1.OTHERDISEASECD = '04' THEN 1 ELSE 0 END) AS OTHERDISEASECD04, ";
            $query .= "         SUM(CASE WHEN L1.OTHERDISEASECD = '05' THEN 1 ELSE 0 END) AS OTHERDISEASECD05, ";
            $query .= "         SUM(CASE WHEN L1.OTHERDISEASECD = '99' THEN 1 ELSE 0 END) AS OTHERDISEASECD99 ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
            $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "             ON  T3.YEAR     = T1.YEAR ";
            $query .= "             AND T3.GRADE    = T1.GRADE ";
            $query .= "         LEFT JOIN MEDEXAM_DET_DAT L1 ";
            $query .= "             ON  L1.YEAR     = T1.YEAR ";
            $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     WHERE ";
            $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     GROUP BY ";
            $query .= "         GROUPING SETS ((T1.GRADE, T2.SEX), (T2.SEX), ()) ";
            $query .= "     ) ";
        } else {
            //登録テーブル
            $query .= " , T_ADDITION1 AS ( ";
            $query .= "     SELECT ";
            $query .= "         * ";
            $query .= "     FROM ";
            $query .= "         MEDEXAM_DISEASE_ADDITION1_DAT ";
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND YEAR = '".CTRL_YEAR."' ";
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.GRADE_NAME, ";
        $query .= "     T1.SEX_NAME, ";
        $query .= "     L1.NUTRITIONCD01, ";
        $query .= "     L1.NUTRITIONCD02, ";
        $query .= "     L1.NUTRITIONCD03, ";
        $query .= "     L1.SPINERIBCD01, ";
        $query .= "     L1.SPINERIBCD02, ";
        $query .= "     L1.SPINERIBCD03, ";
        $query .= "     L1.SPINERIBCD99, ";
        $query .= "     L1.SKINDISEASECD01, ";
        $query .= "     L1.SKINDISEASECD02, ";
        $query .= "     L1.SKINDISEASECD03, ";
        $query .= "     L1.SKINDISEASECD99, ";
        $query .= "     L1.OTHERDISEASECD01, ";
        $query .= "     L1.OTHERDISEASECD02, ";
        $query .= "     L1.OTHERDISEASECD03, ";
        $query .= "     L1.OTHERDISEASECD04, ";
        $query .= "     L1.OTHERDISEASECD05, ";
        $query .= "     L1.OTHERDISEASECD99 ";
        $query .= " FROM ";
        $query .= "     T_GRADE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ";
        } else {
            $query .= "     LEFT JOIN T_ADDITION1 L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEX ";
        return $query;
    }

    //学年人数合計
    function &getAllCntQuery($model)
    {
        $query  = "SELECT COUNT(schregno) AS GRADE_MEMBER ";
        $query .= "  FROM schreg_regd_dat w1 ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= "   AND grade    = '".$model->gc_select."'";
        $query .= "   AND NOT EXISTS ( ";
        $query .= "                    SELECT 'X' ";
        $query .= "                    FROM schreg_base_mst w2 ";
        $query .= "                    WHERE w2.schregno = w1.schregno ";
        $query .= "                      AND w2.grd_div in ('2','3') "; //2:退学 3:転学 者は除く
        $query .= "                   ) ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION1_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR      = '".CTRL_YEAR."'";
        $db->query($query);

        //INSERT
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //学年・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["GRADE"][TEXT]                = $code_arr[0];
            $data["SEX"][TEXT]                  = $code_arr[1];

            //人数
            $data["NUTRITIONCD01"][NUMBER]      = $model->fields["NUTRITIONCD01"][$i];
            $data["NUTRITIONCD02"][NUMBER]      = $model->fields["NUTRITIONCD02"][$i];
            $data["NUTRITIONCD03"][NUMBER]      = $model->fields["NUTRITIONCD03"][$i];
            $data["SPINERIBCD01"][NUMBER]       = $model->fields["SPINERIBCD01"][$i];
            $data["SPINERIBCD02"][NUMBER]       = $model->fields["SPINERIBCD02"][$i];
            $data["SPINERIBCD03"][NUMBER]       = $model->fields["SPINERIBCD03"][$i];
            $data["SPINERIBCD99"][NUMBER]       = $model->fields["SPINERIBCD99"][$i];
            $data["SKINDISEASECD01"][NUMBER]    = $model->fields["SKINDISEASECD01"][$i];
            $data["SKINDISEASECD02"][NUMBER]    = $model->fields["SKINDISEASECD02"][$i];
            $data["SKINDISEASECD03"][NUMBER]    = $model->fields["SKINDISEASECD03"][$i];
            $data["SKINDISEASECD99"][NUMBER]    = $model->fields["SKINDISEASECD99"][$i];
            $data["OTHERDISEASECD01"][NUMBER]   = $model->fields["OTHERDISEASECD01"][$i];
            $data["OTHERDISEASECD02"][NUMBER]   = $model->fields["OTHERDISEASECD02"][$i];
            $data["OTHERDISEASECD03"][NUMBER]   = $model->fields["OTHERDISEASECD03"][$i];
            $data["OTHERDISEASECD04"][NUMBER]   = $model->fields["OTHERDISEASECD04"][$i];
            $data["OTHERDISEASECD05"][NUMBER]   = $model->fields["OTHERDISEASECD05"][$i];
            $data["OTHERDISEASECD99"][NUMBER]   = $model->fields["OTHERDISEASECD99"][$i];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION1_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     T1.GRADE || ':' || T1.SEX || ':' || CHAR(T1.EXECUTE_DATE) AS VALUE, ";
        $query .= "     L1.GRADE_NAME1 || '　' || L2.NAME1 || '　' || CHAR(T1.EXECUTE_DATE) AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_ADDITION1_DAT T1";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "         ON  L1.YEAR     = T1.YEAR ";
        $query .= "         AND L1.GRADE    = T1.GRADE ";
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "         ON  L2.NAMECD1  = 'Z002' ";
        $query .= "         AND L2.NAMECD2  = T1.SEX ";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.EXECUTE_DATE DESC ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getAddition1Dat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION1_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //DELETE
        //県側・疾病等結果一覧テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION1_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $db2->query($query);
        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_DISEASE_ADDITION1_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $db->query($query);
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（疾病等結果一覧）
        $query = knjf301Query::getAddition1Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            //人数
            $data["NUTRITIONCD01"][NUMBER]      = $row["NUTRITIONCD01"];
            $data["NUTRITIONCD02"][NUMBER]      = $row["NUTRITIONCD02"];
            $data["NUTRITIONCD03"][NUMBER]      = $row["NUTRITIONCD03"];
            $data["SPINERIBCD01"][NUMBER]       = $row["SPINERIBCD01"];
            $data["SPINERIBCD02"][NUMBER]       = $row["SPINERIBCD02"];
            $data["SPINERIBCD03"][NUMBER]       = $row["SPINERIBCD03"];
            $data["SPINERIBCD99"][NUMBER]       = $row["SPINERIBCD99"];
            $data["SKINDISEASECD01"][NUMBER]    = $row["SKINDISEASECD01"];
            $data["SKINDISEASECD02"][NUMBER]    = $row["SKINDISEASECD02"];
            $data["SKINDISEASECD03"][NUMBER]    = $row["SKINDISEASECD03"];
            $data["SKINDISEASECD99"][NUMBER]    = $row["SKINDISEASECD99"];
            $data["OTHERDISEASECD01"][NUMBER]   = $row["OTHERDISEASECD01"];
            $data["OTHERDISEASECD02"][NUMBER]   = $row["OTHERDISEASECD02"];
            $data["OTHERDISEASECD03"][NUMBER]   = $row["OTHERDISEASECD03"];
            $data["OTHERDISEASECD04"][NUMBER]   = $row["OTHERDISEASECD04"];
            $data["OTHERDISEASECD05"][NUMBER]   = $row["OTHERDISEASECD05"];
            $data["OTHERDISEASECD99"][NUMBER]   = $row["OTHERDISEASECD99"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION1_DAT");
            $db2->query($query);

            //県側、学校側・報告テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["EXECUTE_DATE"][TEXT]         = str_replace("/","-",$model->execute_date);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "REPORT_DISEASE_ADDITION1_DAT");
            $db->query($query);
            $db2->query($query);
        }
        $result->free();

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }
}
?>
