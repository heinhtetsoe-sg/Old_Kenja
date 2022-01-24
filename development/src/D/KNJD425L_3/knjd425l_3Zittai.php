<?php

require_once('for_php7.php');

class knjd425l_3Zittai
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("zittai", "POST", "knjd425l_3index.php", "", "zittai");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->exp_year;
        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //テーブルタイトル
        $query = knjd425l_3Query::getChallengedAssessmentStatusGrowupDat($model, "0");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row) {
            $arg["SHEET_PATTERN"] = $row["SHEET_PATTERN"];
            $arg["STATUS_NAME"] = $row["STATUS_NAME"];
            $arg["GROWUP_NAME"] = $row["GROWUP_NAME"];
            $arg["isGroupColumn"] = $row["SHEET_PATTERN"] == "2" ? "1" : "";
        }

        //実態＆支援のREMARK内容を取得し連想配列に保持
        $query = knjd425l_3Query::getRemarkZittaiAndSien($model);
        $result = $db->query($query);
        $remarkArray = array();
        while ($remarkRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $remarkArray[$remarkRow["SUBCLASSCD"]]["STATUS"]      = $remarkRow["STATUS"];
            $remarkArray[$remarkRow["SUBCLASSCD"]]["FUTURE_CARE"] = $remarkRow["FUTURE_CARE"];
        }

        //実態文字数
        $moji1 = 25;
        $gyou1 = 30;
        if (!$arg["isGroupColumn"]) {
            // 1枠表示時の 文字数と行数
            $moji1 = 40;
            $gyou1 = 30;
        }
        //支援文字数
        $moji2 = 15;
        $gyou2 = 30;

        //項目内容取得
        $query = knjd425l_3Query::getDetailSchregSubclassRemark($model, $model->exp_year, $model->selKindNo, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];

            $data["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
            $extra = "id=\"STATUS_".$model->subclasscd."\" aria-label=\"".$row["SUBCLASSNAME"]."\" readonly ";
            $data["STATUS"] = knjCreateTextArea($objForm, "STATUS_".$model->subclasscd, $gyou1, ($moji1 * 2), "", $extra, $remarkArray[$subclass]["STATUS"]);

            $extra = "id=\"FUTURE_CARE_".$model->subclasscd."\" aria-label=\"".$row["SUBCLASSNAME"]."\" readonly ";
            $data["FUTURE_CARE"] = knjCreateTextArea($objForm, "FUTURE_CARE_".$model->subclasscd, $gyou2, ($moji2 * 2), "", $extra, $remarkArray[$subclass]["FUTURE_CARE"]);

            $arg["list"][] = $data;
        }
        $result->free();

        //戻るボタン作成
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd425l_3Zittai.html", $arg);
    }
}

?>

