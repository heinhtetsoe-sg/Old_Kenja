<?php

require_once('for_php7.php');

class knjd625iForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd625iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //リスト作成
        makeList($arg, $db);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //前年度データ件数
        $preYear_cnt = $db->getOne(knjd625iQuery::getCopyDataOld($model->pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        
        //今年度データ件数

        $thisYear_cnt = $db->getOne(knjd625iQuery::getCopyData(CTRL_YEAR));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd625iForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
}

//リスト作成
function makeList(&$arg, $db)
{
    $result = $db->query(knjd625iQuery::getList());

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         array_walk($row, "htmlspecialchars_array");
         $arg["data"][] = $row;
    }

    $result->free();
}

?>
