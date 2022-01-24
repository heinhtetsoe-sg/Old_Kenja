<?php

require_once('for_php7.php');

class knje020HyosyoForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("HyosyoForm1", "POST", "knje020index.php", "", "HyosyoForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //対象項目
        $itemArray = array("CONTENT");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //賞リスト
        $counter = 0;
        if ($model->schregno) {
            //常盤木は活動の記録を出力
            if ($model->schoolName == "tokiwagi") {
                $detail_div = "4";
                $namecd1    = "H317";
            } else {
                $detail_div = "1";
                $namecd1    = "H303";
            }
            $result = $db->query(knje020Query::getHyosyo($model, $detail_div, $namecd1));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["DETAIL_SDATE"] = str_replace("-", "/", $row["DETAIL_SDATE"]);

                //選択チェックボックス
                $value = $row["DETAIL_SDATE"];
                $extra = "onclick=\"OptionUse(this);\"";
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

                foreach ($itemArray as $key) {
                    knjCreateHidden($objForm, $key.":".$value, $row[$key."_SHOW"]);
                }

                $arg["data"][] = $row;
                $counter++;
            }
        }

        foreach ($itemArray as $key) {
            //対象項目チェックボックス
            $extra  = ($counter > 0) ? "" : "disabled";
            $extra .= " checked id=\"CHECK_{$key}\" onclick=\"return OptionUse(this);\"";
            $arg["CHECK_".$key] = knjCreateCheckBox($objForm, "CHECK_".$key, $key, $extra, "");
        }

        //取込ボタン
        $target = VARS::get("target");
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$target}');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje020HyosyoForm1.html", $arg);
    }
}
