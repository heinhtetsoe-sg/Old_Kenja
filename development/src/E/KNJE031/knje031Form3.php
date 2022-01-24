<?php

require_once('for_php7.php');

class knje031Form3 {

    function main(&$model) {

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["close"] = "closing_window();";
        }

        //オブジェクト作成
        $objForm = new form;

        //年度
        $arg["top_year"] = CTRL_YEAR;

        //学期
        $arg["top_semester"] = CTRL_SEMESTERNAME;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje031index.php", "", "edit");
        $db = Query::dbCheckOut();
        $query  = knje031Query::getList($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["URL"] = View::alink("knje031index.php", $row["NAME"], "target=right_frame",
                                        array("cmd"         => "from_list",
                                              "SCHREGNO"    => $row["SCHREGNO"]
                                              ));

            $row["GRADE"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $row["ENT_DATE"] = str_replace("-", "/", $row["ENT_DATE"]);
            $arg["data"][] = $row;
        }

        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje031Form3.html", $arg);
    }
}
?>
