<?php

require_once('for_php7.php');

class knje355Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje355index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $opt[] = array('label' => CTRL_YEAR."年度", 'value' => CTRL_YEAR);
        $opt[] = array('label' => (CTRL_YEAR + 1)."年度", 'value' => (CTRL_YEAR + 1));
        $model->leftYear = $model->leftYear ? $model->leftYear : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('copyEdit');\"";
        $arg["top"]["LEFT_YEAR"] = knjCreateCombo($objForm, "LEFT_YEAR", $model->leftYear, $opt, $extra, 1);

        //コピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        $query = knje355Query::getCopyCheckLmst($model->leftYear);
        $setDataCnt = $db->getOne($query);
        //hidden
        knjCreateHidden($objForm, "YEAR_CNT", $setDataCnt);

        $query = knje355Query::getCopyCheckLmst(($model->leftYear - 1));
        $setLastYearCnt = $db->getOne($query);
        //hidden
        knjCreateHidden($objForm, "LAST_YEAR_CNT", $setLastYearCnt);

        $query = knje355Query::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
             array_walk($row, "htmlspecialchars_array");
             $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje355Form1.html", $arg);
    }
}
?>
