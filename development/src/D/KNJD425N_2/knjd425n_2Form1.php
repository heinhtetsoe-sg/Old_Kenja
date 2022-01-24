<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425n_2Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425n_2index.php", "", "subform");

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();

        //所感タイトル
        $arg["data"]["TITLE"] = $db->getOne(knjd425n_2Query::getHreportGuidanceKindNameHdat($model));

        //更新用の科目コード配列を初期化
        $model->subclasscdarry = array();

        //データ取得
        $query = knjd425n_2Query::getDetailRemark($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SUBCLASSNAME"] !== null && $row["SUBCLASSNAME"] !== "") {
                $list = array();

                $model->subclasscdarry[] = $row["SUBCLASSCD"]; //更新用

                //科目名
                $list["REMARKTITLE"] = $row["SUBCLASSNAME"];
                if ($model->isWarning()) {
                    $remark = $model->remarkarry[$row["SUBCLASSCD"]];
                } else {
                    $remark = $row["REMARK"];
                }
                //データ
                $extra = "id=\"REMARK_".$row["SUBCLASSCD"]."\"";
                $list["REMARK"] = knjCreateTextArea($objForm, "REMARK_".$row["SUBCLASSCD"], 10, 90, "", $extra, $remark);
                $list["EXTFMT"] .= "<font size=2, color=\"red\">(全角45文字X25行まで)</font>";
                knjCreateHidden($objForm, "REMARK_".$row["SUBCLASSCD"]."_KETA", 90);
                knjCreateHidden($objForm, "REMARK_".$row["SUBCLASSCD"]."_GYO", 25);
                KnjCreateHidden($objForm, "REMARK_".$row["SUBCLASSCD"]."_STAT", "statusarea_".$row["SUBCLASSCD"]);

                //前年度「年間まとめ」参照 ボタン
                $extra = " onclick=\"loadwindow('knjd425n_2index.php?";
                $extra .= "cmd=nenkan&SUBCLASSNAME={$row["SUBCLASSNAME"]}&SUBCLASSCD=".$row["SUBCLASSCD"]."'";
                $extra .= ", (event.clientX - 200) + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 750, 350);\" ";
                $list["btn_nenkan"] = knjCreateBtn($objForm, "btn_nenkan_".$row["SUBCLASSCD"], "前年度「年間まとめ」参照", $extra);

                $arg["list"][] = $list;
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        //ボタン
        //「実態」参照
        $extra = " onclick=\"loadwindow('knjd425n_2index.php?";
        $extra .= "cmd=zittai&SEND_selectSchoolKind={$model->selectSchoolKind}&KINDNO={$row["KIND_NO"]}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}&UPDDATE={$model->upddate}&TITLE=(仮)subform1'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 500);\" ";
        $arg["btn_zittai"] = knjCreateBtn($objForm, "btn_zittai", "「実態」参照", $extra);

        //「目指したい自立の姿」参照
        $extra = " onclick=\"loadwindow('knjd425n_2index.php?";
        $extra .= "cmd=ziritu&SEND_selectSchoolKind={$model->selectSchoolKind}&KINDNO={$row["KIND_NO"]}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}&UPDDATE={$model->upddate}&TITLE=(仮)subform1'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 500);\" ";
        $arg["btn_ziritu"] = knjCreateBtn($objForm, "btn_ziritu", "「目指したい自立の姿」参照", $extra);

        //終了ボタンを作成する
        $link = REQUESTROOT."/D/KNJD425N/knjd425nindex.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //更新ボタンを作成
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd425n_2Form1.html", $arg);
    }
}
?>
