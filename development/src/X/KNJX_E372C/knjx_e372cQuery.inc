<?php

require_once('for_php7.php');

class knjx_e372cQuery extends Query
{

    //学級コンボ
    public function getGradeHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE ";
        $query .= "     , T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN  SCHREG_REGD_GDAT T2";
        $query .= "         ON T2.YEAR  = T1.YEAR";
        $query .= "        AND T2.GRADE = T1.GRADE";
        $query .= "        AND T2.GRADE_CD = '03' ";
        $query .= "        AND T2.SCHOOL_KIND = 'H' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMstE071()
    {
        $query  = "     SELECT ";
        $query .= "       NAMECD2 AS VALUE, ";
        $query .= "       NAMECD2 || ':' || value(NAME1, '') AS LABEL ";
        $query .= "     FROM ";
        $query .= "       V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1 = 'E071' ";
        $query .= "     ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //推薦枠マスタ登録済み学科CD(専願/併願全て)取得
    public function getRecLimitDepartmentCd($db)
    {
        $query = "   WITH REC_LIMIT AS ( ";
        $query .= "   SELECT ";
        $query .= "     * ";
        $query .= "   FROM ";
        $query .= "     AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "   ) ";
        $query .= "   SELECT DEPARTMENT_S AS DEPARTMENTCD FROM REC_LIMIT ";
        $query .= "   UNION ";
        $query .= "   SELECT DEPARTMENT_H AS DEPARTMENTCD FROM REC_LIMIT ";

        $departmentCdArray = array();
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $departmentCdArray[] = $row["DEPARTMENTCD"];
        }

