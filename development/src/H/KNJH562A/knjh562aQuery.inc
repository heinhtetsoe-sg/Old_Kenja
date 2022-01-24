<?php

require_once('for_php7.php');

class knjh562aQuery extends Query
{
    //テスト種別取得
    public function getDataDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    public function getProName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1";
        $query .= " LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV = T2.PROFICIENCYDIV";
        $query .= "                             AND T1.PROFICIENCYCD  = T2.PROFICIENCYCD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER       = '{$model->semester}' AND ";
        $query .= "     T1.PROFICIENCYDIV = '{$model->proficiencyDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //PROFICIENCY_TICK_WIDTH_MSTマスタ共通選択条件を取得
    private function getWhereConditionOfPrfcyTckWdthMst($model, $alias = "")
    {
        $query  = "     {$alias}.YEAR                    = '{$model->year}' ";
        $query .= " AND {$alias}.PROFICIENCYDIV          = '{$model->proficiencyDiv}' ";
        $query .= " AND {$alias}.PROFICIENCYCD           = '{$model->proficiencyCd}' ";
        $query .= " AND {$alias}.PROFICIENCY_SUBCLASS_CD = '000000' ";//固定。科目に依存しない
        $query .= " AND {$alias}.DIV                     = '1' ";     //固定。１：学年
        $query .= " AND {$alias}.GRADE                   = '00' ";    //固定
        $query .= " AND {$alias}.HR_CLASS                = '000' ";   //固定
        $query .= " AND {$alias}.COURSECD                = '0' ";     //固定
        $query .= " AND {$alias}.MAJORCD                 = '000' ";   //固定
        $query .= " AND {$alias}.COURSECODE              = '0000' ";  //固定

        return $query;
    }

    //登録済み点数分布刻み情報を取得
    public function getScoreList($model)
    {
        $query  = " SELECT ";
        $query .= "     MST.TICK_STAGE , ";
        $query .= "     MST.TICK_LOW , ";
        $query .= "     MST.TICK_HIGH ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_TICK_WIDTH_MST MST ";
        $query .= " WHERE ";
        $query .= knjh562aQuery::getWhereConditionOfPrfcyTckWdthMst($model, "MST");
        $query .= " ORDER BY ";
        $query .= "     MST.TICK_LOW DESC ";

        return $query;
    }

    //点数分布刻み情報を追加または変更
    public function modifyProcifiensyTickWidth($model, $scoreList)
    {
        //DB接続を開始
        $db = Query::dbCheckOut();

        //自動コミットを無効（コミット実行まで１つのトランザクションにまとめる）
        $db->autoCommit(false);

        //既存を一旦削除（INSERT時の重複エラーを回避）
        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_TICK_WIDTH_MST MST ";
        $query .= " WHERE ";
        $query .= knjh562aQuery::getWhereConditionOfPrfcyTckWdthMst($model, "MST");
        $db->query($query);

        //新規登録または更新（＝再追加）
        foreach ($scoreList as $tick_stage => $limitDef) {
            $data = array();

            //共通項
            $data["YEAR"][TEXT]                    = $model->year;
            $data["PROFICIENCYDIV"][TEXT]          = $model->proficiencyDiv;
            $data["PROFICIENCYCD"][TEXT]           = $model->proficiencyCd;
            $data["PROFICIENCY_SUBCLASS_CD"][TEXT] = "000000";//固定。科目に依存しない
            $data["DIV"][TEXT]                     = "1";     //固定。１：学年
            $data["GRADE"][TEXT]                   = "00";
            $data["HR_CLASS"][TEXT]                = "000";
            $data["COURSECD"][TEXT]                = "0";
            $data["MAJORCD"][TEXT]                 = "000";
            $data["COURSECODE"][TEXT]              = "0000";

            //スコア項
            $data["TICK_STAGE"][NUMBER]            = $tick_stage;
            $data["TICK_LOW"][NUMBER]              = $limitDef["tick_low"];
            $data["TICK_HIGH"][NUMBER]             = $limitDef["tick_high"];

            //共通フッタ
            $data["REGISTERCD"][TEXT]              = STAFFCD;
            $data["UPDATED"][FUNC]                 = "sysdate()";

            //追加実行
            $query = Query::insertSQL($data, "PROFICIENCY_TICK_WIDTH_MST");
            $db->query($query);
        }

        //変更同意
        $db->commit();

        //DB接続を終了
        Query::dbCheckIn($db);
    }
}
