<?php

require_once('for_php7.php');

class knje372bForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje372bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $model->oyear = ($model->oyear) ? $model->oyear : CTRL_YEAR;

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knje372bQuery::getList($model, "ONE");
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        if ($model->cmd == "main_kakutei") {
            $Row["SCHOOL_CD"]       = $model->field["SCHOOL_CD"];
            $Row["FACULTYCD"]       = $model->field["FACULTYCD"];
            $Row["DEPARTMENTCD"]    = $model->field["DEPARTMENTCD"];
            $Row["FACULTY_NAME"]    = $db->getOne(knje372bQuery::getFacultyName($model));
            $Row["DEPARTMENT_NAME"] = $db->getOne(knje372bQuery::getDepartmentName($model));
        }

        $Row["LIMIT_COUNT"] = $Row["LIMIT_COUNT_S"] + $Row["LIMIT_COUNT_H"];

        $arg["data"]["YEAR"] = $model->oyear;

        //※推薦枠番号
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["RECOMMENDATION_CD"] = knjCreateTextBox($objForm, $Row["RECOMMENDATION_CD"], "RECOMMENDATION_CD", 4, 4, $extra);

        //専願学科番号
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["DEPARTMENT_S"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_S"], "DEPARTMENT_S", 2, 2, $extra);

        //併願学科番号
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["DEPARTMENT_H"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_H"], "DEPARTMENT_H", 2, 2, $extra);

        //表示順
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["DISP_ORDER"] = knjCreateTextBox($objForm, $Row["DISP_ORDER"], "DISP_ORDER", 2, 2, $extra);

        //学部
        $extra = "";
        $arg["data"]["FACULTY_NAME"] = knjCreateTextBox($objForm, $Row["FACULTY_NAME"], "FACULTY_NAME", 20, 10, $extra);

        //学部略称
        $extra = "";
        $arg["data"]["FACULTY_ABBV"] = knjCreateTextBox($objForm, $Row["FACULTY_ABBV"], "FACULTY_ABBV", 20, 10, $extra);

        //学科
        $extra = "";
        $arg["data"]["DEPARTMENT_NAME"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_NAME"], "DEPARTMENT_NAME", 40, 20, $extra);

        //学科略称
        $extra = "";
        $arg["data"]["DEPARTMENT_ABBV"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_ABBV"], "DEPARTMENT_ABBV", 20, 10, $extra);

        //学科略称2
        $extra = "";
        $arg["data"]["DEPARTMENT_ABBV2"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_ABBV2"], "DEPARTMENT_ABBV2", 6, 3, $extra);

        //学科枠数
        $extra = " onblur=\"this.value=toInteger(this.value);\" disabled ";
        $arg["data"]["LIMIT_COUNT"] = knjCreateTextBox($objForm, $Row["LIMIT_COUNT"], "LIMIT_COUNT", 3, 3, $extra);

        //専願枠数
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["LIMIT_COUNT_S"] = knjCreateTextBox($objForm, $Row["LIMIT_COUNT_S"], "LIMIT_COUNT_S", 2, 2, $extra);

        //併願枠数
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["LIMIT_COUNT_H"] = knjCreateTextBox($objForm, $Row["LIMIT_COUNT_H"], "LIMIT_COUNT_H", 2, 2, $extra);

        //併願有無
        if (array_key_exists("WITHOUT_H_FLG", $Row)) {
            $value = $Row["WITHOUT_H_FLG"];
        } else {
            $value = '1';
        }
        $extra = " id=\"WITHOUT_H_FLG\" ";
        $extra .= ($value == '1') ? " checked " : "";
        $arg["data"]["WITHOUT_H_FLG"] = knjCreateCheckBox($objForm, "WITHOUT_H_FLG", 1, $extra);

        //学科コード
        $extra = "";
        $arg["data"]["DEPARTMENT_LIST_CD"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_LIST_CD"], "DEPARTMENT_LIST_CD", 6, 3, $extra);

        //出力順
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["DEPARTMENT_LIST_ORDER"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_LIST_ORDER"], "DEPARTMENT_LIST_ORDER", 2, 2, $extra);

        //学部
        $extra = "";
        $arg["data"]["FACULTY_LIST_NAME"] = knjCreateTextBox($objForm, $Row["FACULTY_LIST_NAME"], "FACULTY_LIST_NAME", 10, 10, $extra);

        //学科（専攻）
        $extra = "";
        $arg["data"]["DEPARTMENT_LIST_NAME"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_LIST_NAME"], "DEPARTMENT_LIST_NAME", 10, 10, $extra);

        //賢者学校コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["SCHOOL_CD"] = knjCreateTextBox($objForm, $Row["SCHOOL_CD"], "SCHOOL_CD", 8, 8, $extra);
        $arg["data"]["FACULTYCD"] = knjCreateTextBox($objForm, $Row["FACULTYCD"], "FACULTYCD", 3, 3, $extra);
        $arg["data"]["DEPARTMENTCD"] = knjCreateTextBox($objForm, $Row["DEPARTMENTCD"], "DEPARTMENTCD", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = View::setIframeJs();
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            if ($model->cmd != "reset") {
                $arg["reload"]  = "parent.left_frame.location.href='knje372bindex.php?cmd=list';";
            } else {
                $arg["reload"]  = "parent.left_frame.location.href='knje372bindex.php?cmd=list&clear=no';";
            }
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje372bForm2.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //確定ボタン
    $extra = "onclick=\"return btn_submit('main_kakutei');\"";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //学校検索ボタン
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_schsearch"] = knjCreateBtn($objForm, "btn_schsearch", "学校検索", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
