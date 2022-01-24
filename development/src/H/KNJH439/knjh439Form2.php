<?php

require_once('for_php7.php');

class knjh439Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh439index.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjh439Query::getRow($model->mockyear, $model->mockcd, $model->grade);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->mockyear;

        //模試データ
        $query = knjh439Query::getMockcd($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["MOCKCD"], "MOCKCD", $extra, "BLANK");

        //学年
        //$query = knjh439Query::getGrade($model);
        //$extra = "";
        //makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, "BLANK");
        knjCreateHidden($objForm, "GRADE",$Row["GRADE"]);

        //科目数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["K_FIELD_CNT"] = knjCreateTextBox($objForm, $Row["K_FIELD_CNT"], "K_FIELD_CNT", 3, 3, $extra);

        //志望数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["H_FIELD_CNT"] = knjCreateTextBox($objForm, $Row["H_FIELD_CNT"], "H_FIELD_CNT", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "edit" && $model->cmd != "reset") {
            $arg["reload"]  = "parent.left_frame.location.href='knjh439index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh439Form2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $opt = array();
    $result = $db->query($query);
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
