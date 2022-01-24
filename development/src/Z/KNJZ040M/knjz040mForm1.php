<?php

require_once('for_php7.php');

class knjz040mForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz040mindex.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //リスト作成
        $bifkey_natpubpri_cd = "";
        $bifkey_area_div_cd  = "";
        $query = knjz040mQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            if ($bifkey_natpubpri_cd !== $row["NATPUBPRI_CD"]) {
                $cnt = $db->getOne(knjz040mQuery::getNatpubpriCnt($row["NATPUBPRI_CD"]));
                $row["ROWSPAN_NATPUBPRI_CD"] = $cnt > 0 ? $cnt : 1;
            }
            if ($bifkey_area_div_cd !== $row["AREA_DIV_CD"] || $bifkey_natpubpri_cd !== $row["NATPUBPRI_CD"]) { //所在地区分はない場合がある(国公私立のみ)ので所在地区分、国公私立どちらかが変わればという条件
                $cnt = $db->getOne(knjz040mQuery::getAreaDivCnt($row["NATPUBPRI_CD"], $row["AREA_DIV_CD"]));
                $row["ROWSPAN_AREA_DIV_CD"] = $cnt > 0 ? $cnt : 1;
            }
            //更新後その行にスクロールさせる
            if ($row["NATPUBPRI_CD"] == $model->natpubpri_cd && $row["AREA_DIV_CD"] == $model->area_div_cd && $row["AREA_CD"] == $model->area_cd) {
                $row["LINK"] = "<a name=\"target\">　</a><script>location.href='#target';</script>";
            }
            $bifkey_natpubpri_cd = $row["NATPUBPRI_CD"];
            $bifkey_area_div_cd  = $row["AREA_DIV_CD"];
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz040mForm1.html", $arg); 
    }
}
?>
