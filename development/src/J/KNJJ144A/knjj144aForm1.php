<?php

require_once('for_php7.php');

class knjj144aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjj144aindex.php", "", "main");
        $db = Query::dbCheckOut();

        //今年度データ取得
        if (!isset($model->warning)){
            $query = knjj144aQuery::getMarathonEventMst();
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row = $row;
            }
        }else{
            $Row =& $model->field;
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //実施回数
        $arg["data"]["NUMBER_OF_TIMES"] = knjCreateTextBox($objForm, $Row["NUMBER_OF_TIMES"], "NUMBER_OF_TIMES", 10, 5, "");

        //名称
        $arg["data"]["EVENT_NAME"] = knjCreateTextBox($objForm, $Row["EVENT_NAME"], "EVENT_NAME", 20, 10, "");

        //実施日
        $arg["data"]["EVENT_DATE"] = View::popUpCalendar($objForm, "EVENT_DATE", str_replace("-", "/", $Row["EVENT_DATE"]));

        //距離(男子)
        $arg["data"]["MAN_METERS"] = knjCreateTextBox($objForm, $Row["MAN_METERS"], "MAN_METERS", 6, 6, "");

        //距離(女性)
        $arg["data"]["WOMEN_METERS"] = knjCreateTextBox($objForm, $Row["WOMEN_METERS"], "WOMEN_METERS", 6, 6, "");

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj144aForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, &$model) {
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
