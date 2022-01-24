<?php

require_once('for_php7.php');
class knjz065mForm1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz065mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjz065mQuery::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1);

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //一覧表示
        $key = "";
        $query = knjz065mQuery::getList($model, "", "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //列結合
            if ($key !== $row["CONDITION"].'-'.$row["GROUPCD"].'-'.$row["GHR_CD"].'-'.$row["GRADE"].'-'.$row["HR_CLASS"]) {
                $cnt = $db->getOne(knjz065mQuery::getList($model, $row["CONDITION"], $row["GROUPCD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;

                //状態区分
                $row["CONDITION_NAME"] = $db->getOne(knjz065mQuery::getCondition($row["CONDITION"]));

            }

            $query2 = knjz065mQuery::getUnitGroupYmst($model, $row["GHR_CD"], $row["GRADE"], $row["HR_CLASS"], $row["CONDITION"], $row["GROUPCD"], $row["SET_SUBCLASSCD"]);
            $result2 = $db->query($query2);
            $rowSpan2 = 0;
            $tr = "";
            $trEnd = "";
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($rowSpan2 > 0) {
                    $tr = "<tr bgcolor=\"#ffffff\">";
                    $trEnd = "</tr>";
                }

                $row["UNIT"] .= $tr."<td nowrap>".View::alink("knjz065mindex.php", $row2["UNITCD"]."：".$row2["UNITNAME"], "target=right_frame",
                                                   array(
                                                         "cmd"                => "edit2",
                                                         "YEAR"               => $row["YEAR"],
                                                         "GAKUBU_SCHOOL_KIND" => $row["GAKUBU_SCHOOL_KIND"],
                                                         "GHR_CD"             => $row["GHR_CD"],
                                                         "GRADE"              => $row["GRADE"],
                                                         "HR_CLASS"           => $row["HR_CLASS"],
                                                         "CONDITION"          => $row["CONDITION"],
                                                         "GROUPCD"            => $row["GROUPCD"],
                                                         "SET_SUBCLASSCD"     => $row["SET_SUBCLASSCD"],
                                                         "UNITCD"             => $row2["UNITCD"],
                                                        )
                                                  )."</td>".$trEnd;
                $rowSpan2++;
            }
            if ($rowSpan2 == 0) {
                $row["ROWSPAN2"] = 1;
                $row["UNIT"] = "<td></td>";
            } else {
                $row["ROWSPAN2"] = $rowSpan2;
            }

            $arg["data"][] = $row;

            $key = $row["CONDITION"].'-'.$row["GROUPCD"].'-'.$row["GHR_CD"].'-'.$row["GRADE"].'-'.$row["HR_CLASS"];
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            if ($model->cmd == "changeKind") {
                $this->condition = "";
                $this->groupcd = "";
                $this->set_subclasscd = "";
                $this->unitcd = "";
            }
            $arg["jscript"] = "window.open('knjz065mindex.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz065mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
