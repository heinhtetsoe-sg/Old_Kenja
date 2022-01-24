<?php

require_once('for_php7.php');

class knjl328aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタL056
    public function getNameMstL056($year, $applicantdiv)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2,";
        $query .= "     NAME1,"; // 名称
        $query .= "     NAMESPARE1,"; // 金額文言
        $query .= "     NAMESPARE2 "; // 金額
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        if ($applicantdiv == "1") {
            $query .= "     AND NAMECD1 = 'LJ56' ";
        } elseif ($applicantdiv == "2") {
            $query .= "     AND NAMECD1 = 'LH56' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //取扱票種類コンボ
    public function getPatternCombo($model)
    {
        $query   = " SELECT DISTINCT ";
        $query  .= "   PATTERN_CD AS VALUE,";
        $query  .= "   PATTERN_CD || ':' || PATTERN_NAME AS LABEL ";
        $query  .= " FROM ";
        $query  .= "   ENTEXAM_TRANSFER_FORMS_MST ";
        $query  .= " WHERE ";
        $query  .= "   ENTEXAMYEAR = '".$model->examyear."' ";
        $query  .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";

        return $query;
    }

    //試験回コンボ
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        if ($testdiv) {
            $query .= "     TESTDIV_NAME ";
        } else {
            $query .= "     TESTDIV AS VALUE, ";
            $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
            $query .= "     TESTDIV, ";
            $query .= "     TESTDIV_NAME, ";
            $query .= "     PRETEST_FLG "; //プレテストフラグ
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND PRETEST_FLG  IS NULL ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入学コース取得
    public function getEnterCourse($model)
    {
        $query   = " SELECT ";
        $query .= "     L012.NAMECD2 || ':' || L012.NAME1 AS LABEL, ";
        $query .= "     L012.NAMECD2 AS VALUE ";
//        $query .= "     ,L012.NAMESPARE2 ";
        $query  .= " FROM ";
        $query  .= "   V_NAME_MST L012 ";
        $query  .= "   INNER JOIN V_NAME_MST L003 ";
        $query  .= "     ON L003.YEAR = L012.YEAR ";
        $query  .= "     AND L003.NAMECD1 = 'L003' ";
        $query  .= "     AND L003.NAMECD2 = '".$model->field["APPLICANTDIV"]."' ";
        $query  .= " WHERE ";
        $query  .= "   L012.YEAR = '".$model->examyear."' ";
        $query  .= "   AND L012.NAMECD1 = 'L' || L003.NAMESPARE3 || '12' ";

        return $query;
    }

    //志望コース
    public function getCourseCmb($model, $applicantdiv)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->examyear}' ";
        if ($applicantdiv == "1") {
            $query .= "     AND NAMECD1 = 'LJ58' ";
        } elseif ($applicantdiv == "2") {
            $query .= "     AND NAMECD1 = 'LH58' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //１レコード取得
    public function getRow($model)
    {
        $query   = " SELECT ";
        $query  .= "   * ";
        $query  .= " FROM ";
        $query  .= "   ENTEXAM_TRANSFER_FORMS_MST ";
        $query  .= " WHERE ";
        $query  .= "       ENTEXAMYEAR  = '{$model->examyear}' ";
        $query  .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query  .= "   AND PATTERN_CD   = '{$model->field["PATTERN_CD"]}' ";
        if ($model->field["PATTERN_CD"] == "001") {
            $query  .= "   AND TOKUTAI_SELECT = '{$model->field["TOKUTAI_SELECT"]}' ";
        } else {
            $query  .= "   AND TOKUTAI_SELECT = '2' ";
        }

        return $query;
    }

    //更新処理
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query   = " DELETE FROM ";
        $query  .= "   ENTEXAM_TRANSFER_FORMS_WK_MST ";
        $query  .= " WHERE ";
        $query  .= "       ENTEXAMYEAR  = '{$model->examyear}' ";
        $query  .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query  .= "   AND PATTERN_CD   = '{$model->field["PATTERN_CD"]}' ";
        if ($model->field["PATTERN_CD"] == "001") {
            $query  .= "   AND TOKUTAI_SELECT = '{$model->field["TOKUTAI_SELECT"]}' ";
        } else {
            $query  .= "   AND TOKUTAI_SELECT = '2' ";
        }
        $db->query($query);

        //追加
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["PATTERN_CD"][TEXT]       = $model->field["PATTERN_CD"];
        //$data["PATTERN_NAME"][TEXT]    = $model->field["PATTERN_NAME"];
        if ($model->field["PATTERN_CD"] == "001") {
            $data["TOKUTAI_SELECT"][TEXT]   = $model->field["TOKUTAI_SELECT"];
        } else {
            $data["TOKUTAI_SELECT"][TEXT]   = "2";
        }
        $data["PREF_NAME"][TEXT]        = $model->field["PREF_NAME"];
        $data["ACCOUNT_NUMBER1"][TEXT]  = $model->field["ACCOUNT_NUMBER1"];
        $data["ACCOUNT_NUMBER2"][TEXT]  = $model->field["ACCOUNT_NUMBER2"];
        $data["ACCOUNT_NUMBER3"][TEXT]  = $model->field["ACCOUNT_NUMBER3"];
        $data["MEMBER"][TEXT]           = $model->field["MEMBER"];
        $data["SUBJECT"][TEXT]          = $model->field["SUBJECT"];
        $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
        $data["TRANSFER_MONEY"][TEXT]   = $model->field["TRANSFER_MONEY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $db->query(Query::insertSQL($data, "ENTEXAM_TRANSFER_FORMS_WK_MST"));

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
