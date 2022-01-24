<?php

require_once('for_php7.php');

class knjl035fForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl035findex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $result = $db->query(knjl035fQuery::selectYearQuery());
        $opt = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);
            unset($model->year);
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->year == $row["ENTEXAMYEAR"]) {
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if (!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR + 1;
            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];
            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjl035fQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjl035findex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //年度コンボボックス
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //件数取得
        $a_cnt = array();
        $result = $db->query(knjl035fQuery::getDataCnt($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $a_cnt[$row["APPLICANTDIV"]] = $row["CNT"];
        }
        $result->free();

        //件数取得
        $e_cnt = array();
        $result = $db->query(knjl035fQuery::getDataCnt($model->year, "EXEMPTION_CD"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $e_cnt[$row["APPLICANTDIV"]][$row["EXEMPTION_CD"]] = $row["CNT"];
        }
        $result->free();

        //テーブルの中身の作成
        $a_bifKey = $e_bifKey = "";
        $query = knjl035fQuery::selectQuery($model->year);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $hash = array("cmd"             => "edit2",
                          "year"            => $row["ENTEXAMYEAR"],
                          "APPLICANTDIV"    => $row["APPLICANTDIV"],
                          "EXEMPTION_CD"    => $row["EXEMPTION_CD"],
                          "ITEM_CD"         => $row["ITEM_CD"]);

            $row["APPLICANTNAME"]   = $row["APPLICANTDIV"] .":". $row["APPLI_NAME"] ;
            $row["EXEMPTION"]       = $row["EXEMPTION_CD"] .":". $row["EXEMPTION_NAME"];
            $row["ITEM_CD"]         = View::alink("knjl035findex.php", $row["ITEM_CD"], "target=\"right_frame\"", $hash);
            $row["ITEM"]            = $row["ITEM_CD"] .":". $row["ITEM_NAME"];
            $row["ITEM_MONEY"]      = strlen($row["ITEM_MONEY"]) ? number_format($row["ITEM_MONEY"]) : "";

            //重複した入試制度はまとめる
            if ($a_bifKey !== $row["APPLICANTDIV"]) {
                $cnt = $a_cnt[$row["APPLICANTDIV"]];
                $row["ROWSPAN1"] = $cnt > 0 ? $cnt : 1;
            }
            //重複した免除コードはまとめる
            if ($a_bifKey !== $row["APPLICANTDIV"] || $e_bifKey !== $row["EXEMPTION_CD"]) {
                $cnt = $e_cnt[$row["APPLICANTDIV"]][$row["EXEMPTION_CD"]];
                $row["ROWSPAN2"] = $cnt > 0 ? $cnt : 1;
            }
            $a_bifKey = $row["APPLICANTDIV"];
            $e_bifKey = $row["EXEMPTION_CD"];

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjl035findex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl035fForm1.html", $arg);
    }
}
?>
