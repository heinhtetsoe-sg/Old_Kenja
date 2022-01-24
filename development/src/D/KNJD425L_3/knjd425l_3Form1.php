<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425l_3Form1
{
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425l_3index.php", "", "subform");

        $db = Query::dbCheckOut();

        //画面タイトル
        $query = knjd425l_3Query::getHreportGuidanceKindNameHdat($model, $model->exp_year);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["TITLE"] = $row["KIND_NAME"];

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        $subclassList = array();
        //項目内容取得
        $query = knjd425l_3Query::getDetailSchregSubclassRemark($model, $model->exp_year, $model->selKindNo, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];

            $data["SUBCLASSNAME"] = $row["SUBCLASSNAME"];

            $moji = 45;
            $gyou = 25;
            if ($model->cmd != "check") {
                $row["REMARK"] = $row["REMARK"];
            } else {
                $row["REMARK"] = $model->field["REMARK_".$subclass];
            }
            //テキストエリア
            $extra = "id=\"REMARK_".$subclass."\" aria-label=\"".$row["SUBCLASSNAME"]."\" ";
            $data["REMARK"] = knjCreateTextArea($objForm, "REMARK_".$subclass, $gyou, ($moji * 2), "", $extra, $row["REMARK"]);
            $data["REMARK_SIZE"] = "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";

            if ($model->selKindNo == "03") {
                //前年度「年間まとめ」参照 ボタン
                $extra = " onclick=\"loadwindow('knjd425l_3index.php?";
                $extra .= "cmd=nenkan&SUBCLASSNAME={$row["SUBCLASSNAME"]}&SUBCLASSCD=".$subclass."'";
                $extra .= ", (event.clientX - 200) + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), (event.clientY - 200) + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 750, 500);\" ";
                $data["btn_nenkan"] = knjCreateBtn($objForm, "btn_nenkan_".$subclass, "前年度「年間まとめ」参照", $extra);
            }

            knjCreateHidden($objForm, "REMARK_TITLE_".$subclass, $row["SUBCLASSNAME"]);

            $arg["list"][] = $data;
            $subclassList[] = $subclass;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SUBCLASS_LIST", implode(",", $subclassList));

        /**********/
        /* ボタン */
        /**********/
        if ($model->selKindNo == "03") {
            //「実態」参照
            $extra = " onclick=\"loadwindow('knjd425l_3index.php?";
            $extra .= "cmd=zittai&SEND_selectSchoolKind={$model->selectSchoolKind}&KINDNO={$model->selKindNo}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}&UPDDATE={$model->recordDate}'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 500);\" ";
            $arg["btn_zittai"] = knjCreateBtn($objForm, "btn_zittai", "「実態」参照", $extra);
        }

        //更新
        $extra = "id=\"update\" aria-label=\"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻る
        $link = REQUESTROOT."/D/KNJD425L/knjd425lindex.php?cmd=edit&SEND_PRGID={$model->sendPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd425l_3Form1.html", $arg);
    }
}
?>
