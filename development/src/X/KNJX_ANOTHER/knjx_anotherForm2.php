<?php

require_once('for_php7.php');

class knjx_anotherForm2
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjx_anotherindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //extra
        $extraInteger = "onblur=\"this.value=toInteger(this.value)\"";
        $extraRight = "STYLE=\"text-align: right\"";

        //前籍校履歴データ取得
        if (!isset($model->warning) && isset($model->seq)) {
            $query = knjx_anotherQuery::getAnotherSchoolData($model->schregno, $model->seq);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //検索ボタン
        //出身学校
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_searchfs"] = knjCreateBtn($objForm, "btn_searchfs", "検 索", $extra);
        $extra = "";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);
        $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : "";
        $finschoolname = $db->getOne(knjx_anotherQuery::getFinHighSchoolData($Row["FINSCHOOLCD"]));
        $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;

        //前籍校学生区分
        $query = knjx_anotherQuery::getNamecd($model->year, "E026");
        makeCombo($objForm, $arg, $db, $query, $Row["STUDENT_DIV"], "STUDENT_DIV", 1, "");

        //学科名称
        $arg["data"]["MAJOR_NAME"] = knjCreateTextBox($objForm, $Row["MAJOR_NAME"], "MAJOR_NAME", 50, 120, "");

        //在籍期間
        $arg["data"]["REGD_S_DATE"] = View::popUpCalendar(
            $objForm,
            "REGD_S_DATE",
            str_replace("-", "/", $Row["REGD_S_DATE"]),
            ""
        );

        $arg["data"]["REGD_E_DATE"] = View::popUpCalendar(
            $objForm,
            "REGD_E_DATE",
            str_replace("-", "/", $Row["REGD_E_DATE"]),
            ""
        );
        //期間月数
        $arg["data"]["PERIOD_MONTH_CNT"] = $Row["PERIOD_MONTH_CNT"];

        //休学月数
        $arg["data"]["ABSENCE_CNT"] = knjCreateTextBox($objForm, $Row["ABSENCE_CNT"], "ABSENCE_CNT", 2, 2, $extraRight.$extraInteger);

        //月数
        $arg["data"]["MONTH_CNT"] = $Row["MONTH_CNT"];

        //入学形態
        $query = knjx_anotherQuery::getNamecd($model->year, "E027");
        makeCombo($objForm, $arg, $db, $query, $Row["ENT_FORM"], "ENT_FORM", 1, "");

        //事由
        $arg["data"]["REASON"] = knjCreateTextBox($objForm, $Row["REASON"], "REASON", 50, 150, "");

        //スポーツ振興センター
        $extra = ($Row["ANOTHER_SPORT"] == "1") ? "checked" : "";
        $arg["data"]["ANOTHER_SPORT"] = knjCreateCheckBox($objForm, "ANOTHER_SPORT", "1", $extra, "");
        $query = knjx_anotherQuery::getBaseRemark1($model);
        $arg["data"]["ANOTHER_SPORT_LABEL"] = ($db->getOne($query) == "1") ? "<BR>（加入済み）" : "";

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model, $Row);

        switch (VARS::post("cmd")) {
            case "add":
            case "update":
            case "delete":
                $arg["jscript"] = "window.open('knjx_anotherindex.php?cmd=list','left_frame');";
                break;
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjx_anotherForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{
    //追加ボタンを作成する
    if ($model->auth >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    }

    //更新ボタンを作成する
    if ($model->auth >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    }

    //削除ボタンを作成する
    if ($model->auth >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    }

    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    if ($model->getPrgId) {
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"closeMethod();\"");
    } else {
        $extra  = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "YEAR", $model->year);

    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "PERIOD_MONTH_CNT", $Row["PERIOD_MONTH_CNT"]);
    knjCreateHidden($objForm, "MONTH_CNT", $Row["MONTH_CNT"]);
    knjCreateHidden($objForm, "PROGRAMID", $model->getPrgId);

    //knjCreateHidden($objForm, "AUTH", $model->auth);
}
