<?php

require_once('for_php7.php');

class knjj120aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjj120aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR.'年度';

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->Properties["useClubMultiSchoolKind"] != "1") {
            $arg["schkind"] = "1";
            $query = knjj120aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('list');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1, $model);
        }

        //部クラブコンボ作成
        $model->clublist = ($model->cmd == "list2") ? $model->clublist2 : $model->clublist;
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjj120aQuery::getClubList($model);
        makeCmb($objForm, $arg, $db, $query, "CLUBLIST", $model->clublist, $extra, 1, $model, "blank");
        $send_clublist = ($model->clublist) ? $model->clublist : "all";

        //顧問をしている部活取得（権限：制限付用）
        $stf_club = array();
        $query = knjj120aQuery::getClubList($model, "1");
        $stf_club = $db->getCol($query);

        //記録備考一覧取得
        $taikai = "";
        $query = knjj120aQuery::getList($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["DETAIL_DATE"] = str_replace("-", "/", $row["DETAIL_DATE"]);

            $row["DIV_NAME"] = ($row["DIV"] == "1") ? '個人' : '団体';

            if ($taikai != $row["MEET_NAME"]) {
                $row["ROWSPAN"] = $db->getOne(knjj120aQuery::getList($model, "taikai", $row));
                $arg["ALL_ROWSPAN"] = "1";
            }

            //権限（制限付）
            if ((AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) && !in_array($row["CLUBCD"], $stf_club)) {
            } else {
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $row["DETAIL_DATE"]= "<a href=\"knjj120aindex.php?cmd=edit&CLUBCD=".$row["CLUBCD"]."&DETAIL_SCHKIND=".$row["DETAIL_SCHOOL_KIND"]."&DETAIL_DATE=".$row["DETAIL_DATE"]."&DETAIL_SEQ=".$row["DETAIL_SEQ"]."&DIV=".$row["DIV"]."&SCHREGNO=".$row["SCHREGNO"]."&SEND_CLUBLIST=".$send_clublist."\" target=\"right_frame\">".$row["DETAIL_DATE"]."</a>";
                } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $row["DETAIL_DATE"]= "<a href=\"knjj120aindex.php?cmd=edit&CLUBCD=".$row["CLUBCD"]."&SCHKIND=".$model->schkind."&DETAIL_DATE=".$row["DETAIL_DATE"]."&DETAIL_SEQ=".$row["DETAIL_SEQ"]."&DIV=".$row["DIV"]."&SCHREGNO=".$row["SCHREGNO"]."&SEND_CLUBLIST=".$send_clublist."\" target=\"right_frame\">".$row["DETAIL_DATE"]."</a>";
                } else {
                    $row["DETAIL_DATE"]= "<a href=\"knjj120aindex.php?cmd=edit&CLUBCD=".$row["CLUBCD"]."&DETAIL_DATE=".$row["DETAIL_DATE"]."&DETAIL_SEQ=".$row["DETAIL_SEQ"]."&DIV=".$row["DIV"]."&SCHREGNO=".$row["SCHREGNO"]."&SEND_CLUBLIST=".$send_clublist."\" target=\"right_frame\">".$row["DETAIL_DATE"]."</a>";
                }
            }

            $arg["data"][] = $row;

            $taikai = $row["MEET_NAME"];
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "list" && $model->Properties["use_prg_schoolkind"] == "1"){
            $arg["reload"] = "parent.right_frame.location.href='knjj120aindex.php?cmd=edit&SCHKIND=".$model->schkind."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj120aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
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
