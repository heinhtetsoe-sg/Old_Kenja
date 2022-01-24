<?php

require_once('for_php7.php');

class knjz411aForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz411aindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $query = knjz411aQuery::getCollegeMst($model);
        $college_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SCHOOL_CD"] = $college_mst["SCHOOL_CD"];
        $arg["SCHOOL_NAME"] = $college_mst["SCHOOL_NAME"];

        $result = $db->query(knjz411aQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();

        //校内推薦用学部コード
        if ($model->Properties["Internal_Recommendation"] == "1") {
            $arg["useInternalRecommendation"] = 1;
        }

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        Query::dbCheckIn($db);

        //学校検索ボタンを作成する
        $extra = "onclick=\"loadwindow('" .REQUESTROOT."/Z/KNJZ411A/knjz411aindex.php?cmd=sub_search',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 350)\"";
        $arg["button"]["btn_schsearch"] = knjCreateBtn($objForm, "btn_schsearch", "学校検索", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_CD");

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "list_from_right") {
            $arg["reload"]  = "parent.right_frame.location.href='knjz411aindex.php?cmd=edit';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz411aForm1.html", $arg);
    }
}
