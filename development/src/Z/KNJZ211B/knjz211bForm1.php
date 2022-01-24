<?php

require_once('for_php7.php');

class knjz211bForm1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz211bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期取得
        $result = $db->query(knjz211bQuery::getNameMst("Z009"));
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学期を配列で取得
            $model->gakki["SEMESTER"][] = $Row["LABEL"];
            $arg["gakki"][] = $Row;
        }
        $result->free();

        //学期数の取得
        $semestercount = $db->getOne(knjz211bQuery::getSemestercount("Z009"));

        //3学期の表示
        if ($semestercount == "3") {
            $arg["3gakki"] = "1";
        }

        //学年コンボ
        $query = knjz211bQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);

        //データ取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["no_useCurriculumcd"] = "1";
        }
        //初期化
        $model->data = array();
        $counter = 0;
        $key = 0;
        $bifKey = "";
        $result = $db->query(knjz211bQuery::selectQuery($model, $semestercount, $assesslevel));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //科目コード、観点コードを配列で取得し、一つ前のSUBCLASSCD、VIEWCDと取り出したSUBCLASSCD、VIEWCDを比較する
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $model->data["SUBCLASSCD"][] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
            } else {
                $model->data["SUBCLASSCD"][] = $row["SUBCLASSCD"];
            }
            $model->data["VIEWCD"][] = $row["VIEWCD"];
            if ($counter > 0) {
                $set_subviewcd = substr($model->data["VIEWCD"][$counter - 1], 0, 2);
                $set_subclasscd = $model->data["SUBCLASSCD"][$counter - 1];
            } else {
                $set_subviewcd = substr($model->data["VIEWCD"][$counter], 0, 2);
                $set_subclasscd = $model->data["SUBCLASSCD"][$counter];
            }
            $get_subviewcd     = substr($row["VIEWCD"], 0, 2);
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $get_subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
            } else {
                $get_subclasscd = $row["SUBCLASSCD"];
            }
            if (($set_subviewcd == $get_subviewcd) && ($set_subclasscd == $get_subclasscd)) {
                $key++;
                if ($key == 1) {
                    $row["VIEWCD"] = "①".$row["VIEWCD"];
                } else if ($key == 2) {
                    $row["VIEWCD"] = "②".$row["VIEWCD"];
                } else if ($key == 3) {
                    $row["VIEWCD"] = "③".$row["VIEWCD"];
                } else if ($key == 4) {
                    $row["VIEWCD"] = "④".$row["VIEWCD"];
                } else if ($key == 5) {
                    $row["VIEWCD"] = "⑤".$row["VIEWCD"];
                } else if ($key == 6) {
                    $row["VIEWCD"] = "⑥".$row["VIEWCD"];
                }
            } else {
                $key = 1;
                $row["VIEWCD"] = "①".$row["VIEWCD"];
            }

            //評定設定チェック
            $hyouteicnt = $db->getOne(knjz211bQuery::getjviewStatLevelMst($model, $row));
            if ($hyouteicnt > 0) {
                $row["HYOUTEICHECK"] = 'レ';
            }

            if ($bifKey !== $row["KEY"]) {
                $cnt = $db->getOne(knjz211bQuery::getSubclassCnt($model, $row));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["KEY"];

            $counter++;
            $arg["data"][] = $row;
        }
        $result->free();

        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz211bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
