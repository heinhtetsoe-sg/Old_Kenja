<?php

require_once('for_php7.php');

class knjl083oKakutei
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl083oindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //combobox
        $opt = array();
        $opt[] = array('label' => "",
                       'value' => "");
        $query = knjl083oQuery::GetName2("L017", $model->ObjYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $query = knjl083oQuery::getEntClassStd($model);
        $result = $db->query($query);
        $model->kakuteiExam = array();
        $heightCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->kakuteiExam[$row["EXAMNO"]] = "";
            $extra = "";
            $row["ENTCLASS"] = knjCreateCombo($objForm, "ENTCLASS".$row["EXAMNO"], $row["ENTCLASS"], $opt, $extra, 1);
            $arg["data"][] = $row;
            $heightCnt++;
        }
        $result->free();
        $arg["height"] = $heightCnt > 12 ? "height=315;" : "";

        //更新ボタン
        $extra = "onclick=\"btn_submit('updateClass');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $extra = " onclick=\"return btn_submit('retParent');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl083oKakutei.html", $arg); 
    }
}
?>