        return $departmentCdArray;
    }

    //一覧取得
    public function getList($model)
    {
        $query  = "  WITH SCH_HOPE AS ( ";
        $query .= "     SELECT ";
        $query .= "       YEAR, ";
        $query .= "       SCHREGNO ";
        for ($i = 1; $i <= $model->limitCnt; $i++) {
            $hopeOrder = sprintf("%02d", $i);
            $query .= "       , MAX(CASE WHEN HOPE_ORDER = '{$hopeOrder}' THEN DEPARTMENT_CD ELSE NULL END) AS HOPE_ORDER_{$hopeOrder} ";
        }
        $query .= "     FROM ";
        $query .= "       AFT_SCHREG_HOPE_DEPARTMENT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "       YEAR, ";
        $query .= "       SCHREGNO ";
        $query .= "  ) ";
        $query .= "   SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        for ($i = 1; $i <= $model->limitCnt; $i++) {
            $hopeOrder = sprintf("%02d", $i);
            $query .= "     T4.HOPE_ORDER_{$hopeOrder}, ";
        }
        $query .= "     T5.ACTIVITY_CD, ";
        $query .= "     T5.ACTIVITY_CONTENT, ";
        $query .= "     T5.DECLINE_FLG ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN AFT_SCHREG_CONVERT_SCORE_DAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "       ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCH_HOPE T4 ";
        $query .= "       ON T4.YEAR = T1.YEAR ";
        $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN AFT_SCHREG_RECOMMENDATION_INFO_DAT T5 ";
        $query .= "       ON T5.YEAR = T1.YEAR ";
        $query .= "       AND T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "   ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //学籍番号取得
    public function getSchregno($grade, $hrclass, $attendno)
    {
        $query  = "     SELECT ";
        $query .= "       SCHREGNO ";
        $query .= "     FROM ";
        $query .= "       SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND GRADE = '{$grade}' ";
        $query .= "       AND HR_CLASS = '{$hrclass}' ";
        $query .= "       AND ATTENDNO = '{$attendno}' ";

        return $query;
    }

    //存在チェック --AFT_SCHREG_HOPE_DEPARTMENT
    public function getExistsHopeOrderArray($db, $schregno)
    {
        $query  = "     SELECT ";
        $query .= "       HOPE_ORDER ";
        $query .= "     FROM ";
        $query .= "       AFT_SCHREG_HOPE_DEPARTMENT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SCHREGNO = '{$schregno}' ";

        $existsHoperOrderArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $existsHoperOrderArray[] = $row["HOPE_ORDER"];
        }

        return $existsHoperOrderArray;
    }

    //存在チェック --AFT_SCHREG_RECOMMENDATION_INFO_DAT
    public function getExistsCntSchRecInfo($schregno)
    {
        $query  = "     SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "     FROM ";
        $query .= "       AFT_SCHREG_RECOMMENDATION_INFO_DAT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $schregno = $db->getOne(knjx_e372cQuery::getSchregno($data_arr[$i]["GRADE"], $data_arr[$i]["HR_CLASS"], $data_arr[$i]["ATTENDNO"]));

            //削除 -- AFT_SCHREG_HOPE_DEPARTMENT ※入力窓が開いてない順位はゴミになるので削除
            $query  = " DELETE FROM AFT_SCHREG_HOPE_DEPARTMENT ";
            $query .= " WHERE ";
            $query .= "         YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO   = '{$schregno}' ";
            $query .= "     AND HOPE_ORDER > '".sprintf("%02d", $model->limitCnt)."' ";
            $db->query($query);

            //更新 -- AFT_SCHREG_HOPE_DEPARTMENT
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]      = $schregno;
            for ($j = 1; $j <= $model->limitCnt; $j++) {
                $hopeOrder = sprintf("%02d", $j);

                $data["HOPE_ORDER"][TEXT]       = $hopeOrder;
                $data["DEPARTMENT_CD"][TEXT]    = $data_arr[$i]["HOPE_ORDER_".$hopeOrder];

                $existsHoperOrderArray = knjx_e372cQuery::getExistsHopeOrderArray($db, $schregno);
                if (!in_array($hopeOrder, $existsHoperOrderArray)) {
                    //追加
                    $query = Query::insertSQL($data, "AFT_SCHREG_HOPE_DEPARTMENT");
                    if ($data_arr[$i]["HOPE_ORDER_".$hopeOrder] != "") {
                        $db->query($query);
                    }
                } else {
                    //条件
                    $where  = "WHERE   YEAR          = '".CTRL_YEAR."' ";
                    $where .= "  AND   SCHREGNO      = '{$schregno}' ";
                    $where .= "  AND   HOPE_ORDER    = '{$hopeOrder}' ";

                    //更新
                    $query = Query::updateSQL($data, "AFT_SCHREG_HOPE_DEPARTMENT", $where);
                    $db->query($query);
                }
            }

            //更新 -- AFT_SCHREG_RECOMMENDATION_INFO_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["ACTIVITY_CD"][TEXT]      = $data_arr[$i]["ACTIVITY_CD"];
            $data["ACTIVITY_CONTENT"][TEXT] = $data_arr[$i]["ACTIVITY_CONTENT"];
            $data["DECLINE_FLG"][TEXT]      = $data_arr[$i]["DECLINE_FLG"];
            if ($db->getOne(knjx_e372cQuery::getExistsCntSchRecInfo($schregno)) < 1) {
                //追加
                $query = Query::insertSQL($data, "AFT_SCHREG_RECOMMENDATION_INFO_DAT");
                $db->query($query);
            } else {
                //条件
                $where  = "WHERE   YEAR          = '".CTRL_YEAR."' ";
                $where .= "  AND   SCHREGNO      = '{$schregno}' ";

                //更新
                $query = Query::updateSQL($data, "AFT_SCHREG_RECOMMENDATION_INFO_DAT", $where);
                $db->query($query);
            }
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $schregno = $db->getOne(knjx_e372cQuery::getSchregno($data_arr[$i]["GRADE"], $data_arr[$i]["HR_CLASS"], $data_arr[$i]["ATTENDNO"]));

            //削除 -- AFT_SCHREG_HOPE_DEPARTMENT
            $query  = " DELETE FROM AFT_SCHREG_HOPE_DEPARTMENT ";
            $query .= " WHERE ";
            $query .= "         YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO   = '{$schregno}' ";
            $db->query($query);

            //削除 -- AFT_SCHREG_HOPE_DEPARTMENT
            $query  = " DELETE FROM AFT_SCHREG_RECOMMENDATION_INFO_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO   = '{$schregno}' ";
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在チェック
    public function checkRegdDat($grade, $hrclass, $attendno)
    {
        $query  = "     SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "     FROM ";
        $query .= "       SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND GRADE = '{$grade}' ";
        $query .= "       AND HR_CLASS = '{$hrclass}' ";
        $query .= "       AND ATTENDNO = '{$attendno}' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
