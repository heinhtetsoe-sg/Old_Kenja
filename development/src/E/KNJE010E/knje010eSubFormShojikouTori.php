<?php

require_once("for_php7.php");

//ビュー作成用クラス
class knje010eSubFormShojikouTori
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("formShojikouTori", "POST", "knje010eindex.php", "", "formShojikouTori");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  ".$model->name;

        //部活動リスト
        $counter = 0;
        $item = array(
                 "TRAIN_REF1" => array("TRAIN_SEQ" => "001", "TITLE" => "1)学習における特徴等"),
                 "TRAIN_REF2" => array("TRAIN_SEQ" => "002", "TITLE" => "2)行動の特徴，特技等"),
                 "TRAIN_REF3" => array("TRAIN_SEQ" => "003", "TITLE" => "3)部活動，ボランティア<br>活動，留学・海外経験等"),
                 "TRAIN_REF4" => array("TRAIN_SEQ" => "004", "TITLE" => "4)取得資格，検定等"),
                 "TRAIN_REF5" => array("TRAIN_SEQ" => "005", "TITLE" => "5)表彰・顕彰等の記録"),
                 "TRAIN_REF6" => array("TRAIN_SEQ" => "006", "TITLE" => "6)その他")
        );

        $arg["TITLE"] = "指導上参考となる諸事項　".$model->targetYear."年度";

        $moji = 15;
        $gyou = 66;

        //テキストボックス
        $n = 0;
        foreach ($item as $target => $row) {
            $n += 1;
            $arg["TITLE".$n] = $item[$target]["TITLE"];
            $name = "REMARK".$n;
            $query = knje010eQuery::getHexamEntremarkTrainrefDat($model, $model->targetYear, $item[$target]["TRAIN_SEQ"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $remark = "";
            if (is_array($row)) {
                $remark = $row["REMARK"];
            }

            $extra = " id=\"".$name."\" style=\"background-color:lightgray; overflow-y: hidden; \" ";
            $arg[$name] = KnjCreateTextArea($objForm, $name, ($gyou + 1), ($moji * 2 + 1), 'soft', $extra, $remark);

            //取込ボタン
            $extra = "style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('".$n."', '{$model->targetYear}');\"";
            $arg["btn_torikomi".$n] = knjCreateBtn($objForm, "btn_torikomi".$n, "取込", $extra);
        }

        //一括取込ボタン
        $extra = "style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet(null, '{$model->targetYear}');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "一括取込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010eSubFormShojikouTori.html", $arg);
    }
}
