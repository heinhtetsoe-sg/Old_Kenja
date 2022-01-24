<?php

require_once('for_php7.php');

class knjz068Form1
{
    public function main(&$model)
    {
        $arg["reload"] = "";

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz068index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz068Query::getIBYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBYEAR", $model->ibyear, $extra, 1);

        //前年度からのコピーボタン
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からのコピー", $extra);

        //学年コンボ
        $query = knjz068Query::getIBGrade($model, "list");
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBGRADE", $model->ibgrade, $extra, 1);

        //IBコースコンボ
        $query = knjz068Query::getIBPrgCourse($model, "list");
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $model->ibprg_course, $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "add") {
            $arg["reload"] = "window.open('knjz068index.php?cmd=edit','right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz068Form1.html", $arg);
    }
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $bifKey1 = $bifKey2 = $bifKey3 = "";
    $query = knjz068Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //科目
        $row["IBSUBCLASS_SHOW"] = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"].'<BR><font size="2">'.$row["IBSUBCLASSNAME_ENG"].'</font>';
        //評価区分1
        $row["IBEVAL_DIV1_SHOW"] = ($row["IBEVAL_DIV1"] == "") ? "" : $row["IBEVAL_DIV1"].':'.$row["IBEVAL_DIV1_NAME"];
        //評価区分2
        $row["IBEVAL_DIV2_SHOW"] = ($row["IBEVAL_DIV2"] == "") ? "" : $row["IBEVAL_DIV2"].':'.$row["IBEVAL_DIV2_NAME"];

        //列結合
        if ($bifKey1 !== $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"]) {
            $cnt1 = $db->getOne(knjz068Query::getRowDataCnt($row, "1"));
            $row["ROWSPAN1"] = $cnt1 > 0 ? $cnt1 : 1;
        }
        if ($bifKey2 !== $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"].'-'.$row["IBEVAL_DIV1"]) {
            $cnt2 = $db->getOne(knjz068Query::getRowDataCnt($row, "2"));
            $row["ROWSPAN2"] = $cnt2 > 0 ? $cnt2 : 1;
        }
        if ($bifKey3 !== $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"].'-'.$row["IBEVAL_DIV1"].'-'.$row["IBEVAL_DIV2"]) {
            $cnt3 = $db->getOne(knjz068Query::getRowDataCnt($row, "3"));
            $row["ROWSPAN3"] = $cnt3 > 0 ? $cnt3 : 1;
        }
        $bifKey1 = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"];
        $bifKey2 = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"].'-'.$row["IBEVAL_DIV1"];
        $bifKey3 = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"].'-'.$row["IBEVAL_DIV1"].'-'.$row["IBEVAL_DIV2"];

        $arg["data"][] = $row;
    }
    $result->free();
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
