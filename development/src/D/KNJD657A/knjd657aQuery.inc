<?php

require_once('for_php7.php');
class knjd657aquery extends Query
{
    //学期コンボ
    public function getSemesterList()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    public function getGradeHrclass($semester)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試グループ
    public function getGroupcd($model)
    {
        $query  = " SELECT ";
        $query .= "     GROUPCD AS VALUE, ";
        $query .= "     GROUPCD || ' ' || GROUPNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     GROUP_DIV = '{$model->group_div}' AND ";
        $query .= "     STF_AUTH_CD = '{$model->stf_auth_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            /************/
            /* まず削除 */
            /************/
            $query  = " DELETE FROM ";
            $query .= "     MOCK_DOCUMENT_SDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR             = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     GROUP_DIV        = '{$model->group_div}' AND ";
            $query .= "     STF_AUTH_CD      = '{$model->stf_auth_cd}' AND ";
            $query .= "     GROUPCD          = '{$data_arr[$i]["GROUPCD"]}' AND ";
            $query .= "     SCHREGNO         = '{$data_arr[$i]["SCHREGNO"]}' AND ";
            $query .= "     MOCK_SUBCLASS_CD = '{$data_arr[$i]["MOCK_SUBCLASS_CD"]}' ";

            $db->query($query);

            /**************/
            /* インサート */
            /**************/
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["GROUP_DIV"][TEXT]        = $model->group_div;
            $data["STF_AUTH_CD"][TEXT]      = $model->stf_auth_cd;
            $data["GROUPCD"][TEXT]          = $data_arr[$i]["GROUPCD"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT] = $data_arr[$i]["MOCK_SUBCLASS_CD"];
            $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["REMARK3"];

            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_DOCUMENT_SDAT");
            $db->query($query);

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
            //データセット
            $data = array();
            $query  = " DELETE FROM ";
            $query .= "     MOCK_DOCUMENT_SDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR             = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     GROUP_DIV        = '{$model->group_div}' AND ";
            $query .= "     STF_AUTH_CD      = '{$model->stf_auth_cd}' AND ";
            $query .= "     GROUPCD          = '{$data_arr[$i]["GROUPCD"]}' AND ";
            $query .= "     SCHREGNO         = '{$data_arr[$i]["SCHREGNO"]}' AND ";
            $query .= "     MOCK_SUBCLASS_CD = '{$data_arr[$i]["MOCK_SUBCLASS_CD"]}' ";

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
        $year = CTRL_YEAR;

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     MOCK_DOCUMENT_SDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR          = '{$year}' AND ";
        $query .= "     T1.GROUP_DIV     = '{$model->group_div}' AND ";
        $query .= "     T1.STF_AUTH_CD   = '{$model->stf_auth_cd}' AND ";
        $query .= "     L1.SEMESTER      = '{$model->field["SEMESTER"]}' AND ";
        if ($model->field["GROUPCD"] != "999999") {
            $query .= "     T1.GROUPCD       = '{$model->field["GROUPCD"]}' AND ";
        }
        if ($model->field["GRADE_HR_CLASS"] != "999999") {
            $query .= "     L1.GRADE || L1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' AND ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "    (L2.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "     L2.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "     L2.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "     L2.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "     L2.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "     L2.SUBTR_CD1  = '".STAFFCD."') AND ";
        }
        $query .= "     T1.YEAR           = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }

    public function checkMstData($model, $cd, $type)
    {
        if ($type == 'GROUPCD') {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     MOCK_GROUP_MST ";
            $query .= " WHERE ";
            $query .= "     GROUP_DIV   = '{$model->group_div}' AND ";
            $query .= "     STF_AUTH_CD = '{$model->stf_auth_cd}' AND ";
            $query .= "     GROUPCD     = '{$cd}' ";
        } else {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     MOCK_SUBCLASS_MST ";
            $query .= " WHERE ";
            $query .= "     MOCK_SUBCLASS_CD = '{$cd}' ";
        }
        return $query;
    }
}
