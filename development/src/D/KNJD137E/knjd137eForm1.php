<?php

require_once('for_php7.php');

class knjd137eForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd137eindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //年次取得
        $gradeCd = $db->getOne(knjd137eQuery::getGradeCd($model));

        //観点マスタ
        $query = knjd137eQuery::getBehaviorSemesMst($model, $gradeCd);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["RECORD_NAME_".$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knjd137eQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row["RECORD"][$row["CODE"]] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knjd137eQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        for ($i = 1; $i < 11; $i++) {
            $ival = sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        $extra = "onkeyup=\"charCount(this.value, $model->specialRemark_gyou, ($model->specialRemark_moji * 2), true);\"";
        $arg["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", $model->specialRemark_gyou, ($model->specialRemark_moji * 2), "soft", $extra, $row["SPECIALACTREMARK"]);
        $arg["SPECIALACTREMARK_MOJI"] = $model->specialRemark_moji;
        $arg["SPECIALACTREMARK_GYOU"] = $model->specialRemark_gyou;

        //学校からの所見
        $extra = "onkeyup=\"charCount(this.value, $model->communication_gyou, ($model->communication_moji * 2), true);\"";
        $arg["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", $model->communication_gyou, ($model->communication_moji * 2), "soft", $extra, $row["COMMUNICATION"]);
        $arg["COMMUNICATION_MOJI"] = $model->communication_moji;
        $arg["COMMUNICATION_GYOU"] = $model->communication_gyou;

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd137eForm1.html", $arg);
    }
}
?>