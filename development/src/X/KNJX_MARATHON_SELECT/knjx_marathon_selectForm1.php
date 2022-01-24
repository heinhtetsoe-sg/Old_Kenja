<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_marathon_selectForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_marathon_selectindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

		//学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //対象項目
        $itemArray = array("NUMBER_OF_TIMES", "EVENT_NAME", "RANK", "TIME", "METERS", "EVENT_DATE", "ATTEND");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //対象年度取得
        $year = array();
        if ($model->send_prgid == "KNJE020") {
            $query = knjx_marathon_selectQuery::getRegdYear($model);
            $year = $db->getCol($query);
        } else {
            $year[] = $model->exp_year;
        }

        //学籍基礎(性別)取得
        $schreg = array();
        $query = knjx_marathon_selectQuery::getSchregBase($model);
        $schregInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //マラソン大会リスト(生徒情報取得)
        $schregMarathonList = array();
        $query = knjx_marathon_selectQuery::getMarathonEventDat($model, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregMarathonList[$row["YEAR"]][$row["SEQ"]] = $row;
        }
        //マラソン大会マスタ取得
        $query = knjx_marathon_selectQuery::getMarathonEventMst($model, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $schregRow = array();
            if ($schregMarathonList[$row["YEAR"]][$row["SEQ"]]) {
                $schregRow = $schregMarathonList[$row["YEAR"]][$row["SEQ"]];
            }
            //順位(学年-性別順位)
            $row["RANK"] = $schregRow["GRADE_RANK_SEX"];
            if ($row["RANK"]) {
                $row["RANK"] .= "位";
            }
            //タイム
            $row["TIME"] = "";
            if ($schregRow["TIME_H"]) {
                $row["TIME"] .= $schregRow["TIME_H"]."時間";
            }
            if ($schregRow["TIME_M"]) {
                $row["TIME"] .= $schregRow["TIME_M"]."分";
            }
            if ($schregRow["TIME_S"]) {
                $row["TIME"] .= $schregRow["TIME_S"]."秒";
            }
            //欠席
            $row["ATTEND"] = $schregRow["ATTEND_NAME"];

            //性別未入力の時は男性を優先
            $row["METERS"] = $row["MAN_METERS"];
            //学籍基礎の性別で距離を設定 ※マラソン大会のデータが無い場合
            if ($schregInfo["SEX"] == "2") {
                $row["METERS"] = $row["WOMEN_METERS"];
            }
            //マラソン大会での距離を再設定
            if ($schregRow["SEX"] == "1") {
                $row["METERS"] = $row["MAN_METERS"];
            } else if ($schregRow["SEX"] == "2") {
                $row["METERS"] = $row["WOMEN_METERS"];
            }
            if ($row["METERS"]) {
                $row["METERS"] .= "Km";
            }
            //実施日
            $row["EVENT_DATE"] = str_replace("-","/",$row["EVENT_DATE"]);

            //選択チェックボックス
            $value = $row["SEQ"];
            $extra = "onclick=\"OptionUse(this);\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["YEAR"]."_".$value, $extra, "1");

            foreach ($itemArray as $key) {
                knjCreateHidden($objForm, $key.":".$row["YEAR"]."_".$value, $row[$key]);
            }

            $arg["data"][] = $row;
            $counter++;
        }

        $checkedList = array("EVENT_NAME", "RANK", "TIME");
        foreach ($itemArray as $key) {
            //対象項目チェックボックス
            $extra  = ($counter > 0) ? "" : "disabled";
            if (in_array($key, $checkedList)) {
                $extra .= " checked ";
            }
            $extra .= " id=\"CHECK_{$key}\" onclick=\"return OptionUse(this);\"";
            $arg["CHECK_".$key] = knjCreateCheckBox($objForm, "CHECK_".$key, $key, $extra, "");
        }

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_marathon_selectForm1.html", $arg);
    }
}
?>
