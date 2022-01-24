<?php

require_once('for_php7.php');

class knjz221Form2{

    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz221index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = ($model->year) ? $model->year."年度" : "";

        //学期表示
        $arg["SEMESTER"] = $db->getOne(knjz221Query::getSemester($model, "show"));

        //単位表示
        $arg["CREDIT"] = ($model->credit > 0) ? $model->credit."単位" : "";

        //データ取得
        if (!isset($model->warning) && $model->semester && $model->credit) {
            $result = $db->query(knjz221Query::getAttendScoreCreditMst($model, "right"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["KEKKA_LOW".$row["ATTEND_SCORE"]]  = $row["KEKKA_LOW"];
                $Row["KEKKA_HIGH".$row["ATTEND_SCORE"]] = $row["KEKKA_HIGH"];
            }
            $result->free();
        } else {
            $Row =& $model->field;
        }

        for ($score=10; $score >= 0; $score--) {

            $setTmp["ATTEND_SCORE"] = $score."点";

            //下限値
            $extra = " style=\"text-align: center;\" onblur=\"this.value=toInteger(this.value);\"";
            $setTmp["KEKKA_LOW"] = knjCreateTextBox($objForm, $Row["KEKKA_LOW".$score], "KEKKA_LOW".$score, 3, 3, $extra);

            //上限値
            $extra = " style=\"text-align: center;\" onblur=\"this.value=toInteger(this.value);\"";
            $setTmp["KEKKA_HIGH"] = knjCreateTextBox($objForm, $Row["KEKKA_HIGH".$score], "KEKKA_HIGH".$score, 3, 3, $extra);

            $arg["data"][] = $setTmp;
        }

        //更新ボタン
        if ($model->semester && $model->credit) {
            $extra = " onclick=\"return btn_submit('update');\"";
        } else {
            $extra = " disabled";
        }
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "CREDIT", $model->credit);

        //DB切断
        Query::dbCheckIn($db);

        //更新できたら左のリストを再読込
        if (isset($model->message)) {
            $arg["reload"] = "window.open('knjz221index.php?cmd=list&init=1&YEAR=".$model->year."', 'left_frame');";
        }

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz221Form2.html", $arg); 
    }
}
?>
