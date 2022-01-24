<?php

require_once('for_php7.php');


class knjb3056SubForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb3056SubForm2", "POST", "knjb3056index.php", "", "knjb3056SubForm2");

        
        //DB接続
        $db = Query::dbCheckOut();
        
        $query = knjb3056Query::maxDate($db, $model);
        $maxDate = str_replace('-','/',$db->getOne($query));
        $arg["data"]["MAX_DATE"] = $maxDate;
        
        $query=knjb3056Query::getSemesterRange($model);
        $row = $db->getRow(knjb3056Query::getSemesterRange($model));
        $arg["data"]["SDATE"] = str_replace('-','/',$row[0]);
        $arg["data"]["EDATE"] = str_replace('-','/',$row[1]);

        //変更開始日付
        $selectDate = str_replace('-','/',$row[0]);
        if ($maxDate) {
            $selectDate = date('Y/m/d',strtotime($maxDate)+60*60*24); // 出欠済日付の翌日
        }
        $arg["data"]["SELECT_DATE"] = View::popUpCalendar($objForm, "SELECT_DATE", $selectDate,"");

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        
        knjCreateHidden($objForm, "cmd");
        
        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3056SubForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //終了
    $extra = "onclick=\"return parent.closeit();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
