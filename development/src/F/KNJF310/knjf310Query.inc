<?php

require_once('for_php7.php');

class knjf310Query extends Query {

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
            $query .= "         COUNT(*) AS REMARK101, ";
            $query .= "         SUM(CASE WHEN VALUE(TOOTH.TREATEDBABYTOOTH, 0) + VALUE(TOOTH.REMAINBABYTOOTH, 0) + VALUE(TOOTH.TREATEDADULTTOOTH, 0) + VALUE(TOOTH.REMAINADULTTOOTH, 0) = 0 THEN 1 ELSE 0 END) AS REMARK102, ";
            $query .= "         SUM(CASE WHEN VALUE(TOOTH.TREATEDBABYTOOTH, 0) + VALUE(TOOTH.REMAINBABYTOOTH, 0) + VALUE(TOOTH.TREATEDADULTTOOTH, 0) + VALUE(TOOTH.REMAINADULTTOOTH, 0) >= 1 THEN 1 ELSE 0 END) AS REMARK103, ";
            $query .= "         SUM(CASE WHEN VALUE(TOOTH.REMAINBABYTOOTH, 0) + VALUE(TOOTH.REMAINADULTTOOTH, 0) >= 1 THEN 1 ELSE 0 END) AS REMARK104, ";
            $query .= "         SUM(CASE WHEN VALUE(TOOTH.REMAINBABYTOOTH, 0) + VALUE(TOOTH.REMAINADULTTOOTH, 0) = 0 AND VALUE(TOOTH.TREATEDBABYTOOTH, 0) + VALUE(TOOTH.TREATEDADULTTOOTH, 0) >= 1 THEN 1 ELSE 0 END) AS REMARK105, ";
            $query .= "         SUM(CASE WHEN VALUE(TOOTH.BRACK_BABYTOOTH, 0) >= 1 THEN 1 ELSE 0 END) AS REMARK106, ";
            $query .= "         SUM(CASE WHEN VALUE(TOOTH.BRACK_ADULTTOOTH, 0) >= 1 THEN 1 ELSE 0 END) AS REMARK107, ";
            $query .= "         SUM(CASE WHEN VALUE(TOOTH.LOSTADULTTOOTH, 0) >= 1 THEN 1 ELSE 0 END) AS REMARK108, ";
            $query .= "         SUM(CASE WHEN F510.ABBV3 = '10' THEN 1 ELSE 0 END) AS REMARK201, ";
            $query .= "         SUM(CASE WHEN F510.ABBV3 = '20' THEN 1 ELSE 0 END) AS REMARK202, ";
            $query .= "         SUM(CASE WHEN F510.ABBV3 = '30' THEN 1 ELSE 0 END) AS REMARK203, ";
            $query .= "         SUM(CASE WHEN F511.ABBV3 = '10' THEN 1 ELSE 0 END) AS REMARK204, ";
            $query .= "         SUM(CASE WHEN F511.ABBV3 = '20' THEN 1 ELSE 0 END) AS REMARK205, ";
            $query .= "         SUM(CASE WHEN F511.ABBV3 = '30' THEN 1 ELSE 0 END) AS REMARK206, ";
            $query .= "         SUM(CASE WHEN F520.ABBV3 = '10' THEN 1 ELSE 0 END) AS REMARK207, ";
            $query .= "         SUM(CASE WHEN F520.ABBV3 = '20' THEN 1 ELSE 0 END) AS REMARK208, ";
            $query .= "         SUM(CASE WHEN F520.ABBV3 = '30' THEN 1 ELSE 0 END) AS REMARK209, ";
            $query .= "         SUM(CASE WHEN F513.ABBV3 = '10' THEN 1 ELSE 0 END) AS REMARK210, ";
            $query .= "         SUM(CASE WHEN F513.ABBV3 = '20' THEN 1 ELSE 0 END) AS REMARK211, ";
            $query .= "         SUM(CASE WHEN F513.ABBV3 = '30' THEN 1 ELSE 0 END) AS REMARK212 ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
            $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "             ON  T3.YEAR     = T1.YEAR ";
            $query .= "             AND T3.GRADE    = T1.GRADE ";
            $query .= "         INNER JOIN MEDEXAM_TOOTH_DAT TOOTH ";
            $query .= "             ON  TOOTH.YEAR     = T1.YEAR ";
            $query .= "             AND TOOTH.SCHREGNO    = T1.SCHREGNO ";
            $query .= "         LEFT JOIN NAME_MST F510 ON F510.NAMECD1 = 'F510' ";
            $query .= "              AND F510.NAMECD2 = TOOTH.JAWS_JOINTCD ";
            $query .= "         LEFT JOIN NAME_MST F511 ON F511.NAMECD1 = 'F511' ";
            $query .= "              AND F511.NAMECD2 = TOOTH.JAWS_JOINTCD2 ";
            $query .= "         LEFT JOIN NAME_MST F520 ON F520.NAMECD1 = 'F520' ";
            $query .= "              AND F520.NAMECD2 = TOOTH.PLAQUECD ";
            $query .= "         LEFT JOIN NAME_MST F513 ON F513.NAMECD1 = 'F513' ";
            $query .= "              AND F513.NAMECD2 = TOOTH.GUMCD ";
            $query .= "     WHERE ";
            $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     GROUP BY ";
            $query .= "         GROUPING SETS ((T1.GRADE, T2.SEX), (T2.SEX), ()) ";
            $query .= "     ) ";
        } else {
            //登録テーブル
            $query .= " , T_ADDITION AS ( ";
            $query .= "     SELECT ";
            $query .= "         GRADE, ";
            $query .= "         SEX, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '101' THEN COUNT ELSE 0 END) AS REMARK101, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '102' THEN COUNT ELSE 0 END) AS REMARK102, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '103' THEN COUNT ELSE 0 END) AS REMARK103, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '104' THEN COUNT ELSE 0 END) AS REMARK104, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '105' THEN COUNT ELSE 0 END) AS REMARK105, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '106' THEN COUNT ELSE 0 END) AS REMARK106, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '107' THEN COUNT ELSE 0 END) AS REMARK107, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '108' THEN COUNT ELSE 0 END) AS REMARK108, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '201' THEN COUNT ELSE 0 END) AS REMARK201, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '202' THEN COUNT ELSE 0 END) AS REMARK202, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '203' THEN COUNT ELSE 0 END) AS REMARK203, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '204' THEN COUNT ELSE 0 END) AS REMARK204, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '205' THEN COUNT ELSE 0 END) AS REMARK205, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '206' THEN COUNT ELSE 0 END) AS REMARK206, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '207' THEN COUNT ELSE 0 END) AS REMARK207, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '208' THEN COUNT ELSE 0 END) AS REMARK208, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '209' THEN COUNT ELSE 0 END) AS REMARK209, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '210' THEN COUNT ELSE 0 END) AS REMARK210, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '211' THEN COUNT ELSE 0 END) AS REMARK211, ";
            $query .= "         SUM(CASE WHEN REMARK_DIV = '212' THEN COUNT ELSE 0 END) AS REMARK212 ";
            $query .= "     FROM ";
            if ($model->fixedData) {
                $query .= "         MEDEXAM_DISEASE_ADDITION5_FIXED_DAT ";
            } else {
                $query .= "         MEDEXAM_DISEASE_ADDITION5_DAT ";
            }
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND SHORI_CD = '01' ";
            $query .= "         AND YEAR = '".CTRL_YEAR."' ";
            if ($model->fixedData) {
                $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
            }
            $query .= "     GROUP BY ";
            $query .= "         GRADE, ";
            $query .= "         SEX ";
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.GRADE_NAME, ";
        $query .= "     T1.SEX_NAME, ";
        $query .= "     L1.REMARK101, ";
        $query .= "     L1.REMARK102, ";
        $query .= "     L1.REMARK103, ";
        $query .= "     L1.REMARK104, ";
        $query .= "     L1.REMARK105, ";
        $query .= "     L1.REMARK106, ";
        $query .= "     L1.REMARK107, ";
        $query .= "     L1.REMARK108, ";
        $query .= "     L1.REMARK201, ";
        $query .= "     L1.REMARK202, ";
        $query .= "     L1.REMARK203, ";
        $query .= "     L1.REMARK204, ";
        $query .= "     L1.REMARK205, ";
        $query .= "     L1.REMARK206, ";
        $query .= "     L1.REMARK207, ";
        $query .= "     L1.REMARK208, ";
        $query .= "     L1.REMARK209, ";
        $query .= "     L1.REMARK210, ";
        $query .= "     L1.REMARK211, ";
        $query .= "     L1.REMARK212 ";
        $query .= " FROM ";
        $query .= "     T_GRADE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ";
        } else {
            $query .= "     LEFT JOIN T_ADDITION L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ";
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
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION5_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND SHORI_CD  = '01' ";
        $query .= "   AND YEAR      = '".CTRL_YEAR."'";
        $db->query($query);

        //備考区分
        $codeArray = array("101"
                          ,"102"
                          ,"103"
                          ,"104"
                          ,"105"
                          ,"106"
                          ,"107"
                          ,"108"
                          ,"201"
                          ,"202"
                          ,"203"
                          ,"204"
                          ,"205"
                          ,"206"
                          ,"207"
                          ,"208"
                          ,"209"
                          ,"210"
                          ,"211"
                          ,"212");

        //INSERT
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //学年・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);
            //備考区分
            foreach ($codeArray as $code) {
                //キー
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                $data["SHORI_CD"][TEXT]             = '01';
                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["GRADE"][TEXT]                = $code_arr[0];
                $data["SEX"][TEXT]                  = $code_arr[1];
                $data["REMARK_DIV"][TEXT]           = $code;
                //人数
                $data["COUNT"][NUMBER]              = $model->fields["REMARK".$code][$i];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            =" sysdate()";

                $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION5_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION5_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knjf310Query::getAddition5Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["SHORI_CD"][TEXT]             = $row["SHORI_CD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["REMARK_DIV"][TEXT]           = $row["REMARK_DIV"];
            //人数
            $data["COUNT"][NUMBER]              = $row["COUNT"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION5_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE AS LABEL, ";
        $query .= "     T1.EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_ADDITION5_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC ";

        return $query;
    }

    //確定コンボ
    function getFixed($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIXED_DATE AS LABEL, ";
        $query .= "     T1.FIXED_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION5_FIXED_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.FIXED_DATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.FIXED_DATE DESC ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getAddition5Dat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION5_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND SHORI_CD = '01' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getAddition5FixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION5_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND SHORI_CD = '01' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_DISEASE_ADDITION5_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_DISEASE_ADDITION5_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・疾病等結果一覧テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION5_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND SHORI_CD          = '01' ";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（疾病等結果一覧）
        $query = knjf310Query::getAddition5FixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["SHORI_CD"][TEXT]             = $row["SHORI_CD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["REMARK_DIV"][TEXT]           = $row["REMARK_DIV"];
            //人数
            $data["COUNT"][NUMBER]              = $row["COUNT"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION5_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSV
    function selectCsvQuery($model) {
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

        //登録テーブル
        $query .= " , T_ADDITION AS ( ";
        $query .= "     SELECT ";
        $query .= "         GRADE, ";
        $query .= "         SEX, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '101' THEN COUNT ELSE 0 END) AS REMARK101, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '102' THEN COUNT ELSE 0 END) AS REMARK102, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '103' THEN COUNT ELSE 0 END) AS REMARK103, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '104' THEN COUNT ELSE 0 END) AS REMARK104, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '105' THEN COUNT ELSE 0 END) AS REMARK105, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '106' THEN COUNT ELSE 0 END) AS REMARK106, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '107' THEN COUNT ELSE 0 END) AS REMARK107, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '108' THEN COUNT ELSE 0 END) AS REMARK108, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '201' THEN COUNT ELSE 0 END) AS REMARK201, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '202' THEN COUNT ELSE 0 END) AS REMARK202, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '203' THEN COUNT ELSE 0 END) AS REMARK203, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '204' THEN COUNT ELSE 0 END) AS REMARK204, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '205' THEN COUNT ELSE 0 END) AS REMARK205, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '206' THEN COUNT ELSE 0 END) AS REMARK206, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '207' THEN COUNT ELSE 0 END) AS REMARK207, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '208' THEN COUNT ELSE 0 END) AS REMARK208, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '209' THEN COUNT ELSE 0 END) AS REMARK209, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '210' THEN COUNT ELSE 0 END) AS REMARK210, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '211' THEN COUNT ELSE 0 END) AS REMARK211, ";
        $query .= "         SUM(CASE WHEN REMARK_DIV = '212' THEN COUNT ELSE 0 END) AS REMARK212 ";
        $query .= "     FROM ";
        if ($model->fixedData) {
            $query .= "         MEDEXAM_DISEASE_ADDITION5_FIXED_DAT ";
        } else {
            $query .= "         MEDEXAM_DISEASE_ADDITION5_DAT ";
        }
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND SHORI_CD = '01' ";
        $query .= "         AND YEAR = '".CTRL_YEAR."' ";
        if ($model->fixedData) {
            $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         GRADE, ";
        $query .= "         SEX ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.GRADE_NAME, ";
        $query .= "     T1.SEX_NAME, ";
        $query .= "     L1.REMARK101, ";
        $query .= "     L1.REMARK102, ";
        $query .= "     L1.REMARK103, ";
        $query .= "     L1.REMARK104, ";
        $query .= "     L1.REMARK105, ";
        $query .= "     L1.REMARK106, ";
        $query .= "     L1.REMARK107, ";
        $query .= "     L1.REMARK108, ";
        $query .= "     L1.REMARK201, ";
        $query .= "     L1.REMARK202, ";
        $query .= "     L1.REMARK203, ";
        $query .= "     L1.REMARK204, ";
        $query .= "     L1.REMARK205, ";
        $query .= "     L1.REMARK206, ";
        $query .= "     L1.REMARK207, ";
        $query .= "     L1.REMARK208, ";
        $query .= "     L1.REMARK209, ";
        $query .= "     L1.REMARK210, ";
        $query .= "     L1.REMARK211, ";
        $query .= "     L1.REMARK212 ";
        $query .= " FROM ";
        $query .= "     T_GRADE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ";
        } else {
            $query .= "     LEFT JOIN T_ADDITION L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEX ";

        return $query;
    }
}
?>
