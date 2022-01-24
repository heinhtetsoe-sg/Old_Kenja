<?php

require_once('for_php7.php');

class knjl361cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl361cindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjl361cQuery::selectYearQuery());
        $opt       = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR+1,"value" => CTRL_YEAR+1);
            unset($model->year);
        }else{
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->year == $row["ENTEXAMYEAR"]){
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if(!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR + 1;
            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];
            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjl361cQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjl361cindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //年度コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //テーブルの中身の作成
        $query  = knjl361cQuery::selectQuery($model->year);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
             array_walk($row, "htmlspecialchars_array");

             $row["TOTALCD"]  = $row["COURSECD"] . $row["MAJORCD"];

             $hash = array("cmd"         => "edit",
                           "year"        => $row["ENTEXAMYEAR"],
                           "ITEMCD"      => $row["ITEMCD"],
                           "ITEMNAME"    => $row["ITEMNAME"],
                           "MONEY_BOY"   => $row["MONEY_BOY"],
                           "MONEY_GIRL"  => $row["MONEY_GIRL"]);

             $row["ITEMCD"]         = View::alink("knjl361cindex.php", $row["ITEMCD"], "target=\"right_frame\"", $hash);
             $row["ITEMNAME"]       = $row["ITEMNAME"];
             $row["MONEY_BOY"]      = number_format($row["MONEY_BOY"]);
             $row["MONEY_GIRL"]     = number_format($row["MONEY_GIRL"]);
             $arg["data"][] = $row;
        }
        $result->free();

        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd", "");

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjl361cindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl361cForm1.html", $arg);
    }
}
?>
