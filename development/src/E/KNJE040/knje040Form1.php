<?php

require_once('for_php7.php');

class knje040Form1 {
    function main(&$model) {
        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["close"] = "closing_window();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje040index.php", "", "edit");
        $db = Query::dbCheckOut();
        $query  = knje040Query::getlist(CTRL_YEAR, CTRL_SEMESTER, $model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["URL"] = View::alink(REQUESTROOT ."/X/KNJXATTEND/index.php", $row["NAME"], "target=right_frame",
                                        array("cmd"         => "detail",
                                              "SCHREGNO"    => $row["SCHREGNO"],
                                              "MEMO"        => "knje040"
                                              ));

            $row["GRADE"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $arg["data"][] = $row;
        }

        $arg["top_year"] = CTRL_YEAR;
        $arg["top_semester"] = CTRL_SEMESTERNAME;

        $result->free();
        Query::dbCheckIn($db);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje040Form1.html", $arg);
    }
}
?>
