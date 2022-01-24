<?php

require_once('for_php7.php');

class knjz252Form1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz252index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["unUsePrgStampInei"] == "1") {
            $arg["unUseInei"] = "1";
        } else {
            $arg["useInei"] = "1";
        }

        //ヘッダー表示
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["useSchool_KindField"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjz252Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //データCNT
        $query = knjz252Query::ReadQueryCnt($model);
        $result = $db->query($query);
        $dataCntArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataCntArray[$row["PROGRAMID"]]["CNT"] = $row["CNT"];
            $dataCntArray[$row["PROGRAMID"]]["DISP_FLG"] = false;
        }
        $result->free();

        //リスト内データ取得
        $query = knjz252Query::ReadQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            if (!$dataCntArray[$row["PROGRAMID"]]["DISP_FLG"]) {
                $row["ROWSPAN"] = $dataCntArray[$row["PROGRAMID"]]["CNT"];
                $dataCntArray[$row["PROGRAMID"]]["DISP_FLG"] = true;
            }
            //権限
            $row["PROGRAMID"] = View::alink("knjz252index.php", $row["PROGRAMID"], "target=right_frame",
                                                array("cmd"              => "edit",
                                                      "SEND_PROGRAMID"   => $row["PROGRAMID"],
                                                      "SEND_SEQ"         => $row["SEQ"]
                                                      )
                                           );
            if ($row["FILE_NAME"]) {
                $row["FILE_NAME"] = $row["FILE_NAME"]."(".$row["STAFFNAME"].")";
            }
            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz252Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
