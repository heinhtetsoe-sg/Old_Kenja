<?php

require_once('for_php7.php');

class knjm704dForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm704dindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //コピー年度設定
        $opt = array();
        $value_flg = false;
        $query = knjm704dQuery::getCopyYear();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->copyYear == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $model->copyYear = ($model->copyYear && $value_flg) ? $model->copyYear : CTRL_YEAR;
        $extra = "";
        $arg["COPY_YEAR"] = knjCreateCombo($objForm, "COPY_YEAR", $model->copyYear, $opt, $extra, 1);

        //左の年度の特別活動内容のデータをコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "左の年度の特別活動内容のデータをコピー", $extra);

        //リスト表示
        $result = $db->query(knjm704dQuery::selectQuery());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["SPECIALCD"] = View::alink(
                "knjm704dindex.php",
                $row["SPECIALCD"],
                "target=\"right_frame\"",
                array("cmd"       => "edit",
                                                      "YEAR"      => $row["YEAR"],
                                                      "SPECIALCD" => $row["SPECIALCD"],
                                                      )
            );
            $row["SPECIAL_SDATE"] = str_replace("-", "/", $row["SPECIAL_SDATE"]);
            $row["SPECIAL_EDATE"] = str_replace("-", "/", $row["SPECIAL_EDATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm704dForm1.html", $arg);
    }
}
