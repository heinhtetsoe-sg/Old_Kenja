<?php

require_once('for_php7.php');

class knjl003aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl003aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボボックスを作成する
        $query = knjl003aQuery::selectYearQuery();
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "year", $model->year, $extra, 1, "");

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //受験校種コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl003aQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //通知書用区分1
        $noticeClassArray = array();
        $query  = knjl003aQuery::getNoticeClass();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $noticeClassArray[$row["VALUE"]] = $row["NAME"];
        }
        $result->free();
        //通知書用区分2
        $noticeKindArray = array();
        $query  = knjl003aQuery::getNoticeKind();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $noticeKindArray[$row["VALUE"]] = $row["NAME"];
        }
        $result->free();

        //表示切替
        $arg["SCHOLARSHIP_LABEL"] = ($model->applicantdiv == '1') ? '授業料（年額）' : '奨学金';
        $arg["SPAN"] = ($model->applicantdiv == '1') ? "rowspan=\"2\"" : "colspan=\"2\"";
        $arg["showSCHOLARSHIP"] = ($model->applicantdiv == '2') ? 1 : "";
        if ($model->applicantdiv == '1') {
            $arg["enroll_fees_cols"] = '1';
        } else {
            $arg["enroll_fees_cols"] = '2';
            $arg["showENROLL_FEES2"] = 1;
        }

        //リスト作成
        $query  = knjl003aQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"             => "edit2",
                          "APPLICANTDIV"    => $row["APPLICANTDIV"],
                          "HONORDIV"         => $row["HONORDIV"]);

            $row["HONORDIV"] = View::alink("knjl003aindex.php", $row["HONORDIV"], "target=\"right_frame\"", $hash);

            $row["CLUB_FLG"] = ($row["CLUB_FLG"] == "1") ? "レ": "";

            $row["NOTICE_CLASS"] = $noticeClassArray[$row["NOTICE_CLASS"]];
            $row["NOTICE_KIND"] = $noticeKindArray[$row["NOTICE_KIND"]];

            if ($row["APPLICANTDIV"] == '1') {
                $row["ENROLL_FEES"] = (strlen($row["ENROLL_FEES"])) ? $row["ENROLL_FEES"].'円' : $row["ENROLL_FEES"];
            } else {
                $row["ENROLL_FEES2"] = (strlen($row["ENROLL_FEES2"])) ? $row["ENROLL_FEES2"].'円' : $row["ENROLL_FEES2"];
            }
            if ($row["APPLICANTDIV"] == '1') {
                $row["SCHOLARSHIP1"] = (strlen($row["SCHOOL_FEES"])) ? $row["SCHOOL_FEES"].'円' : $row["SCHOOL_FEES"];
            } else {
                $row["SCHOLARSHIP1"] = (strlen($row["SCHOLARSHIP1"])) ? $row["SCHOLARSHIP1"].'円' : $row["SCHOLARSHIP1"];
                $row["SCHOLARSHIP2"] = (strlen($row["SCHOLARSHIP2"])) ? $row["SCHOLARSHIP2"].'円' : $row["SCHOLARSHIP2"];
            }

            //特待区分名称
            $row["HONOR_TYPE_NAME"] = $model->honorTypeList[$row["HONOR_TYPE"]]["LABEL"];

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"] = "parent.right_frame.location.href='knjl003aindex.php?cmd=edit"
                           . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl003aForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    if ($name == "year") {
        $value = ($value != "" && $value_flg) ? $value : (CTRL_YEAR + 1);
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
