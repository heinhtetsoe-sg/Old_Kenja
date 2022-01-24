<?php

require_once('for_php7.php');

class knjz412aForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz412aindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $query = knjz412aQuery::getCollegeMst($model);
        $college_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SCHOOL_CD"] = $college_mst["SCHOOL_CD"];
        $arg["SCHOOL_NAME"] = $college_mst["SCHOOL_NAME"];

        //学部コード
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $query = knjz412aQuery::getFacultycd($model);
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->facultycd == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $model->facultycd = ($model->facultycd && $value_flg) ? $model->facultycd : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('chenge_cd')\"";
        $arg["FACULTYCD"] = knjCreateCombo($objForm, "FACULTYCD", $model->facultycd, $opt, $extra, 1);

        $result = $db->query(knjz412aQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();

        //校内推薦用学科コード
        if ($model->Properties["Internal_Recommendation"] == "1") {
            $arg["useInternalRecommendation"] = 1;
        }

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        Query::dbCheckIn($db);

        //学校検索ボタンを作成する
        $extra = "onclick=\"loadwindow('" .REQUESTROOT."/Z/KNJZ412A/knjz412aindex.php?cmd=sub_search',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 350)\"";
        $arg["button"]["btn_schsearch"] = knjCreateBtn($objForm, "btn_schsearch", "学校検索", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_CD");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz412aForm1.html", $arg);
    }
}
