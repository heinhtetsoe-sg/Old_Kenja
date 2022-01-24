<?php
class knjl610hForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl610hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["ENTEXAMYEAR"] = $model->year;

        //表示順序ラジオボタン 1:氏名（50音順） 2:登録順
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('chagneSort');\"", "id=\"SORT2\" onclick=\"btn_submit('chagneSort');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        $query = knjl610hQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //並び順を保持
        $arg["HID_SORT"] = $model->sort;

        if ($model->cmd == "chagneSort") {
            $arg["reload"]  = "parent.right_frame.location.href='knjl610hindex.php?cmd=edit&HID_SORT=".$model->sort."';";
        }

        //hidden
        $objForm->ae(array("type"    => "hidden",
                            "name"    => "cmd"
                            ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl610hForm1.html", $arg);
    }
}
