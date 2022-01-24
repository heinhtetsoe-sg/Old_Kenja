<?php

require_once('for_php7.php');

class knjj520Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj520index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = htmlspecialchars($model->name);

        //測定日付
        $date = $db->getOne(knjj520Query::getScoreDate($model, "date"));
        $cnt = get_count($db->getCol(knjj520Query::getScoreDate($model, "data")));

        if(!$cnt) {
            $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        } else {
            $model->field["DATE"] = ($date) ? str_replace("-", "/", $date) : "";
        }
        $arg["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //EXTRA
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";

        //初期化
        $model->data = array();

        //データ一覧
        $query = knjj520Query::getSportsScoreDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $model->data["ITEMCD"][] = $row["ITEMCD"];

            $row["RECORD"] = knjCreateTextBox($objForm, $row["RECORD"], "RECORD-".$row["ITEMCD"], 7, 7, $extra);

            $arg["data"][] = $row;
        }
        $result->free();

        //総合判定
        $value = $db->getOne(knjj520Query::getSportsScoreDat2($model));
        $model->field["VALUE"] = ($model->field["VALUE"]) ? $model->field["VALUE"] : $value;
        $extra = "style=\"text-align: center\" onblur=\"ValueCheck(this);\"";
        $arg["VALUE"] = knjCreateTextBox($objForm, $model->field["VALUE"], "VALUE", 3, 3, $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj520Form1.html", $arg);
    }
}
?>
