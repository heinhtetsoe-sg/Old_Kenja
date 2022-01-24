<?php

require_once('for_php7.php');

class knjl052qQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称取得（評価）高校入試用
    public function getInterviewName($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L027' ";
        $query .= "     AND NAME1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称取得（評価）中学入試用
    public function getInterviewNameJ($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L027' ";
        $query .= "     AND NAME2 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         S1.NAME, ";
        $query .= "         FIN_M.FINSCHOOL_NAME, ";
        $query .= "         L1.INTERVIEW_VALUE ";
        if (SCHOOLKIND == "J") {
            $query .= "         ,L1.INTERVIEW_VALUE2 ";
        }
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAMNO           = T1.EXAMNO ";
        $query .= "         LEFT JOIN FINSCHOOL_MST FIN_M ";
        $query .= "              ON S1.FS_CD            = FIN_M.FINSCHOOLCD ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' AND ";
        $query .= "         VALUE(T1.JUDGEDIV,'') <> '4' ";
        if (SCHOOLKIND != "J") {
            $query .= "     AND VALUE(T1.JUDGEDIV, '') != '4' ";   //欠席者を除く
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //評価未入力の生徒取得
    public function selectNullQuery($model)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         L1.INTERVIEW_VALUE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAMNO           = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' AND ";
        $query .= "         VALUE(T1.JUDGEDIV,'') <> '4' ";
        $query .= "     AND VALUE(T1.JUDGEDIV, '') != '4' ";   //欠席者を除く
        $query .= "     AND L1.INTERVIEW_VALUE IS NULL ";   //評価なし
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //更新 ENTEXAM_INTERVIEW_DAT
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (SCHOOLKIND == "J") {
            //削除
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
            $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
            $query .= "   AND EXAMNO IN ('".implode("','", $model->examno)."')";
            $db->query($query);

            //追加
            foreach ($model->interview_value as $receptno => $value) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $model->examno[$receptno];
                $data["INTERVIEW_VALUE"][TEXT]  = $value;
                $data["INTERVIEW_VALUE2"][TEXT] = $model->interview_value2[$receptno];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
                $db->query($query);
            }
        } else {
            //条件
            $where  = " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV        = '".$model->testdiv."' ";
            $where .= "   AND EXAMNO         = '".$model->hid_up_examno."' ";

            //データチェック
            $getCount = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_INTERVIEW_DAT " .$where);

            $data = array();
            $data["INTERVIEW_VALUE"][TEXT] = $model->hid_up_interview_value;
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            if ($getCount > 0) {
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            } else {
                //追加
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $model->hid_up_examno;
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //初期値2を設定する
    public function getINSERTQuery(&$model, $receptno, $count)
    {
        $data = array();
        $data["INTERVIEW_VALUE"][TEXT]      = '2'; //初期値
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        if ($count > 0) {
            //更新
            $where .= " WHERE ";
            $where .= "   ENTEXAMYEAR       = '{$model->ObjYear}'";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}'";
            $where .= "   AND TESTDIV       = '{$model->testdiv}'";
            $where .= "   AND EXAMNO        = '{$receptno}'";
            $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
        } else {
            //追加
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAMNO"][TEXT]           = $receptno;
            $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
        }
        return $query;
    }

    //面接評価の存在確認
    public function getCount($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*)";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR    = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     EXAMNO = '".$receptno."' ";

        return $query;
    }
}
?>